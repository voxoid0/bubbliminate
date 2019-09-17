package com.voxoid.bubbliminate;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.thoughtworks.xstream.XStream;
import com.voxoid.bubbliminate.gameplay.GameScreen;

public class AppRestorer {

	private static final String STATE_DATA_KEY = "appRestoreState";
	//public static final AppRestorer INSTANCE = new AppRestorer();
	
	private Class<? extends Screen> screenClass;
	private Object state;
	private IGameData gameData;
	private XStream xstream = CirclesGlobal.systemUtil.createXStream();
	
	private static class StateContainer {
		public Class<? extends Screen> screenClass;
		public Object state;

		/** For de-serialization only. */
		public StateContainer() {
		}
		
		public StateContainer(Class<? extends Screen> screenClass, Object state) {
			super();
			this.screenClass = screenClass;
			this.state = state;
		}
	}
	
	public AppRestorer(IGameData gameData) {
		this.gameData = gameData;
	}

	/**
	 * 
	 * @param screenClass
	 * @param state
	 */
	public synchronized void saveState(Class<? extends Screen> screenClass, Object state) {
//		if (!screenClass.equals(game.getScreen().getClass())) {
//			throw new IllegalArgumentException("Expected screenClass to be " + game.getScreen().getClass().getName() + " but was " + screenClass.getName());
//		}
		if (!screenClass.equals(ReloadingScreen.class)) {
			if (screenClass.equals(GameScreen.class)) { // Only restore game screen (not title screen or config screen etc)
				this.screenClass = screenClass;
				this.state = state;
				
				// Bug Workaround: somehow state is null sometimes for GameScreen! So if that happens, don't save it (which might override previous state that DID record the last 20 moves e.g.)
				if (!(screenClass == GameScreen.class && state == null)) {
					String stateString = xstream.toXML(new StateContainer(screenClass, state));
					gameData.putGameData(STATE_DATA_KEY, stateString);
					
					if (screenClass == null || state == null || stateString == null) {
						Map<String, String> logParams = new HashMap<String, String>();
						logParams.put("screenClass", screenClass == null ? "null" : screenClass.toString());
						logParams.put("state", state == null ? "null" : state.getClass().getSimpleName());
						logParams.put("stateString", stateString.length() > 255 ? stateString.substring(0, 255) : stateString);
						CirclesGlobal.flurry.logEvent("stateSaved", logParams);
					}
				} else {
					CirclesGlobal.flurry.logEvent("stateSaveSkipped", false);
				}
				
			} else {
				CirclesGlobal.flurry.logEvent("clearRestoreState",
						Collections.singletonMap("screenClass", screenClass.getSimpleName()));
				clearRestoreState();
			}
		}
	}
	
	/**
	 * Restores the game screen and screen state, returning true if successful, and false if not successful. In
	 * the latter case the client should set the game's current screen (e.g. the beginning screen).
	 *  
	 * @param game
	 * @return
	 */
	public synchronized void restoreState(final Game game, final Runnable onNoRestore) {
		loadRestoreState();
		if (screenClass == null) {
			onNoRestore.run();
		} else {
			final Object state = this.state;
			final Class<? extends Screen> screenClass = this.screenClass;
			
			game.setScreen(new ReloadingScreen(new Runnable() {
				@Override
				public void run() {
					try {
						Screen screen;
						if (isRestorable(screenClass)) {
							if (state == null) {
								CirclesGlobal.flurry.onError("unableToRestoreApp", "Screen state was still null while screen being restored; saveState() was apparently not called soon enough (from ctor of screen)", "");
								onNoRestore.run();
							}
							Constructor<? extends Screen> ctor = screenClass.getConstructor(state.getClass());
							screen = ctor.newInstance(state);
						} else {
							Constructor<? extends Screen> ctor = screenClass.getConstructor();
							screen = ctor.newInstance();
						}
						game.setScreen(screen);
					} catch (Exception e) {
						// give up and reboot
						CirclesGlobal.flurry.onError("unableToRestoreApp", e.getMessage(), e);
						onNoRestore.run();
					}
				}
			}));
		}
	}

	public static boolean isRestorable(Class<? extends Screen> screenClass) {
		return screenClass.getAnnotation(Restorable.class) != null;
	}
	
	public void clearRestoreState() {
		gameData.putGameData(STATE_DATA_KEY, null);
	}

	private void loadRestoreState() {
		if (screenClass == null || state == null) {
			String stateString = gameData.getGameData(STATE_DATA_KEY);
			if (stateString != null) {
				try {
					StateContainer stateContainer = (StateContainer) xstream.fromXML(stateString);
					
					Map<String, String> logParams = new HashMap<String, String>();
					logParams.put("screenClass", stateContainer.screenClass == null ? "null" : stateContainer.screenClass.toString());
					logParams.put("state", stateContainer.state == null ? "null" : stateContainer.state.getClass().getSimpleName());
					logParams.put("stateString", stateString.length() > 255 ? stateString.substring(0, 255) : stateString);
					logParams.put("screenClass-before", screenClass == null ? "null" : screenClass.toString());
					logParams.put("state-before", state == null ? "null" : state.getClass().getSimpleName());
					CirclesGlobal.flurry.logEvent("stateReloaded", logParams);
					
					state = stateContainer.state;
					screenClass = stateContainer.screenClass;
				} catch (Exception ex) {
					CirclesGlobal.flurry.onError("restoreState", "Unable to restore state from state XML", ex);
				}
			}
		}
	}
}
