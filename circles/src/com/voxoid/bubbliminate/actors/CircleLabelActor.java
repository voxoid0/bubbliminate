package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin.TintedDrawable;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;

public class CircleLabelActor extends TextActor {

	private static final float MIN_FONT_HEIGHT_INCHES = 1f/16f;
	
	private CircleActor circleActor;
	public CircleLabelActor(String text, CircleActor circleActor) {
		super(Assets.tinyFont, Color.WHITE.cpy(), text);
		this.circleActor = circleActor;
		setScale();
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		setPosition(
				circleActor.getX() - getWidth() / 2,
				circleActor.getY() + getHeight() / 2);
		super.draw(batch, parentAlpha);
	}
	
	private void setScale() {
		float minFontSizePixels = CirclesGlobal.platform.inchesToPixels(MIN_FONT_HEIGHT_INCHES);
//		float scale = getHeight() < minFontSizePixels ? minFontSizePixels / getHeight() : 1f;
		setFont(getHeight() < minFontSizePixels ? Assets.smallNormalFont : Assets.tinyFont);
	}
}
