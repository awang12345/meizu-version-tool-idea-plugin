package com.meizu.tool.maven;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.VcsCommitMetadata;
import com.intellij.vcs.log.VcsLogProvider;
import com.meizu.tool.bean.BranchVersionLastLog;
import com.meizu.tool.ui.maven.ShowAllProjectBranchVersionDialog;
import com.meizu.tool.utils.GitUtils;
import com.meizu.tool.utils.MavenUtils;
import com.meizu.tool.utils.NotificationUtil;
import git4idea.GitRemoteBranch;
import git4idea.history.GitLogUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.util.*;

/**
 * 查看所有分支版本
 */
public class ShowAllBranchVersionAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
        final Project project = e.getProject();

        new Task.Backgroundable(project, "Fetch branch version log", true) {
            List<BranchVersionLastLog> branchVersionLastLogList = null;
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Fetching all branch version log for " + mavenProject.getName());
                GitRepository gitRepository = GitRepositoryManager.getInstance(project).getRepositoryForFile(mavenProject.getFile());
                branchVersionLastLogList = getBranchVersionLog(indicator, project, mavenProject, gitRepository);
                if (CollectionUtils.isEmpty(branchVersionLastLogList)) {
                    JOptionPane.showMessageDialog(null, "Not found branch version log", "Info message", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            @Override
            public void onSuccess() {
                ShowAllProjectBranchVersionDialog.show(project, mavenProject, branchVersionLastLogList);
            }
        }.queue();

    }


    private List<BranchVersionLastLog> getBranchVersionLog(ProgressIndicator indicator, Project project, MavenProject mavenProject,
                                                           GitRepository gitRepository) {
        Collection<GitRemoteBranch> gitRemoteBranches = gitRepository.getBranches().getRemoteBranches();
        if (CollectionUtils.isEmpty(gitRemoteBranches)) {
            return Collections.emptyList();
        }
        final VirtualFile root = mavenProject.getFile().getParent();
        List<BranchVersionLastLog> branchVersionLastLogList = new ArrayList<>();

        final String currentBranchName = gitRepository.getCurrentBranchName();

        gitRemoteBranches.forEach(gitRemoteBranch -> {
            BranchVersionLastLog log = new BranchVersionLastLog();
            String branchName = gitRemoteBranch.getName();
            indicator.setText2("fetch commit log for branch " + branchName);
            log.setBranchName(branchName);
            log.setCurrentBranch(isSameBranch(currentBranchName, branchName));
            String branchProjectVersion = GitUtils.getBranchProjectVersion(project, mavenProject, branchName);
            log.setVersion(branchProjectVersion);
            String[] params = new String[] { branchName, "--pretty=format:\"%ci %cn %s\"", "-1" };
            try {
                VcsLogProvider.DetailedLogData data = GitLogUtil.collectMetadata(project, root, false, params);
                List<VcsCommitMetadata> commits = Objects.requireNonNull(data).getCommits();
                if (CollectionUtils.isNotEmpty(commits)) {
                    commits.stream().findFirst().ifPresent(cm -> {
                        log.setLastCommitter(cm.getCommitter().getName());
                        log.setCommitTime(cm.getCommitTime());
                        log.setLastCommitMsg(cm.getSubject());
                    });
                    branchVersionLastLogList.add(log);
                }
            } catch (VcsException e1) {
                e1.printStackTrace();
            }
        });
        return branchVersionLastLogList;
    }


    private boolean isSameBranch(String branchName1, String branchName2) {
        if (branchName1 == null || branchName2 == null) {
            return false;
        }
        return branchName1.substring(branchName1.lastIndexOf('/') + 1).equals(branchName2.substring(branchName2.lastIndexOf("/") + 1));
    }


    @Override
    protected boolean isVisible(AnActionEvent e) {
        MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
        if (mavenProject == null) {
            return false;
        }
        GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(e.getProject());
        if (repositoryManager == null) {
            return false;
        }
        return true;
    }
}
