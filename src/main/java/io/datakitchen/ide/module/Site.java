package io.datakitchen.ide.module;

import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Site implements NamedObject {
    public static final Site PRODUCTION = new Site("Production","https://cloud.datakitchen.io:443/");
    public static final Site STAGING = new Site("Staging","https://staging.datakitchen.io:443/");
    public static final Site LOCALDEV = new Site("Local dev","http://192.168.50.101:80/");

    public static final Site[] DEFAULT_SITES = {PRODUCTION, STAGING, LOCALDEV};

    private String description;
    private String url;

    public Site(String name) {
        this.description = name;
    }

    public static Site siteByUrl(String url) {
        for (Site site: DEFAULT_SITES){
            if (site.url.equals(url)){
                return site;
            }
        }
        return null;
    }

    public boolean equals(Object other){
        return other instanceof Site
                && new EqualsBuilder()
                .append(description, ((Site) other).description)
                .append(url, ((Site) other).url)
                .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
                .append(description)
                .append(url)
                .toHashCode();
    }

    public String toString() {
        return description;
    }

    public Site(){}

    public Site(String description, String url) {
        this.description = description;
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getName() {
        return description;
    }

    @Override
    public void setName(String name) {
        this.description = name;
    }
}
