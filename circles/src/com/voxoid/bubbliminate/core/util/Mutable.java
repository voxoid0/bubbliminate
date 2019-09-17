package com.voxoid.bubbliminate.core.util;

public class Mutable<T> {
	private T value;
	
	public Mutable() {}
	
	public Mutable(T value) {
		this.value = value;
	}
	
	public T get() {
		return value;
	}
	
	public void set(T value) {
		this.value = value;
	}
}
