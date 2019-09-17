package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.core.model.IPlayer;

public class BannerTextActor extends Label {

	private boolean staying = false;
	
	public BannerTextActor() {
		super("", Assets.skin, Assets.LABEL_STYLE_BIG);
	}
	
	public void setBannerText(final String text, final IPlayer player, final boolean stay) {
		final int scrnWidth = Gdx.graphics.getWidth();
		final int textWidth = (int) getStyle().font.getBounds(text).width;
		
		RunnableAction initTextAction = Actions.run(new Runnable() {
			public void run() {
				setPosition(Gdx.graphics.getWidth(), getY());						
				setText(text);
				setColor(player == null ? Color.WHITE.cpy() : player.getColor().cpy());
			}
		});
		
		// Action to move staying text out first
		Action stopStayingAction = staying ?
				Actions.moveTo(-textWidth, getY(), 1f, Interpolation.bounceIn) :
				Actions.delay(0f);
		staying = stay;
		
		if (stay) {
			final int destX = (Gdx.graphics.getWidth() - textWidth) / 2;
			final int xDist = scrnWidth - destX;
			addAction(
				//Actions.after(
					Actions.sequence(
						stopStayingAction,
						initTextAction,
						Actions.moveTo(destX, getY(), 1f, Interpolation.bounceOut),
						Actions.delay(2f)	// Should show staying text for at least 2 seconds before next text comes in
					)
//				)
			);
		} else {
			final int destX = -textWidth - 1;
			final int xDist = scrnWidth - destX;
			final float moveDuration = xDist * 4 / scrnWidth; 
			addAction(Actions.after(Actions.sequence(
					stopStayingAction,
					initTextAction,
					Actions.moveTo(destX, getY(), moveDuration)
			)));
		}
	}
}
