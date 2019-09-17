package com.voxoid.bubbliminate.core;

import com.voxoid.bubbliminate.core.model.IGame;

public interface IAppController {
	void addDifferentGameListener(IDifferentGameListener listener);
	void removeDifferentGameListener(IDifferentGameListener listener);
	void setCurrentGame(IGame game);
	IGame getCurrGame();
}
