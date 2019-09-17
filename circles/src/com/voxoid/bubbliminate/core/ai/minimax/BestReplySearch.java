package com.voxoid.bubbliminate.core.ai.minimax;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.IPlayerState;


/**
 * Treats the game much like a two-player game in that when considering the opponents' options, it
 * finds the strongest reply of all the players and analyzes from there, ignoring the weaker moves
 * that other players may reply with.
 * 
 * Because it is treated as a two-player game, alpha-beta pruning algorithm can be applied. Suppose
 * player 1 is the moving player. After player 1 considering one move and the opponents' strongest
 * reply move which scores only 5 for player 1 (opponents minimize player 1's score), then if
 * player one considers a different move where the opponents are able to reply with a score of
 * only 4 for player 1, player 1 will not consider that move any further because the first move would
 * prevent his opponents from damaging him as much (5). Thus the rest of the sub tree for the 2nd
 * move can be "pruned" (ignored). This lower bound (5 in this example) is named alpha.
 * 
 * "If the evaluated move is worse for my parent than what he's seen before, prune/return because he's
 * not going to choose that move which results in this state where I can make things worse for him."
 * 
 * @author Joel
 *
 */
public class BestReplySearch implements IEvaluation {

 	private int plyToSearch;
 	 
	private IMovesGenerator movesGenerator;
	private IGamePositionEvaluator posEval;
	private int numPlayers;
	private volatile int plyHurry;
	private volatile boolean cancel;
	private int nMovesProcessed;	// not multithreaded
	private boolean trace = true;
	private IMoveChooser moveChooser;
	
	private enum Turn {
		MAX, MIN
	};
	

	
	public BestReplySearch(int plyToSearch, IMovesGenerator movesGenerator,
			IGamePositionEvaluator posEval, int numPlayers, IMoveChooser moveChooser) {
		Validate.isTrue(plyToSearch > 0);
		this.plyToSearch = plyToSearch;
		Validate.notNull(movesGenerator);
		this.movesGenerator = movesGenerator;
		Validate.notNull(posEval);
		this.posEval = posEval;
		Validate.isTrue(numPlayers > 1);
		this.numPlayers = numPlayers;
		Validate.notNull(moveChooser);
		this.moveChooser = moveChooser;
	}

	/** For de-serialization only. */
	public BestReplySearch() {}
	
	@Override
	public IGameMove bestMove(IGameState s, int playerNum) {
		trace = false;
		if (trace) System.out.println("******** Player " + playerNum + " Moving ********");
		plyHurry = 0;
		cancel = false;
		
		nMovesProcessed = 0;
		long startTime = System.currentTimeMillis();
		
		MoveEvaluation eval = bestReply(s, Integer.MIN_VALUE+1, Integer.MAX_VALUE, 0, Turn.MAX, playerNum, playerNum);
		
		System.out.println(String.format("%d moves processed with %d ply, in %.1f seconds",
				nMovesProcessed, plyToSearch, (System.currentTimeMillis() - startTime) / 1000f));
		return eval.move;
	}

	@Override
	public void hurryUp() {
		plyHurry++;
	}
	
	@Override
	public void cancel() {
		cancel = true;
	}
	
	/**
	 * 
	 * @param state
	 * @param worstForParent Worst score the other/parent Turn has seen so far, but in terms of us: higher == worse for them, better for us.
	 * @param worstForMe Worst score Turn has seen so far in grand-parent node, in terms of us: lower == worse for us, better for them. So both of these have the same scale for us: higher is better for us.
	 * @param ply
	 * @param turn
	 * @param playerNum
	 * @param movingPlayer
	 * @return
	 */
	private MoveEvaluation bestReply(IGameState state, int worstForParent, int worstForMe,
			int ply, Turn turn, int playerNum, int movingPlayer) {
		
		if (trace) print(ply, "worstForParent=" + worstForParent + "  worstForMe=" + worstForMe);
		
		List<MoveEvaluation> firstPlyEvals = new ArrayList<MoveEvaluation>();
		
		List<IGameMove> moves;
		if (turn == Turn.MAX) {
			moves = movesGenerator.generateMoves(state, movingPlayer, ply);
			
			if (trace) print(ply, "MAX's turn. " + moves.size() + " moves");
			turn = Turn.MIN;
		} else {
			moves = movesGenerator.generateBestReplyMoves(state, movingPlayer, ply);
			if (trace) print(ply, "MIN's turn. " + moves.size() + " moves");
			turn = Turn.MAX;
		}
		
		if (moves.isEmpty()) {
			throw new IllegalStateException("IMovesGenerator did not generate any moves!");
		}
		
		MoveEvaluation best = new MoveEvaluation(worstForParent); // -Integer.MIN_VALUE?
		try {
			for (IGameMove move : moves) {
				MoveEvaluation eval; // Evaluation of this move, in terms of how good it is for us
				IGameState newState = move.make(state);

				if (trace) {
					String str = "Evaluating move " + move + " resulting in numCircles {";
					ICirclesGameState cgs = (ICirclesGameState) newState;
					for (IPlayerState ps : cgs.getPlayerStates()) str += ps.getNumCircles() + ",";
					print(ply, str + "}");
				}
				
				if (newState.gameIsOver() || newState.gameIsOverFor(movingPlayer) ||
						ply+1+plyHurry >= plyToSearch || cancel) {
					
					eval = new MoveEvaluation(move, posEval.eval(newState, move.getPlayerNum()));
					if (trace) print(ply, "LEAFY! eval score " + eval.score + ", move " + eval.move);
				} else {
					int worstForNextParent = -worstForMe;
					int worstForNextMe = best.score > worstForParent ? -best.score : -worstForParent;	// The worst the next guy has seen is (the negative of...) the best I've seen of my moves, or the worst he has seen of my moves (for him), whichever is greater
					eval = bestReply(newState, worstForNextParent, worstForNextMe,
							ply+1, turn, move.getPlayerNum(), movingPlayer);
					eval = new MoveEvaluation(move, -eval.score);
				}
				
				if (ply == 0) {
					firstPlyEvals.add(eval);
				}
				
				if (eval.score > best.score || best.move == null) {
					// || (eval.score == best.score && Math.random() > 0.66)
					best = eval;
					// If the parent node's player's score is minimized here more than previously (if this node's player's score is maximized here more than previously), then don't bother him with the rest of the tree
					// "I can already see that I could make the other guy worse off in this state resulting from the move he made, so I'll save him some time by not searching for better moves, since he won't want to move here.
					if (best.score >= -worstForParent) { // -eval.score ?? worstForMe originally
						if (trace) print(ply, "PRUNE best.score " + best.score + " >= -worstForParent " + (-worstForParent));
						break;
					}
				}
			}
			nMovesProcessed += moves.size();
		} catch (Exception e) { // error making a move should just cancel CPU thinking about this possibility
			// TODO: log, but only a few times or once
			e.printStackTrace();
		}
		
		if (trace) print(ply, "best = score " + best.score + ", move " + best.move);
		return ply == 0 ? moveChooser.choose(firstPlyEvals) : best;
	}

	private static void print(int ply, String msg) {
		for (int i = 0; i < ply; i++) System.out.print("\t");
		System.out.println(msg);
	}
}
