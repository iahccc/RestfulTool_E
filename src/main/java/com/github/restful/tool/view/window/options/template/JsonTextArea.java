/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: TextInput
  Author:   ZhangYuanSheng
  Date:     2020/9/1 20:21
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.view.window.options.template;

import com.github.restful.tool.beans.settings.SettingKey;
import com.github.restful.tool.beans.settings.Settings;
import com.github.restful.tool.view.components.editor.CustomEditor;
import com.github.restful.tool.view.window.options.Option;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;

/**
 * TextArea
 *
 * @author iahc
 * @since 2022/11/25
 */
public class JsonTextArea<T> extends JPanel implements Option {

    public final SettingKey<T> key;
    private final Integer topInset;

    private final CustomEditor textField;

    private final Verify<T> verify;

    public JsonTextArea(@Nullable T defaultValue, @NotNull SettingKey<T> key, Verify<T> verify, boolean labelOnTop) {
        this(key.getName(), defaultValue, key, null, verify, labelOnTop);
    }

    public JsonTextArea(@NotNull String label, @Nullable T defaultValue, @NotNull SettingKey<T> key, Integer topInset, Verify<T> verify, boolean labelOnTop) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.key = key;
        this.topInset = topInset;
        this.textField = new CustomEditor(ProjectManager.getInstance().getDefaultProject(), CustomEditor.JSON_FILE_TYPE);
        this.verify = verify;

        this.add(
                FormBuilder.createFormBuilder()
                        .addLabeledComponent(label, textField, labelOnTop)
                        .getPanel()
        );

        initInput();
    }

    @Override
    public void showSetting(@NotNull Settings setting) {
        this.textField.setText(toString(setting.getData(this.key)));
    }

    @Override
    public void applySetting(@NotNull Settings setting) {
        T value = fromString(textField.getText());
        if (value == null) {
            return;
        }
        if (verify != null && !verify.check(value)) {
            return;
        }
        setting.putData(this.key, value);
    }

    /**
     * T -> String
     *
     * @param data data
     * @return String
     */
    @Nullable
    protected String toString(T data) {
        return (String) data;
    }

    /**
     * String -> T
     *
     * @param data data
     * @return T
     */
    @Nullable
    protected T fromString(String data) {
        return (T) data;
    }

    @Nullable
    @Override
    public Integer getTopInset() {
        return this.topInset;
    }

    protected void appendInputVerify(@NotNull KeyAdapter adapter) {
        this.textField.addKeyListener(adapter);
    }

    protected CustomEditor getInput() {
        return this.textField;
    }

    public interface Verify<T> {

        /**
         * 验证内容
         *
         * @param data 验证数据
         * @return bool
         */
        boolean check(@Nullable T data);
    }

    private void initInput() {
        getInput().setPreferredSize(new Dimension(500, 250));
        getInput().setMinimumSize(new Dimension(500, 250));
    }

}
