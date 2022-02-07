package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SwitchCLIContextDialog extends DialogWrapper {

    private final FormPanel panel = new FormPanel();
    private final ComboBox<String> contexts = new ComboBox<>();
    public SwitchCLIContextDialog(){
        super(true);
        setTitle("Switch CLI Context");
        panel.addField("Context", contexts);
        setup();
        init();
    }

    private void setup() {
        File dkFolder = new File(System.getProperty("user.home"),".dk");
        File[] files = dkFolder.listFiles(File::isDirectory);
        assert files != null;
        List<String> options = Arrays.stream(files).map(File::getName).collect(Collectors.toList());

        contexts.setModel(new DefaultComboBoxModel<>(options.toArray(String[]::new)));

        File contextFile = new File(dkFolder,".context");
        try (FileReader reader = new FileReader(contextFile)) {
            String contextName = IOUtils.toString(reader);
            if (StringUtils.isNotBlank(contextName)){
                contexts.setSelectedItem(contextName.trim());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public void switchContext(){
        File dkFolder = new File(System.getProperty("user.home"),".dk");
        File contextFile = new File(dkFolder,".context");

        String context = (String)contexts.getSelectedItem();

        try (FileWriter writer = new FileWriter(contextFile)){
            IOUtils.write(context, writer);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
