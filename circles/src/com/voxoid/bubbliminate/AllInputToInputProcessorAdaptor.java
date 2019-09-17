package com.voxoid.bubbliminate;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

public class AllInputToInputProcessorAdaptor extends AllInputAdapter {

	private InputProcessor inputProcessor;
	
	
	/**
	 * 
	 * @param inputProcessor InputProcessor, or null to do nothing upon input.
	 */
	public AllInputToInputProcessorAdaptor(InputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor == null ? new InputAdapter() : inputProcessor;
	}

	@Override
	public boolean keyDown(int keycode) {
		return inputProcessor.keyDown(keycode);
	}

	@Override
	public boolean keyUp(int keycode) {
		return inputProcessor.keyUp(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		return inputProcessor.keyTyped(character);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return inputProcessor.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return inputProcessor.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return inputProcessor.touchDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return inputProcessor.mouseMoved(screenX, screenY);
	}

	@Override
	public boolean scrolled(int amount) {
		return inputProcessor.scrolled(amount);
	}

}
