package com.voxoid.bubbliminate.gameplay;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voxoid.bubbliminate.AppRestorer;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.DemoVersionHelper;
import com.voxoid.bubbliminate.InputStack;
import com.voxoid.bubbliminate.InstructionsHelper;
import com.voxoid.bubbliminate.LogoScreen;
import com.voxoid.bubbliminate.core.util.Disposer;
import com.voxoid.bubbliminate.core.util.Function1;

public class CirclesGame extends Game {

	public static final Logger LOGGER = Logger.getLogger(CirclesGame.class);
	
	private boolean pausedBySystem;
	private String prevScreenEventName;
	
	private List<Function1<Boolean>> pauseListeners = new CopyOnWriteArrayList<Function1<Boolean>>();
	
	
	@Override
	public void create() {
		try {
			LOGGER.info("Creating CirclesGame.");
			CirclesGlobal.game = this;
			CirclesGlobal.input = new InputStack();
			CirclesGlobal.shapeRenderer = new ShapeRenderer();
			CirclesGlobal.purchasing.startInit();
			CirclesGlobal.webMessages.startInit();
			CirclesGlobal.demoHelper = new DemoVersionHelper(CirclesGlobal.purchasing, CirclesGlobal.webMessages, CirclesGlobal.gameData);
			CirclesGlobal.instructionsHelper = new InstructionsHelper(CirclesGlobal.gameData);
			
			Gdx.input.setInputProcessor(CirclesGlobal.input);
			Controllers.addListener(CirclesGlobal.input);	// TODO: listen for new controller connections!?
			Gdx.input.setCatchBackKey(true);
			Gdx.input.setCatchMenuKey(true); // prevent on-screen keyboard


//			Assets.loadAll();
//			Assets.finishLoad();

			CirclesGlobal.appRestorer.restoreState(this, new Runnable() {
				public void run() {
					setScreen(new LogoScreen());
					
//					GameConfig gameConfig = new GameConfig(2);
//		            IGame game = new GameFactory().createGame(gameConfig, 
//		                    new DefaultPlayerFactory());
//					((Game) Gdx.app.getApplicationListener()).setScreen(new GameScreen(game));
				}
			});
	
////			((Game) Gdx.app.getApplicationListener()).setScreen(new PlayerConfigScreen());
//			
//			
//			List<PlayerConfig> playerConfigs = new ArrayList<PlayerConfig>();
//			playerConfigs.add(PlayerConfig.human());
//			playerConfigs.add(PlayerConfig.human());
//			playerConfigs.add(PlayerConfig.human());
//			playerConfigs.add(PlayerConfig.human());
//			playerConfigs.add(PlayerConfig.human());
//			playerConfigs.add(PlayerConfig.human());
//			playerConfigs.add(PlayerConfig.cpu(2));
//			playerConfigs.add(PlayerConfig.cpu(2));
//			playerConfigs.add(PlayerConfig.cpu(2));
//			playerConfigs.add(PlayerConfig.cpu(2));
//			ICircle bound = new Circle(Player.NONE, Vector2.ZERO, game.getConfig().getEnvironmentRadius() - 1f);
//			for (IPlayerState ps : game.getCurrentState().getPlayerStates()) {
//				for (int i = 0; i < 10; i++) {
//					ps.addCircle(new Circle(ps.getPlayer(),
//							CircleUtil.randomLocWithinCircle(bound), 1f));
//				}
//			}
			
		} catch (Throwable t) {
			handleAppError(t);
		}		
	}

	public void addPauseListener(Function1<Boolean> listener) {
		pauseListeners.add(listener);
	}
	
	public void removePauseListener(Function1<Boolean> listener) {
		pauseListeners.remove(listener);
	}
	
	public boolean isPausedBySystem() {
		return pausedBySystem;
	}
	
	@Override
	public void dispose() {
//		Disposer.cleanup();
//		Assets.unloadAll();
		CirclesGlobal.shapeRenderer.dispose();
		super.dispose();
	}

	@Override
	public void pause() {
		pausedBySystem = true;
		super.pause();
		notifyPauseListeners();
	}

	@Override
	public void resume() {
		Assets.assetMgr.finishLoading();
		pausedBySystem = false;
		// TODO: switch to Loading screen while assets reload (Texture.setAssetManager(assets))
		super.resume();
		notifyPauseListeners();
	}

	private void notifyPauseListeners() {
		for (Function1<Boolean> listener : pauseListeners) {
			listener.run(pausedBySystem);
		}
	}

	private long lastRenderTime = 0;
	private long msPerFrame = 1000 / 8;
	
	@Override
	public void render() {
		if (System.currentTimeMillis() < lastRenderTime + msPerFrame) {
			try {
				final long dur = lastRenderTime + msPerFrame - System.currentTimeMillis();
				Thread.sleep(dur);
			} catch (InterruptedException e) {
				LOGGER.warn(e);
				Gdx.app.exit();
			}
		}
			
		try {
			Disposer.cleanup();
			super.render();
		} catch (Throwable t) {
			handleAppError(t);
		}
	}

	@Override
	public void setScreen(Screen screen) {
		LOGGER.info("setScreen: " + screen.getClass().getSimpleName());
		if (prevScreenEventName != null) {
			CirclesGlobal.flurry.endTimedEvent(prevScreenEventName);
		}
		prevScreenEventName = "Screen Change:" + screen.getClass().getSimpleName();
		CirclesGlobal.flurry.logEvent(prevScreenEventName, true);
		CirclesGlobal.flurry.onPageView();
		
		CirclesGlobal.input.clear();
		CirclesGlobal.input.enable(true);
		
		super.setScreen(screen);
		CirclesGlobal.appRestorer.saveState(screen.getClass(), null);
	}
	

//	static boolean alreadyTried = false;
	
	private void handleAppError(Throwable t) {
		LOGGER.error("Application shutting down because of error", t);
		try {
			dispose();
			// TODO: wish i could shut down the audio! ...
//			if (!alreadyTried) {
//				alreadyTried = true;
//				Gdx.app.exit();
//			} else {
//				System.exit(2);
//			}
		} catch (Throwable e) {
			CirclesGlobal.flurry.onError("Critical", e.getMessage(), e);
			System.exit(1);
		}
		throw new RuntimeException(t);	// Need to rethrow so that OUYA will report crash to me!
	}
}
