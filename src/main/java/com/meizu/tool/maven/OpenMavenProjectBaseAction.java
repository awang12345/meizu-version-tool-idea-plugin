package com.meizu.tool.maven;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.meizu.tool.utils.MavenUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import java.util.List;

public abstract class OpenMavenProjectBaseAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        List<MavenProject> paddingMavenProjectList = MavenUtils.getOpenMavenProjectList(project);
        if (paddingMavenProjectList == null || paddingMavenProjectList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Not found open maven projects", "Info message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        execute(project, paddingMavenProjectList);
    }


    protected abstract void execute(Project project, List<MavenProject> openMavenProjectList);

}
