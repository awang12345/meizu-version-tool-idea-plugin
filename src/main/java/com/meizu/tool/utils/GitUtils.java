package com.meizu.tool.utils;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffContentFactoryEx;
import com.intellij.diff.contents.DiffContent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.ByteBackedContentRevision;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcsUtil.VcsUtil;
import com.meizu.tool.common.GitConst;
import com.meizu.tool.common.MavenConst;
import git4idea.*;
import git4idea.branch.GitBranchesCollection;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jetbrains.idea.maven.project.MavenProject;

public class GitUtils {

    /**
     * 获取master分支项目版本
     *
     * @param project
     * @param mavenProject
     * @return
     */
    public static String getMasterBranchProjectVersion(Project project, MavenProject mavenProject) {
        String masterBranchName = getMasterBranchName(project, mavenProject);
        return getBranchProjectVersion(project, mavenProject, masterBranchName);
    }


    /**
     * 获取分支版本
     *
     * @param project
     * @param mavenProject
     * @param branchName
     * @return
     */
    public static String getBranchProjectVersion(Project project, MavenProject mavenProject, String branchName) {
        if (StringUtils.isBlank(branchName)) {
            return StringUtils.EMPTY;
        }
        GitRevisionNumber compareRevisionNumber = new GitRevisionNumber(branchName);
        ContentRevision revision = GitContentRevision
                .createRevision(VcsUtil.getFilePath(mavenProject.getFile()), compareRevisionNumber, project, null);
        try {
            String pomXmlContent = ObjectUtils.assertNotNull(revision).getContent();
            Document document = DocumentHelper.parseText(pomXmlContent);
            Element projectElement = document.getRootElement();
            Element versionElement = projectElement.element(MavenConst.PomTag.VERSION);
            if (versionElement != null) {
                return versionElement.getTextTrim();
            }
            Element parentElement = projectElement.element(MavenConst.PomTag.PARENT);
            if (parentElement != null) {
                versionElement = parentElement.element(MavenConst.PomTag.VERSION);
                if (versionElement != null) {
                    return versionElement.getText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }


    private static DiffContent getPomContent(Project project, ContentRevision revision) {
        try {
            if (revision == null)
                return DiffContentFactory.getInstance().createEmpty();
            FilePath filePath = revision.getFile();
            DiffContentFactoryEx contentFactory = DiffContentFactoryEx.getInstanceEx();
            if (revision instanceof CurrentContentRevision) {
                VirtualFile vFile = ((CurrentContentRevision) revision).getVirtualFile();
                if (vFile == null || !vFile.isValid())
                    return null;
                return contentFactory.create(project, vFile);
            }
            DiffContent content;
            if (revision instanceof ByteBackedContentRevision) {
                byte[] revisionContent = ((ByteBackedContentRevision) revision).getContentAsBytes();
                if (revisionContent == null)
                    return null;
                content = contentFactory.createFromBytes(project, revisionContent, filePath);
            } else {
                String revisionContent = revision.getContent();
                if (revisionContent == null)
                    return null;
                content = contentFactory.create(project, revisionContent, filePath);
            }
            return content;
        } catch (Exception ex) {
            return null;
        }
    }


    private static String getMasterBranchName(Project project, MavenProject mavenProject) {
        try {
            GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
            GitRepository gitRepository = ObjectUtils.assertNotNull(repositoryManager.getRepositoryForFile(mavenProject.getFile()));
            GitBranchesCollection gitBranchesCollection = ObjectUtils.assertNotNull(gitRepository.getBranches());
            if (CollectionUtils.isNotEmpty(gitBranchesCollection.getRemoteBranches())) {
                GitRemoteBranch master = ContainerUtil.find(gitBranchesCollection.getRemoteBranches(),
                        (gitRemoteBranch -> gitRemoteBranch.getName().contains(GitConst.Branch.MASTER)));
                if (master != null) {
                    return master.getName();
                }
            }
            if (CollectionUtils.isNotEmpty(gitBranchesCollection.getLocalBranches())) {
                GitLocalBranch master = ContainerUtil.find(gitBranchesCollection.getLocalBranches(),
                        (gitLocalBranch -> gitLocalBranch.getName().contains(GitConst.Branch.MASTER)));
                if (master != null) {
                    return master.getName();
                }
            }
            return GitConst.Branch.MASTER;
        } catch (Exception ex) {
            ex.printStackTrace();
            return StringUtils.EMPTY;
        }
    }


    /**
     * 获取当前分支名称
     *
     * @param project
     * @param mavenProject
     * @return
     */
    public static String getCurrentBranchName(Project project, MavenProject mavenProject) {
        try {
            GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
            GitRepository gitRepository = ObjectUtils.assertNotNull(repositoryManager.getRepositoryForFile(mavenProject.getFile()));
            return gitRepository.getCurrentBranch().getName();
        } catch (Exception ex) {
            return StringUtils.EMPTY;
        }
    }
}
