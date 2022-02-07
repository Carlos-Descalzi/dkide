package io.datakitchen.ide.editors;

import java.util.EventListener;

public interface VariationEditionListener extends EventListener {
    void variationItemAdded(VariationEditionEvent event);
    void variationItemRemoved(VariationEditionEvent event);
    void variationItemChanged(VariationEditionEvent event);
}
