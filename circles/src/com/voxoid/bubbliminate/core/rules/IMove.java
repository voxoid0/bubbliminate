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
package com.voxoid.bubbliminate.core.rules;

import com.voxoid.bubbliminate.core.ai.minimax.IGameMove;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.model.MutableGameStateDiff;

/**
 * @author jbecker
 *
 */
public interface IMove extends IGameMove {
	ICirclesGameState make(ICirclesGameState old, MutableGameStateDiff diffOut);
	String describe();
//	MoveType getType();
	IPlayer getPlayer();
	ICircle getCircle();
	MoveType getMoveType();
	boolean equals(IMove other, float distEpsilon, float radianEpsilon);
}
