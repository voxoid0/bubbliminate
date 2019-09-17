package com.voxoid.bubbliminate.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.voxoid.bubbliminate.core.model.Circle;
import com.voxoid.bubbliminate.core.model.Game;
import com.voxoid.bubbliminate.core.model.GameHistory;
import com.voxoid.bubbliminate.core.model.GameState;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.model.Player;
import com.voxoid.bubbliminate.core.model.PlayerState;
import com.voxoid.bubbliminate.core.rules.GrowMove;
import com.voxoid.bubbliminate.core.rules.MoveMove;
import com.voxoid.bubbliminate.core.rules.SplitMove;

import com.thoughtworks.xstream.XStream;

public class GameSerializer {

	private XStream xstream;
	
	public GameSerializer() {
		xstream = createXstream();
	}

	/**
	 * Creates an XStream instance for serializing a game or its components, with the appropriate class aliases.
	 * @return
	 */
	public static XStream createXstream() {
		XStream xstream = new XStream();
		xstream.setClassLoader(GameSerializer.class.getClassLoader());
		xstream.alias("Game", Game.class);
		xstream.alias("Circle", Circle.class);
		xstream.alias("GameState", GameState.class);
		xstream.alias("GameHistory", GameHistory.class);
		xstream.alias("Player", Player.class);
		xstream.alias("PlayerState", PlayerState.class);
		xstream.alias("MoveMove", MoveMove.class);
		xstream.alias("GrowMove", GrowMove.class);
		xstream.alias("SplitMove", SplitMove.class);
		return xstream;
	}
	
	public IGame open(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		IGame game = (IGame) xstream.fromXML(in);
		in.close();
		return game;
	}
	
	public void save(IGame game, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		xstream.toXML(game, out);
		out.close();
	}
}
