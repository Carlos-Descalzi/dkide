package io.datakitchen.ide.config;

import io.datakitchen.ide.module.Site;

import java.util.ArrayList;
import java.util.List;

public class GlobalConfiguration extends Configuration {
    private static final long serialVersionUID = 1;

    private MiscOptions miscOptions = new MiscOptions();
    private List<Site> sites;

    public MiscOptions getMiscOptions() {
        return miscOptions;
    }

    public void setMiscOptions(MiscOptions miscOptions) {
        this.miscOptions = miscOptions;
    }

    public List<Site> getSites() {
        return sites;
    }

    public List<Site> getAllSites(){
        List<Site> allSites = new ArrayList<>(List.of(Site.DEFAULT_SITES));
        if (this.sites != null){
            allSites.addAll(this.sites);
        }
        return allSites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }
}
