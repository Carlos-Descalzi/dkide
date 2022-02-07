package io.datakitchen.ide.editors.graph.paste;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.util.Set;

public interface PasteHandler {

    enum Target {
        GRAPH,
        NODE
    }

    default Set<Target> getTargets(){return Set.of(Target.values());};

    boolean isTransferableSupported(Transferable transferable, Target target);

    void accept(Transferable transferable, JComponent targetComponent);
}
