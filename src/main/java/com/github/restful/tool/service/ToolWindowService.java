package com.github.restful.tool.service;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface ToolWindowService {

    /**
     * getInstance
     *
     * @param project project
     * @return obj
     */
    static ToolWindowService getInstance(@NotNull Project project) {
        return project.getService(ToolWindowService.class);
    }

    /**
     * get view content
     *
     * @return ContentView
     */
    JComponent getContent();

    /**
     * get console view
     *
     * @return ConsoleView
     */
    ConsoleView getConsoleView();

    /**
     * init window
     *
     * @param toolWindow toolWindow
     */
    void init(@NotNull ToolWindow toolWindow);
}
