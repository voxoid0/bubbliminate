package com.voxoid.bubbliminate.actors;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Action;

public class PlaySoundAction extends Action {

	private Sound sound;
	
	public PlaySoundAction(Sound sound) {
		this.sound = sound;
	}

	@Override
	public boolean act(float delta) {
		sound.play();
		return true;
	}
}
