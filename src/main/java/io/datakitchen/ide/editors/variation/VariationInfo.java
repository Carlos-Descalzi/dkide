package io.datakitchen.ide.editors.variation;

import io.datakitchen.ide.editors.graph.VariationGraph;
import io.datakitchen.ide.editors.schedule.ScheduleItem;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;
import java.util.Map;

/**
 * This class is used for copying variations between recipes
 */
public class VariationInfo implements Serializable {

    public static final DataFlavor FLAVOR = new DataFlavor(VariationInfo.class, "Variation");

    private final String recipeName;
    private final VariationItem variation;
    private final ScheduleItem scheduleData;
    private final Map<String, Object> overrides;
    private final VariationGraph graph;

    public VariationInfo(String recipeName, VariationItem variation, ScheduleItem scheduleData, Map<String, Object> overrides, VariationGraph graph) {
        this.recipeName = recipeName;
        this.variation = variation;
        this.scheduleData = scheduleData;
        this.overrides = overrides;
        this.graph = graph;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public VariationItem getVariation() {
        return variation;
    }

    public ScheduleItem getScheduleData() {
        return scheduleData;
    }

    public Map<String, Object> getOverrides() {
        return overrides;
    }

    public VariationGraph getGraph() {
        return graph;
    }
}
