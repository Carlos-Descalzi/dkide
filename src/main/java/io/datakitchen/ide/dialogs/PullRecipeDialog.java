package io.datakitchen.ide.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.GlobalConfiguration;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SimpleAction;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class PullRecipeDialog extends DialogWrapper {

    private final ComboBox<Site> site = new ComboBox<>();
    private final ComboBox<Account> account = new ComboBox<>();
    private final ComboBox<String> kitchen = new ComboBox<>();
    private final ComboBox<String> recipe = new ComboBox<>();
    private ServiceClient client;

    public PullRecipeDialog(Project project) {
        super(true);

        GlobalConfiguration configuration = ConfigurationService.getInstance(project).getGlobalConfiguration();


        site.setModel(new DefaultComboBoxModel<>(configuration.getAllSites().toArray(Site[]::new)));
        account.setModel(new DefaultComboBoxModel<>(configuration.getAccounts().toArray(Account[]::new)));

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        FormPanel panel = new FormPanel();

        panel.addField("Site",site);

        JPanel connectPanel = new JPanel(new BorderLayout());
        connectPanel.add(account, BorderLayout.CENTER);
        connectPanel.add(new JButton(new SimpleAction("Connect",this::connect)), BorderLayout.EAST);

        panel.addField("Account", connectPanel);
        panel.addField("Kitchen", kitchen);
        panel.addField("Recipe", recipe);

        kitchen.addActionListener(this::showRecipes);

        return panel;
    }


    private void connect(ActionEvent event) {
        client = new ServiceClient(site.getItem().getUrl());

        Account account = this.account.getItem();

        kitchen.setModel(new DefaultComboBoxModel<>());
        recipe.setModel(new DefaultComboBoxModel<>());

        new Thread(()->{
            try {
                client.login(account.getUsername(), account.getPassword());
            }catch(Exception ex){
                SwingUtilities.invokeLater(()->
                    Messages.showErrorDialog(ex.getMessage(),"Error")
                );
                return;
            }

            try {
                String[] kitchens = client.getKitchens()
                        .stream().map(k -> (String) k.get("name"))
                        .toArray(String[]::new);

                SwingUtilities.invokeLater(()->
                    PullRecipeDialog.this.kitchen.setModel(new DefaultComboBoxModel<>(kitchens))
                );
            }catch(Exception ex){
                SwingUtilities.invokeLater(()->
                    Messages.showErrorDialog(ex.getMessage(),"Error")
                );
            }
        }).start();
    }

    private void showRecipes(ActionEvent event) {
        recipe.setModel(new DefaultComboBoxModel<>());

        new Thread(()->{
            try {

                List<String> recipes = client.getRecipeNames(kitchen.getItem());

                SwingUtilities.invokeLater(()->
                    PullRecipeDialog.this.recipe.setModel(
                            new DefaultComboBoxModel<>(recipes.toArray(String[]::new))
                    )
                );
            }catch(Exception ex){
                SwingUtilities.invokeLater(()->
                    Messages.showErrorDialog(ex.getMessage(),"Error")
                );
            }

        }).start();
    }

    public Site getSite(){
        return site.getItem();
    }

    public Account getAccount(){
        return account.getItem();
    }

    public String getKitchenName(){
        return kitchen.getItem();
    }

    public String getRecipeName(){
        return recipe.getItem();
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(kitchen.getItem())){
            validations.add(new ValidationInfo("Kitchen is required", kitchen));
        }

        if (StringUtils.isBlank(recipe.getItem())){
            validations.add(new ValidationInfo("Recipe is required", recipe));
        }

        return validations;
    }

}
