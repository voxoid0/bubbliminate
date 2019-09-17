package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.voxoid.bubbliminate.Assets;
import com.voxoid.bubbliminate.CirclesGlobal;
import com.voxoid.bubbliminate.SimpleTransform2;
import com.voxoid.bubbliminate.core.model.ICircle;


public class CircleActor extends TextureRegionActor {

	private ICircle circle;
	
	public CircleActor(Color color, ICircle circle, boolean glare) {
		super(glare ?
				new Texture[] { Assets.bubbleBase, Assets.bubbleGlare } :
				new Texture[] { Assets.bubbleBase });

		setColor(color);
		update(circle);
	}

	
	public void update(ICircle circle) {
		this.circle = circle;
		SimpleTransform2 tr = CirclesGlobal.gameEnvirTransform == null ? SimpleTransform2.NONE : CirclesGlobal.gameEnvirTransform;
		
		setX(tr.transformX(circle.getLocation().x));
		setY(tr.transformY(circle.getLocation().y));
		setWidth(circle.getRadius() * 2f * tr.getScaleX());
		setHeight(circle.getRadius() * 2f * tr.getScaleY());
		setOrigin(circle.getRadius() * tr.getScaleX(), circle.getRadius() * tr.getScaleY());
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		update(circle);
		super.draw(batch, parentAlpha);
	}
	
	public ICircle getCircle() {
		return circle;
	}
}
