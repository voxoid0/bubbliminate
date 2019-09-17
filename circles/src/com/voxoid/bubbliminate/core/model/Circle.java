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

import org.apache.commons.lang.Validate;

import com.voxoid.bubbliminate.core.Vector2;


/**
 * @author jbecker
 *
 */
public class Circle implements ICircle {

	private Vector2 location;
	private float radius;
	private IPlayer player;

	/** Constructor. */
	public Circle(IPlayer player, Vector2 location, float radius) {
		Validate.notNull(player);
		this.player = player;
		Validate.notNull(location);
		this.location = location;
		Validate.notNull(radius);
		this.radius = radius;
	}
	
	/** For de-serialization only. */
	public Circle() {}

	/** {@inheritDoc} */
	@Override
	public Vector2 getLocation() {
		return location;
	}

	/** {@inheritDoc} */
	@Override
	public float getRadius() {
		return radius;
	}
	
	@Override
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return String.format("Circle { Player %d at (%.2f,%.2f) radius %.2f }",
				player.getIndex() + 1, location.x, location.y, radius);
	}
}
