package com.voxoid.bubbliminate.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MutableGameStateDiff extends GameStateDiff {

	public MutableGameStateDiff() {
		super(Collections.<ICircle>emptyList(), Collections.<ICircle>emptyList(),
				Collections.<ICircle>emptyList());
	}

	public MutableGameStateDiff(Collection<ICircle> movedOld,
			Collection<ICircle> movedNew, Collection<ICircle> popped) {
		super(movedOld, movedNew, popped);
	}

	public void setMovedOld(Collection<ICircle> movedOld) {
		this.movedOld = new ArrayList<ICircle>(movedOld);
	}

	public void setMovedNew(Collection<ICircle> movedNew) {
		this.movedNew = new ArrayList<ICircle>(movedNew);
	}

	public void setDestroyed(Collection<ICircle> popped) {
		this.popped = new ArrayList<ICircle>(popped);
	}
	
	public void setShrunk(Collection<ICircle> shrunk) {
		this.shrunk = new ArrayList<ICircle>(shrunk);
	}
}
