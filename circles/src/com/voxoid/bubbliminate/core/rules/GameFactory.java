package com.voxoid.bubbliminate.core.rules;

import java.util.List;

import com.voxoid.bubbliminate.IPlayerFactory;
import com.voxoid.bubbliminate.core.Angle;
import com.voxoid.bubbliminate.core.PolarCoords;
import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.Game;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.GameState;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.IPlayerState;


public class GameFactory {

	public IGame createGame(GameConfig gameConfig, IPlayerFactory playerFactory) {
		List<IPlayer> players = playerFactory.createPlayers(gameConfig);
		IGame game = new Game(players, gameConfig);
		
		//// Create initial state (placing player circles in a circle)
		ICirclesGameState initialState = createInitialState(game, gameConfig);
		game.setCurrentState(initialState);
		game.getHistory().add(initialState, null);
		
		return game;
	}

	private ICirclesGameState createInitialState(IGame game, GameConfig gameConfig) {
		ICirclesGameState initialState = new GameState(game);
		float angleStep = (float) (Math.PI * 2.0) / gameConfig.getNumPlayers();
		float angle = (float) (Math.PI / 2.0);
		for (int i = 0; i < gameConfig.getNumPlayers(); i++) {
			PolarCoords polar = new PolarCoords(Angle.fromRadians(angle), gameConfig.getRingRadius());
			IPlayerState playerState = initialState.getPlayerState(i);
			playerState.addCircle(
					new Circle(playerState.getPlayer(), polar.toVector2(), 1f));
			angle += angleStep;
		}
		return initialState;
	}

}
