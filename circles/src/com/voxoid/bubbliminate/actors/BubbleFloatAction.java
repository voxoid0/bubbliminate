package com.voxoid.bubbliminate.actors;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;

public class BubbleFloatAction extends Action {

	private Vector2 centralPosition;
	private Vector2 wavelength;
	private Vector2 magnitude;
	private Vector2 angleOffset;
	private float t;
	
	public BubbleFloatAction(Vector2 centralPosition, Vector2 wavelength, Vector2 magnitude, Vector2 angleOffset) {
		Validate.notNull(centralPosition);
		this.centralPosition = centralPosition;
		this.wavelength = wavelength;
		this.magnitude = magnitude;
		this.angleOffset = angleOffset;
		t = 0f;
	}
	
	@Override
	public boolean act(float delta) {
		t += delta;
		actor.setX((float) (Math.sin(t * Math.PI * 2.0 / wavelength.x + angleOffset.x) * magnitude.x + centralPosition.x));
		actor.setY((float) (Math.sin(t * Math.PI * 2.0 / wavelength.y + angleOffset.y) * magnitude.y + centralPosition.y));
		return false;
	}
}
