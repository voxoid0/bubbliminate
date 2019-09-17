package com.voxoid.bubbliminate.core.ai;

import java.util.ArrayList;
import java.util.List;

public class ItemGrid<T> {

	public final int width;
	public final int height;
	private int size;
	private List<T> items[];
	
	public ItemGrid(int width, int height) {
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("width and height must be positive");
		}
		this.width = width;
		this.height = height;
		size = width * height;
		items = new List[width * height];
		for (int i = 0; i < size; i++) {
			items[i] = new ArrayList<T>();
		}
	}
	
	public void add(int x, int y, T item) {
		items[y*width + x].add(item);
	}
	
	public List<T> getItemsIn(int x, int y) {
		return items[y*width + x];
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
