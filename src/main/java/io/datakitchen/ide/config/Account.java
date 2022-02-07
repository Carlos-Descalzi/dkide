package io.datakitchen.ide.config;


import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang3.StringUtils;

public class Account implements NamedObject {

    private static final long serialVersionUID = -1;

    private String name;
    private String username;
    private String password;
    private boolean ingredientAccount;

    public Account(){}

    public Account(String name, String username, String password){
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public Account(String name) {
        this.name = name;
    }

    public String toString(){
        return StringUtils.isBlank(name) ? "(no name)" : (ingredientAccount ? "(I) " : "") + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIngredientAccount() {
        return ingredientAccount;
    }

    public void setIngredientAccount(boolean ingredientAccount) {
        this.ingredientAccount = ingredientAccount;
    }

    public boolean isValid(){
        return StringUtils.isNotBlank(name)
                && StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(password);
    }
}
