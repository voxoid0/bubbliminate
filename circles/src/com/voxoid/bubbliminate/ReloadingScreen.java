package com.voxoid.bubbliminate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.voxoid.bubbliminate.actors.BubbleFloatAction;
import com.voxoid.bubbliminate.actors.TextureRegionActor;

public class ReloadingScreen implements Screen {

	private Stage stage;
	private TextureRegion reloading;
	private Runnable onComplete;
	
	
	public ReloadingScreen(Runnable onComplete) {
		super();
		this.onComplete = onComplete;
	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(0f, 0f, 0f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
		
		if (Assets.assetMgr.update()) {
			Assets.finishLoad();
			if (onComplete != null) {
				onComplete.run();
				onComplete = null;
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		stage = new Stage();
		reloading = TextureUtils.loadNonPotTexture("Reloading.png", false);
		Actor reloadingActor = new TextureRegionActor(reloading);
		stage.addActor(reloadingActor);
		ActorUtil.centerActor(reloadingActor);
//		reloadingActor.addAction(new BubbleFloatAction(new Vector2(reloadingActor.getX(), reloadingActor.getY()),
//				new Vector2(5f, 5f), new Vector2(50f, 50f), new Vector2(0.123f, 1.234f)));
		
		Assets.unloadAll();
		Assets.loadAll();
	}

	@Override
	public void hide() {
		stage.dispose();
		reloading.getTexture().dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
	}

}
