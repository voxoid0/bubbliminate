package com.voxoid.bubbliminate.gameplay;

import com.voxoid.bubbliminate.SimpleTransform2;

public interface IGameView {

	/**
	 * Sets the game environment transform, clamping the scale and translation to appropriate bounds.
	 * @param transf
	 */
	public abstract void setEnvirTransform(SimpleTransform2 transf);

	public abstract SimpleTransform2 getEnvirTransform();

}