package com.zfun.funmodule;

import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.util.Hashtable;
import java.util.Map;

//用来管理module模块，有两个功能：
// 1，自动切换module模块的 apply plugin: 'com.android.application' 和  apply plugin: 'com.android.library'
// 2，自动去掉module模块的Manifest.xml文件中的 android.intent.category.LAUNCHER Activity配置。
class ModulesConfigPlugin extends BasePlugin {
    private final ProcessFactory processFactory;
    private Map<Project,IProcess> processMap;

    public ModulesConfigPlugin() {
        processFactory = new ProcessFactory();
        processMap = new Hashtable<>();
    }

    @Override
    protected void beforeEvaluate(Project project) {
        final IProcess process = findProcess(project);
        process.beforeEvaluate(project);
        LogMe.D("beforeEvaluate："+project.getName() +"==process=="+ process);
    }

    @Override
    protected void afterEvaluate(Project project) {
        LogMe.D("afterEvaluate："+project.getName() +"==process==");
        final IProcess process = findProcess(project);
        process.afterEvaluate(project);
    }

    @Override
    protected void buildStarted(Project project, Gradle gradle) {
        final IProcess process = findProcess(project);
        process.buildStarted(project,gradle);
    }

    @Override
    protected void buildFinished(Project project, BuildResult buildResult) {
        final IProcess process = findProcess(project);
        process.buildFinished(project,buildResult);
    }

    @Override
    protected Class<ModulesEx> getMyExtension() {
        return ModulesEx.class;
    }

    private IProcess findProcess(Project project){
        IProcess process = processMap.get(project);
        if(null == process){
            ModulesEx modulesEx = findPluginEx(project);
            process = processFactory.createProcess(project,modulesEx);
            processMap.put(project,process);
            LogMe.D("创建Process");
        }
        return process;
    }

    private ModulesEx findPluginEx(Project project){
        ModulesEx resultEx = project.getExtensions().findByType(getMyExtension());
        final ModulesEx rootEx = project.getRootProject().getExtensions().findByType(getMyExtension());
        if(null == rootEx){
            return null;
        }
        if(rootEx.mainAppName == null || rootEx.mainAppName.trim().length()==0){
            rootEx.mainAppName = Constants.sDefaultAppName;
        }
        if(null == resultEx){
            resultEx = new ModulesEx();
        }
        resultEx.runType = rootEx.runType;
        resultEx.mainAppName = rootEx.mainAppName;
        resultEx.buildType = rootEx.buildType;
        resultEx.libName = rootEx.libName;

        if (null == resultEx.moduleName || resultEx.moduleName.trim().length() == 0) {
            resultEx.moduleName = project.getName();
        }
        LogMe.D("Project的Extension："+resultEx);
        return resultEx;
    }
}
