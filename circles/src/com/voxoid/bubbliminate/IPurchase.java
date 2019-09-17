package com.voxoid.bubbliminate;

import java.util.concurrent.ExecutionException;

public interface IPurchase {

	
	/** Starts initialization without blocking; initialization is not complete until finishInit() is complete. */
	void startInit();
	
	/** Will block until initialization is completed. 
	 * @throws ExecutionException If an exception occured inside the initialization thread
	 * @throws InterruptedException If the thread was interrupted
	 */
	void finishInit() throws InterruptedException;
	
	boolean hasBeenPurchased();
	
	PurchaseResult purchase() throws Exception;
}
