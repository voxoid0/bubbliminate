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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author jbecker
 *
 */
public class PlayerState implements IPlayerState {

	private IPlayer player;
	private List<ICircle> circles;

	
	/** For de-serialization only. */
	public PlayerState() {
	}
	
	/** Constructor. */
	public PlayerState(IPlayer player) {
		Validate.notNull(player);
		this.player = player;
		circles = new LinkedList<ICircle>();
	}
	
	/** Copy constructor. */
	public PlayerState(IPlayerState dub) {
		this.player = dub.getPlayer();
		circles = new LinkedList<ICircle>(dub.getCircles());
	}
	
	@Override
	public IPlayer getPlayer() {
		return player;
	}
	
	/** {@inheritDoc} */
	@Override
	public void addCircle(ICircle circle) {
		if (circle.getPlayer() != player) {
			throw new IllegalArgumentException("Circle must belong to this player");
		}
		circles.add(circle);
	}

	/** {@inheritDoc} */
	@Override
	public void addCircles(List<ICircle> circleCollection) {
		circles.addAll(circleCollection);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<ICircle> getCircles() {
		return Collections.unmodifiableList(circles);
	}

	/** {@inheritDoc} */
	@Override
	public int getNumCircles() {
		return circles.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAlive() {
		return getNumCircles() > 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public void removeCircle(ICircle circle) {
		if(!circles.remove(circle)) {
			throw new IllegalArgumentException("Circle did not exist for this player state");
		}
	}
	
	public void removeAllCircles() {
		circles.clear();
	}
}
