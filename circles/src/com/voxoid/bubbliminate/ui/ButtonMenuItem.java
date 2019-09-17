package com.voxoid.bubbliminate.ui;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.voxoid.bubbliminate.Assets;

public class ButtonMenuItem extends TextButton implements IMenuItem {

	public static final Color NORMAL_COLOR = Color.WHITE.cpy(); //Color.GRAY.cpy();
	public static final Color FOCUSED_COLOR = Color.WHITE.cpy();
	public static final Color DISABLED_COLOR = new Color(1f, 1f, 1f, 0.5f);
	
	private Menu menu;
	private boolean focused = false;
	private boolean enabled = true;
	private boolean changeOnTouch = false;
	
	public ButtonMenuItem(Menu menu, String text) {
		super(text, Assets.skin, Assets.TEXT_BUTTON_STYLE);
		Validate.notNull(menu);
		this.menu = menu;
		setColor(NORMAL_COLOR.cpy());
		addListener(new MyListener());
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
	public String getItemText() {
		return super.getText().toString();
	}


	@Override
	public void setItemText(String text) {
		super.setText(text);
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
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			super.touchUp(event, x, y, pointer, button);
			super.touchDown(event, x, y, pointer, button);
			menu.selectItem(ButtonMenuItem.this);
		}


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
			menu.setFocusedItem(ButtonMenuItem.this, true);
		}

		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			menu.setFocusedItem(null, true);
		}
	}

}
