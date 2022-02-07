package io.datakitchen.ide.module;

import io.datakitchen.ide.config.Account;

public class PullData {
    private final String url;
    private final Account account;
    private final String token;
    private final String kitchen;
    private final String recipeName;

    public PullData(String url, Account account, String token, String kitchen, String recipe) {
        this.recipeName = recipe;
        this.url = url;
        this.token = token;
        this.kitchen = kitchen;
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getKitchen() {
        return kitchen;
    }

    public String getRecipeName() {
        return recipeName;
    }

}
