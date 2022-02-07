package io.datakitchen.ide.platform;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.LoginException;
import io.datakitchen.ide.util.ObjectUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceClient {

    private static final Set<String> TEXT_EXTENSIONS = Set.of("sql","json","txt","html","md","csv","xml","yaml","iml","py","java","sh","r","rb");
    private static final Set<String> SKIP_FILES = Set.of(".runsettings.propertes"); // TODO check others

    private final String url;
    private final HttpClient client;
    private String token;

    public ServiceClient(String url){
        this.url = url;
        client = HttpClientBuilder.create().build();
    }

    public boolean isLoggedIn(){
        return token != null;
    }

    public String getToken(){
        return token;
    }

    public void login(String username, String password) throws IOException, LoginException {
        HttpPost post = new HttpPost(url+"v2/login");
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == 200) {
            token = getContent(response);
        } else {
            throw new LoginException("Invalid user/password");
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<Map<String,Object>> getKitchens() throws IOException, ParseException {
        HttpGet get = new HttpGet(url + "v2/kitchen/list");
        get.setHeader(new BasicHeader("authorization", "Bearer " + token));

        HttpResponse response = client.execute(get);

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);
        return (List<Map<String,Object>>)obj.get("kitchens");
    }

    private Map<String, Object> getKitchen(String kitchenName) throws IOException, ParseException {
        HttpGet get = new HttpGet(url + "v2/kitchen/"+kitchenName);
        get.setHeader(new BasicHeader("authorization", "Bearer " + token));

        HttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() != 200){
            throw new RuntimeException(response.getStatusLine().getReasonPhrase());
        }

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);

        return ObjectUtil.cast(obj.get("kitchen"));
    }

    private void updateKitchen(String kitchenName, Map<String, Object> kitchenData) throws IOException {
        HttpPost post = new HttpPost(url+"v2/kitchen/update/"+kitchenName);

        post.setHeader(new BasicHeader("authorization", "Bearer " + token));
        post.setHeader(new BasicHeader("Content-type", "application/json"));
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("kitchen.json",kitchenData);
        post.setEntity(new StringEntity(JsonUtil.toJsonString(content)));

        HttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() != 200){
            throw new RuntimeException(response.getStatusLine().getReasonPhrase());
        }
    }

    public void setKitchenOverrides(String kitchenName, Map<String, Object> overrides, boolean overwrite) throws IOException, ParseException{

        Map<String, Object> kitchenData = getKitchen(kitchenName);

        kitchenData.remove("_hid");

        Map<String, Object> existingOverrides = ObjectUtil.cast(kitchenData.get("recipeoverrides"));

        if (!overwrite){
            for (Map.Entry<String, Object> entry: overrides.entrySet()){
                if (!existingOverrides.containsKey(entry.getKey())){
                    existingOverrides.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            existingOverrides.putAll(overrides);
        }

        updateKitchen(kitchenName, kitchenData);
    }

    public List<Order> getOrders(String kitchenName, int orderCount) throws IOException, ParseException{
        HttpGet get = new HttpGet(url+"v2/order/status/"+kitchenName+"?start=0&count="+orderCount+"&servingCount=3&structuredResponse=true");
        get.setHeader(new BasicHeader("authorization", "Bearer " + token));

        HttpResponse response = client.execute(get);

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);
        List<Map<String, Object>> orders = ObjectUtil.cast(obj.get("orders"));
        return orders.stream().map(Order::new).collect(Collectors.toList());
    }

    private String getContent(HttpResponse response) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(new InputStreamReader(response.getEntity().getContent()), writer);
        return writer.toString().strip();
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getRecipeNames(String kitchenName) throws IOException, ParseException{
        HttpGet get = new HttpGet(url+"v2/kitchen/recipenames/"+kitchenName);
        get.setHeader(new BasicHeader("authorization", "Bearer " + token));


        HttpResponse response = client.execute(get);

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);
        return (List<String>)obj.get("recipes");
    }

    @SuppressWarnings({"unchecked"})
    public List<Node> getNodes(String kitchenName, String recipeName) throws IOException, ParseException{
        HttpPost post = new HttpPost(url+"v2/recipe/get/"+kitchenName+"/"+recipeName);
        post.setHeader(new BasicHeader("authorization", "Bearer " + token));

        post.setEntity(new StringEntity("{\"include-recipe-tree\": true}", ContentType.APPLICATION_JSON));

        HttpResponse response = client.execute(post);

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);
        Map<String,Object> recipeTree = (Map<String, Object>) obj.get("recipe-tree");
        Map<String,Object> contents = (Map<String,Object>)recipeTree.get(recipeName);

        Map<String, Node> nodes = new HashMap<>();

        for (Map.Entry<String,Object> entry:contents.entrySet()){
            String folder = entry.getKey();
            if (folder.equals(recipeName)){
                continue;
            }
            if (folder.equals(recipeName+"/resources")){
                continue;
            }
            String[] tokens = folder.split("/");

            String nodeName = tokens[1];

            Node node = nodes.get(nodeName);
            if (node == null){
                node = new Node(kitchenName, recipeName, nodeName);
                nodes.put(nodeName, node);
            }

            List<Map<String,Object>> items = (List<Map<String,Object>>)entry.getValue();

            for (Map<String, Object> item: items){
                String filename = (String)item.get("filename");


                if (tokens.length >= 2){
                    filename = StringUtils.join(tokens,"/",1,tokens.length) + "/"+filename;
                    System.out.println("Node file:"+filename);
                    node.files.add(filename);
                } else {
                    System.out.println("Discarded "+StringUtils.join(tokens,"/",1,tokens.length) + "/"+filename);
                }
            }


        }
        return nodes.values()
            .stream()
            .sorted(Comparator.comparing(Node::toString))
            .collect(Collectors.toList());
    }

    private HttpResponse execute(HttpUriRequest request) throws IOException {
        request.setHeader(new BasicHeader("authorization", "Bearer " + token));
        return client.execute(request);
    }

    public void pushRecipe(String kitchenName, VirtualFile recipeFolder, boolean removeRemote) throws IOException, ParseException {
        HttpResponse response = execute(new HttpGet(url+"v2/recipe/tree/"+kitchenName+"/"+recipeFolder.getName()));

        if (response.getStatusLine().getStatusCode() != 200){
            LOGGER.error("Error getting recipe tree: "+response.getStatusLine());
            throw new IOException(response.getStatusLine().getReasonPhrase());
        }

        Map<String, Object> content = JsonUtil.read(getContent(response));
        Map<String, Object> recipes = ObjectUtil.cast(content.get("recipes"));

        if (!recipes.containsKey(recipeFolder.getName())){
            // create the recipe
            response = execute(new HttpPost(url+"v2/recipe/create/"+kitchenName+"/"+recipeFolder.getName()));

            if (response.getStatusLine().getStatusCode() != 200){
                LOGGER.error("Error creating recipe"+response.getStatusLine());
                throw new IOException(response.getStatusLine().getReasonPhrase());
            } else {
                System.out.println("Recipe created");
            }

            // and now get the recipe again.
            for (int i=0;i<3;i++) {
                System.out.println("Getting recipe, attempt "+(i+1));
                response = execute(new HttpGet(url + "v2/recipe/tree/" + kitchenName + "/" + recipeFolder.getName()));

                if (response.getStatusLine().getStatusCode() != 200) {
                    LOGGER.error("Error getting recipe tree: " + response.getStatusLine());
                    if (i == 2) {
                        throw new IOException(response.getStatusLine().getReasonPhrase());
                    } else {
                        try {
                            Thread.sleep(1000);
                        }catch (Exception ignored){}
                        continue;
                    }
                }

                content = JsonUtil.read(getContent(response));
                recipes = ObjectUtil.cast(content.get("recipes"));
                System.out.println("Got recipe");
                break;
            }
        } else {
            LOGGER.info("Recipe already exists");
        }
        Map<String, Object> recipeJson = ObjectUtil.cast(recipes.get(recipeFolder.getName()));
        Map<String, Object> files = createRecipePayload(recipeFolder, recipeJson, removeRemote);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Recipe update "+recipeFolder.getName());
        payload.put("skipCompile", true);
        payload.put("files", files);

        String stringPayload = JsonUtil.toJsonString(payload);
        System.out.println(stringPayload);
//        System.out.println(JsonUtil.read(stringPayload));

        HttpPost post = new HttpPost(url+"v2/recipe/update/"+kitchenName+"/"+recipeFolder.getName());

        post.setHeader(new BasicHeader("Content-Type",ContentType.APPLICATION_JSON.getMimeType()));
        post.setEntity(new StringEntity(stringPayload, ContentType.APPLICATION_JSON));

        response = execute(post);

        if (response.getStatusLine().getStatusCode() != 200){
            String message = getMessage(response);
            LOGGER.error("Error uploading recipe "+message);
            throw new IOException(message);
        }
    }

    private HttpClient getClient(){
        return HttpClientBuilder.create().build();
    }

    private String getMessage(HttpResponse response) throws IOException, ParseException {
        if (response.getEntity().getContentType().getValue().equals("application/json")){
            Map<String, Object> jsonData = JsonUtil.read(getContent(response));
            if (jsonData.containsKey("message")){
                Map<String, Object> message = ObjectUtil.cast(jsonData.get("message"));
                if (message.containsKey("error")){
                    return (String)message.get("error");
                }
            }
        }
        return response.getStatusLine().getReasonPhrase();
    }

    private Map<String, Object> createRecipePayload(VirtualFile recipeFolder, Map<String, Object> recipeJson, boolean removeRemote) throws IOException{
        List<VirtualFile> files = listFilesRecursively(recipeFolder);
        Map<String, Object> result = new LinkedHashMap<>();

        for (VirtualFile vFile: files){

            String path = vFile.getPath().substring(recipeFolder.getPath().length()+1);

            if (!skipFile(path)) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("contents", encodeFile(vFile));
                data.put("isNew", !pathExists(path, recipeFolder.getName(), recipeJson));
                result.put(path, data);
            }
        }

        if (removeRemote) {
            Set<String> remotePaths = makeRemotePaths(recipeJson)
                    .stream().map(s -> s.substring(recipeFolder.getName().length()+1))
                    .collect(Collectors.toSet());

            for (String path : remotePaths) {
                if (!result.containsKey(path)) {
                    result.put(path, new LinkedHashMap<>());
                }
            }
        }

        return result;
    }

    private Set<String> makeRemotePaths(Map<String, Object> recipeJson) {
        Set<String> paths = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry: recipeJson.entrySet()){
            List<Map<String,Object>> content = ObjectUtil.cast(entry.getValue());
            for (Map<String, Object> item: content) {
                paths.add(entry.getKey() + "/" + item.get("filename"));
            }
        }
        return paths;
    }

    private boolean pathExists(String path, String recipeName, Map<String, Object> recipeJson) {
        path = recipeName+"/"+path;
        String fileName = FilenameUtils.getName(path);
        String dirName = FilenameUtils.getPath(path);
        dirName = dirName.substring(0,dirName.length()-1);

        List<Map<String, Object>> dirEntries = ObjectUtil.cast(recipeJson.get(dirName));

        if (dirEntries != null){
            return dirEntries.stream().anyMatch(e -> e.get("filename").equals(fileName));
        }

        return false;
    }

    private boolean skipFile(String name){
        return SKIP_FILES.contains(name);
    }

    private Object encodeFile(VirtualFile vFile) throws IOException{
        String extension = vFile.getName();
        extension = extension.substring(extension.lastIndexOf(".")+1).toLowerCase();

        if (isTextFile(extension)){
            try (InputStream input = vFile.getInputStream()) {
                return IOUtils.toString(input, Charset.defaultCharset());
            }
        }
        try (InputStream input = vFile.getInputStream()) {
            byte[] byteArray = IOUtils.toByteArray(input);
            return Base64.encodeBase64String(byteArray);
        }
    }

    private boolean isTextFile(String extension) {
        return TEXT_EXTENSIONS.contains(extension);
    }

    private List<VirtualFile> listFilesRecursively(VirtualFile folder) {
        List<VirtualFile> files = new ArrayList<>();
        VfsUtilCore.visitChildrenRecursively(folder, new VirtualFileVisitor<Object>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()){
                    files.add(file);
                }
                return true;
            }
        });

        return files;
    }

    public void pullNode(Node node, VirtualFile folder, Project project) throws IOException, ParseException{
        HttpPost post = new HttpPost(url+"v2/recipe/get/"+node.kitchen+"/"+node.recipe);

        Map<String,Object> msg = new LinkedHashMap<>();
        msg.put("recipe-files",node.files);
        String strMessage = JsonUtil.toJsonString(msg);

        post.setEntity(new StringEntity(strMessage, ContentType.APPLICATION_JSON));

        HttpResponse response = execute(post);

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);
        Map<String, Object> recipes = ObjectUtil.cast(obj.get("recipes"));
        Map<String, Object> recipe = ObjectUtil.cast(recipes.get(node.recipe));

        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                for (Map.Entry<String, Object> entry : recipe.entrySet()) {
                    String path = entry.getKey();
                    VirtualFile targetFolder = makePath(folder, path);
                    List<Map<String, Object>> val = ObjectUtil.cast(entry.getValue());
                    for (Map<String, Object> fileObj : val) {
                        save(fileObj, targetFolder);
                    }
                }
                ProjectView.getInstance(project).refresh();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });

    }

    private void save(Map<String, Object> fileObj, VirtualFile targetFolder) throws IOException{
        String filename = (String)fileObj.get("filename");

        String content = (String)fileObj.get("json");

        if (content == null){
            content = (String)fileObj.get("text");
        }
        VirtualFile file = targetFolder.createChildData(this,filename);
        System.out.println("Writing file:"+file);
        try (Writer writer = new OutputStreamWriter(file.getOutputStream(this))){
            IOUtils.write(content, writer);
            writer.flush();
        }
    }

    private VirtualFile makePath(VirtualFile folder, String path) throws IOException{
        String[] tokens = path.split("/");

        for (int i=1;i<tokens.length;i++){
            VirtualFile child = folder.findChild(tokens[i]);
            if (child == null){
                child = folder.createChildDirectory(this,tokens[i]);
            }
            folder = child;
        }
        return folder;
    }


    public static class Node {
        private String kitchen;
        private String recipe;
        private String name;
        private final List<String> files = new ArrayList<>();

        public Node(){}

        public String toString(){
            return name;
        }

        public Node(String kitchen, String recipe, String name){
            this.kitchen = kitchen;
            this.recipe = recipe;
            this.name = name;
        }

    }

    private static final Logger LOGGER = Logger.getInstance(ServiceClient.class);

    public void pullRecipe(String kitchen, String recipe, VirtualFile targetFolder) throws IOException, ParseException {

        HttpResponse response = execute(new HttpGet(url + "v2/recipe/get/"+kitchen+"/"+recipe));

        String content = getContent(response);
        Map<String,Object> obj = JsonUtil.read(content);

        Map<String, Object> recipesData = ObjectUtil.cast(obj.get("recipes"));
        Map<String,Object> recipeObject = ObjectUtil.cast(recipesData.get(recipe));

        ApplicationManager.getApplication().invokeLater(()->
            ApplicationManager.getApplication().runWriteAction(()->{
                for (String path: recipeObject.keySet()){

                    try {
                        List<Map<String, Object>> folderFiles = ObjectUtil.cast(recipeObject.get(path));

                        path = path.replace(recipe, "");

                        File folder = new File(targetFolder.getPath(), path).getCanonicalFile();

                        if (!folder.exists()) {
                            if (!folder.mkdirs()) {
                                LOGGER.error("pullRecipe: Unable to make dirs " + folder.getPath());
                                continue;
                            }
                        }

                        for (Map<String, Object> folderFile : folderFiles) {
                            try {
                                writeFile(folderFile, folder);
                            } catch (Exception ex) {
                                LOGGER.error(ex);
                            }
                        }
                    }catch (IOException ex){
                        LOGGER.error(ex);
                    }
                }
            })
        );




    }

    private void writeFile(Map<String,Object> jsonObject, File moduleFolder) throws IOException {

        File targetFile = new File(moduleFolder, (String)jsonObject.get("filename"));
        FileWriter writer = new FileWriter(targetFile);

        if (jsonObject.containsKey("json")){
            String jsonString = (String)jsonObject.get("json");
            writer.write(jsonString);
        } else if (jsonObject.containsKey("text")){
            String textString = (String)jsonObject.get("text");
            writer.write(textString);
        }

        writer.flush();

    }

}
