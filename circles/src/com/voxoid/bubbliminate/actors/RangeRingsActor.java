package com.voxoid.bubbliminate.actors;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.SimpleTransform2;
import com.voxoid.bubbliminate.Vector2Util;
import com.voxoid.bubbliminate.core.model.ICircle;

public class RangeRingsActor extends Actor {

	private static final float RANGE_RING_ALPHA = 0.16f;

	/**
	 * Factor used to darken the player color for use in the range ring, so that
	 * saturated alpha from many range rings of the same color doesn't
	 * camouflage the bubbles.
	 */
	private static final float COLOR_FACTOR = 0.66f;
	
	private ShapeRenderer shapeRenderer;
	private ICircle circle;
	private int nRings;
	private Color color;
	private float lineWidth;
	
	
	public RangeRingsActor(ICircle circle, int nRings, Color color, float lineWidth) {
		Validate.notNull(circle);
		this.circle = circle;
		this.nRings = nRings;
		this.color = color;
		this.lineWidth = lineWidth;
		this.shapeRenderer = CirclesGlobal.shapeRenderer;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.end();
		
		Gdx.gl.glLineWidth(lineWidth);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_BLEND_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
		
		shapeRenderer.begin(ShapeType.Filled);
		
		shapeRenderer.setColor(new Color(color.r * COLOR_FACTOR, color.g * COLOR_FACTOR, color.b * COLOR_FACTOR, RANGE_RING_ALPHA));
		
		float radius = circle.getRadius() + nRings;
		SimpleTransform2 tr = CirclesGlobal.gameEnvirTransform;
		Vector2 loc = tr.transform(Vector2Util.fromCoreVector2(circle.getLocation()));
		shapeRenderer.circle(loc.x, loc.y, radius * tr.getScaleX(), 128);
		
		shapeRenderer.end();
		batch.begin();
	}
}
