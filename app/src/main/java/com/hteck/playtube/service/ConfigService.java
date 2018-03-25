package com.hteck.playtube.service;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.FileUtils;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ConfigInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;

public class ConfigService {

	public static ConfigInfo getConfigInfo() {
		String fileContent = FileUtils.readFileContent(
				PlayTubeController.getMainDirectory(), Constants.FILE_CONFIG);
		if (Utils.stringIsNullOrEmpty(fileContent)) {
			fileContent = FileUtils.readFileContentInAssets(Constants.FILE_CONFIG);
		}

		return getConfigInfo(fileContent);
	}

	public static ConfigInfo getConfigInfo(String fileContent) {
		try {

			Document doc = Utils.parseDoc(fileContent);
			if (doc != null) {
				ConfigInfo result = new ConfigInfo();
				result = populate(result, doc.getDocumentElement());
				return result;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		ConfigInfo result = new ConfigInfo();
		return result;
	}

	private static ConfigInfo populate(ConfigInfo configInfo, Element element) {
		try {
			Class<?> clazz = configInfo.getClass();

			for (Field field : clazz.getDeclaredFields()) {

				NodeList nodeList = element.getElementsByTagName(field
						.getName());
				if (nodeList.getLength() > 0) {
					Object propValue = Utils.convertToObject(field.getType(),
							((Element) nodeList.item(0)).getAttribute("value"));

					field.set(configInfo, propValue);
				}
			}

			return configInfo;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

}
