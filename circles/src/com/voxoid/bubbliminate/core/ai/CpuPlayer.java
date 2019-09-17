package com.voxoid.bubbliminate.core.ai;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.badlogic.gdx.graphics.Color;
import com.voxoid.bubbliminate.core.ai.minimax.BestReplySearch;
import com.voxoid.bubbliminate.core.ai.minimax.IEvaluation;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.model.ICirclesGameState;
import com.voxoid.bubbliminate.core.model.Player;
import com.voxoid.bubbliminate.core.rules.IMove;

/**
 * 
 * @author Joel
 *
 */
public class CpuPlayer extends Player implements ICpuPlayer {

	private static final Logger LOGGER = Logger.getLogger(CpuPlayer.class);
	
	private float maxWaitTime;
	private float minWaitTime;
	private float hurryPokeInterval = 1f;
	private IEvaluation evaluator;
	private transient ExecutorService moveExec;
	private transient ScheduledExecutorService hurryExec;
	private transient ScheduledFuture<?> hurryTimer;

	
	CpuPlayer(String name, int index, Color color, GameConfig gameConfig, int ply,
			float minWaitTime, float maxWaitTime,
			IEvaluation evaluator) {
		
		super(name, index, color);
		Validate.notNull(evaluator);
		this.evaluator = evaluator;
		
		this.minWaitTime = minWaitTime;
		this.maxWaitTime = maxWaitTime;
		readResolve();
	}
	
	/** For de-serialization only. */
	public CpuPlayer() {}
	
	private Object readResolve() {
		moveExec = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "CPU Player");
				thread.setDaemon(true);
				return thread;
			}
		});
		hurryExec = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "CPU Player Timer");
				thread.setDaemon(true);
				return thread;
			}
		});
		hurryTimer = null;
		return this;
	}
	
//	public CpuPlayer(String name, int index, Color color, GameConfig gameConfig, int ply, float maxWaitTime) {
//		this(name, index, color, gameConfig, ply, maxWaitTime,
//				new BestReplySearch()
//	}
	
	public void cancel() {
		evaluator.cancel();
	}
	
	public void dispose() {
		cancel();
		moveExec.shutdown();
		hurryExec.shutdown();
	}
	
	@Override
	public Future<IMove> chooseMove(final ICirclesGameState gameState) {
		hurryTimer = hurryExec.scheduleAtFixedRate(new Runnable() {	// TODO: pause when game paused!! (use delta time on updates...)
			@Override
			public void run() {
				LOGGER.info("Telling CPU Player to HURRY UP!");
				evaluator.hurryUp();
			}
		}, (long) (maxWaitTime * 1000f), (long) (hurryPokeInterval * 1000f), TimeUnit.MILLISECONDS);
		
		final long minCompleteTime = System.currentTimeMillis() + (long) (1000f * minWaitTime);
		final Future<IMove> future = moveExec.submit(new Callable<IMove>() {
			@Override
			public IMove call() throws Exception {
				IMove move = (IMove) evaluator.bestMove(gameState, getIndex());
				hurryTimer.cancel(true);
				LOGGER.info("CPU Player completing move now");
				
				long delay = minCompleteTime - System.currentTimeMillis();
				if (delay > 0) {
					Thread.sleep(delay);
				}
				return move;
			}
		});
		
		return future;
	}
	
}
