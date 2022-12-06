/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestDetail
  Author:   ZhangYuanSheng
  Date:     2020/5/21 23:54
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.frame;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.EnvironmentInfo;
import com.github.restful.tool.beans.HttpMethod;
import com.github.restful.tool.beans.RequestInfo;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.configuration.AppSetting;
import com.github.restful.tool.service.Notify;
import com.github.restful.tool.service.ToolWindowService;
import com.github.restful.tool.service.topic.RestDetailTopic;
import com.github.restful.tool.utils.Async;
import com.github.restful.tool.utils.HttpUtils;
import com.github.restful.tool.utils.convert.ParamsConvert;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.utils.data.JsonUtil;
import com.github.restful.tool.view.components.editor.CustomEditor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class HttpTestPanel extends JPanel {

    public static final FileType DEFAULT_FILE_TYPE = CustomEditor.TEXT_FILE_TYPE;

    private static final String IDENTITY_URL = "URL";
    private static final String IDENTITY_HEAD = "HEAD";
    private static final String IDENTITY_BODY = "BODY";
    private static final String IDENTITY_SCRIPT = "SCRIPT";

    private final transient Project project;

    private final transient Map<ApiService, String> bodyCache;
    private final transient Map<ApiService, FileType> bodyTextTypeCache;

    /**
     * 下拉框 - 选择选择请求方法
     */
    private JComboBox<HttpMethod> requestMethod;
    /**
     * 下拉框 - 选择环境
     */
    private JComboBox<String> environment;

    /**
     * 输入框 - url地址
     */
    private CustomEditor requestUrl;
    /**
     * 按钮 - 发送请求
     */
    private JButton sendRequest;

    /**
     * 选项卡面板 - 请求信息
     */
    private transient JBTabs tabs;

    /**
     * 文本域 - 请求头
     */
    private transient TabInfo headTab;
    private CustomEditor requestHead;

    /**
     * 文本域 - 请求体
     */
    private transient TabInfo bodyTab;
    private CustomEditor requestBody;
    private ComboBox<FileType> requestBodyFileType;

    /**
     * 标签 - 显示返回结果
     */
    private transient TabInfo responseTab;
    private CustomEditor responseView;

    /**
     * 脚本
     */
    private TabInfo scriptTab;
    private CustomEditor requestScript;
    private JButton resetBtn;

    private transient DetailHandle callback;

    /**
     * 选中的Request
     */
    private transient ApiService chooseApiService;

    public HttpTestPanel(@NotNull Project project) {
        this.project = project;

        this.bodyCache = new HashMap<>();
        this.bodyTextTypeCache = new HashMap<>();

        initView();

        initEvent();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(filterPanel, BorderLayout.NORTH);

        JPanel panelInput = new JPanel();
        northPanel.add(panelInput, BorderLayout.SOUTH);
        panelInput.setLayout(new BorderLayout(0, 0));

        requestMethod = new ComboBox<>(HttpMethod.getValues());
        filterPanel.add(requestMethod);

        EnvironmentInfo environmentInfo = EnvironmentInfo.fromJson(AppSetting.getInstance().getEnvJson());
        String[] items = {"No Environment"};
        if(environmentInfo != null) {
            items = environmentInfo.keySet().toArray(new String[0]);
        }
        environment = new ComboBox<>(items);
        filterPanel.add(environment);


        requestUrl = new CustomEditor(project, CustomEditor.TEXT_FILE_TYPE, false, false, false);
        requestUrl.setOneLineMode(true);
        requestUrl.setName(IDENTITY_URL);
        if(requestUrl.getEditor() != null) {
            requestUrl.getEditor().getSettings().setLineNumbersShown(false);
        }
        panelInput.add(requestUrl);

        JPanel jPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        panelInput.add(jPanel, BorderLayout.EAST);
        sendRequest = new JButton(AllIcons.Actions.Execute);
        sendRequest.setPreferredSize(new Dimension(30, 30));
        sendRequest.setToolTipText("Click to send the request");
        sendRequest.setOpaque(false);
        sendRequest.setContentAreaFilled(false);
        sendRequest.setBorder(BorderFactory.createEmptyBorder());
        jPanel.add(sendRequest);
        resetBtn = new JButton(AllIcons.Actions.BuildLoadChanges);
        resetBtn.setPreferredSize(new Dimension(30, 30));
        resetBtn.setToolTipText("Double click to reset request info");
        resetBtn.setOpaque(false);
        resetBtn.setContentAreaFilled(false);
        resetBtn.setBorder(BorderFactory.createEmptyBorder());
        jPanel.add(resetBtn);

        tabs = new JBTabsImpl(project);

        requestHead = new CustomEditor(project, CustomEditor.JSON_FILE_TYPE);
        requestHead.setName(IDENTITY_HEAD);
        headTab = new TabInfo(requestHead);
        headTab.setText(Bundle.getString("http.tool.tab.head"));
        tabs.addTab(headTab);

        requestBody = new CustomEditor(project, DEFAULT_FILE_TYPE);
        requestBody.setName(IDENTITY_BODY);
        bodyTab = new TabInfo(requestBody);
        bodyTab.setText(Bundle.getString("http.tool.tab.body"));
        tabs.addTab(bodyTab);
        // 设置JsonEditor为JPanel的下一个焦点
        putClientProperty("nextFocus", requestBody);

        responseView = new CustomEditor(project);
        responseView.setViewer(true);
        responseTab = new TabInfo(responseView);
        responseTab.setText(Bundle.getString("http.tool.tab.response"));
        tabs.addTab(responseTab);

        // Script component
        requestScript = new CustomEditor(project);
        requestScript.setName(IDENTITY_SCRIPT);
        scriptTab = new TabInfo(requestScript);
        scriptTab.setText(Bundle.getString("http.tool.tab.script"));
        tabs.addTab(scriptTab);

        add(tabs.getComponent(), BorderLayout.CENTER);

        JPanel bodyFileTypePanel = new JPanel(new BorderLayout());
        bodyFileTypePanel.add(new JBLabel(Bundle.getString("other.restDetail.chooseBodyFileType")), BorderLayout.WEST);
        requestBodyFileType = new ComboBox<>(new FileType[]{
                CustomEditor.TEXT_FILE_TYPE,
                CustomEditor.JSON_FILE_TYPE,
                CustomEditor.HTML_FILE_TYPE,
                CustomEditor.XML_FILE_TYPE
        });
        requestBodyFileType.setFocusable(false);
        bodyFileTypePanel.add(requestBodyFileType, BorderLayout.CENTER);
        bodyFileTypePanel.setBorder(JBUI.Borders.emptyLeft(3));
        add(bodyFileTypePanel, BorderLayout.SOUTH);
        tabs.addListener(new TabsListener() {
            @Override
            public void beforeSelectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                bodyFileTypePanel.setVisible(bodyTab.getText().equalsIgnoreCase(newSelection.getText()));
            }
        });

        TabInfo selectedTab = tabs.getSelectedInfo();
        if (selectedTab == null) {
            bodyFileTypePanel.setVisible(false);
        } else {
            bodyFileTypePanel.setVisible(bodyTab.getText().equalsIgnoreCase(selectedTab.getText()));
        }

        setColor(false);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        // 发送请求按钮监听
        sendRequest.addActionListener(event -> {
            String url = requestUrl.getText();
            if (url == null || "".equals(url.trim())) {
                requestUrl.requestFocus();
                return;
            }

            // 选择Response页面
            tabs.select(responseTab, true);
            sendRequest(url);
        });

        requestBodyFileType.setSelectedItem(getCacheType());
        requestBodyFileType.setRenderer(new FileTypeRenderer());
        requestBodyFileType.addItemListener(event -> {
            ItemSelectable selectable = event.getItemSelectable();
            if (selectable == null) {
                return;
            }
            Object[] selects = selectable.getSelectedObjects();
            if (selects == null || selects.length < 1) {
                return;
            }
            Object select = selects[0];
            if (select instanceof FileType) {
                FileType fileType = (FileType) select;
                requestBody.setFileType(fileType);
                setCacheType(fileType);
            }
        });

        MessageBusConnection messageBusConnection = project.getMessageBus().connect();
        messageBusConnection.subscribe(RestDetailTopic.TOPIC, (RestDetailTopic) request -> {
            if (request != null) {
                bodyCache.remove(request);
                bodyTextTypeCache.remove(request);
            } else {
                bodyCache.clear();
                bodyTextTypeCache.clear();
            }
        });

        DocumentListener documentListenerForCache = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                CustomEditor editor = getCurrentTabbedOfRequest();
                if (editor != null && chooseApiService != null) {
                    String name = editor.getName();
                    String text = editor.getText();
                    if(name != null) {
                        setCache(name, chooseApiService, text);
                    }

                    // 保存请求信息
                    RequestInfo requestInfo = new RequestInfo();
                    HttpMethod method = Optional.ofNullable((HttpMethod) requestMethod.getSelectedItem()).orElse(HttpMethod.GET);
                    requestInfo.setHttpMethod(method);
                    requestInfo.setUrl(requestUrl.getText());
                    requestInfo.setHead(requestHead.getText());
                    requestInfo.setRequestBody(requestBody.getText());
                    requestInfo.setScript(requestScript.getText());
                    if(!requestInfo.equals(AppSetting.getInstance().getRequestInfo(chooseApiService.getIdentity()))) {
                        AppSetting.getInstance().saveRequestInfo(chooseApiService.getIdentity(), requestInfo);
                        setColor(true);
                    }
                }
            }
        };
        // fixBug: 无法正确绑定监听事件，导致无法缓存单个request的请求头或请求参数的数据
        Function<CustomEditor, FocusAdapter> function = (editor) -> new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                editor.addDocumentListener(documentListenerForCache);
            }

            @Override
            public void focusLost(FocusEvent e) {
                editor.removeDocumentListener(documentListenerForCache);
            }
        };

        requestUrl.addFocusListener(function.apply(requestUrl));
        requestHead.addFocusListener(function.apply(requestHead));
        requestBody.addFocusListener(function.apply(requestBody));
        requestScript.addFocusListener(function.apply(requestScript));

        resetBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2) {
                    if (chooseApiService != null) {
                        AppSetting.getInstance().removeRequestInfo(chooseApiService.getIdentity());
                        bodyCache.remove(chooseApiService);
                        bodyTextTypeCache.remove(chooseApiService);
                        chooseApiService.setHeaders("");
                        chooseRequest(chooseApiService);
                    }
                }
            }
        });
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                Component component = e.getComponent();
                if(component instanceof JButton) {
                    JButton button = (JButton) component;
                    button.setOpaque(true);
                    button.setContentAreaFilled(true);
                    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                Component component = e.getComponent();
                if(component instanceof JButton) {
                    JButton button = (JButton) component;
                    button.setOpaque(false);
                    button.setContentAreaFilled(false);
                    button.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        sendRequest.addMouseListener(mouseAdapter);
        resetBtn.addMouseListener(mouseAdapter);
    }

    private void execScript(String responseBody, Map<String, List<String>> headers) {
        ScriptEngine engine =  new ScriptEngineManager().getEngineByName("graal.js");;
        Invocable invocable = (Invocable) engine;
        InputStream jsInputStream = this.getClass().getResourceAsStream("/js/base.js");
        InputStreamReader jsReader = new InputStreamReader(jsInputStream);

        String script = requestScript.getText();

        Gson gson = new Gson();
        try {
            engine.eval(jsReader);
            invocable.invokeFunction("setResponseBody", responseBody);
            invocable.invokeFunction("setResponseHeaders", gson.toJson(headers));
            engine.eval(script);

//            String testResult = (String) invocable.invokeFunction("test");
//            System.out.println(testResult);
            String result = (String) invocable.invokeFunction("getGlobalVariables");

            Map<String, String> map = gson.fromJson(result, Map.class);
            addVariable((String) environment.getSelectedItem(), map);
            System.out.println(result);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void execScript0(String responseBody, Map<String, List<String>> headers) {

        Thread.currentThread().setContextClassLoader(Context.class.getClassLoader());
        try (Context context = Context.newBuilder().allowAllAccess(true).build()) {
            Value value = context.getBindings("js");

            InputStream jsInputStream = this.getClass().getResourceAsStream("/js/base.js");
            InputStreamReader jsReader = new InputStreamReader(jsInputStream);

            String script = requestScript.getText();
            Gson gson = new Gson();
            try {
                Source baseSource = Source.newBuilder("js", jsReader, "base").build();
                context.eval(baseSource);
                value.getMember("setResponseBody").executeVoid(responseBody);;
                value.getMember("setResponseHeaders").executeVoid(gson.toJson(headers));;
                context.eval("js", script);
                Value globalVariables = value.getMember("getGlobalVariables").execute();

                String result = globalVariables.asString();
                Map<String, String> map = gson.fromJson(result, Map.class);
                addVariable((String) environment.getSelectedItem(), map);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addVariable(String envKey, Map<String, String> newVariableMap) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        EnvironmentInfo environmentInfo = EnvironmentInfo.fromJson(AppSetting.getInstance().getEnvJson());
        if(environmentInfo == null) {
            return;
        }
        Map<String, Object> variableMap = environmentInfo.get(envKey);

        for(Map.Entry<String, String> entry : newVariableMap.entrySet()) {
            if(entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            variableMap.put(entry.getKey(), entry.getValue());
        }
        AppSetting.getInstance().setEnvJson(gson.toJson(environmentInfo));
    }

    private void sendRequest(String url) {
        HttpMethod method = (HttpMethod) requestMethod.getSelectedItem();
        if (method == null) {
            method = HttpMethod.GET;
        }
        String head = requestHead.getText();
        if ("".equals(head.trim())) {
            head = "{}";
        }
        //noinspection unchecked
        Map<String, Object> headers = (Map<String, Object>) JsonUtil.formatMap(replaceVariable(head));
        if (headers == null) {
            // 选择Header页面
            tabs.select(headTab, true);
            Notify.getInstance(project).error("Incorrect request header format!");
            return;
        }

        responseView.setText(null);

        String body = replaceVariable(requestBody.getText());
        HttpRequest request = HttpUtils.newHttpRequest(method, replaceVariable(url), headers, body);
        HttpUtils.run(
                request,
                response -> {
                    final FileType fileType = HttpUtils.parseFileType(response);
                    final String responseBody = response.body();
                    ApplicationManager.getApplication().invokeLater(
                            () -> {
                                responseView.setText(responseBody, fileType);
                                execScript0(responseBody, response.headers());
                            }
                    );
                    String now = LocalDateTime.now().toString();
                    StringBuilder logSb = new StringBuilder();
                    logSb.append("======Request ").append(now).append("======").append("\n");
                    logSb.append("----General----").append("\n");
                    logSb.append("Request URL: ").append(request.getUrl()).append("\n");
                    logSb.append("Request Method: ").append(request.getMethod().name()).append("\n");
                    try {
                        logSb.append("Status Code: ").append(request.getConnection().responseCode()).append("\n");
                    } catch (IOException ignored) {
                    }
//                    logSb.append("Remote Address: " + "");
//                    logSb.append("Referrer Policy: " + "");

                    logSb.append("----Request Headers----").append("\n");
                    for(Map.Entry<String, List<String>> entry : request.headers().entrySet()) {
                        logSb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    logSb.append("----Response Headers----").append("\n");
                    for(Map.Entry<String, List<String>> entry : response.headers().entrySet()) {
                        logSb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    logSb.append("----Request Body----").append("\n");
                    logSb.append(body).append("\n");
                    logSb.append("----Response Body----").append("\n");
                    logSb.append(response.body()).append("\n");
                    logSb.append("=====Request ").append(now).append("=====").append("\n");
                    ToolWindowService.getInstance(project).getConsoleView().print(logSb.toString(), ConsoleViewContentType.NORMAL_OUTPUT);
                    ToolWindowService.getInstance(project).getConsoleView().requestScrollingToEnd();
                },
                e -> {
                    final String response = String.format("%s", e);
                    ApplicationManager.getApplication().invokeLater(
                            () -> responseView.setText(response, CustomEditor.TEXT_FILE_TYPE)
                    );
                }
        );
    }

    private String replaceVariable(String str) {
        EnvironmentInfo environmentInfo = EnvironmentInfo.fromJson(AppSetting.getInstance().getEnvJson());
        if(environmentInfo == null) {
            return str;
        }
        Map<String, Object> variableMap = environmentInfo.get((String) environment.getSelectedItem());

        StringBuilder strBuilder = new StringBuilder(str);
        Pattern pattern = Pattern.compile("\\{\\{(.+?)}}");
        Matcher matcher = pattern.matcher(strBuilder);
        while(matcher.find()) {
            String variable = (String) Optional.ofNullable(variableMap.get(matcher.group(1))).orElse("");
            strBuilder.replace(matcher.start(1) - 2, matcher.end(1) + 2, variable);
            matcher = pattern.matcher(strBuilder);
        }
        return strBuilder.toString();
    }

    @Nullable
    private CustomEditor getCurrentTabbedOfRequest() {
        TabInfo tabInfo = tabs.getSelectedInfo();
        if (tabInfo == null) {
            return null;
        }
        Component component = tabInfo.getComponent();
        if (component instanceof CustomEditor) {
            return (CustomEditor) component;
        }
        return null;
    }

    public void chooseRequest(@Nullable ApiService apiService) {
        this.chooseApiService = apiService;
        this.requestBodyFileType.setSelectedItem(getCacheType());

        Callable<ParseRequest> parseRequestCallable = () -> ParseRequest.wrap(apiService, this);
        Consumer<ParseRequest> parseRequestConsumer = parseRequest -> {

            RequestInfo requestInfo = null;
            if(chooseApiService != null) {
                requestInfo = AppSetting.getInstance().getRequestInfo(chooseApiService.getIdentity());
            }
            if(null != requestInfo) {
                requestMethod.setSelectedItem(requestInfo.getHttpMethod());
                requestUrl.setText(requestInfo.getUrl());
                requestHead.setText(requestInfo.getHead());
                requestBody.setText(requestInfo.getRequestBody());
                responseView.setText(null);
                requestScript.setText(requestInfo.getScript());
                setColor(true);
            } else {
                requestMethod.setSelectedItem(parseRequest.getMethod());
                requestUrl.setText(parseRequest.getUrl());
                requestHead.setText(parseRequest.getHead());
                requestBody.setText(parseRequest.getBody());
                responseView.setText(null);
                requestScript.setText(null);
                setColor(false);
            }

            // 选择Body页面
            tabs.select(bodyTab, false);
        };
        Async.runRead(project, parseRequestCallable, parseRequestConsumer);
    }


    public void setCallback(DetailHandle callback) {
        this.callback = callback;
    }

    public void reset() {
        this.chooseRequest(null);
        refreshEnvironment();
        setColor(false);
    }

    public void refreshEnvironment() {
        environment.removeAllItems();
        EnvironmentInfo environmentInfo = EnvironmentInfo.fromJson(AppSetting.getInstance().getEnvJson());
        String[] items = {"No Environment"};
        if(environmentInfo != null) {
            items = environmentInfo.keySet().toArray(new String[0]);
        }
        for(String item : items) {
            environment.addItem(item);
        }
    }

    @NotNull
    public String getCache(@NotNull String name, @NotNull ApiService apiService) {
        switch (name) {
            case IDENTITY_HEAD:
                return apiService.getHeaders();
            case IDENTITY_BODY:
                String body = bodyCache.getOrDefault(apiService, null);
                if (body == null) {
                    bodyCache.remove(apiService);
                    body = "";
                }
                return body;
            default:
                break;
        }
        return "";
    }

    public void setCache(@NotNull String name, @NotNull ApiService apiService, @NotNull String cache) {
        switch (name) {
            case IDENTITY_HEAD:
                apiService.setHeaders(cache);
                break;
            case IDENTITY_BODY:
                if (cache.equals(bodyCache.get(apiService))) {
                    return;
                }
                bodyCache.put(apiService, cache);
                break;
            default:
                break;
        }
    }

    @NotNull
    public FileType getCacheType() {
        if (chooseApiService == null) {
            return DEFAULT_FILE_TYPE;
        }
        return bodyTextTypeCache.getOrDefault(chooseApiService, CustomEditor.JSON_FILE_TYPE);
    }

    public void setCacheType(@NotNull FileType fileType) {
        if (chooseApiService == null) {
            return;
        }
        bodyTextTypeCache.put(chooseApiService, fileType);
    }

    public void setColor(boolean modified) {
        if(modified) {
            setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, JBColor.ORANGE));
        } else {
            setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, JBColor.BLUE));
        }
    }

    public interface DetailHandle {

        /**
         * 处理逻辑
         */
        void handle();
    }

    protected static class ParseRequest {

        private final HttpMethod method;
        private final String url;
        private final String head;
        private final String body;

        private ParseRequest(HttpMethod method, String url, String head, String body) {
            this.method = method;
            this.url = url;
            this.head = head;
            this.body = body;
        }

        @NotNull
        public static ParseRequest wrap(ApiService apiService, HttpTestPanel detail) {
            HttpMethod selItem = HttpMethod.GET;
            String reqUrl = null;
            String reqHead = null;
            String reqBody = null;
            try {
                if (apiService != null) {
                    reqUrl = apiService.getRequestUrl();

                    selItem = apiService.getMethod() == null || apiService.getMethod() == HttpMethod.REQUEST ?
                            HttpMethod.GET : apiService.getMethod();

                    JSONObject json = new JSONObject(apiService.getModuleHeaders());
                    try {
                        // Global Header
                        String globalHeader = Settings.HttpToolOptionForm.GLOBAL_HEADER.getData();
                        JSONObject parse = JSONUtil.parseObj(globalHeader);
                        if(!parse.isEmpty()) {
                            json.putAll(parse);
                        }

                        String header = apiService.getHeaders();
                         parse = JSONUtil.parseObj(header);
                        if (!parse.isEmpty()) {
                            json.putAll(parse);
                        }
                    } catch (Exception ignore) {
                    }
                    reqHead = json.toStringPretty();

                    if (detail.bodyCache.containsKey(apiService)) {
                        reqBody = detail.getCache(IDENTITY_BODY, apiService);
                    } else {
                        reqBody = ParamsConvert.getParam(apiService.getPsiElement());
                        detail.setCache(IDENTITY_BODY, apiService, reqBody);
                    }
                }
            } catch (PsiInvalidElementAccessException e) {
                /*
                @Throws Code: request.getPsiMethod().getResolveScope()
                @Throws Message: 无效访问，通常代表指向方法已被删除
                 */
                if (detail.callback != null) {
                    detail.callback.handle();
                }
            }

            return new ParseRequest(selItem, reqUrl, reqHead, reqBody);
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public String getHead() {
            return head;
        }

        public String getBody() {
            return body;
        }
    }
}
