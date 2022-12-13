package com.github.restful.tool.listener;

import com.github.restful.tool.view.window.WindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * MyStartupActivity
 *
 * @author iahc
 * @since 2022/12/13
 */
public class MyStartupActivity implements StartupActivity {
	@Override
	public void runActivity(@NotNull Project project) {
		WindowFactory windowFactory = new WindowFactory();
		ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(WindowFactory.TOOL_WINDOW_ID);
		windowFactory.createToolWindowContent(project, toolWindow);
	}
}