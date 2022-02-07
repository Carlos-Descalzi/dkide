package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.Mapping;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class ArrowsGlassPane extends JPanel{

    private final DataMapperNodeView editor;

    public ArrowsGlassPane(DataMapperNodeView editor){
        this.editor = editor;
        setOpaque(false);
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    protected void paintComponent(Graphics g){

        for (Mapping item:editor.getModel().getMappings()){
            Point s = editor.getHookPointForSourceFile(item.getSourceKey().getConnection().getName(),item.getSourceKey().getName());
            Point t = editor.getHookPointForSinkFile(item.getSinkKey().getConnection().getName(),item.getSinkKey().getName());

            drawRectArrow(g, s, t);

        }
    }

    private void drawCurvedArrow(Graphics g, Point s, Point t){

        float deltaY = t.y - s.y;

        Path2D.Float path = new Path2D.Float();
        if (deltaY != 0f){

            float step = deltaY / 4;

            path.moveTo(s.x,s.y);
            path.lineTo(s.x+30,s.y);
            path.curveTo(s.x+35,s.y-5,s.x+50,s.y-10,s.x+45,s.y-15);
        } else{
            path.lineTo(t.x,t.y);
        }
        ((Graphics2D)g).draw(path);
    }

    private void drawRectArrow(Graphics g, Point s, Point t) {
        g.drawLine(s.x, s.y, t.x, t.y);

        double angle = Math.atan2(t.y- s.y, t.x- s.x);

        final int headSize = 10;
        final int headAngle = 30;

        double angle1 = angle+headAngle * Math.PI/180.0;
        double angle2 = angle-headAngle * Math.PI/180.0;

        g.drawLine(t.x, t.y,(int)(t.x-headSize*Math.cos(angle1)),(int)(t.y-headSize*Math.sin(angle1)));
        g.drawLine(t.x, t.y,(int)(t.x-headSize*Math.cos(angle2)),(int)(t.y-headSize*Math.sin(angle2)));
    }

}
