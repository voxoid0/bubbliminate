package com.voxoid.bubbliminate.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.voxoid.bubbliminate.ActorUtil;
import com.voxoid.bubbliminate.AllInputAdapter;
import com.voxoid.bubbliminate.AllInputToInputProcessorAdaptor;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.core.util.Function1;

public class MenuUtils {

	private static final float CENTER_DIALOG_MOVE_TIME = 0.25f;
	private static final float TOP_DIALOG_MOVE_TIME = 0.1f;
	public static int FLAG_TOP = 1;
	public static int FLAG_NONMODAL = 2;
	
	private static final Logger LOGGER = Logger.getLogger(MenuUtils.class);
	
	
	private MenuUtils() {}
	
	public static Menu openWaitDialog(Stage stage, final String message, final Runnable action, float delayShow) {
		Table table = createMessageWidget(message);
		return openWaitDialog(stage, table, action, delayShow);
	}
	
	public static Menu openWaitDialog(Stage stage, final WidgetGroup text, final Runnable action, float delayShow) {
		Table content = new Table();
		content.add(text);
		final Menu menu = new Menu(Collections.<IMenuItem>emptyList(), 
				Controllers.getControllers().<Controller>toArray(Controller.class),
				Align.center, 0, content, new AllInputAdapter());
		menu.pack();
		stage.addActor(menu);
		ActorUtil.centerActor(menu);
		float destX = menu.getX();
		float destY = menu.getY();
		menu.setY(-menu.getHeight());	// below bottom of screen
		
		menu.start();
		CirclesGlobal.input.enable(false);
		menu.addAction(Actions.sequence(
				Actions.delay(delayShow),
				Actions.moveTo(destX, destY, 0.5f, Interpolation.sineOut),
				Actions.run(new Runnable() {
					public void run() {
						CirclesGlobal.input.enable(true);
					}
				})));
		return menu;
	}
	
	public static void closeWaitDialog(final Menu menu, final Runnable afterGone) {
		CirclesGlobal.input.enable(false);
		menu.stop();
		menu.clearActions();
		menu.addAction(Actions.sequence(
				Actions.moveTo(menu.getX(), -menu.getHeight(),
						0.5f, Interpolation.sineIn),
				Actions.run(new Runnable() {
					public void run() {
						CirclesGlobal.input.enable(true);
						
						if (afterGone != null) {
							afterGone.run();
						}
					}
				})
			));
	}
	
	public static void messageDialog(Stage stage, final String message, final String okButton, final Runnable action) {
		dialog(stage, message, new String[] {okButton}, 0, 0, new Function1<String>() {
			@Override
			public void run(String arg1) {
				if (action != null) {
					action.run();
				}
			}
		});
	}
	
	/**
	 * 
	 * @param stage the current Stage
	 * @param message Message to display
	 * @param options Options to present the user for selection (e.g. "OK" and "Cancel")
	 * @param defaultOption Index of option to have intially selected
	 * @param flags flags for how to present the dialog; see MenuUtil.FLAG* constants
	 * @param func Function to run when the dialog is completed
	 */
	public static void dialog(Stage stage, final String message, final String[] options, int defaultOption, int flags, final Function1<String> func) {
		LOGGER.info("message=" + message);
		Table table = message == null ? null : createMessageWidget(message);
		dialog(stage, table, options, defaultOption, func, flags);
	}

	private static Table createMessageWidget(final String message) {
		Table table = new Table();
		Label label = new Label(message, Assets.skin);
		label.setWrap(true);
		table.add(label)
			.width(ActorUtil.isScreenPortrait() ? Gdx.graphics.getWidth() * 0.8f : Gdx.graphics.getWidth() * 0.33f)
			.pad(0f);
		return table;
	}
	
	/**
	 * 
	 * @param stage
	 * @param text
	 * @param options
	 * @param defaultOption
	 * @param func Callback function when an option is selected; the option text is passed to the function
	 * @param flags TODO
	 */
	public static void dialog(Stage stage, final WidgetGroup text, final String[] options, int defaultOption, final Function1<String> func, int flags) {
		if (stage == null) {
			func.run(options[defaultOption]);
			return;
		}
		final boolean top = (flags & FLAG_TOP) > 0;
		final boolean modal = !((flags & FLAG_NONMODAL) > 0);
		Table content = null;
		if (text != null) {
		    content = new Table();
		    content.add(text);
		}
		final Menu menu = new Menu(Collections.<IMenuItem>emptyList(), 
				Controllers.getControllers().<Controller>toArray(Controller.class),
				Align.center, Menu.STYLE_BUTTONS, content,
				modal ? new AllInputAdapter() : new AllInputToInputProcessorAdaptor(stage));
		
		List<IMenuItem> menuItems = new ArrayList<IMenuItem>();
		for (String option : options) {
			if (CirclesGlobal.isTouchDevice) {
				menuItems.add(new ButtonMenuItem(menu, option));				
			} else {
				menuItems.add(new CirclesMenuItem(menu, option));
			}
		}
		menu.setMenuItems(menuItems);
		
		menu.pack();
		stage.addActor(menu);
		ActorUtil.centerActor(menu);
		if (top) {
			menu.setY(Gdx.graphics.getHeight() - menu.getHeight());
			menu.setX(Gdx.graphics.getWidth() - menu.getWidth());
		}
		
		float destX = menu.getX();
		float destY = menu.getY();
		final float offScreenY = top ? Gdx.graphics.getHeight() : -menu.getHeight();
		final float moveTime = top ? TOP_DIALOG_MOVE_TIME : CENTER_DIALOG_MOVE_TIME;

		menu.setY(offScreenY); // Move off screen
		menu.setFocusedItem(menuItems.get(defaultOption));
		
		if ((flags & FLAG_NONMODAL) > 0) {
			CirclesGlobal.input.enable(false);
		}
		
		menu.addMenuListener(new IMenuListener() {
			
			@Override
			public void onSelected(IMenu source, final IMenuItem selection) {
				menu.stop();
				menu.addAction(Actions.sequence(
						Actions.moveTo(menu.getX(), offScreenY,
								moveTime, Interpolation.sineIn),
						Actions.run(new Runnable() {
							public void run() {
								func.run(selection.getItemText());
							}
						})
					));
			}
			
			@Override
			public void onBack(IMenu source) {
				menu.stop();
				menu.addAction(Actions.sequence(
						Actions.moveTo(menu.getX(), -menu.getHeight(),
								0.5f, Interpolation.sineIn),
						Actions.run(new Runnable() {
							public void run() {
								func.run(null);
							}
						})
					));
			}

			@Override
			public void onChanged(IMenu source, IMenuItem item, int changeDir) {
				// nothing
			}
		});

		menu.start();
		menu.addAction(Actions.sequence(
			Actions.moveTo(destX, destY, moveTime, Interpolation.sineOut),
			Actions.run(new Runnable() {
				public void run() {
					CirclesGlobal.input.enable(true);
				}
			})));
	}
	
}
