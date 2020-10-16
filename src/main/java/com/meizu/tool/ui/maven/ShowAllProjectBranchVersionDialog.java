package com.meizu.tool.ui.maven;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.table.JBListTable;
import com.meizu.tool.bean.BranchVersionLastLog;
import com.meizu.tool.utils.GitUtils;
import com.meizu.tool.utils.MessageTipUtils;
import git4idea.branch.GitBrancher;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.time.FastDateFormat;
import org.jdesktop.swingx.JXTable;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

public class ShowAllProjectBranchVersionDialog extends JDialog {
    private JPanel  contentPane;
    private JButton buttonOK;
    private JPanel  mainPanel;
    private JButton checkoutBtn;
    private JBTable table;

    private static FastDateFormat DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");


    public static void show(Project project, MavenProject mavenProject, List<BranchVersionLastLog> branchVersionLastLogList) {
        new ShowAllProjectBranchVersionDialog(project, mavenProject, branchVersionLastLogList).setVisible(true);
    }


    private ShowAllProjectBranchVersionDialog(Project project, MavenProject mavenProject,
                                              List<BranchVersionLastLog> branchVersionLastLogList) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(mavenProject.getName());
        setSize(800, 400);
        setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        buttonOK.addActionListener(e -> onOK());

        checkoutBtn.addActionListener(e -> {
            onCheckout(project, mavenProject, branchVersionLastLogList);
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        mainPanel.setLayout(new MigLayout("fill, top, insets 0", "[grow, fill]"));

        table = new JBTable();
        String[] title = { "BranchName", "Version", "LastCommitTime", "LastCommitter", "LastCommitMsg" };
        DefaultTableModel myTableModel = new DefaultTableModel();
        myTableModel.setColumnIdentifiers(title);
        Collections.sort(branchVersionLastLogList);
        branchVersionLastLogList.forEach(log -> {
            myTableModel.addRow(new String[] { formatBranchName(log.getBranchName()), log.getVersion(),
                    DATETIME_FORMAT.format(log.getCommitTime()), log.getLastCommitter(), log.getLastCommitMsg() });
        });
        table.setModel(myTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setRowSelectionAllowed(true);

        JTableHeader tableHeader = table.getTableHeader();
        mainPanel.add(tableHeader, "wrap");
        mainPanel.add(ScrollPaneFactory.createScrollPane(table));
    }


    private String formatBranchName(String branchName) {
        int idx = branchName.indexOf('/');
        if (idx == -1) {
            return branchName;
        }
        return branchName.substring(idx + 1);
    }


    private void onCheckout(Project project, MavenProject mavenProject, List<BranchVersionLastLog> branchVersionLastLogList) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            MessageTipUtils.errorTip(this, "Check out error", "No branch selected.");
            return;
        }
        BranchVersionLastLog branchVersionLastLog = branchVersionLastLogList.get(selectedRow);
        if (branchVersionLastLog.isCurrentBranch()) {
            MessageTipUtils.infoTip(this, "Check out error",
                    "Project current branch was " + formatBranchName(branchVersionLastLog.getBranchName())
                            + ".Please reselect other branch to checkout!");
            return;
        }
        String myBranchName = formatBranchName(branchVersionLastLog.getBranchName());
        List<GitRepository> myRepositories = GitRepositoryManager.getInstance(project).getRepositories();
        String projectName = mavenProject.getFile().getParent().getName();
        myRepositories = myRepositories.stream()
                .filter(gitRepository -> gitRepository.getRoot().getName().equals(projectName))
                .collect(Collectors.toList());
        GitBrancher brancher = GitBrancher.getInstance(project);
        brancher.checkout(myBranchName, false, myRepositories, null);
        dispose();
    }


    private void onOK() {
        dispose();
    }


    private void onCancel() {
        dispose();
    }
}
