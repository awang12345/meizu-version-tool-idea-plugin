package com.meizu.tool.bean;

import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.model.MavenId;

import java.util.Objects;

public class MavenGroupArtifact {

    private String groupId;
    private String artifactId;

    public MavenGroupArtifact() {
    }

    public MavenGroupArtifact(MavenId mavenId) {
        this.groupId = mavenId.getGroupId();
        this.artifactId = mavenId.getArtifactId();

    }

    public MavenGroupArtifact setValue(MavenDomDependency mavenDomDependency) {
        this.groupId = mavenDomDependency.getGroupId().getValue();
        this.artifactId = mavenDomDependency.getArtifactId().getValue();
        return this;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MavenGroupArtifact that = (MavenGroupArtifact) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }

}
