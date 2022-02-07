package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.RoundedLineBorder;
import io.datakitchen.ide.editors.graph.paste.PasteOptionsDialog;
import io.datakitchen.ide.model.ContainerModelEvent;
import io.datakitchen.ide.ui.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class DefaultContainerView extends ContainerView{
    private final JPanel containerArea = new JPanel();
    private final LabelWithActions imageLabel;
    private final JTextPane commandLabel = new JTextPane();
    private String command;

    public DefaultContainerView(ContainerNodeView nodeView){
        super(nodeView);
        imageLabel = new LabelWithActions("<< container image name >>", JLabel.CENTER,null, this::getImageActions);
        imageLabel.setToolTipText("Double click to edit image name");
        this.nodeView.getModel().addContainerModelListener(this);
        setLayout(new BorderLayout(10,10));
        add(containerArea, BorderLayout.CENTER);

        containerArea.setBorder(new RoundedLineBorder(getForeground(), 10,1));
        commandLabel.setEditorKit(new HTMLEditorKit());
        commandLabel.setEditable(false);

        containerArea.setLayout(new BorderLayout());
        containerArea.add(imageLabel, BorderLayout.NORTH);
        imageLabel.setHighlightOnHover(false);
        imageLabel.setBorder(LineBorder.bottom());
        setBorder(new CompoundBorder(new RoundedLineBorder(getForeground(),5,1), new EmptyBorder(15,15,15,15)));
        JPanel centerPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.CENTER));
        centerPanel.add(commandLabel);
        containerArea.add(centerPanel, BorderLayout.CENTER);
        commandLabel.addMouseListener(new DoubleClickHandler(this::editCommand));
        imageLabel.addMouseListener(new DoubleClickHandler(this::editImageName));
        setCommand(null);
        DropTarget dropTarget = new DropTarget();
        dropTarget.setActive(true);
        try {
            dropTarget.addDropTargetListener(this);
        }catch(Exception ex){}
        commandLabel.setDropTarget(dropTarget);

        String image = nodeView.getModel().getImageName();
        imageLabel.setText(image == null ? "<< container image name >>" : image);
        setCommand(nodeView.getModel().getCommand());
    }

    private Action[] getImageActions(LabelWithActions labelWithActions) {
        return new Action[]{
            new SimpleAction("Choose an image", this::choseImage),
            new SimpleAction("Edit details", this::editDetails),
            new SimpleAction("Paste image from clipboard", this::pasteImage)
        };
    }

    private void pasteImage(ActionEvent event) {
        Transferable content = getToolkit().getSystemClipboard().getContents(this);
        if (content.isDataFlavorSupported(DataFlavor.stringFlavor)){
            try {
                String data = (String)content.getTransferData(DataFlavor.stringFlavor);
                if (data.startsWith("docker pull ")) {
                    String imageName = data.replace("docker pull ", "");
                    nodeView.getModel().setImageName(imageName);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void editDetails(ActionEvent event) {
        ContainerDetailsDialog dialog = new ContainerDetailsDialog(nodeView.getModel().getModule());
        dialog.setDetails(
                nodeView.getModel().getImageName(),
                nodeView.getModel().getUsername(),
                nodeView.getModel().getPassword(),
                nodeView.getModel().getRegistry()
        );
        if (dialog.showAndGet()){
            nodeView.getModel().setImageName(dialog.getImageName());
            nodeView.getModel().setUsername(dialog.getUsername());
            nodeView.getModel().setPassword(dialog.getPassword());
            nodeView.getModel().setRegistry(dialog.getRegistry());
        }
    }

    private void choseImage(ActionEvent event) {
        PasteOptionsDialog dialog = new PasteOptionsDialog(nodeView.getModel().getProject(), null, false, false);

        if (dialog.showAndGet()){
            String image = dialog.getSelectedImageName();
            String command = dialog.getCommand();

            handleImageSettings(null, image, command);
        }
    }

    private void setCommand(String command){
        this.command = command;
        if (StringUtils.isBlank(command)){
            command = "&lt;&lt;Run&gt;&gt;<br/><br/><br/><p style=\"font-size:smaller\">"
                    +"double click to enter command or<br/> drop the script to be executed</p>";
        } else {
            command = command.replace("<","&lt;").replace(">","&gt;");
        }
        commandLabel.setText("<html><center style=\"font-family:monospace;\">"+command+"</center></html>");
    }

    private void editImageName(MouseEvent e) {
        TextFieldInlineEditor editor = new TextFieldInlineEditor(nodeView.getModel().getImageName());
        editor.setPreferredSize(new Dimension(imageLabel.getWidth(),28));
        InlineEditorPopup.edit(imageLabel, editor, (String s)->{
            nodeView.getModel().setImageName(s);
        });
    }

    private void editCommand(MouseEvent e) {
        TextAreaInlineEditor editor = new TextAreaInlineEditor();
        editor.setText(StringUtils.defaultString(command,""));
        editor.setPreferredSize(new Dimension(commandLabel.getWidth(),200));
        InlineEditorPopup.edit(commandLabel, editor, (String s)->{
            nodeView.getModel().setCommand(s);
        });
    }

    @Override
    public void nodePropertyChanged(ContainerModelEvent event) {
        if (event.getPropertyName().equals("imageName")){
            imageLabel.setText(event.getNewValue() == null ? "<< container image name >>" : (String)event.getNewValue());
        } else if (event.getPropertyName().equals("command")){
            setCommand((String)event.getNewValue());
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                for (File file : (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                    PasteOptionsDialog dialog = new PasteOptionsDialog(nodeView.getModel().getProject(), file, false, false);

                    if (dialog.showAndGet()){
                        String image = dialog.getSelectedImageName();
                        String command = dialog.getCommand();

                        handleImageSettings(file, image, command);
                        return;
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void handleImageSettings(File file, String image, String command) {
        nodeView.getModel().setImageName(image);
        if (file != null && StringUtils.isNotBlank(command)){
            nodeView.getModel().setCommand(String.format(command, file.getName()));
        } else {
            nodeView.getModel().setCommand(null);
        }
    }

}
