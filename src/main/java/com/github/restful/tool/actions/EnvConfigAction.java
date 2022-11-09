package com.github.restful.tool.actions;

import com.github.restful.tool.configuration.AppSetting;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.view.components.editor.CustomEditor;
import com.github.restful.tool.view.window.WindowFactory;
import com.github.restful.tool.view.window.frame.Window;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


/**
 * EnvConfigAction
 *
 * @author iahc
 * @since 2022/11/8
 */
public class EnvConfigAction extends DumbAwareAction {

    public EnvConfigAction() {
        getTemplatePresentation().setText(Bundle.getString("action.envConfig.text"));
        getTemplatePresentation().setIcon(AllIcons.Actions.Colors);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        CustomEditor editor =  new CustomEditor(project, CustomEditor.JSON_FILE_TYPE);
        String envJson = AppSetting.getInstance().getEnvJson();
        editor.setText(envJson);

        JBPopupFactory instance = JBPopupFactory.getInstance();
        instance.createComponentPopupBuilder(new JScrollPane(editor), new JBLabel())
                .setTitle("Environment Config")
                .setMovable(true)
                .setResizable(true)
                .setMayBeParent(true)
                .setNormalWindowLevel(true)
                .setRequestFocus(true)
                .setMinSize(new Dimension(300, 100))
                .createPopup()
                .showInFocusCenter();

        editor.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                DocumentListener.super.documentChanged(event);

                String text = event.getDocument().getText();
                AppSetting.getInstance().setEnvJson(text);

//                Window window = WindowFactory.getToolWindow(project);
//                if(window != null) {
//                    window.refresh();
//                }
            }
        });

    }
}
