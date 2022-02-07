package io.datakitchen.ide.editors.neweditors;

import javax.swing.*;
import java.awt.*;

public interface FileView {

    JComponent asComponent();
    Point getHookPoint();
}
