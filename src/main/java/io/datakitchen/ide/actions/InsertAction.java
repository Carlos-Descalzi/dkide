package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.util.RecipeUtil;
import io.datakitchen.ide.ui.FiltrableList;
import io.datakitchen.ide.ui.SimpleAction;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InsertAction extends AnAction {

    private VirtualFile file;
    private Editor editor;
    private Module module;
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        file = e.getData(LangDataKeys.VIRTUAL_FILE);
        module = e.getData(LangDataKeys.MODULE);
        editor = e.getData(LangDataKeys.EDITOR);

        if (editor != null) {
            Point p = editor.getContentComponent().getMousePosition();
            JPopupMenu popup = new JPopupMenu();
            popup.add(createAction("Insert Secret", KeyEvent.VK_S, this::insertSecret));
            popup.add(createAction("Insert Variable", KeyEvent.VK_V, this::insertVariable));
            popup.add(createAction("Insert Test", KeyEvent.VK_T, this::insertTest));
            popup.add(createAction("Insert File Key", KeyEvent.VK_F, this::insertFileKey));
            popup.add(createAction("Insert SQL Key", KeyEvent.VK_Q, this::insertSqlKey));
            popup.show(editor.getContentComponent(), p.x, p.y);
        }
    }

    private Action createAction(String text, int s, ActionListener listener) {
        Action action = new SimpleAction(text, listener);
        action.putValue(Action.MNEMONIC_KEY,s);
        return action;
    }

    private void insertSecret(ActionEvent e){
        List<String> secrets = ConfigurationService
                .getInstance(module.getProject())
                .getSecrets()
                .stream()
                .map(Secret::toString)
                .collect(Collectors.toList());

        runInsert(secrets, option ->
            editor.getDocument().insertString(editor.getCaretModel().getOffset(),"#{vault://"+option+"}")
        );
    }
    private void insertVariable(ActionEvent e){
        List<String> allVariables = RecipeUtil.getPlainVariables(module);

        runInsert(allVariables, option->
            editor.getDocument().insertString(editor.getCaretModel().getOffset(),"{{"+option+"}}")
        );
    }
    private void runInsert(List<String> options, Consumer<String> action){

        FiltrableList list = new FiltrableList();
        list.setList(options);
        list.setPreferredSize(new Dimension(300, 200));

        Point p = MouseInfo.getPointerInfo().getLocation();

        JBPopup popup = JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(list, list.getFilterField())
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
        list.addActionListener(e->{
            WriteCommandAction.runWriteCommandAction(module.getProject(),()->
                action.accept(list.getValue())
            );
            popup.cancel();
        });
        popup.showInScreenCoordinates(editor.getContentComponent(), p);
    }

    private void insertFileKey(ActionEvent e){
        insertTemplate("/templates/filekey.json");
    }
    private void insertTest(ActionEvent e){
        insertTemplate("/templates/test.json");
    }
    private void insertSqlKey(ActionEvent e){
        insertTemplate("/templates/sqlkey.json");
    }

    private void insertTemplate(String templateName){
        try {
            String template = IOUtils.toString(
                    new InputStreamReader(
                            Objects.requireNonNull(getClass().getResourceAsStream(templateName))));

            WriteCommandAction.runWriteCommandAction(module.getProject(), () ->
                editor.getDocument().insertString(editor.getCaretModel().getOffset(), template)
            );
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
