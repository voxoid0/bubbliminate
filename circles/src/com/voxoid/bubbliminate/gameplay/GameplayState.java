package com.voxoid.bubbliminate.gameplay;

public enum GameplayState {
	/** Waiting for a CPU or network player's move */
	WAITING_FOR_PLAYER,
	
	/** Letting player select a circle (with a game controller) */
	SELECTING,
	
	/** Waiting for a local player to make a move (touch or mouse devices) */
	WAITING_FOR_MOVE,
	
	/** Letting player adjust the selected circle */
	ADJUSTING,
	
	
	ANIMATING,
	WON,
	DRAW
}