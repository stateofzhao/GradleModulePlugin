package com.zfun.funmodule.process;

import com.zfun.funmodule.IProcess;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

//
public class ConfigBuildFileProcess implements IProcess {

    @Override
    public void beforeEvaluate(Project project) {
        //
    }

    @Override
    public void afterEvaluate(Project project) {
//        if(project.getRootProject() != project){
//            return;
//        }
//        final File newProjectBuildParentDir = new File(project.getRootDir()+"/"+Constants.sBuildTempFile);
//        if(!newProjectBuildParentDir.exists()){
//            newProjectBuildParentDir.mkdirs();
//        }
//        LogMe.D("newProjectBuildParentDir === "+newProjectBuildParentDir);
//        final Map<String,Project> childProjects = project.getChildProjects();
//        final Set<String> keys = childProjects.keySet();
//        for(String aKey:keys){
//            final Project aProject = childProjects.get(aKey);
//            final File oriProjectBuildFile = aProject.getBuildFile();
//            LogMe.D("oriProjectBuildFile === "+oriProjectBuildFile);
//            final File newProjectBuildDir = new File(newProjectBuildParentDir.getAbsolutePath()+"/"+aProject.getName()+"_"+oriProjectBuildFile.getName());
//            LogMe.D("newProjectBuildDir === "+newProjectBuildDir);
//            project.copy(new Action<CopySpec>() {
//                @Override
//                public void execute(CopySpec copySpec) {
//                    copySpec.from(oriProjectBuildFile.getPath()).into(newProjectBuildDir);
//                    aProject.setBuildDir(newProjectBuildDir);
//                    LogMe.D("copy finish");
//                }
//            });
//        }
    }

    @Override
    public void buildStarted(Project project, Gradle gradle) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }
}
