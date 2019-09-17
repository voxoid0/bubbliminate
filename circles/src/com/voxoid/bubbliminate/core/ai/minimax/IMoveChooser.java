package com.voxoid.bubbliminate.core.ai.minimax;

import java.util.List;

/**
 * Chooses a move among the given {@link MoveEvaluation}s. This is used for non-expert CPU levels
 * to choose a less-than-ideal move.
 * 
 * @author Joel
 *
 */
public interface IMoveChooser {

	MoveEvaluation choose(List<MoveEvaluation> evals);
}
