/**
 * Copyright (c) 1996-2009 by 21st Century Systems Inc. All rights reserved.
 *
 * Data and materials contained herein are proprietary to 21st Century Systems, Inc.
 * and may contain trade secrets or patented technology.
 *
 * Use is subject to the software license agreement contained in or referred to in
 * this plug-ins about.html file. Please refer any questions to:
 *
 *
 * 21st Century Systems, Inc.
 * 2611 Jefferson Davis Highway, Suite 111000,
 * Arlington, VA 22202
 *
 * $Id$
 */
package com.voxoid.bubbliminate.core.model;

import java.util.Collection;

import com.voxoid.bubbliminate.core.Vector2;
import com.voxoid.bubbliminate.core.ai.minimax.IGameState;


/**
 * @author jbecker
 *
 */
public interface ICirclesGameState extends IGameState {
	IGame getGame();
	
	/**
	 * 0-based
	 * @return
	 */
	int getPlayerToMove();
	void setPlayerToMove(int player);
	IPlayerState getPlayerState(int player);
	int getNumCircles();
	Collection<IPlayerState> getPlayerStates();
	ICircle circleAt(Vector2 loc);
}
