package io.datakitchen.ide.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MultilineChart extends JComponent {

    @FunctionalInterface
    public interface ScaleCalculator {
        float[] maxValue(int series, List<float[]> valueList);
    }

    private static final ScaleCalculator DEFAULT_SCALE_CALCULATOR = (series, valueList) -> {
        float[] scales = new float[series];
        for (int i=0;i<series;i++){
            scales[i] = 0;
        }
        for (float[] values:valueList){
            for (int i=0;i<series;i++){
                scales[i] = Math.max(scales[i],values[i]);
            }
        }
        for (int i=0;i<series;i++){
            double ceil = Math.ceil(Math.log(scales[i])/Math.log(2));
            scales[i] = (float)Math.pow(2, ceil);
        }
        return scales;
    };

    private final int seriesCount;
    private final String[] seriesLabels;
    private final List<Date> xAxis = new ArrayList<>();
    private final List<float[]> yAxis = new ArrayList<>();
    private float[] scales;
    private int maxVisibleValues = 30;
    private Point mousePosition;
    private boolean mouseOver;
    private Function<Date, String> xLabelRenderer = Date::toString;
    private BiFunction<Integer, Float, String> yLabelRenderer = (Integer i, Float f)->String.valueOf(f);

    private ScaleCalculator scaleCalculator = DEFAULT_SCALE_CALCULATOR;

    public MultilineChart(String[] seriesLabels){
        this.seriesCount = seriesLabels.length;
        this.scales = new float[seriesCount];
        this.seriesLabels = seriesLabels;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK| AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public int getMaxVisibleValues() {
        return maxVisibleValues;
    }

    public void setMaxVisibleValues(int maxVisibleValues) {
        this.maxVisibleValues = maxVisibleValues;
    }

    public Function<Date, String> getXLabelRenderer() {
        return xLabelRenderer;
    }

    public void setXLabelRenderer(Function<Date, String> xLabelRenderer) {
        this.xLabelRenderer = xLabelRenderer;
    }

    public BiFunction<Integer, Float, String> getYLabelRenderer() {
        return yLabelRenderer;
    }

    public void setYLabelRenderer(BiFunction<Integer, Float, String> yLabelRenderer) {
        this.yLabelRenderer = yLabelRenderer;
    }

    public void addValues(Date x, float[] y){
        this.xAxis.add(x);
        this.yAxis.add(y);
        calculateScales();
        SwingUtilities.invokeLater(this::repaint);
    }

    public ScaleCalculator getScaleCalculator() {
        return scaleCalculator;
    }

    public void setScaleCalculator(ScaleCalculator scaleCalculator) {
        this.scaleCalculator = scaleCalculator;
    }

    private void calculateScales() {
        scales = scaleCalculator.maxValue(seriesCount, yAxis);
    }

    public int getXAxisSize() {
        return xAxisSize;
    }

    public void setXAxisSize(int xAxisSize) {
        this.xAxisSize = xAxisSize;
    }

    private int xAxisSize = 100;

    protected void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();

        int leftPadding = 5;
        int rightPadding = 5;
        int yLabelWidth = 100;

        int xAxisPos = getHeight()-xAxisSize;

        int chartAreaHeight = (int)((getHeight()-xAxisSize) / 3f);

        FontMetrics fm = getFontMetrics(getFont());
        int chartLabelHeight = 5+fm.getHeight();

        int axisValueCount = this.yAxis.size();

        int axisValuesStart = this.yAxis.size()-maxVisibleValues;

        List<float[]> values = axisValueCount > maxVisibleValues
                ? new ArrayList<>(this.yAxis.subList(axisValuesStart,this.yAxis.size()))
                : this.yAxis;

        List<Date> xValues = axisValueCount > maxVisibleValues
                ? new ArrayList<>(this.xAxis.subList(axisValuesStart,this.xAxis.size()))
                : this.xAxis;

        int valuesAreaWidth = getWidth()- (yLabelWidth + leftPadding + rightPadding);
        float valueWidth = (float)valuesAreaWidth / (float)maxVisibleValues;

        float valueStartX = leftPadding + valuesAreaWidth - (valueWidth * values.size());

        AffineTransform originalTransform = g2d.getTransform();
        AffineTransform rotate = AffineTransform.getRotateInstance(-Math.PI/2);

        for (int i=0;i<xValues.size();i++){

            String xLabel = xLabelRenderer.apply(xValues.get(i));

            AffineTransform translate = AffineTransform
                    .getTranslateInstance(valueStartX + i*valueWidth,xAxisPos + fm.stringWidth(xLabel));

            translate.concatenate(rotate);

            AffineTransform transform = (AffineTransform)originalTransform.clone();
            transform.concatenate(translate);

            g2d.setTransform(transform);
            g2d.drawString(xLabel, 0,  fm.getHeight() - fm.getDescent());
            g2d.drawLine(fm.stringWidth(xLabel)-20,0,fm.stringWidth(xLabel),0);
        }
        g2d.setTransform(originalTransform);

        for (int i=0,y=0;i<seriesCount;i++,y+=chartAreaHeight){

            Rectangle chartAreaRect = new Rectangle(
                    leftPadding,
                    y+chartLabelHeight,
                    width- (yLabelWidth + leftPadding + rightPadding),
                    chartAreaHeight-chartLabelHeight
            );

            g2d.setColor(getForeground());

            g2d.drawString(seriesLabels[i],leftPadding,y+chartLabelHeight);

            g2d.drawLine(leftPadding,y+chartLabelHeight,leftPadding,y+chartAreaHeight);
            g2d.drawLine(leftPadding,y+chartAreaHeight, width-leftPadding-yLabelWidth, y+chartAreaHeight);
            g2d.drawLine(width-leftPadding-yLabelWidth,y+chartLabelHeight,width-leftPadding-yLabelWidth,y+chartAreaHeight);

            valueStartX = leftPadding + valuesAreaWidth - (valueWidth * values.size());

            Path2D.Float path = new Path2D.Float();

            for (int j=0;j<values.size();j++){
                float value = values.get(j)[i];
                if (j == 0){
                    path.moveTo(valueStartX,y+chartAreaHeight-toPixels(i,value, chartAreaHeight-chartLabelHeight));
                } else {
                    path.lineTo(valueStartX,y+chartAreaHeight-toPixels(i,value, chartAreaHeight-chartLabelHeight));
                }

                valueStartX+=valueWidth;
            }
            g2d.setColor(Color.RED);
            g2d.draw(path);

            if (mousePosition != null && chartAreaRect.contains(mousePosition)){

                float selectedValue = (float)Math.floor ((mousePosition.x - leftPadding) / valueWidth);

                int index = axisValuesStart +(int)selectedValue;//(maxVisibleValues-(int)selectedValue);

                if (index >= 0) {

                    int adjustedPos = (int) (leftPadding + selectedValue * valueWidth);

                    String legend ="Time:" + xLabelRenderer.apply(this.xAxis.get(index))
                            + ", "+seriesLabels[i]
                            +":" + yLabelRenderer.apply(i,yAxis.get(index)[i]);

                    int legendWidth = fm.stringWidth(legend);

                    g.setColor(getBackground());

                    if (adjustedPos+2+legendWidth > width){
                        g.fillRect(adjustedPos - legendWidth - 2, mousePosition.y, legendWidth, fm.getHeight() + fm.getDescent());
                        g.setColor(Color.GREEN.darker());
                        g.drawRect(adjustedPos - legendWidth - 2, mousePosition.y, legendWidth, fm.getHeight() + fm.getDescent());
                        g.drawString(legend,
                                adjustedPos - legendWidth - 2,
                                mousePosition.y + fm.getHeight()
                        );
                    } else {
                        g.fillRect(adjustedPos + 2, mousePosition.y, legendWidth, fm.getHeight() + fm.getDescent());
                        g.setColor(Color.GREEN.darker());
                        g.drawRect(adjustedPos + 2, mousePosition.y, legendWidth, fm.getHeight() + fm.getDescent());
                        g.drawString(legend,
                                adjustedPos + 2,
                                mousePosition.y + fm.getHeight()
                        );
                    }

                    g.drawLine(adjustedPos, chartAreaRect.y, adjustedPos, chartAreaRect.y + chartAreaRect.height);

                }
            }

            g.setColor(getForeground());

            float maxValue = scales[i];

            g2d.drawString(yLabelRenderer.apply(i,maxValue), width-leftPadding-yLabelWidth,y+chartLabelHeight);

        }
    }

    private float toPixels(int i, float value, float chartAreaHeight) {
        return (value / scales[i]) * chartAreaHeight;
    }

    protected void processMouseEvent(MouseEvent e){
        super.processMouseEvent(e);
        if (e.getID() == MouseEvent.MOUSE_ENTERED) {
            mouseOver = true;
            repaint();
        } else if (e.getID() == MouseEvent.MOUSE_EXITED){
            mouseOver = false;
            repaint();
        }
    }

    protected void processMouseMotionEvent(MouseEvent e){
        super.processMouseMotionEvent(e);
        if (e.getID() == MouseEvent.MOUSE_MOVED && mouseOver){
            mousePosition = e.getPoint();
            repaint();
        }
    }
}
