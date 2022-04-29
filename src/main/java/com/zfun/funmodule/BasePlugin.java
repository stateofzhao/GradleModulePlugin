package com.zfun.funmodule;

import com.zfun.funmodule.processplug.extension.DebugEx;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.Pair;
import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.util.Set;

//此插件只可以配置到根目录的build.gradle上！
//
//1.哪个build.gradle文件中 apply 了插件，哪个build.gradle执行到 apply plugin: 'com.zfun.funmodule' 这行代码时，
// 就会执行到 apply的插件的 apply(Project)方法；
//2.每个build.gradle apply的插件都对应一个自己的Plugin实例，互不干扰，例如，在同一个根Project中 Project A的build.gradle 中apply了插件A，
// Project B的build.gradle也apply插件A，那么A和B的插件都独立存在互相不干扰；
//3.不 apply 插件的build.gradle，不会触发 插件的 apply(Project)方法；
//4.一个插件实体一种类型的Extension只有一个实体对象，比如，在根工程中的build.gradle中apply了插件A，然后在根工程的build.gradle中配置了A的扩展参数Extension 1，
// 之后又在其子工程的build.gradle中重新配置了A的Extension 1，此时根build.gradle中配置的Extension就会被覆盖。
// 【这里容易混淆，并不是哪个build.gradle配置Extension，就是哪个build.gradle拥有此Extension，始终只有apply插件的那个build.gradle拥有此Extension】
public abstract class BasePlugin implements Plugin<Project> {
    protected abstract Pair<String,Class<? extends BaseExtension>>[] getMyExtension();

    @Override
    public void apply(Project project) {//梦开始的地方
        if(!isRootProject(project)){ //只有 根build.gradle 起作用
            return;
        }
        LogMe.P("插件入口apply ==== "+project.getName());
        //一个"串行"build.gradle上只能存在一个同名的Extension，比如此Extension，已经在 根build.gradle 创建了，
        // 那么就不能在 子build.gradle 上面再次创建与此同名的Extension了。<br/>

        //由于上面的判断，只有只对 根 build.gradle 起作用，所以下面这个Extension只有 根build.gradle 有，
        // 所有在 子build.gradle 对此Extension的赋值都是对 根build.gradle Extension值的覆盖。<br/>

        //在 apply() 方法中是获取不到 Extension 的值的！但是Task中可以（doFirst()中也不可以获取到）
        for(Pair<String,Class<? extends BaseExtension>> aPair:getMyExtension()){
            //一旦创建，project就含有了创建的Extension，用户在build.gradle中的调用只是给其设置值，
            // 如果用户没有调用那么获取的Extension就是默认值。
            project.getExtensions().create(aPair.getKey(),aPair.getValue(),project);
        }
        configProject(project);
    }

    protected void beforeEvaluate(Project project){

    }

    protected void afterEvaluate(Project project){

    }

    protected void projectsEvaluated(Project project){

    }

    protected void buildFinished(Project project,BuildResult buildResult){

    }

    private void configInRootProjectNoExtensionValue(final Project project){
        addSubProjectListener(project);

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
                    configInRootProjectHaveExtensionValue(project);
                }
                afterEvaluate(project);
            }
        });

        project.getGradle().buildFinished(new Action<BuildResult>() {
            @Override
            public void execute(BuildResult buildResult) {
                buildFinished(project,buildResult);
            }
        });
    }

    private void configInRootProjectHaveExtensionValue(Project project){
        Pair<String,Class<? extends BaseExtension>>[] exs = getMyExtension();
        if(null == exs){
            return;
        }
        boolean isDebug = false;
        for (Pair<String, Class<? extends BaseExtension>> aPair : exs) {
            BaseExtension baseExtension = project.getExtensions().findByType(aPair.getValue());
            if (null == baseExtension) {
                continue;
            }
            if (baseExtension instanceof DebugEx) {
                isDebug = ((DebugEx) baseExtension).buildType == Constants.BUILD_DEBUG;
            }
        }
        LogMe.isDebug =isDebug;
        LogMe.P("当前插件的buildType是否为Debug模式："+LogMe.isDebug);
    }

    private void configProject(Project project){
        if(isRootProject(project)){
            configInRootProjectNoExtensionValue(project);
        }
    }

    //给所有子Project注册 回调
    private void addSubProjectListener(Project project){
        Set<Project> projects = project.getAllprojects();
        LogMe.P("包含几个Project ==== "+projects.size());
        for(final Project aProject:projects){
            if(isRootProject(aProject)){
                continue;
            }
            LogMe.P("addSubProjectListener - projectName ==== "+aProject.getName());
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
            //注意区分gradle#projectsEvaluated()生命周期和上面的Project的afterEvaluate()生命周期。
            //gradle#projectsEvaluated() 会在根工程包含的所有project的afterEvaluate()后回调。
            aProject.getGradle().addBuildListener(new BuildAdapter(){
                @Override
                public void projectsEvaluated(Gradle gradle) {
                    BasePlugin.this.projectsEvaluated(aProject);
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
        return project.getRootProject().getName().equals(project.getName());
    }
}
