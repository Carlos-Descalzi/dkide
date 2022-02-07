package io.datakitchen.ide.editors.ingredient;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import io.datakitchen.ide.editors.*;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.ui.ItemListEvent;
import io.datakitchen.ide.ui.ItemListListener;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IngredientsListEditor extends JPanel implements DocumentEditor, Disposable, VariationItemEditor {
    private final EventSupport<VariationEditionListener> editionListeners = EventSupport.of(VariationEditionListener.class);
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final ItemList<IngredientItem> itemList = new ItemList<>(this::createIngredient);
    private final IngredientEditor ingredientEditor = new IngredientEditor();

    private final ListSelectionListener listSelectionListener = this::selectIngredient;
    private final DocumentChangeListener documentChangeListener = this::documentChanged;
    private final ItemListListener itemListListener = new ItemListListener() {
        @Override
        public void itemAdded(ItemListEvent event) {
            notifyItemAdded(event);
        }

        @Override
        public void itemRemoved(ItemListEvent event) {
            notifyItemRemoved(event);
        }

        @Override
        public void itemChanged(ItemListEvent event) {
            notifyItemChanged(event);
        }
    };

    private IngredientItem currentItem;
    private Map<String, Object> document;

    public IngredientsListEditor(){
        setLayout(new BorderLayout());
        add(itemList, BorderLayout.WEST);
        Disposer.register(this, itemList);
        add(ingredientEditor, BorderLayout.CENTER);
        enableEvents();

    }

    private IngredientItem createIngredient(){
        return new IngredientItem("ingredient-"+(itemList.getDataSize()+1));
    }

    private void documentChanged(DocumentChangeEvent documentChangeEvent) {
        ingredientEditor.setIngredientList(itemList.getData().stream().map(IngredientItem::toString).collect(Collectors.toList()));
    }

    private void enableEvents(){
        ingredientEditor.addDocumentChangeListener(this.documentChangeListener);
        itemList.addListSelectionListener(this.listSelectionListener);
        itemList.addDocumentChangeListener(this.documentChangeListener);
        itemList.addItemListListener(this.itemListListener);
    }

    private void disabledEvents(){
        ingredientEditor.removeDocumentChangeListener(this.documentChangeListener);
        itemList.removeListSelectionListener(this.listSelectionListener);
        itemList.removeDocumentChangeListener(this.documentChangeListener);
        itemList.removeItemListListener(this.itemListListener);
    }

    private void updateActions(){
        ingredientEditor.setEnabled(currentItem != null);
    }

    @Override
    public void addDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.addListener(listener);
    }

    @Override
    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    public void setVariationDocument(Map<String, Object> document) {
        disabledEvents();
        this.document = document;
        List<Map<String,Object>> ingredients = ObjectUtil.cast(document.get("ingredient-definition-list"));

        if (ingredients == null){
            ingredients = new ArrayList<>();
        }
        itemList.setData(ingredients.stream().map(IngredientItem::new).collect(Collectors.toList()));
        currentItem = null;
        ingredientEditor.setIngredientList(itemList.getData().stream().map(IngredientItem::toString).collect(Collectors.toList()));
        ingredientEditor.loadItem(currentItem);
        enableEvents();
        if (ingredients.size() > 0){
            this.itemList.setSelectedIndex(0);
        }
    }

    public void saveDocument() {
        List<Map<String,Object>> ingredients = this.itemList.getData().stream().map(IngredientItem::getContent).collect(Collectors.toList());
        document.put("ingredient-definition-list",ingredients);
    }

    private void selectIngredient(ListSelectionEvent listSelectionEvent) {
        if (currentItem != null){
            ingredientEditor.saveIngredient(currentItem);
        }
        currentItem = itemList.getSelected();

        if (currentItem != null){
            ingredientEditor.loadItem(currentItem);
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void addVariationEditionListener(VariationEditionListener listener) {
        editionListeners.addListener(listener);
    }

    @Override
    public void removeVariationEditionListener(VariationEditionListener listener) {
        editionListeners.removeListener(listener);
    }

    private void notifyItemAdded(ItemListEvent event){
        editionListeners.getProxy().variationItemAdded(
                new VariationEditionEvent(this,"ingredient",event.getItem().getName())
        );
    }
    private void notifyItemRemoved(ItemListEvent event){
        editionListeners.getProxy().variationItemRemoved(
                new VariationEditionEvent(this,"ingredient",event.getItem().getName())
        );
    }
    private void notifyItemChanged(ItemListEvent event){
        editionListeners.getProxy().variationItemChanged(
                new VariationEditionEvent(this,"ingredient",event.getItem().getName(), event.getOldName())
        );
    }

}
