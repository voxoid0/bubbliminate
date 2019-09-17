package com.voxoid.bubbliminate;

import java.util.Map;

public class DummyFlurryAgent implements IFlurryAgent {

	@Override
	public void setVersionName(String versionName) {
	}

	@Override
	public void onStartSession(String apiKey) {
	}

	@Override
	public void onEndSession() {
	}

	@Override
	public void endTimedEvent(String eventId) {
	}

	@Override
	public void logEvent(String eventId, boolean timed) {
	}

	@Override
	public void logEvent(String eventId, Map<String, String> parameters) {
	}

	@Override
	public void onError(String errorId, String message, Throwable exception) {
	}

	@Override
	public void onError(String errorId, String message, String s) {
	}

	@Override
	public void onPageView() {
	}
}
