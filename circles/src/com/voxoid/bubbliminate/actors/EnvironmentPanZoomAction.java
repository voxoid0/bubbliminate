package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.voxoid.bubbliminate.SimpleTransform2;
import com.voxoid.bubbliminate.gameplay.IGameView;

public class EnvironmentPanZoomAction extends Action {

	private final IGameView view;
	private final SimpleTransform2 startTransf;
	private final SimpleTransform2 endTransf;
	private final float duration = 1f;

	private float t = 0f;
	

	public EnvironmentPanZoomAction(IGameView view, SimpleTransform2 startTransf,
			SimpleTransform2 endTransf) {
		super();
		this.view = view;
		this.startTransf = startTransf;
		this.endTransf = endTransf;
	
	}	
	
	@Override
	public boolean act(float delta) {
		t += delta;
		if (t > duration) return true;
		
		// cosine interpolation
		float ct = ((float) Math.cos((t / duration + 1) * Math.PI) + 1f) / 2f;
		
		float scale = startTransf.getScaleX() + (endTransf.getScaleX() - startTransf.getScaleX()) * ct;
		float tx = startTransf.getTx() + (endTransf.getTx() - startTransf.getTx()) * ct;
		float ty = startTransf.getTy() + (endTransf.getTy() - startTransf.getTy()) * ct;
		
		view.setEnvirTransform(new SimpleTransform2(scale, tx, ty));
		return false;	}
}
