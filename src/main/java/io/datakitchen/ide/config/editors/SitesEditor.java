package io.datakitchen.ide.config.editors;

import com.intellij.openapi.diagnostic.Logger;
import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.ui.HelpContainer;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SitesEditor extends JPanel {

    private static final Logger LOGGER = Logger.getInstance(SitesEditor.class);

    private final ItemList<Site> items = new ItemList<>(this::createSite);
    private final SiteEditor siteEditor = new SiteEditor();
    private final Action importFromCliAction = new SimpleAction("Import from CLI", this::importFromCli);
    private final DocumentChangeListener listener = e -> saveItem();
    private Site currentSite;

    public SitesEditor() {
        setLayout(new BorderLayout());
        add(items, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(siteEditor, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new HelpContainer(new JButton(importFromCliAction), HelpMessages.IMPORT_SITES_HELP_MSG));
        centerPanel.add(buttons, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        items.addListSelectionListener(__ ->selectItem());
        siteEditor.setSite(null);
    }

    private Site createSite(){
        return new Site("site-"+(items.getDataSize()+1));
    }

    private void selectItem() {
        disableEvents();
        saveItem();
        currentSite = items.getSelected();
        siteEditor.setSite(currentSite);
        updateActions();
        enableEvents();
    }

    private void saveItem() {
        if (currentSite != null) {
            siteEditor.saveSite(currentSite);
        }
    }

    private void disableEvents(){
        siteEditor.removeDocumentChangeListener(listener);
    }

    private void enableEvents(){
        siteEditor.addDocumentChangeListener(listener);
    }

    private void updateActions(){}


    private void importFromCli(ActionEvent event) {
        List<Site> sites = readSites();
        Map<String, Site> sitesByUrl = new LinkedHashMap<>();

        for (Site site: items.getData()){
            sitesByUrl.put(site.getUrl(),site);
        }

        for (Site site: sites){
            if (!sitesByUrl.containsKey(site.getUrl())){
                sitesByUrl.put(site.getUrl(), site);
            }
        }

        for (Site site: Site.DEFAULT_SITES){
            sitesByUrl.remove(site.getUrl());
        }

        items.setData(new ArrayList<>(sitesByUrl.values()));
    }

    @NotNull
    private List<Site> readSites() {
        File cliContextFolder = new File(System.getenv("HOME"), ".dk");
        List<Site> sites = new ArrayList<>();
        if (cliContextFolder.exists()){
            for (File item:cliContextFolder.listFiles()){
                if (item.isDirectory()){
                    try {
                        Site site = readSite(item);
                        if (site != null) {
                            sites.add(site);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
        return sites;
    }

    private Site readSite(File item) throws Exception{
        File configFile = new File(item,"config.json");
        if (configFile.exists()){
            try (InputStream input = new FileInputStream(configFile)) {
                try {
                    Map<String, Object> configJson = JsonUtil.read(input);
                    String host = (String) configJson.get("dk-cloud-ip");
                    String port = String.valueOf(configJson.get("dk-cloud-port"));
                    return new Site(item.getName(), makeHost(host, port));
                }catch(MalformedURLException ex){
                    LOGGER.error(ex);
                }
            }
        }
        return null;
    }

    private String makeHost(String host, String port) throws MalformedURLException {
        URL url = new URL(host);
        return new URL(url.getProtocol(), url.getHost(), Integer.parseInt(port), url.getFile()) +"/";
    }

    public void setSites(List<Site> sites) {
        items.setData(sites == null ? new ArrayList<>() : new ArrayList<>(sites));
    }

    public List<Site> getSites() {
        return items.getData();
    }
}
