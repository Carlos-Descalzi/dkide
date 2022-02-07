package io.datakitchen.ide.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CompilerService implements Disposable {

    private static final Logger LOGGER = Logger.getInstance(CompilerService.class);

    private final Module module;

    private Container container;
    private String url;

    public static CompilerService getInstance(Module module){
        return module.getService(CompilerService.class);
    }

    public CompilerService(Module module){
        this.module = module;
    }

    private void init(){
        ContainerService service = ContainerService.getInstance(module.getProject());

        String containerName = "compiler-service-"+module.getName();

        try {
            container = service.findContainerByName(containerName, true);
            if (container != null && !service.isRunning(container)){
                service.stopContainer(container);
                service.removeContainer(container);
                container = null;
            }

            if (container == null) {
                ContainerDefinition definition = new ContainerDefinition();
                definition.setImageName("datakitchenprivate/dk_recipe_runner:latest-sd9987");
                VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
                Map<String, String> mounts = new HashMap<>();
                mounts.put(recipeFolder.getPath(), "/dk/" + recipeFolder.getName());
                definition.setMounts(mounts);
                definition.setCommandLine(Arrays.asList("/dk/compiler-server.sh", module.getName()));
                definition.setContainerName(containerName);

                container = service.createContainer(definition);

                url = "http://"+service.getIpAddress(container)+":18881";

                waitInit(url);
            } else {
                url = "http://"+service.getIpAddress(container)+":18881";
            }
            LOGGER.info("Compiler service for "+module.getName()+" started");
        }catch (Exception ex){
            LOGGER.error(ex);
        }
    }

    private void waitInit(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        while(true){
            HttpGet get = new HttpGet(url+"/compile");
            try {
                client.execute(get);
                break;
            } catch (IOException ignored){
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public synchronized void checkInit(){
        if (container == null){
            LOGGER.trace("checkInit - start");
            init();
            LOGGER.trace("checkInit - finish");
        }
    }

    public void compileFile(VirtualFile path, Consumer<String> consumer) {
        ConfigurationService service = ConfigurationService.getInstance(module.getProject());

        ApplicationManager.getApplication().invokeLater(()->{
            checkInit();
            try {
                HttpPost post = new HttpPost(url+"/compile");
                System.out.println("URL:"+url);

                Map<String, Object> requestData = new LinkedHashMap<>();
                requestData.put("kitchen",service.getProjectConfiguration().getProjectSettings().getKitchenName());
                requestData.put("variation",RecipeUtil.getActiveVariation(module));
                try (InputStream input = path.getInputStream()) {
                    requestData.put("content", Base64.encodeBase64String(IOUtils.toByteArray(input),false));
                }
                try (InputStream input = RecipeUtil.getLocalOverridesFile(module.getProject()).getInputStream()){
                    requestData.put("overrides", Base64.encodeBase64String(IOUtils.toByteArray(input),false));
                }
                post.setEntity(new StringEntity(JsonUtil.toJsonString(requestData), ContentType.APPLICATION_JSON));
                HttpResponse response = HttpClientBuilder.create().build().execute(post);
                String content = IOUtils.toString(new InputStreamReader(response.getEntity().getContent()));

                consumer.accept(content);

            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

    }
    public void compileText(String text, Consumer<String> consumer) {
        ConfigurationService service = ConfigurationService.getInstance(module.getProject());

        ApplicationManager.getApplication().invokeLater(()->{
            checkInit();
            try {
                HttpPost post = new HttpPost(url+"/compile");
                System.out.println("URL:"+url);

                Map<String, Object> requestData = new LinkedHashMap<>();
                requestData.put("kitchen",service.getProjectConfiguration().getProjectSettings().getKitchenName());
                requestData.put("variation",RecipeUtil.getActiveVariation(module));
                requestData.put("content", Base64.encodeBase64String(text.getBytes(),false));
                try (InputStream input = RecipeUtil.getLocalOverridesFile(module.getProject()).getInputStream()){
                    requestData.put("overrides", Base64.encodeBase64String(IOUtils.toByteArray(input),false));
                }
                post.setEntity(new StringEntity(JsonUtil.toJsonString(requestData), ContentType.APPLICATION_JSON));
                HttpResponse response = HttpClientBuilder.create().build().execute(post);
                String content = IOUtils.toString(new InputStreamReader(response.getEntity().getContent()));

                consumer.accept(content);

            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

    }

    @Override
    public void dispose() {
    }
}
