package com.voxoid.bubbliminate.gameplay;

import com.badlogic.gdx.math.Vector2;
import com.voxoid.bubbliminate.ITimeUpdatable;
import com.voxoid.bubbliminate.core.model.ICircle;
import com.voxoid.bubbliminate.core.model.IGame;
import com.voxoid.bubbliminate.core.model.IPlayer;
import com.voxoid.bubbliminate.core.rules.MoveType;

public interface IGameController extends ITimeUpdatable {

	public abstract void exit();

	public abstract GameplayState getState();

	public abstract IGame getGame();

	public abstract ICircle getCurrCircle();

	/**
	 * In selection state this sets the currently-highlighted circle
	 */
	public abstract void setCurrCircle(ICircle circle);

	public abstract IPlayer getCurrPlayer();

	public abstract MoveType getCurrMoveType();

	/**
	 * Sets the current move type. If in SELECTING state, the current circle will be selected
	 * first.
	 * 
	 * @param moveType
	 */
	public abstract void setMoveType(MoveType moveType);

	public abstract void selectCircle(ICircle circle);

	public abstract void setControlPoint(Vector2 point);

	public abstract Vector2 getControlPoint();

	public abstract void moveControlPoint(Vector2 delta);

	public abstract void confirmMove();
	
	public abstract void completeMove();

	public abstract void doneAnimatingMove();

	public abstract void undoLastMove();

	public abstract void redoLastMove();

	/**
	 * Cancel adjustment state and go back to selecting state to select a (potentially) different circle.
	 */
	public abstract void selectDifferentCircle();

	public abstract void cycleSelectionLeft();

	public abstract void cycleSelectionRight();

	public abstract void cycleSelectionUp();

	public abstract void cycleSelectionDown();

	public abstract void showInstructions();

	public abstract void toggleCircleLabels();
	
	public abstract void toggleRangeRings();

	public abstract void handleCannotSplit();

}