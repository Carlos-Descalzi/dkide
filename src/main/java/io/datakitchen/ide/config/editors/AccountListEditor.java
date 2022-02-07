package io.datakitchen.ide.config.editors;

import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountListEditor extends JPanel {

    private final ItemList<Account> list = new ItemList<>(this::createAccount);
    private final AccountEditor accountEditor = new AccountEditor();
    private final Action importFromCliAction = new SimpleAction("Import from CLI",this::importFromCli);
    private Account currentAccount;

    private final DocumentChangeListener listener = e -> saveItem();

    public AccountListEditor(){
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(accountEditor,BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(importFromCliAction));
        centerPanel.add(buttons, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(list, BorderLayout.WEST);
        list.addListSelectionListener(__ ->selectItem());
    }

    private Account createAccount(){
        return new Account("account-"+(list.getDataSize()+1));
    }

    private void disableEvents(){
        accountEditor.removeDocumentChangeListener(listener);
    }

    private void enableEvents(){
        accountEditor.addDocumentChangeListener(listener);
    }

    private void updateActions(){}

    private void saveItem(){
        if (currentAccount != null) {
            accountEditor.saveAccount(currentAccount);
        }
    }

    private void selectItem(){
        disableEvents();
        saveItem();
        currentAccount = list.getSelected();
        accountEditor.setAccount(currentAccount);
        updateActions();
        enableEvents();
    }

    public boolean isValidData(){
        return list.getData().stream().anyMatch(Account::isValid);
    }

    public List<Account> getLoginInfoList() {
        return list.getData().stream().filter(Account::isValid).collect(Collectors.toList());
    }

    public void setLoginInfoList(List<Account> items) {
        if (items == null){
            items = new ArrayList<>();
        }
        list.setData(items);
        currentAccount = null;
        selectItem();
    }

    private void importFromCli(ActionEvent event) {
        File cliContextFolder = new File(System.getenv("HOME"), ".dk");
        List<Account> accounts = new ArrayList<>();
        if (cliContextFolder.exists()){
            for (File item:cliContextFolder.listFiles()){
                if (item.isDirectory()){
                    try {
                        Account account = readAccount(item);
                        if (account != null) {
                            accounts.add(account);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
        List<Account> oldAccounts = list.getData();
        oldAccounts.addAll(accounts);
        list.setData(oldAccounts);
    }

    private Account readAccount(File item) throws Exception{
        File configFile = new File(item,"config.json");
        if (configFile.exists()){
            try (InputStream input = new FileInputStream(configFile)) {
                Map<String, Object> configJson = JsonUtil.read(input);
                String userName = (String)configJson.get("dk-cloud-username");
                String password = (String)configJson.get("dk-cloud-password");
                return new Account(item.getName(), userName, password);
            }
        }
        return null;
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        list.setEnabled(enabled);
        accountEditor.setEnabled(enabled);
        importFromCliAction.setEnabled(enabled);
    }

}
