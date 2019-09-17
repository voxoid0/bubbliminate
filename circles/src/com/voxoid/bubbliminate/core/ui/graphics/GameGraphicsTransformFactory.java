package com.voxoid.bubbliminate.core.ui.graphics;

//import android.graphics.Matrix;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.voxoid.bubbliminate.core.model.IGame;

/**
 * Creates {@link AffineTransform}s for transforming between view coordinates and world coordinates.
 * 
 * @author Joel
 * @date 9/13/2010
 */
public class GameGraphicsTransformFactory {
	private static final GameGraphicsTransformFactory INSTANCE = new GameGraphicsTransformFactory();
	
	public static GameGraphicsTransformFactory instance() {
		return INSTANCE;
	}
	
	public Matrix3 makeWorldToViewTransform(Actor view, IGame game) {
		Matrix3 transf = new Matrix3();
		float rectSize = Math.min(view.getWidth(), view.getHeight());
		float scale = rectSize / (game.getEnvironmentRadius() * 2f);
		float tx = view.getWidth() / 2f + view.getX();
		float ty = view.getHeight() / 2f + view.getY();
		
		transf.setToScaling(scale, scale);
		transf.translate(tx, ty);
		return transf;
	}
	
	public Matrix3 makeViewToWorldTransform(Actor view, IGame game) {
		Matrix3 transf = makeWorldToViewTransform(view, game);
		return transf.inv();
	}
}
