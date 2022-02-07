package io.datakitchen.ide.editors.neweditors.variation;

import io.datakitchen.ide.editors.graph.VariationGraph;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.NamedObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;

public class VariationInfo implements NamedObject {

    public static class Schedule {
        private String schedule;
        private String timeZone;
        private Integer ram;
        private Integer disk;

        public Schedule(String schedule, String timeZone, Integer ram, Integer disk) {
            this.schedule = schedule;
            this.timeZone = timeZone;
            this.ram = ram;
            this.disk = disk;
        }

        public String getSchedule() {
            return schedule;
        }

        public void setSchedule(String schedule) {
            this.schedule = schedule;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public Integer getRam() {
            return ram;
        }

        public void setRam(Integer ram) {
            this.ram = ram;
        }

        public Integer getDisk() {
            return disk;
        }

        public void setDisk(Integer disk) {
            this.disk = disk;
        }

        public Map<String, Object> toJson() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("schedule", schedule);
            json.put("scheduleTimeZone", timeZone);
            if (ram != null) {
                json.put("max-ram", ram);
            }
            if (disk != null){
                json.put("max-disk", disk);
            }
            return json;
        }
    }

    public static class IngredientVariable {
        private String name;
        private String displayName;
        private String type;
        private boolean required;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Map<String, Object> toJson(){
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("name", name);
            result.put("display-name", displayName);
            result.put("type", type);
            return result;
        }

        public static IngredientVariable fromJson(Map<String, Object> json){
            IngredientVariable variable = new IngredientVariable();
            variable.setName((String)json.get("name"));
            variable.setDisplayName((String)json.get("display-name"));
            variable.setType((String)json.get("type"));

            return variable;
        }
    }

    public static class Ingredient {
        private String name;
        private String description;
        private String shortDescription;
        private String rollbackIngredient;
        private List<IngredientVariable> requiredVariables = new ArrayList<>();
        private List<String> applyVariables = new ArrayList<>();

        public Ingredient(String name){
            this.name = name;
        }

        public Ingredient(
                String name,
                String description,
                String shortDescription,
                String rollbackIngredient,
                List<IngredientVariable> requiredVariables,
                List<String> applyVariables) {
            this.name = name;
            this.description = description;
            this.shortDescription = shortDescription;
            this.rollbackIngredient = rollbackIngredient;
            this.requiredVariables = requiredVariables;
            this.applyVariables = applyVariables;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public void setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
        }

        public String getRollbackIngredient() {
            return rollbackIngredient;
        }

        public void setRollbackIngredient(String rollbackIngredient) {
            this.rollbackIngredient = rollbackIngredient;
        }

        public List<IngredientVariable> getRequiredVariables() {
            return requiredVariables;
        }

        public void setRequiredVariables(List<IngredientVariable> requiredVariables) {
            this.requiredVariables = requiredVariables;
        }

        public List<String> getApplyVariables() {
            return applyVariables;
        }

        public void setApplyVariables(List<String> applyVariables) {
            this.applyVariables = applyVariables;
        }

        public Map<String, Object> toJson() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("ingredient-name", name);
            json.put("description", description);
            json.put("short-description", shortDescription);
            json.put("rollback-ingredient", rollbackIngredient);
            json.put("required-recipe-variables", requiredVariables.stream().map(IngredientVariable::toJson).collect(Collectors.toList()));
            json.put("apply-runtime-recipe-variables", applyVariables);
            return json;
        }
    }

    private final EventSupport<PropertyChangeListener> listeners = EventSupport.of(PropertyChangeListener.class);

    private String name;
    private VariationGraph variationGraph;
    private Schedule schedule;
    private Ingredient ingredient;
    private Set<String> overrideSets = new LinkedHashSet<>();
    private String description;

    public VariationInfo(){}

    public VariationInfo(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        listeners.getProxy().propertyChange(new PropertyChangeEvent(this, "name", oldName, name));
    }

    public VariationGraph getVariationGraph() {
        return variationGraph;
    }

    public void setVariationGraph(VariationGraph variationGraph) {
        this.variationGraph = variationGraph;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        Schedule oldSchedule = this.schedule;
        this.schedule = schedule;
        listeners.getProxy().propertyChange(new PropertyChangeEvent(this, "schedule", oldSchedule, schedule));
    }

    public Set<String> getOverrideSets() {
        return overrideSets;
    }

    public void setOverrideSets(Set<String> overrideSets) {
        Set<String> oldOverrideSets = this.overrideSets;
        this.overrideSets = overrideSets;
        listeners.getProxy().propertyChange(new PropertyChangeEvent(this, "overrideSets", oldOverrideSets, overrideSets));
    }

    public String toString(){
        return name;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        Ingredient oldIngredient = this.ingredient;
        this.ingredient = ingredient;
        listeners.getProxy().propertyChange(new PropertyChangeEvent(this, "ingredient", oldIngredient, ingredient));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        listeners.addListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        listeners.removeListener(listener);
    }
}
