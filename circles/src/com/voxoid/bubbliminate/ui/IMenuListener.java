package com.voxoid.bubbliminate.ui;

public interface IMenuListener {

	void onSelected(IMenu source, IMenuItem selection);
	void onBack(IMenu source);
	void onChanged(IMenu source, IMenuItem item, int changeDir);
}
