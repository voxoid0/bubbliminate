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
package com.voxoid.bubbliminate.core;


/**
 * @author jbecker
 *
 */
public class PolarCoords {

	private Angle angle;
	private float radius;
	
	public PolarCoords(Angle angle, float radius) {
		this.angle = angle;
		this.radius = radius;
	}
	
	public PolarCoords(Vector2 point) {
		angle = Angle.fromRadians((float) Math.atan2(point.y, point.x));
		radius = (float) Math.sqrt(point.x * point.x + point.y * point.y);
	}
	
	public Angle getAngle() {
		return angle;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public Vector2 toVector2() {
		float x = (float) Math.cos(angle.getRadians()) * radius;
		float y = (float) Math.sin(angle.getRadians()) * radius;
		return new Vector2(x, y);
	}
	
	/** Moves the given point by this polar vector. */
	public Vector2 movePoint(Vector2 old) {
		Vector2 delta = toVector2();
		return new Vector2(old.x + delta.x, old.y + delta.y);
	}
}
