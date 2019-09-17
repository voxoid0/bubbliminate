package com.voxoid.bubbliminate.gameplay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.AllInputMultiplexer;
import com.voxoid.bubbliminate.AllInputToInputProcessorAdaptor;
import com.voxoid.bubbliminate.AppRestorer;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.IAllInputProcessor;
import com.voxoid.bubbliminate.ITimeUpdatable;
import com.voxoid.bubbliminate.Vector2Util;
import com.voxoid.bubbliminate.PlayerConfig.Type;
import com.voxoid.bubbliminate.core.Angle;
import com.voxoid.bubbliminate.core.ai.CirclesAreaCalc;
import com.voxoid.bubbliminate.core.ai.ComplexGamePositionEvaluator;
import com.voxoid.bubbliminate.core.ai.CpuPlayer;
import com.voxoid.bubbliminate.core.ai.GamePositionEvaluator;
import com.voxoid.bubbliminate.core.ai.ICpuPlayer;
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
import com.voxoid.bubbliminate.core.util.Function1;
import com.voxoid.bubbliminate.ui.MenuUtils;

/**
 * Controls user interaction between view, game state, and input devices
 * 
 * @author Joel
 * 
 */
public class GameControllerTouch extends Actor implements IGameController {

	private static final Logger LOGGER = Logger.getLogger(GameControllerTouch.class);

	private static final float MOVE_SFX_VOLUME = 0.75f;

	private GameViewTouch view;

	private IGame game;
	private GameplayState currState;

	/** Current move type. */
	private MoveType currMoveType;

	/** Currently-selected or -adjusted circle. */
	private ICircle currCircle;

	/**
	 * Current (tentative) move, based on the control point for the current move
	 * type.
	 */
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
	private IAllInputProcessor generalGestureProc;
	private IAllInputProcessor touchMoveInputProc;

	private Future<IMove> futureMove;

	private RepetitionGuard repetitionGuard = new RepetitionGuard(3);
	private RepetitionGuard repetitionWarner = new RepetitionGuard(2);
	private boolean showingInstructions = false;

	public GameControllerTouch(IGame game, GameViewTouch view) {
		Validate.notNull(game);
		this.game = game;
		Validate.notNull(view);
		this.view = view;

		currMoveType = MoveType.Move;
		currCircle = null;
		currMove = null;
		resetControlPoints();

		generalInputProc = new GeneralGameInputProcessor(this);
		GeneralGestureListener gestureListener = new GeneralGestureListener(view);
		generalGestureProc = new AllInputToInputProcessorAdaptor(new GestureDetector(
				gestureListener));
		selectionInputProc = new SelectionInputProcessor(this);
		adjustingInputProc = new AdjustInputProcessor(this);
		touchMoveInputProc = new TouchMoveInputProcessing(this, view);

		doneInputProc = new AllInputAdapter();

		currCircle = game.getCurPlayerState().getCircles().iterator().next();

		setState(GameplayState.WAITING_FOR_MOVE);

		view.init(this);

	}

	public void update(float delta) {
		if (currControllerListener != null && currControllerListener instanceof ITimeUpdatable
				&& CirclesGlobal.input.hasFocus(currInputProc)) {

			((ITimeUpdatable) currControllerListener).update(delta);
		}

		// See if CPU move was in progress and is now complete; if complete,
		// complete the CPU's move
		if (futureMove != null) {
			if (futureMove.isDone() && CirclesGlobal.input.hasFocus(currInputProc) && !showingInstructions) {
				try {
					currMove = futureMove.get();
					futureMove = null;
					currMoveType = currMove.getMoveType();

					MutableGameStateDiff diff = new MutableGameStateDiff();
					tentativeState = currMove.make(game.getCurrentState(), diff);
					view.update(tentativeState, currState, currMove, game.getCurPlayer(),
							currCircle, diff);
					completeMove();
				} catch (InterruptedException e) {
					LOGGER.warn("Future move thread interrupted");
				} catch (ExecutionException e) {
					LOGGER.error("While getting future move", e);
					e.printStackTrace();
					LOGGER.info("Re-trying move");
					setState(GameplayState.WAITING_FOR_MOVE);
				}
			}
		}
	}

	public void exit() {
		if (getState() != GameplayState.WON && getState() != GameplayState.DRAW) {
			view.promptExit(new Runnable() {
				public void run() {
					doExit(false);
				}
			});
		} else {
			doExit(false);
		}
	}

	private void doExit(boolean playAgain) {
		view.setBanner("", null, true); // Move bouncing banner out

		if (currInputProc != null) {
			CirclesGlobal.input.enable(false);
			Controllers.removeListener(currInputProc);
		}

		view.exit(new Runnable() {
			public void run() {
				logGameEnded();
				dispose();
			}
		}, playAgain);
	}

	private void logGameEnded() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("winner", game.getWinner() == null ? "" : Integer.toString(game.getWinner().getIndex()+1));
		params.put("nextPlayerToMove", Integer.toString(game.getCurrentState().getPlayerToMove()+1));
		params.put("numCircles", Integer.toString(game.getCurrentState().getNumCircles()));
		params.put("numMoves", Integer.toString(game.getHistory().getHistory().size()));
		params.put("numMovesPerPlayer", Float.toString(game.getHistory().getHistory().size() / game.getNumPlayers()));
		if (game.getWinner() != null) {
			params.put("cpuWon", game.getConfig().getPlayerConfigs().get(game.getWinner().getIndex()).type == Type.CPU ? "1" : "0");
		}
		String circlesPerPlayer = "";
		for (IPlayerState ps : game.getCurrentState().getPlayerStates()) {
			circlesPerPlayer = circlesPerPlayer + Integer.toString(ps.getNumCircles()) + ",";
		}
		params.put("numCirclesPerPlayer", circlesPerPlayer);
		CirclesGlobal.flurry.logEvent("Exit Game", params);
	}
	
	private String getPlayerIdText() {
		return getPlayerIdText(game.getCurPlayer());
	}

	private String getPlayerIdText(IPlayer player) {
		return "Player " + (player.getIndex() + 1);
	}

	private void setState(GameplayState state) {
		if ((state == GameplayState.SELECTING || state == GameplayState.WAITING_FOR_MOVE)
				&& game.getCurPlayer() instanceof ICpuPlayer) {
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
		case WAITING_FOR_MOVE:

			stateInputProc = touchMoveInputProc;
			// TODO: Select their previously-selected circle if it's still alive
			selectInitialCircle();

			resetControlPoints();
			view.showControlsHelp();
			// view.setEnvirTransform(new SimpleTransform2((float)(Math.random()
			// + 2) * 20, (float) (Math.random() - 0.5) * 25,
			// (float)(Math.random() - 0.5) * 25));
			break;
		case SELECTING:

			stateInputProc = selectionInputProc;
			selectInitialCircle();

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
			promptPlayAgain();
			break;
		case DRAW:
			stateInputProc = doneInputProc;
			view.setBanner("Draw!", null, true);
			view.setHelpText("");
			Assets.loseMusic.play();
			promptPlayAgain();
			break;
		default:
			throw new UnsupportedOperationException(
					"Game controller state not yet supported in setState()");
		}

		IAllInputProcessor oldProc = currInputProc;
		currControllerListener = stateInputProc;
		currInputProc = new AllInputMultiplexer(generalInputProc, generalGestureProc,
				stateInputProc, new AllInputToInputProcessorAdaptor(view.getTheStage()));
		CirclesGlobal.input.replace(oldProc, currInputProc);

		view.onControllerStateChanged(game.getCurrentState(), currState, currMove,
				game.getCurPlayer(), currCircle);
	}

	private void promptPlayAgain() {
		view.promptPlayAgain(new Function1<Boolean>() {
			public void run(Boolean playAgain) {
				doExit((boolean) playAgain);
			}
		});
	}

	/**
	 * Sets the current circle to the one that should be initially selected for
	 * the current player. This is for when entering the selection state, so
	 * that the correct circle is initially selected.
	 */
	private void selectInitialCircle() {
		IPlayerState ps = game.getCurPlayerState();
		if (currCircle != null) {
			currCircle = CircleUtil.findMatchingCircle(currCircle, ps.getCircles(), game
					.getConfig().getMinCircleRadius() / 10f);
		}
		if (currCircle == null) {
			currCircle = ps.getCircles().iterator().next();
		}
	}

	public GameplayState getState() {
		return currState;
	}

	public IGame getGame() {
		return game;
	}

	public ICircle getCurrCircle() {
		return currCircle;
	}

	/**
	 * In selection state this sets the currently-highlighted circle
	 */
	public void setCurrCircle(ICircle circle) {
		currCircle = circle;
		view.update(game.getCurrentState(), currState, currMove, game.getCurPlayer(), circle, null);
	}

	public IPlayer getCurrPlayer() {
		return game.getCurPlayer();
	}

	public MoveType getCurrMoveType() {
		return currMoveType;
	}

	/**
	 * Sets the current move type. If in SELECTING state, the current circle
	 * will be selected first.
	 * 
	 * @param moveType
	 */
	public void setMoveType(MoveType moveType) {
		if (moveType == MoveType.Split && !SplitMove.canBePerformedOn(currCircle, game.getConfig())) {
			handleCannotSplit();
		} else {
			if (currState == GameplayState.SELECTING || currState == GameplayState.WAITING_FOR_MOVE) {
				selectCircle(currCircle);
			}
			if (currState == GameplayState.ADJUSTING || currState == GameplayState.WAITING_FOR_MOVE) {
				currMoveType = moveType;

				currMove = createMove(moveType);
				view.onMoveTypeChanged(game.getCurrentState(), currState, currMove,
						game.getCurPlayer(), currCircle);
				updateTentativeMove();

				switch (moveType) {
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
			}
		}
	}

	@Override
	public void handleCannotSplit() {
		if (currState != GameplayState.WAITING_FOR_MOVE) {
			setState(GameplayState.WAITING_FOR_MOVE);
		}
		Assets.nonoSound.play(MOVE_SFX_VOLUME);
		view.showCannotSplitMessage();
	}

	/**
	 * Creates a {@link IMove} for the given move type, using the current
	 * control point.
	 * 
	 * @param moveType
	 * @return The {@link IMove}, or null if the move is not legal.
	 */
	private IMove createMove(MoveType moveType) {
		IMove move;
		Vector2 controlPoint = currControlPoint[moveType.ordinal()].cpy();
		switch (moveType) {

		case Move: {
			MoveMove m = new MoveMove(game.getConfig(), game.getCurrentState(),
					game.getCurPlayer(), currCircle, currCircle.getLocation().add(
							Vector2Util.toCoreVector2(controlPoint)));
			move = m;
			currControlPoint[MoveType.Move.ordinal()] = Vector2Util.fromCoreVector2(m
					.getNewLocation().subtract(currCircle.getLocation()));
			break;
		}
		case Grow: {
			GrowMove m = new GrowMove(game.getConfig(), game.getCurrentState(),
					game.getCurPlayer(), currCircle, controlPoint.len());
			move = m;
			currControlPoint[MoveType.Grow.ordinal()] = new Vector2(m.getNewRadius(), 0);
			break;
		}
		case Split: {
			if (SplitMove.canBePerformedOn(currCircle, game.getConfig())) {
				SplitMove m = new SplitMove(game.getConfig(), game.getCurrentState(),
						game.getCurPlayer(), currCircle, Angle.fromDegrees(controlPoint.angle()));
				move = m;
				currControlPoint[MoveType.Split.ordinal()] = Vector2Util.fromCoreVector2(m
						.getSplitAngle().toVector2());
			} else {
				// move = new MoveMove(game, game.getCurPlayer(), currCircle,
				// currCircle.getLocation());
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

	public void selectCircle(ICircle circle) {
		if (currState == GameplayState.SELECTING) {
			currCircle = circle;
			Assets.selectSound.play(MOVE_SFX_VOLUME);
			if (currMoveType == MoveType.Split
					&& !SplitMove.canBePerformedOn(currCircle, game.getConfig())) {
				currMoveType = MoveType.Grow;
			}
			setState(GameplayState.ADJUSTING);
		}
	}

	/**
	 * Control point is relative to the current circle; i.e., (0,0) is at the
	 * center of the current circle.
	 */
	public void setControlPoint(Vector2 point) {
		this.currControlPoint[currMoveType.ordinal()] = point.cpy();
		onControlPointChanged();
	}

	public Vector2 getControlPoint() {
		return currControlPoint[currMoveType.ordinal()].cpy();
	}

	public void moveControlPoint(Vector2 delta) {
		currControlPoint[currMoveType.ordinal()].add(delta);
		onControlPointChanged();
	}

	private void onControlPointChanged() {
		updateTentativeMove();
	}

	public void confirmMove() {
		if (tentativeState != null) {
			view.zoomOutToFullView();
			view.promptKeepMove(new Function1<Boolean>() {
				public void run(Boolean keep) {
					if (keep) {
						completeMove();
					} else {
						view.zoomInToPreviousView();
						selectDifferentCircle();
					}
				}
			});
		}
	}

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
		
		// TODO: remove: game position evaluations
//		GamePositionEvaluator eval = new GamePositionEvaluator();
//		System.out.println("Old Eval:");
//		for (int p = 0; p < game.getNumPlayers(); p++) {
//			System.out.print(String.format("%8.3f", eval.calculatePlayerStrength(game.getCurrentState(), p)));
//		}
//		System.out.println("");
		ComplexGamePositionEvaluator eval2 = new ComplexGamePositionEvaluator(new CirclesAreaCalc(game.getMinCircleRadius() / 2f));
		System.out.println("New Eval:");
		for (int p = 0; p < game.getNumPlayers(); p++) {
			System.out.print(String.format("%8.3f", eval2.calculatePlayerStrength(game.getCurrentState(), p)));
		}
		System.out.println("");
	}

	private void setGameState(final ICirclesGameState gameState) {

		// Notify user of eliminated players
		ICirclesGameState oldState = game.getCurrentState();
		for (IPlayerState ps : gameState.getPlayerStates()) {
			if (ps.getNumCircles() == 0
					&& oldState.getPlayerState(ps.getPlayer().getIndex()).getNumCircles() > 0) {
				playerEliminated(ps.getPlayer());
			}
		}

		game.setCurrentState(gameState);
		
		// If game is over, clear out game state so that reloading doesn't continue a game
		if (gameState.gameIsOver()) {
			CirclesGlobal.appRestorer.clearRestoreState();
			
		// Otherwise save the game state!
		} else {
			CirclesGlobal.appRestorer.saveState(GameScreen.class, game);
		}
	}

	private void playerEliminated(IPlayer player) {
		view.setBanner(getPlayerIdText(player) + " Eliminated!", player, false);
		// TODO: play sound
	}

	public void doneAnimatingMove() {
		IPlayer winner = game.getWinner();
		if (winner != null) {
			setState(GameplayState.WON);
		} else if (!repetitionGuard.isOk(game.getCurrentState())) {
			setState(GameplayState.DRAW);
		} else {
			setState(GameplayState.WAITING_FOR_MOVE);
			if (!repetitionWarner.isOk(game.getCurrentState())) {
				String message = "Threefold repetition rule: One more repetition of this position for Player "
						+ (game.getCurPlayer().getIndex() + 1)
						+ " will end the game in a draw.";
				MenuUtils.messageDialog(view.getTheStage(), message, "OK", null);
			}
		}
	}

	public void undoLastMove() {

	}

	public void redoLastMove() {

	}

	/**
	 * Cancel adjustment state and go back to selecting state to select a
	 * (potentially) different circle.
	 */
	public void selectDifferentCircle() {
		setState(GameplayState.WAITING_FOR_MOVE);
	}

	public void cycleSelectionLeft() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextLeft(getCurrCircle(), playerCircles));
	}

	public void cycleSelectionRight() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextRight(getCurrCircle(), playerCircles));
	}

	public void cycleSelectionUp() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextAbove(getCurrCircle(), playerCircles));
	}

	public void cycleSelectionDown() {
		Collection<ICircle> playerCircles = getGame().getCurPlayerState().getCircles();
		setCurrCircle(CircleUtil.findNextBelow(getCurrCircle(), playerCircles));
	}

	public void showInstructions() {
		showingInstructions = true;
		view.showInstructions(new Runnable() {
			public void run() {
				showingInstructions = false;
			}
		});
	}

	public void toggleCircleLabels() {
		view.toggleCircleLabels();
	}
	
	public void toggleRangeRings() {
		view.toggleRangeRings();
	}

	private void dispose() {
		for (IPlayer player : game.getPlayers()) {
			if (player instanceof CpuPlayer) {
				((CpuPlayer) player).dispose();
			}
		}
	}

}
