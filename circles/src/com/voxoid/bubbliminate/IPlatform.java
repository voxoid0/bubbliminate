package com.voxoid.bubbliminate;

public interface IPlatform {

	void start();
	void stop();

	void enableTopBannerAds(boolean enable);
	void shareScreenshot();
	float inchesToPixels(float inches);
	void rateApp();
}