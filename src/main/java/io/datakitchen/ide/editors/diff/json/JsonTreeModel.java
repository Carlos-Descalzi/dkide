package io.datakitchen.ide.editors.diff.json;

import com.google.gson.JsonObject;
import com.intellij.util.Consumer;
import com.intellij.util.ui.tree.AbstractTreeModel;

import javax.swing.tree.TreePath;
import java.util.Iterator;
import java.util.List;

public class JsonTreeModel extends AbstractTreeModel {

    private final KeyValuePairList<String, Object> root;

    public JsonTreeModel(KeyValuePairList<String, Object> root){
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public Object getChild(Object parent, int index) {
        if (parent instanceof List){
            return ((List)parent).get(index);
        } else if (parent instanceof KeyValuePair && ((KeyValuePair)parent).getValue() instanceof List){
            return ((List)((KeyValuePair)parent).getValue()).get(index);
        }
        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public int getChildCount(Object parent) {
        if (parent instanceof List){
            return ((List)parent).size();
        }else if (parent instanceof KeyValuePair && ((KeyValuePair)parent).getValue() instanceof List){
            return ((List)((KeyValuePair)parent).getValue()).size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node instanceof KeyValuePair){
            return !(((KeyValuePair<?, ?>) node).getValue() instanceof List);
        }
        return !(node instanceof List);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof List){
            return ((List)parent).indexOf(child);
        } else if (parent instanceof KeyValuePair && ((KeyValuePair)parent).getValue() instanceof List){
            return ((List)((KeyValuePair)parent).getValue()).indexOf(child);
        }
        return 0;
    }

    public void updateTree() {
        updateDocument(root);
        treeStructureChanged(new TreePath(root),null,null);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void updateDocument(KeyValuePairList<String, Object> doc){
        for (Iterator<KeyValuePair<String, Object>> i = doc.iterator();i.hasNext();){

            KeyValuePair<String, Object> pair = i.next();
            if (pair instanceof Patch){
                PatchKeyValuePair patchPair = (PatchKeyValuePair)pair;

                if (patchPair.isDone()){

                    if ((patchPair.getAction() == Patch.Action.APPLY
                        && patchPair.getOperation().equals(Patch.OP_REMOVE)
                        || (patchPair.getAction() == Patch.Action.IGNORE
                            && patchPair.getOperation().equals(Patch.OP_ADD)))
                    ){
                        i.remove();
                    }
                }
            } // TODO finish
            else if (pair.getValue() instanceof KeyValuePairList){
                updateDocument((KeyValuePairList) pair.getValue());
            }

        }
    }

    public void applyAll() {
        walkThrough(root, Patch::apply);
    }

    public void ignoreAll() {
        walkThrough(root, Patch::ignore);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void walkThrough(KeyValuePairList<String, Object> obj, Consumer<Patch> consumer){
        for (KeyValuePair kv:obj){
            if (kv instanceof Patch){
                consumer.consume((Patch)kv);
            } else if (kv.getValue() instanceof Patch){
                consumer.consume((Patch)kv.getValue());
            } else if (kv.getValue() instanceof KeyValuePairList){
                walkThrough((KeyValuePairList<String, Object>) kv.getValue(), consumer);
            } else if (kv.getValue() instanceof List){
                walkThrough((List<JsonObject>)kv.getValue(), consumer);
            }
        }

    }

    @SuppressWarnings({"unchecked"})
    private void walkThrough(List<JsonObject> value, Consumer<Patch> consumer) {
        for (Object item: value) {
            if (item instanceof Patch) {
                consumer.consume((Patch) item);
            } else if (item instanceof KeyValuePairList){
                walkThrough((KeyValuePairList<String, Object>) item, consumer);
            } else if (item instanceof List){
                walkThrough(((List<JsonObject>) item), consumer);
            }
        }
    }


}
