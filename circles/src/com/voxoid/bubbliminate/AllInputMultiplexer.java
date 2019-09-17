package com.voxoid.bubbliminate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class AllInputMultiplexer implements IAllInputProcessor {

	private List<IAllInputProcessor> procs;
	private boolean enabled = true;

	public AllInputMultiplexer(IAllInputProcessor... procs) {
		this.procs = new ArrayList<IAllInputProcessor>(Arrays.asList(procs));
	}

	public void clear() {
		procs.clear();
	}

	public void addToBeginning(IAllInputProcessor proc) {
		procs.add(0, proc);
	}

	public void add(IAllInputProcessor proc) {
		procs.add(proc);
	}

	public void remove(IAllInputProcessor proc) {
		procs.remove(proc);
	}

	public void enable(boolean enable) {
		enabled = enable;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.keyDown(keycode))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.keyUp(keycode))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.keyTyped(character))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (enabled) {

			for (IAllInputProcessor proc : procs) {
				if (proc.touchDown(screenX, screenY, pointer, button))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.touchUp(screenX, screenY, pointer, button))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.touchDragged(screenX, screenY, pointer))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.mouseMoved(screenX, screenY))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.scrolled(amount))
					return true;
			}
		}
		return false;
	}

	@Override
	public void connected(Controller controller) {
		for (IAllInputProcessor proc : procs) {
			proc.connected(controller);
		}
	}

	@Override
	public void disconnected(Controller controller) {

		for (IAllInputProcessor proc : procs) {
			proc.disconnected(controller);
		}
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.buttonDown(controller, buttonCode))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.buttonUp(controller, buttonCode))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.axisMoved(controller, axisCode, value))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode,
			PovDirection value) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.povMoved(controller, povCode, value))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode,
			boolean value) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.xSliderMoved(controller, sliderCode, value))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode,
			boolean value) {
		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.ySliderMoved(controller, sliderCode, value))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller,
			int accelerometerCode, Vector3 value) {

		if (enabled) {
			for (IAllInputProcessor proc : procs) {
				if (proc.accelerometerMoved(controller, accelerometerCode,
						value))
					return true;
			}
		}
		return false;
	}

}
