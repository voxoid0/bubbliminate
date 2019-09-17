package com.voxoid.bubbliminate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class InputStack implements IAllInputProcessor {

	private static final IAllInputProcessor DUMMY_PROC = new AllInputAdapter();

	private List<IAllInputProcessor> procs;
	private boolean enabled = true;

	public InputStack(IAllInputProcessor... procs) {
		this.procs = new ArrayList<IAllInputProcessor>(Arrays.asList(procs));
		if (this.procs.isEmpty()) {
			clear();
		}
	}

	public void clear() {
		procs = new ArrayList<IAllInputProcessor>();
		procs.add(DUMMY_PROC);
	}

	public void addToBeginning(IAllInputProcessor proc) {
		procs.add(0, proc);
	}

	public void addToTop(IAllInputProcessor proc) {
		procs.add(proc);
	}

	public void remove(IAllInputProcessor proc) {
		procs.remove(proc);
	}

	public void enable(boolean enable) {
		enabled = enable;
	}
	
	public boolean hasFocus(IAllInputProcessor proc) {
		return procs.get(procs.size() - 1) == proc;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.keyDown(keycode))
				return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.keyUp(keycode))
				return true;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.keyTyped(character))
				return true;
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (enabled) {

			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.touchDown(screenX, screenY, pointer, button))
				return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.touchUp(screenX, screenY, pointer, button))
				return true;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.touchDragged(screenX, screenY, pointer))
				return true;
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.mouseMoved(screenX, screenY))
				return true;
		}
		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.scrolled(amount))
				return true;
		}
		return false;
	}

	@Override
	public void connected(Controller controller) {
		IAllInputProcessor proc = procs.get(procs.size() - 1);
		proc.connected(controller);
	}

	@Override
	public void disconnected(Controller controller) {

		IAllInputProcessor proc = procs.get(procs.size() - 1);
		proc.disconnected(controller);
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.buttonDown(controller, buttonCode))
				return true;
		}
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.buttonUp(controller, buttonCode))
				return true;
		}
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.axisMoved(controller, axisCode, value))
				return true;
		}
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode,
			PovDirection value) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.povMoved(controller, povCode, value))
				return true;
		}
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode,
			boolean value) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.xSliderMoved(controller, sliderCode, value))
				return true;
		}
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode,
			boolean value) {
		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.ySliderMoved(controller, sliderCode, value))
				return true;
		}
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller,
			int accelerometerCode, Vector3 value) {

		if (enabled) {
			IAllInputProcessor proc = procs.get(procs.size() - 1);
			if (proc.accelerometerMoved(controller, accelerometerCode, value))
				return true;
		}
		return false;
	}

	/**
	 * Replaces the old proc with the new proc. If oldProc is null, it just adds the newProc to the top.
	 * @param oldProc
	 * @param newProc
	 */
	public void replace(IAllInputProcessor oldProc, IAllInputProcessor newProc) {
		if (oldProc != null) {
			Collections.replaceAll(procs, oldProc, newProc);
		} else {
			procs.add(newProc);
		}
	}

}
