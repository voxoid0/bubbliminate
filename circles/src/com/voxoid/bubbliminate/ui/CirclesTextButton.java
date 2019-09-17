package com.voxoid.bubbliminate.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.esotericsoftware.tablelayout.Cell;
import com.voxoid.bubbliminate.Assets;

public class CirclesTextButton {

    public static Cell configureButton(TextButton button, Table parent) {
        BitmapFont font = Assets.mediumGameFont;
        Cell cell = parent.add(button)
                .height((int) (font.getCapHeight() * 3))
                .width(font.getCapHeight() * 6)
                .expandX()
                .pad(6);
        button.add(button.getLabel()).padBottom(font.getCapHeight() - 36);
        return cell;
    }
}
