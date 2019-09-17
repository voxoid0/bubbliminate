package com.voxoid.bubbliminate;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;


public class OpenBrowserPurchaser implements IPurchase {

	private static final Logger LOGGER = Logger.getLogger(OpenBrowserPurchaser.class);
	
	private URI webpageUri;
	
	
	public OpenBrowserPurchaser(String webpageUri) throws URISyntaxException {
		Validate.notNull(webpageUri);
		this.webpageUri = new URI(webpageUri);
	}

	@Override
	public void startInit() {
		// TODO Check local purchase data
	}

	@Override
	public void finishInit() {
	}

	@Override
	public boolean hasBeenPurchased() {
		return true;
	}
	
	@Override
	public PurchaseResult purchase() {
		try {
			openWebpage(webpageUri);
			return PurchaseResult.Unknown;
		} catch (IOException e) {
			LOGGER.error("Error opening web page for purchasing", e);
			return PurchaseResult.Error;
		}
	}

	public static void openWebpage(URI uri) throws IOException {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri);
	    }
	}

	public static void openWebpage(URL url) throws IOException, URISyntaxException {
        openWebpage(url.toURI());
	}
}
