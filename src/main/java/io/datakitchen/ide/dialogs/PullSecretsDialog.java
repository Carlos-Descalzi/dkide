package io.datakitchen.ide.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.config.*;
import io.datakitchen.ide.tools.ProcessLogView;
import io.datakitchen.ide.util.ToolWindowUtil;
import io.datakitchen.ide.ui.FormLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class PullSecretsDialog extends DialogWrapper {

    private final JPanel panel = new JPanel();
    private final JTextField vaultUrl = new JTextField();
    private final JTextField token = new JTextField();
    private final JTextField prefix = new JTextField();
    private final JCheckBox verifyCertificate = new JCheckBox();
    private final JCheckBox overwrite = new JCheckBox();
    private final JCheckBox asProjectSettings = new JCheckBox();

    public PullSecretsDialog() {
        super(true);
        setTitle("Import Secrets From Vault");

        panel.setLayout(new FormLayout(5,5));
        JLabel l = new JLabel("Vault URL");
        l.setPreferredSize(new Dimension(100,28));
        l.setLabelFor(vaultUrl);
        vaultUrl.setPreferredSize(new Dimension(200,28));
        panel.add(l);
        panel.add(vaultUrl);

        l = new JLabel("Vault token");
        l.setLabelFor(token);
        token.setPreferredSize(new Dimension(200,28));
        panel.add(l);
        panel.add(token);

        l = new JLabel("Prefix");
        l.setLabelFor(prefix);
        prefix.setPreferredSize(new Dimension(200,28));
        panel.add(l);
        panel.add(prefix);

        l = new JLabel("Verify certificate");
        l.setLabelFor(verifyCertificate);
        panel.add(l);
        panel.add(verifyCertificate);

        l = new JLabel("Overwrite");
        l.setLabelFor(overwrite);
        panel.add(l);
        panel.add(overwrite);

        l = new JLabel("Save as project secrets");
        l.setLabelFor(asProjectSettings);
        panel.add(l);
        panel.add(asProjectSettings);

        init();
    }

    @Override
    protected @NotNull java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(vaultUrl.getText())){
            validations.add(new ValidationInfo("URL is required", vaultUrl));
        }
        if (StringUtils.isBlank(token.getText())){
            validations.add(new ValidationInfo("Token is required", token));
        }

        return validations;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public void doPull(Project project){
        SecretPuller puller = new SecretPuller(vaultUrl.getText(),token.getText(),prefix.getText(),verifyCertificate.isSelected(),false);

        Runnable task = ()->{
            try {
                List<Secret> secrets = puller.pull();

                ConfigurationService config = ConfigurationService.getInstance(project);

                if (asProjectSettings.isSelected()){
                    ProjectConfiguration configuration = config.getProjectConfiguration();
                    storeSecrets(configuration, secrets);
                    config.setProjectConfiguration(configuration);
                } else {
                    GlobalConfiguration configuration = config.getGlobalConfiguration();
                    storeSecrets(configuration, secrets);
                    config.setGlobalConfiguration(configuration);
                }

            }catch(Exception ex){
                ex.printStackTrace();
            }
        };

        ProcessLogView view = new ProcessLogView();
        puller.setLogTarget(view);
        ToolWindowUtil.show("secrets-pull",project, List.of(new ContentImpl(view, "Pulling Secrets ...", false)));
        view.runTask(task);
    }

    private void storeSecrets(Configuration configuration, List<Secret> secrets){
        List<Secret> existingSecrets = configuration.getSecrets();
        if (existingSecrets == null){
            existingSecrets = new ArrayList<>();
        }

        Set<Secret> newSecrets = new LinkedHashSet<>();
        if (overwrite.isSelected()){
            newSecrets.addAll(secrets);
            newSecrets.addAll(existingSecrets);
        } else {
            newSecrets.addAll(existingSecrets);
            newSecrets.addAll(secrets);
        }

        configuration.setSecrets(new ArrayList<>(newSecrets));

    }
}
