package com.voxoid.bubbliminate;

import java.net.URISyntaxException;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.voxoid.bubbliminate.gameplay.CirclesGame;

public class CirclesDesktop {

	private static final String PURCHASE_URL = "http://voxoidgames.com";
	private static final String PROPERTIES_URL = "http://voxoidgames.com/bubbliminate/data/data.properties";
	
	
	public static void main(String[] args) throws URISyntaxException {
        CirclesGlobal.systemUtil = new AbstractSystemUtil();
		CirclesGlobal.purchasing = new OpenBrowserPurchaser(PURCHASE_URL);
		CirclesGlobal.webMessages = new WebMessages(PROPERTIES_URL);
		CirclesGlobal.gameData = new DesktopGameData();
		CirclesGlobal.appRestorer = new AppRestorer(CirclesGlobal.gameData);
		CirclesGlobal.flurry = new DummyFlurryAgent();
		CirclesGlobal.platform = new DesktopPlatform();
        CirclesGlobal.isTouchDevice = true;
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		
		config.width = 480;
		config.height = 270;
//		config.width = 960;
//		config.height = 540;
		config.width = 1280;
		config.height = 720;
//		config.width = 1920;
//		config.height = 1080;
		config.useGL20 = true;
		
		// Phones
		// Normal size screen, high density
		config.width = 480;
		config.height = 800;
		// Small screen, high density
//        config.width = 640;
//        config.height = 480;
		// Normal size screen, medium density: NOT SUPPORTED yet
//		config.width = 480;
//		config.height = 320;

		// 7" Tablets
//		config.width = 1024;
//		config.height = 600;
//		config.width = 1280;
//		config.height = 800;
		config.width = 1280;
		config.height = 720;
//		config.width = 2048;
//		config.height = 1536;
//		config.width = 2560;
//		config.height = 1600;
		
		// 10" Tablets
//		config.width = 1280;
//		config.height = 800;
//		config.width = 2048;
//		config.height = 1536;
//		config.width = 2560;
//		config.height = 1600;
		
		// YouTube
		config.width = 854;
		config.height = 480;

		// FaceBook Ad
		config.width = 1280;
		config.height = 628;
		
		new LwjglApplication(new CirclesGame(), config);
	}
}
