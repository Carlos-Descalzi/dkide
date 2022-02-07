package io.datakitchen.ide.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.module.PullData;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class RecipeUtil {

    private static final Logger LOGGER = Logger.getInstance(RecipeUtil.class);

    public static Key<Boolean> CUSTOM_EDITOR_ENABLED = new Key<>("CustomEditorEnabled");

    public static final String NODE_TYPE_NOOP = "DKNode_NoOp";
    public static final String NODE_TYPE_ACTION = "DKNode_Action";
    public static final String NODE_TYPE_DATA_MAPPER = "DKNode_DataMapper";
    public static final String NODE_TYPE_CONTAINER = "DKNode_Container";
    public static final String NODE_TYPE_INGREDIENT = "DKNode_Ingredient";
    public static final String NODE_TYPE_CONTAINER_SUBTYPE_SCRIPT = "DKNode_Container.script";

    public static final Collection<String> NODES_WITH_DATA_SOURCE = Set.of(NODE_TYPE_ACTION, NODE_TYPE_CONTAINER, NODE_TYPE_DATA_MAPPER);
    public static final Collection<String> NODES_WITH_DATA_SINK = Set.of(NODE_TYPE_CONTAINER, NODE_TYPE_DATA_MAPPER);

    private static final Set<String>SCRIPT_NODE_NAMES = Set.of("dk_general_purpose_container", "{{gpcConfig.image_repo}}");

    public static boolean isCustomEditorEnabled(){
        Boolean enabled = ApplicationManager.getApplication().getUserData(CUSTOM_EDITOR_ENABLED);
        return enabled == null || enabled;
    }

    public static VirtualFile recipeFolder(Project project, VirtualFile aFile){
        return recipeFolder(ModuleUtil.findModuleForFile(aFile, project));
    }

    public static VirtualFile recipeFolder(Module module){
        if (module == null){
            return null;
        }
        VirtualFile moduleFile = module.getModuleFile();

        if (moduleFile != null){
            return moduleFile.getParent();
        }
        return null;
    }

    public static boolean isNodeFolder(Module module, VirtualFile currentFile) {
        return currentFile.getParent().equals(recipeFolder(module))
                && currentFile.findChild(Constants.FILE_DESCRIPTION_JSON) != null
                && currentFile.findChild(Constants.FILE_NOTEBOOK_JSON) != null;
    }

    public static boolean isNodeFolder(File file) {
        if (file.isDirectory()){

            Set<String> contents = Arrays
                .stream(Objects.requireNonNull(file.listFiles()))
                .map(File::getName)
                .collect(Collectors.toSet());

            return contents.contains(Constants.FILE_NOTEBOOK_JSON)
                && contents.contains(Constants.FILE_DESCRIPTION_JSON);
        }
        return false;
    }

    public static boolean isDataSourceFolder(Module module, VirtualFile currentFile) {
        return (currentFile.getName().equals(Constants.FOLDER_DATA_SOURCES)
                || currentFile.getName().equals(Constants.FOLDER_ACTIONS))
                && isNodeFolder(module, currentFile.getParent());
    }

    public static boolean isDataSinkFolder(Module module, VirtualFile currentFile) {
        return currentFile.getName().equals(Constants.FOLDER_DATA_SINKS)
                && isNodeFolder(module, currentFile.getParent());
    }

    public static List<String> getRecipeNodes(VirtualFile recipeFolder){
        return Arrays.stream(recipeFolder.getChildren())
                .filter((VirtualFile f)-> f.findChild(Constants.FILE_DESCRIPTION_JSON) != null)
                .map(VirtualFile::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getVariations(Module module) {
        VirtualFile recipeFolder = recipeFolder(module);
        VirtualFile variationsFile = recipeFolder.findChild(Constants.FILE_VARIATIONS_JSON);
        if (variationsFile != null) {
            try {
                Map<String, Object> variationsObj = JsonUtil.read(variationsFile);
                return new ArrayList<>(((Map<String, Object>) variationsObj.get("variation-list")).keySet());
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
        return List.of();
    }

    public static VirtualFile getResourcesFolder(Module module) throws IOException {
        VirtualFile recipeFolder = recipeFolder(module);
        VirtualFile resourcesFolder = recipeFolder.findChild(Constants.FOLDER_RESOURCES);

        if (resourcesFolder != null){
            return resourcesFolder;
        }

        ThrowableComputable<VirtualFile, IOException> action = ()->
            recipeFolder.createChildDirectory(module,Constants.FOLDER_RESOURCES);

        return ApplicationManager.getApplication().runWriteAction(action);
    }

    public static void createLocalOverridesFile(String path, ModifiableRootModel model) throws IOException {
        VirtualFile projectFolder = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Path.of(path));

        assert projectFolder != null;

        ThrowableComputable<VirtualFile, IOException> action = ()-> {
            VirtualFile localOverrides = projectFolder.findChild("local-overrides.json");
            if (localOverrides == null){

                localOverrides = projectFolder.createChildData(projectFolder, "local-overrides.json");
                try (OutputStream out = localOverrides.getOutputStream(projectFolder);
                     InputStream in =RecipeUtil.class.getResourceAsStream("/templates/local-overrides.json")
                ){
                    assert in != null;
                    IOUtils.copy(in, out);
                    out.flush();
                }
            }
            return localOverrides;
        };


        VirtualFile localOverrides = ApplicationManager.getApplication().runWriteAction(action);
        if (model != null) {
            model.addContentEntry(localOverrides);
        }
    }


    private static void writeFile(Map<String,Object> jsonObject, File moduleFolder) throws IOException {

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

    public static void pullRecipe(@NotNull Project project, Module module, @Nullable ModifiableModuleModel model, PullData pullRecipeData){

        VirtualFile moduleFolder = VirtualFileManager.getInstance().findFileByNioPath(
                module.getModuleNioFile().getParent());

        ApplicationManager.getApplication().invokeLater(()->{
            try {


                HttpClient client = HttpClientBuilder.create().build();

                HttpGet get = new HttpGet(pullRecipeData.getUrl() + "v2/recipe/get/"+pullRecipeData.getKitchen()+"/"+pullRecipeData.getRecipeName());
                get.setHeader(new BasicHeader("authorization", "Bearer " + pullRecipeData.getToken()));

                HttpResponse response = client.execute(get);

                String content = getContent(response);
                Map<String,Object> obj = JsonUtil.read(content);

                Map<String,Object> recipeObject = (Map<String,Object>) ((Map<String,Object>)obj.get("recipes")).get(pullRecipeData.getRecipeName());

                for (String path: recipeObject.keySet()){
                    List<Map<String,Object>> folderFiles = (List<Map<String,Object>>) recipeObject.get(path);

                    path = path.replace(pullRecipeData.getRecipeName(), "");
                    assert moduleFolder != null;
                    File folder = new File(moduleFolder.getPath(),path).getCanonicalFile();

                    if (!folder.exists()){
                        if (!folder.mkdirs()){
                            LOGGER.error("pullRecipe: Unable to make dirs "+folder.getPath());
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
                }
            }catch (Exception ex){
                LOGGER.error(ex);
            }
        }, ModalityState.NON_MODAL);


    }

    private static String getContent(HttpResponse response) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(new InputStreamReader(response.getEntity().getContent()), writer);
        return writer.toString().strip();
    }

    public static @NotNull VirtualFile getProjectSettingsFolder(Project project){
        VirtualFile workspaceFile = project.getWorkspaceFile();
        assert workspaceFile != null;
        return workspaceFile.getParent();
    }

    public static @NotNull VirtualFile getProjectFolder(Project project){
        return getProjectSettingsFolder(project).getParent();
    }

    public static VirtualFile getLocalOverridesFile(Project project) {
        return getProjectFolder(project).findChild("local-overrides.json");
    }

    private static final Map<String,String> cachedActiveVariations = new HashMap<>();

    public static String getActiveVariation(Module module) {

        if (module != null) {
            try {
                String cachedActiveVariation = cachedActiveVariations.get(module.getName());

                if (cachedActiveVariation == null) {
                    cachedActiveVariation = readActiveVariation(module);

                    if (cachedActiveVariation != null) {
                        cachedActiveVariations.put(module.getName(), cachedActiveVariation);
                    }
                }

                return cachedActiveVariation;
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
        return null;
    }

    private static String readActiveVariation(Module module) {

        VirtualFile recipeFolder = recipeFolder(module);

        if (recipeFolder != null) {
            VirtualFile runSettingsFile = recipeFolder.findChild(".runsettings.propertes");

            if (runSettingsFile != null) {
                Properties prop = new Properties();
                try {
                    prop.load(runSettingsFile.getInputStream());
                } catch (Exception ex) {
                    LOGGER.error(ex);
                    return null;
                }
                return prop.getProperty("activeVariation");
            }
        }
        return null;
    }

    public static void setActiveVariation(Module module, String variationName){
        cachedActiveVariations.put(module.getName(),variationName);
        ApplicationManager.getApplication().runWriteAction(()->{
            VirtualFile runSettingsFile = recipeFolder(module).findChild(".runsettings.propertes");
            try {
                if (runSettingsFile == null) {
                    runSettingsFile = recipeFolder(module).createChildData(module, ".runsettings.propertes");
                }
                Properties prop = new Properties();
                prop.setProperty("activeVariation",variationName);

                try (OutputStream out = runSettingsFile.getOutputStream(module)){
                    prop.store(out,"");
                }
            }catch(Exception ex){
                LOGGER.error(ex);
            }
        });
    }

    public static VirtualFile createTempFolder(Module module) throws IOException{
        ThrowableComputable<VirtualFile, IOException> action = ()->
            Objects.requireNonNull(VirtualFileManager
                            .getInstance()
                            .findFileByNioPath(Path.of(System.getProperty("java.io.tmpdir"))))
                    .createChildDirectory(module, "data_"+System.currentTimeMillis());

        return ApplicationManager.getApplication().runWriteAction(action);
    }

    public static boolean isScriptNode(VirtualFile nodeFolder){
        if (nodeFolder != null) {
            VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);
            if (notebookFile != null) {
                try {
                    Map<String, Object> obj = JsonUtil.read(notebookFile);
                    String imageRepo = (String) obj.get("image-repo");
                    if (imageRepo != null && SCRIPT_NODE_NAMES.contains(imageRepo)) {
                        return true;
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex);
                }
            }
        }
        return false;
    }

    private static Map<String, Object> getAllVariables(Module module){
        Map<String, Object> allVariables = new LinkedHashMap<>();

        VirtualFile recipeRoot = recipeFolder(module);

        if (recipeRoot != null) {
            try {
                Map<String, Object> variableList = JsonUtil.read(
                        Objects.requireNonNull(recipeRoot.findChild(Constants.FILE_VARIABLES_JSON)));
                Map<String, Object> variables = (Map<String, Object>) variableList.get("variable-list");
                if (variables != null) {
                    allVariables.putAll(variables);
                }
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
            try {
                String activeVariation = getActiveVariation(module);

                if (activeVariation != null){
                    Map<String, Object> variationsJson = JsonUtil.read(Objects.requireNonNull(
                            recipeRoot.findChild(Constants.FILE_VARIATIONS_JSON)));
                    Map<String, Object> allOverrides = (Map<String, Object>) variationsJson.get("override-setting-list");
                    Map<String, Object> variations = (Map<String, Object>)variationsJson.get("variation-list");
                    Map<String, Object> variation = (Map<String, Object>)variations.get(activeVariation);

                    List<String> overrideSettings;

                    Object overrideSetting = variation.get("override-setting");
                    if (overrideSetting instanceof String){
                        overrideSettings = List.of((String) overrideSetting);
                    } else {
                        overrideSettings = (List<String>)overrideSetting;
                    }

                    if (overrideSettings != null) {
                        for (String overrideName : overrideSettings) {
                            allVariables.putAll((Map<String, Object>)allOverrides.get(overrideName));
                        }
                    }
                }

            } catch (Exception ex){
                LOGGER.error(ex);
            }
        } else {
            LOGGER.error("getAllVariables: recipe root was null");
        }


        try {
            Map<String, Object> overrides = JsonUtil.read(
                    Objects.requireNonNull(getProjectFolder(module.getProject()).findChild("local-overrides.json")));
            allVariables.putAll(overrides);
        } catch (Exception ex){
            LOGGER.error(ex);
        }

        return allVariables;
    }

    public static List<String> getPlainVariables(Module module) {
        Map<String, Object> allVariables = getAllVariables(module);
        return explode(allVariables).stream().sorted().collect(Collectors.toList());
    }

    private static List<String> explode(Map<String, Object> variables) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry: variables.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map){
                list.addAll(explode((Map<String, Object>)value).stream().map((String s)-> key + "."+s).collect(Collectors.toList()));
            } else {
                list.add(key);
            }
        }
        return list;
    }

    public static List<String> getVariableNames(Module module) {
        Map<String, Object> allVariables = getAllVariables(module);
        return new ArrayList<>(allVariables.keySet()).stream().sorted().collect(Collectors.toList());
    }

    public static Set<String> getActiveGraphNodes(Module module) {
        Set<String> nodes = new HashSet<>();
        String activeVariation = getActiveVariation(module);

        if (activeVariation != null) {
            try {
                VirtualFile recipeRoot = recipeFolder(module);

                Map<String, Object> variationsJson = JsonUtil.read(Objects.requireNonNull(
                        recipeRoot.findChild(Constants.FILE_VARIATIONS_JSON)));
                Map<String, Object> variations = (Map<String, Object>) variationsJson.get("variation-list");
                Map<String, Object> variation = (Map<String, Object>) variations.get(activeVariation);

                if (variation != null) {
                    String graphName = (String) variation.get("graph-setting");

                    if (StringUtils.isNotBlank(graphName)) {
                        Map<String, Object> graphs = (Map<String, Object>) variationsJson.get("graph-setting-list");
                        List<List<String>> graph = (List<List<String>>) graphs.get(graphName);

                        if (graph != null) {
                            for (List<String> edge : graph) {
                                nodes.addAll(edge);
                            }
                        }
                    }
                }
            }catch (ParseException ex){
                LOGGER.error("Unable to parse variations.json: "+ex.getMessage());
            }catch(Exception ex){
                LOGGER.error(ex);
            }
        }

        return nodes;
    }

    public static String getDataSourcesFolderNameForNode(VirtualFile folder) throws IOException, ParseException {
        String nodeType = getNodeType(folder);
        if (!NODES_WITH_DATA_SOURCE.contains(nodeType)){
            return null;
        }

        return NODE_TYPE_ACTION.equals(nodeType)
                ? Constants.FOLDER_ACTIONS
                : Constants.FOLDER_DATA_SOURCES;
    }


    public static String getDataSinksFolderNameForNode(VirtualFile folder)  throws IOException, ParseException{
        String nodeType = getNodeType(folder);
        if (!NODES_WITH_DATA_SINK.contains(nodeType)){
            return null;
        }
        return Constants.FOLDER_DATA_SINKS;
    }
    public static String getNodeType(VirtualFile folder) throws IOException, ParseException{
        Map<String, Object> descriptionJson = JsonUtil.read(Objects.requireNonNull(folder.findChild(Constants.FILE_DESCRIPTION_JSON)));

        return (String)descriptionJson.get("type");
    }

    public static Map<String, Object> loadAllVariables(Module module){
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.putAll(loadVariables(module));
        variables.putAll(loadOverrides(module));
        variables.putAll(loadLocalOverrides(module));
        return variables;
    }

    private static Map<String,Object> loadLocalOverrides(Module module) {
        try {
            VirtualFile localOverridesFile = RecipeUtil.getLocalOverridesFile(module.getProject());

            if (localOverridesFile != null) {
                return JsonUtil.read(localOverridesFile);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return Map.of();
    }

    private static Map<String, Object> loadVariables(Module module) {
        try {
            VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
            VirtualFile variablesJson = recipeFolder.findChild(Constants.FILE_VARIABLES_JSON);
            if (variablesJson != null) {
                Map<String, Object> content = JsonUtil.read(variablesJson);
                return (Map<String, Object>) content.get("variable-list");
            }
            LOGGER.error("variables.json not found in recipe "+module.getName());
        }catch (Exception ex){
            LOGGER.error(ex);
        }
        return Map.of();
    }

    private static Map<String, Object> loadOverrides(Module module){
        try {
            VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
            VirtualFile variationsJson = recipeFolder.findChild(Constants.FILE_VARIATIONS_JSON);
            assert variationsJson != null;
            Map<String, Object> content = JsonUtil.read(variationsJson);
            String activeVariation = RecipeUtil.getActiveVariation(module);
            if (activeVariation != null) {
                Map<String, Object> variationList = (Map<String, Object>) content.get("variation-list");
                if (variationList != null) {
                    Map<String, Object> variation = (Map<String, Object>) variationList.get(activeVariation);

                    List<String> overrideSettingNames;
                    if (variation != null) {
                        Object overrideSettings = variation.get("override-setting");
                        if (overrideSettings instanceof String) {
                            overrideSettingNames = List.of((String) overrideSettings);
                        } else if (overrideSettings instanceof List) {
                            overrideSettingNames = (List<String>) overrideSettings;
                        } else {
                            overrideSettingNames = new ArrayList<>();
                        }
                    } else {
                        overrideSettingNames = List.of();
                    }

                    Map<String, Object> overrideSets = (Map<String, Object>) content.get("override-setting-list");

                    Map<String, Object> allOverrides = new LinkedHashMap<>();

                    for (String overrideName: overrideSettingNames){
                        allOverrides.putAll((Map<String,Object>)overrideSets.getOrDefault(overrideName,new LinkedHashMap<>()));
                    }

                    return allOverrides;
                }
            }
        }catch(Exception ex){
            LOGGER.error(ex);
        }
        return Map.of();
    }

    public static boolean isFileInRecipe(Module module, File file) throws IOException{
        VirtualFile resourcesFolder = getResourcesFolder(module);

        VirtualFile aFile = VfsUtil.findFileByURL(file.toURI().toURL());
        if (aFile != null) {
            return aFile.getPath().startsWith(resourcesFolder.getPath());
        }
        return false;
    }

    public static void copyToResourcesFolder(Module module, File file) throws IOException{
        VirtualFile resourcesFolder = getResourcesFolder(module);

        VirtualFile aFile = VfsUtil.findFileByURL(file.toURI().toURL());

        if (aFile != null) {
            ThrowableComputable<VirtualFile, IOException> action = () -> VfsUtil.copyFile(module, aFile, resourcesFolder);

            ApplicationManager.getApplication().runWriteAction(action);
        } else {
            LOGGER.error("copyToResourcesFolder: aFile was null");
        }
    }

    public static boolean isRecipeFolder(Module module, VirtualFile file) {
        VirtualFile recipeFolder = recipeFolder(module);
        return recipeFolder != null
                && recipeFolder.equals(file);
    }

    public static boolean isRecipeFolder(String path){
        File file = new File(path);

        if (!file.exists()){
            return false;
        }

        File[] children = file.listFiles();
        if (children.length == 0){
            return false;
        }
        return Arrays.stream(children).anyMatch(f -> f.getName().equals("description.json"))
                && Arrays.stream(children).anyMatch(f -> f.getName().equals("variations.json"))
                && Arrays.stream(children).anyMatch(f -> f.getName().equals("variables.json"));
    }

    public static boolean isLibrary(VirtualFile recipeFolder) {
        VirtualFile descriptionFile = recipeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        if (descriptionFile != null){
            try {
                Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);
                return (Boolean) descriptionJson.getOrDefault("library", false);
            }catch(Exception ex){
                LOGGER.error(ex);
            }
        }
        return false;
    }

    public static void setupNewRecipe(Project project, Path recipeFolderPath) throws IOException {
        VirtualFile recipeFolder = VfsUtil.findFile(recipeFolderPath, true);

        if (recipeFolder != null) {
            setupNewRecipe(project, recipeFolder);
        } else {
            LOGGER.error("setupNewRecipe: Recipe folder was not found for path "+recipeFolderPath);
        }
    }

    public static void setupNewRecipe(Project project, VirtualFile recipeFolder) throws IOException {
        VirtualFile descriptionJson = recipeFolder.findChild("description.json");

        boolean simplifiedView = ConfigurationService
                .getInstance(project).getGlobalConfiguration().getMiscOptions().isSimplifiedView();

        if (simplifiedView){
            try {
                assert descriptionJson != null;
                Map<String, Object> description = JsonUtil.read(descriptionJson);

                Map<String, Object> options = new LinkedHashMap<>();
                options.put("simplified-view", true);
                description.put("options", options);

                JsonUtil.write(description, descriptionJson);
            }catch(ParseException ignored){}
        }
    }
}
