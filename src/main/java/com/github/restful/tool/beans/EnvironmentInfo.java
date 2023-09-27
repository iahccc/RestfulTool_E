package com.github.restful.tool.beans;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EnvironmentInfo
 *
 * @author iahc
 * @since 2022/11/7
 */
public class EnvironmentInfo extends LinkedHashMap<String, Map<String, Object>> implements Serializable {
	private static final long serialVersionUID = -7543418458252396202L;

	public static EnvironmentInfo fromJson(String json) {
		Gson gson = new Gson();
		EnvironmentInfo environmentInfo = null;
		try {
			environmentInfo = gson.fromJson(json, EnvironmentInfo.class);
		} catch (JsonSyntaxException ignored) {
		}
		return environmentInfo;
	}
}
