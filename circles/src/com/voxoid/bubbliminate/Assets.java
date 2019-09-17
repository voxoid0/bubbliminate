package com.voxoid.bubbliminate;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.voxoid.bubbliminate.ui.Menu;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;

/**
 * Assets used throughout the game
 * @author Joel
 *
 */
public class Assets {
	public static final String LABEL_STYLE_NORMAL = "default";
	public static final String LABEL_STYLE_BIG = "big";
	public static final String LABEL_STYLE_MENU_ITEM = "menuItem";
	public static final String TEXT_BUTTON_STYLE = "default";
	public static final String WINDOW_PATCH = "windowPatch";
	public static final String BUTTON_NORMAL_PATCH = "buttonNormalPatch";
	public static final String BUTTON_PRESSED_PATCH = "buttonPressedPatch";

	public static int BUTTON_X_OFFSET = 16;
	
	public static Skin skin;
	public static Texture whiteTexture;
	
	public static Texture bubbleBase;
	public static Texture bubbleGlare;
	public static TextureRegion gameBackground;
	public static TextureRegion iconShare;
	public static TextureRegion iconRanges;
	public static TextureRegion iconLabels;
	public static TextureRegion iconHelp;
	public static TextureRegion iconExit;
	
	public static Sound moveSound;
	public static Sound growSound;
	public static Sound splitSound;
	public static Sound selectSound;
	public static Sound nonoSound;
	public static Sound popSound;
	
	public static BitmapFont bigGameFont;
	public static BitmapFont mediumGameFont;
	public static BitmapFont mediumNormalFont;
	public static BitmapFont smallNormalFont;
	public static BitmapFont tinyFont;
	
	private static BitmapFont font16;
	private static BitmapFont font21;
	private static BitmapFont font32;
	private static BitmapFont font42;
	private static BitmapFont font48;
	private static BitmapFont font64;
	private static BitmapFont normalFont12;
	private static BitmapFont normalFont16;
	private static BitmapFont normalFont21;
	private static BitmapFont normalFont32;
	private static BitmapFont normalFont48;
	public static NinePatchDrawable window9Patch;
	public static NinePatchDrawable buttonNormal9Patch;
	public static NinePatchDrawable buttonPressed9Patch;
	
	public static Music titleMusic;
	public static Music winMusic;
	public static Music loseMusic;
	public static Music wavesMusic;
	
	public static ControllerAssets controller;
	
	public static AssetManager assetMgr;
	
	private static final Logger LOGGER = Logger.getLogger(Assets.class);
	private static final TextureParameter TEXTURE_PARAMS = new TextureParameter();
	

	{
		TEXTURE_PARAMS.genMipMaps = true;
		TEXTURE_PARAMS.magFilter = TextureFilter.MipMap;
		TEXTURE_PARAMS.minFilter = TextureFilter.MipMap;
	}
	
	/**
	 * Queues up an AssetManager with resources to be loaded and returns it. The client
	 * should then call AssetManager.update() each frame (optional), followed by Assets.finishLoad().
	 * The client should <i>never<i/> use it to get resources.
	 * 
	 * @return
	 */
	public static AssetManager loadAll() {
		assetMgr = new AssetManager();
		Texture.setAssetManager(assetMgr);
		
		assetMgr.load("sounds/move.wav", Sound.class);
		assetMgr.load("sounds/grow.wav", Sound.class);
		assetMgr.load("sounds/split.wav", Sound.class);
		assetMgr.load("sounds/select.wav", Sound.class);
		assetMgr.load("sounds/nono.wav", Sound.class);
		assetMgr.load("sounds/pop.wav", Sound.class);
		
		assetMgr.load("music/Title.ogg", Music.class);
		assetMgr.load("music/Win.ogg", Music.class);
		assetMgr.load("music/Lose.ogg", Music.class);
		assetMgr.load("music/waves.ogg", Music.class);
		
		reloadTextures();
		
		return assetMgr;
	}
	
	public static void reloadTextures() {
		assetMgr.load("bubble-base.png", Texture.class, TEXTURE_PARAMS);
		assetMgr.load("bubble-glare.png", Texture.class, TEXTURE_PARAMS);
		assetMgr.load("GameBackground.png", Pixmap.class);
		
		if (!CirclesGlobal.isTouchDevice) {
			controller = new ControllerAssets(assetMgr, "images");
			controller.loadAll();
		}
				
		assetMgr.load("fonts/font16.fnt", BitmapFont.class);
        assetMgr.load("fonts/font21.fnt", BitmapFont.class);
		assetMgr.load("fonts/font32.fnt", BitmapFont.class);
		assetMgr.load("fonts/font42.fnt", BitmapFont.class);
		assetMgr.load("fonts/font48.fnt", BitmapFont.class);
		assetMgr.load("fonts/font64.fnt", BitmapFont.class);
        assetMgr.load("fonts/droidSans12.fnt", BitmapFont.class);
        assetMgr.load("fonts/droidSans16.fnt", BitmapFont.class);
		assetMgr.load("fonts/droidSans21.fnt", BitmapFont.class);
		assetMgr.load("fonts/droidSans32.fnt", BitmapFont.class);
		assetMgr.load("fonts/droidSans48.fnt", BitmapFont.class);
		
		assetMgr.load("ui/window.png", Texture.class, TEXTURE_PARAMS);
//		assetMgr.load("ui/button.9.png", Texture.class);
//		assetMgr.load("ui/button-pressed.9.png", Texture.class);
//		assetMgr.load("ui/button32-up.png", Texture.class);
//		assetMgr.load("ui/button32-down.png", Texture.class);
        assetMgr.load("ui/bubbleButton32-up.png", Texture.class);
		
        assetMgr.load("ui/gameScreenIcons.png", Texture.class);
//		createWhiteTexture();
	}
	
	public static TextureRegion controlImage(ControllerAssets.Control control) {
		return controller.getImage(control);
	}

	private static void createWhiteTexture() {
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		whiteTexture = new Texture(pixmap);
	}
	
	public static void finishLoad() {
		LOGGER.info("Assets.finishLoad()");

		assignReferences();
	}
	
	public static void assignReferences() {
		assetMgr.finishLoading();

		moveSound = assetMgr.get("sounds/move.wav");
		growSound = assetMgr.get("sounds/grow.wav");
		splitSound = assetMgr.get("sounds/split.wav");
		selectSound = assetMgr.get("sounds/select.wav");
		nonoSound = assetMgr.get("sounds/nono.wav");
		popSound = assetMgr.get("sounds/pop.wav");
//		Menu.setFocusSound(selectSound);
//		Menu.setSelectSound(splitSound);
//		Menu.setBackSound(moveSound);

		titleMusic = assetMgr.get("music/Title.ogg");
		winMusic = assetMgr.get("music/Win.ogg");
		loseMusic = assetMgr.get("music/Lose.ogg");
		wavesMusic = assetMgr.get("music/waves.ogg");
		
		titleMusic.setVolume(0.75f);
//		winMusic.setVolume(0.75f);
		loseMusic.setVolume(0.75f);
		wavesMusic.setVolume(0.75f);
		
		reassignTextureReferences();
	}
	
	public static void reassignTextureReferences() {
		assetMgr.finishLoading();
				
		createWhiteTexture();
		
		bubbleBase = assetMgr.get("bubble-base.png");
		bubbleGlare = assetMgr.get("bubble-glare.png");
		
//		gameBackground = TextureUtils.loadNonPotTexture("GameBackground.png", true, assetMgr);
		
		if (!CirclesGlobal.isTouchDevice) {
			controller.finishLoad();
		}
		
		font16 = assetMgr.get("fonts/font16.fnt");
        font21 = assetMgr.get("fonts/font21.fnt");
		font32 = assetMgr.get("fonts/font32.fnt");
		font42 = assetMgr.get("fonts/font42.fnt");
		font48 = assetMgr.get("fonts/font48.fnt");
		font64 = assetMgr.get("fonts/font64.fnt");
		normalFont12 = assetMgr.get("fonts/droidSans12.fnt");
		normalFont16 = assetMgr.get("fonts/droidSans16.fnt");
		normalFont21 = assetMgr.get("fonts/droidSans21.fnt");
		normalFont32 = assetMgr.get("fonts/droidSans32.fnt");
		normalFont48 = assetMgr.get("fonts/droidSans48.fnt");
		
//		window9Patch = new NinePatchDrawable(loadNinePatch("ui/window.9.png", 68, 125, 68, 125));
//		window9Patch = new NinePatchDrawable(loadNinePatch("ui/window.png", 80, 176, 80, 176));
		window9Patch = new NinePatchDrawable(loadNinePatch("ui/window.png", 40, 88, 40, 88));
//		buttonNormal9Patch = new NinePatchDrawable(loadNinePatch("ui/button.9.png", 185, 215, 185, 215));
//		buttonPressed9Patch = new NinePatchDrawable(loadNinePatch("ui/button-pressed.9.png", 185, 215, 185, 215));
//		buttonNormal9Patch = new NinePatchDrawable(loadNinePatch("ui/button32-up.png", 32, 46, 31, 33));
//		buttonPressed9Patch = new NinePatchDrawable(loadNinePatch("ui/button32-down.png", 32, 46, 31, 33));
//		buttonNormal9Patch = new NinePatchDrawable(loadNinePatch("ui/button32-up.png", 30, 50, 30, 34));
//		buttonPressed9Patch = new NinePatchDrawable(loadNinePatch("ui/button32-down.png", 30, 50, 30, 34));
      buttonNormal9Patch = new NinePatchDrawable(loadNinePatch("ui/bubbleButton32-up.png", 30, 34, 30, 34));
      buttonPressed9Patch = new NinePatchDrawable(loadNinePatch("ui/bubbleButton32-up.png", 30, 34, 30, 34));

      Texture icons = assetMgr.get("ui/gameScreenIcons.png");
      iconShare = new TextureRegion(icons, 0, 0, 51, 64);
      iconRanges = new TextureRegion(icons, 55, 0, 55, 64);
      iconLabels = new TextureRegion(icons, 111, 0, 36, 64);
      iconHelp = new TextureRegion(icons, 166, 0, 34, 64);
      iconExit = new TextureRegion(icons, 222, 0, 47, 64);
      
		/* Android resolutions:
		 *  320x240
		 *  400x240
		 *  432x240
		 *  320x480
		 *  640x400
		 *  800x480
		 *  854x480
		 *  1024x640
		 */
		if (ActorUtil.getScreenShortDim() >= 1080) {
			bigGameFont = font64;
			mediumGameFont = font48;
			mediumNormalFont = normalFont48;
			smallNormalFont = normalFont32;
		} else if (ActorUtil.getScreenShortDim() >= 720){
			bigGameFont = font42;
			mediumGameFont = font32;
			mediumNormalFont = normalFont32;
			smallNormalFont = normalFont21;
		} else if (ActorUtil.getScreenShortDim() >= 400) {
			bigGameFont = font32;
			mediumGameFont = font21;
			mediumNormalFont = normalFont21;
			smallNormalFont = normalFont16;
		} else {
			bigGameFont = font21;
			mediumGameFont = font16;
			mediumNormalFont = normalFont16;
			smallNormalFont = normalFont12;
		}
		tinyFont = new BitmapFont();
		
		createSkin();
		LOGGER.info("Finished loading assets.");
	}
	
	public static void unloadAll() {
		if (assetMgr != null) {
			assetMgr.dispose();
		}
		if (controller != null) {
			controller.dispose();
		}
//		if (gameBackground != null) {
//			gameBackground.getTexture().dispose();
//		}
	}
	
	public static void playTitleMusic() {
		if (!Assets.titleMusic.isPlaying()) {
			Assets.titleMusic.dispose();
			Assets.titleMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Title.ogg"));
			
			Assets.titleMusic.setLooping(true);
			Assets.titleMusic.play();
		}
	}
	
	private static void createSkin() {
		skin = new Skin();

//		bigGameFont = assetMgr.get("fonts/bigGameFont.)
		skin.add("default", mediumGameFont);
		
		skin.add(LABEL_STYLE_NORMAL, new LabelStyle(smallNormalFont, new Color(0.9f, 0.9f, 0.9f, 1f)));		
		skin.add(LABEL_STYLE_BIG, new LabelStyle(bigGameFont, Color.WHITE.cpy()));		
		skin.add(LABEL_STYLE_MENU_ITEM, new LabelStyle(mediumGameFont, Color.WHITE.cpy()));
		skin.add(WINDOW_PATCH, window9Patch);
		skin.add(BUTTON_NORMAL_PATCH, buttonNormal9Patch);
		skin.add(BUTTON_PRESSED_PATCH, buttonPressed9Patch);
		
		TextButtonStyle btnStyle = new TextButtonStyle(buttonNormal9Patch, buttonNormal9Patch, buttonNormal9Patch, mediumGameFont);
		skin.add(TEXT_BUTTON_STYLE, btnStyle);
		
		skin.add("default", new WindowStyle(mediumGameFont, Color.WHITE, window9Patch));
	}
	
	private static NinePatch loadNinePatch(String assetName, int leftX, int rightX, int topY, int bottomY) {
		Texture t = assetMgr.get(assetName);
	    final int width = t.getWidth() - 2;
	    final int height = t.getHeight() - 2;
	    return new NinePatch(new TextureRegion(t, 1, 1, width, height),
	    		leftX, t.getWidth() - rightX,
	    		topY, t.getHeight() - bottomY);
	}	
}
