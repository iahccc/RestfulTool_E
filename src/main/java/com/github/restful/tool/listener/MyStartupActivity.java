package com.github.restful.tool.listener;

import com.github.restful.tool.view.window.WindowFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
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
//		ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(WindowFactory.TOOL_WINDOW_ID);
		ToolWindow toolWindow = ToolWindowManager.getInstance(project)
				.registerToolWindow(RegisterToolWindowTask.closable("RestfulTool", AllIcons.Toolwindows.WebToolWindow, ToolWindowAnchor.RIGHT));
		windowFactory.createToolWindowContent(project, toolWindow);
	}
}