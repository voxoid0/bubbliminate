package com.voxoid.bubbliminate.ui;

/**
 * Interface to a UI element which responds to being selected, focused, etc. The responses
 * are presumably visual, not functional; functional response is handled by non-UI code.
 * 
 * @author joel.becker
 *
 */
public interface IMenuItem {
	String getItemText();
	void setItemText(String text);
	boolean isFocused();
	void setFocused(boolean focus);
	boolean isEnabled();
	void setEnabled(boolean enabled);
	boolean isChangeOnTouch();
	void setChangeOnTouch(boolean changeOnTouch);
	void select();
}
