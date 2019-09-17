package com.voxoid.bubbliminate.actors;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.voxoid.bubbliminate.ActorUtil;
import com.voxoid.bubbliminate.Assets;

public class FadeActor extends TextureRegionActor {
	private Color fadeColor;
	private Color clearColor;
	
	public FadeActor(Stage stage, Color fadeColor) {
		super(Assets.whiteTexture);
		setTouchable(Touchable.disabled);
		Validate.notNull(fadeColor);
		this.fadeColor = fadeColor;
		clearColor = new Color(fadeColor.r, fadeColor.g, fadeColor.b, 0f);
		setColor(clearColor);
		ActorUtil.fillScreen(this);
		setZIndex(1000000);
		stage.addActor(this);
	}
	
	public void fadeIn(float duration, Runnable after) {
		clearActions();
		setColor(fadeColor);
		addAction(Actions.sequence(
				getFadeInAction(duration),
				//Actions.removeActor(this),
				after == null ? Actions.delay(0f) : Actions.run(after)));
	}

	public Action getFadeInAction(float duration) {
		return Actions.alpha(0f, duration, Interpolation.linear);
	}
	
	public void fadeOut(float duration, Runnable after) {
		clearActions();
		setColor(clearColor);
		addAction(Actions.sequence(
				getFadeOutAction(duration),
				after == null ? Actions.delay(0f) : Actions.run(after)));
	}

	public Action getFadeOutAction(float duration) {
		return Actions.alpha(1f, duration, Interpolation.linear);
	}
}
