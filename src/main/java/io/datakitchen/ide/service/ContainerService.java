package io.datakitchen.ide.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.DockerConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ContainerService implements Disposable {
    private final Project project;
    private final Set<String> containers = new HashSet<>();
    private boolean serviceAvailable = true;

    public ContainerService(Project project){
        this.project = project;
    }

    public static ContainerService getInstance(Project project){
        return project.getService(ContainerService.class);
    }

    public void startService() throws ContainerServiceException {
        serviceAvailable = doCheckServiceAvailable();
        System.out.println("Docker service started");
    }

    public static String getDefaultDockerHost(){
        return new DefaultDockerClientConfig.Builder().build().getDockerHost().toString();
    }

    private boolean hostSet(DockerConfiguration dockerConfiguration){
        return StringUtils.isNotBlank(dockerConfiguration.getSocketPath())
                && ! "/var/run/docker.sock".equals(dockerConfiguration.getSocketPath()); // ignore old default bad setting
    }

    private DockerClient createClient() throws ContainerServiceException {

        ConfigurationService configService = ConfigurationService.getInstance(project);
        DockerConfiguration dockerConfig = configService.getDockerConfiguration();

        DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig
                .createDefaultConfigBuilder();

        if (dockerConfig != null) {
            configBuilder
                .withRegistryUsername(dockerConfig.getUsername())
                .withRegistryPassword(dockerConfig.getPassword());

            if (hostSet(dockerConfig)){
                configBuilder.withDockerHost(dockerConfig.getSocketPath());
            }
        }
        DockerClientConfig config = configBuilder.build();
        DockerHttpClient httpClient = new JerseyDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }
    public boolean isServiceAvailable() {
        return serviceAvailable;
    }

    public boolean checkServiceAvailable(){
        this.serviceAvailable = doCheckServiceAvailable();
        return this.serviceAvailable;
    }

    private boolean doCheckServiceAvailable(){
        try (DockerClient client = createClient()) {
            client.infoCmd().exec();
            return true;
        } catch (Exception ex){
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public Container createContainer(ContainerDefinition definition) throws ContainerServiceException {
        try {
            DockerClient client = createClient();

            removeExistingContainers(client, List.of(definition.getContainerName()));

            CreateContainerCmd cmd = client.createContainerCmd(definition.getImageName());

            List<Bind> binds = definition
                    .getMounts()
                    .entrySet()
                    .stream()
                    .map((Map.Entry<String, String> e) -> {
                        return new Bind(e.getKey(), new Volume(e.getValue()));
                    })
                    .collect(Collectors.toList());

            cmd.withBinds(binds);

            List<String> environment = definition
                    .getEnvironment()
                    .entrySet()
                    .stream()
                    .map((Map.Entry<String, String> e) -> {
                        return e.getKey() + "=" + e.getValue();
                    })
                    .collect(Collectors.toList());

            cmd.withEnv(environment);

            if (definition.getCommandLine() != null) {
                cmd.withCmd(definition.getCommandLine());
            }

            if (StringUtils.isNotBlank(definition.getContainerName())) {
                cmd.withName(definition.getContainerName());
            }

            CreateContainerResponse response = cmd.exec();

            String containerId = response.getId();

            containers.add(containerId);

            client.startContainerCmd(containerId).exec();

            return new ContainerImpl(createClient(), containerId);
        } catch (Exception ex){
            throw new ContainerServiceException(ex.getMessage());
        }
    }

    private void removeExistingContainers(DockerClient client, List<String> containerNames) {
        for (com.github.dockerjava.api.model.Container c:client.listContainersCmd().withNameFilter(containerNames).exec()){
            client.removeContainerCmd(c.getId());
        }
    }

    @SuppressWarnings("deprecation")
    public void createContainerAsync(ContainerDefinition definition, Consumer<Container> consumer) throws ContainerServiceException {
        try  {
            DockerClient client = createClient() ;
            CreateContainerCmd cmd = client.createContainerCmd(definition.getImageName());

            List<Bind> binds = definition
                    .getMounts()
                    .entrySet()
                    .stream()
                    .map((Map.Entry<String, String> e) -> {
                        return new Bind(e.getKey(), new Volume(e.getValue()));
                    })
                    .collect(Collectors.toList());

            cmd.withBinds(binds);

            List<String> environment = definition
                    .getEnvironment()
                    .entrySet()
                    .stream()
                    .map((Map.Entry<String, String> e) -> {
                        return e.getKey() + "=" + e.getValue();
                    })
                    .collect(Collectors.toList());

            cmd.withEnv(environment);

            if (definition.getCommandLine() != null) {
                cmd.withCmd(definition.getCommandLine());
            }

            if (StringUtils.isNotBlank(definition.getContainerName())) {
                cmd.withName(definition.getContainerName());
            }

            DockerClient newClient = getClient();

            new Thread(() -> {
                CreateContainerResponse response = cmd.exec();
                String containerId = response.getId();
                addContainer(containerId);
                client.startContainerCmd(containerId).exec();
                consumer.accept(new ContainerImpl(newClient, containerId));
            }).start();
        }catch(Exception ex){
            throw new ContainerServiceException(ex.getMessage());
        }
    }

    private synchronized void addContainer(String containerId){
        containers.add(containerId);
    }

    public Container findContainerByName(String containerName, boolean includeExited) throws ContainerServiceException {
        try (DockerClient client = createClient()) {
            ListContainersCmd cmd = client
                    .listContainersCmd()
                    .withFilter("name", List.of(containerName));

            if (includeExited) {
                cmd.withShowAll(true);
            }

            List<com.github.dockerjava.api.model.Container> containers = cmd.exec();

            if (!containers.isEmpty()) {
                return new ContainerImpl(getClient(), containers.get(0).getId());
            }
        }catch(IOException ex){
            throw new ContainerServiceException(ex.getMessage());
        }
        return null;
    }

    public String getIpAddress(Container container){

        DockerClient client = ((ContainerImpl)container).getClient();

        InspectContainerResponse inspectResponse = client.inspectContainerCmd(container.getContainerId()).exec();

        return inspectResponse.getNetworkSettings().getNetworks().get("bridge").getIpAddress();
    }

    public void getStatsStream(Container container, Consumer<ContainerStats> consumer){
        DockerClient client = ((ContainerImpl)container).getClient();
        ContainerStats stats = new ContainerStats();
        try {

            StatsCmd cmd = client.statsCmd(container.getContainerId());

            cmd
                    .withNoStream(false)
                    .exec(new ResultCallback.Adapter<>() {

                        @Override
                        public void onNext(Statistics object) {
                            MemoryStatsConfig memoryStats = object.getMemoryStats();
                            if (memoryStats != null) {
                                stats.setMemoryUsage(memoryStats.getUsage());
                                stats.setMemoryLimit(memoryStats.getLimit());
                            }
                            try {
                                CpuStatsConfig cpuStats = object.getCpuStats();
                                CpuStatsConfig preCpuStats = object.getPreCpuStats();
                                Long systemCpuUsage = cpuStats.getSystemCpuUsage();
                                Long systemPreCpuUsage = preCpuStats.getSystemCpuUsage();

                                if (cpuStats != null && preCpuStats != null && systemCpuUsage != null) {

                                    if (systemPreCpuUsage != null) {
                                        float deltaTotalUsage = (float) (cpuStats.getCpuUsage().getTotalUsage()
                                                - preCpuStats.getCpuUsage().getTotalUsage())
                                                / (float) object.getPreCpuStats().getCpuUsage().getTotalUsage();

                                        float deltaSystemUsage = (float) (systemCpuUsage
                                                - systemPreCpuUsage)
                                                / (float) systemPreCpuUsage;

                                        float usagePercent = cpuStats.getCpuUsage().getPercpuUsage() != null
                                                ? (deltaTotalUsage - deltaSystemUsage)
                                                * cpuStats.getCpuUsage().getPercpuUsage().size()
                                                * 100
                                                : 0;

                                        if (usagePercent < 100f) {
                                            stats.setCpuUsage(usagePercent);
                                        }
                                    }
                                } else {
                                    try {
                                        this.close();
                                        cmd.close();
                                    }catch(Exception ignored){}
                                }
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                            consumer.accept(stats);
                        }
                    });
        }catch (ConflictException ignored){
        }
    }

    public String getLogs(Container container, Long lastTs){
        DockerClient client = ((ContainerImpl)container).getClient();

        List<String> logs = new ArrayList<>();

        LogContainerCmd cmd = client.logContainerCmd(container.getContainerId())
                .withStdOut(true)
                .withStdErr(true)
                .withTailAll()
                .withFollowStream(false);

        try {
            cmd.exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame object) {
                    try {
                        logs.add(object.toString().replace("STDOUT: ","").replace("STDERR: ",""));
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }).awaitCompletion();
        }catch (ConflictException | InterruptedException ignored){
        }
        return logs.isEmpty() ? null : String.join("\n",logs);
    }

    public void getLogsStream(Container container, Consumer<String> consumer, Runnable onComplete){
        DockerClient client = ((ContainerImpl)container).getClient();

        LogContainerCmd cmd = client.logContainerCmd(container.getContainerId())
                .withStdOut(true)
                .withStdErr(true)
                .withTailAll()
                .withFollowStream(true);

        try {
            cmd.exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame object) {
                    consumer.accept(
                        object.toString()
                            .replace("STDOUT: ","")
                            .replace("STDERR: ","")
                            +"\n"
                    );
                }

                @Override
                public void onComplete() {
                    super.onComplete();
                    onComplete.run();
                }
            });
        }catch (ConflictException ignored){
        }
    }

    public boolean isRunning(Container container) {
        DockerClient client = ((ContainerImpl)container).getClient();

        InspectContainerResponse inspectResponse = client.inspectContainerCmd(container.getContainerId()).exec();

        return inspectResponse.getState().getRunning();
    }

    public int getExitCode(Container container) {
        DockerClient client = ((ContainerImpl)container).getClient();

        InspectContainerResponse inspectResponse = client.inspectContainerCmd(container.getContainerId()).exec();

        return inspectResponse.getState().getExitCode();
    }

    public void stopContainer(Container container) throws ContainerServiceException {

        DockerClient client = ((ContainerImpl)container).getClient();

        try {
            client.killContainerCmd(container.getContainerId()).exec();
        }catch (ConflictException ex){
            // already stopped
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void cleanup(){
        try {
            DockerClient client = createClient();
            for (String containerId : containers) {
                new Thread(()->{
                    try {
                        client.killContainerCmd(containerId).exec();
                    }catch (Exception ignored){}
                    try {
                        client.removeContainerCmd(containerId).exec();
                    }catch (ConflictException ignored){}
                }).start();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void pullImage(String imageName, PullProgressListener listener) throws ContainerServiceException{
        try (DockerClient client = createClient()) {
            client.pullImageCmd(imageName).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(PullResponseItem object) {
                    ResponseItem.ProgressDetail progressDetail = object.getProgressDetail();

                    ResponseItem.ErrorDetail errorDetail = object.getErrorDetail();
                    try {
                        listener.progress(
                                object.getId(),
                                object.isPullSuccessIndicated(),
                                progressDetail != null ? progressDetail.getCurrent() : null,
                                progressDetail != null ? progressDetail.getTotal() : null,
                                errorDetail != null ? errorDetail.getMessage() : null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).awaitCompletion();
        }catch(IOException ex){
            throw new ContainerServiceException(ex.getMessage());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void pullAsync(
            ContainerDefinition containerDefinition,
            Consumer<InspectImageResponse> onFinish,
            Consumer<String> onError)
        throws ContainerServiceException{

        try (DockerClient client = createClient()) {

            PullImageCmd cmd = client.pullImageCmd(containerDefinition.getImageName());
            if (StringUtils.isNotBlank(containerDefinition.getUserName())) {

                cmd.withAuthConfig(new AuthConfig()
                        .withUsername(containerDefinition.getUserName())
                        .withPassword(containerDefinition.getPassword()));
            } else {
                cmd.withAuthConfig(new AuthConfig());
            }
            if (StringUtils.isNotBlank(containerDefinition.getRegistryUrl())) {
                cmd.withRegistry(containerDefinition.getRegistryUrl());
            }
            cmd.exec(new ResultCallback.Adapter<>() {
                @Override
                public void onError(Throwable throwable) {
                    onError.accept(throwable.getMessage());
                }

                @Override
                public void onNext(PullResponseItem object) {
                    try {
                        if (object.isPullSuccessIndicated()) {
                            onFinish.accept(client.inspectImageCmd(containerDefinition.getImageName()).exec());
                        } else if (object.isErrorIndicated()) {
                            onError.accept(object.getErrorDetail().getMessage());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (IOException ex){
            throw new ContainerServiceException(ex.getMessage());
        }
    }

    public void removeContainer(Container container) {
        DockerClient client = ((ContainerImpl)container).getClient();

        try {
            client.removeContainerCmd(container.getContainerId()).exec();
        }catch (ConflictException ex){
            // already removed
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public DockerClient getClient() throws ContainerServiceException {
        return createClient();
    }

    @Override
    public void dispose() {
        cleanup();
    }
}
