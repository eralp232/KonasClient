package org.newdawn.slick.muffin;

import java.io.IOException;
import java.util.HashMap;

/**
 * A muffin load/save implementation based on using Webstart Muffins (a bit like cookies only 
 * for webstart)
 * 
 * @author kappaOne
 */
public class WebstartMuffin implements Muffin {

	/**
	 * @see org.newdawn.slick.muffin.Muffin#saveFile(java.util.HashMap, java.lang.String)
	 */
	public void saveFile(HashMap scoreMap, String fileName) throws IOException {

	}

	/**
	 * @see org.newdawn.slick.muffin.Muffin#loadFile(java.lang.String)
	 */
	public HashMap loadFile(String fileName) throws IOException {
		HashMap hashMap = new HashMap();

		return hashMap;
	}
}
