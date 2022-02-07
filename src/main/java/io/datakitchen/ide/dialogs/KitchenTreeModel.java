package io.datakitchen.ide.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.ui.EventSupport;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitchenTreeModel implements TreeModel {

    private final EventSupport<TreeModelListener> listeners = EventSupport.of(TreeModelListener.class);

    private static final String ROOT = "Kitchens";

    private final ServiceClient client;

    private List<Kitchen> kitchenNames = new ArrayList<>();

    public class RecipeNode {
        private final ServiceClient.Node node;
        private Recipe recipe;

        public String toString(){
            return node.toString();
        }

        public RecipeNode(Recipe recipe, ServiceClient.Node node){
            this.recipe = recipe;
            this.node = node;
        }

        public ServiceClient.Node getNode() {
            return node;
        }
    }

    public interface LoadableNode {
        boolean isLoaded();
        void load();
    }

    public class Recipe implements LoadableNode{
        private final String name;
        private final Kitchen kitchen;
        private List<RecipeNode> nodes = new ArrayList<>();

        public boolean isLoaded(){
            return false; // TODO REMOVE
        }

        public void load(){
            ApplicationManager.getApplication().invokeLater(()->{
                try {
                    nodes = client
                        .getNodes(kitchen.name,this.name)
                        .stream().map((ServiceClient.Node node)->new RecipeNode(this,node))
                        .collect(Collectors.toList());
                    SwingUtilities.invokeLater(()->
                            listeners.getProxy().treeStructureChanged(
                                    new TreeModelEvent(this, new Object[]{ROOT, kitchen, this}))
                    );

                } catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }

        public String toString(){
            return name;
        }

        public Recipe(Kitchen kitchen, String name){
            this.kitchen = kitchen;
            this.name = name;
        }
    }

    public class Kitchen implements LoadableNode{
        private final String name;
        private List<Recipe> recipes = new ArrayList<>();

        public boolean isLoaded(){
            return false; // TODO REMOVE
        }

        public void load(){
            ApplicationManager.getApplication().invokeLater(()->{
                try {
                    recipes = client
                            .getRecipeNames(this.name)
                            .stream().map((String s)->new Recipe(this, s))
                            .collect(Collectors.toList());
                    SwingUtilities.invokeLater(()->
                            listeners.getProxy().treeStructureChanged(new TreeModelEvent(this, new Object[]{ROOT, this}))
                    );
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            });
        }

        public String toString(){
            return name;
        }

        public Kitchen(String name){
            this.name = name;
        }
    }

    public KitchenTreeModel(ServiceClient client){
        this.client = client;
        ApplicationManager.getApplication().invokeLater(()->{
            try {
                kitchenNames = client
                    .getKitchens()
                    .stream()
                    .map((Map <String, Object> o)-> new Kitchen((String)o.get("name")))
                    .collect(Collectors.toList());
                SwingUtilities.invokeLater(()->
                    listeners.getProxy().treeStructureChanged(new TreeModelEvent(this, new Object[]{ROOT}))
                );
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent == ROOT){
            return kitchenNames.get(index);
        } else if (parent instanceof Kitchen){
            return ((Kitchen)parent).recipes.get(index);
        } else if (parent instanceof Recipe){
            return ((Recipe)parent).nodes.get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == ROOT){
            return kitchenNames.size();
        } else if (parent instanceof Kitchen){
            return ((Kitchen)parent).recipes.size();
        } else if (parent instanceof Recipe){
            return ((Recipe)parent).nodes.size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof RecipeNode;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == ROOT){
            return kitchenNames.indexOf(child);
        } else if (parent instanceof Kitchen){
            return ((Kitchen)parent).recipes.indexOf(child);
        } else if (parent instanceof Recipe){
            return ((Recipe)parent).nodes.indexOf(child);
        }
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.addListener(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.removeListener(l);
    }
}
