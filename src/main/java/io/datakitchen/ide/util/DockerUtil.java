package io.datakitchen.ide.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.service.PullProgressListener;
import io.datakitchen.ide.tools.ProcessLogView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.util.*;

public class DockerUtil {

    private static final Logger LOGGER = Logger.getInstance(DockerUtil.class);

    public static void pullImages(Project project){
        ProcessLogView view = new ProcessLogView();

        PullProgressListener listener = (id, success, current, total, errorMessage) -> {
            if (errorMessage != null){
                view.println(errorMessage);
            } else if (success){
                view.println("Pull success");
            } else {
                if (current == null){
                    current = 0L;
                }
                if(total == null){
                    total = 0L;
                }
                long percent = total == 0 ? 0 : (current / total) * 100;

                if (percent != 0 && id != null) {
                    view.println(id + ":" + percent + "%");
                }
            }
        };

        Runnable task = ()->{
            try {
                for (String image:new String[]{Constants.RECIPE_RUNNER_IMAGE, Constants.GPC_IMAGE, Constants.GPC_DEBUG_IMAGE}){
                    view.println("Pulling image "+image);
                    ContainerService.getInstance(project).pullImage(image, listener);
                }
            }catch (Exception ex){
                Messages.showErrorDialog(ex.getMessage(),"Error");
            }
        };

        ToolWindowUtil.show("images-pull",project, List.of(new ContentImpl(view, "Pulling Docker Images ...", false)));
        view.runTask(task);
    }

    public static Map<String, String> makeEnvironment(Project project){
        Map<String, String> environment = new LinkedHashMap<>();
        environment.putAll(getBasicEnvironment(project));
        environment.putAll(ingredientAccountToEnvironmentMap(project));
        environment.putAll(secretsToEnvironmentMap(project));
        return environment;
    }

    public static Map<String, String> secretsToEnvironmentMap(Project project){
        Map<String, String> environment = new LinkedHashMap<>();
        List<Secret> secrets = getSecrets(project);
        if (secrets != null) {
            for (Secret secret : secrets) {
                String envKey = "SECRET_" + secret.getPath().replace("/","_").replace("-","_").toUpperCase();
                environment.put(envKey, secret.getValue());
            }
        }
        return environment;
    }

    public static Map<String, String> getBasicEnvironment(Project project){
        Map<String, String> environment = new HashMap<>();

        String kitchenName = ConfigurationService.getInstance(project)
                .getProjectConfiguration().getProjectSettings().getKitchenName();

        if (StringUtils.isBlank(kitchenName)){
            kitchenName = "master";
        }

        environment.put("DK_SHARE_VOLUME","/dk/data/share");
        environment.put("AGENT_TYPE","docker");
        environment.put("KITCHEN", kitchenName);

        return environment;
    }

    public static Map<String, String> ingredientAccountToEnvironmentMap(Project project){
        ConfigurationService service = ConfigurationService.getInstance(project);
        Map<String, String> environment = new LinkedHashMap<>();
        Account account = getIngredientAccount(service);
        String site = service.getProjectConfiguration().getProjectSettings().getIngredientSite();

        if (account != null && site != null){
            environment.put("DK_ACCOUNT",account.getUsername());
            environment.put("DK_PASSWORD",account.getPassword());
            environment.put("DK_URL",site);
        }
        return environment;
    }

    private static List<Secret> getSecrets(Project project){
        ConfigurationService service = project.getService(ConfigurationService.class);
        return service.getSecrets();
    }

    private static Account getIngredientAccount(ConfigurationService service) {
        return service
                .getGlobalConfiguration()
                .getAccounts()
                .stream()
                .filter(Account::isIngredientAccount)
                .findFirst()
                .orElse(null);
    }

    public static String getSocketPath(String dockerSocket) {
        try {
            String socketPath = new URI(dockerSocket).getPath().replace(":rw","");
            File socketFile = new File(socketPath);
            if (FileUtils.isSymlink(socketFile)){
                socketPath = socketFile.toPath().toRealPath().toString();
            }
            LOGGER.info("Real path of socket "+dockerSocket+":"+socketPath);
            return socketPath;
        } catch (Exception ex){
            return dockerSocket;
        }
    }


}
