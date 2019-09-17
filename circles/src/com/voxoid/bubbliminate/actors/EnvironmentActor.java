package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.SimpleTransform2;

public class EnvironmentActor extends Group {

	private static final Color ENV_BG_COLOR = new Color(0f, 0f, 0f, 0.8f);
	private static final Color ENV_BORDER_COLOR = new Color(0.75f, 0.75f, 0.75f, 1f);
	
	private float envRadius;
	private ShapeRenderer shapeRenderer;
	
	public EnvironmentActor(float envRadius) {
		this.envRadius = envRadius;
		shapeRenderer = CirclesGlobal.shapeRenderer;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		SimpleTransform2 tr = CirclesGlobal.gameEnvirTransform;
		setWidth(envRadius * 2f * tr.getScaleX());
		setHeight(envRadius * 2f * tr.getScaleY());
		
		batch.end();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_BLEND_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		shapeRenderer.begin(ShapeType.Filled);
//		shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.5f));
		
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(ENV_BG_COLOR);
		shapeRenderer.circle(
				tr.transformX(getX()), tr.transformY(getY()), getWidth() / 2f, 128);
		shapeRenderer.end();
		
		Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
		Gdx.gl.glLineWidth(Math.min(8f, Math.max(1f, getWidth() / 100f)));
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(ENV_BORDER_COLOR);
		
		shapeRenderer.circle(
				tr.transformX(getX()), tr.transformY(getY()), getWidth() / 2f, 128);
		
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		batch.begin();
		super.draw(batch, parentAlpha);
	}
}
