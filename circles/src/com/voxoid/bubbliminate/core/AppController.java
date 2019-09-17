package com.voxoid.bubbliminate.core;

import java.util.LinkedList;
import java.util.List;

import com.voxoid.bubbliminate.core.model.IGame;

public class AppController implements IAppController {

	private List<IDifferentGameListener> diffGameListeners = new LinkedList<IDifferentGameListener>();
	private IGame game;
	
	@Override
	public void addDifferentGameListener(IDifferentGameListener listener) {
		diffGameListeners.add(listener);
		if (game != null) {
			listener.differentGame(game);
		}
	}

	@Override
	public void removeDifferentGameListener(IDifferentGameListener listener) {
		diffGameListeners.remove(listener);
	}

	@Override
	public void setCurrentGame(IGame game) {
		this.game = game;
		notifyDiffGame();
	}
	
	@Override
	public IGame getCurrGame() {
		return game;
	}
	
	private void notifyDiffGame() {
		for (IDifferentGameListener listener : diffGameListeners) {
			listener.differentGame(game);
		}
	}
}
