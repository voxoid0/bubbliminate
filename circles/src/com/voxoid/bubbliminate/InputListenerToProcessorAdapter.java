package com.voxoid.bubbliminate;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * Adapts an {@link InputListener} into an {@link IAllInputProcessor}
 * @author Joel
 *
 */
public class InputListenerToProcessorAdapter extends InputListener {

	public IAllInputProcessor getProcessor() {
        return processor;
    }



    public void setProcessor(IAllInputProcessor processor) {
        this.processor = processor;
    }



    public InputListenerToProcessorAdapter(IAllInputProcessor processor) {
        this.processor = processor;
    }
	
	

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
	    return processor.touchDown((int) x, (int) y, pointer, button);
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        processor.touchUp((int) x, (int) y, pointer, button);
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        processor.touchDragged((int) x, (int) y, pointer);
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
        return processor.mouseMoved((int) x, (int) y);
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        // nothing
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        // nothing
    }

    @Override
    public boolean scrolled(InputEvent event, float x, float y, int amount) {
        return processor.scrolled(amount);
    }

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        return processor.keyDown(keycode);
    }

    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        return processor.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(InputEvent event, char character) {
        return processor.keyTyped(character);
    }

    private IAllInputProcessor processor;
	
	
}
