package io.datakitchen.ide.platform;

import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.module.Site;

import java.util.Map;
import java.util.function.Consumer;

public class OverridesPusher {

    private final Site site;
    private final Account account;
    private final String kitchenName;
    private final boolean overwrite;

    public OverridesPusher(Site site, Account account, String kitchenName, boolean overwrite){
        this.site = site;
        this.account = account;
        this.kitchenName = kitchenName;
        this.overwrite = overwrite;
    }

    public void run(Map<String, Object> overrides, Runnable onFinish, Consumer<Exception> onError){
        new Thread(()->{
            try {
                doPush(overrides);
                if (onFinish != null) {
                    onFinish.run();
                }
            }catch(Exception ex){
                if (onError != null){
                    onError.accept(ex);
                }
            }
        }).start();
    }

    private void doPush(Map<String, Object> overrides) throws Exception {
        ServiceClient client = new ServiceClient(site.getUrl());
        client.login(account.getUsername(), account.getPassword());
        client.setKitchenOverrides(kitchenName,overrides,overwrite);
    }
}
