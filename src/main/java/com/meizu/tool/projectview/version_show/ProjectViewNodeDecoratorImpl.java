package com.meizu.tool.projectview.version_show;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.meizu.tool.common.VersionToolConfig;
import com.meizu.tool.utils.GitUtils;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProjectViewNodeDecoratorImpl implements ProjectViewNodeDecorator {

    private static final SimpleTextAttributes VERSION_STYLE = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
            new JBColor(new Color(0x006699), new Color(0xFFCC00)));

    private static final SimpleTextAttributes BRANCH_STYLE = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
            new JBColor(new Color(0x600000), new Color(0x339933)));


    @Override
    public void decorate(ProjectViewNode node, PresentationData presentationData) {
        Optional.of(node).filter(projectViewNode -> PluginSettings.getInstance().isShowVersion()).map(ProjectViewNode::getProject)
                .map(MavenProjectsManager::getInstance).filter(MavenProjectsManager::isMavenizedProject).ifPresent(
                mavenProjectsManager -> getPomXml(node).map(mavenProjectsManager::findProject).flatMap(this::getVersion)
                        .ifPresent(doDecorate(node, presentationData)));
    }


    private Consumer<String> doDecorate(ProjectViewNode node, PresentationData data) {
        List<PresentableNodeDescriptor.ColoredFragment> coloredText = data.getColoredText();
        if (coloredText == null || coloredText.isEmpty()) {
            data.addText(data.getPresentableText(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
        }
        String separator = SystemInfo.isWindows ? "  " : "\t";
        return version -> {
            if (VersionToolConfig.getInstance().isProjectViewPomVersion()) {
                data.addText(separator + version, VERSION_STYLE);
            }
            if (VersionToolConfig.getInstance().isProjectViewGitBranch()) {
                MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(node.getProject());
                MavenProject mavenProject = mavenProjectsManager.findProject(getPomXml(node).get());
                data.addText(separator + GitUtils.getCurrentBranchName(node.getProject(), mavenProject), BRANCH_STYLE);
            }
        };
    }


    private Optional<String> getVersion(MavenProject mavenProject) {
        return Optional.of(mavenProject.getMavenId()).map(MavenId::getVersion);
    }


    private Optional<VirtualFile> getPomXml(ProjectViewNode node) {
        return Optional.ofNullable(node).map(ProjectViewNode::getVirtualFile).map(virtualFile -> virtualFile.findChild("pom.xml"));
    }


    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode, ColoredTreeCellRenderer coloredTreeCellRenderer) {
    }
}
