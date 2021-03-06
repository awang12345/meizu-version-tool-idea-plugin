package com.meizu.tool.projectview.version_show;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

import java.util.Objects;

public class ToggleShowMavenSettingsAction extends ToggleAction {
    private Runnable onUpdateListener = () -> {
    };


    public ToggleShowMavenSettingsAction() {
        super("Show Maven version in modules");
    }


    @Override
    public boolean isSelected(AnActionEvent e) {
        return PluginSettings.getInstance().isShowVersion();
    }


    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        PluginSettings.getInstance().setShowVersion(state);
        this.onUpdateListener.run();
    }


    public void setOnUpdateListener(Runnable onUpdateListener) {
        this.onUpdateListener = Objects.requireNonNull(onUpdateListener);
    }
}

