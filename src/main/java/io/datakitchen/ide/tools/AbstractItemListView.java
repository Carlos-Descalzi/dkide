package io.datakitchen.ide.tools;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.NumberUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public abstract class AbstractItemListView extends JPanel {

    private static final Logger LOGGER = Logger.getInstance(AbstractItemListView.class);

    protected final Project project;
    protected JPanel contentPane = new JPanel(new BorderLayout());
    protected JList<File> itemList = new JBList<>();

    public AbstractItemListView(Project project){
        this.project = project;

        setLayout(new BorderLayout());
        JScrollPane scroll = new JBScrollPane(itemList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(200,200));
        JPanel p = new JPanel(new BorderLayout());
        p.add(scroll, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new SimpleAction("Refresh list",this::refreshList)));
        buttons.add(new JButton(new SimpleAction("Open", this::openFile)));
        buttons.add(new JButton(new SimpleAction("Open Folder", this::openFolder)));
        p.add(buttons,BorderLayout.SOUTH);
        add(p,BorderLayout.WEST);
        add(contentPane, BorderLayout.CENTER);
        itemList.addListSelectionListener( e -> showItem());
        itemList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                File fileValue = (File)value;
                if (fileValue.getName().endsWith(".output")){
                    value = fileValue.getName().replace(".output","")
                            + " ("+ NumberUtil.formatInBytes(fileValue.length())+")";
                } else if (fileValue.getName().equals("runtime-vars.json")){
                    value = "Runtime variables";
                } else if (fileValue.getName().equals("compiled.json")){
                    value = "Compiled file";
                } else if (fileValue.getName().equals("progress.json")){
                    value = "Progress";
                } else {
                    value = fileValue.getName()
                            + " ("+ NumberUtil.formatInBytes(fileValue.length())+")";
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

    }

    private void openFolder(ActionEvent event) {
        File file = itemList.getSelectedValue();
        try {
            new ProcessBuilder(getLauncherCommand(), file.getParentFile().getAbsolutePath()).start();
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    private void openFile(ActionEvent event) {
        File file = itemList.getSelectedValue();
        try {
            new ProcessBuilder(getLauncherCommand(), file.getAbsolutePath()).start();
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    @NotNull
    private String getLauncherCommand() {
        // TODO: Move this code to another place
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")){
            return "start";
        }

        if (osName.contains("mac")){
            return "open";
        }

        return "xdg-open";
    }

    private void refreshList(ActionEvent event) {
        refresh();
    }

    public abstract void refresh();
    protected abstract void showItem();

}
