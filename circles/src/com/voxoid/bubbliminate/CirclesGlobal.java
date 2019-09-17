package com.voxoid.bubbliminate;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voxoid.bubbliminate.gameplay.CirclesGame;



public class CirclesGlobal {

	public static CirclesGame game;
	
	/** Current transformation for the game environment */
	public static SimpleTransform2 gameEnvirTransform;
	
	public static final int MAX_PLAYERS = 8;
	
	public static InputStack input;
	
	public static ShapeRenderer shapeRenderer;
	
	public static IPurchase purchasing;
	public static WebMessages webMessages;
	public static DemoVersionHelper demoHelper;
	public static IPlatform platform;
	public static IFlurryAgent flurry;
	
	public static IGameData gameData;
	public static AppRestorer appRestorer;
	public static InstructionsHelper instructionsHelper;
	public static AbstractSystemUtil systemUtil;
	
	public static boolean isTouchDevice;	// or mouse
	public static boolean isOuya;
}
