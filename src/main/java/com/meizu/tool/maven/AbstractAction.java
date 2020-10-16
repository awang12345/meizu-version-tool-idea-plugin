package com.meizu.tool.maven;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

public abstract class AbstractAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        boolean isVisible = isVisible(e);
        if (isVisible) {
            presentation.setEnabledAndVisible(true);
        } else {
            presentation.setVisible(false);
        }
    }


    protected boolean isVisible(AnActionEvent e) {
        return MavenActionUtil.isMavenizedProject(e.getDataContext());
    }

}
