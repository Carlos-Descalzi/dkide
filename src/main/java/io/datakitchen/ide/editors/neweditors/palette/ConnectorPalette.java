package io.datakitchen.ide.editors.neweditors.palette;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.RoundedLineBorder;
import io.datakitchen.ide.model.Connector;
import io.datakitchen.ide.model.ConnectorNature;
import io.datakitchen.ide.model.ConnectorType;
import io.datakitchen.ide.ui.ObjectTransferable;
import io.datakitchen.ide.util.JsonUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.List;

public class ConnectorPalette extends JPanel {

    private final DragSource dragSource;
    private ComponentSource componentSource;
    private final JPanel connectors = new JPanel(new VerticalFlowLayout());
    private final ConnectorNature nature;

    public ConnectorPalette() {
        this(null);
    }
    public ConnectorPalette(ConnectorNature nature){
        this.nature = nature;
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Connectors", JLabel.CENTER);
        setBorder(new LineBorder(getBackground().brighter()));
        title.setOpaque(true);
        title.setBackground(getBackground().brighter());
        title.setPreferredSize(new Dimension(80,28));
        add(title, BorderLayout.NORTH);
        add(connectors, BorderLayout.CENTER);
        this.dragSource = new DragSource();
        setPreferredSize(new Dimension(200,200));
    }

    public ComponentSource getComponentSource() {
        return componentSource;
    }

    public void setComponentSource(ComponentSource componentSource) {
        this.componentSource = componentSource;
        if (componentSource != null){
            List<Connector> connectors = ConnectorUtil.getConnectors(componentSource);

            for (Connector connector: connectors){
                if (this.nature == null || this.nature.equals(connector.getConnectorType().getNature())) {
                    addConnector(connector);
                }
            }
        }
        validate();
    }

    private void addConnector(Connector connector) {

        ConnectorType type = connector.getConnectorType();

        JLabel connectorIcon = new JLabel(connector.getName());
        connectorIcon.setIcon(IconLoader.getIcon("/icons/connectors/"+type.getName()+".svg",getClass()));
        connectorIcon.setBorder(new RoundedLineBorder(getBackground().brighter(),10,1));

        String stringContent = JsonUtil.toJsonString(connector.getConfig());

        connectorIcon.setToolTipText("<html><body><pre>"+stringContent+"</pre></body></html>");

        dragSource.createDefaultDragGestureRecognizer(
                connectorIcon,
                DnDConstants.ACTION_COPY,
                dge -> dge.startDrag(
                        Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR),
                        new ObjectTransferable(connector, Connector.FLAVOR)
                )
        );
        connectorIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectors.add(connectorIcon);
    }

}
