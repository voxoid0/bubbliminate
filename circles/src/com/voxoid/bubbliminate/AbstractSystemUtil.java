package com.voxoid.bubbliminate;

import com.thoughtworks.xstream.XStream;
import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.Game;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.GameState;
import com.voxoid.bubbliminate.core.model.Player;
import com.voxoid.bubbliminate.core.model.PlayerState;

public class AbstractSystemUtil {
	private XStream xstream;

	public XStream createXStream() {
		if (xstream == null) {
			xstream = new XStream();
			xstream.alias("PlayerState", PlayerState.class);
			xstream.alias("Circle", Circle.class);
			xstream.alias("Player", Player.class);
			xstream.alias("Game", Game.class);
			xstream.alias("GameConfig", GameConfig.class);
			xstream.alias("GameState", GameState.class);
			xstream.alias("PlayerConfig", PlayerConfig.class);
		}
		return xstream;
	}
}
