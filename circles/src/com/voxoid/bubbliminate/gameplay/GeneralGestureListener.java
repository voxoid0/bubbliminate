package com.voxoid.bubbliminate.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.voxoid.bubbliminate.SimpleTransform2;

public class GeneralGestureListener extends GestureDetector.GestureAdapter {

	private GameViewTouch view;
	private SimpleTransform2 initialEnvTransf;
	private Vector2 initialOriginScreen;
	
	public GeneralGestureListener(GameViewTouch view) {
		super();
		this.view = view;
	}
	
	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		// reset pinch stuff
		initialEnvTransf = view.getEnvirTransform();
		initialOriginScreen = view.worldToScreen(Vector2.Zero);
		
		return super.touchDown(x, y, pointer, button);
	}

	
	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		SimpleTransform2 prev = view.getEnvirTransform();

		Vector2 delta = pointer1.cpy().add(pointer2).div(2f).sub(
				initialPointer1.cpy().add(initialPointer2).div(2f));
		
		Vector2 initialMidPoint = initialPointer1.cpy().add(initialPointer2).div(2f);
		Vector2 initialMidToOrigin = initialOriginScreen.cpy().sub(initialMidPoint);
		float scale = prev.getScaleX() / initialEnvTransf.getScaleX();
		Vector2 newOriginScreen = initialMidToOrigin.cpy().scl(scale).add(initialMidPoint);

		view.setEnvirTransform(new SimpleTransform2(prev.getScaleX(),
				newOriginScreen.x + delta.x, newOriginScreen.y - delta.y));
		Gdx.app.log("pinch", "delta = " + delta.toString() + "\tinitial1=" + initialPointer1 + "\tinitial2=" + initialPointer2);
		
		return true;
		
		
//		lastTimePannedOrZoomedMs = System.currentTimeMillis();
//		
//		Vector2 ipDelta = initialPointer2.cpy().sub(initialPointer1);
//		Vector2 pDelta = pointer2.cpy().sub(pointer1);
//		float initialPointersDist = ipDelta.len();
//		float pointersDist = pDelta.len();
//		
//		if (initialPointersDist > Float.MIN_VALUE && pointersDist > Float.MIN_VALUE) {
//			float scaleChange = pointersDist / initialPointersDist;
//			float scale = initialEnvTransf.getScaleX() * scaleChange;
//			
//			Vector2 initialMidPoint = initialPointer1.cpy().add(initialPointer2).div(2f);
//			Vector2 midPoint = pointer1.cpy().add(pointer2.cpy()).div(2f);
//			Vector2 deltaMidPoint = midPoint.cpy().sub(initialMidPoint);
//			Vector2 midToInitOrigin = initialOriginScreen.cpy().sub(midPoint);
//			
//			// Move vector is based on the change in midpoint between fingers (y-axis flipped), plus the amount that
//			// the origin (0,0) must move away in order to keep the original mid-point to be the same environment location
//			// at the new mid-point
////			Vector2 deltaTranslate = deltaMidPoint.cpy().scl(1f, -1f)
////					.add(midToInitOrigin.cpy().scl(scaleChange - 1f));
////			
////			float tx = initialEnvTransf.getTx() + deltaTranslate.x;
////			float ty = initialEnvTransf.getTy() + deltaTranslate.y;
//			
//			Vector2 initMidToInitOrig = initialOriginScreen.cpy().sub(initialMidPoint);
//			Vector2 newOrigin = initialMidPoint.cpy().add(initMidToInitOrig.cpy().scl(scaleChange))
//					.add(deltaMidPoint);
//			Vector2 deltaOrigin = newOrigin.cpy().sub(initialOriginScreen);
//			//newOrigin = initialOriginScreen.cpy().add(deltaOrigin.cpy().scl(1f, -1f));
//			newOrigin.y = initialOriginScreen.y - (newOrigin.y - initialOriginScreen.y);
////			newOrigin.y = 2f * initialOriginScreen.y - newOrigin.y; // I.y - (N.y - Iy)
//			
//			view.setEnvirTransform(new SimpleTransform2(scale, newOrigin.x, newOrigin.y));
//		}
//		return true;
//	}
	
	
//	@Override
//	public boolean pan (float x, float y, float deltaX, float deltaY) {
//		SimpleTransform2 prev = view.getEnvirTransform();
//		view.setEnvirTransform(new SimpleTransform2(prev.getScaleX(), prev.getTx() + deltaX, prev.getTy() - deltaY));
//		return true;
	}

	@Override
	public boolean zoom (float initialDistance, float distance) {
		SimpleTransform2 prev = view.getEnvirTransform();
		view.setEnvirTransform(new SimpleTransform2(initialEnvTransf.getScaleX() * distance / initialDistance,
				prev.getTx(), prev.getTy()));
		return true;
	}

}
