package com.voxoid.bubbliminate.core.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.voxoid.bubbliminate.core.rules.IMove;


public class GameHistory implements IGameHistory {

	private ArrayList<Snapshot> history = new ArrayList<Snapshot>();
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	private Snapshot undoSnapshot;
	
	@Override
	public void add(ICirclesGameState resultingState, IMove previous) {
		if (undoSnapshot != null) {
			rewindTo(undoSnapshot);
			undoSnapshot = null;
		}
		history.add(new Snapshot(resultingState, previous));
		propSupport.firePropertyChange("history", null, history);
	}

	@Override
	public List<Snapshot> getHistory() {
		return Collections.unmodifiableList(history);
	}

	@Override
	public void rewindTo(Snapshot snapshot) {
		int index = history.indexOf(snapshot) + 1;
		if (index == -1) {
			throw new IllegalArgumentException("Snapshot does not exist in history.");
		}
		if (history.size() > index) {
			while (history.size() > index) {
				history.remove(index);
			}
			propSupport.firePropertyChange("history", null, history);
		}
	}

	@Override
	public ICirclesGameState undo() {
		boolean first = true;
		if (undoSnapshot != null) {
			int last = history.indexOf(undoSnapshot);
			if (last != -1) {
				first = false;
				if (last > 0) {
					undoSnapshot = history.get(last - 1);
				}
			}
		}
		if (first && history.size() > 1) {
			undoSnapshot = history.get(history.size() - 2);
		}
		return undoSnapshot == null ? null : undoSnapshot.getState();
	}
	
	@Override
	public ICirclesGameState redo() {
		if (undoSnapshot != null) {
			int last = history.indexOf(undoSnapshot);
			if (last != -1) {
				if (last < history.size()) {
					undoSnapshot = history.get(last + 1);
				}
			}
		}
		return undoSnapshot == null ? null : undoSnapshot.getState();
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

}
