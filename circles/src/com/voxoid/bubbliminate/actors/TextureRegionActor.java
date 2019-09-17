package com.voxoid.bubbliminate.actors;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * @author joel.becker
 *
 */
public class TextureRegionActor extends Actor {

	private TextureRegion[] textureRegions;
	
	public TextureRegionActor(TextureRegion[] textureRegions) {
		Validate.noNullElements(textureRegions);
		this.textureRegions = textureRegions;
		setSize(textureRegions[0].getRegionWidth(), textureRegions[0].getRegionHeight());
	}
	
	public TextureRegionActor(TextureRegion textureRegion) {
		this(new TextureRegion[] { textureRegion });
	}

	/**
     * It is the client's responsibility to dispose the {@link Texture}
     * 
	 * @param texture
	 */
	public TextureRegionActor(Texture texture) {
		this(new TextureRegion(texture));
	}
	
	public TextureRegionActor(Texture[] textures) {
		Validate.notEmpty(textures);
		textureRegions = new TextureRegion[textures.length];
		for (int i = 0; i < textureRegions.length; i++) {
			textureRegions[i] = new TextureRegion(textures[i]);
		}
		setSize(textureRegions[0].getRegionWidth(), textureRegions[0].getRegionHeight());
	}
	
	public TextureRegion getTextureRegion() {
		return textureRegions[0];
	}

	public void setTextureRegion(TextureRegion textureRegion) {
		this.textureRegions = new TextureRegion[] { textureRegion };
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        for (TextureRegion textureRegion : textureRegions) {
	        batch.draw(
	        		textureRegion, getX() - getOriginX(), getY() - getOriginY(), getOriginX(), getOriginY(),
	                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            batch.setColor(Color.WHITE);	// TODO: Don't make all TextureRegionActors only set the color of their first texture region (this is just for CircleActors sake; didn't want to refactor for now)
        }
	}
}
