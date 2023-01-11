/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: AppSettingsState
  Author:   ZhangYuanSheng
  Date:     2020/5/27 18:08
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.configuration;

import com.github.restful.tool.beans.RequestInfo;
import com.github.restful.tool.beans.settings.Settings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
@State(name = "RestfulSetting", storages = @Storage(AppSetting.IDE))
public class AppSetting implements PersistentStateComponent<AppSetting> {

    public static final String IDE = "RestfulToolSetting.xml";

    private final Settings setting;

    public String envJson;
    public final Map<String, RequestInfo> requestInfoMap;

    public AppSetting() {
        this.setting = new Settings();
        this.setting.initValue();

        requestInfoMap = new HashMap<>();
        envJson = "";
    }

    public static AppSetting getInstance() {
        return ApplicationManager.getApplication().getService(AppSetting.class);
    }

    public boolean isModified(Settings changedSetting) {
        if (changedSetting == null) {
            return false;
        }
        return this.setting.isModified(changedSetting);
    }

    @Nullable
    @Override
    public AppSetting getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSetting state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    public Settings getAppSetting() {
        return this.setting;
    }

    public void setAppSetting(Settings setting) {
        this.setting.applySetting(setting);
    }

    public RequestInfo getRequestInfo(Project project, String key) {
        if(key == null) {
            return null;
        }
        return requestInfoMap.get(getRequestInfoKey(project, key));
    }

    public void removeRequestInfo(Project project, String key) {
        if(key == null) {
            return;
        }
        requestInfoMap.remove(getRequestInfoKey(project, key));
    }

    public void saveRequestInfo(Project project, String key, RequestInfo requestInfo) {
        if(key == null) {
            return;
        }
        requestInfoMap.put(getRequestInfoKey(project, key), requestInfo);
    }

    private static String getRequestInfoKey(Project project, String requestIdentity) {
        return project.getName() + " " + requestIdentity;
    }

    public String getEnvJson() {
        return envJson;
    }

    public void setEnvJson(String envJson) {
        this.envJson = envJson;
    }
}
