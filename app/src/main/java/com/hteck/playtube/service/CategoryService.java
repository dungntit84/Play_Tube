package com.hteck.playtube.service;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.FileUtils;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.CategoryInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Vector;

public class CategoryService {
	public static Map.Entry<String, Vector<CategoryInfo>> getGenreListInfo() {
		String fileContent = FileUtils.readFileContent(
				PlayTubeController.getMainDirectory(), Constants.FILE_CATEGORYIES);
		if (Utils.stringIsNullOrEmpty(fileContent)) {
			fileContent = FileUtils.readFileContentInAssets(Constants.FILE_CATEGORYIES);
		}
		return getGenreListInfoByContent(fileContent);
	}

	public static AbstractMap.SimpleEntry<String, Vector<CategoryInfo>> getGenreListInfoByContent(
			String fileContent) {
		try {
			Document doc = Utils.parseDoc(fileContent);
			if (doc != null) {
				Vector<CategoryInfo> categoryList = new Vector<>();
				NodeList nodeListGenres = doc
						.getElementsByTagName(Constants.ItemConstants.ITEM);
				for (int i = 0; i < nodeListGenres.getLength(); ++i) {
					CategoryInfo categoryInfo = new CategoryInfo();
					Element element = (Element) nodeListGenres.item(i);
					categoryInfo.title = element
							.getAttribute(Constants.ItemConstants.TITLE);
					categoryInfo.id = element
							.getAttribute(Constants.ItemConstants.ID);

					categoryList.add(categoryInfo);
				}
				String version = Constants.MIN_VERSION;
				NodeList nodeListVersion = doc
						.getElementsByTagName(Constants.ItemConstants.VERSION);
				if (nodeListVersion.getLength() > 0) {
					version = ((Element) nodeListVersion.item(0))
							.getAttribute(Constants.ItemConstants.VALUE);
				}
				return new AbstractMap.SimpleEntry<>(version,
						categoryList);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
