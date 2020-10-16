package com.meizu.tool.git.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.ui.CommitMessageProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * git提交信息自动加上redmineID
 */
public class AutoRedmineIdCommitMessageProvider implements CommitMessageProvider {

    private final static Pattern REDMINE_ID_PATTERN = Pattern.compile("#\\d+");


    @Override
    public @Nullable String getCommitMessage(LocalChangeList forChangelist, Project project) {
        String comment = forChangelist.getComment();
        if (StringUtils.isBlank(comment)) {
            return "Update " + forChangelist.getChanges().size() + " files #0";
        }
        if (REDMINE_ID_PATTERN.matcher(comment).find()) {
            return comment;
        }
        return comment + " #0";
    }

}
