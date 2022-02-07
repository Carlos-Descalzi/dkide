package io.datakitchen.ide.dialogs;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorUtil;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.FormLayout;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeFromSQLFileDialog extends DialogWrapper {

    public enum Option {
        CREATE_ACTION,
        CREATE_MAPPER
    }
    private final CardLayout mainPanelLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(mainPanelLayout);

    private Option option = null;

    private final JTextField nodeName = new JTextField();

    private final ComboBox<Connector> dataSource = new ComboBox<>();
    private final ComboBox<DataSourceType> dataSourceType = new ComboBox<>();
    private final ComboBox<Connector> dataSink = new ComboBox<>();
    private final ComboBox<DataSinkType> dataSinkType = new ComboBox<>();
    private final File file;
    private final ComponentSource componentSource;

    public NodeFromSQLFileDialog(File file, ComponentSource componentSource) {
        super(true);
        this.file = file;
        setTitle("Create New Node");
        this.componentSource = componentSource;
        init();
    }

    private static final String OPTION_NODE = "nodeOption";
    private static final String OPTION_SOURCE = "sourceOption";
    private static final String OPTION_SINK = "sinkOption";
    private static final String OPTION_NAME = "nodeNameOption";

    @Override
    protected @Nullable JComponent createCenterPanel() {
        mainPanel.add(buildNodeOptionPanel(),OPTION_NODE);
        mainPanel.add(buildSourceOptionPanel(), OPTION_SOURCE);
        mainPanel.add(buildSinkOptionsPanel(), OPTION_SINK);
        mainPanel.add(buildNodeNamePanel(), OPTION_NAME);

        String nodeName = file.getName();
        nodeName = nodeName.substring(0, nodeName.indexOf('.')).toLowerCase();

        this.nodeName.setText(nodeName);
        mainPanel.setPreferredSize(new Dimension(500,300));
        showNodeOption();
        return mainPanel;
    }

    private JComponent buildNodeOptionPanel(){
        JPanel nodeOptionPanel = new FormPanel();

        JButton createActionOption = new JButton(
                new SimpleAction("Execute DML or DDL Statements in a database",
                        this::doCreateAction));
        JButton createMapperOption = new JButton(new SimpleAction(
                "Extract data from a database to move it to another location", this::doCreateMapper));

        createActionOption.setContentAreaFilled(false);
        createActionOption.setBorderPainted(false);
        createActionOption.setHorizontalAlignment(SwingConstants.LEADING);
        createActionOption.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createActionOption.setBorder(JBUI.Borders.empty(20, 15, 0, 0));
        createActionOption.setIcon(AllIcons.General.ArrowRight);

        createMapperOption.setContentAreaFilled(false);
        createMapperOption.setBorderPainted(false);
        createMapperOption.setHorizontalAlignment(SwingConstants.LEADING);
        createMapperOption.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createMapperOption.setBorder(JBUI.Borders.empty(20, 15, 0, 0));
        createMapperOption.setIcon(AllIcons.General.ArrowRight);

        JPanel options = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.MIDDLE,true, false));
        options.add(createActionOption);
        options.add(createMapperOption);

        nodeOptionPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, true, false));
        nodeOptionPanel.add(new JLabel("Choose the type of action to perform with this SQL file"), BorderLayout.NORTH);
        nodeOptionPanel.add(options, BorderLayout.CENTER);

        return nodeOptionPanel;
    }

    private JComponent buildNodeNamePanel() {
        JPanel nodeNamePanel = new JPanel(new BorderLayout());
        nodeNamePanel.setBorder(UIUtil.EMPTY_BORDER_5x5);
        nodeNamePanel.add(new JLabel("Enter the node name"), BorderLayout.NORTH);

        FormPanel panel = new FormPanel();
        panel.setBorder(JBUI.Borders.empty(20, 15, 0, 0));
        panel.addField("Node name", nodeName);
        nodeNamePanel.add(panel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JButton(new SimpleAction("<< Back", this::prevFromName)));
        nodeNamePanel.add(bottomPanel, BorderLayout.SOUTH);

        return nodeNamePanel;
    }

    private JComponent buildSourceOptionPanel(){

        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setBorder(UIUtil.EMPTY_BORDER_5x5);
        optionPanel.add(new JLabel("Select the type of connection to create"), BorderLayout.NORTH);

        FormPanel contents = new FormPanel();
        contents.setLayout(new FormLayout(10,20));
        contents.setLabelDimension(new Dimension(200,28));
        contents.setBorder(JBUI.Borders.empty(20, 15, 0, 0));
        contents.addField("Choose connector", dataSource);
        contents.addField("Or a connector type", dataSourceType);

        optionPanel.add(contents, BorderLayout.CENTER);
        List<Connector> connectors = new ArrayList<>();
        connectors.add(null);

        connectors.addAll(ConnectorUtil.getConnectors(componentSource)
                .stream()
                .filter(c -> c.getConnectorType().getNature() == ConnectorNature.SQL)
                .collect(Collectors.toList()));

        dataSource.setModel(new DefaultComboBoxModel<>(connectors.toArray(Connector[]::new)));

        dataSource.setRenderer(new ConnectorRenderer());

        List<DataSourceType> dsTypes = new ArrayList<>();
        dsTypes.add(null);
        dsTypes.addAll(Arrays.stream(DataSourceType.values())
                .filter(d -> d.getConnectorType().getNature() == ConnectorNature.SQL)
                .collect(Collectors.toList()));

        dataSourceType.setModel(new DefaultComboBoxModel<>(dsTypes.toArray(DataSourceType[]::new)));

        dataSourceType.setRenderer(new DataTypeRenderer());
        ActionListener listener = this::updateActions;
        dataSource.addActionListener(listener);
        dataSourceType.addActionListener(listener);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JButton(new SimpleAction("<< Back", this::prevFromSource)));
        bottomPanel.add(new JButton(new SimpleAction("Next >>", this::nextFromSource)));

        optionPanel.add(bottomPanel, BorderLayout.SOUTH);

        return optionPanel;
    }

    private JComponent buildSinkOptionsPanel(){

        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setBorder(UIUtil.EMPTY_BORDER_5x5);
        optionPanel.add(new JLabel("Select the type of sink connection to create"), BorderLayout.NORTH);

        FormPanel contents = new FormPanel();
        contents.setLayout(new FormLayout(10,20));
        contents.setLabelDimension(new Dimension(200,28));
        contents.setBorder(JBUI.Borders.empty(20, 15, 0, 0));
        contents.addField("Choose sink connector", dataSink);
        contents.addField("Or a sink connector type", dataSinkType);

        optionPanel.add(contents, BorderLayout.CENTER);

        List<Connector> connectors = new ArrayList<>();
        connectors.add(null);
        connectors.addAll(ConnectorUtil.getConnectors(componentSource));

        dataSink.setModel(new DefaultComboBoxModel<>(connectors.toArray(Connector[]::new)));
        dataSink.setRenderer(new ConnectorRenderer());

        List<DataSinkType> dsTypes = new ArrayList<>();
        dsTypes.add(null);
        dsTypes.addAll(List.of(DataSinkType.values()));

        dataSinkType.setModel(new DefaultComboBoxModel<>(dsTypes.toArray(DataSinkType[]::new)));
        dataSinkType.setRenderer(new DataTypeRenderer());
        ActionListener listener = this::updateActions;
        dataSink.addActionListener(listener);
        dataSinkType.addActionListener(listener);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JButton(new SimpleAction("<< Back", this::prevFromSink)));
        bottomPanel.add(new JButton(new SimpleAction("Next >>", this::nextFromSink)));

        optionPanel.add(bottomPanel, BorderLayout.SOUTH);

        return optionPanel;
    }

    private void showNameOption() {
        mainPanelLayout.show(mainPanel, OPTION_NAME);
    }

    private void showNodeOption() {
        option = null;
        mainPanelLayout.show(mainPanel, OPTION_NODE);
    }

    private void showSourceOptionPanel() {
        mainPanelLayout.show(mainPanel, OPTION_SOURCE);
    }

    private void showSinkOptionPanel() {
        mainPanelLayout.show(mainPanel, OPTION_SINK);
    }

    private void doCreateAction(ActionEvent event) {
        option = Option.CREATE_ACTION;
        showSourceOptionPanel();
    }

    private void doCreateMapper(ActionEvent event) {
        option = Option.CREATE_MAPPER;
        showSourceOptionPanel();
    }

    private void prevFromName(ActionEvent event) {
        if (option == Option.CREATE_ACTION) {
            showSourceOptionPanel();
        } else {
            showSinkOptionPanel();
        }
    }

    private void nextFromSource(ActionEvent event) {
        if (option == Option.CREATE_ACTION) {
            showNameOption();
        } else {
            showSinkOptionPanel();
        }
    }

    private void prevFromSource(ActionEvent event) {
        showNodeOption();
    }

    private void nextFromSink(ActionEvent event) {
        showNameOption();
    }

    private void prevFromSink(ActionEvent event) {
        showSourceOptionPanel();
    }

    private void updateActions(ActionEvent event) {
        dataSourceType.setEnabled(dataSource.getItem() == null);
        dataSinkType.setEnabled(dataSink.getItem() == null);
    }

    public Option getOption(){
        return option;
    }

    public Connector getDataSource(){
        return dataSource.getItem();
    }

    public DataSourceType getDataSourceType(){
        return dataSourceType.getItem();
    }

    public Connector getDataSink(){ return dataSink.getItem();}

    public DataSinkType getDataSinkType(){
        return dataSinkType.getItem();
    }

    public String getName() {
        return nodeName.getText();
    }

    private static class DataTypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                DataType c = (DataType)value;
                l.setIcon(IconLoader.getIcon("/icons/connectors/"+c.getConnectorType().getName()+"_small.svg",getClass()));
                l.setText(c.getConnectorType().getName());
            } else {
                l.setText(" ");
            }
            return l;
        }
    }

    private static class ConnectorRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                Connector c = (Connector)value;
                l.setIcon(IconLoader.getIcon("/icons/connectors/"+c.getConnectorType().getName()+"_small.svg",getClass()));
                l.setText(c.getName());
            } else {
                l.setText(" ");
            }
            return l;
        }
    }
}
