package io.datakitchen.ide.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.SearchItem;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.ui.ListListModel;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes","unchecked"})
public class DockerRegistrySearchPanel extends JPanel {

    private final JPanel imagesPanel = new JPanel(new BorderLayout());
    private final JPanel tagsPanel = new JPanel(new BorderLayout());
    private final JPanel selectedImagePanel = new JPanel(new BorderLayout());
    private final JTextField keywords = new JTextField();
    private final Action search = new SimpleAction("Search", this::search);
    private final JBList imageList = new JBList();
    private final JBList tagsList = new JBList();
    private final DockerClient client;

    public DockerRegistrySearchPanel(DockerClient client){
        this(client, null);
    }
    public DockerRegistrySearchPanel(DockerClient client, String searchTerms){
        this.client = client;
        setLayout(new BorderLayout());

        buildImagesPanel();
        buildTagsPanel();
        add(imagesPanel, BorderLayout.CENTER);

        if (searchTerms != null) {
            keywords.setText(searchTerms);
            search(null);
        }
    }

    private void buildTagsPanel() {
        tagsPanel.add(selectedImagePanel, BorderLayout.NORTH);
        tagsPanel.add(new JBScrollPane(tagsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JButton(new SimpleAction("<< Back", this::backToImageList)));
        tagsPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void buildImagesPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Keywords"), BorderLayout.WEST);
        topPanel.add(keywords, BorderLayout.CENTER);
        topPanel.add(new JButton(this.search),BorderLayout.EAST);
        keywords.setAction(search);

        imagesPanel.add(topPanel, BorderLayout.NORTH);
        imagesPanel.add(new JBScrollPane(imageList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        imageList.setEmptyText("Searching images ...");
        imageList.setCellRenderer(new SearchItemCellRenderer());
        imageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (imageList.getSelectedValue() != null){
                    showTags();
                }
            }
        });

    }

    private void backToImageList(ActionEvent event) {
        removeAll();
        add(imagesPanel, BorderLayout.CENTER);
        revalidate();
    }

    private void showTags() {
        selectedImagePanel.removeAll();
        SearchItem item = (SearchItem) imageList.getSelectedValue();
        selectedImagePanel.add(new SearchItemView(item),BorderLayout.CENTER);
        tagsList.setEmptyText("Searching tags for "+item.getName()+" ...");
        searchTags(item.getName(), tags ->
            SwingUtilities.invokeLater(()->
                tagsList.setModel(new ListListModel(tags))
            )
        );
        removeAll();
        add(tagsPanel, BorderLayout.CENTER);
        revalidate();
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        keywords.setEnabled(enabled);
        search.setEnabled(enabled);
        imageList.setEnabled(enabled);
    }

    private void search(ActionEvent event) {
        new Thread(()-> {
            setSearching(true);
            List<SearchItem> items = client
                    .searchImagesCmd(keywords.getText())
                    .withLimit(30)
                    .exec();
            SwingUtilities.invokeLater(()-> showItems(items));
            setSearching(false);
        }).start();
    }

    private void searchTags(String imageName, Consumer<List<String>> onFinish){
        AuthConfig authConfig = client.authConfig();
        Header header = new BasicHeader(
            "Authorization",
            "Basic "+authConfig.getUsername()+":"+authConfig.getPassword()
        );
//        String registryAddress = client.authConfig().getRegistryAddress();
//        if (StringUtils.isBlank(registryAddress)){
//            registryAddress = "https://registry-1.docker.io/v2/";
//        } else if (registryAddress.contains("/v1/")){
////            registryAddress.replace("/v1/","/v2/");
//        }
        String registryAddress = "https://registry.hub.docker.com/v1/"; // TODO: fix this

        String url = String.format("%srepositories/%s/tags", registryAddress, imageName);

        new Thread(()->{
            try {
                HttpGet get = new HttpGet(url);
                get.addHeader(header);
                HttpClient client = HttpClientBuilder.create().build();
                HttpResponse response = client.execute(get);

                List<Map<String, String>> jsonData = JsonUtil.read(getContent(response));

                List<String> tags = jsonData.stream().map(m -> m.get("name")).collect(Collectors.toList());
                onFinish.accept(tags);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }).start();
        // listUri="https://registry-1.docker.io/v2/$item/tags/list"
//        String searchQuery = "https://registry.hub.docker.com/api/v1/repositories/"+imageName+"/tags?page=1&page_size=25";
        //        curl
    }
    private String getContent(HttpResponse response) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(new InputStreamReader(response.getEntity().getContent()), writer);
        return writer.toString().strip();
    }


    private void showItems(List<SearchItem> items) {

        items.sort((i1, i2)->{
            Integer v1 = (i1.getStarCount() == null ? 0 : i1.getStarCount()) * 10 + (i1.isOfficial() ? 1 : 0);
            Integer v2 = (i2.getStarCount() == null ? 0 : i2.getStarCount()) * 10 + (i2.isOfficial() ? 1 : 0);

            return v2 - v1;
        });

        this.imageList.setModel(new ListListModel(items));
    }

    private void setSearching(boolean searching){
        SwingUtilities.invokeLater(()->{
            keywords.setEnabled(isEnabled() && !searching);
            search.setEnabled(isEnabled() && !searching);
        });
    }

    public String getSelectedImageName() {
        SearchItem item = (SearchItem) imageList.getSelectedValue();
        if (item != null){
            String tag = (String)tagsList.getSelectedValue();
            if (StringUtils.isNotBlank(tag)){
                return item.getName()+":"+tag;
            }
            return item.getName();
        }
        return null;
    }
}
