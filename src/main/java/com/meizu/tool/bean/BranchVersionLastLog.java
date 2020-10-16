package com.meizu.tool.bean;

import org.jetbrains.annotations.NotNull;

public class BranchVersionLastLog implements Comparable<BranchVersionLastLog> {

    private String branchName;
    private String version;
    private String lastCommitMsg;
    private String lastCommitter;
    private long   commitTime;
    private boolean isCurrentBranch;


    public BranchVersionLastLog() {
    }


    public String getLastCommitMsg() {
        return lastCommitMsg;
    }


    public void setLastCommitMsg(String lastCommitMsg) {
        this.lastCommitMsg = lastCommitMsg;
    }


    public String getLastCommitter() {
        return lastCommitter;
    }


    public void setLastCommitter(String lastCommitter) {
        this.lastCommitter = lastCommitter;
    }


    public String getBranchName() {
        return branchName;
    }


    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }

    public long getCommitTime() {
        return commitTime;
    }


    public void setCommitTime(long commitTime) {
        this.commitTime = commitTime;
    }


    public boolean isCurrentBranch() {
        return isCurrentBranch;
    }


    public void setCurrentBranch(boolean currentBranch) {
        isCurrentBranch = currentBranch;
    }


    @Override
    public int compareTo(@NotNull BranchVersionLastLog o) {
        return Long.compare(o.commitTime, this.commitTime);
    }
}
