package com.voxoid.bubbliminate;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.voxoid.bubbliminate.actors.FadeActor;
import com.voxoid.bubbliminate.actors.TextureRegionActor;
import com.voxoid.bubbliminate.ui.CirclesMenuItem;

public class CreditsScreen extends AllInputAdapter implements Screen {

	private Stage stage;
	private TextureRegionActor background;
	
	
	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(0f, 0f, 0f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
		// TODO test by resizing window
	}

	@Override
	public void show() {
		stage = new Stage(); //100f, 100f, false);  // dint make no difference
		
		background = new TextureRegionActor(TextureUtils.loadNonPotTexture("DistantBackground.jpg", true));
		ActorUtil.fillScreenKeepingRatio(background);
//		background.addAction(
//				Actions.forever(Actions.moveBy(0f, 20f, 4f, SinusoidInterpolation.INSTANCE)));
		stage.addActor(background);
		
		CirclesGlobal.input.addToTop(this);

		new FadeActor(stage, Color.BLACK).fadeIn(2f, null);
		
		Table credits = buildCredits();
//		ActorUtil.centerActor(credits);
		credits.setPosition((Gdx.graphics.getWidth() - credits.getWidth()) / 2f, -credits.getHeight());
		credits.addAction(Actions.sequence(
				Actions.moveBy(0f, credits.getHeight() + Gdx.graphics.getHeight(), 20f),
				Actions.run(new Runnable() {
					public void run() {
						endScreen();
					}
				})
				));
		stage.addActor(credits);
	}
	
	private Table buildCredits() {
		Table table = new Table();
		
		addCreditHeader("Game Design", table, false);
		addCreditHeader("Programming", table, false);
		addCreditHeader("Art", table, false);
		addCreditHeader("Music", table, false);
		addCreditItem("Joel Becker", table);
		
		addCredit("Game Background Photo", "llee_wu", table);
		addCredit("Distant Island Photo", "(unknown)", table);
		addCredit("Ocean Sound FX Recorded By", "Mike Koenig", table);
		
		table.pack();
//		table.setFillParent(true);
//		table.debug();
		return table;
	}
	
	private void addCredit(String heading, String item, Table table) {
		addCreditHeader(heading, table, true);
		addCreditItem(item, table);
	}
	private void addCreditHeader(String header, Table creditsTable, boolean pad) {
		final Label label = new Label(header, Assets.skin, Assets.LABEL_STYLE_MENU_ITEM);
		creditsTable.add(label).padTop(pad ? Assets.mediumGameFont.getCapHeight() : 0f);
		label.setColor(CirclesMenuItem.FOCUSED_COLOR);
		creditsTable.row();
	}
	
	private void addCreditItem(String item, Table creditsTable) {
		final Label label = new Label(item, Assets.skin, Assets.LABEL_STYLE_MENU_ITEM);
		label.setColor(CirclesMenuItem.NORMAL_COLOR);
		creditsTable.add(label);
		creditsTable.row();
	}
	
	private void endScreen() {
		CirclesGlobal.input.enable(false);
		Controllers.removeListener(CreditsScreen.this);
		
		FadeActor fade = new FadeActor(stage, Color.BLACK);
		fade.fadeOut(0.5f, new Runnable() {
			public void run() {
				((Game) Gdx.app.getApplicationListener()).setScreen(new TitleScreen(Color.BLACK));			
			}
		});
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		background.getTextureRegion().getTexture().dispose();
		stage.dispose();
	}

	@Override
	public boolean keyDown(int keyCode) {
		endScreen();
		return false;
	}
	
	

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		endScreen();
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		endScreen();
		return true;
	}
}
