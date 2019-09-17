package com.voxoid.bubbliminate.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.IAllInputProcessor;

/**
 * TODO: mouse support
 * @author joel.becker
 *
 */
public class Menu extends Table implements IMenu {

	public static final int STYLE_BUTTONS = 1;
	
	private float menuSfxVolume = 0.5f;
	
	private static Sound focusSound;
	private static Sound selectSound;
	private static Sound backSound;
	private static Sound changeSound;
	
	private List<IMenuItem> menuItems = Collections.emptyList();
	private List<InputListener> itemListeners = Collections.emptyList();
	private MyInputProcessor inputListener = new MyInputProcessor();
	private IMenuItem focusedItem;
	private List<IMenuListener> listeners = new CopyOnWriteArrayList<IMenuListener>();
	private int itemAlignment;
	private int style;
	private Table heading;
	private IAllInputProcessor nextInput;
	
	public Menu(List<IMenuItem> menuItems, Controller[] controllers, int itemAlignment, int style) {
		this(menuItems, controllers, itemAlignment, 0, new Table(), new AllInputAdapter());
	}
	
	public Menu(List<IMenuItem> menuItems, Controller[] controllers, int itemAlignment, int style, Table heading, IAllInputProcessor nextInput) {
		this.itemAlignment = itemAlignment;
		this.style = style;
		this.heading = heading;
		this.nextInput = nextInput;
		setMenuItems(menuItems);
	}
	
	public void setNextInput(IAllInputProcessor nextInput) {
		this.nextInput = nextInput;
	}
		
	public static void setFocusSound(Sound focusSound) {
		Menu.focusSound = focusSound;
	}
	
	public static void setSelectSound(Sound selectSound) {
		Menu.selectSound = selectSound;
	}
	
	public static void setBackSound(Sound backSound) {
		Menu.backSound = backSound;
	}
	
	@Override
	public void setMenuItems(List<IMenuItem> menuItems) {
		clearMenu();
		this.menuItems = new ArrayList<IMenuItem>(menuItems);
		layoutMenu();
	}
	
	private void clearMenu() {
		for (IMenuItem item : menuItems) {
			removeActor((Actor) item);
		}		
	}
	
	private void layoutMenu() {
//		setSize(400f, 500f);
		NinePatchDrawable patch = Assets.window9Patch;
		
		this.debug();
		this.debugTable();
		
		setBackground(patch);
		Table inside = new Table().pad(64f);
		inside.debug(); inside.debugTable(); inside.debugCell();
		add(inside)
			.pad(-80f)	// WHY do I have to do this??
//			.pad(patch.getTopHeight() - 32f, patch.getLeftWidth() - 32f,
//				patch.getBottomHeight() - 32f, patch.getRightWidth() - 32f)
			.fill().expand(); //.padBottom(32);
		
		if (heading != null) {
    		inside.add(heading).expandX().colspan(2).padBottom(32);
    		inside.row();
		}
		
		if ((style & STYLE_BUTTONS) > 0) {
			for (IMenuItem item : menuItems) {
			    CirclesTextButton.configureButton((TextButton) item, inside)
			        .align(itemAlignment);
			}
			//inside.row();
		} else {
			for (final IMenuItem item : menuItems) {
				inside.add((Actor) item).expandX().align(itemAlignment);
				inside.row();
				
				((Actor) item).addListener(new InputListener() {
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						setFocusedItem(item);
						return true;
					}

					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						selectItem(item);
					}
				});
			}
		}
		
		pack();
		layout();
	}

	@Override
	public void start() {
		CirclesGlobal.input.addToTop(inputListener);
		if (focusedItem == null && !menuItems.isEmpty() && (style & STYLE_BUTTONS) == 0) {
			setFocusedItem(menuItems.get(0));
		}
		
		// Handle clicks
		itemListeners = new ArrayList<InputListener>();
		for (final IMenuItem item : menuItems) {
			InputListener listener = new InputListener() {
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					
					setFocusedItem(item);
					return true;
				}

				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					
					selectItem(item);
				}
			};
			((Actor) item).addListener(listener);
			itemListeners.add(listener);
		}		
	}

	@Override
	public void stop() {
		int index = 0;
		for (final IMenuItem item : menuItems) {
			((Actor) item).removeListener(itemListeners.get(index));
			++index;
		}

		CirclesGlobal.input.remove(inputListener);
	}


	@Override
	public void addMenuListener(IMenuListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeMenuListener(IMenuListener listener) {
		listeners.remove(listener);
	}
	
	public void selectItem(IMenuItem item) {
		if (focusedItem != null && focusedItem.isEnabled()) {
			focusedItem.select();
			if (selectSound != null) {
				selectSound.play(menuSfxVolume);
			}
			notifySelect(item);
		}
	}
	
	public IMenuItem getFocusedMenuItem() {
		return focusedItem;
	}
	
	private void notifySelect(IMenuItem selectedItem) {
		for (IMenuListener listener : listeners) {
			listener.onSelected(this, selectedItem);
		}
	}
	
	private void changeItem(IMenuItem item, int dir) {
		if (changeSound != null) {
			changeSound.play(menuSfxVolume);
		}
		for (IMenuListener listener : listeners) {
			listener.onChanged(this, item, dir);
		}
	}
	
	public void setFocusedItem(IMenuItem item) {
		setFocusedItem(item, false);
	}
	
	void setFocusedItem(IMenuItem item, boolean userSelected) {
		if (item != focusedItem && item != null && item.isEnabled()) {
			if (focusedItem != null) {
				focusedItem.setFocused(false);
			}
			focusedItem = item;
			item.setFocused(true);
			if (focusSound != null && userSelected) {
				focusSound.play(menuSfxVolume);
			}
		}
	}
	
	public void goBack() {
		if (backSound != null) {
			backSound.play(menuSfxVolume);
		}
		notifyBack();
	}
	
	private void notifyBack() {
		for (IMenuListener listener : listeners) {
			listener.onBack(this);
		}
	}
	
	private void moveFocus(int dir) {
		int oldIndex = indexOf(focusedItem);
		if (oldIndex != -1) {
			
			// Find next enabled item in that direction
			int newIndex = oldIndex;
			do {
				newIndex = (newIndex + dir + menuItems.size()) % menuItems.size();
			} while (!menuItems.get(newIndex).isEnabled() && oldIndex != newIndex);
			
			setFocusedItem(menuItems.get(newIndex), true);
		}
	}
	
	public int indexOf(IMenuItem item) {
		if (item != null) {
			for (int i = 0; i < menuItems.size(); i++) {
				if (menuItems.get(i) == item) {
					return i;
				}
			}
		}
		return -1;
	}
	

	/**
	 * Returns the menu item from this menu at the given screen location, or
	 * null if none.
	 * 
	 * @param screenX
	 * @param screenY
	 * @return
	 */
	private IMenuItem getMenuItemHit(int screenX, int screenY) {
		IMenuItem item = null;
		Stage stage = Menu.this.getStage();
		if (stage != null) { // Almost always true (but we had a NPE on this within about 300 downloads)
			Actor hit = stage.hit(screenX, Gdx.graphics.getHeight() - screenY, true);
			item = hit == null ? null :
				(hit instanceof IMenuItem ? (IMenuItem) hit :
				(hit.getParent() instanceof IMenuItem ? (IMenuItem) hit.getParent() : null));
			if (!menuItems.contains(item)) {
				item = null;
			}
		}
		return item;
	}


	private class MyInputProcessor extends AllInputAdapter {

		private IMenuItem itemTouched;
		
		@Override
		public boolean buttonDown(Controller controller, int buttonCode) {
			boolean handled = true;
			if (buttonCode == Ouya.BUTTON_O) {
				selectItem(focusedItem);
			} else if (buttonCode == Ouya.BUTTON_A || buttonCode == Ouya.BUTTON_MENU) {
				goBack();
			} else if (buttonCode == Ouya.BUTTON_DPAD_UP) {
				moveFocus(-1);
			} else if (buttonCode == Ouya.BUTTON_DPAD_DOWN) {
				moveFocus(1);
			} else if (buttonCode == Ouya.BUTTON_DPAD_LEFT) {
				changeItem(focusedItem, -1);
			} else if (buttonCode == Ouya.BUTTON_DPAD_RIGHT) {
				changeItem(focusedItem, 1);
			} else {
				handled = nextInput.buttonDown(controller, buttonCode);
			}
			return handled;
		}

		@Override
		public boolean axisMoved(Controller controller, int axisCode,
				float value) {
			
			// TODO: support stick
			return nextInput.axisMoved(controller, axisCode, value);
		}

		@Override
		public boolean keyDown(int keycode) {
			boolean handled = true;
			if (keycode == Input.Keys.ENTER) {
				selectItem(focusedItem);
			} else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.BACK) {
				goBack();
			} else if (keycode == Input.Keys.UP) {
				moveFocus(-1);
			} else if (keycode == Input.Keys.DOWN) {
				moveFocus(1);
			} else if (keycode == Input.Keys.LEFT) {
				changeItem(focusedItem, -1);
			} else if (keycode == Input.Keys.RIGHT) {
				changeItem(focusedItem, 1);
			} else {
				handled = nextInput.keyDown(keycode);
			}
			return handled;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			itemTouched = getMenuItemHit(screenX, screenY);
			if (itemTouched != null) {
				setFocusedItem(itemTouched);
                if (itemTouched.isChangeOnTouch()) {
                    changeItem(itemTouched, 1);
                } else {
//                    selectItem(item);
                }
				return true;					
			} else {
				return nextInput.touchDown(screenX, screenY, pointer, button);
			}
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            IMenuItem item = getMenuItemHit(screenX, screenY);
            if (item != null && item == itemTouched && !item.isChangeOnTouch()) {
                selectItem(item);
                return true;
            } else {
                return nextInput.touchUp(screenX, screenY, pointer, button);
            }
		}
		
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return nextInput.mouseMoved(screenX, screenY);
		}
		
		
	}
}
