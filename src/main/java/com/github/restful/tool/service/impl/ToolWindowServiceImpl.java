package com.github.restful.tool.service.impl;

import com.github.restful.tool.service.ToolWindowService;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ToolWindowServiceImpl implements ToolWindowService {

    private final Project project;
    private Window mainView;
    private ConsoleView consoleView;

    public ToolWindowServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public JComponent getContent() {
        return mainView;
    }

    @Override
    public ConsoleView getConsoleView() {
        return consoleView;
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Window window = new Window(project);
        this.mainView = window;
        Content content = contentFactory.createContent(window, "Main", false);
        toolWindow.getContentManager().addContent(content);

        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        this.consoleView = consoleView;
        Content content1 = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "Console", false);
        toolWindow.getContentManager().addContent(content1);
    }

}
