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
public class Angle {

	private float radians;
	
	private Angle(float radians) {
		this.radians = radians;
	}
	
	public static Angle fromDegrees(float degrees) {
		return new Angle((float) Math.toRadians(degrees));
	}
	
	public static Angle fromRadians(float radians) {
		return new Angle(radians);
	}
	
	public float getDegrees() {
		return (float) Math.toDegrees(radians);
	}
	
	public float getRadians() {
		return radians;
	}
	
	public Vector2 toVector2() {
		return new Vector2((float) Math.cos(radians), (float) Math.sin(radians));
	}
	
	public float cos() {
		return (float) Math.cos(radians);
	}
	
	public float sin() {
		return (float) Math.sin(radians);
	}
	
	public String formatDegrees(int nDecimalPlaces) {
		String format = String.format("%%.%df\260", nDecimalPlaces);
		return String.format(format, getDegrees());
	}
	
	@Override
	public String toString() {
		return formatDegrees(1);
	}
}
