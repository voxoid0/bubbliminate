package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.voxoid.bubbliminate.MathUtil;

/**
 * Actor for displaying non-power-of-two images.
 * 
 * TODO: Make more memory-efficient version, which creates and draws multiple textures so that less memory is used.
 * @author Joel
 *
 */
public class NonPotActor extends TextureRegionActor implements Disposable {

	private Texture texture;
	
//	public static NonPotActor create(FileHandle file, boolean antiBleeding) {
//		Pixmap pixmap = new Pixmap(file);
//		NonPotActor instance = new NonPotActor(pixmap, antiBleeding);
//		pixmap.dispose();
//		return instance;
//	}

	private static TextureRegion createTextureRegion(Pixmap pixmap, boolean antiBleeding) {
		int potWidth = MathUtil.getNextPot(pixmap.getWidth());
		int potHeight = MathUtil.getNextPot(pixmap.getHeight());
		Texture texture = new Texture(potWidth, potHeight, pixmap.getFormat());
		
//		Pixmap test = new Pixmap(potWidth, potHeight, pixmap.getFormat());
//		test.setColor(Color.BLUE);
//		test.fill();
//		test.drawPixmap(pixmap, 0, 0);		
//		texture.draw(test, 0, 0);
//		return new TextureRegion(texture, potWidth, potHeight);
		texture.draw(pixmap, 0, 0);
		if (antiBleeding) {
			return new TextureRegion(texture, 1, 1, pixmap.getWidth() - 2, pixmap.getHeight() - 2);
		} else {
			return new TextureRegion(texture, 0, 0, pixmap.getWidth(), pixmap.getHeight());
		}
	}
	
	public NonPotActor(Pixmap pixmap, boolean antiBleeding) {
		super(createTextureRegion(pixmap, antiBleeding));
		texture = getTextureRegion().getTexture();
		setSize(getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
	}

	@Override
	public void dispose() {
		texture.dispose();
	}

	
//	@Override
//	public void draw(SpriteBatch batch, float parentAlpha) {
//		for (Sprite sprite : sprites) {
//			sprite.draw(batch);
//		}
//	}
}
