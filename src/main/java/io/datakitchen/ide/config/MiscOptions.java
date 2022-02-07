package io.datakitchen.ide.config;

import java.io.Serializable;

public class MiscOptions implements Serializable {
    private static final long serialVersionUID = 1;

    private boolean useCustomForms = true;
    private boolean hideInactiveNodes = false;
    private boolean hideConfigJsonOnForms = false;
    private boolean customDsFormsEnabled = true;
    private boolean customNodeFormsEnabled = true;
    private boolean simplifiedView = true;

    public boolean isUseCustomForms() {
        return useCustomForms;
    }

    public void setUseCustomForms(boolean useCustomForms) {
        this.useCustomForms = useCustomForms;
    }

    public boolean isHideInactiveNodes() {
        return hideInactiveNodes;
    }

    public void setHideInactiveNodes(boolean hideInactiveNodes) {
        this.hideInactiveNodes = hideInactiveNodes;
    }

    public boolean isHideConfigJsonOnForms() {
        return hideConfigJsonOnForms;
    }

    public void setHideConfigJsonOnForms(boolean hideConfigJsonOnForms) {
        this.hideConfigJsonOnForms = hideConfigJsonOnForms;
    }

    public boolean isCustomDsFormsEnabled() {
        return customDsFormsEnabled;
    }

    public void setCustomDsFormsEnabled(boolean customDsFormsEnabled) {
        this.customDsFormsEnabled = customDsFormsEnabled;
    }

    public boolean isCustomNodeFormsEnabled() {
        return customNodeFormsEnabled;
    }

    public void setCustomNodeFormsEnabled(boolean customNodeFormsEnabled) {
        this.customNodeFormsEnabled = customNodeFormsEnabled;
    }

    public boolean isSimplifiedView() {
        return simplifiedView;
    }

    public void setSimplifiedView(boolean simplifiedView) {
        this.simplifiedView = simplifiedView;
    }

}
