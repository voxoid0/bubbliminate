package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.voxoid.bubbliminate.CirclesGlobal;

public class RectangleActor extends Group {

	private float x, y, w, h;
	private Color color;
	
	public RectangleActor(float x, float y, float w, float h, Color color) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.color = color;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		setWidth(w);
		setHeight(h);
		
		batch.end();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_BLEND_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		ShapeRenderer shapeRenderer = CirclesGlobal.shapeRenderer;
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.rect(x,  y,  w, h, color, color, color, color);
		
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		batch.begin();
		super.draw(batch, parentAlpha);
	}
}
