package com.voxoid.bubbliminate;

public class DesktopPlatform implements IPlatform {

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void enableTopBannerAds(boolean enable) {
		
	}

	@Override
	public void shareScreenshot() {
		System.out.println("Share Screenshot");
	}

	@Override
	public float inchesToPixels(float inches) {
		return inches * (1600 / 12);
	}

	@Override
	public void rateApp() {
	}

}
