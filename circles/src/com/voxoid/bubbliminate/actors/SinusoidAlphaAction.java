package com.voxoid.bubbliminate.actors;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;

public class SinusoidAlphaAction extends Action {

	private Color color;
	private float wavelength;
	private float magnitude;
	private float offset;
	private float t;
	
	public SinusoidAlphaAction(Color color, float wavelength, float magnitude, float offset) {
		Validate.notNull(color);
		this.color = color;
		this.wavelength = wavelength;
		this.magnitude = magnitude;
		this.offset = offset;
		t = 0f;
	}
	
	@Override
	public boolean act(float delta) {
		t += delta;
		float alpha = (float) (Math.cos(t * Math.PI * 2.0 / wavelength) + 1.0) / 2f * magnitude + offset;
		color.a = Math.max(Math.min(alpha, 1f), 0f); // clamp to [0,1] range
		getActor().setColor(color);
		return false;
	}
}
