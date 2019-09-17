package com.voxoid.bubbliminate;


import com.badlogic.gdx.graphics.Color;
import com.voxoid.bubbliminate.core.model.IPlayer;


public class VisualSettings {
	private VisualSettings() {
		// nothing
	}
	
	public static Color getBackgroundColor() {
		return Color.BLACK;
	}
	
	public static Color getEnvironmentColor() {
		return Color.WHITE;
	}
	
	public static Color getLineColor() {
		return Color.BLACK;
	}
	
	public static float getRingWidth() {
		return 1f;	// TODO: environment resolution-dependent
	}
	
	public static float getSelectedRingWidth() {
		return 2f;	// TODO: environment resolution-dependent
	}
	
	public static Color getPlayerRingColor(IPlayer player) {
		return player.getColor().cpy().mul(1f, 1f, 1f, 0.5f);
	}
	
	public static Color getPlayerSelectedRingColor(IPlayer player) {
		return player.getColor().cpy();
	}
	
	public static float getTentativeGameStateAlpha() {
		return 1f;
	}
//	
//	public static Stroke getBoundsStroke() {
//		return new BasicStroke(0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f,
//				new float[] {0.5f, 0.5f}, 0f);
//	}
//	
//	public static Stroke getDottedLineStroke() {
//		return new BasicStroke(0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f,
//				new float[] {0.5f, 0.5f}, 0f);
//	}
}
