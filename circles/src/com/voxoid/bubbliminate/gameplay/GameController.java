package com.voxoid.bubbliminate.gameplay;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.AllInputMultiplexer;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.IAllInputProcessor;
import com.voxoid.bubbliminate.ITimeUpdatable;
import com.voxoid.bubbliminate.Vector2Util;
import com.voxoid.bubbliminate.core.Angle;
import com.voxoid.bubbliminate.core.GameSerializer;
import com.voxoid.bubbliminate.core.ai.CpuPlayer;
import com.voxoid.bubbliminate.core.ai.ICpuPlayer;
import com.voxoid.bubbliminate.core.ai.minimax.IGameState;
import com.voxoid.bubbliminate.core.model.CircleUtil;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.model.MutableGameStateDiff;
import com.voxoid.bubbliminate.core.rules.GrowMove;
import com.voxoid.bubbliminate.core.rules.IMove;
import com.voxoid.bubbliminate.core.rules.MoveMove;
import com.voxoid.bubbliminate.core.rules.MoveType;
import com.voxoid.bubbliminate.core.rules.RepetitionGuard;
import com.voxoid.bubbliminate.core.rules.SplitMove;


/**
 * Controls user interaction between view, game state, and input devices
 * 
 * @author Joel
 *
 */
public class GameController extends Actor implements IGameController {

	private static final Logger LOGGER = Logger.getLogger(GameController.class);

	private static final float MOVE_SFX_VOLUME = 0.75f;
	
	private GameView view;
	
	private IGame game;
	private GameplayState currState;
	
	/** Current move type. */
	private MoveType currMoveType;
	
	/** Currently-selected or -adjusted circle. */
	private ICircle currCircle;
	
	/** Current (tentative) move, based on the control point for the current move type. */
	private IMove currMove;
	
	/** Game state resulting from current (tentative) move. */
	private ICirclesGameState tentativeState;
	private Vector2 currControlPoint[] = new Vector2[MoveType.values().length];
	
	private IAllInputProcessor currInputProc;
	private ControllerListener currControllerListener;
	private SelectionInputProcessor selectionInputProc;
	private IAllInputProcessor adjustingInputProc;
	private IAllInputProcessor doneInputProc;
	private IAllInputProcessor generalInputProc;

	private Future<IMove> futureMove;
	
	private RepetitionGuard repetitionGuard = new RepetitionGuard(3);
	private RepetitionGuard repetitionWarner = new RepetitionGuard(2);
	private boolean drawWarningShown = false;
	
	
	
	public GameController(IGame game, GameView view) {
		Validate.notNull(game);
		this.game = game;
		Validate.notNull(view);
		this.view = view;
		
		currMoveType = MoveType.Move;
		currCircle = null;
		currMove = null;
		resetControlPoints();
		
		generalInputProc = new GeneralGameInputProcessor(this);
		selectionInputProc = new SelectionInputProcessor(this);
		adjustingInputProc = new AdjustInputProcessor(this);
		doneInputProc = new AllInputAdapter();
		
		currCircle = game.getCurPlayerState().getCircles().iterator().next();
		
		setState(GameplayState.SELECTING);
		
		view.init(this);
	}
	
	public void update(float delta) {
		if (currControllerListener != null && currControllerListener instanceof ITimeUpdatable
				&& CirclesGlobal.input.hasFocus(currInputProc)) {
			
			((ITimeUpdatable) currControllerListener).update(delta);
		}
		
		if (futureMove != null) {
			if (futureMove.isDone() && CirclesGlobal.input.hasFocus(currInputProc)) {
				try {
					currMove = futureMove.get();
					futureMove = null;
					currMoveType = currMove.getMoveType();
					
					MutableGameStateDiff diff = new MutableGameStateDiff();
					tentativeState = currMove.make(game.getCurrentState(), diff);
					view.update(tentativeState, currState, currMove, game.getCurPlayer(), currCircle, diff);
					completeMove();
				} catch (InterruptedException e) {
					LOGGER.warn("Future move thread interrupted");
				} catch (ExecutionException e) {
					LOGGER.error("While getting future move", e);
					e.printStackTrace();
					LOGGER.info("Re-trying move");
					setState(GameplayState.SELECTING);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#exit()
	 */
	@Override
	public void exit() {
		if (getState() != GameplayState.WON && getState() != GameplayState.DRAW) {
			view.promptExit(new Runnable() {
				public void run() {
					doExit();
				}
			});
		} else {
			doExit();
		}
	}
	
	private void doExit() {
		view.setBanner("", null, true);	// Move bouncing banner out
		
		if (currInputProc != null) {
			CirclesGlobal.input.enable(false);
			Controllers.removeListener(currInputProc);
		}
		
		
		view.exit(new Runnable() {
			public void run() {
				for (IPlayer player : game.getPlayers()) {
					if (player instanceof CpuPlayer) {
						((CpuPlayer) player).dispose();
					}
				}
			}
		});
	}
	
	private String getPlayerIdText() {
		return getPlayerIdText(game.getCurPlayer());
	}
	
	private String getPlayerIdText(IPlayer player) {
		return "Player " + (player.getIndex() + 1);		
	}
	
	private void setState(GameplayState state) {
		if (state == GameplayState.SELECTING && game.getCurPlayer() instanceof ICpuPlayer) {
			this.currState = GameplayState.WAITING_FOR_PLAYER;
		} else {
			this.currState = state;
		}
		
		// Select correct input proc
		IAllInputProcessor stateInputProc;
		switch (currState) {
		case WAITING_FOR_PLAYER:
			stateInputProc = generalInputProc;
			view.setHelpText("Waiting for CPU move...");
			final IPlayer player = game.getCurPlayer();
			futureMove = ((ICpuPlayer) game.getCurPlayer()).chooseMove(game.getCurrentState());
			break;
		case SELECTING:
			
			stateInputProc = selectionInputProc;
			// TODO: Select their previously-selected circle if it's still alive
			IPlayerState ps = game.getCurPlayerState();
			currCircle = CircleUtil.findMatchingCircle(currCircle, ps.getCircles(),
					game.getConfig().getMinCircleRadius() / 10f);
			if (currCircle == null) {
				currCircle = ps.getCircles().iterator().next();
			}
			
			resetControlPoints();
			view.showControlsHelp();
			break;
		case ADJUSTING:
			resetControlPoints();
			onControlPointChanged();
			
			stateInputProc = adjustingInputProc;
			setMoveType(currMoveType);
			view.showControlsHelp();
			break;
		case ANIMATING:
			stateInputProc = generalInputProc;
			resetControlPoints();
			view.setHelpText("");
			break;
		case WON:
			stateInputProc = doneInputProc;
			view.setBanner(getPlayerIdText() + " Wins!", game.getCurPlayer(), true);
			view.setHelpText("");
			if (game.getCurPlayer() instanceof CpuPlayer) {
				Assets.loseMusic.play();
			} else {
				Assets.winMusic.play();
			}
			break;
		case DRAW:
			stateInputProc = doneInputProc;
			view.setBanner("Draw!", null, true);
			view.setHelpText("");
			Assets.loseMusic.play();
			break;
		default:
			throw new UnsupportedOperationException("Game controller state not yet supported in setState()");
		}
		
		IAllInputProcessor oldProc = currInputProc;
		currControllerListener = stateInputProc;
		currInputProc = new AllInputMultiplexer(generalInputProc, stateInputProc);
		CirclesGlobal.input.replace(oldProc, currInputProc);
		
		view.onControllerStateChanged(game.getCurrentState(), currState, currMove, game.getCurPlayer(), currCircle);
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#getState()
	 */
	@Override
	public GameplayState getState() {
		return currState;
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#getGame()
	 */
	@Override
	public IGame getGame() {
		return game;
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#getCurrCircle()
	 */
	@Override
	public ICircle getCurrCircle() {
		return currCircle;
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#setCurrCircle(com.voxoid.bubbliminate.core.model.ICircle)
	 */
	@Override
	public void setCurrCircle(ICircle circle) {
		currCircle = circle;
		view.update(game.getCurrentState(), currState, currMove, game.getCurPlayer(), circle, null);
	}

	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#getCurrPlayer()
	 */
	@Override
	public IPlayer getCurrPlayer() {
		return game.getCurPlayer();
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#getCurrMoveType()
	 */
	@Override
	public MoveType getCurrMoveType() {
		return currMoveType;
	}


	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#setMoveType(com.voxoid.bubbliminate.core.rules.MoveType)
	 */
	@Override
	public void setMoveType(MoveType moveType) {
		if (moveType == MoveType.Split && !SplitMove.canBePerformedOn(currCircle, game.getConfig())) {
			if (currState != GameplayState.SELECTING) {
				setState(GameplayState.SELECTING);
			}
			Assets.nonoSound.play(MOVE_SFX_VOLUME);
			view.setHelpText("That circle is too small to split any further. Try a different move.", true);
		} else {
			if (currState == GameplayState.SELECTING) {
				selectCircle(currCircle);
			}
			if (currState == GameplayState.ADJUSTING) {
				currMoveType = moveType;

				Assets.selectSound.play(MOVE_SFX_VOLUME);
				currMove = createMove(moveType);
				view.onMoveTypeChanged(game.getCurrentState(), currState,
						currMove, game.getCurPlayer(), currCircle);
				updateTentativeMove();
			}
		}
	}
	
	/**
	 * Creates a {@link IMove} for the given move type, using the current control point.
	 * 
	 * @param moveType
	 * @return The {@link IMove}, or null if the move is not legal.
	 */
	private IMove createMove(MoveType moveType) {
		IMove move;
		Vector2 controlPoint = currControlPoint[moveType.ordinal()].cpy();
		switch (moveType) {
		
		case Move: {
			MoveMove m = new MoveMove(game.getConfig(), game.getCurrentState(), game.getCurPlayer(), currCircle,
					currCircle.getLocation().add(Vector2Util.toCoreVector2(controlPoint)));
			move = m;
			currControlPoint[MoveType.Move.ordinal()] = Vector2Util.fromCoreVector2(m.getNewLocation().subtract(currCircle.getLocation()));
			break;
		}
		case Grow: {
			GrowMove m = new GrowMove(game.getConfig(), game.getCurrentState(), game.getCurPlayer(), currCircle,
					controlPoint.len());
			move = m;
			currControlPoint[MoveType.Grow.ordinal()] = new Vector2(m.getNewRadius(), 0);
			break;
		}
		case Split: {
			if (SplitMove.canBePerformedOn(currCircle, game.getConfig())) {
				SplitMove m = new SplitMove(game.getConfig(), game.getCurrentState(), game.getCurPlayer(), currCircle,
						Angle.fromDegrees(controlPoint.angle()));
				move = m;
				currControlPoint[MoveType.Split.ordinal()] = Vector2Util.fromCoreVector2(
						m.getSplitAngle().toVector2());
			} else {
				//move = new MoveMove(game, game.getCurPlayer(), currCircle, currCircle.getLocation());
				move = null;
			}
			break;
		}
		default:
			move = null;
			break;
		}
		
		return move;
	}
	
	private void updateTentativeMove() {
		IMove move = createMove(currMoveType);
		if (move != null) {
			currMove = move;
			MutableGameStateDiff diff = new MutableGameStateDiff();
			tentativeState = currMove.make(game.getCurrentState(), diff);
			view.update(tentativeState, currState, currMove, game.getCurPlayer(), currCircle, diff);
		} else {
			Gdx.app.error("", "Attempted to updateTentativeMove() with Split on circle too small");
			currMoveType = MoveType.Grow;
		}
	}
	
	private void resetControlPoints() {
		currControlPoint[MoveType.Move.ordinal()] = Vector2.Zero;
		currControlPoint[MoveType.Grow.ordinal()] = Vector2Util.right().scl(
				currCircle == null ? 1f : currCircle.getRadius());
		currControlPoint[MoveType.Split.ordinal()] = Vector2Util.up();
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#selectCircle(com.voxoid.bubbliminate.core.model.ICircle)
	 */
	@Override
	public void selectCircle(ICircle circle) {
		if (currState == GameplayState.SELECTING) {
			currCircle = circle;
			Assets.selectSound.play(MOVE_SFX_VOLUME);
			if (currMoveType == MoveType.Split && !SplitMove.canBePerformedOn(currCircle, game.getConfig())) {
				currMoveType = MoveType.Grow;
			}
			setState(GameplayState.ADJUSTING);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#setControlPoint(com.badlogic.gdx.math.Vector2)
	 */
	@Override
	public void setControlPoint(Vector2 point) {
		this.currControlPoint[currMoveType.ordinal()] = point.cpy();
		onControlPointChanged();
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#getControlPoint()
	 */
	@Override
	public Vector2 getControlPoint() {
		return currControlPoint[currMoveType.ordinal()].cpy();
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#moveControlPoint(com.badlogic.gdx.math.Vector2)
	 */
	@Override
	public void moveControlPoint(Vector2 delta) {
		currControlPoint[currMoveType.ordinal()].add(delta);
		onControlPointChanged();
	}
	
	private void onControlPointChanged() {
		updateTentativeMove();
	}
	
	
	public void confirmMove() {
		completeMove();
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#completeMove()
	 */
	@Override
	public void completeMove() {
		setGameState(tentativeState);
		game.getHistory().add(tentativeState, currMove);
		LOGGER.info("completeMove: " + currMove.toString());
		
		switch (currMove.getMoveType()) {
		case Move:
			Assets.moveSound.play(MOVE_SFX_VOLUME);
			break;
		case Grow:
			Assets.growSound.play(MOVE_SFX_VOLUME);
			break;
		case Split:
			Assets.splitSound.play(MOVE_SFX_VOLUME);
			break;
		}
		setState(GameplayState.ANIMATING);
	}

	private void setGameState(final ICirclesGameState gameState) {
		
		// Notify user of eliminated players
		ICirclesGameState oldState = game.getCurrentState();
		for (IPlayerState ps : gameState.getPlayerStates()) {
			if (ps.getNumCircles() == 0 && oldState.getPlayerState(ps.getPlayer().getIndex()).getNumCircles() > 0) {
				playerEliminated(ps.getPlayer());
			}
		}
		
		game.setCurrentState(gameState);
	}

	private void playerEliminated(IPlayer player) {
		view.setBanner(getPlayerIdText(player) + " Eliminated!", player, false);
		// TODO: play sound
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#doneAnimatingMove()
	 */
	@Override
	public void doneAnimatingMove() {
		IPlayer winner = game.getWinner();
		if (winner != null) {
			setState(GameplayState.WON);
		} else if (!repetitionGuard.isOk(game.getCurrentState())) {
			setState(GameplayState.DRAW);
		} else {
			setState(GameplayState.SELECTING);
			if (!repetitionWarner.isOk(game.getCurrentState())) {
				view.setBanner("Threefold repetition rule: One more repetition of this position for Player " +
						(game.getCurPlayer().getIndex() + 1) + " will end the game in a draw.",
						null, false);
				drawWarningShown = true;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#undoLastMove()
	 */
	@Override
	public void undoLastMove() {
		
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#redoLastMove()
	 */
	@Override
	public void redoLastMove() {
		
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#selectDifferentCircle()
	 */
	@Override
	public void selectDifferentCircle() {
		setState(GameplayState.SELECTING);
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#cycleSelectionLeft()
	 */
	@Override
	public void cycleSelectionLeft() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextLeft(
				getCurrCircle(), playerCircles));
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#cycleSelectionRight()
	 */
	@Override
	public void cycleSelectionRight() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextRight(
				getCurrCircle(), playerCircles));
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#cycleSelectionUp()
	 */
	@Override
	public void cycleSelectionUp() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextAbove(
				getCurrCircle(), playerCircles));
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#cycleSelectionDown()
	 */
	@Override
	public void cycleSelectionDown() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextBelow(
				getCurrCircle(), playerCircles));
	}
	

	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#showInstructions()
	 */
	@Override
	public void showInstructions() {
		view.showInstructions();
	}

	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#toggleCircleLabels()
	 */
	@Override
	public void toggleCircleLabels() {
		view.toggleCircleLabels();
	}

	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameController#toggleCircleLabels()
	 */
	@Override
	public void toggleRangeRings() {
		view.toggleRangeRings();
	}
	public void handleCannotSplit() {
	}
	
}
