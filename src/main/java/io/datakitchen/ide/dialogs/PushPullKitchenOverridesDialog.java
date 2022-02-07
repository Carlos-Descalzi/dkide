package io.datakitchen.ide.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.HelpContainer;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PushPullKitchenOverridesDialog extends DialogWrapper {
    private final Project project;
    private final FormPanel panel = new FormPanel();
    private final ComboBox<Site> site = new ComboBox<>();
    private final ComboBox<Account> account = new ComboBox<>();
    private final Action connectAction = new SimpleAction("Connect", this::connect);
    private final ComboBox<Map<String,Object>> kitchens = new ComboBox<>();
    private final JCheckBox preserveExisting = new JCheckBox();
    private ServiceClient client;

    public Site getSite(){
        return site.getItem();
    }

    public Account getAccount(){
        return account.getItem();
    }

    public String getKitchenName(){
        return (String)kitchens.getItem().get("name");
    }

    public boolean isPreserveExisting(){
        return preserveExisting.isSelected();
    }

    public PushPullKitchenOverridesDialog(Project project){
        super(true);
        this.project = project;

        List<Site> sites = ConfigurationService.getInstance(project).getGlobalConfiguration().getAllSites();

        panel.addField("Site", new HelpContainer(site, HelpMessages.SITE_HELP_MSG));
        site.setModel(new DefaultComboBoxModel<>(sites.toArray(Site[]::new)));

        List<Account> accounts = ConfigurationService.getInstance(project).getGlobalConfiguration().getAccounts();
        account.setModel(new DefaultComboBoxModel<>(accounts.toArray(Account[]::new)));

        JPanel p = new JPanel(new BorderLayout());
        p.add(new HelpContainer(account, HelpMessages.ACCOUNT_HELP_MSG),BorderLayout.CENTER);
        p.add(new JButton(connectAction), BorderLayout.EAST);
        panel.addField("Account",p);
        panel.addField("Kitchen",kitchens);
        kitchens.setRenderer(new KitchenCellRenderer());
        panel.addField("Preserve existing", preserveExisting);

        init();
    }

    @Override
    protected @NotNull java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (site.getSelectedItem() == null){
            validations.add(new ValidationInfo("Site is required", site));
        }

        if (account.getSelectedItem() == null){
            validations.add(new ValidationInfo("Account type is required", account));
        }

        if (kitchens.getSelectedItem() == null){
            validations.add(new ValidationInfo("Kitchen type is required", kitchens));
        }

        return validations;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private void connect(ActionEvent e){
        this.connectAction.setEnabled(false);
        ApplicationManager.getApplication().invokeLater(()->{
            client = new ServiceClient(site.getItem().getUrl());
            Account info = account.getItem();
            try {
                client.login(info.getUsername(), info.getPassword());
                this.kitchens.setModel(new DefaultComboBoxModel<>(
                    client.getKitchens().toArray(Map[]::new)
                ));
            }catch (Exception ex){
                ex.printStackTrace();
            }
            connectAction.setEnabled(true);
        });
    }

    @SuppressWarnings("unchecked")
    public void pullOverrides(){
        // TODO move to another place
        ApplicationManager.getApplication().invokeLater(()->{
            try {
                Map<String, Object> kitchen = kitchens.getItem();
                VirtualFile file = RecipeUtil.getLocalOverridesFile(project);
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        Map<String, Object> kitchenOverrides = (Map<String, Object>)kitchen.get("recipeoverrides");

                        if (preserveExisting.isSelected()){
                            Map<String, Object> overrides = JsonUtil.read(file);
                            kitchenOverrides.putAll(overrides);
                        }

                        JsonUtil.write(kitchenOverrides, file);
                        file.refresh(true, true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    private static class KitchenCellRenderer extends DefaultListCellRenderer {
        @Override
        @SuppressWarnings("unchecked")
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null){
                Map<String,Object> kitchenVal = (Map<String,Object>) value;
                value = kitchenVal.get("name");
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
