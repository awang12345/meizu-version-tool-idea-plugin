package com.meizu.tool.ui.maven;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.history.VcsDiffUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.vcsUtil.VcsUtil;
import com.meizu.tool.utils.GitUtils;
import com.meizu.tool.utils.MavenUtils;
import com.meizu.tool.utils.NotificationUtil;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.MavenUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class AllMavenProjectVCSettingDialog extends JDialog {
    private JPanel                        contentPane;
    private JButton                       buttonOK;
    private JButton                       buttonCancel;
    private JPanel                        mainPanel;
    private Project                       project;
    private Map<String, ProjectVersionUI> projectVersionUIMap = new HashMap<>();


    public static void show(Project project, List<MavenProject> mavenProjectList) {
        AllMavenProjectVCSettingDialog dialog = new AllMavenProjectVCSettingDialog(project, mavenProjectList);
        dialog.setTitle("Maven project version setting");
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        dialog.setVisible(true);
    }


    private AllMavenProjectVCSettingDialog(Project project, List<MavenProject> mavenProjectList) {
        this.project = project;
        initGUI(project, mavenProjectList);
        bindAction();
    }


    private void bindAction() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void initGUI(Project project, List<MavenProject> mavenProjectList) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        mainPanel.setLayout(new MigLayout("fill, top, insets 0", "[]10[grow, fill]"));
        mainPanel.add(new JBLabel("Please set project version:"), "span 2,wrap");
        JPanel projectVersionPanel = new JPanel(new MigLayout("fill, insets 2 10 2 10", "[]1[]10[grow,fill]"));
        projectVersionPanel.setBorder(JBUI.Borders.customLine(Color.LIGHT_GRAY, 0, 1, 1, 0));
        mainPanel.add(ScrollPaneFactory.createScrollPane(projectVersionPanel), "spanx, growx, pushx, wrap");
        mavenProjectList.forEach(mp -> {
            JCheckBox checkBox = new JCheckBox(mp.getName(), true);
            projectVersionPanel.add(checkBox, "gapleft 10");

            String masterVersion = GitUtils.getMasterBranchProjectVersion(project, mp);
            projectVersionPanel.add(new LinkLabel(masterVersion, null, null), "gapleft 10");

            String newRcVersion = MavenUtils.generateRCVersion(masterVersion, mp.getMavenId().getVersion());
            JTextField versionTextField = new JFormattedTextField(newRcVersion);
            projectVersionPanel.add(versionTextField, "wrap");
            projectVersionUIMap.put(mp.getName(), new ProjectVersionUI(checkBox, versionTextField, mp));
        });

        JBLabel tip = new JBLabel("If auto generate version invalid.Please repull all project!", UIUtil.ComponentStyle.SMALL);
        mainPanel.add(tip,"span 2,wrap");

        JPanel selectPanel = new JPanel(new MigLayout("fill, insets 0", "[]10[]10[grow]"));
        mainPanel.add(selectPanel, "spanx, growx, pushx, wrap");
        selectPanel.add(new LinkLabel("Select all", null, (s, l) -> changeItemsSelection(true)));
        selectPanel.add(new LinkLabel("Select none", null, (s, l) -> changeItemsSelection(false)));
    }


    private void changeItemsSelection(boolean selectStatus) {
        projectVersionUIMap.values().forEach(p -> p.checkBox.setSelected(selectStatus));
    }


    private class ProjectVersionUI {

        private JCheckBox    checkBox;
        private JTextField   versionTextField;
        private MavenProject mavenProject;


        public ProjectVersionUI(JCheckBox checkBox, JTextField versionTextField, MavenProject mavenProject) {
            this.checkBox = checkBox;
            this.versionTextField = versionTextField;
            this.mavenProject = mavenProject;
        }
    }


    private void onOK() {
        List<ProjectVersionUI> selectProjects = projectVersionUIMap.values().stream().filter(p -> p.checkBox.isSelected())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(selectProjects)) {
            JOptionPane.showMessageDialog(this, "Select least 1 projects!", "Error tip", JOptionPane.ERROR_MESSAGE);
        } else {

            new Task.Backgroundable(project, "Update project version....", true) {

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    selectProjects.forEach(p -> {
                        if (indicator.isRunning()) {
                            String version = p.versionTextField.getText();
                            indicator.setText(p.mavenProject.getName() + " version to " + version);
                            MavenUtils.updateProjectVersion(project, p.mavenProject, version);
                        }
                    });
                }


                @Override
                public void onSuccess() {
                    NotificationUtil.info("Maven version update success", selectProjects.size() + " project haven updated");
                }
            }.queue();

            dispose();
        }

    }


    private void onCancel() {
        dispose();
    }


    @Override
    public void dispose() {
        super.dispose();
        projectVersionUIMap.clear();
    }
}
