package io.datakitchen.ide.editors.graph.paste;

import com.github.dockerjava.api.DockerClient;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.docker.DockerRegistrySearchPanel;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class PasteOptionsDialog extends DialogWrapper {

    private static class ScriptLaunchInfo {
        private final String searchTerm;
        private final String command;

        public ScriptLaunchInfo(String searchTerm, String command){
            this.searchTerm = searchTerm;
            this.command = command;
        }
    }
    private final Map<String, ScriptLaunchInfo> scriptInfo = new LinkedHashMap<>();
    private final JRadioButton useGpc = new JRadioButton("Use GPC Image for running this file");
    private final JRadioButton useImage = new JRadioButton("Choose an image from Dockerhub for running this file");
    private DockerRegistrySearchPanel searchPanel;
    private final boolean gpcFirst;
    private final boolean allowGpc;
    private final File file;

    public PasteOptionsDialog(Project project, File file, boolean allowGpc, boolean gpcFirst) {
        super(true);
        this.file = file;
        loadScriptInfo();
        this.gpcFirst = gpcFirst;
        this.allowGpc = allowGpc;
        try {
            DockerClient client = ContainerService.getInstance(project).getClient();
            String searchTerm = getSearchTermForFile(file);
            searchPanel = new DockerRegistrySearchPanel(client, searchTerm);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        init();
    }
    private String getSearchTermForFile(File file) {
        if (file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            ScriptLaunchInfo info = scriptInfo.get(extension);
            if (info != null) {
                return info.searchTerm;
            }
        }
        return null;
    }

    private void loadScriptInfo(){
        try (InputStream input = getClass().getResourceAsStream("/searchTermsByExtension.json")){
            Map<String, Object> items = JsonUtil.read(input);

            for (Map.Entry<String, Object> entry:items.entrySet()){
                Map<String, String> item = ObjectUtil.cast(entry.getValue());
                scriptInfo.put(entry.getKey(), new ScriptLaunchInfo(
                        item.get("search-term"),
                        item.get("command")
                ));
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        ButtonGroup group = new ButtonGroup();
        group.add(useGpc);
        group.add(useImage);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2,1,10,10));
        topPanel.setBorder(JBUI.Borders.emptyBottom(10));
        topPanel.add(useGpc);
        topPanel.add(useImage);

        ActionListener listener = this::optionSelected;
        useGpc.addActionListener(listener);
        useImage.addActionListener(listener);


        if (searchPanel == null){
            panel.add(new JLabel("Docker settings not configured, unable to retrieve image list"), BorderLayout.CENTER);
        } else {
            panel.add(searchPanel, BorderLayout.CENTER);
        }
        if (allowGpc) {
            panel.add(topPanel, BorderLayout.NORTH);
            if (gpcFirst) {
                useGpc.setSelected(true);
            } else {
                useImage.setSelected(true);
            }
        } else {
            useImage.setSelected(true);
        }
        updateState();

        panel.setPreferredSize(new Dimension(800,400));

        return panel;
    }

    private void optionSelected(ActionEvent event) {
        updateState();
    }

    private void updateState(){
        searchPanel.setEnabled(useImage.isSelected());
    }

    public boolean isUseGpc(){
        return useGpc.isSelected();
    }

    public String getSelectedImageName(){
        if (searchPanel != null){
            return searchPanel.getSelectedImageName();
        }
        return null;
    }

    public String getCommand(){
        if (file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            ScriptLaunchInfo info = scriptInfo.get(extension);

            if (info != null) {
                return String.format(info.command, file.getName());
            }
        }
        return null;
    }

}
