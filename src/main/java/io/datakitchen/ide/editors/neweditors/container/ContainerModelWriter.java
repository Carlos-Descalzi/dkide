package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.neweditors.ModelWriterUtil;
import io.datakitchen.ide.model.Assignment;
import io.datakitchen.ide.model.Connection;
import io.datakitchen.ide.model.Key;
import io.datakitchen.ide.model.Test;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContainerModelWriter {
    protected final Project project;
    protected final ContainerModelImpl model;

    public ContainerModelWriter(Project project, ContainerModelImpl model) {
        this.project = project;
        this.model = model;
    }

    public void write(VirtualFile nodeFolder) throws IOException {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                doWrite(nodeFolder);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    protected void doWrite(VirtualFile nodeFolder) throws IOException, ParseException {
        writeDescription(nodeFolder);
        writeNotebook(nodeFolder);
        writeDataSources(nodeFolder);
        writeDataSinks(nodeFolder);
    }

    private void writeDescription(VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        assert descriptionFile != null;
        Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);
        descriptionJson.put("description", model.getDescription());
        JsonUtil.write(descriptionJson, descriptionFile);
    }

    private void writeNotebook(VirtualFile nodeFolder) throws IOException {
        Map<String, Object> notebook = new LinkedHashMap<>();

        String imageName = model.getImageName();
        String namespace = null;
        String imageTag = "latest";

        if (StringUtils.isNotBlank(imageName)) {
            if (imageName.contains("/")) {
                namespace = imageName.substring(0, imageName.lastIndexOf("/"));
                imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
            }
            if (imageName.contains(":")) {
                imageTag = imageName.substring(imageName.lastIndexOf(":") + 1);
                imageName = imageName.substring(0, imageName.lastIndexOf(":"));
            }
        }

        notebook.put("image-repo", imageName);
        notebook.put("image-tag", imageTag);
        if (namespace != null){
            notebook.put("dockerhub-namespace",namespace);
        }
        if (StringUtils.isNotBlank(model.getUsername())){
            notebook.put("dockerhub-username",model.getUsername());
        }
        if (StringUtils.isNotBlank(model.getPassword())){
            notebook.put("dockerhub-password",model.getPassword());
        }
        if (StringUtils.isNotBlank(model.getRegistry())){
            notebook.put("dockerhub-url",model.getRegistry());
        }
        notebook.put("analytic-container",false);
        if (StringUtils.isNotBlank(model.getCommand())){
            notebook.put("command-line",model.getCommand());
        }

        VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);

        if (!model.getDataSources().getConnections().isEmpty()){
            List<Map<String, String>> inputFiles = new ArrayList<>();

            for (Connection connection: model.getDataSources().getConnections()){
                for (Key key: connection.getKeys()){
                    DataSourceKey dsKey = (DataSourceKey) key;
                    Map<String, String> inputFile = new LinkedHashMap<>();

                    boolean wildcard = dsKey instanceof DataSourceFileKey && ((DataSourceFileKey)dsKey).isWildcard();

                    inputFile.put("key", connection.getName()+"."+(wildcard ? "*" : key.getName()));
                    inputFile.put("filename", dsKey.getContainerFileName());
                    inputFiles.add(inputFile);
                }
            }

            notebook.put("container-input-file-keys", inputFiles);
        }

        if (!model.getDataSinks().getConnections().isEmpty()){
            List<Map<String, String>> outputFiles = new ArrayList<>();

            for (Connection connection:model.getDataSinks().getConnections()){
                for (Key key: connection.getKeys()){
                    DataSinkKey dsKey = (DataSinkKey) key;
                    Map<String, String> outputFile = new LinkedHashMap<>();
                    outputFile.put("key", connection.getName()+"."+key.getName());
                    outputFile.put("filename", dsKey.getContainerFileName());
                    outputFiles.add(outputFile);
                }
            }

            notebook.put("container-output-file-keys", outputFiles);
        }

        if (!model.getAssignments().isEmpty()) {
            List<Map<String, String>> assignmentList = new ArrayList<>();
            for (Assignment assignment : model.getAssignments()) {
                Map<String, String> assignmentJson = new LinkedHashMap<>();
                assignmentJson.put("name", assignment.getVariable());
                assignmentJson.put("file", assignment.getFile());
                assignmentList.add(assignmentJson);
            }
            notebook.put("assign-variables", assignmentList);
        }
        Map<String, Object> tests = new LinkedHashMap<>();
        for (Test test:model.getTests()){
            tests.put("test-"+test.getVariable().getVariableName(),test.toJson());
        }
        notebook.put("tests",tests);

        JsonUtil.write(notebook, notebookFile);
    }

    private void writeDataSinks(VirtualFile nodeFolder) throws IOException {
        VirtualFile dataSinksFolder = nodeFolder.findChild("data_sinks");

        if (dataSinksFolder == null){
            dataSinksFolder = nodeFolder.createChildDirectory(this, "data_sinks");
        }

        for (VirtualFile file: dataSinksFolder.getChildren()){
            file.delete(this);
        }
        for (Connection connection : model.getDataSinks().getConnections()){
            ModelWriterUtil.writeDataSink(dataSinksFolder, connection);
        }
    }

    private void writeDataSources(VirtualFile nodeFolder) throws IOException {
        VirtualFile dataSourcesFolder = nodeFolder.findChild("data_sources");

        if (dataSourcesFolder == null){
            dataSourcesFolder = nodeFolder.createChildDirectory(this, "data_sources");
        }

        for (VirtualFile file: dataSourcesFolder.getChildren()){
            file.delete(this);
        }
        for (Connection connection : model.getDataSources().getConnections()){
            ModelWriterUtil.writeDataSource(dataSourcesFolder, connection);
        }
    }

}
