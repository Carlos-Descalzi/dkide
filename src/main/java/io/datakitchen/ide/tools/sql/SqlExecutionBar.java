package io.datakitchen.ide.tools.sql;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorUtil;
import io.datakitchen.ide.model.Connector;
import io.datakitchen.ide.model.ConnectorNature;
import io.datakitchen.ide.service.SQLRunnerService;
import io.datakitchen.ide.tools.DatabaseConfiguration;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.ToolWindowUtil;
import io.datakitchen.ide.views.SQLResultSetView;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SqlExecutionBar extends JPanel {

    public interface StatementSource {
        Promise<String> getSql();
    }

    private Action executeSqlAction = new SimpleAction("Execute query",this::executeQuery);
    private final JComboBox connections = new ComboBox();
    private final StatementSource statementSource;
    private final Project project;

    public SqlExecutionBar(Project project, StatementSource statementSource, ComponentSource componentSource){
        super(new FlowLayout(FlowLayout.RIGHT));
        this.project = project;
        this.statementSource = statementSource;

        List<Object> allConnections = new ArrayList<>();

        allConnections.addAll(ConnectorUtil.getConnectors(componentSource)
                .stream().filter(c -> c.getConnectorType().getNature() == ConnectorNature.SQL)
                .collect(Collectors.toList()));

        allConnections.addAll(ConfigurationService.getInstance(project)
                .getConnections()
                .stream().map(DatabaseConfiguration::getName)
                .collect(Collectors.toList()));

        connections.setModel(new DefaultComboBoxModel(allConnections.toArray()));
        connections.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof String){
                    l.setText((String)value);
                } else if (value instanceof Connector) {
                    l.setText(((Connector)value).getName());
                } else {
                    l.setText("");
                }

                return l;
            }
        });
        add(new JLabel("Connection:"));
        add(connections);
        add(new JButton(executeSqlAction));
    }

    private ResultSet execute(SQLRunnerService service, Object target, String sql) throws SQLException {
        if (target instanceof String){
            return service.execute((String)target, sql);
        } else {
            return service.execute((Connector) target, sql);
        }
    }


    private void executeQuery(ActionEvent event) {
        statementSource.getSql().onSuccess(sql -> {
            SQLRunnerService service = SQLRunnerService.getInstance(project);
            new Thread(()->{
                try {
                    ResultSet result = execute(service, connections.getSelectedItem(), sql);

                    JComponent view = result == null
                            ? createSQLMessageView("Statement executed successfully")
                            : new SQLResultSetView(result);

                    showView(view, "SQL Result");
                }catch (SQLException ex){
                    showView(createSQLErrorView(ex), "SQL Error");
                    ex.printStackTrace();
                }
            }).start();
        });
    }

    private void showView(JComponent view, String title){
        SwingUtilities.invokeLater(()->{
            ToolWindowUtil.show("sql-view", project,
                    Arrays.asList(new ContentImpl(view,title, false))
            );
        });
    }

    private JComponent createSQLMessageView(String message) {
        JEditorPane editor = new JEditorPane();
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
        editor.setText(message);
        editor.setEditable(false);
        return new JBScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JComponent createSQLErrorView(SQLException ex) {
        return createSQLMessageView("SQL Error "+ex.getErrorCode()+", SQL State:"+ex.getSQLState()+"\n"+ex.getMessage());
    }

}
