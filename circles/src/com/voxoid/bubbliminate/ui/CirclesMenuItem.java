package com.voxoid.bubbliminate.ui;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.voxoid.bubbliminate.Assets;

public class CirclesMenuItem extends Label implements IMenuItem {

	public static final Color NORMAL_COLOR = Color.GRAY;
	public static final Color FOCUSED_COLOR = Color.WHITE.cpy();
	public static final Color DISABLED_COLOR = new Color(1f, 1f, 1f, 0.5f);
	
	private Menu menu;
	private boolean focused = false;
	private boolean enabled = true;
	private boolean changeOnTouch = false;
	
	public CirclesMenuItem(Menu menu, String text) {
		super(text, Assets.skin, Assets.LABEL_STYLE_MENU_ITEM);
		Validate.notNull(menu);
		this.menu = menu;
		setColor(NORMAL_COLOR.cpy());
		addListener(new MyListener());
	}
	public CirclesMenuItem(CharSequence text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}

	@Override
	public void setItemText(String text) {
		setText(text);
	}
	
	public String getItemText() {
		return getText().toString();
	}
	
	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void setFocused(boolean focus) {
		this.focused = focus;
		setColor(focused ? FOCUSED_COLOR.cpy() : NORMAL_COLOR.cpy());
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setColor(enabled ? NORMAL_COLOR.cpy() : DISABLED_COLOR.cpy());
	}

	@Override
	public void select() {
		clearActions();
		addAction(Actions.repeat(4, Actions.sequence(
				Actions.color(NORMAL_COLOR, 0.08f),
				Actions.color(FOCUSED_COLOR, 0.08f))));
	}
	

    @Override
    public boolean isChangeOnTouch() {
        return changeOnTouch;
    }


    @Override
    public void setChangeOnTouch(boolean changeOnTouch) {
        this.changeOnTouch = changeOnTouch;
    }

	private class MyListener extends InputListener {

		
		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			return super.touchDown(event, x, y, pointer, button);
		}

		@Override
		public boolean mouseMoved(InputEvent event, float x, float y) {
			return super.mouseMoved(event, x, y);
		}

		@Override
		public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			menu.setFocusedItem(CirclesMenuItem.this, true);
		}

		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			menu.setFocusedItem(null, true);
		}

//		@Override
//		public void clicked(InputEvent event, float x, float y) {
//			menu.selectItem(CirclesMenuItem.this);
//		}
	}
}
