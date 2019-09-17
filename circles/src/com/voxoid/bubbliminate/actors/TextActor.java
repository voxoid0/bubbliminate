package com.voxoid.bubbliminate.actors;

import org.apache.commons.lang.Validate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;


public class TextActor extends Actor {

    private BitmapFont font;
    private Color color;
    private String text;
    
    public TextActor(Color color){
    	this(new BitmapFont(), color, "");
    }
    
    public TextActor(BitmapFont font, Color color, String text){
    	Validate.notNull(font);
    	this.font = font;
    	Validate.notNull(color);
    	this.color = color.cpy();
    	Validate.notNull(text);
    	this.text = text;
    	
    	TextBounds bounds = font.getBounds(text);
    	setSize(bounds.width, bounds.height);
    }

    public BitmapFont getFont() {
		return font;
	}

	public void setFont(BitmapFont font) {
		this.font = font;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
    public void draw(SpriteBatch batch, float parentAlpha) {
		batch.enableBlending();
		Color prevColor = font.getColor();
		font.setColor(color);
         font.draw(batch, text, getX(), getY(
        		 ));
         font.setColor(prevColor);
//         batch.disableBlending();
         //Also remember that an actor uses local coordinates for drawing within
         //itself!
    }
}
