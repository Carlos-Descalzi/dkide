package io.datakitchen.ide.editors;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.script.KeyEditor;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.ObjectUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataMapperNotebookEditor extends AbstractNodeEditor {

    private MappingModel mappingsModel;
    private WildcardModel wildcardModel;
    private JBTable mappingsTable;
    private JBTable wildcardsTable;
    private Action addMapping;
    private Action removeMapping;
    private Action addWildcard;
    private Action removeWildcard;
    private Map<String,DsInfo> sourceMap;
    private Map<String,DsInfo> sinkMap;
    private TableModelListener modelListener;
    private ComboBox<DsInfo> dataSourcesSelector;
    private ComboBox<DsInfo> dataSinksSelector;

    public DataMapperNotebookEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    protected Map<String,JComponent> getTabs(){
        mappingsModel = new MappingModel();
        wildcardModel = new WildcardModel();
        mappingsTable = new JBTable();
        wildcardsTable = new JBTable();
        addMapping = new SimpleAction("Add", this::addMapping);
        removeMapping = new SimpleAction("Remove", this::removeMapping);
        addWildcard = new SimpleAction("Add", this::addWildcard);
        removeWildcard = new SimpleAction("Remove", this::removeWildcard);
        sourceMap = new HashMap<>();
        sinkMap = new HashMap<>();
        modelListener = e -> saveDocument();

        JPanel mainTab = new JPanel(new GridLayout(2, 1));

        List<DsInfo> dataSources = DsInfo.loadDsItems(file.getParent(),"data_sources");
        DefaultComboBoxModel<DsInfo> sourcesModel = new DefaultComboBoxModel<>(dataSources.toArray(DsInfo[]::new));
        sourceMap = dataSources.stream().collect(Collectors.toMap(DsInfo::getName, Function.identity()));

        List<DsInfo> dataSinks = DsInfo.loadDsItems(file.getParent(),"data_sinks");
        DefaultComboBoxModel<DsInfo> sinksModel = new DefaultComboBoxModel<>(dataSinks.toArray(DsInfo[]::new));
        sinkMap = dataSinks.stream().collect(Collectors.toMap(DsInfo::getName, Function.identity()));

        dataSourcesSelector = new ComboBox<>(sourcesModel);
        dataSinksSelector = new ComboBox<>(sinksModel);
        {
            JPanel itemMappings = new JPanel(new BorderLayout());
            itemMappings.setPreferredSize(new Dimension(600,300));
            itemMappings.setBorder(new CompoundBorder(new TitledBorder("Mappings"), JBUI.Borders.empty(10)));
            itemMappings.add(new JBScrollPane(mappingsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
            mappingsTable.setAutoCreateColumnsFromModel(false);
            TableColumn c = new TableColumn(0, 200,
                    new DefaultTableCellRenderer(),
                    new DefaultCellEditor(dataSourcesSelector));
            c.setHeaderValue("Source");
            mappingsTable.getColumnModel().addColumn(c);
            c = new TableColumn(1, 200,
                    new DefaultTableCellRenderer(),
                    new KeyEditor(0));
            c.setHeaderValue("Source Key");
            mappingsTable.getColumnModel().addColumn(c);
            c = new TableColumn(2, 200,
                    new DefaultTableCellRenderer(),
                    new DefaultCellEditor(dataSinksSelector));
            c.setHeaderValue("Sink");
            mappingsTable.getColumnModel().addColumn(c);
            c = new TableColumn(3, 200,
                    new DefaultTableCellRenderer(),
                    new KeyEditor(2));
            c.setHeaderValue("Sink Key");
            mappingsTable.getColumnModel().addColumn(c);
            mappingsTable.setModel(mappingsModel);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            itemMappings.add(buttons, BorderLayout.SOUTH);
            buttons.add(new JButton(addMapping));
            buttons.add(new JButton(removeMapping));

            mainTab.add(itemMappings);
        }

        {
            JPanel wildcardMappings = new JPanel(new BorderLayout());
            wildcardMappings.setPreferredSize(new Dimension(600,300));
            wildcardMappings.setBorder(new CompoundBorder(new TitledBorder( "Wildcard Mappings"), JBUI.Borders.empty(10)));
            wildcardMappings.add(new JBScrollPane(wildcardsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

            wildcardsTable.setAutoCreateColumnsFromModel(false);
            TableColumn c = new TableColumn(0, 200,
                    new DefaultTableCellRenderer(),
                    new DefaultCellEditor(dataSourcesSelector));
            c.setHeaderValue("Source");
            wildcardsTable.getColumnModel().addColumn(c);
            c = new TableColumn(1, 200,
                    new DefaultTableCellRenderer(),
                    new DefaultCellEditor(dataSinksSelector));
            c.setHeaderValue("Sink");
            wildcardsTable.getColumnModel().addColumn(c);
            wildcardsTable.setModel(wildcardModel);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            wildcardMappings.add(buttons, BorderLayout.SOUTH);
            buttons.add(new JButton(addWildcard));
            buttons.add(new JButton(removeWildcard));

            mainTab.add(wildcardMappings);

        }

        updateActions();
        mappingsModel.addTableModelListener(this.modelListener);
        mappingsTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        wildcardsTable.getSelectionModel().addListSelectionListener(e -> updateActions());

        Map<String, JComponent> tabs = new HashMap<>();

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT));
        content.add(mainTab);
        mainTab.setBorder(JBUI.Borders.empty(10));

        tabs.put("Mappings", content);
        return tabs;
    }

    @Override
    public void selectNotify() {
        super.selectNotify();
        checkValid();
    }

    private void checkValid() {
        List<DsInfo> dataSources = DsInfo.loadDsItems(file.getParent(),"data_sources");
        DefaultComboBoxModel<DsInfo> sourcesModel = new DefaultComboBoxModel<>(dataSources.toArray(DsInfo[]::new));
        sourceMap = dataSources.stream().collect(Collectors.toMap(DsInfo::getName, Function.identity()));
        dataSourcesSelector.setModel(sourcesModel);

        List<DsInfo> dataSinks = DsInfo.loadDsItems(file.getParent(),"data_sinks");
        DefaultComboBoxModel<DsInfo> sinksModel = new DefaultComboBoxModel<>(dataSinks.toArray(DsInfo[]::new));
        sinkMap = dataSinks.stream().collect(Collectors.toMap(DsInfo::getName, Function.identity()));
        dataSinksSelector.setModel(sinksModel);

        mappingsModel.removeInvalid(sourceMap, sinkMap);
        wildcardModel.removeInvalid(sourceMap, sinkMap);
    }

    public @NotNull VirtualFile getFile(){
        return file;
    }


    private void addMapping(ActionEvent e) {
        mappingsModel.addFile();
        updateActions();
    }

    private void removeMapping(ActionEvent e) {
        int index = mappingsTable.getSelectedRow();
        if (index != -1){
            mappingsModel.removeFile(index);
        }
        updateActions();
    }

    private void addWildcard(ActionEvent event) {
        wildcardModel.addWildcard();
        updateActions();
    }

    private void removeWildcard(ActionEvent event) {
        int index = wildcardsTable.getSelectedRow();
        if (index != -1){
            wildcardModel.removeWildcard(index);
        }
        updateActions();
    }

    private void updateActions(){
        boolean canLoad = !(sourceMap.isEmpty() || sinkMap.isEmpty());
        addMapping.setEnabled(canLoad);
        removeMapping.setEnabled(canLoad && mappingsModel.getRowCount() > 0);
        addWildcard.setEnabled(canLoad);
        removeWildcard.setEnabled(canLoad && wildcardModel.getRowCount() > 0);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Data Mapper Editor";
    }

    protected void doLoadDocument(Map<String,Object> notebook){
        this.mappingsModel.removeTableModelListener(this.modelListener);
        this.wildcardModel.removeTableModelListener(this.modelListener);
        try {
            Map<String,Object> mappingsObj = ObjectUtil.cast(notebook.get("mappings"));

            List<Mapping> mappings = new ArrayList<>();
            if (mappingsObj != null) {
                mappings.addAll(mappingsObj
                        .values()
                        .stream()
                        .map(o -> Mapping.fromJson(ObjectUtil.cast(o),sourceMap, sinkMap))
                        .collect(Collectors.toList()));
            }
            this.mappingsModel.setMappings(mappings);

            List<Map<String,Object>> wildcardsObj = ObjectUtil.cast(notebook.get("wildcard-will-automatically-create-mappings"));

            List<WildcardMapping> wildcardMappings = new ArrayList<>();
            if (wildcardsObj != null) {
                wildcardMappings.addAll(wildcardsObj
                        .stream()
                        .map((Map<String,Object>o)-> WildcardMapping.fromJson(o, sourceMap, sinkMap))
                        .collect(Collectors.toList()));
            }
            this.wildcardModel.setMappings(wildcardMappings);


        }catch (Exception ex){
            ex.printStackTrace();
        }

        this.mappingsModel.addTableModelListener(this.modelListener);
        this.wildcardModel.addTableModelListener(this.modelListener);
    }

    protected void doSaveDocument(Map<String,Object> notebook){

        notebook.put("name","mappings");

        Map<String,Object> mappingsObj = new LinkedHashMap<>();

        List<Mapping> mappings = mappingsModel.getValidMappings();
        if (mappings.size() > 0) {
            int i = 1;
            for (Mapping mapping : mappings) {
                mappingsObj.put("mapping" + (i++), mapping.toJsonObject());
            }

            notebook.put("mappings", mappingsObj);
        }
        List<WildcardMapping> wildcardMappings = wildcardModel.getValidMappings();
        if (wildcardMappings.size() > 0){
            List<Map<String,Object>> array = wildcardMappings
                    .stream()
                    .map(WildcardMapping::toJsonObject)
                    .collect(Collectors.toList());
            notebook.put("wildcard-will-automatically-create-mappings",array);
        }
    }


    private static class Mapping{
        private DsInfo source;
        private String sourceKey;
        private DsInfo sink;
        private String sinkKey;

        public Mapping(){
        }

        public static Mapping fromJson(Map<String, Object> mappingObj, Map<String, DsInfo> sourceMap, Map<String, DsInfo> sinkMap){
            Mapping mapping = new Mapping();
            mapping.setSource(sourceMap.get((String)mappingObj.get("source-name")));
            mapping.setSourceKey((String) mappingObj.get("source-key"));
            mapping.setSink(sinkMap.get((String)mappingObj.get("sink-name")));
            mapping.setSinkKey((String) mappingObj.get("sink-key"));
            return mapping;
        }

        public DsInfo getSource() {
            return source;
        }

        public void setSource(DsInfo source) {
            this.source = source;
        }

        public String getSourceKey() {
            return sourceKey;
        }

        public void setSourceKey(String sourceKey) {
            this.sourceKey = sourceKey;
        }

        public DsInfo getSink() {
            return sink;
        }

        public void setSink(DsInfo sink) {
            this.sink = sink;
        }

        public String getSinkKey() {
            return sinkKey;
        }

        public void setSinkKey(String sinkKey) {
            this.sinkKey = sinkKey;
        }

        public boolean isValid(){
            return source != null
                    && sink != null
                    && sourceKey != null
                    && sinkKey != null;
        }

        public Map<String,Object> toJsonObject(){
            Map<String,Object> obj = new LinkedHashMap<>();
            obj.put("source-name",source.getName());
            obj.put("source-key",sourceKey);
            obj.put("sink-name",sink.getName());
            obj.put("sink-key",sinkKey);
            return obj;
        }

    }

    private static class MappingModel extends AbstractTableModel {

        private final String[] COLUMN_NAMES = {"Source", "source key", "Sink", "sink key"};

        protected List<Mapping> mappings = new ArrayList<>();

        public void setMappings(List<Mapping> mappings){
            this.mappings = mappings;
            fireTableDataChanged();
        }
        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        public void removeInvalid(Map<String, DsInfo> sourceMap, Map<String, DsInfo> sinkMap) {
            Set<Integer> indexes = new LinkedHashSet<>();

            for (int i=0;i<mappings.size();i++){
                Mapping mapping = mappings.get(i);
                DsInfo source = sourceMap.get(mapping.getSource().getName());
                DsInfo sink = sinkMap.get(mapping.getSink().getName());
                if (source == null){
                    indexes.add(i);
                } else if (source.getKeys().contains(mapping.getSourceKey())){
                    indexes.add(i);
                } else {
                    mapping.setSource(source);
                }
                if (sink == null){
                    indexes.add(i);
                } else if (sink.getKeys().contains(mapping.getSinkKey())){
                    indexes.add(i);
                } else {
                    mapping.setSink(sink);
                }
            }

            for (int index: indexes){
                mappings.remove(index);
                fireTableRowsDeleted(index,index);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            Mapping mapping = mappings.get(rowIndex);
            switch(columnIndex){
                case 1:
                    return mapping.source != null;
                case 3:
                    return mapping.sink != null;
                default:
                    return true;
            }

        }

        public void addFile(){
            int index = mappings.size();
            mappings.add(new Mapping());
            fireTableRowsInserted(index,index);
        }

        public void removeFile(int index){
            mappings.remove(index);
            fireTableRowsDeleted(index,index);
        }


        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Mapping mapping = mappings.get(rowIndex);
            switch (columnIndex){
                case 0:
                    return mapping.getSource();
                case 1:
                    return mapping.getSourceKey();
                case 2:
                    return mapping.getSink();
                case 3:
                    return mapping.getSinkKey();
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Mapping mapping = mappings.get(rowIndex);
            switch(columnIndex){
                case 0:
                    mapping.setSource((DsInfo)aValue);
                    break;
                case 1:
                    mapping.setSourceKey((String)aValue);
                    break;
                case 2:
                    mapping.setSink((DsInfo)aValue);
                    break;
                case 3:
                    mapping.setSinkKey((String)aValue);
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        private List<Mapping> getValidMappings(){
            return mappings.stream().filter(Mapping::isValid).collect(Collectors.toList());
        }

    }

    private static class WildcardModel extends AbstractTableModel {
        private final String[] COLUMN_NAMES = {"Source","Sink"};
        List<WildcardMapping> mappings = new ArrayList<>();

        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            WildcardMapping mapping = mappings.get(rowIndex);
            switch(columnIndex){
                case 0:
                    return mapping.getSource();
                case 1:
                    return mapping.getSink();
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            WildcardMapping mapping = mappings.get(rowIndex);
            switch(columnIndex){
                case 0:
                    mapping.setSource((DsInfo) aValue);
                    break;
                case 1:
                    mapping.setSink((DsInfo) aValue);
                    break;
            }
            fireTableCellUpdated(rowIndex,columnIndex);
        }

        public List<WildcardMapping> getValidMappings() {
            return mappings.stream().filter(WildcardMapping::isValid).collect(Collectors.toList());
        }

        public void addWildcard() {
            int index = mappings.size();
            mappings.add(new WildcardMapping());
            fireTableRowsInserted(index,index);
        }

        public void removeWildcard(int index) {
            mappings.remove(index);
            fireTableRowsDeleted(index,index);
        }

        public void setMappings(List<WildcardMapping> wildcardMappings) {
            mappings = wildcardMappings;
            fireTableDataChanged();
        }

        public void removeInvalid(Map<String, DsInfo> sourceMap, Map<String, DsInfo> sinkMap) {
            Set<Integer> indexes = new LinkedHashSet<>();

            for (int i=0;i<mappings.size();i++){
                WildcardMapping mapping = mappings.get(i);

                DsInfo source = sourceMap.get(mapping.getSource().getName());
                if (source == null){
                    indexes.add(i);
                }
                DsInfo sink = sinkMap.get(mapping.getSink().getName());
                if (sink == null){
                    indexes.add(i);
                }
            }

            for (int index: indexes){
                mappings.remove(index);
                fireTableRowsDeleted(index, index);
            }
        }
    }

    private static class WildcardMapping {
        private DsInfo source;
        private DsInfo sink;


        public DsInfo getSource() {
            return source;
        }

        public void setSource(DsInfo source) {
            this.source = source;
        }

        public DsInfo getSink() {
            return sink;
        }

        public void setSink(DsInfo sink) {
            this.sink = sink;
        }

        public Map<String,Object> toJsonObject() {
            Map<String,Object> obj = new LinkedHashMap<>();
            obj.put("data-source",source.getName());
            obj.put("data-sink",sink.getName());
            return obj;
        }

        public boolean isValid() {
            return source != null && sink != null;
        }

        public static WildcardMapping fromJson(Map<String, Object> wildcardObj, Map<String, DsInfo> sourceMap,Map<String, DsInfo> sinkMap) {
            WildcardMapping mapping = new WildcardMapping();

            mapping.setSource(sourceMap.get((String)wildcardObj.get("data-source")));
            mapping.setSink(sinkMap.get((String)wildcardObj.get("data-sink")));

            return mapping;
        }
    }

    @Override
    protected void disableEvents() {

    }

    @Override
    protected void enableEvents() {

    }
}
