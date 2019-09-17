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

import com.badlogic.gdx.graphics.Color;


/**
 * @author jbecker
 *
 */
public class Player implements IPlayer {

	// TODO: remove this and create Circle class without reference to player, and PlayerCircle class like current impl
	public static final Player NONE = new Player("", 0, Color.WHITE.cpy());
	
	private Color color;
	private String name;
	private int index;
	
	public Player(String name, int index, Color color) {
		this.name = name;
		this.index = index;
		this.color = color;
	}
	
	/** For de-serialization only. */
	public Player() {}
	
	/** {@inheritDoc} */
	@Override
	public Color getColor() {
		return color;
	}

	/** {@inheritDoc} */
	@Override
	public int getIndex() {
		return index;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}
}
