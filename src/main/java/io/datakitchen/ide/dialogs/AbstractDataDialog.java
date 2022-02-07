package io.datakitchen.ide.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorUtil;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.model.Connector;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.HelpContainer;
import io.datakitchen.ide.ui.RegExValidatedField;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractDataDialog extends DialogWrapper {

    private final RegExValidatedField name = new RegExValidatedField(RegExValidatedField.IDENTIFIER);
    private final ComboBox<DsType> dsType = new ComboBox<>();
    private final ComboBox<Connector> connector = new ComboBox<>();

    protected abstract String getTemplatesFolder();
    protected abstract String getPrefix();

    public AbstractDataDialog(Module module){
        super(true);

        try {
            Map<String,Object> obj = JsonUtil.read(Objects.requireNonNull(
                    getClass().getResource(getTemplatesFolder() + "/types.json")));

            List<DsType> dsTypes = new ArrayList<>();
            dsTypes.add(null);
            dsTypes.addAll(obj.entrySet().stream()
                    .map(e -> new DsType(e.getKey(), (String) e.getValue()))
                    .sorted(Comparator.comparing(DsType::toString)).collect(Collectors.toList()));

            dsType.setModel(new DefaultComboBoxModel<>(dsTypes.toArray(DsType[]::new)));

            List<Connector> connectors = new ArrayList<>();
            connectors.add(null);
            connectors.addAll(ConnectorUtil.getConnectors(new ModuleComponentSource(module)));

            connector.setModel(new DefaultComboBoxModel<>(connectors.toArray(Connector[]::new)));
            connector.setRenderer(new DefaultListCellRenderer(){
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value != null){
                        value = ((Connector)value).getName();
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
        init();
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(name.getText())){
            validations.add(new ValidationInfo("Name is required", name));
        }

        if (dsType.getItem() == null && connector.getItem() == null){
            validations.add(new ValidationInfo("Type or connector are required", dsType));
        }

        return validations;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel();
        panel.addField("Name", name);
        panel.addField("Connector", new HelpContainer(connector, HelpMessages.CONNECTOR_MSG));
        panel.addField(".. or Type", new HelpContainer(dsType, HelpMessages.CONNECTION_MSG),new Dimension(300,28));

        return panel;
    }

    public void writeToFolder(Module module, VirtualFile folder, boolean sink, Consumer<VirtualFile> onFinish) {
        DsType item = dsType.getItem();
        Connector connector = this.connector.getItem();
        String typeName = item == null
            ? getPrefix()+"_"+connector.getConnectorType().getName()
            : item.name;

        ApplicationManager.getApplication().runWriteAction(()->{
            try {

                VirtualFile targetFolder = null;

                if (RecipeUtil.isNodeFolder(module, folder)){
                    String folderName = sink
                            ? RecipeUtil.getDataSinksFolderNameForNode(folder)
                            : RecipeUtil.getDataSourcesFolderNameForNode(folder);
                    if (folderName != null) {
                        targetFolder = folder.findChild(folderName);
                        if (targetFolder == null) {
                            targetFolder = folder.createChildDirectory(this, folderName);
                        }
                    }
                } else {
                    targetFolder = folder;
                }

                if (targetFolder != null) { // should never be null but in case of a broken node
                    Map<String, Object> template = JsonUtil.read(
                            Objects.requireNonNull(
                                    getClass().getResource(getTemplatesFolder() + "/" + typeName + ".json")));

                    VirtualFile dsFile = targetFolder.createChildData(this, name.getText() + ".json");

                    template.put("name", name.getText());

                    if (connector != null) {
                        template.put("config-ref", connector.getName());
                    }

                    JsonUtil.write(template, dsFile);
                    targetFolder.refresh(true, true);
                    if (onFinish != null) {
                        onFinish.accept(dsFile);
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (StringUtils.isBlank(name.getText().trim())){
            return new ValidationInfo("Name is mandatory",name);
        }
        return null;
    }

    private static class DsType {
        private final String name;
        private final String displayName;

        public DsType(String name, String displayName){
            this.name = name;
            this.displayName = displayName;
        }

        public String toString(){
            return displayName;
        }
    }
}
