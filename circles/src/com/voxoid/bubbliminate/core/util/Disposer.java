package com.voxoid.bubbliminate.core.util;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

public class Disposer {

	private Disposer() {}
	
	private static Set<Disposable> toBeDisposed = new HashSet<Disposable>();
	
	
	public static void cleanup() {
		for (Disposable disposable : toBeDisposed) {
			disposable.dispose();
		}
		toBeDisposed.clear();
	}
	
	public static void dispose(Object object) {
		if (object instanceof Disposable) {
			toBeDisposed.add((Disposable) object);
		}
	}
	
	/**
	 * Removes the given actor from the given group, and disposes the actor. Also,
	 * if the actor itself is a Group, its children are cleared and disposed recursively.
	 * @param actor
	 * @param group
	 */
	public static void removeAndDispose(Actor actor, Group group) {
		if (actor instanceof Group) {
			clearAndDisposeActors((Group) actor);
		}
		group.removeActor(actor);
		dispose(actor);
	}
	
	public static void removeAndDispose(Actor actor, Stage stage) {
		removeAndDispose(actor, stage.getRoot());
	}
	
	public static void clearAndDisposeActors(Group group) {
		for (Actor actor : group.getChildren().items) {
			removeAndDispose(actor, group);
		}
		group.clearChildren();
	}
}
