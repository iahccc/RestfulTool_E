package com.github.restful.tool.view.components.editor;

import com.github.restful.tool.utils.data.Constants;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.EditorTextField;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.border.Border;

/**
 * @author ZhangYuanSheng
 */
public class CustomEditor extends EditorTextField {

    /**
     * 文本格式
     */
    public static final FileType TEXT_FILE_TYPE = FileTypes.PLAIN_TEXT;
    /**
     * json格式
     */
    public static final FileType JSON_FILE_TYPE = JsonFileType.INSTANCE;
    /**
     * html格式
     */
    public static final FileType HTML_FILE_TYPE = HtmlFileType.INSTANCE;
    /**
     * xml格式
     */
    public static final FileType XML_FILE_TYPE = XmlFileType.INSTANCE;

    private boolean lineNumberShown;
    private boolean horizontalScrollbarVisible;
    private boolean verticalScrollbarVisible;

    public CustomEditor(Project project) {
        this(project, TEXT_FILE_TYPE);
    }

    public CustomEditor(Project project, FileType fileType) {
        this(project, fileType, true, true, true);
    }

    public CustomEditor(Project project, FileType fileType, boolean lineNumberShown, boolean horizontalScrollbarVisible, boolean verticalScrollbarVisible) {
        super(null, project, fileType, false, false);

        this.lineNumberShown = lineNumberShown;
        this.horizontalScrollbarVisible = horizontalScrollbarVisible;
        this.verticalScrollbarVisible = verticalScrollbarVisible;
    }

    public void setupEditor(@NotNull EditorEx editor) {
        EditorSettings settings = editor.getSettings();
        settings.setFoldingOutlineShown(true);
        settings.setIndentGuidesShown(true);
        settings.setLineNumbersShown(lineNumberShown);
        editor.setHorizontalScrollbarVisible(horizontalScrollbarVisible);
        editor.setVerticalScrollbarVisible(verticalScrollbarVisible);
    }

    public void setText(@Nullable final String text, @NotNull final FileType fileType) {
        super.setFileType(fileType);
        Document document = createDocument(text, fileType);
        setDocument(document);
        PsiFile psiFile = PsiDocumentManager.getInstance(getProject()).getPsiFile(document);
        if (psiFile != null) {
            try {
                WriteCommandAction.runWriteCommandAction(
                        getProject(),
                        () -> {
                            CodeStyleManager.getInstance(getProject()).reformat(psiFile);
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setFileType(@NotNull FileType fileType) {
        setNewDocumentAndFileType(fileType, createDocument(getText(), fileType));
    }

    @Override
    protected Document createDocument() {
        return createDocument(null, getFileType());
    }

    private void initOneLineModePre(@NotNull final EditorEx editor) {
//        editor.setOneLineMode(false);
        editor.setColorsScheme(editor.createBoundColorSchemeDelegate(null));
        editor.getSettings().setCaretRowShown(false);
    }

    @NotNull
    @Override
    protected EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        initOneLineModePre(editor);
        setupEditor(editor);
        return editor;
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        super.repaint(tm, x, y, width, height);
        if (getEditor() instanceof EditorEx) {
            initOneLineModePre(((EditorEx) getEditor()));
        }
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(JBUI.Borders.empty());
    }

    public Document createDocument(@Nullable final String text, @NotNull final FileType fileType) {
        final PsiFileFactory factory = PsiFileFactory.getInstance(getProject());
        final long stamp = LocalTimeCounter.currentTime();
        final PsiFile psiFile = factory.createFileFromText(
                Constants.Application.NAME,
                fileType,
                text == null ? "" : text,
                stamp,
                true,
                false
        );
        return PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);
    }
}
