package com.voxoid.bubbliminate;

import java.util.Map;

public interface IFlurryAgent {
	void setVersionName(String versionName);
	void onStartSession(String apiKey);
	void onEndSession();
	void endTimedEvent(String eventId);
	void logEvent(String eventId, boolean timed);
	void logEvent(String eventId, Map<String, String> parameters);
	void onError(String errorId, String message, Throwable exception);
	void onError(String errorId, String message, String s);
	void onPageView();
}
