package com.meizu.tool.maven;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.meizu.tool.utils.GitUtils;
import com.meizu.tool.utils.MavenUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;

public class ReleaseVersionAction extends AbstractAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
        String version = mavenProject.getMavenId().getVersion();
        String masterBranchVersionName = GitUtils.getMasterBranchProjectVersion(project, mavenProject);
        String newRcVersion = MavenUtils.generateRCVersion(masterBranchVersionName, version);
        String snapshotVersion = JOptionPane
                .showInputDialog("Master version is " + masterBranchVersionName + ".Please input rc version", newRcVersion);
        if (StringUtils.isBlank(snapshotVersion)) {
            return;
        }
        MavenUtils.updateProjectVersion(project, mavenProject, snapshotVersion);

    }

}
