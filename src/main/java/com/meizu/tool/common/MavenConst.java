package com.meizu.tool.common;

import java.util.regex.Pattern;

public interface MavenConst {

    interface Version {
        Pattern SNAPSHOT_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+).*\\-SNAPSHOT", Pattern.CASE_INSENSITIVE);
        Pattern RC_VERSION_PATTERN       = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+).*\\-RC(\\d+)", Pattern.CASE_INSENSITIVE);
        String RC = "RC";
        String SNAPSHOT = "SNAPSHOT";
    }

    interface App {
        String GROUP_ID = "VersionTool";
    }

    interface Biz {
        String DEPENDENCY            = "Dependency";
        String MANAGEMENT_DEPENDENCY = "ManagementDependency";
    }

    interface File {

        interface SpecificFile {
            String POM = "pom.xml";
        }
    }

    interface PomTag {
        String PROJECT                 = "project";
        String MODEL_VERSION           = "modelVersion";
        String PARENT                  = "parent";
        String GROUP_ID                = "groupId";
        String ARTIFACT_ID             = "artifactId";
        String VERSION                 = "version";
        String PACKAGING               = "packaging";
        String NAME                    = "name";
        String URL                     = "url";
        String DESCRIPTION             = "description";
        String MODULES                 = "modules";
        String PROPERTIES              = "properties";
        String DEPENDENCIES            = "dependencies";
        String DEPENDENCY_MANAGEMENT   = "dependencyManagement";
        String PROFILES                = "profiles";
        String REPOSITORIES            = "repositories";
        String DISTRIBUTION_MANAGEMENT = "distributionManagement";
        String PLUGIN_REPOSITORIES     = "pluginRepositories";
        String BUILD                   = "build";
        String MODULE                  = "module";
        String PROPERTY                = "property";
        String DEPENDENCY              = "dependency";
        String PLUGIN                  = "plugin";
    }

    interface XmlTag {
        String PREFIX = "XmlTag:";
    }

}
