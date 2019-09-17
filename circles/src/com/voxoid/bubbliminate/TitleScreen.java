package com.voxoid.bubbliminate;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.voxoid.bubbliminate.ControllerAssets.Control;
import com.voxoid.bubbliminate.actors.FadeActor;
import com.voxoid.bubbliminate.actors.TextActor;
import com.voxoid.bubbliminate.actors.TextureRegionActor;

public class TitleScreen extends AllInputAdapter implements Screen {

	private static final String COPYRIGHT = "Game and Software Copyright 2013 Joel Becker. All Rights Reserved.";
	
	private static final Vector2[] START_BUBBLE_CENTER = new Vector2[] 
			{ new Vector2(1100, 1080 - 502), new Vector2(329, 1024 - 497) };
	private static final float[] START_BUBBLE_SIZE = new float[] {356f, 206f};

	private static final Vector2[] EXIT_BUBBLE_CENTER = new Vector2[] 
			{ new Vector2(800, 1080 - 602), new Vector2(157, 1024 - 553) };
	private static final float[] EXIT_BUBBLE_SIZE = new float[] {271f, 159f};

	private static final Vector2[] CREDITS_BUBBLE_CENTER = new Vector2[] 
			{ new Vector2(966, 1080 - 698), new Vector2(251,1024 - 610) };
	private static final float[] CREDITS_BUBBLE_SIZE = new float[] {109f, 64f};

	private static final Vector2[] BUY_BUBBLE_CENTER = new Vector2[] 
			{ new Vector2(1070, 1080 - 737), new Vector2(312, 1024 - 633) };
	private static final float[] BUY_BUBBLE_SIZE = new float[] {109f, 64f};
	
	private int orientation;

	
	private Stage stage;
	private TextureRegionActor title;
	private Color fadeInColor;
	

	public TitleScreen() {
		this(Color.BLACK);
	}
	
	public TitleScreen(Color fadeInColor) {
		Validate.notNull(fadeInColor);
		this.fadeInColor = fadeInColor;
		orientation = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? 0 : 1;
	}
	
	private void showCopyright() {
		TextActor copyright = new TextActor(Assets.tinyFont, Color.LIGHT_GRAY.cpy(), COPYRIGHT);
		float centeringX = ActorUtil.getXForCentering(copyright);
		copyright.setPosition(centeringX, -16);
		stage.addActor(copyright);
		copyright.addAction(Actions.sequence(
				Actions.moveTo(centeringX, Gdx.graphics.getHeight() * 0.05f, 1f, Interpolation.sineOut)));
	}
	
	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(0f, 0f, 0f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glLineWidth(2f);
		stage.draw();
//		Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
		// TODO test by resizing window
	}

	@Override
	public void show() {
		stage = new Stage(); //100f, 100f, false);  // dint make no difference
		
		title = new TextureRegionActor(loadTitleImage());
		
		SimpleTransform2 titleImgToScreen = ActorUtil.fillScreenKeepingRatio(title);
//		title.setScale(titleImgToScreen.scale);
//		title.setPosition(titleImgToScreen.tx, titleImgToScreen.ty);
		
		// Scaling this way for the sake of the coordinates of the text labels of the bubble buttons
//		title.setScale(Gdx.graphics.getWidth() / title.getWidth(), Gdx.graphics.getHeight() / title.getHeight()); // do this in ActorUtil.fillScreen()?
		
		
//		title.addAction(
//				Actions.forever(Actions.moveBy(0f, 20f, 4f, SinusoidInterpolation.INSTANCE)));
		stage.addActor(title);

		Assets.playTitleMusic();
		
		CirclesGlobal.input.addToTop(this);

		addControlsTouch(titleImgToScreen);
		
		new FadeActor(stage, fadeInColor).fadeIn(2f, new Runnable() {
			public void run() {
				showCopyright();
				if (CirclesGlobal.isOuya) {
				CirclesGlobal.demoHelper.finishPurchaseInit(stage, new Runnable() {
					public void run() {
						addControlsOuya();
					}
				});
				}
			}
		});
	}

	private TextureRegion loadTitleImage() {
		return TextureUtils.loadNonPotTexture(
				Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? "title-tight.png" : "title-portrait.png",
				true);
	}
	
	private void addControlsTouch(SimpleTransform2 titleImgToScreen) {
		String fontName = ActorUtil.isScreenPortrait() ? Assets.LABEL_STYLE_BIG : Assets.LABEL_STYLE_MENU_ITEM;
		
		addBubbleButton("Start", titleImgToScreen,
				START_BUBBLE_CENTER[orientation],
				START_BUBBLE_SIZE[orientation],
				ActorUtil.isScreenPortrait() ? Assets.LABEL_STYLE_BIG : Assets.LABEL_STYLE_MENU_ITEM,
				1.33f,
				new ClickListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y,
							int pointer, int button) {
						endToNext();
						return true;
					}
		});
		
		addBubbleButton("Quit",
				titleImgToScreen,
				EXIT_BUBBLE_CENTER[orientation],
				EXIT_BUBBLE_SIZE[orientation],
				ActorUtil.isScreenPortrait() ? Assets.LABEL_STYLE_BIG : Assets.LABEL_STYLE_MENU_ITEM,
				1f,
				new ClickListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y,
							int pointer, int button) {
						endToExit();
						return true;
					}
		});

		addBubbleButton("Credits", 
				titleImgToScreen, 
				CREDITS_BUBBLE_CENTER[orientation], 
				CREDITS_BUBBLE_SIZE[orientation], 
				Assets.LABEL_STYLE_NORMAL,
				ActorUtil.isScreenPortrait() ? 0.75f : 0.5f,
				new ClickListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y,
							int pointer, int button) {
						doCredits();
						return true;
					}
		});

		addBubbleButton("Rate", 
				titleImgToScreen, 
				BUY_BUBBLE_CENTER[orientation], 
				BUY_BUBBLE_SIZE[orientation], 
				Assets.LABEL_STYLE_NORMAL, 
				ActorUtil.isScreenPortrait() ? 0.75f : 0.5f,
				new ClickListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y,
							int pointer, int button) {
						doRate();
						return true;
					}
		});
	}

	private void addBubbleButton(String label, SimpleTransform2 titleImgToScreen,
			Vector2 bubbleCenter, float bubbleSize, String font, float fontScale, ClickListener clickListener) {
		
		final Label startLabel = new Label(label, Assets.skin, font); //, Assets.LABEL_STYLE_BIG);
//				fontScale < 1f ? Assets.LABEL_STYLE_NORMAL : Assets.LABEL_STYLE_MENU_ITEM);
		startLabel.setAlignment(Align.center);
		startLabel.setAlignment(Align.center, Align.center);
		startLabel.setFontScale(fontScale);
		
		Table table = new Table();
		table.add(startLabel).center().expand();
		table.setSize(bubbleSize * 0.7f * titleImgToScreen.scale, bubbleSize * 0.7f * titleImgToScreen.scale);
		ActorUtil.centerActorOrigin(table);
		
		ActorUtil.centerActor(table, titleImgToScreen.transformX(bubbleCenter.x),
				titleImgToScreen.transformY(bubbleCenter.y));
		//table.setY(Gdx.graphics.getHeight() - table.getY());
		table.debug();
		table.debugTable();
		table.setTouchable(Touchable.enabled);
		table.addListener(clickListener);
		stage.addActor(table);
	}
	
	private void addControlOuya(Control control, String desc, int align, float x, float delay) {
		Table content = new Table();
		UiUtil.addControlOuya(content, control, desc, new Color(0.2f, 0.2f, 0.2f, 1f));
		content.pack();
//		content.debug();

		Table wrapper = new Table();
		wrapper.add(content).fillX().expandX().align(align);
		wrapper.setWidth(Gdx.graphics.getWidth() * 0.1f);
		wrapper.setPosition(x, -content.getHeight() * 2f);
		wrapper.addAction(Actions.sequence(
				Actions.delay(delay),
				Actions.moveTo(wrapper.getX(), Gdx.graphics.getHeight() * 0.15f, 0.5f, Interpolation.sineOut)
				));
//		wrapper.debug();
		stage.addActor(wrapper);
	}
	
	private void addControlsOuya() {
		addControlOuya(Control.U, "Credits", Align.left, Gdx.graphics.getWidth() * 0.35f, 0.5f);
		
		if (!CirclesGlobal.purchasing.hasBeenPurchased()) {
			addControlOuya(Control.Y, "Buy", Align.center, Gdx.graphics.getWidth() * 0.45f, 0.7f);
		}
		
		addControlOuya(Control.O, "Start", Align.right, Gdx.graphics.getWidth() * 0.55f, 0.9f);
	}
	
	private void endToNext() {
		CirclesGlobal.demoHelper.showGeneralMessages(stage, new Runnable() {
			public void run() {
				final PlayerConfigScreen screen = new PlayerConfigScreen();
				endToScreen(screen);
			}
		});
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
		Assets.reassignTextureReferences();
		title.getTextureRegion().getTexture().dispose();
		title.setTextureRegion(loadTitleImage());
	}

	@Override
	public void dispose() {
		title.getTextureRegion().getTexture().dispose();
		stage.dispose();
	}

	@Override
	public boolean keyDown(int keyCode) {
		if (!CirclesGlobal.demoHelper.pause()) {
			if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK) {
				endToExit();
			} else if (keyCode == Input.Keys.ENTER){
				endToNext();
			} else if (keyCode == Input.Keys.C){
				doCredits();
			} else if (keyCode == Input.Keys.B){
				doBuy();
			}
		}
		return false;
	}

	private void endToExit() {
		FadeActor fade = new FadeActor(stage, Color.BLACK);
		fade.fadeOut(0.5f, new Runnable() {
			public void run() {
				CirclesGlobal.appRestorer.clearRestoreState();
				Gdx.app.exit();
			}
		});
	}
	
	private void doCredits() {
		endToScreen(new CreditsScreen());
	}
	
	private void doBuy() {
		CirclesGlobal.demoHelper.makePurchase(stage, null);
	}
	
	private void doRate() {
		CirclesGlobal.flurry.logEvent("rateTapped", false);
		CirclesGlobal.platform.rateApp();
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		if (!CirclesGlobal.demoHelper.pause()) {
			if (buttonCode == Ouya.BUTTON_MENU) {
				endToExit();
			} else if (buttonCode == Ouya.BUTTON_O){
				endToNext();
			} else if (buttonCode == Ouya.BUTTON_U){
				doCredits();
			} else if (buttonCode == Ouya.BUTTON_Y){
				doBuy();
			}
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (!CirclesGlobal.demoHelper.pause()) {
			return stage.touchDown(screenX, screenY, pointer, button);
		} else {
			return false;
		}
	}

	private void endToScreen(final Screen screen) {
		CirclesGlobal.input.enable(false);
		Controllers.removeListener(TitleScreen.this);
		
		FadeActor fade = new FadeActor(stage, Color.BLACK);
		fade.fadeOut(0.5f, new Runnable() {
			public void run() {
				((Game) Gdx.app.getApplicationListener()).setScreen(screen);			
			}
		});
	}
}
