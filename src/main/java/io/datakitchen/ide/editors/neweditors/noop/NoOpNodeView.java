package io.datakitchen.ide.editors.neweditors.noop;

import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.editors.neweditors.NodeTestsView;
import io.datakitchen.ide.editors.neweditors.TestEditorDialog;
import io.datakitchen.ide.model.NoOpNodeModel;
import io.datakitchen.ide.model.NodeModelListener;
import io.datakitchen.ide.ui.LineBorder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class NoOpNodeView extends JPanel implements NodeModelListener {
    private final JEditorPane description = new JEditorPane();

    private final NoOpNodeModel model;

    public NoOpNodeView(NoOpNodeModel model){
        this.model = model;
        setLayout(new BorderLayout());

        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(100,100));
        JPanel descriptionArea = new JPanel(new BorderLayout());
        descriptionArea.add(scroll, BorderLayout.CENTER);
        descriptionArea.setPreferredSize(new Dimension(600,300));

        NodeTestsView tests = new NodeTestsView(model, this::onAddTest);

        JPanel content = new JPanel(new BorderLayout());
        content.add(descriptionArea, BorderLayout.NORTH);
        content.add(tests, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(content, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        descriptionArea.setBorder(new TitledBorder(LineBorder.top(), "Description"));
        tests.setBorder(new TitledBorder(LineBorder.top(),"Tests"));

        description.setText(StringUtils.defaultString(model.getDescription(),""));
        description.getDocument().putProperty(PlainDocument.tabSizeAttribute,4);
        model.addNodeModelListener(this);

        description.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                model.setDescription(description.getText());
            }
        });
    }

    private void onAddTest() {
        TestEditorDialog dialog = new TestEditorDialog(model.getModule(), model.getNodeName());
        if (dialog.showAndGet()){
            model.addTest(dialog.createTest());
        }
    }

}
