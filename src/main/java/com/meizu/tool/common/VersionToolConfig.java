package com.meizu.tool.common;

public class VersionToolConfig {

    private final static VersionToolConfig INSTANCE = new VersionToolConfig();

    //项目是否显示maven版本号
    private boolean isProjectViewPomVersion  = true;
    //项目是否显示git分支
    private boolean isProjectViewGitBranch   = true;
    //快照版本格式
    private String  snapshotVersionFormat    = "{version}-{branch}-SNAPSHOT";
    //默认提交信息
    private String  defaultCommitMessage     = "feat:desc of feat #0";
    //review平台域名
    private String  meizuGerritWebsiteDomain = "review.rnd.meizu.com";
    //推送到review平台时在branch前面加上前缀
    private String pushGerritBranchPrefix = "HEAD:refs/for/";


    private VersionToolConfig() {
    }


    public static VersionToolConfig getInstance() {
        return INSTANCE;
    }


    public boolean isProjectViewPomVersion() {
        return isProjectViewPomVersion;
    }


    public void setProjectViewPomVersion(boolean projectViewPomVersion) {
        isProjectViewPomVersion = projectViewPomVersion;
    }


    public boolean isProjectViewGitBranch() {
        return isProjectViewGitBranch;
    }


    public void setProjectViewGitBranch(boolean projectViewGitBranch) {
        isProjectViewGitBranch = projectViewGitBranch;
    }


    public String getSnapshotVersionFormat() {
        return snapshotVersionFormat;
    }


    public void setSnapshotVersionFormat(String snapshotVersionFormat) {
        this.snapshotVersionFormat = snapshotVersionFormat;
    }


    public String getDefaultCommitMessage() {
        return defaultCommitMessage;
    }


    public void setDefaultCommitMessage(String defaultCommitMessage) {
        this.defaultCommitMessage = defaultCommitMessage;
    }


    public String getMeizuGerritWebsiteDomain() {
        return meizuGerritWebsiteDomain;
    }


    public void setMeizuGerritWebsiteDomain(String meizuGerritWebsiteDomain) {
        this.meizuGerritWebsiteDomain = meizuGerritWebsiteDomain;
    }


    public String getPushGerritBranchPrefix() {
        return pushGerritBranchPrefix;
    }


    public void setPushGerritBranchPrefix(String pushGerritBranchPrefix) {
        this.pushGerritBranchPrefix = pushGerritBranchPrefix;
    }
}
