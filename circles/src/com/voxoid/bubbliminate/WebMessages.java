package com.voxoid.bubbliminate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;

/**
 * Messages loaded from the Web, or falling back on defaults if the URL is unavailable.
 * 
 * @author joel.becker
 *
 */
public class WebMessages {
	private static final String DEFAULT_PURCHASE_ERROR_MESSAGE = "Oh no, Mon! We're sorry to hear that something went wrong. We'll try to fix it.";

	private static final String DEFAULT_PURCHASE_NO_BUTTON = "No";

	private static final String DEFAULT_PURCHASE_YES_BUTTON = "Yah Mon!";

	private static final List<String> DEFAULT_PURCHASE_DECLINED_MESSAGES = Collections.singletonList("Bummer, Mon. Well maybe next time.");

	private static final List<String> DEFAULT_PURCHASE_COMPLETED_MESSAGES = Collections.singletonList("Thanks, Mon, and enjoy!");

//	private static final List<String> DEFAULT_RESTRICTIONS_MESSAGES = Arrays.asList(new String[] {
//			"This is the demo version: you can play 3-player games with 1 human player and 2 Beginner CPU players.",
//			"Get the Full Version of Bubbliminate and play 2 to 8 players, human or CPU, forever! Also includes all 4 CPU levels. All for less than the price of a typical pizza! (And a pizza is gone within a day! OMNOM.)"	
//	});

	private static final List<String> DEFAULT_RESTRICTIONS_MESSAGES = Arrays.asList(new String[] {
			"Hey, Mon, wanna get the full version (for about the price of a banana, Mon!) so that you can play not only Beginner CPU level, but all 4 CPU levels? Forever?"	
	});

	private static final List<String> DEFAULT_GENERAL_MESSAGES = Collections.<String>emptyList();

	private static final List<String> DEFAULT_AD_MESSAGES = Collections.<String>emptyList();
	
	private static final int HTTP_TIMEOUT_MS = 30 * 1000;
	
	private static final Logger LOGGER = Logger.getLogger(WebMessages.class);
	
	private String propertiesUrl;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private CountDownLatch initLatch = new CountDownLatch(1);
	private List<String> generalMessages;
	private List<String> restrictionsMessages;
	private List<String> adMessages;
	private List<String> purchaseDeclinedMessages;
	private List<String> purchaseCompletedMessages;
	private String purchaseYesButtonText;
	private String purchaseNoButtonText;
	private String errorDuringPurchaseMessage;
	
	
	public WebMessages(String propertiesUrl) {
		this.propertiesUrl = propertiesUrl;
	}

	/**
	 * Loads the messages from the web, or uses the default values if that fails. When finished
	 * initializing the messages, the given {@link CountDownLatch} is decremented.
	 * 
	 * @param initLatch
	 */
	public void startInit() {
		final Properties props = new Properties();
		HttpRequest httpRequest = new HttpRequest(HttpMethods.GET);
		httpRequest.setUrl(propertiesUrl);
		httpRequest.setTimeOut(HTTP_TIMEOUT_MS);
		
		LOGGER.info("Requesting game data");
//		CirclesGlobal.flurry.logEvent("Requesting game data", false);
		
		Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				LOGGER.info("Game data received");
				
				InputStream in = httpResponse.getResultAsStream();
				
				try {
					props.load(in);
				} catch (IOException e) {
					LOGGER.error("Error getting data from " + propertiesUrl, e);
				}
				initProperties();
				initLatch.countDown();
			}

			@Override
			public void failed(Throwable t) {
				CirclesGlobal.flurry.logEvent("Failed getting game data from " + propertiesUrl + ": " + t.getMessage(), false);
				initProperties();
				initLatch.countDown();
			}
			
			private void initProperties() {
				// Note: DO NOT mention a price in the default messages, since the price may change
				generalMessages = readMessages(props, "generalMessage", DEFAULT_GENERAL_MESSAGES);
				adMessages = readMessages(props, "adMessage", DEFAULT_AD_MESSAGES);
				restrictionsMessages = readMessages(props, "restrictionsMessage", DEFAULT_RESTRICTIONS_MESSAGES);
				purchaseCompletedMessages = readMessages(props, "purchaseCompletedMessage", DEFAULT_PURCHASE_COMPLETED_MESSAGES);
				purchaseDeclinedMessages = readMessages(props, "purchaseDeclinedMessage", DEFAULT_PURCHASE_DECLINED_MESSAGES);
				purchaseYesButtonText = props.getProperty("purchaseYes", DEFAULT_PURCHASE_YES_BUTTON);
				purchaseNoButtonText = props.getProperty("purchaseNo", DEFAULT_PURCHASE_NO_BUTTON);
				errorDuringPurchaseMessage = props.getProperty("errorDuringPurchaseMessage", DEFAULT_PURCHASE_ERROR_MESSAGE);
			}
			
			private List<String> readMessages(Properties props, String baseName, List<String> defaults) {
				List<String> msgs = new ArrayList<String>();
				int i = 1;
				while (props.containsKey(baseName + i)) {
					msgs.add(props.getProperty(baseName + i));
					i++;
				}
				return msgs.isEmpty() ? defaults : msgs;
			}
			
		});
		
	}
	
	public synchronized void finishInit() throws InterruptedException {
		initLatch.await();
		initialized.set(true);
	}


	private void checkInit() {
		if (!initialized.get()) {
			throw new IllegalStateException("Must finishInit() first");
		}
	}
	
	public List<String> getGeneralMessages() {
		checkInit();
		return new ArrayList<String>(generalMessages);
	}


	public List<String> getRestrictionsMessages() {
		checkInit();
		return new ArrayList<String>(restrictionsMessages);
	}


	public List<String> getAdMessages() {
		checkInit();
		return new ArrayList<String>(adMessages);
	}


	public List<String> getPurchaseCompletedMessages() {
		checkInit();
		return new ArrayList<String>(purchaseCompletedMessages);
	}


	public List<String> getPurchaseDeclinedMessages() {
		checkInit();
		return new ArrayList<String>(purchaseDeclinedMessages);
	}


	public String getPurchaseYesButtonText() {
		checkInit();
		return purchaseYesButtonText;
	}


	public String getPurchaseNoButtonText() {
		checkInit();
		return purchaseNoButtonText;
	}


	public String getErrorDuringPurchaseMessage() {
		checkInit();
		return errorDuringPurchaseMessage;
	}

}
