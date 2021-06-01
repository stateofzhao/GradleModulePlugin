package com.zfun.funmodule;

import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.util.Set;

//每个build.gradle都对应一个自己的Plugin实例
public abstract class BasePlugin implements Plugin<Project> {
    protected abstract Class<? extends BaseExtension> getMyExtension();

    @Override
    public void apply(Project project) {//梦开始的地方
        LogMe.P("插件apply===="+project.getName());
        project.getExtensions().create(Constants.sExtensionName,getMyExtension());//在 apply() 方法中是获取不到 Extension 的值的！但是Task中可以（doFirst()中也不可以获取到）
        configProject(project);
    }

    //根工程build.gradle配置获取到
    protected void onRootProjectExtensionValueGet(Project project,BaseExtension extension){

    }

    protected void beforeEvaluate(Project project){

    }

    protected void afterEvaluate(Project project){

    }

    protected void buildStarted(Project project,Gradle gradle){

    }

    protected void buildFinished(Project project,BuildResult buildResult){

    }

    private void configInRootProjectNoExtensionValue(final Project project){
        addSubProjectBeforeAfterEvaluate(project);

        //-----------------------配置rootProject的监听
        project.beforeEvaluate(new Action<Project>() {//这个是监听不到的，以为此时rootProject已经开始Evaluate了
            @Override
            public void execute(Project project) {

            }
        });
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                if(isRootProject(project)){//根据配置的扩展参数来初始化一些东西
                    final BaseExtension baseExtension = project.getExtensions().findByType(getMyExtension());
                    configInRootProjectHaveExtensionValue(project,baseExtension);
                    onRootProjectExtensionValueGet(project,baseExtension);
                }
                afterEvaluate(project);
            }
        });

        project.getGradle().buildStarted(new Action<Gradle>() { //监听不到，因为到这里时已经buildStarted了
            @Override
            public void execute(Gradle gradle) {
                buildStarted(project,gradle);
            }
        });

        project.getGradle().buildFinished(new Action<BuildResult>() {
            @Override
            public void execute(BuildResult buildResult) {
                buildFinished(project,buildResult);
            }
        });
    }

    private void configInRootProjectHaveExtensionValue(Project project,BaseExtension baseExtension){
        LogMe.isDebug = baseExtension.buildType==Constants.BUILD_DEBUG;
    }

    private void configProject(Project project){
        if(isRootProject(project)){
            configInRootProjectNoExtensionValue(project);
        }
    }

    //给所有子Project注册 beforeEvaluate() 回调
    private void addSubProjectBeforeAfterEvaluate(Project project){
        Set<Project> projects = project.getAllprojects();
        for(final Project aProject:projects){
            if(isRootProject(aProject)){
                continue;
            }
            aProject.beforeEvaluate(new Action<Project>() {
                @Override
                public void execute(Project project) {
                    beforeEvaluate(project);
                }
            });
            aProject.afterEvaluate(new Action<Project>() {
                @Override
                public void execute(Project project) {
                    afterEvaluate(project);
                }
            });
            aProject.getGradle().buildStarted(new Action<Gradle>() {//监听不到
                @Override
                public void execute(Gradle gradle) {
                    buildStarted(aProject,gradle);
                }
            });
            aProject.getGradle().buildFinished(new Action<BuildResult>() {
                @Override
                public void execute(BuildResult buildResult) {
                    buildFinished(aProject,buildResult);
                }
            });
        }
    }

    final protected boolean isRootProject(Project project){
        return project.getRootProject() == project;
    }
}
