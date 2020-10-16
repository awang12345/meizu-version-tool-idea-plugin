package com.meizu.tool.maven;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.meizu.tool.ui.maven.AllMavenProjectVCSettingDialog;
import com.meizu.tool.ui.maven.SyncVersionProjectSelectorDialog;
import com.meizu.tool.utils.MavenUtils;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;

/**
 * 所有打开的maven项目进行rc版本操作
 */
public class AllMavenProjectRCVersionAction extends OpenMavenProjectBaseAction {

    @Override
    protected void execute(Project project, List<MavenProject> openMavenProjectList) {
        AllMavenProjectVCSettingDialog.show(project, openMavenProjectList);
    }

}
