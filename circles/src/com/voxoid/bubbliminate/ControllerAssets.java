package com.voxoid.bubbliminate;

import org.apache.log4j.Logger;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ControllerAssets {

	public enum Control {
		O,
		U,
		Y,
		A,
		L1,
		R1,
		L2,
		R2,
		L3,
		R3,
		DPAD,
		DPAD_UP,
		DPAD_DOWN,
		DPAD_LEFT,
		DPAD_RIGHT,
		LS,
		LS_UP,
		LS_DOWN,
		LS_LEFT,
		LS_RIGHT,
		RS,
		RS_UP,
		RS_DOWN,
		RS_LEFT,
		RS_RIGHT,
		SYSTEM,
		TOUCHPAD,
	}

	private TextureRegion textureRegion[] = new TextureRegion[Control.values().length];
	
	private static final Logger LOGGER = Logger.getLogger(ControllerAssets.class);
	
	private AssetManager assetMgr;
	private String baseDir;
	

	public ControllerAssets(AssetManager assetMgr, String baseDir) {
		super();
		this.assetMgr = assetMgr;
		this.baseDir = baseDir;
	}

	public void loadAll() {
		for (Control control : Control.values()) {
			try {
				assetMgr.load(filenameFor(control), Pixmap.class);
			} catch (RuntimeException e) {
				LOGGER.info("Skipping loading of OUYA controller icon for " + control.name());
			}
		}
	}
	
	public void finishLoad() {
		for (Control control : Control.values()) {
			try {
				TextureRegion tr =  TextureUtils.loadNonPotTexture(
						filenameFor(control), true, assetMgr);
				textureRegion[control.ordinal()] = tr;
			} catch (RuntimeException e) {
				// nothing
			}
		}
	}
	
	public TextureRegion getImage(Control control) {
		return textureRegion[control.ordinal()];
	}
	
	public void dispose() {
		for (TextureRegion tr : textureRegion) {
			if (tr != null && tr.getTexture() != null) {
				tr.getTexture().dispose();
			}
		}
	}
	
	private String filenameFor(Control control) {
		return String.format("%s/OUYA_%s.png", baseDir, control.name());
	}
}
