package io.datakitchen.ide.tools;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.service.LibraryService;
import io.datakitchen.ide.service.LibraryServiceEvent;
import io.datakitchen.ide.service.LibraryServiceListener;
import io.datakitchen.ide.ui.VerticalStackLayout;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NodePalette extends JPanel implements DragGestureListener, ActionListener, Disposable, LibraryServiceListener {

    private final JPanel nodes = new JPanel(new VerticalStackLayout());
    private final DragSource dragSource = new DragSource();
    private final JButton expander = new JButton("");
    private final JPanel content = new JPanel(new BorderLayout());

    private final Project project;

    public NodePalette(Project project){
        this.project = project;
        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);

        JScrollPane scroll = new JBScrollPane(nodes, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(scroll, BorderLayout.CENTER);

        JPanel expanderArea = new JPanel(new FlowLayout());
        expanderArea.setPreferredSize(new Dimension(15,300));
        expanderArea.add(expander);
        expander.setPreferredSize(new Dimension(13,13));
        expander.addActionListener(this);
        expander.setIcon(AllIcons.General.ArrowLeft);
        expander.setFocusPainted(false);
        expander.setBorderPainted(false);
        add(expander,BorderLayout.WEST);

        loadPalette();
        content.setVisible(false);
        setPreferredSize(new Dimension(15,100));
        LibraryService.getInstance(project).addLibraryServiceListener(this);
    }

    private void toggleVisible() {
        content.setVisible(!content.isVisible());
        if (content.isVisible()){
            expander.setIcon(AllIcons.General.ArrowRight);
            setPreferredSize(nodes.getPreferredSize());
        } else {
            expander.setIcon(AllIcons.General.ArrowLeft);
            setPreferredSize(new Dimension(15,100));
        }
        validate();
    }

    private void loadPalette() {
        Map<String, List<String>> libraryNodes = LibraryService
                .getInstance(project)
                .getLibraryNodes();

        for (Map.Entry<String, List<String>> entry: libraryNodes.entrySet()){
            addNodes(entry.getKey(), entry.getValue());
        }

    }

    private void addNodes(String key, List<String> value) {
        nodes.add(new Section(key, value));
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        Node node = (Node)dge.getComponent();
        dge.startDrag(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), new NodeReference(node.path));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toggleVisible();
    }

    @Override
    public void dispose() {
        LibraryService.getInstance(project).removeLibraryServiceListener(this);
    }

    @Override
    public void libraryAdded(LibraryServiceEvent event) {

    }

    private class Section extends JPanel {
        public Section(String recipe, List<String> nodes){
            setLayout(new BorderLayout());
            File recipeFolder = new File(recipe);
            JLabel label =new JLabel(recipeFolder.getName(), JLabel.CENTER);
            label.setOpaque(true);
            label.setBackground(getBackground().brighter());
            add(label, BorderLayout.NORTH);
            setBorder(JBUI.Borders.emptyBottom(15));
            JPanel content = new JPanel();
            nodes.sort(Comparator.naturalOrder());
            for (String node: nodes){
                try {
                    content.add(createNode(new File(recipeFolder, node).getAbsolutePath()));
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
            content.setLayout(new VerticalStackLayout());
            add(content, BorderLayout.CENTER);
        }
    }

    private Node createNode(String path) throws Exception{
        File descriptionFile = new File(path, Constants.FILE_DESCRIPTION_JSON);
        if (!descriptionFile.exists()){
            throw new Exception("File not found");
        }
        try (InputStream input = new FileInputStream(descriptionFile)) {
            Map<String, Object> descriptionJson = JsonUtil.read(input);

            String type = (String) descriptionJson.get("type");
            String description = (String) descriptionJson.get("description");

            return new Node(descriptionFile.getParentFile().getName(), type, description, path);
        }
    }

    private class Node extends JLabel {

        private static final int ICON_WIDTH = 50;

        private final String name;
        private final String path;
        private final Icon icon;

        public Node(String name, String type, String description, String path){
            this.name = name;
            this.path = path;
            this.icon = IconLoader.getIcon("/icons/" + type + "_big.svg",getClass());
            setToolTipText(
                "<html><pre>"
                +(StringUtils.isNotBlank(description) ? description : name)
                +"</pre></html>"
            );
            enableEvents(AWTEvent.MOUSE_EVENT_MASK| AWTEvent.MOUSE_MOTION_EVENT_MASK);
            dragSource.createDefaultDragGestureRecognizer(
                    this,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    NodePalette.this
            );
            setPreferredSize(
                    new Dimension(
                            Math.max(80, getFontMetrics(getFont()).stringWidth(name)+20),
                            80)
            );
        }

        protected void paintComponent(Graphics g){
            Graphics2D g2d = (Graphics2D)g;

            int width = getWidth();

            g.setColor(Color.WHITE);
            g.fillArc((width-ICON_WIDTH) / 2, 5, ICON_WIDTH, ICON_WIDTH, 0, 360);

            g.setColor(Color.BLACK);

            if (this.icon != null){
                this.icon.paintIcon(this,g2d,(width-ICON_WIDTH+2)/2,7);
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            g.drawArc((width-ICON_WIDTH) / 2, 5, ICON_WIDTH, ICON_WIDTH, 0, 360);

            FontMetrics m = getFontMetrics(getFont());

            int w = m.stringWidth(name);

            g.setColor(getForeground());

            g.drawString(name, (getWidth()-w)/2, 50 + m.getHeight()+10);
        }


    }
}
