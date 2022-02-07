package io.datakitchen.ide.tree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiManager;
import com.intellij.ui.SimpleTextAttributes;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.tree.nodes.NodeFactory;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeNode extends ProjectViewNode<Module> {

    private static final Set<String> SKIP = Set.of(
        Constants.FILE_DESCRIPTION_JSON,
        Constants.FILE_VARIABLES_JSON,
        Constants.FILE_VARIATIONS_JSON,"resources","README.md"
    );

    private final Project project;
    private final Module module;

    public RecipeNode(Project project, Module module, ViewSettings v){
        super(project, module,v);
        this.project = project;
        this.module = module;
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
                ProjectView.getInstance(project).refresh();
            }
        });
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> children = new ArrayList<>();

        PsiManager mgr = PsiManager.getInstance(project);

        ViewSettings viewSettings = getSettings();

        VirtualFile moduleFile = getValue().getModuleFile();

        if (moduleFile != null) {
            VirtualFile recipeFolder = moduleFile.getParent();

            try {
                for (String fileName: new String[]{Constants.FILE_DESCRIPTION_JSON, Constants.FILE_VARIATIONS_JSON, Constants.FILE_VARIABLES_JSON}){
                    VirtualFile file = recipeFolder.findChild(fileName);
                    if (file != null){
                        children.add(new RecipeFile(project, mgr.findFile(file), viewSettings));
                    }
                }

                VirtualFile readme = recipeFolder.findChild(Constants.FILE_README_MD);
                if (readme != null) {
                    children.add(new RecipeFile(project, mgr.findFile(readme), viewSettings));
                }

                VirtualFile resourcesFolder = recipeFolder.findChild(Constants.FOLDER_RESOURCES);
                if (resourcesFolder != null){
                    children.add(new ResourcesFolderNode(project, mgr.findDirectory(resourcesFolder), viewSettings));
                }

                List<VirtualFile> childItems = Arrays.stream(recipeFolder.getChildren())
                        .sorted(Comparator.comparing(VirtualFile::getName))
                        .collect(Collectors.toList());

                Set<String> activeNodes = RecipeUtil.getActiveGraphNodes(module);

                boolean hideInactiveNodes = ConfigurationService.getInstance(project)
                        .getGlobalConfiguration()
                        .getMiscOptions()
                        .isHideInactiveNodes();

                for (VirtualFile item : childItems) {
                    if (item.isDirectory()
                            && !item.getName().equals(".idea")
                            && !SKIP.contains(item.getName())) {

                        if (activeNodes.isEmpty()
                            || activeNodes.contains(item.getName())
                            || !hideInactiveNodes){
                            try {
                                AbstractTreeNode<?> node = NodeFactory.createNodeForFile(project, item, viewSettings);
                                if (node != null) {
                                    children.add(node);
                                }
                            }catch(ProcessCanceledException ex){
                                // must ignore
                            }catch (Exception ex){
                                LOG.warn("Unable to create tree view for node",ex);
                            }
                        }

                    }
                }
            } catch (ProcessCanceledException ignored){
                // must ignore
            } catch (Exception ex) {
                LOG.error("Error building project tree",ex);
            }
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {

        String activeVariation = RecipeUtil.getActiveVariation(module);
        String recipeName = module.getName();

        String label = recipeName + (StringUtils.isNotBlank(activeVariation) ? " ("+activeVariation+")" : "");

        presentation.addText(label, SimpleTextAttributes.REGULAR_ATTRIBUTES);

    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        VirtualFile root = RecipeUtil.getProjectFolder(getProject());
        return VfsUtilCore.isAncestor(root, file, false);
    }


}
