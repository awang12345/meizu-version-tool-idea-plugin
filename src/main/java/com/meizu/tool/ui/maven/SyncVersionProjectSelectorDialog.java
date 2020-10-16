package com.meizu.tool.ui.maven;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.meizu.tool.utils.MavenUtils;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SyncVersionProjectSelectorDialog extends JDialog {
    private JPanel               contentPane;
    private JButton              buttonOK;
    private JButton              buttonCancel;
    private JPanel               projectPanel;
    private CheckBoxList<String> projectCheckBoxList;
    private List<MavenProject>   mavenProjectList;
    private Project              project;
    private JCheckBox            forceDiffVersionSync;


    public static void show(Project project, List<MavenProject> mavenProjectList) {
        SyncVersionProjectSelectorDialog dialog = new SyncVersionProjectSelectorDialog(project, mavenProjectList);
        dialog.setTitle("Maven project version sync");
        dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        dialog.setSize(400, 400);
        dialog.setVisible(true);
    }


    private SyncVersionProjectSelectorDialog(Project project, List<MavenProject> mavenProjectList) {
        this.project = project;
        this.mavenProjectList = mavenProjectList;
        setContentPane(contentPane);
        initGUI();
        bindAction();
    }


    private void bindAction() {
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void initGUI() {
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        projectPanel.setLayout(new MigLayout("fill, top, insets 0", "[]10[grow, fill]"));
        projectPanel.add(new JBLabel("Please select version sync projects:"));
        forceDiffVersionSync = new JCheckBox("Force diff version sync", true);
        projectPanel.add(forceDiffVersionSync, "wrap");
        projectCheckBoxList = new CheckBoxList<String>((index, value) -> {
            buttonOK.setEnabled(CollectionUtils.isNotEmpty(projectCheckBoxList.getSelectedValuesList()));
        });
        Map<String, Boolean> projectMap = new HashMap<>();
        mavenProjectList.forEach(p -> projectMap.put(p.getName(), true));
        projectCheckBoxList.setStringItems(projectMap);
        projectPanel.add(ScrollPaneFactory.createScrollPane(projectCheckBoxList), "gaptop 10, span, grow, push, wrap");
        JPanel selectPanel = new JPanel(new MigLayout("fill, insets 0", "[]5[]10[grow]"));
        projectPanel.add(selectPanel, "spanx, growx, pushx, wrap");
        selectPanel.add(new LinkLabel("Select all", null, (s, l) -> changeItemsSelection(true)));
        selectPanel.add(new LinkLabel("Select none", null, (s, l) -> changeItemsSelection(false)));
    }


    private void changeItemsSelection(boolean selected) {
        mavenProjectList.forEach(p -> projectCheckBoxList.setItemSelected(p.getName(), selected));
        projectCheckBoxList.repaint();
    }


    private void onOK() {
        List<MavenProject> selectProjects = mavenProjectList.stream().filter(mp -> projectCheckBoxList.isItemSelected(mp.getName()))
                .collect(Collectors.toList());
        if (selectProjects == null || selectProjects.size() < 2) {
            JOptionPane.showMessageDialog(this, "Select least 2 projects!", "Error tip", JOptionPane.ERROR_MESSAGE);
        } else {
            MavenUtils.syncMavenVersion(this.project, selectProjects, forceDiffVersionSync.isSelected());
            dispose();
        }
    }


    private void onCancel() {
        dispose();
    }


    @Override
    public void dispose() {
        super.dispose();
        mavenProjectList.clear();
    }

}
