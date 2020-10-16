package com.meizu.tool.projectview.path_hiden;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 用来去掉project上面文件路径
 */
public class CustomTreeStructureProvider implements TreeStructureProvider {

    @NotNull
    @Override
    public Collection<AbstractTreeNode> modify(@NotNull AbstractTreeNode parent, @NotNull Collection<AbstractTreeNode> children,
                                               ViewSettings settings) {
        ArrayList<AbstractTreeNode> nodes = new ArrayList<AbstractTreeNode>();
        for (AbstractTreeNode child : children) {
            Project project = child.getProject();
            if (project != null) {
                if (child.getValue() instanceof PsiDirectory) {
                    PsiDirectory directory = (PsiDirectory) child.getValue();
                    nodes.add(new PsiDirectoryNodeCustom(project, directory, settings));
                    continue;
                }
                nodes.add(child);
            }
        }
        return nodes;
    }


    @Nullable
    @Override
    public Object getData(Collection<AbstractTreeNode> collection, String s) {
        return null;
    }
}
