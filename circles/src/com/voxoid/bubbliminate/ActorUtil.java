package com.voxoid.bubbliminate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Utility methods for LibGDX Actors.
 */
public class ActorUtil {

	private ActorUtil() {}
	
	public static void centerActorOrigin(Actor a) {
		a.setOrigin(a.getWidth() / 2f, a.getHeight() / 2f);
	}
	
	public static void centerActor(Actor a) {
		a.setPosition(getXForCentering(a), getYForCentering(a));
	}
	
	public static float getXForCentering(Actor a) {
		return Gdx.graphics.getWidth()/2f + a.getOriginX() - a.getWidth()/2f;
	}
	
	public static float getYForCentering(Actor a) {
		return Gdx.graphics.getHeight()/2f + a.getOriginY() - a.getHeight()/2f;
	}
	
	/**
	 * Centers the give actor on the given point
	 * @param a
	 * @param target
	 */
	public static void centerActor(Actor a, float targetX, float targetY) {
		a.setPosition(targetX - a.getWidth()*a.getScaleX()/2f, targetY - a.getHeight()*a.getScaleY()/2f);		
	}
	
	/**
	 * 
	 * @param a
	 * @return Transform which changes a point from the cordinates of the actor as an image (with 0,0 at top-left)
	 * to screen coordinates (with 0,0 at bottom-left)
	 */
	public static SimpleTransform2 fillScreenKeepingRatio(Actor a) {
		SimpleTransform2 t = new SimpleTransform2(1f, 0f, 0f);
		if (a.getWidth() < 1f || a.getHeight() < 1f) return t;
		float actorRatio = a.getWidth() / a.getHeight();
		float screenRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
		if (actorRatio > screenRatio) {   // if actor is wider
			a.setScale(Gdx.graphics.getHeight() / a.getHeight());
			a.setPosition(-(a.getWidth() * a.getScaleX() - Gdx.graphics.getWidth()) / 2, 0);
		} else {
			a.setScale(Gdx.graphics.getWidth() / a.getWidth());
			a.setPosition(0, -(a.getHeight() * a.getScaleY() - Gdx.graphics.getHeight()) / 2);
		}
		a.setOrigin(0, 0);
		SimpleTransform2 tran = new SimpleTransform2(a.getScaleX(), a.getX(), a.getY());
		centerActorOrigin(a);
		centerActor(a);
		
		return tran;
	}
	
	public static void fillScreen(Actor a) {
		a.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public static String describePose(Actor a) {
		return String.format("rgba=(%.2f, %.2f, %.2f, %.2f); pos=(%.2f, %.2f); size=(%.2f, %.2f)",
				a.getColor().r, a.getColor().g, a.getColor().b, a.getColor().a,
				a.getX(), a.getY(), a.getWidth(), a.getHeight());
	}
	
	public static void setAlpha(Actor actor, float a) {
		Color c = actor.getColor();
		actor.setColor(c.r, c.g, c.b, a);
	}
	
	public static float getScreenLongDim(Actor actor) {
		return Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? actor.getWidth() : actor.getHeight();
	}
	
	public static float getScreenLongDim() {
		return Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? Gdx.graphics.getWidth() : Gdx.graphics.getHeight();
	}

	public static float getScreenShortDim(Actor actor) {
		return Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? actor.getHeight() : actor.getWidth();
	}
	
	public static float getScreenShortDim() {
		return Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? Gdx.graphics.getHeight() : Gdx.graphics.getWidth();
	}
	
	public static boolean isScreenPortrait() {
		return Gdx.graphics.getHeight() > Gdx.graphics.getWidth();
	}
}
