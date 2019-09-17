package com.voxoid.bubbliminate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.voxoid.bubbliminate.core.model.GameConfig;
import com.voxoid.bubbliminate.core.util.Function1;
import com.voxoid.bubbliminate.gameplay.CirclesGame;
import com.voxoid.bubbliminate.ui.Menu;
import com.voxoid.bubbliminate.ui.MenuUtils;


/**
 * Helps handle demo version limitations and purchasing the full version,
 * with UI messages and prompts.
 * 
 * @author Joel
 *
 */
public class DemoVersionHelper {

	private static final String LOCATING_PURCHASE_INFO_MSG = "Please wait while I locate purchase information...";
	private static final Logger LOGGER = Logger.getLogger(DemoVersionHelper.class);
	private static final String READ_GENERAL_MESSAGES_KEY = "readGeneralMessages";
	
	private AtomicBoolean allowed = new AtomicBoolean(false);
//	private AtomicBoolean pause = new AtomicBoolean(false);
	private AtomicInteger pause = new AtomicInteger(0);
	private IPurchase purchase;
	private WebMessages msgs;
	private IGameData gameData;
	
	public DemoVersionHelper(IPurchase purchase, WebMessages webMessages, IGameData gameData) {
		Validate.notNull(purchase);
		this.purchase = purchase;
		Validate.notNull(webMessages);
		this.msgs = webMessages;
		Validate.notNull(gameData);
		this.gameData = gameData;
	}
	
	public void setGameConfig(GameConfig gameConfig) {
		int nHumanPlayers = 0;
		int nNonBeginnerCpuPlayers = 0;
		for (PlayerConfig playerConfig : gameConfig.getPlayerConfigs()) {
			if (playerConfig.type == PlayerConfig.Type.HUMAN) {
				nHumanPlayers++;
			} else if (playerConfig.type == PlayerConfig.Type.CPU && playerConfig.cpuLevel != PlayerConfig.MIN_CPU_LEVEL) {
				nNonBeginnerCpuPlayers++;
			}
		}
		
		allowed.set(purchase.hasBeenPurchased() || nNonBeginnerCpuPlayers == 0);
	}
	
	/** True to pause host screen, because we're showing the user a message or waiting for his response. */
	public boolean pause() {
		return pause.get() > 0;
	}
	
	private void pauseForShow() {
		pause.incrementAndGet();
//		if (!pause.compareAndSet(false, true)) {
//			throw new IllegalStateException("Already showing something");
//		}
	}
	
	public void finishPurchaseInit(Stage stage, final Runnable after) {
			final Menu dialog = MenuUtils.openWaitDialog(stage, LOCATING_PURCHASE_INFO_MSG,
					null, 0.25f);
			Thread waitThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						purchase.finishInit();
						msgs.finishInit();
						MenuUtils.closeWaitDialog(dialog, after);
					} catch (InterruptedException e) {
						// nothing
					}
				}
			}, "Finish purchase and web messages init");
			waitThread.setDaemon(true);
			waitThread.start();
	}
	
	private Runnable createAfter(final Runnable after) {
		return new Runnable() {
			@Override
			public void run() {
				pause.decrementAndGet();
				if (after != null) after.run();
			}
		};
	}
	
	public void showGeneralMessages(final Stage stage, final Runnable after) {
		finishPurchaseInit(stage, new Runnable() {
			public void run() {
				
				List<String> unread = findUnreadGeneralMessages();
				
				if (!unread.isEmpty()) {
					pauseForShow();
					showMessageSequence(stage, unread, createAfter(after));
					updateReadGeneralMessages(unread);
				} else {
					after.run();
				}
			}
		});
	}
	
	private List<String> findUnreadGeneralMessages() {
		// Find unread general messages
		List<String> unread = msgs.getGeneralMessages();
		List<String> read = Arrays.asList(
				StringUtils.split(
						StringUtils.defaultString(gameData.getGameData(READ_GENERAL_MESSAGES_KEY)),
						'|'));
		unread = new ArrayList<String>(CollectionUtils.subtract(unread, read));
		return unread;
	}
	
	private void updateReadGeneralMessages(List<String> read) {
		String prevValue = gameData.getGameData(READ_GENERAL_MESSAGES_KEY);
		String newValue = (StringUtils.isEmpty(prevValue) ? "" : prevValue + "|") +
				StringUtils.join(read, '|');
		gameData.putGameData(READ_GENERAL_MESSAGES_KEY, newValue);
	}
	
	public void showAdMessages(final Stage stage, final Runnable after) {
		finishPurchaseInit(stage, new Runnable() {
			public void run() {
				if (!msgs.getAdMessages().isEmpty()) {
					pauseForShow();
					showPurchasePrompt(stage, msgs.getAdMessages(), createAfter(after));
				} else {
					after.run();
				}
			}
		});
	}
	
	public void showRestrictionsMessages(final Stage stage, final Runnable after) {
		finishPurchaseInit(stage, new Runnable() {
			public void run() {
				if (!msgs.getRestrictionsMessages().isEmpty()) {
					pauseForShow();
					showPurchasePrompt(stage, msgs.getRestrictionsMessages(), createAfter(after));
				} else {
					after.run();
				}
			}
		});
	}
	
	private void showPurchasePrompt(final Stage stage, final List<String> messages, final Runnable after) {
		showMessageSequence(stage, messages.subList(0, messages.size() - 1), new Runnable() {
			public void run() {
				final String[] options = new String[] { msgs.getPurchaseYesButtonText(), msgs.getPurchaseNoButtonText() };
				MenuUtils.dialog(stage, messages.get(messages.size() - 1),
						options,
						0, 0, new Function1<String>() {
							public void run(String result) {
								if (options[0].equals(result)) {
									makePurchase(stage, after);
								} else {
									declinePurchase(stage, after);
								}
							}
						});
			}
		});
	}
	
	public void makePurchase(final Stage stage, final Runnable after) {
		finishPurchaseInit(stage, new Runnable() {
			public void run() {
				try {
					purchase.purchase();
					final CirclesGame game = (CirclesGame)Gdx.app.getApplicationListener();
					game.addPauseListener(new Function1<Boolean>() {
						public void run(Boolean paused) {
							if (!paused) {
								game.removePauseListener(this);
								if (purchase.hasBeenPurchased()) {
									allowed.set(true);
									showMessageSequence(stage, msgs.getPurchaseCompletedMessages(), after);
								} else {
									if (after != null) after.run();
								}
							}
						}
					});
				} catch (Exception e) {
					MenuUtils.messageDialog(stage, msgs.getErrorDuringPurchaseMessage(), "OK", after);
					if (after != null) after.run();
				}
			}
		});
	}
	
	private void declinePurchase(final Stage stage, Runnable after) {
		showMessageSequence(stage, msgs.getPurchaseDeclinedMessages(), after);
	}
	
	private void showMessageSequence(final Stage stage, final List<String> msgs, Runnable after) {
		new ResursiveMessagesFunc(stage, msgs.iterator(), after).run();
	}
	
	public boolean allowedToPlay() {
		return allowed.get();
	}
	
	/**
	 * Recursive runnable for showing a sequence of message dialogs.
	 * @author Joel
	 *
	 */
	private static class ResursiveMessagesFunc implements Runnable {
		private Stage stage;
		private Iterator<String> iter;
		private Runnable after;
		
		public ResursiveMessagesFunc(Stage stage, Iterator<String> iter, Runnable after) {
			this.stage = stage;
			this.iter = iter;
			this.after = after;
		}
		
		public void run() {
			if (iter.hasNext()) {
				MenuUtils.messageDialog(stage, iter.next(), "OK", this);
			} else {
				if (after != null) {
					after.run();
				}
			}
		}
	}
}
