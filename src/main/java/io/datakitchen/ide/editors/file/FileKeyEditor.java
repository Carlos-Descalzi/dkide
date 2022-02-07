package io.datakitchen.ide.editors.file;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileKeyEditor extends JPanel implements ClipboardOwner{

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final JTextField filePath = new JTextField();

    private final JTextField sizeRt = new JTextField();
    private final JTextField md5Rt = new JTextField();
    private final JTextField shaRt = new JTextField();

    private final FieldListener fieldListener = new FieldListener(this::notifyChanged);

    private Action copyAction = new SimpleAction(AllIcons.Actions.Copy, "Copy",this::copyKey);
    private Action pasteAction = new SimpleAction(AllIcons.Actions.MenuPaste, "Paste",this::pasteKey);

    private final Project project;

    public FileKeyEditor(Project project) {
        this.project = project;

        FormPanel generalPanel = new FormPanel();
        generalPanel.setBorder(new CompoundBorder(new TitledBorder("General"), JBUI.Borders.empty(10)));
        generalPanel.addField("File Path", filePath, new Dimension(400,28));
        FormPanel runtimeVarsPanel = new FormPanel();
        runtimeVarsPanel.setBorder(new CompoundBorder(new TitledBorder("Runtime variables"), JBUI.Borders.empty(10)));
        runtimeVarsPanel.addField("Size", sizeRt, new Dimension(200,28));
        runtimeVarsPanel.addField("MD5", md5Rt, new Dimension(200,28));
        runtimeVarsPanel.addField("SHA", shaRt, new Dimension(200,28));

        setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyButton = new JButton(copyAction);
        copyButton.setPreferredSize(new Dimension(20,28));
        buttons.add(copyButton);
        JButton pasteButton = new JButton(pasteAction);
        pasteButton.setPreferredSize(new Dimension(20,28));
        buttons.add(pasteButton);
        topPanel.add(buttons, BorderLayout.SOUTH);
        JPanel panel = new JPanel(new GridLayout(2,1));
        panel.add(generalPanel);
        panel.add(runtimeVarsPanel);
        topPanel.add(panel, BorderLayout.CENTER);
        add(topPanel);

        fieldListener.listen(filePath);
        fieldListener.listen(sizeRt);
        fieldListener.listen(md5Rt);
        fieldListener.listen(shaRt);
    }

    private void notifyChanged() {
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public void addDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.removeListener(listener);
    }

    public void loadKey(FileKey currentKey) {
        if (currentKey != null) {
            filePath.setText(StringUtils.defaultString(currentKey.getFilePath(), ""));

            Map<String, Object> runtimeVars = currentKey.getRuntimeVariables();

            sizeRt.setText(StringUtils.defaultString((String) runtimeVars.get("size"), ""));
            md5Rt.setText(StringUtils.defaultString((String) runtimeVars.get("md5"), ""));
            shaRt.setText(StringUtils.defaultString((String) runtimeVars.get("sha"), ""));

            filePath.setEnabled(true);
            sizeRt.setEnabled(true);
            md5Rt.setEnabled(true);
            shaRt.setEnabled(true);
        } else {
            filePath.setText("");
            sizeRt.setText("");
            md5Rt.setText("");
            shaRt.setText("");

            filePath.setEnabled(false);
            sizeRt.setEnabled(false);
            md5Rt.setEnabled(false);
            shaRt.setEnabled(false);
        }
    }

    public void saveKey(FileKey currentKey) {
        if (currentKey != null) {
            currentKey.setFilePath(filePath.getText());

            String sizeRt = this.sizeRt.getText();
            String md5Rt = this.md5Rt.getText();
            String shaRt = this.md5Rt.getText();

            Map<String, Object> runtimeVars = new LinkedHashMap<>();

            if (StringUtils.isNotBlank(sizeRt)) {
                runtimeVars.put("size", sizeRt);
            }
            if (StringUtils.isNotBlank(md5Rt)) {
                runtimeVars.put("md5", md5Rt);
            }
            if (StringUtils.isNotBlank(shaRt)) {
                runtimeVars.put("sha", shaRt);
            }
            currentKey.setRuntimeVariables(runtimeVars);
        }
    }

    private void copyKey(ActionEvent e){
        FileKey key = new FileKey();
        saveKey(key);
        String keyString = JsonUtil.toJsonString(key.toJson());
        Transferable transferable = new BasicTransferable(keyString, null);
        getToolkit().getSystemClipboard().setContents(transferable, this);
    }
    private void pasteKey(ActionEvent e){
        Transferable transferable = getToolkit().getSystemClipboard().getContents(this);
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)){
            try {
                String stringContent = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                Map<String, Object> jsonData = JsonUtil.read(stringContent);
                FileKey key = FileKey.fromJsonData(jsonData);
                loadKey(key);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }
}
