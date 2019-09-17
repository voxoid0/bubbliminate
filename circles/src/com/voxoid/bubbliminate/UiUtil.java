package com.voxoid.bubbliminate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.voxoid.bubbliminate.ControllerAssets.Control;
import com.voxoid.bubbliminate.actors.BubbleFloatAction;
import com.voxoid.bubbliminate.actors.CircleActor;
import com.voxoid.bubbliminate.actors.UiCircleActor;

public class UiUtil {

	/**
	 * Adds console controller icon and description with the default description color (OUYA)
	 * @param controlList
	 * @param control
	 * @param desc
	 * @param descColor
	 */
	public static void addControlOuya(Table controlList, Control control, String desc) {
		addControlOuya(controlList, control, desc, Color.WHITE);
 	}
 	
	/**
	 * Adds console controller icon and description (OUYA)
	 * @param controlList
	 * @param control
	 * @param desc
	 * @param descColor
	 */
 	public static void addControlOuya(Table controlList, final Control control, final String desc, Color descColor) {
 		
		float iconSize = Gdx.graphics.getHeight() / 16.875f;	// 64 in 1080p
		controlList.add(createControlImageOuya(control)).width(iconSize).height(iconSize);
		//.uniformX().height(Assets.mediumGameFont.getLineHeight() * 1.5f)
		
		final Label label = new Label(desc, Assets.skin, Assets.LABEL_STYLE_NORMAL);
		label.setColor(descColor);
		
		controlList.add(label).align(Align.left);
 	}	
 	
 	/***
 	 * Creates a console controller image for the given control (OUYA)
 	 * @param control
 	 * @return
 	 */
	public static Image createControlImageOuya(final Control control) {
		return new Image(
				new TextureRegionDrawable(
						Assets.controller.getImage(control)),
				Scaling.fit,
				Align.center);
	}

	public static Actor createBackButton(final Runnable onClick) {
		return createUiCircleButton("Back", Gdx.graphics.getWidth() * 0.1f,
				ActorUtil.isScreenPortrait() ? Gdx.graphics.getHeight() * (1 - 0.15f) : 0,
				onClick);
	}
	public static Actor createNextButton(final Runnable onClick) {
		return createUiCircleButton("Next", Gdx.graphics.getWidth() * (1f - 0.1f), 
				ActorUtil.isScreenPortrait() ? Gdx.graphics.getHeight() * 0.15f : 0,
						onClick);
	}
	
	public static Actor createUiCircleButton(String text, float x, float y, final Runnable onClick) {
		if (y == 0f) {
			y = Gdx.graphics.getHeight() / 2f;
		}
		
		Table group = new Table();
		float radius = ActorUtil.getScreenLongDim() * 0.05f;
		group.setBounds(x - radius, y - radius, radius*2f, radius*2f);
		
		Actor circle = new UiCircleActor(new Color(17f/255f, 189f/255f, 1f, 1f), radius, radius, radius, true);
		group.addActor(circle);
		
		Label label = new Label(text, Assets.skin, Assets.LABEL_STYLE_MENU_ITEM);
		label.setAlignment(Align.center, Align.center);
		ActorUtil.centerActorOrigin(label);
		ActorUtil.centerActor(label);
		
		group.add(label).center().expand();
		
		group.setTouchable(Touchable.enabled);
		label.setTouchable(Touchable.disabled);
		circle.setTouchable(Touchable.disabled);
		group.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				onClick.run();
				return true;
			}

			public void clicked(InputEvent event, float x, float y) {
				onClick.run();
			}
			
		});
		group.debug();
		group.debugTable();
		
		group.addAction(new BubbleFloatAction(
				new Vector2(group.getX(),  group.getY()),
				new Vector2((float)(20 + Math.random()*4),
						(float)(20 + Math.random()*4)),
				new Vector2(Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getHeight() * 0.06f),
				new Vector2((float)(Math.random() * Math.PI), (float)(Math.random() * Math.PI))));
		
//		actor.addAction(new SinuMoveAction())
		return group;
	}
}
