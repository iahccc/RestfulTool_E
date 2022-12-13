package com.github.restful.tool.utils.data;

import com.github.restful.tool.beans.ApiService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GlobalInfo
 *
 * @author iahc
 * @since 2022/12/13
 */
public class ProjectInfo {
	private static final Map<Project, ProjectInfo> INSTANCE_MAP = new HashMap<>();

	private Map<PsiMethod, List<ApiService>> apiServiceMap = new HashMap<>();

	private ProjectInfo() {
	}

	public static ProjectInfo get(Project project) {
		ProjectInfo projectInfo = INSTANCE_MAP.get(project);
		if(projectInfo == null) {
			INSTANCE_MAP.putIfAbsent(project, new ProjectInfo());
		}
		return INSTANCE_MAP.get(project);
	}

	public Map<PsiMethod, List<ApiService>> getApiServiceMap() {
		return apiServiceMap;
	}

	public void setApiServiceMap(Map<PsiMethod, List<ApiService>> apiServiceMap) {
		this.apiServiceMap = apiServiceMap;
	}
}
