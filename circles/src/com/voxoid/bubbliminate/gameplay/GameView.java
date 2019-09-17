package com.voxoid.bubbliminate.gameplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.voxoid.bubbliminate.ActorUtil;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.ControllerAssets;
import com.voxoid.bubbliminate.ControllerAssets.Control;
import com.voxoid.bubbliminate.SimpleTransform2;
import com.voxoid.bubbliminate.TitleScreen;
import com.voxoid.bubbliminate.UiUtil;
import com.voxoid.bubbliminate.VisualSettings;
import com.voxoid.bubbliminate.actors.BannerTextActor;
import com.voxoid.bubbliminate.actors.CircleActor;
import com.voxoid.bubbliminate.actors.EnvironmentActor;
import com.voxoid.bubbliminate.actors.FadeActor;
import com.voxoid.bubbliminate.actors.HelpTextActor;
import com.voxoid.bubbliminate.actors.PlaySoundAction;
import com.voxoid.bubbliminate.actors.RangeRingsActor;
import com.voxoid.bubbliminate.actors.SinusoidAlphaAction;
import com.voxoid.bubbliminate.actors.TextActor;
import com.voxoid.bubbliminate.actors.TextureRegionActor;
import com.voxoid.bubbliminate.core.model.CircleUtil;
import com.voxoid.bubbliminate.core.model.GameStateDiff;
import com.voxoid.bubbliminate.core.model.GameStateUtil;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;
import com.voxoid.bubbliminate.core.rules.CirclePointQuery;
import com.voxoid.bubbliminate.core.rules.GrowMove;
import com.voxoid.bubbliminate.core.rules.IMove;
import com.voxoid.bubbliminate.core.rules.MoveMove;
import com.voxoid.bubbliminate.core.rules.SplitMove;
import com.voxoid.bubbliminate.core.util.Disposer;
import com.voxoid.bubbliminate.core.util.Function1;
import com.voxoid.bubbliminate.ui.MenuUtils;

public class GameView extends Group implements IGameView {

	private static final float SELECTION_ALPHA_OFFSET = 0.55f;
	private static final float SELECTION_ALPHA_AMPLITUDE = 0.45f;
	private static final float SELECTION_ALPHA_WAVELENGTH = 0.66f;
	
	private IGameController controller;
	private IViewState viewState;
	private Group envir;
	private Group rangeRings = new Group();
	private Actor selectionRangeRings = new Actor();
	private Group circles = new Group();
	private Group circleLabels = new Group();
	private IGame game;
	private HelpTextActor helpText;
	private BannerTextActor scrollBannerText;
	private BannerTextActor bounceBannerText;
	private Label playerTurnLabel;
	private Map<ICircle, CircleActor> circleActorByCircle = new HashMap<ICircle, CircleActor>();
	private Table controlsHelpContent;
	private TextureRegionActor background;
	
	
	public GameView(IGame game, Stage stage) {
		Validate.notNull(game);
		Validate.notNull(stage);

		this.game = game;

		background = new TextureRegionActor(Assets.gameBackground);
		ActorUtil.fillScreenKeepingRatio(background);
		envir.getStage().addActor(background);
		
		this.envir = new EnvironmentActor(game.getEnvironmentRadius());
		stage.addActor(envir);
		
		helpText = new HelpTextActor();
		helpText.hide();
		scrollBannerText = new BannerTextActor();
		bounceBannerText = new BannerTextActor();
		playerTurnLabel = new Label("", Assets.skin, Assets.LABEL_STYLE_BIG);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		
		circles = createCirclesGroup(game.getCurrentState());
		envir.addActor(circles);
		circleLabels = createCircleLabels(game.getCurrentState());
		circleLabels.setVisible(false);
		envir.addActor(circleLabels);
		
		viewState = new SelectionState();
		viewState.start(game.getCurrentState(), game.getCurPlayer(), null);
		
		buildControlsHelpContent();
	}
	
	public void init(IGameController controller) {
		Validate.notNull(controller);
		this.controller = controller;
		updatePlayerTurnBanner();
	}

	public void setHelpText(String text, boolean returnToControls) {
		helpText.setHelpSequence(new Object[] { text, controlsHelpContent }, 3f);
		updateHelpTextHomePosition();
	}
	
	public void setHelpText(String text) {
		helpText.setHelpText(text);
		updateHelpTextHomePosition();
	}

	private void buildControlsHelpContent() {
		Table content = new Table();
		addToControlList(content, ControllerAssets.Control.DPAD, "Choose Bubble");
		addToControlList(content, ControllerAssets.Control.U, "Move");
		addToControlList(content, ControllerAssets.Control.Y, "Grow");
		addToControlList(content, ControllerAssets.Control.A, "Split");
		addToControlList(content, ControllerAssets.Control.LS, "Adjust");
		addToControlList(content, ControllerAssets.Control.RS, "Tweak");
		addToControlList(content, ControllerAssets.Control.O, "Complete Move");
		content.add(new Label(" ", Assets.skin, Assets.LABEL_STYLE_NORMAL));
		content.row();
		addToControlList(content, ControllerAssets.Control.R2, "Instructions");
		addToControlList(content, ControllerAssets.Control.L2, "Color Blind");
		content.pack();

		controlsHelpContent = content;
	}
	
	public void showControlsHelp() {
		if (helpText.getHelpContent() != controlsHelpContent) {
			helpText.setHelpContent(controlsHelpContent);
			updateHelpTextHomePosition();
		}
	}

	private void addToControlList(Table controlList, final Control control,
			final String desc) {
		UiUtil.addControlOuya(controlList, control, desc);
		controlList.row();
	}

	public void setBanner(String text, IPlayer player, boolean stay) {
		if (stay) {
			bounceBannerText.setBannerText(text, player, true);
		} else {
			scrollBannerText.setBannerText(text, player, false);
			bounceBannerText.setBannerText("", null, true);
		}
	}
	
	public void resize(int width, int height) {
		float eh = height * 0.9f;
		float ew = width * 0.9f;
		if (eh < ew) ew = eh; else eh = ew;
		float scale = eh / (game.getEnvironmentRadius() * 2f);
//		CirclesGlobal.gameEnvirTransform = new SimpleTransform2(scale, width * 0.1f + ew/2, height / 2);
		CirclesGlobal.gameEnvirTransform = new SimpleTransform2(scale, width / 2, height / 2);
		envir.setSize(ew, eh);
//		envir.setSize(width, height);

		helpText.updatePosition();
		envir.getStage().addActor(helpText);
		
		scrollBannerText.setY(Gdx.graphics.getHeight() * 3 / 4);
		envir.getStage().addActor(scrollBannerText);
		
		bounceBannerText.setY(Gdx.graphics.getHeight() * 0.85f);
		envir.getStage().addActor(bounceBannerText);
		
//		playerTurnLabel.setAlignment(Align.bottom, Align.left);
		playerTurnLabel.setY(Gdx.graphics.getHeight() * 0.9f);
		envir.getStage().addActor(playerTurnLabel);
	}

	private void updateHelpTextHomePosition() {
//		float helpX = Gdx.graphics.getWidth()*0.95f - helpText.getWidth() + 20f;
//		float helpY = Gdx.graphics.getHeight()*0.05f;
//		helpText.setHomePosition(helpX, helpY);
	}
	
	
	public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
			IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
		
		viewState.update(gameState, gameCtrlrState, currMove, currPlayer, currCircle, diff);
	}
	
	public void onControllerStateChanged(ICirclesGameState gameState, GameplayState gameCtrlrState,
			IMove currMove, IPlayer currPlayer, ICircle currCircle) {
		
		viewState.stop(gameState, currPlayer, currCircle);
		switch (gameCtrlrState) {
		case WAITING_FOR_PLAYER:
			viewState = new WaitingForPlayerState();
			viewState.start(gameState, currPlayer, currCircle);
			break;
		case SELECTING:
			viewState = new SelectionState();
			viewState.start(gameState, currPlayer, currCircle);
			break;
		case ADJUSTING:
			onMoveTypeChanged(gameState, gameCtrlrState, currMove, currPlayer, currCircle);
			break;
		case ANIMATING:
			viewState = new AnimatingMoveState();
			viewState.start(gameState, currPlayer, currCircle);
			break;
		case WON:
		case DRAW:
			viewState = new GameDoneState();
			viewState.start(gameState, currPlayer, currCircle);
			break;
		}
	}

	public void onMoveTypeChanged(ICirclesGameState gameState, GameplayState gameCtrlrState,
			IMove currMove, IPlayer currPlayer, ICircle currCircle) {
		
		viewState.stop(gameState, currPlayer, currCircle);
		if (currMove instanceof MoveMove) {
			viewState = new MoveState();
		} else if (currMove instanceof GrowMove) {
			viewState = new GrowState();
		} else if (currMove instanceof SplitMove) {
			viewState = new SplitState();
		}
		viewState.start(gameState, currPlayer, currCircle);
	}
	
	public void exit(final Runnable runnable) {
		new FadeActor(envir.getStage(), Color.BLACK).fadeOut(1f, new Runnable() {
			public void run() {
				envir.getStage().clear();
				runnable.run();
				((CirclesGame) Gdx.app.getApplicationListener()).setScreen(new TitleScreen(Color.BLACK));
			}
		});
	}
	
	private Actor createSelectionRangeRings(ICircle circle, IPlayer player, IPlayerState playerState) {
		return new RangeRingsActor(circle, playerState.getNumCircles(),
				VisualSettings.getPlayerSelectedRingColor(player),
				VisualSettings.getSelectedRingWidth());
	}
	
	private Group createRangeRings(ICirclesGameState gameState, GameStateDiff diff) {
		Group grp = new Group();
		Collection<ICircle> circles = new ArrayList<ICircle>(GameStateUtil.getAllCircles(gameState));
		
		// Remove the moving circles from the collection of state circles
		for (ICircle moving : diff.getMovedNew()) {
			ICircle found = CircleUtil.findMatchingCircle(moving, circles, game.getMinCircleRadius() / 10f);
			circles.remove(found);
		}
		
		for (ICircle circle : circles) {
			IPlayer player = circle.getPlayer();
			IPlayerState playerState = gameState.getPlayerState(player.getIndex());
			
			Actor rangeRingsActor = new RangeRingsActor(circle, playerState.getNumCircles(),
					VisualSettings.getPlayerRingColor(player), VisualSettings.getRingWidth());
			grp.addActor(rangeRingsActor);
		}
		return grp;
	}
	
	private Group createCirclesGroup(ICirclesGameState gameState) {
		circleActorByCircle.clear();
		Group grp = new Group();
		IGame game = gameState.getGame();
		Iterator<IPlayer> playerIter = game.getPlayers().iterator();
		
		for (int i = 0; i < game.getNumPlayers(); i++) {
			IPlayer player = playerIter.next();
			IPlayerState playerState = gameState.getPlayerState(i);
			
			for (ICircle circle : playerState.getCircles()) {
				CircleActor circleActor = new CircleActor(player.getColor(), circle,
						SplitMove.canBePerformedOn(circle, game.getConfig()));
				circleActorByCircle.put(circle, circleActor);
				grp.addActor(circleActor);
			}
		}
		return grp;
	}

	private Group createCircleLabels(ICirclesGameState gameState) {
		Group grp = new Group();
		IGame game = gameState.getGame();
		Iterator<IPlayer> playerIter = game.getPlayers().iterator();
		
		for (int i = 0; i < game.getNumPlayers(); i++) {
			IPlayerState playerState = gameState.getPlayerState(i);
			
			for (ICircle circle : playerState.getCircles()) {
				Actor circleActor = circleActorByCircle.get(circle);
				if (circleActor != null) {	// Should never be null
					TextActor labelActor = new TextActor(Assets.tinyFont, Color.WHITE.cpy(), Integer.toString(i + 1));
					labelActor.setFont(Assets.tinyFont);
					labelActor.setPosition(circleActor.getX() - labelActor.getWidth() / 2,
							circleActor.getY() + labelActor.getHeight() / 2);
					grp.addActor(labelActor);
				}
			}
		}
		return grp;
	}
	
	public void setCircleLabelsVisible(boolean visible) {
		circleLabels.setVisible(visible);
	}
	
	public boolean isCircleLabelsVisible() {
		return circleLabels.isVisible();
	}
	
	public void toggleCircleLabels() {
		setCircleLabelsVisible(!isCircleLabelsVisible());
	}
	
	public void toggleRangeRings() {
		throw new NotImplementedException();
	}

	private CircleActor getCircleActor(ICircle circle, ICirclesGameState gameState) {
		circle = new CirclePointQuery(circle.getLocation(), gameState).getCircle();
		if (circle != null) {
			return circleActorByCircle.get(circle);
		} else {
			return null;
		}
	}

	private void recreateCircleActors(ICirclesGameState gameState) {
		int prevNumCircles = circles.getChildren().size;
		
		Disposer.removeAndDispose(circles, envir);
		Disposer.removeAndDispose(circleLabels, envir);
		circles = createCirclesGroup(gameState);
		
		for (Actor circleActor : circles.getChildren()) {
			Color c = circleActor.getColor();
			circleActor.setColor(c.r, c.g, c.b, VisualSettings.getTentativeGameStateAlpha());
		}
		envir.addActor(circles);
		
		boolean visible = circleLabels.isVisible();
		circleLabels = createCircleLabels(gameState);
		circleLabels.setVisible(visible);
		envir.addActor(circleLabels);
		
		makePoppingSounds(prevNumCircles);
	}

	private void makePoppingSounds(int prevNumCircles) {
		int numPopped = prevNumCircles - circles.getChildren().size;
		if (numPopped > 0) {
			circles.addAction(Actions.repeat(numPopped, Actions.sequence(
					new PlaySoundAction(Assets.popSound),
					Actions.delay(0.125f))));
		}
	}
	
	interface IViewState {
		
		void start(ICirclesGameState gameState, IPlayer player, ICircle circle);

		void stop(ICirclesGameState gameState, IPlayer player, ICircle circle);
		
		void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
				IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff);
	}
	
	interface IAdjustmentViewState extends IViewState {

//		void setControlPoint(Vector2 pnt);
	}
	
	private class SelectionState implements IViewState {
		
		private Action selectionAction;
		
		@Override
		public void start(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			setSelectedCircle(gameState, player, circle);
		}
		
		private void setSelectedCircle(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			unselectCircle(player);
			if (circle != null) {
				CircleActor circleActor = getCircleActor(circle, gameState);
				if (circleActor != null) {
					circleActor.clearActions();
					selectionAction = new SinusoidAlphaAction(circleActor.getColor(), SELECTION_ALPHA_WAVELENGTH, SELECTION_ALPHA_AMPLITUDE, SELECTION_ALPHA_OFFSET);
					circleActor.addAction(selectionAction);
				}
			}
		}

		private void unselectCircle(IPlayer player) {
			if (selectionAction != null && selectionAction.getActor() != null) {
				Actor actor = selectionAction.getActor();
				actor.clearActions();
				ActorUtil.setAlpha(actor, 1f);
			}
		}
		
		@Override
		public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			unselectCircle(player);
		}
		
		@Override
		public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
				IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
			setSelectedCircle(gameState, currPlayer, currCircle);
		}
	}

	private abstract class AdjustmentState implements IAdjustmentViewState {
		@Override
		public void start(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			
			rangeRings = createRangeRings(gameState, GameStateDiff.EMPTY);
			envir.addActor(rangeRings);
			
			selectionRangeRings = createSelectionRangeRings(circle, player,
					gameState.getPlayerState(player.getIndex()));
			envir.addActor(selectionRangeRings);
			
			recreateCircleActors(gameState);
		}
		
		@Override
		public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			recreateCircleActors(gameState);
//			envir.removeActor(circles);
			Disposer.removeAndDispose(selectionRangeRings, envir);
			Disposer.removeAndDispose(rangeRings, envir);
		}
		
		@Override
		public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
				IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
			
			/// Update range rings to new tentative state (e.g. one less ring because one of a player's circles were destroyed).
			Disposer.removeAndDispose(rangeRings, envir);
			rangeRings = createRangeRings(gameState, diff);
			envir.addActor(rangeRings);
			
			recreateCircleActors(gameState);
		}

	}
	
	private class MoveState extends AdjustmentState {
	}

	private class GrowState extends AdjustmentState {
	}

	private class SplitState extends AdjustmentState {
	}

	
	private class AnimatingMoveState implements IViewState {
		
		@Override
		public void start(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			controller.doneAnimatingMove();
//			circles = createCirclesGroup(gameState);
//			envir.addActor(circles);
		
		}
//		public void start(IGameState oldGameState, IGameState newGameState, IMove currMove, IPlayer player, ICircle circle) {
//			if (currMove instanceof MoveMove) {
//				ICircle oldCircle = currMove.getCircle();
//				
//				oldGameState.getPlayerState(player.getIndex()).get
//			} else if (currMove instanceof GrowMove) {
//				
//			} else if (currMove instanceof SplitMove) {
//				
//			}
//			currMove.getCircle();
//			circles = createCirclesGroup(gameState);
//			stage.addActor(circles);
//			
//		}
		
		
		@Override
		public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			updatePlayerTurnBanner();
		}
		
		@Override
		public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
				IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
			// nothing: nothing can change during animation
		}
	}
	
	
	private class GameDoneState implements IViewState {

		@Override
		public void start(ICirclesGameState gameState, IPlayer player, ICircle circle) {
		}

		@Override
		public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
		}

		@Override
		public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
				IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
		}
	}

	private class WaitingForPlayerState implements IViewState {

		@Override
		public void start(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			helpText.addAction(new SinusoidAlphaAction(Color.WHITE.cpy(), 2f, 0.4f, 0.2f));
		}

		@Override
		public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			recreateCircleActors(gameState);
		}

		@Override
		public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
				IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
		}
	}

	public void promptExit(final Runnable runnable) {
		final String[] options = new String[] { "Quit", "Cancel" };
		MenuUtils.dialog(envir.getStage(), "This will end your game. Are you sure you want to quit?",
				options, 1, 0, new Function1<String>() {
					
					@Override
					public void run(String option) {
						// If they chose to quit, show ad messages and quit (run the runnable)
						if (option != null && option.equals(options[0])) {
							if (!CirclesGlobal.purchasing.hasBeenPurchased()) {
								CirclesGlobal.demoHelper.showAdMessages(envir.getStage(),
									new Runnable() {
										public void run() {
											runnable.run();
										}
								});
							} else {
								runnable.run();
							}
						}
					}
				}
		);
	}

	public void showInstructions() {
		CirclesGlobal.instructionsHelper.showInstructions(envir.getStage(), null);
	}

	private void updatePlayerTurnBanner() {
		final IPlayer nextPlayer = controller.getCurrPlayer();
		playerTurnLabel.addAction(Actions.after(Actions.sequence(
				Actions.moveTo(-Gdx.graphics.getWidth() / 2f, playerTurnLabel.getY(), 0.5f, Interpolation.sineOut),
				Actions.run(new Runnable() {
					public void run() {
						playerTurnLabel.setText("Player " + (nextPlayer.getIndex()+1) + "'s Turn");
						playerTurnLabel.setColor(nextPlayer.getColor());
					}
				}),
				controller.getState() == GameplayState.WON || controller.getState() == GameplayState.DRAW ?
					Actions.delay(0f) :
					Actions.moveTo(Gdx.graphics.getWidth() * 0.05f, playerTurnLabel.getY(), 0.5f, Interpolation.sineIn)
		)));
	}

	@Override
	public void setEnvirTransform(SimpleTransform2 transf) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public SimpleTransform2 getEnvirTransform() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}
}
