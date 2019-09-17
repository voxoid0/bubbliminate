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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.voxoid.bubbliminate.ActorUtil;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.ControllerAssets;
import com.voxoid.bubbliminate.ControllerAssets.Control;
import com.voxoid.bubbliminate.PlayerConfigScreen;
import com.voxoid.bubbliminate.SimpleTransform2;
import com.voxoid.bubbliminate.TitleScreen;
import com.voxoid.bubbliminate.UiUtil;
import com.voxoid.bubbliminate.VisualSettings;
import com.voxoid.bubbliminate.actors.BannerTextActor;
import com.voxoid.bubbliminate.actors.CircleActor;
import com.voxoid.bubbliminate.actors.CircleLabelActor;
import com.voxoid.bubbliminate.actors.EnvironmentActor;
import com.voxoid.bubbliminate.actors.EnvironmentPanZoomAction;
import com.voxoid.bubbliminate.actors.FadeActor;
import com.voxoid.bubbliminate.actors.HelpTextActor;
import com.voxoid.bubbliminate.actors.PlaySoundAction;
import com.voxoid.bubbliminate.actors.RangeRingsActor;
import com.voxoid.bubbliminate.actors.RectangleActor;
import com.voxoid.bubbliminate.actors.SinusoidAlphaAction;
import com.voxoid.bubbliminate.actors.TextActor;
import com.voxoid.bubbliminate.actors.TextureRegionActor;
import com.voxoid.bubbliminate.actors.TransparentTextureRegionActor;
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
import com.voxoid.bubbliminate.ui.CirclesTextButton;
import com.voxoid.bubbliminate.ui.MenuUtils;

public class GameViewTouch extends Group implements IGameView {
	private static final float MIN_TOOLBAR_BUTTON_HEIGHT_INCHES = 0.25f;
	private static final float PLAYER_TURN_MOVE_DURATION = 0.25f;
	private static final float MIN_ENV_SIZE_FRACTION_OF_SCREEN = 0.9f;
	//private static final String TOUCH_MOVE_INSTRUCTIONS = "Move by dragging center of bubble.\n\nGrow by dragging edge of bubble.\n\nSplit by swiping through bubble at desired angle.";
	private static final float SELECTION_ALPHA_OFFSET = 0.55f;
	private static final float SELECTION_ALPHA_AMPLITUDE = 0.45f;
	private static final float SELECTION_ALPHA_WAVELENGTH = 0.66f;
	private static final float BACKGROUND_PARALAX_SCALE = 8f;
	
	private IGameController controller;
	private IViewState viewState;
	private float backgroundScale;
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
	private SimpleTransform2 prevEnvTransf;	// previous environment transform
	private SimpleTransform2 fullViewEnvTransf;
	private EnvironmentPanZoomAction envPanZoomAction;
	private boolean rangeRingsAlwaysVisible = false;
	
	public GameViewTouch(IGame game, Stage stage) {
		Validate.notNull(game);
		Validate.notNull(stage);

		this.game = game;

		ActorUtil.fillScreenKeepingRatio(GameScreen.background);
//		stage.addActor(GameScreen.background);
		backgroundScale = GameScreen.background.getScaleX();
		
		this.envir = new EnvironmentActor(game.getEnvironmentRadius());
		
		float toolbarHeight = calcToolbarButtonHeight();
		buildToolbarBackground(stage, toolbarHeight);
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
		
		viewState = new WaitingForMoveState();
		viewState.start(game.getCurrentState(), game.getCurPlayer(), null);
		
//		buildControlsHelpContent();
		buildToolbarButtons(toolbarHeight);
		helpText.updatePosition();
	}

	private float calcToolbarButtonHeight() {
		float minPixelsTall = CirclesGlobal.platform.inchesToPixels(MIN_TOOLBAR_BUTTON_HEIGHT_INCHES);
		return 64f >= minPixelsTall ? 64f : minPixelsTall;
	}
	
	public Stage getTheStage() {
	    return envir.getStage();
	}
	
	public void init(IGameController controller) {
		Validate.notNull(controller);
		this.controller = controller;
		updatePlayerTurnBanner();
	}

	public void showCannotSplitMessage() {
//		setHelpText("That circle is too small to split any further. Try a different move.",
//				true);
		MenuUtils.messageDialog(getTheStage(), "That circle is too small to split any further. Try a different move.", "OK", null);
	}
	
	public void setHelpText(String text, boolean returnToControls) {
//		helpText.setHelpSequence(new Object[] { text, controlsHelpContent }, 3f);
        helpText.setHelpText(text);
		updateHelpTextHomePosition();
	}
	
	public void setHelpText(String text) {
		helpText.setHelpText(text);
		updateHelpTextHomePosition();
	}

	private void buildToolbarBackground(Stage stage, float size) {
		float margin = size / 8f;
		
		// Toolbar
		stage.addActor(new RectangleActor(0, 0, Gdx.graphics.getWidth(), margin + size + margin, new Color(1f, 1f, 1f, 0.33f)));	
	}
	
	private void buildToolbarButtons(float size) {
		float padding = 1f;
		float margin = size / 8f;
		float btnScale = size / 64f;
		
		TextureRegionActor btn;
		
		float y = margin;
		
		float x = margin;
		final Actor btnShare = new TransparentTextureRegionActor(Assets.iconShare, 0.75f);
	    envir.getStage().addActor(btnShare);
	    btnShare.setPosition(x, y);
	    btnShare.setScale(btnScale);
	    btnShare.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				btnShare.setTouchable(Touchable.disabled);
				
				// Running after a delay allows the stopping of touch events on the button while the screen shot is being taken
				getTheStage().addAction(Actions.sequence(Actions.delay(0.1f), Actions.run(new Runnable() {
					public void run() {
						CirclesGlobal.platform.shareScreenshot();
						btnShare.setTouchable(Touchable.enabled);
					}
				})));
				return true;
			}			
		});
		
		x += size + padding;
		btn = new TransparentTextureRegionActor(Assets.iconRanges, 0.75f);
	    envir.getStage().addActor(btn);
		btn.setPosition(x, y);
		btn.setScale(btnScale);
		btn.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				controller.toggleRangeRings();
				return true;
			}			
		});
		
		x += size + padding;
		btn = new TransparentTextureRegionActor(Assets.iconLabels, 0.75f);
	    envir.getStage().addActor(btn);
		btn.setPosition(x, y);
		btn.setScale(btnScale);
		btn.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				controller.toggleCircleLabels();
				return true;
			}			
		});

		x = Gdx.graphics.getWidth() - margin - Assets.iconExit.getRegionWidth();
		btn = new TransparentTextureRegionActor(Assets.iconExit, 0.75f);
	    envir.getStage().addActor(btn);
		btn.setPosition(x, y);
		btn.setScale(btnScale);
		btn.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				controller.exit();
				return true;
			}			
		});

		x -= size + padding;
		btn = new TransparentTextureRegionActor(Assets.iconHelp, 0.75f);
	    envir.getStage().addActor(btn);
		btn.setPosition(x, y);
		btn.setScale(btnScale);
		btn.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				controller.showInstructions();
				return true;
			}			
		});		
	}
	
//	private void buildControlsHelpContent() {
//		Table content = new Table();
//		if (CirclesGlobal.isTouchDevice) {
//			Label label = new Label(TOUCH_MOVE_INSTRUCTIONS, Assets.skin, Assets.LABEL_STYLE_NORMAL);
//			label.setAlignment(Align.top | Align.left);
//			label.setWrap(true);
//			content.add(label).top().left().expand().fill();
//		} else {
//			addToControlList(content, ControllerAssets.Control.DPAD, "Choose Bubble");
//			addToControlList(content, ControllerAssets.Control.U, "Move");
//			addToControlList(content, ControllerAssets.Control.Y, "Grow");
//			addToControlList(content, ControllerAssets.Control.A, "Split");
//			addToControlList(content, ControllerAssets.Control.LS, "Adjust");
//			addToControlList(content, ControllerAssets.Control.RS, "Tweak");
//			addToControlList(content, ControllerAssets.Control.O, "Complete Move");
//			content.add(new Label(" ", Assets.skin, Assets.LABEL_STYLE_NORMAL));
//			content.row();
//			addToControlList(content, ControllerAssets.Control.R2, "Instructions");
//			addToControlList(content, ControllerAssets.Control.L2, "Color Blind");
//		}
//		content.pack();
//
//		controlsHelpContent = content;
//	}
	
//	private void buildTouchMoveHelpContent() {
//		Table content = new Table();
//		content.add(new Label()
//		addToControlList(content, ControllerAssets.Control.DPAD, "Choose Bubble");
//		addToControlList(content, ControllerAssets.Control.U, "Move");
//		addToControlList(content, ControllerAssets.Control.Y, "Grow");
//		addToControlList(content, ControllerAssets.Control.A, "Split");
//		addToControlList(content, ControllerAssets.Control.LS, "Adjust");
//		addToControlList(content, ControllerAssets.Control.RS, "Tweak");
//		addToControlList(content, ControllerAssets.Control.O, "Complete Move");
//		content.add(new Label(" ", Assets.skin, Assets.LABEL_STYLE_NORMAL));
//		content.row();
//		addToControlList(content, ControllerAssets.Control.R2, "Instructions");
//		addToControlList(content, ControllerAssets.Control.L2, "Color Blind");
//		content.pack();
//
//		controlsHelpContent = content;
//	}
	
	public void showControlsHelp() {
//		if (helpText.getHelpContent() != controlsHelpContent) {
//			helpText.setHelpContent(controlsHelpContent);
//			updateHelpTextHomePosition();
//		}
	    
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
	
	public Vector2 screenToWorld(Vector2 screen) {
		//return envir.stageToLocalCoordinates(new Vector2(screen.x, Gdx.graphics.getHeight() - screen.y));
		SimpleTransform2 tr = CirclesGlobal.gameEnvirTransform == null ? SimpleTransform2.NONE : CirclesGlobal.gameEnvirTransform;
		
		return new Vector2((screen.x - tr.getTx()) / tr.getScaleX(), ((Gdx.graphics.getHeight() - screen.y) - tr.getTy()) / tr.getScaleY());

	}
	
	public Vector2 worldToScreen(Vector2 world) {
		//return envir.localToStageCoordinates(world);
		SimpleTransform2 tr = CirclesGlobal.gameEnvirTransform == null ? SimpleTransform2.NONE : CirclesGlobal.gameEnvirTransform;
		return new Vector2(world.x * tr.getScaleX() + tr.getTx(), world.y * tr.getScaleY() + tr.getTy());
	}
	
	public ICircle circleHit(Vector2 screen) {
		Actor actor = envir.hit(screen.x, screen.y, true);
		if (actor instanceof CircleActor) {
			return ((CircleActor) actor).getCircle();
		} else {
			return null;
		}
	}
	
	public void resize(int width, int height) {
		float eh = height * MIN_ENV_SIZE_FRACTION_OF_SCREEN;
		float ew = width * MIN_ENV_SIZE_FRACTION_OF_SCREEN;
		if (eh < ew) ew = eh; else eh = ew;
		float scale = eh / (game.getEnvironmentRadius() * 2f);
//		CirclesGlobal.gameEnvirTransform = new SimpleTransform2(scale, width * 0.1f + ew/2, height / 2);
		
		fullViewEnvTransf = new SimpleTransform2(scale, width / 2, height / 2);
		CirclesGlobal.gameEnvirTransform = fullViewEnvTransf;
//		envir.setSize(ew, eh);

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
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameView#setEnvirTransform(com.voxoid.bubbliminate.SimpleTransform2)
	 */
	@Override
	public void setEnvirTransform(SimpleTransform2 transf) {
		
		// bounds of scaling
		final float minScale = ActorUtil.getScreenShortDim() / (game.getEnvironmentRadius() * 2f) * MIN_ENV_SIZE_FRACTION_OF_SCREEN;
		final float maxScale = minScale * 10f;
		
		final float scale = Math.min(Math.max(transf.getScaleX(), minScale), maxScale);
		
		// screen center
		final float scx = Gdx.graphics.getWidth() / 2f;
		final float scy = Gdx.graphics.getHeight() / 2f;
		
		// bounds of translation
		final float envScreenDiameter = game.getEnvironmentRadius()*2f * scale;
		final float minX = (Gdx.graphics.getWidth() * MIN_ENV_SIZE_FRACTION_OF_SCREEN) - envScreenDiameter;
		final float maxX = (Gdx.graphics.getWidth() * (1f - MIN_ENV_SIZE_FRACTION_OF_SCREEN)) + envScreenDiameter;
		final float minY = (Gdx.graphics.getHeight() * MIN_ENV_SIZE_FRACTION_OF_SCREEN) - envScreenDiameter;
		final float maxY = (Gdx.graphics.getHeight() * (1f - MIN_ENV_SIZE_FRACTION_OF_SCREEN)) + envScreenDiameter;
		
		final float tx = Math.min(Math.max(transf.getTx(), minX), maxX);
		final float ty = Math.min(Math.max(transf.getTy(), minY), maxY);
		
		CirclesGlobal.gameEnvirTransform = new SimpleTransform2(scale, tx, ty);
		
		// Scale background
		float z = minScale / scale; // z = (0, 1]
		float lessZ = 1 - (1 - z) / BACKGROUND_PARALAX_SCALE;
		float lessScale = minScale / lessZ;
		float bgScale = backgroundScale / minScale * lessScale;
		GameScreen.background.setScale(bgScale);
	}
	
	public void zoomOutToFullView() {
		prevEnvTransf = CirclesGlobal.gameEnvirTransform;
		if (envPanZoomAction != null) {
			envir.removeAction(envPanZoomAction);
		}
		envPanZoomAction = new EnvironmentPanZoomAction(this, CirclesGlobal.gameEnvirTransform, fullViewEnvTransf);
		envir.addAction(envPanZoomAction);
	}
	
	public void zoomInToPreviousView() {
		if (envPanZoomAction != null) {
			envir.removeAction(envPanZoomAction);
		}
		envPanZoomAction = new EnvironmentPanZoomAction(this, CirclesGlobal.gameEnvirTransform, prevEnvTransf);
		envir.addAction(envPanZoomAction);		
	}
	
	/* (non-Javadoc)
	 * @see com.voxoid.bubbliminate.gameplay.IGameView#getEnvirTransform()
	 */
	@Override
	public SimpleTransform2 getEnvirTransform() {
		return CirclesGlobal.gameEnvirTransform;
	}

	private void updateHelpTextHomePosition() {
//		float helpX = Gdx.graphics.getWidth()*0.95f - helpText.getWidth() + 20f;
//		float helpY = Gdx.graphics.getHeight()*0.05f;
//		helpText.setHomePosition(helpX, helpY);
	}
	
	
	public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
			IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {

		//background.setTextureRegion(Assets.gameBackground); // In case texture had to be restored after lost OpenGL context
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
		case WAITING_FOR_MOVE:
		    viewState = new WaitingForMoveState();
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
		default:
		    throw new NotImplementedException();
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
	
	public void exit(final Runnable runnable, final boolean playAgain) {
		new FadeActor(envir.getStage(), Color.BLACK).fadeOut(1f, new Runnable() {
			public void run() {
				envir.getStage().clear();
				runnable.run();
				((CirclesGame) Gdx.app.getApplicationListener()).setScreen(
						playAgain ?
							new GameScreen(PlayerConfigScreen.createGame(game.getConfig())) :
							new TitleScreen(Color.BLACK));
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
		if (diff != null) { // TODO: this null check wasn't necessary before *Touch classes...
			for (ICircle moving : diff.getMovedNew()) {
				ICircle found = CircleUtil.findMatchingCircle(moving, circles, game.getMinCircleRadius() / 10f);
				circles.remove(found);
			}
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
				CircleActor circleActor = new CircleActor(player.getColor().cpy(), circle,
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
					TextActor labelActor = new CircleLabelActor(Integer.toString(i + 1), (CircleActor) circleActor);
					grp.addActor(labelActor);
				}
			}
		}
		return grp;
	}
	
	public void setCircleLabelsVisible(boolean visible) {
		circleLabels.setVisible(visible);
	}
	
	public void setRangeRingsAlwaysVisible(boolean visible) {
		rangeRingsAlwaysVisible = visible;
		rangeRings.setVisible(visible);
	}
	
	public boolean isCircleLabelsVisible() {
		return circleLabels.isVisible();
	}
	
	public void toggleCircleLabels() {
		setCircleLabelsVisible(!isCircleLabelsVisible());
	}
	
	public void toggleRangeRings() {
		setRangeRingsAlwaysVisible(!isRangeRingsAlwaysVisible());
	}

	private boolean isRangeRingsAlwaysVisible() {
		// TODO Auto-generated method stub
		return rangeRingsAlwaysVisible;
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

		Disposer.removeAndDispose(rangeRings, envir);
		rangeRings = createRangeRings(gameState, GameStateDiff.EMPTY);
		rangeRings.setVisible(rangeRingsAlwaysVisible);
		envir.addActor(rangeRings);

		
		Disposer.removeAndDispose(circles, envir);
		circles = createCirclesGroup(gameState);
		
		for (Actor circleActor : circles.getChildren()) {
			Color c = circleActor.getColor();
			circleActor.setColor(c.r, c.g, c.b, VisualSettings.getTentativeGameStateAlpha());
		}
		envir.addActor(circles);
		
		recreateCircleLabels(gameState);
		makePoppingSounds(prevNumCircles);
	}
	
	private void recreateCircleLabels(ICirclesGameState gameState) {
		boolean visible = circleLabels.isVisible();
		Disposer.removeAndDispose(circleLabels, envir);
		circleLabels = createCircleLabels(gameState);		
		circleLabels.setVisible(visible);
		envir.addActor(circleLabels);
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
			
			
			selectionRangeRings = createSelectionRangeRings(circle, player,
					gameState.getPlayerState(player.getIndex()));
			envir.addActor(selectionRangeRings);
			
			recreateCircleActors(gameState);
			
			rangeRings.setVisible(true);
		}
		
		@Override
		public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
			recreateCircleActors(gameState);
//			envir.removeActor(circles);
			Disposer.removeAndDispose(selectionRangeRings, envir);
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
//			helpText.addAction(new SinusoidAlphaAction(Color.WHITE.cpy(), 2f, 0.4f, 0.2f));

//            helpText.addAction(new SinusoidAlphaAction(Color.WHITE.cpy(), 2f, 0.4f, 0.2f));
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

    private class WaitingForMoveState implements IViewState {

        @Override
        public void start(ICirclesGameState gameState, IPlayer player, ICircle circle) {
//            helpText.addAction(new SinusoidAlphaAction(Color.WHITE.cpy(), 2f, 0.4f, 0.2f));

//            helpText.addAction(new SinusoidAlphaAction(Color.WHITE.cpy(), 2f, 0.4f, 0.2f));
            
            glowCircles(gameState, player);
        }

        private void glowCircles(ICirclesGameState gameState, IPlayer player) {
            for (ICircle c : gameState.getPlayerState(player.getIndex()).getCircles()) {
                CircleActor circleActor = getCircleActor(c, gameState);
                circleActor.clearActions();
                circleActor.addAction(new SinusoidAlphaAction(
                        c.getPlayer().getColor().cpy(), SELECTION_ALPHA_WAVELENGTH, SELECTION_ALPHA_AMPLITUDE, SELECTION_ALPHA_OFFSET));
            }
        }
        
        private void stopGlowingCircles(ICirclesGameState gameState, IPlayer player) {
            for (ICircle c : gameState.getPlayerState(player.getIndex()).getCircles()) {
                CircleActor circleActor = getCircleActor(c, gameState);
                circleActor.clearActions();
                ActorUtil.setAlpha(circleActor, 1f);
            }
        }

        @Override
        public void stop(ICirclesGameState gameState, IPlayer player, ICircle circle) {
//          stopGlowingCircles(gameState, player);
            recreateCircleActors(gameState);
        }

        @Override
        public void update(ICirclesGameState gameState, GameplayState gameCtrlrState,
                IMove currMove, IPlayer currPlayer, ICircle currCircle, GameStateDiff diff) {
//            glowCircles(gameState, currPlayer);
        }
    }

    public void promptKeepMove(final Function1<Boolean> func) {
		MenuUtils.dialog(getTheStage(), null, new String[] { "Keep", "Retry" }, 1, MenuUtils.FLAG_TOP | MenuUtils.FLAG_NONMODAL,
				new Function1<String>() {
					public void run(String arg1) {
						func.run("Keep".equals(arg1));
					}
				});
	}
    
    public void promptPlayAgain(final Function1<Boolean> func) {
    	bounceBannerText.addAction(Actions.sequence(Actions.delay(5f), Actions.run(new Runnable() {
			public void run() {
				MenuUtils.dialog(envir.getStage(), null, new String[] { "Play Again", "Quit" }, 1, MenuUtils.FLAG_TOP | MenuUtils.FLAG_NONMODAL,
						new Function1<String>() {
							public void run(String arg1) {
								func.run("Play Again".equals(arg1));
							}
						});
			}
		})));
	}
	
	public void promptExit(final Runnable runnable) {
		final String[] options = new String[] { "Quit", "Cancel" };
		MenuUtils.dialog(envir.getStage(), "This will end your game. Are you sure you want to quit?",
				options, 1, 0, new Function1<String>() {
					
					@Override
					public void run(String option) {
						// If they chose to quit, show ad messages and quit (run the runnable)
						if (option != null && option.equals(options[0])) {
							CirclesGlobal.demoHelper.finishPurchaseInit(getTheStage(), new Runnable() {
								public void run() {
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
							});
						}
					}
				}
		);
	}

	public void showInstructions(Runnable after) {
		CirclesGlobal.instructionsHelper.showInstructions(envir.getStage(), after);
	}

	private void updatePlayerTurnBanner() {
		final IPlayer nextPlayer = controller.getCurrPlayer();
		playerTurnLabel.addAction(Actions.after(Actions.sequence(
				Actions.moveTo(-Gdx.graphics.getWidth() / 2f, playerTurnLabel.getY(), PLAYER_TURN_MOVE_DURATION, Interpolation.sineOut),
				Actions.run(new Runnable() {
					public void run() {
						playerTurnLabel.setText("Player " + (nextPlayer.getIndex()+1) + "'s Turn");
						playerTurnLabel.setColor(nextPlayer.getColor().cpy());
					}
				}),
				controller.getState() == GameplayState.WON || controller.getState() == GameplayState.DRAW ?
					Actions.delay(0f) :
					Actions.moveTo(Gdx.graphics.getWidth() * 0.05f, playerTurnLabel.getY(), 0.5f, Interpolation.sineIn)
		)));
	}
}
