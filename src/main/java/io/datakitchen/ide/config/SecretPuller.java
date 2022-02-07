package io.datakitchen.ide.config;

import com.intellij.openapi.application.ApplicationManager;
import io.datakitchen.ide.tools.LogTarget;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecretPuller {

    private String url;
    private String token;
    private String prefix;
    private boolean verify;
    private boolean overwrite;
    private Header[] headers;
    private LogTarget logTarget = new LogTarget() {
        @Override
        public void println(String message) {
            System.out.println(message);
        }
    };

    public SecretPuller(String url, String token, String prefix, boolean verify, boolean overwrite){
        this.url = url;
        this.token = token;
        this.prefix = prefix;
        this.verify = verify;
        this.overwrite = overwrite;
    }

    public SecretPuller setLogTarget(LogTarget logTarget){
        this.logTarget = logTarget;
        return this;
    }

    public void run(){
        ApplicationManager.getApplication().invokeLater(()->{
            try {
                this.pull();
            }catch (Exception ex){ex.printStackTrace();}
        });
    }

    private String listUrl(String path){
        return url + "/v1/secret/" + path + (path.endsWith("/") ? "" : "/") + "?list=true";
    }

    private String getUrl(String path){
        return url + "/v1/secret/" + path;
    }

    public List<Secret> pull() throws Exception {
        headers = new Header[]{
                new BasicHeader("X-Vault-Token", token),
                new BasicHeader("Accept", "application/json")
        };
        return pull(prefix, prefix);
    }

    private List<Secret> pull(String prefix, String basePath) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();

        HttpGet get = new HttpGet(listUrl(basePath));
        get.setHeaders(headers);

        HttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() != 200){
            return new ArrayList<>();
        }

        Map<String, Object> result = getContent(response);
        Map<String, Object> data = ObjectUtil.cast(result.get("data"));
        List<String> keys = ObjectUtil.cast(data.get("keys"));
        List<Secret> secrets = new ArrayList<>();

        for (String key: keys){
            String path = basePath + (basePath.endsWith("/") ? "" : "/") + key;
            if (key.endsWith("/")){
                if (!key.equals("_config/")) {
                    secrets.addAll(pull(prefix, path));
                }
            } else {
                logTarget.println("Storing secret "+path);
                secrets.add(readSecret(prefix, path));
            }
        }
        return secrets;
    }

    private Secret readSecret(String prefix, String path) throws Exception{
        HttpGet get = new HttpGet(getUrl(path));
        get.setHeaders(headers);
        HttpClient client = HttpClientBuilder.create().build();

        HttpResponse response = client.execute(get);

        Map<String, Object> content = getContent(response);

        String secretPath = path.substring(prefix.length()+1);

        Map<String, Object> data = ObjectUtil.cast(content.get("data"));

        String vale = String.valueOf(data.get("value"));

        return new Secret(secretPath, vale);
    }

    private Map<String, Object> getContent(HttpResponse response) throws Exception {
        return JsonUtil.read(response.getEntity().getContent());
    }

}
