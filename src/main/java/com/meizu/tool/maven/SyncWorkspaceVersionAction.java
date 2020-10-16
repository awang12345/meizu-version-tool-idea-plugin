package com.meizu.tool.maven;

import com.intellij.openapi.project.Project;
import com.meizu.tool.ui.maven.SyncVersionProjectSelectorDialog;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.List;

/**
 * 同步同工作区打开的maven项目版本号
 */
public class SyncWorkspaceVersionAction extends OpenMavenProjectBaseAction {

    @Override
    protected void execute(Project project, List<MavenProject> openMavenProjectList) {
        SyncVersionProjectSelectorDialog.show(project, openMavenProjectList);
    }

}
