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

import java.util.List;

/**
 * @author jbecker
 *
 */
public interface IPlayerState {
	IPlayer getPlayer();
	int getNumCircles();
	
	/**
	 * Returns true if the player is still alove (has 1 or more circles).
	 * @return true if the player is still alove (has 1 or more circles).
	 */
	boolean isAlive();
	
	List<ICircle> getCircles();
	void removeCircle(ICircle circle);
	void addCircle(ICircle circle);
	void addCircles(List<ICircle> circles);
}
