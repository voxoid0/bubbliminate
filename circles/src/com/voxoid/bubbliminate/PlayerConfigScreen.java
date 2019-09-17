package com.voxoid.bubbliminate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.voxoid.bubbliminate.actors.FadeActor;
import com.voxoid.bubbliminate.actors.TextureRegionActor;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.rules.GameFactory;
import com.voxoid.bubbliminate.core.util.Disposer;
import com.voxoid.bubbliminate.gameplay.GameScreen;
import com.voxoid.bubbliminate.ui.CirclesMenuItem;
import com.voxoid.bubbliminate.ui.IMenu;
import com.voxoid.bubbliminate.ui.IMenuItem;
import com.voxoid.bubbliminate.ui.IMenuListener;
import com.voxoid.bubbliminate.ui.Menu;

/**
 * Lays out the game screen elements and starts things rolling.
 * @author joel.becker
 *
 */
public class PlayerConfigScreen implements Screen {
	
	private Stage stage;
	private Group menusGroup;
	private TextureRegionActor background;
	private Menu numPlayersMenu;
	private Menu playerConfigMenu;
	private Menu activeMenu;
	private List<IMenuItem> configMenuItems;
	private static int[] playerConfigs;
	private static int numPlayers = 3;
	private List<PlayerConfig> configValues;
	private List<String> configValueText;
	private IMenuListener playerConfigMenuListener = new PlayerConfigMenuListener();
	
	public PlayerConfigScreen() {
	}
	
	@Override
	public void show() {
		
		// TODO: load from previous settings
		if (playerConfigs == null) {
			playerConfigs = new int[CirclesGlobal.MAX_PLAYERS];
			playerConfigs[0] = 0;
			for (int i = 1; i < CirclesGlobal.MAX_PLAYERS; i++) {
				playerConfigs[i] = 1;
			}
		}
		
		
		stage = new Stage();
		
		background = new TextureRegionActor(loadBackground());

		ActorUtil.fillScreenKeepingRatio(background);
		stage.addActor(background);
		
//		Table table = createMarginsTable();
////		table.setBounds(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//		stage.addActor(table);
//		table.validate();
		
		menusGroup = new Group();
		menusGroup.setBounds(Gdx.graphics.getWidth()*2, 0, Gdx.graphics.getWidth() * 2, Gdx.graphics.getHeight());
		stage.addActor(menusGroup);

		configValues = new ArrayList<PlayerConfig>();
		configValues.add(PlayerConfig.human());
		for (int i = PlayerConfig.MIN_CPU_LEVEL; i <= PlayerConfig.MAX_CPU_LEVEL; i++) {
			configValues.add(PlayerConfig.cpu(i));
		}
		
		configValueText = Arrays.asList(new String[] { "Human", "CPU - Beginner", "CPU - Intermediate", "CPU - Advanced", "CPU - Expert"});
		if (configValueText.size() != configValues.size()) {
			throw new IllegalStateException();
		}

		createNumPlayersMenu();
		createPlayerConfigMenu(CirclesGlobal.MAX_PLAYERS);
		
		stage.addActor(UiUtil.createBackButton(new Runnable() {
			public void run() {
				Menu active = activeMenu;
				if (active != null) {
					active.goBack();
				}
			}
		}));
		stage.addActor(UiUtil.createNextButton(new Runnable() {
			public void run() {
				Menu active = activeMenu;
				if (active != null) {
					active.selectItem(active.getFocusedMenuItem());
				}
			}
		}));
		
		//		playerConfigs = new ArrayList<PlayerConfig>();
//		playerConfigs.add(PlayerConfig.human());
//		playerConfigs.add(PlayerConfig.cpu(1));
//		playerConfigs.add(PlayerConfig.cpu(1));
//		playerConfigs.add(PlayerConfig.cpu(1));
//		playerConfigs.add(PlayerConfig.cpu(1));
//		playerConfigs.add(PlayerConfig.cpu(1));
		
		updatePlayerConfigMenu();
		
		Assets.playTitleMusic();		
		
		// Slide in
		new FadeActor(stage, Color.BLACK).fadeIn(0.5f,
			new Runnable() {
				@Override
				public void run() {
					menusGroup.addAction(createForwardAction());
					numPlayersMenu.start();
					activeMenu = numPlayersMenu;
				}
			});
	}

	private TextureRegion loadBackground() {
		return TextureUtils.loadNonPotTexture("DistantBackground.jpg", true);
	}
	
	private void createPlayerConfigMenu(int numPlayers) {
		if (playerConfigMenu != null) {
			playerConfigMenu.removeMenuListener(playerConfigMenuListener);
			Disposer.removeAndDispose(playerConfigMenu, menusGroup);
		}
		
		playerConfigMenu = new Menu(Collections.<IMenuItem>emptyList(),
				Controllers.getControllers().<Controller>toArray(Controller.class),
				Align.left, 0, null, new AllInputToInputProcessorAdaptor(stage));
		
		configMenuItems = new ArrayList<IMenuItem>();
		
		for (int i = 0; i < numPlayers; i++) {
		    CirclesMenuItem item = new CirclesMenuItem(playerConfigMenu, "Player 6: CPU - Intermediate");
		    item.setChangeOnTouch(true);
			configMenuItems.add(item);
		}
		playerConfigMenu.setMenuItems(configMenuItems);
		
		menusGroup.addActor(playerConfigMenu);
		ActorUtil.centerActor(playerConfigMenu);
//		playerConfigMenu.setX(Gdx.graphics.getWidth() * 1 / 3);
		playerConfigMenu.setPosition(
				playerConfigMenu.getX() + Gdx.graphics.getWidth()*2f,
				playerConfigMenu.getY());
		
		playerConfigMenu.addMenuListener(playerConfigMenuListener);
		updatePlayerConfigMenu();
	}

	private void createNumPlayersMenu() {
		numPlayersMenu = new Menu(Collections.<IMenuItem>emptyList(),
				Controllers.getControllers().<Controller>toArray(Controller.class),
				Align.center, 0, null, new AllInputToInputProcessorAdaptor(stage));
		
		List<IMenuItem> numPlayersMenuItems = new ArrayList<IMenuItem>();
		for (int i = 2; i <= CirclesGlobal.MAX_PLAYERS; i++) {
			numPlayersMenuItems.add(new CirclesMenuItem(numPlayersMenu, String.format("%d Players", i)));
		}
		numPlayersMenu.setMenuItems(numPlayersMenuItems);
		
		menusGroup.addActor(numPlayersMenu);
		ActorUtil.centerActor(numPlayersMenu);
		
		numPlayersMenu.addMenuListener(new NumPlayersMenuListener());
		numPlayersMenu.setFocusedItem(numPlayersMenuItems.get(numPlayers - 2));	// Recall last selected
	}

	private void updatePlayerConfigMenu() {
		for (int i = 0; i < numPlayers; i++) {
			configMenuItems.get(i).setItemText(String.format("Player %d: %s",
					(i + 1), configValueText.get(playerConfigs[i])));
		}
	}
	
	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(0f, 0f, 0f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
		//Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		// nothing
	}

	@Override
	public void resume() {
		Assets.reassignTextureReferences();
		background.getTextureRegion().getTexture().dispose();
		background.setTextureRegion(loadBackground());
	}

	@Override
	public void dispose() {
		stage.dispose();
		background.getTextureRegion().getTexture().dispose();
	}

	private Action createForwardAction() {
		return Actions.moveBy(-Gdx.graphics.getWidth()*2f, 0, 1f, Interpolation.sine);
	}
	
	private Action createBackAction() {
		return Actions.moveBy(Gdx.graphics.getWidth()*2f, 0, 1f, Interpolation.sine);
	}
	
	private class NumPlayersMenuListener implements IMenuListener {

		@Override
		public void onSelected(IMenu source, IMenuItem selection) {
			numPlayers = source.indexOf(selection) + 2;
			
			//// Disallow non-demo features in demo version
			CirclesGlobal.demoHelper.setGameConfig(new GameConfig(numPlayers));
			if (!CirclesGlobal.demoHelper.allowedToPlay()) {
				CirclesGlobal.demoHelper.showRestrictionsMessages(stage, new Runnable() {
					public void run() {
						if (CirclesGlobal.demoHelper.allowedToPlay()) {
							goToPlayerConfigMenu();
						}
					}
				});
			} else {
				goToPlayerConfigMenu();
			}
		}

		private void goToPlayerConfigMenu() {
			createPlayerConfigMenu(numPlayers);
			numPlayersMenu.stop();
			playerConfigMenu.start();
			activeMenu = playerConfigMenu;
			menusGroup.addAction(createForwardAction());
		}

		@Override
		public void onBack(IMenu source) {
			activeMenu = null;
			// Slide in
			menusGroup.addAction(Actions.sequence(createBackAction(), Actions.run(new Runnable() {
				@Override
				public void run() {
					new FadeActor(stage, Color.BLACK).fadeOut(1f, new Runnable() {
						public void run() {
							stage.clear();
							((Game)Gdx.app.getApplicationListener()).setScreen(new TitleScreen(Color.BLACK));
						}
					});
				}
			})));
		}

		@Override
		public void onChanged(IMenu source, IMenuItem item, int changeDir) {
			// nothing
		}
	}
	
	public static IGame createGame(GameConfig gameConfig) {
		IPlayerFactory playerFactory = new DefaultPlayerFactory();
		final IGame game = new GameFactory().createGame(gameConfig, playerFactory);
		return game;
	}
	
	private class PlayerConfigMenuListener implements IMenuListener {

		@Override
		public void onSelected(IMenu source, IMenuItem selection) {
			final IGame game = createGame();

			//// Disallow non-demo features in demo version
			CirclesGlobal.demoHelper.finishPurchaseInit(stage, new Runnable() {
				public void run() {
					CirclesGlobal.demoHelper.setGameConfig(game.getConfig());
					if (!CirclesGlobal.demoHelper.allowedToPlay()) {
						CirclesGlobal.demoHelper.showRestrictionsMessages(stage, new Runnable() {
							public void run() {
								if (CirclesGlobal.demoHelper.allowedToPlay()) {
									completeConfig(game);
								}
							}
						});
					} else {
						completeConfig(game);
					}
				}
			});
		}
		
		private void completeConfig(final IGame game) {
			playerConfigMenu.stop();
			activeMenu = null;

			// Slide in
			menusGroup.addAction(Actions.sequence(
					createForwardAction(),
					Actions.run(new Runnable() {
						@Override
						public void run() {
							new FadeActor(stage, Color.BLACK).fadeOut(1f, new Runnable() {
								public void run() {
									Assets.titleMusic.stop();
									((Game)Gdx.app.getApplicationListener()).setScreen(new GameScreen(game));
								}
							});
						}
					})
			));
		}

		private IGame createGame() {
			List<PlayerConfig> pc = new ArrayList<PlayerConfig>();
			for (int i = 0; i < numPlayers; i++) {
				pc.add(configValues.get(playerConfigs[i]));
			}
			GameConfig gameConfig = new GameConfig(pc);
			Logger.getLogger(PlayerConfigScreen.class).info("Creating game with config: " + gameConfig);
			
			final IGame game = PlayerConfigScreen.createGame(gameConfig);
			return game;
		}


		@Override
		public void onBack(IMenu source) {
			playerConfigMenu.stop();
			numPlayersMenu.start();
			activeMenu = numPlayersMenu;
			menusGroup.addAction(createBackAction());
		}

		@Override
		public void onChanged(IMenu source, IMenuItem item, int changeDir) {
			int index = source.indexOf(item); // - 2;
			if (index != -1) {
				playerConfigs[index] = (playerConfigs[index] + changeDir + configValues.size()) % configValues.size();
				updatePlayerConfigMenu();
			}
		}
		
	}
}
