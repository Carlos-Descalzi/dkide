package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.model.ScriptNodeKey;
import io.datakitchen.ide.ui.LabelWithActions;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptNodeKeyView extends JPanel implements PropertyChangeListener {

    private final ScriptNodeKey key;
    private final LabelWithActions title;
    private final JTextPane detail = new JTextPane();
    private ContainerNodeView nodeView;

    public ScriptNodeKeyView(ContainerNodeView nodeView, ScriptNodeKey key, LabelWithActions.ActionSupplier<LabelWithActions> supplier){
        this.nodeView = nodeView;
        this.key = key;
        this.key.addPropertyChangeListener(this);
        setLayout(new BorderLayout());
        title = new LabelWithActions("",null, supplier);
        title.setDoubleClickAction(new SimpleAction("", this::openFile));
        add(title, BorderLayout.NORTH);
        add(detail, BorderLayout.CENTER);
        detail.setEditable(false);

        setBorder(new CompoundBorder(JBUI.Borders.empty(2),
                new CompoundBorder(new LineBorder(getBackground().brighter()), JBUI.Borders.empty(2))));

        updateView();
        addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateView();
            }
        });

    }

    private void openFile(ActionEvent event) {
        VirtualFile file = nodeView.getModel().getFile(key.getScript());

        if (file != null) {
            FileEditorManager.getInstance(nodeView.getProject()).openFile(file, true);
        }
    }

    public ScriptNodeKey getKey() {
        return key;
    }

    private void updateView() {
        Container parent = getParent();
        String title ="Run script "+key.getScript();
        if (parent != null){
            int index = Arrays.asList(parent.getComponents()).indexOf(this);
            title = (index+1)+" - "+title;
        }
        this.title.setText(title);

        List<String> detail = new ArrayList<>();

        if (!key.getParameters().isEmpty()) {
            detail.add("Parameters:\n"
                    + key.getParameters().entrySet().stream()
                            .map(e -> "\t" + e.getKey() + ":" + e.getValue() + "\n")
                            .collect(Collectors.joining()));
        }
        if (key.getEnvironment() != null && !key.getEnvironment().isEmpty()){
            detail.add("Environment variables:\n"
                    + key.getEnvironment().entrySet().stream()
                            .map(e -> "\t" + e.getKey() + ":" + e.getValue() + "\n")
                            .collect(Collectors.joining()));
        }

        this.detail.setText(String.join("\n",detail));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateView();
    }
}
