package com.voxoid.bubbliminate.core.ai.minimax;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NonIdealMoveChooser implements IMoveChooser {

	private int maxScoreDrop;
	private int ordinalToChoose;	// 0 == best move, 1 == second best, etc.
	
	private int nCalls;
	private int nNonIdeal;
	

	/**
	 * 
	 * @param maxScoreDrop
	 * @param ordinalToChoose 1 is choose best move, 2 is choose 2nd best move, etc.
	 */
	public NonIdealMoveChooser(int maxScoreDrop, int ordinalToChoose) {
		super();
		this.maxScoreDrop = maxScoreDrop;
		this.ordinalToChoose = ordinalToChoose - 1;
	}
	
	/** For de-serialization only. */
	public NonIdealMoveChooser() {}

	@Override
	public MoveEvaluation choose(List<MoveEvaluation> evals) {
		nCalls++;
		
		if (evals.isEmpty()) {
			return null;
		}
		
		Collections.sort(evals, new Comparator<MoveEvaluation>() {
			public int compare(MoveEvaluation o1, MoveEvaluation o2) {
				return o2.score > o1.score ? 1 : (o1.score > o2.score ? -1 : 0);
			}
		});
		
		int i = Math.min(ordinalToChoose, evals.size() - 1);
		MoveEvaluation best = evals.get(0);
		while (i > 0 && best.score - evals.get(i).score > maxScoreDrop) {
			i--;
		}
		
		if (i != 0)
			nNonIdeal++;
		
		MoveEvaluation chosen = evals.get(i);
		//System.out.println("Choose " + i + " from best move, score " + chosen.score + " compared to best score " + evals.get(0).score);
		return chosen;
	}

}
