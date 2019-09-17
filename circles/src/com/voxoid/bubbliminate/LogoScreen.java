package com.voxoid.bubbliminate;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.voxoid.bubbliminate.actors.TextureRegionActor;

/**
 * Voxoid logo splash screen.
 * 
 * TODO: 1080p Voxoid logo
 * 
 * @author Joel
 * 
 */
public class LogoScreen implements Screen {

    private static final Color WHITE_TRANSPARENT = new Color(1f, 1f, 1f, 0f);

    private Stage stage;
    private TextureRegionActor logo;
    private Music music;
    private AssetManager assetManager;

    @Override
    public void render(float delta) {
        stage.act(delta);

        Gdx.gl.glClearColor(1f, 1f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();

        assetManager.update();
    }

    @Override
    public void resize(int width, int height) {
        stage.setViewport(width, height, true);
        // TODO test by resizing window
    }

    @Override
    public void show() {
        stage = new Stage(); // 100f, 100f, false); // dint make no difference

        AssetManager amgr = new AssetManager();
        amgr.load("Logo.png", Pixmap.class);
        amgr.finishLoading();
        Pixmap pixmap = amgr.get("Logo.png");
        logo = new TextureRegionActor(TextureUtils.loadNonPotTexture(pixmap, true));
        amgr.dispose();

        final float portionOfWidth = (ActorUtil.isScreenPortrait() ? 0.8f : 0.5f);
        final float scale = ActorUtil.getScreenShortDim() * portionOfWidth / ActorUtil.getScreenShortDim(logo);
        logo.setScale(scale);
        // float ratio = logo.getWidth() / logo.getHeight();
        // logo.setWidth(Gdx.graphics.getWidth() / 2f);
        // logo.setHeight(logo.getWidth() / ratio);

        ActorUtil.centerActorOrigin(logo);
        ActorUtil.centerActor(logo);

        logo.setColor(WHITE_TRANSPARENT);
        logo.addAction(Actions.sequence(Actions.parallel(
                Actions.scaleBy(scale / 4f, scale / 4f, 4f, Interpolation.circleOut),
                Actions.sequence(Actions.alpha(1f, 2f), Actions.delay(3f)
                // TODO: show "Loading..."
                )), Actions.run(new Runnable() {
            @Override
            public void run() {
                endScreen();
            }
        })));
        stage.addActor(logo);

        music = Gdx.audio.newMusic(Gdx.files.internal("voxoid.ogg"));
        music.setLooping(false);
        music.play();

        assetManager = Assets.loadAll();
    }

    private void endScreen() {
        Assets.finishLoad();
        logo.addAction(Actions.sequence(Actions.alpha(0f, 1f), Actions.run(new Runnable() {
            @Override
            public void run() {
                ((Game) Gdx.app.getApplicationListener()).setScreen(new TitleScreen(Color.WHITE
                        .cpy()));
            }
        })));
    }

    @Override
    public void hide() {
        music.stop();
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
        music.dispose();
        stage.dispose();
    }
}
