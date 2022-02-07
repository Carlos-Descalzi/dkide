package io.datakitchen.ide.tree.nodes.script;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.MiscOptions;
import io.datakitchen.ide.tree.BaseRecipeNodeFolder;
import io.datakitchen.ide.util.JsonUtil;

import java.util.*;

public class ScriptsFolderNode extends BaseRecipeNodeFolder {

    private final Set<String> mainScripts = new HashSet<>();
    private final boolean rootScriptsFolder;

    @SuppressWarnings("unchecked")
    public ScriptsFolderNode(Project project, PsiDirectory item, ViewSettings viewSettings) {

        super(project, item, viewSettings);
        this.rootScriptsFolder = item.getName().equals("docker-share");

        try {
            VirtualFile configJsonFile = item.getVirtualFile().findChild("config.json");
            if (configJsonFile != null) {
                Map<String, Object> obj = JsonUtil.read(configJsonFile);
                Map<String, Object> keys = (Map<String, Object>) obj.get("keys");
                for (Map.Entry<String, Object> entry : keys.entrySet()) {
                    Map<String, Object> content = (Map<String, Object>) entry.getValue();
                    mainScripts.add((String) content.get("script"));
                }
            }
        } catch (Exception ignored){

        }
    }

    public Set<String> getMainScripts(){
        return mainScripts;
    }

    @Override
    protected String getDisplayName() {
        return rootScriptsFolder ? "Scripts" : getValue().getName();
    }

    @Override
    public Collection<AbstractTreeNode<?>> getChildrenImpl() {
        List<AbstractTreeNode<?>> items = new ArrayList<>();

        for (PsiFile child:getValue().getFiles()){
            if (!"config.json".equals(child.getName()) || allowConfigJson()){
                items.add(new ScriptFileNode(getProject(), this, child,getSettings()));
            }
        }
        for (PsiDirectory child: getValue().getSubdirectories()){
            items.add(new ScriptsFolderNode(getProject(),child,getSettings()));
        }

        return items;
    }

    private boolean allowConfigJson(){
        ConfigurationService service = ConfigurationService.getInstance(getProject());
        MiscOptions options = service.getGlobalConfiguration().getMiscOptions();
        return !options.isUseCustomForms()
                || !options.isHideConfigJsonOnForms();
    }
}
