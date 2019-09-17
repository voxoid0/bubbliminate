package com.voxoid.bubbliminate.core.ai;

import java.util.List;

import com.badlogic.gdx.math.Circle;
import com.voxoid.bubbliminate.core.model.ICircle;

public class CirclesAreaCalc {

	private float cellSize;
	private transient float minX;
	private transient float maxX;
	private transient float minY;
	private transient float maxY;
	private transient int width;
	private transient int height;
	private transient boolean[] occupied = new boolean[0];
	
	
	public CirclesAreaCalc(float cellSize) {
		super();
		this.cellSize = cellSize;
	}


	public void setCircles(List<Circle> circles) {
		minX = Float.POSITIVE_INFINITY;
		maxX = Float.NEGATIVE_INFINITY;
		minY = Float.POSITIVE_INFINITY;
		maxY = Float.NEGATIVE_INFINITY;
		for (Circle circle : circles) {
			minX = Math.min(minX, circle.x - circle.radius);
			minY = Math.min(minY, circle.y - circle.radius);
			maxX = Math.max(maxX, circle.x + circle.radius);
			maxY = Math.max(maxY, circle.y + circle.radius);
		}
		minX -= cellSize;
		maxX += cellSize;
		minY -= cellSize;
		maxY += cellSize;
		
		width = (int) Math.ceil((maxX - minX) / cellSize);
		height = (int) Math.ceil((maxY - minY) / cellSize);
		
		occupied = new boolean[width * height];
		for (Circle circle : circles) {
			int cxi = (int) Math.round(((circle.x - minX) / cellSize));
			int cyi = (int) Math.round(((circle.y - minY) / cellSize));
			int yi = 0;
			for (float cy = 0f; cy < circle.radius; cy += cellSize, yi++) {
				float lineHalfWidth = (float) Math.sqrt(circle.radius*circle.radius - cy*cy);
				int xi = 0;
				for (float cx = 0f; cx < lineHalfWidth; cx += cellSize, xi++) {
					occupied[(cyi - yi)*width + (cxi - xi)] = true;
					occupied[(cyi + yi)*width + (cxi - xi)] = true;
					occupied[(cyi - yi)*width + (cxi + xi)] = true;
					occupied[(cyi + yi)*width + (cxi + xi)] = true;
				}
			}
		}
	}
	
	public float calculateArea() {
		int count = 0;
		for (boolean cell : occupied) {
			if (cell) count++;
		}
		return count * cellSize*cellSize;
	}
	
	public void clipToCircle(Circle clip) {
		for (int yi = 0; yi < height; yi++) {
			for (int xi = 0; xi < width; xi++) {
				
			}
		}
		
		int cxi = (int) Math.round(((clip.x - minX) / cellSize));
		int cyi = (int) Math.round(((clip.y - minY) / cellSize));
		int yi = 0;
		
		// Above the circle
		int topYi = (int) (cyi - clip.radius/cellSize);
		for (yi = 0; yi <= topYi; yi++) {
			for (int xi = 0; xi < width; xi++) {
				occupied[yi*width + xi] = false;
			}
		}
		
		// Below the circle
		int bottomYi = (int) (cyi + clip.radius/cellSize);
		for (yi = bottomYi; yi < height; yi++) {
			for (int xi = 0; xi < width; xi++) {
				occupied[yi*width + xi] = false;
			}
		}
		
		// To the sides of the circle
		yi = 0;
		for (float cy = 0f; cy < clip.radius; cy += cellSize, yi++) {
			
			float lineHalfWidth = (float) Math.sqrt(clip.radius*clip.radius - cy*cy);
			
			// To the left
			int xi = 0;
			for (float cx = minX; cx < clip.x - lineHalfWidth && xi < width; cx += cellSize, xi++) {
				if (cyi - yi >= 0 && cyi - yi < height) {
					occupied[(cyi - yi)*width + xi] = false;
				}
				if (cyi + yi >= 0 && cyi + yi < height) {
					occupied[(cyi + yi)*width + xi] = false;
				}
			}
			
			// To the right
			xi = width - 1;
			for (float cx = maxX - cellSize; cx > clip.x + lineHalfWidth && xi >= 0; cx -= cellSize, xi--) {
				if (cyi - yi >= 0 && cyi - yi < height) {
					occupied[(cyi - yi)*width + xi] = false;
				}
				if (cyi + yi >= 0 && cyi + yi < height) {
					occupied[(cyi + yi)*width + xi] = false;
				}
			}
		}		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i++) {
				sb.append(occupied[i] ? '*' : ' ');
//				  .append(' '); // To fix aspect ratio of text
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
}
