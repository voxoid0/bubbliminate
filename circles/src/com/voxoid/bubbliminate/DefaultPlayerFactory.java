package com.voxoid.bubbliminate;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.voxoid.bubbliminate.core.ai.CirclesAreaCalc;
import com.voxoid.bubbliminate.core.ai.ComplexGamePositionEvaluator;
import com.voxoid.bubbliminate.core.ai.CpuPlayerBuilder;
import com.voxoid.bubbliminate.core.ai.GamePositionEvaluator;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.Player;

public class DefaultPlayerFactory implements IPlayerFactory {
	/** Maximum time allowed for the CPU to make its move, in seconds. */
	public static final float MAX_CPU_PLAYER_WAIT_TIME = 30f; //24f*60f*60f; //10f;
	
	public static final Color PLAYER_COLOR[] = new Color[] {
		new Color(1f, 4f/255f, 136f/255f, 1f),	// pink
		new Color(1f, 148f/255f, 51f/255f, 1f), // orange   ff9433ff
		new Color(1f, 1f, 0f, 1f), // yellow
		new Color(147f/255f, 1f, 0f, 1f), // greener yellow-green	93ff00ff , used to distinguish from yellow in 6-player game
		
		new Color(17f/255f, 189f/255f, 1f, 1f), // turquoise blue	11bfffff
		new Color(104f/255f, 23f/255f, 1f, 1f), // purple-violet
		new Color(217f/255f, 1f, 0f, 1f), // yellow-green
		new Color(0f, 0.25f, 1f, 1f), 	// blue
		
		new Color(1f, 0.25f, 0f, 1f),	// red-orange
//		new Color(0f, 1f, 0.75f, 1f), 	// aqua green
	};

	private static final int COLOR_INDEX[][] = new int[][] {
		new int[] {},
		new int[] { 4 },
		new int[] { 0, 6 },
		new int[] { 0, 6, 4},
		new int[] { 0, 2, 4, 5 },
		new int[] { 0, 1, 6, 4, 5},
		new int[] { 0, 1, 2, 3, 4, 5},
		new int[] { 0, 8, 1, 2, 3, 4, 5 },
		new int[] { 0, 8, 1, 2, 3, 7, 4, 5 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5},
		new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5},
		new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5},
		new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5},
		new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5},
	};
	
	private float minWaitTime = 2f;
	
	public void setMinWaitTime(float s) {
		minWaitTime = s;
	}
	
	@Override
	public List<IPlayer> createPlayers(GameConfig gameConfig) {
		List<IPlayer> players = new ArrayList<IPlayer>();
		int num = 0;
		
		for (PlayerConfig config : gameConfig.getPlayerConfigs()) {
			IPlayer player;
			String name = "Player " + (num + 1);
			Color color = getPlayerColor(num, gameConfig.getNumPlayers());
			
			switch (config.type) {
			case HUMAN:
				player = new Player(name, num, color);
				break;
			case CPU:
//				player = new CpuPlayerBuilder().ply(8).movesGenerator(new MovesGenerator(new GameConfig(2), 1, 4, 4))				
				player = new CpuPlayerBuilder()
						.ply(2)
						.bestMoveOrdinal(PlayerConfig.MAX_CPU_LEVEL - PlayerConfig.MIN_CPU_LEVEL + 1 - config.cpuLevel + 1)
						.minWaitTime(minWaitTime)
						.maxWaitTime(MAX_CPU_PLAYER_WAIT_TIME)
						.gamePositionEvaluator(config.cpuLevel > 2 ?
								new ComplexGamePositionEvaluator(new CirclesAreaCalc(gameConfig.getMinCircleRadius())) :
								new GamePositionEvaluator())
						.build(gameConfig, name, num, color);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			players.add(player);
			num++;
		}
		
		return players;
	}

	private Color getPlayerColor(int playerIndex, int nPlayers) {
		return PLAYER_COLOR[ COLOR_INDEX[nPlayers][playerIndex] ];
	}	
}
