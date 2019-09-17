package com.voxoid.bubbliminate.ui;

import java.util.List;

public interface IMenu {

//	List<IUiSelectable> getSelectables();
	void setMenuItems(List<IMenuItem> menuItems);
	void start();
	void stop();
//	void setSelectionDirection(SelectionDirection dir);
	void addMenuListener(IMenuListener listener);
	void removeMenuListener(IMenuListener listener);
	int indexOf(IMenuItem item);
}
