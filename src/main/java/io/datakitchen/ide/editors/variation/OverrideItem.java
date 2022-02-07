package io.datakitchen.ide.editors.variation;

public class OverrideItem {
    private boolean selected;
    private String name;

    public String toString() {
        return name;
    }

    public OverrideItem(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
