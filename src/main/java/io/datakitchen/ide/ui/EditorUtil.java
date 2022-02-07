package io.datakitchen.ide.ui;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.psi.SqlFileType;

import java.util.Locale;

public class EditorUtil {
    public static Editor createSqlEditor(Project project){
        Document document = EditorFactory.getInstance().createDocument("");
        return EditorFactory.getInstance().createEditor(document, project, SqlFileType.INSTANCE, false);
    }

    public static Editor createSqlViewer(Project project) {
        Document document = EditorFactory.getInstance().createDocument("");
        return EditorFactory.getInstance().createEditor(document, project, SqlFileType.INSTANCE, true);
    }

    public static Editor createJsonEditor(Project project){
        Document document = EditorFactory.getInstance().createDocument("");
        return EditorFactory.getInstance().createEditor(document, project, JsonFileType.INSTANCE, false);
    }

    public static Editor createJsonViewer(Project project) {
        Document document = EditorFactory.getInstance().createDocument("");
        return EditorFactory.getInstance().createEditor(document, project, JsonFileType.INSTANCE, true);
    }

    public static void setText(Editor overrideSetEditor, String text) {
        ApplicationManager.getApplication().runWriteAction(()->
            overrideSetEditor.getDocument().setText(text)
        );
    }

    public static Editor createViewer(Project project, String fileName) {
        FileType fileType;
        if (fileName.toLowerCase(Locale.ROOT).endsWith(".json")){
            fileType = JsonFileType.INSTANCE;
        } else if (fileName.toLowerCase(Locale.ROOT).endsWith(".sql")){
            fileType = SqlFileType.INSTANCE;
        } else {
            fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
//            if (fileType == null) {
//                fileType = PlainTextFileType.INSTANCE;
//            }
        }
        Document document = EditorFactory.getInstance().createDocument("");
        return EditorFactory.getInstance().createEditor(document, project, fileType, true);
    }

}
