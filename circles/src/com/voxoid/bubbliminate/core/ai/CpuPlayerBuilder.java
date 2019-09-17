package com.voxoid.bubbliminate.core.ai;

import com.badlogic.gdx.graphics.Color;
import com.voxoid.bubbliminate.core.ai.minimax.BestReplySearch;
import com.voxoid.bubbliminate.core.ai.minimax.IGamePositionEvaluator;
import com.voxoid.bubbliminate.core.ai.minimax.IMovesGenerator;
import com.voxoid.bubbliminate.core.ai.minimax.NonIdealMoveChooser;
import com.voxoid.bubbliminate.core.model.GameConfig;

public class CpuPlayerBuilder {
	private String description;
	private int ply;
	private int bestMoveOrdinal = 1;
	private float minWaitTime;
	private float maxWaitTime;
	private IMovesGenerator movesGenerator;
	private IGamePositionEvaluator gamePositionEvaluator;
	
	
	public CpuPlayer build(GameConfig gameConfig, String name, int index, Color color) {
		if (description == null) description = "";
		if (ply == 0) ply = 2;
		if (maxWaitTime == 0f) maxWaitTime = 10f;
//		if (minWaitTime == 0f) minWaitTime = 0f; 
		if (movesGenerator == null) movesGenerator = new MovesGenerator(gameConfig, ply - 1);
		if (gamePositionEvaluator == null) gamePositionEvaluator = new ComplexGamePositionEvaluator(new CirclesAreaCalc(gameConfig.getMinCircleRadius()));
		
		BestReplySearch evaluator = new BestReplySearch(ply, movesGenerator, gamePositionEvaluator, gameConfig.getNumPlayers(),
				new NonIdealMoveChooser(bestMoveOrdinal * 500, bestMoveOrdinal));
		return new CpuPlayer(name, index, color, gameConfig, ply, minWaitTime, maxWaitTime,
				evaluator);
	}

	public CpuPlayerBuilder description(String description) {
		this.description = description;
		return this;
	}
	
	public CpuPlayerBuilder bestMoveOrdinal(int ordinal) {
		this.bestMoveOrdinal = ordinal;
		return this;
	}
	
	public CpuPlayerBuilder ply(int ply) {
		this.ply = ply;
		return this;
	}

	public CpuPlayerBuilder minWaitTime(float minWaitTime) {
		this.minWaitTime = minWaitTime;
		return this;
	}
	
	public CpuPlayerBuilder maxWaitTime(float maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
		return this;
	}

	public CpuPlayerBuilder movesGenerator(IMovesGenerator movesGenerator) {
		this.movesGenerator = movesGenerator;
		return this;
	}

	public CpuPlayerBuilder gamePositionEvaluator(
			IGamePositionEvaluator gamePositionEvaluator) {
		this.gamePositionEvaluator = gamePositionEvaluator;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public int getPly() {
		return ply;
	}

	public float getMinWaitTime() {
		return minWaitTime;
	}
	
	public float getMaxWaitTime() {
		return maxWaitTime;
	}

	public IMovesGenerator getMovesGenerator() {
		return movesGenerator;
	}

	public IGamePositionEvaluator getGamePositionEvaluator() {
		return gamePositionEvaluator;
	}
	
	
}
