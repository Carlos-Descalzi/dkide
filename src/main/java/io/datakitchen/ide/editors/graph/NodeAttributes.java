package io.datakitchen.ide.editors.graph;

import java.awt.*;

public class NodeAttributes {

    private Color color;

    public NodeAttributes(){
        color = Color.WHITE;
    }

    public NodeAttributes(Color color){
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
