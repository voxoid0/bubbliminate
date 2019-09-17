package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.voxoid.bubbliminate.core.model.ICircle;


public class TransparentTextureRegionActor extends TextureRegionActor {

	private ICircle circle;
	
	public TransparentTextureRegionActor(TextureRegion textureRegion, float alpha) {
		super(textureRegion);
		setColor(0f, 0f, 0f, 1f);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
}
