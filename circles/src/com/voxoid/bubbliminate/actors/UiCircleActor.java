package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.voxoid.bubbliminate.Assets;


public class UiCircleActor extends TextureRegionActor {
	
	public UiCircleActor(Color color, float x, float y, float radius, boolean glare) {
		super(glare ?
				new Texture[] { Assets.bubbleBase, Assets.bubbleGlare } :
				new Texture[] { Assets.bubbleBase });

		setColor(color);
		update(x, y, radius);
	}

	public void update(float x, float y, float radius) {
		setPosition(x, y);
		setWidth(radius * 2f);
		setHeight(radius * 2f);
		setOrigin(radius, radius);
	}
}
