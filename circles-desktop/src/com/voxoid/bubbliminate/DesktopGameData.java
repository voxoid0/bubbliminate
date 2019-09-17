package com.voxoid.bubbliminate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

public class DesktopGameData implements IGameData {

	private File file = new File("c:/users/Joel/bubbliminate.xml");
	private XStream xstream = CirclesGlobal.systemUtil.createXStream();
	
	private Map<String, String> data = new HashMap<String, String>();
	
	public DesktopGameData() {
		if (file.exists()) {
			try {
				data = (Map<String, String>) xstream.fromXML(file);
			} catch (Exception ex) {
			}
		}
	}
	
	@Override
	public Map<String, String> getAllGameData() {
		return Collections.unmodifiableMap(data);
	}

	@Override
	public String getGameData(String key) {
		return data.get(key);
	}

	@Override
	public void putGameData(String key, String value) {
		data.put(key, value);
		try {
			OutputStream out = new FileOutputStream(file);
			try {
				xstream.toXML(data, out);
			} finally {
				out.close();
			}
		} catch (Exception ex) {
			System.err.println("Error saving GameData: " + ex.toString());
		}
	}
}
