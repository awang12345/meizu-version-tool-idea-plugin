package com.meizu.tool.git.push;

import com.intellij.dvcs.push.PushSpec;
import com.intellij.openapi.project.Project;
import com.meizu.tool.common.GitConst;
import com.meizu.tool.common.VersionToolConfig;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandlerListener;
import git4idea.push.*;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 用于review平台代码push，不需要每次将目标地址改成refs/for/{branchName}的形式
 */
public class GerritGitPushProcessCustomizationFactory implements GitPushProcessCustomizationFactory {

    @Override
    public @Nullable GitPushProcessCustomization createCustomization(@NotNull Project project,
                                                                     @NotNull Map<GitRepository, PushSpec<GitPushSource, GitPushTarget>> pushSpecs,
                                                                     boolean forcePush) {
        return GerritGitPushProcessCustomization.INSTANCE;
    }


    private static class GerritGitPushProcessCustomization implements GitPushProcessCustomization {

        private static final GerritGitPushProcessCustomization INSTANCE = new GerritGitPushProcessCustomization();


        @Override
        public @NotNull Map<GitRepository, GitPushRepoResult> executeAfterPushIteration(
                @NotNull Map<GitRepository, GitPushRepoResult> results) {
            return results;
        }


        @Override
        public @NotNull GitCommandResult runPushCommand(@NotNull GitRepository repository,
                                                        @NotNull PushSpec<GitPushSource, GitPushTarget> pushSpec,
                                                        @NotNull GitPushParams pushParams,
                                                        @NotNull GitLineHandlerListener progressListener) {
            Git myGit = Git.getInstance();
            GitRemote remote = pushParams.getRemote();
            //是否push到review平台
            String meizuGerritWebsiteDomain = VersionToolConfig.getInstance().getMeizuGerritWebsiteDomain();
            boolean isPushToGerrit =
                    StringUtils.isNotBlank(meizuGerritWebsiteDomain) && remote.getFirstUrl().contains(meizuGerritWebsiteDomain);
            if (isPushToGerrit) {
                String branchName = repository.getCurrentBranch().getName();
                String spec = GitConst.PUSH_GERRIT_BRANCH_PREFIX + branchName;
                return myGit
                        .push(repository, remote.getName(), remote.getFirstUrl(), spec, pushParams.shouldSetupTracking(), progressListener);
            }
            return myGit.push(repository, pushParams, progressListener);
        }


        @Override
        public void executeAfterPush(@NotNull Map<GitRepository, GitPushRepoResult> results) {
        }
    }

}
