package com.voxoid.bubbliminate.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.voxoid.bubbliminate.ActorUtil;
import com.voxoid.bubbliminate.Assets;

public class HelpTextActor extends Table {

	private static final float SLIDE_IN_DURATION = 0.5f;
	private float textX;
	private float textY;
	private float textXHidden;
	private Table mainTable;
	private Label label;
	private Actor content;

	public HelpTextActor() {
		setBackground(Assets.window9Patch);
		mainTable = new Table();
		add(mainTable).pad(-20f).expand().fill();
		label = new Label("", Assets.skin, Assets.LABEL_STYLE_NORMAL);
		label.setAlignment(Align.top | Align.left);
		mainTable.add(label).top().left().expand().fill();
		content = label;
		
		setPosition(Gdx.graphics.getWidth(), 0);
	}
	
	/**
	 * Sets the help text's home position (after scrolling and what not). Will not take effect
	 * until the next time the text is changed.
	 * 
	 * @param x
	 * @param y
	 */
	private void setHomePosition(float x, float y) {
		textX = x;
		textY = y;
		textXHidden = -getWidth();
		label.setWrap(true);
	}
	
	public void hide() {
		setX(textXHidden);
		setVisible(false);
	}
	
	public void setHelpSequence(Object[] content, float delayBetw) {
		List<Runnable> runnables = new ArrayList<Runnable>();
		for (Object o : content) {
			if (o instanceof String) {
				runnables.add(createChangeToTextRunnable((String) o));
			} else if (o instanceof Table) {
				runnables.add(createChangeToContentRunnable((Table) o));
			} else {
				throw new IllegalArgumentException("Each content object must be either a String or a Table");
			}
		}
		changeContentMulti(runnables.toArray(new Runnable[0]), delayBetw);
	}
	
	public void setHelpText(final String text) {
		this.content = label;
		if (!StringUtils.isEmpty(text)) {
			changeContent(createChangeToTextRunnable(text));
		} else {
			changeContent(null);
		}
	}
	
	public String getHelpText() {
		return content instanceof Label ? ((Label) content).getText().toString() : "";
	}

	private Runnable createChangeToTextRunnable(final String text) {
		return new Runnable() {
			public void run() {
				mainTable.clearChildren();
				mainTable.add(label).top().left().expand().fill();
				label.setText(text);
				updatePosition();
			}
		};
	}
	
	public void setHelpContent(final Table content) {
		this.content = content;
		changeContent(createChangeToContentRunnable(content));
	}

	private Runnable createChangeToContentRunnable(final Table content) {
		return new Runnable() {
			public void run() {
				mainTable.clearChildren();
				mainTable.add(content).expand().fill();
				updatePosition();
			}
		};
	}
	
	public Actor getHelpContent() {
		return content;
	}
	
	public void updatePosition() {
//		float helpX = Gdx.graphics.getWidth()*0.95f - getWidth() + 20f;
		float helpX = 8f; //(Gdx.graphics.getWidth() + Gdx.graphics.getHeight()) / 2f - 16f;
		float helpY = 8f; //Gdx.graphics.getHeight()*0.05f;
		setOrigin(0, 0);
		setHomePosition(helpX, helpY);
//		pack();
		setWidth(ActorUtil.getScreenLongDim() * 0.25f); // * 0.95f - helpX);
		setHeight(ActorUtil.getScreenShortDim() * 0.3f); //* 0.9f * 0.7f);
	}
	
	private void changeContent(final Runnable runnable) {
		changeContentMulti(new Runnable[] { runnable }, 0f);
	}
	
	private void changeContentMulti(final Runnable[] runnables, float delayBetw) {
		clearActions();
		for (Runnable runnable : runnables) {
			Action action = Actions.after(Actions.sequence(
				Actions.moveTo(textXHidden, getY(), SLIDE_IN_DURATION, Interpolation.sineIn),
				(runnable != null ?
					Actions.sequence(
						Actions.moveTo(textXHidden, textY, 0f),
						Actions.show(),
						Actions.run(runnable),
						Actions.moveTo(textX, textY, SLIDE_IN_DURATION, Interpolation.sineOut)
					) :
					Actions.delay(0f)
				),
				Actions.delay(delayBetw)
			));
			addAction(action);
		}
	}
}
