package com.meizu.tool.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import com.meizu.tool.bean.MavenGroupArtifact;
import com.meizu.tool.common.MavenConst;
import com.meizu.tool.common.VersionToolConfig;
import com.sun.jna.Callback;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MavenUtils {

    private static final String SNAPSHOT_VERSION_SUFFIX = "-SNAPSHOT";


    public static MavenDomProjectModel getMavenProjectModel(Project project, VirtualFile virtualFile) {
        if (MavenActionUtil.isMavenProjectFile(virtualFile)) {
            return MavenDomUtil.getMavenDomProjectModel(project, virtualFile);
        }
        return null;
    }


    public static List<MavenDomDependency> getAllSnapshotDependencyList(Project project, MavenProject mavenProject) {
        List<MavenDomDependency> result = getSnapshotDependencyList(project, mavenProject);
        MavenProjectsManager instance = MavenProjectsManager.getInstance(project);
        List<MavenProject> modules = instance.getModules(mavenProject);
        if (CollectionUtils.isNotEmpty(modules)) {
            for (MavenProject module : modules) {
                result.addAll(getSnapshotDependencyList(project, module));
            }
        }
        return result;
    }


    public static List<MavenDomDependency> getSnapshotDependencyList(Project project, MavenProject mavenProject) {
        PsiFile file = PsiManager.getInstance(project).findFile(mavenProject.getFile());
        MavenDomProjectModel rootModel = MavenDomUtil.getMavenDomModel(file, MavenDomProjectModel.class);
        return getSnapshotDependencyList(rootModel);
    }


    /**
     * 获取当前pom下快照依赖
     *
     * @param model
     * @return
     */
    public static List<MavenDomDependency> getSnapshotDependencyList(MavenDomProjectModel model) {
        List<MavenDomDependency> snapshotDependencyList = getSnapshotDependencyList(model.getDependencies());
        snapshotDependencyList.addAll(getSnapshotDependencyList(model.getDependencyManagement().getDependencies()));
        return snapshotDependencyList;
    }


    private static List<MavenDomDependency> getSnapshotDependencyList(MavenDomDependencies dependencies) {
        if (!dependencies.exists()) {
            return new ArrayList<>();
        }
        dependencies.getDependencies().forEach(v -> {
            System.out.println(v.getArtifactId().getValue() + ":" + String.valueOf(v.getVersion().getRawText()));
        });
        return dependencies.getDependencies().stream().filter(v -> v.getVersion().getValue() != null)
                .filter(v -> v.getVersion().getValue().endsWith(SNAPSHOT_VERSION_SUFFIX)).collect(Collectors.toList());
    }


    /**
     * 更改依赖版本号
     *
     * @param model
     * @param dependency
     * @param newVersionStr
     */
    public static boolean changeDependencyVersion(MavenDomProjectModel model, MavenDomDependency dependency, String newVersionStr) {
        GenericDomValue<String> versionValue = dependency.getVersion();
        if (versionValue.getRawText() == null) {
            return false;
        }
        String rawVersionText = versionValue.getRawText().trim();
        if (rawVersionText.startsWith("${")) {
            //在properties中定义了版本
            String versionPlaceHolder = rawVersionText.substring(2, rawVersionText.length() - 1);
            XmlTag propertiesXmlTag = model.getProperties().ensureTagExists();
            if (propertiesXmlTag == null) {
                return false;
            }
            @NotNull XmlTag[] subTags = propertiesXmlTag.findSubTags(versionPlaceHolder);
            if (subTags != null && subTags.length > 0) {
                subTags[0].getValue().setText(newVersionStr);
            }
            return true;
        }
        dependency.getVersion().setValue(newVersionStr);
        return true;
    }


    /**
     * 同步版本号
     *
     * @param project
     * @param mavenProjectList
     */
    public static void syncMavenVersion(Project project, List<MavenProject> mavenProjectList, boolean isForceDiffVersionSync) {
        if (project == null || CollectionUtils.isEmpty(mavenProjectList)) {
            return;
        }
        List<MavenProject> allMavenProjects = new ArrayList<>(mavenProjectList);
        //将子module的版本加入到列表中
        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        mavenProjectList.stream().map(p -> mavenProjectsManager.getModules(p)).filter(list -> CollectionUtils.isNotEmpty(list)).forEach(
                list -> list.stream().filter(mavenProject -> mavenProject.getMavenId() != null)
                        .forEach(mavenProject -> allMavenProjects.add(mavenProject)));
        //遍历列表，建立版本映射关系
        Map<MavenGroupArtifact, String> mavenIdMap = new HashMap<>();
        allMavenProjects.stream().map(p -> p.getMavenId())
                .filter(m -> m != null && m.getArtifactId() != null && m.getGroupId() != null && m.getVersion() != null).forEach(m -> {
            mavenIdMap.put(new MavenGroupArtifact(m), m.getVersion());
        });
        //执行版本同步
        WriteCommandAction.runWriteCommandAction(project, () -> {
            List<String> syncDetails = new ArrayList<>();
            allMavenProjects.stream().map(p -> MavenDomUtil.getMavenDomProjectModel(project, p.getFile())).filter(model -> model != null)
                    .forEach(model -> {
                        Optional.of(model.getDependencies()).ifPresent(mavenDomDependencies -> {
                            updateDependencyVersion(isForceDiffVersionSync, model, mavenDomDependencies.getDependencies(), mavenIdMap,
                                    syncDetails);
                        });
                        Optional.of(model.getDependencyManagement()).ifPresent(mavenDomDependencies -> {
                            if (mavenDomDependencies.getDependencies() != null) {
                                updateDependencyVersion(isForceDiffVersionSync, model,
                                        mavenDomDependencies.getDependencies().getDependencies(), mavenIdMap, syncDetails);
                            }
                        });
                    });
            showSyncResultTip(project, syncDetails);
        });
    }


    private static void showSyncResultTip(Project project, List<String> syncDetails) {
        StringBuilder content = new StringBuilder();
        content.append("Total sync dependency version count : ").append(syncDetails.size());
        if (!syncDetails.isEmpty()) {
            content.append("\n");
            syncDetails.forEach(syncDetail -> content.append(syncDetail).append("\n"));
        }
        NotificationUtil.info("Maven version sync finish", content.toString(), project);
    }


    private static void updateDependencyVersion(boolean isForceDiffVersionSync, MavenDomProjectModel model,
                                                List<MavenDomDependency> processList, Map<MavenGroupArtifact, String> targetVersionMap,
                                                List<String> syncDetails) {
        MavenGroupArtifact matcher = new MavenGroupArtifact();
        Optional.of(processList).ifPresent(p -> p.stream().forEach(m -> {
            String dependencyVersion = StringUtils.defaultString(m.getVersion().getValue(), m.getVersion().getRawText());
            if (dependencyVersion != null) {
                String newVersion = targetVersionMap.get(matcher.setValue(m));
                if (isNeedVersionSync(dependencyVersion, newVersion, isForceDiffVersionSync)) {
                    if (MavenUtils.changeDependencyVersion(model, m, newVersion)) {
                        syncDetails.add("[" + model.getName() + "] " + m.getGroupId() + ":" + m.getArtifactId() + " " + dependencyVersion
                                + ">>" + newVersion);
                    }
                }
            }
        }));
    }


    /**
     * 是否需要版本同步
     *
     * @param dependenceVersion
     * @param newVersion
     * @param isForceDiffVersionSync 是否只要版本不一样就进行版本同步
     * @return
     */
    private static boolean isNeedVersionSync(String dependenceVersion, String newVersion, boolean isForceDiffVersionSync) {
        if (dependenceVersion == null || newVersion == null || dependenceVersion.equals(newVersion)) {
            return false;
        }
        if (isForceDiffVersionSync) {
            return true;
        }
        //如果新版本是RC版本，依赖版本也是RC版本，那么就不需要进行版本同步
        boolean isNewVersionRC = newVersion.contains(MavenConst.Version.RC);
        if (isNewVersionRC) {
            //依赖版本非RC版本或者大版本号相同才需要进行更新
            return !dependenceVersion.contains(MavenConst.Version.RC) || newVersion.substring(0, newVersion.lastIndexOf('-'))
                    .equals(dependenceVersion.substring(0, dependenceVersion.lastIndexOf('-')));
        }
        return true;
    }


    /**
     * 更改项目版本号，自动同步子模块项目的版本
     *
     * @param project
     * @param mavenProject
     * @param newVersion
     */
    public static void updateProjectVersion(Project project, MavenProject mavenProject, String newVersion) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                changeCurrentProjectVersionWithOutChildModule(project, mavenProject, newVersion);
                MavenProjectsManager instance = MavenProjectsManager.getInstance(project);
                List<MavenProject> modules = instance.getModules(mavenProject);
                if (modules == null || modules.isEmpty()) {
                    return;
                }
                //更新子模块module里面的版本号
                String aid = mavenProject.getMavenId().getArtifactId();
                for (MavenProject module : modules) {
                    MavenDomProjectModel childModule = MavenDomUtil.getMavenDomProjectModel(project, module.getFile());
                    if (childModule.getVersion().getRawText() != null) {
                        childModule.getVersion().setValue(newVersion);
                        return;
                    }
                    String parentAid = childModule.getMavenParent().getArtifactId().getValue();
                    if (parentAid.equals(aid)) {
                        childModule.getMavenParent().getVersion().setValue(newVersion);
                    }
                }
            } catch (Exception ex) {
                NotificationUtil.error("Update project version error",
                        "Update version of project:" + mavenProject.getName() + " error:" + ex.getMessage());
            }
        });
    }


    /**
     * 更改当前项目的版本，而不修改子项目模块版本
     *
     * @param project
     * @param mavenProject
     * @param newVersion
     */
    private static void changeCurrentProjectVersionWithOutChildModule(Project project, MavenProject mavenProject, String newVersion) {
        MavenDomProjectModel projectModel = MavenDomUtil.getMavenDomProjectModel(project, mavenProject.getFile());
        if (projectModel != null) {
            if (projectModel.getVersion().getRawText() != null) {
                projectModel.getVersion().setValue(newVersion);
            } else if (projectModel.getMavenParent() != null && projectModel.getMavenParent().getVersion() != null) {
                //使用了父parent版本
                projectModel.getMavenParent().getVersion().setValue(newVersion);
            }
            return;
        }
        XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(mavenProject.getFile());
        if (xmlFile == null) {
            NotificationUtil.error("Error", "Pom file" + xmlFile.getVirtualFile().getPath() + " not found");
        }
        XmlTag projectTag = xmlFile.getRootTag();
        @NotNull XmlTag versionTag = projectTag.findFirstSubTag(MavenConst.PomTag.VERSION);
        if (versionTag != null) {
            versionTag.getValue().setText(newVersion);
            return;
        }
        @NotNull XmlTag parentTag = projectTag.findFirstSubTag(MavenConst.PomTag.PARENT);
        if (parentTag != null) {
            parentTag.getValue().setText(newVersion);
            return;
        }
        //在ARTIFACT_ID后面增加version
        XmlTag newVersionTag = projectTag.createChildTag(MavenConst.PomTag.VERSION, projectTag.getNamespace(), newVersion, false);
        projectTag.addAfter(newVersionTag, projectTag.findFirstSubTag(MavenConst.PomTag.ARTIFACT_ID));
    }


    /**
     * 排除子module模块
     *
     * @param mavenProjectList
     * @return
     */
    public static List<MavenProject> excludeChildModuleMavenProjects(List<MavenProject> mavenProjectList) {
        //去掉子module模块，只保留父模块
        Set<MavenId> existMavenIdSet = mavenProjectList.stream().filter(mp -> mp.getMavenId() != null).map(mp -> mp.getMavenId())
                .collect(Collectors.toSet());
        return mavenProjectList.stream().filter(mp -> mp.getMavenId() != null).filter(mp -> !existMavenIdSet.contains(mp.getParentId()))
                .collect(Collectors.toList());
    }


    /**
     * 获取工作区打开的项目，ignore的maven项目将不会被返回
     *
     * @param project
     * @return
     */
    public static List<MavenProject> getOpenMavenProjectList(Project project) {
        List<MavenProject> mavenProjectList = MavenProjectsManager.getInstance(project).getRootProjects();
        if (CollectionUtils.isEmpty(mavenProjectList)) {
            return Collections.emptyList();
        }
        VirtualFile[] openProjects = ProjectRootManager.getInstance(project).getContentRoots();
        if (openProjects == null || openProjects.length == 0) {
            return Collections.emptyList();
        }
        Set<String> openProjectNameSet = new HashSet<>();
        for (VirtualFile openProject : openProjects) {
            openProjectNameSet.add(openProject.getName());
        }
        return mavenProjectList.stream().filter(mp -> openProjectNameSet.contains(mp.getFile().getParent().getName()))
                .collect(Collectors.toList());
    }


    /**
     * 1、如果本项目是RC版本，则只需要在基础上递增小版本，比如RC01升级为RC02
     * 2、如果版本是SNAPSHOT版本，则在master分支基础上递增大版本并且RC01，比如：1.0-RC01升级为1.1-RC01
     *
     * @param masterBranchVersion
     * @param currentVersion
     * @return
     */
    public static String generateRCVersion(String masterBranchVersion, String currentVersion) {

        int rcIdx = currentVersion.indexOf(MavenConst.Version.RC);
        if (rcIdx != -1) {
            //本项目是RC版本，则只需要在基础上递增小版本，比如RC01升级为RC02
            int offset = rcIdx + MavenConst.Version.RC.length();
            int versionNum = NumberUtils.toInt(currentVersion.substring(offset), 0) + 1;
            return currentVersion.substring(0, offset) + String.format("%02d", versionNum);
        }

        rcIdx = masterBranchVersion.indexOf(MavenConst.Version.RC);
        if (rcIdx != -1) {
            //在master分支基础上递增大版本并且RC01，比如：1.0-RC01升级为1.1-RC01
            //比如输入：1.2.3-RC01 输出:1.2.4-RC01
            String versionPart = StringUtils.split(masterBranchVersion, '-')[0];//得到1.2.3
            String[] versionPartArr = StringUtils.split(versionPart, '.');//得到1,2,3
            int smallVersion = NumberUtils.toInt(versionPartArr[2]) + 1;//得到4
            return versionPartArr[0] + "." + versionPartArr[1] + "." + smallVersion + "-RC01";
        }

        rcIdx = currentVersion.indexOf(MavenConst.Version.SNAPSHOT);
        if (rcIdx != 0) {
            //直接将snapshot改成RC01
            return currentVersion.substring(0, rcIdx) + "RC01";
        }
        return currentVersion;
    }


    /**
     * 生成snapshot版本
     *
     * @param masterBranchVersion
     * @param currentVersion
     * @param branchName
     * @return
     */
    public static String generateSnapshotVersion(String masterBranchVersion, String currentVersion, String branchName) {
        int idx = currentVersion.indexOf(MavenConst.Version.SNAPSHOT);
        if (idx != -1) {
            //当前本身就是snapshot版本
            return currentVersion;
        }
        idx = currentVersion.indexOf(MavenConst.Version.RC);
        if (idx != -1) {
            //在master分支基础上递增大版本并且RC01，比如：1.0-RC01升级为1.1-RC01
            //比如输入：1.2.3-RC01 输出:1.2.4-RC01
            String versionPart = StringUtils.split(masterBranchVersion, '-')[0];//得到1.2.3
            String[] versionPartArr = StringUtils.split(versionPart, '.');//得到1,2,3
            int smallVersion = NumberUtils.toInt(versionPartArr[2]) + 1;//得到4
            String snapshotVersionFormat = VersionToolConfig.getInstance().getSnapshotVersionFormat();//snapshot版本格式
            String newVersion = versionPartArr[0] + "." + versionPartArr[1] + "." + smallVersion;
            return snapshotVersionFormat.replace("{version}", newVersion).replace("{branch}", formatBranchName(branchName));
        }
        return currentVersion;
    }


    private static String formatBranchName(String branchName) {
        if (branchName.indexOf('/') == -1) {
            return branchName;
        }
        String[] split = StringUtils.split(branchName, '/');
        return split[split.length - 1];
    }

}
