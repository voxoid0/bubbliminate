package com.voxoid.bubbliminate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureUtils {

	public static TextureRegion loadNonPotTexture(String filename, boolean antiBleeding) {
		Pixmap pixmap = new Pixmap(Gdx.files.internal(filename));
		TextureRegion instance = loadNonPotTexture(pixmap, antiBleeding);
		pixmap.dispose();
		return instance;
	}
	
	/**
	 * Loads a non-power-of-2 texture from the given pixmap resource found in
	 * the given {@link AssetManager}, and unloads the source pixmap. It is the
	 * responsibility of the client to dispose the {@link Texture} of the
	 * returned {@link TextureRegion}.
	 * 
	 * @param filename
	 * @param antiBleeding
	 * @param assetMgr
	 * @return
	 */
	public static TextureRegion loadNonPotTexture(String filename, boolean antiBleeding, AssetManager assetMgr) {
		Pixmap pixmap = assetMgr.get(filename, Pixmap.class);
		TextureRegion instance = loadNonPotTexture(pixmap, antiBleeding);
		assetMgr.unload(filename);
		return instance;
	}
	
	
	public static TextureRegion loadNonPotTexture(Pixmap pixmap, boolean antiBleeding) {
		int potWidth = MathUtil.getNextPot(pixmap.getWidth());
		int potHeight = MathUtil.getNextPot(pixmap.getHeight());
		
		Texture texture = new Texture(potWidth, potHeight, pixmap.getFormat());
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
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
}
