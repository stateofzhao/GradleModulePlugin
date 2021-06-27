package com.zfun.funmodule;

import com.zfun.funmodule.util.LogMe;
import javafx.util.Pair;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//使用IProcess来处理事务
class ModulesConfigPlugin extends BasePlugin {
    private final FactoryProvider processFactoryProvider;
    private final Map<Project, IProcess[]> processMap;

    public ModulesConfigPlugin() {
        processFactoryProvider = new FactoryProvider();
        processMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void beforeEvaluate(Project project) {
        final IProcess[] process = findProcess(project);
        if(null == process){
            return;
        }
        for (IProcess aProcess:process){
            if(null == aProcess){
                continue;
            }
            aProcess.beforeEvaluate(project);
            LogMe.D("beforeEvaluate：" + project.getName() + "==process==" + aProcess);
        }
    }

    @Override
    protected void afterEvaluate(Project project) {
        final IProcess[] process = findProcess(project);
        if(null == process){
            return;
        }
        for (IProcess aProcess:process){
            if(null == aProcess){
                continue;
            }
            aProcess.afterEvaluate(project);
            LogMe.D("afterEvaluate：" + project.getName() + "==process==" + aProcess);
        }
    }

    @Override
    protected void buildStarted(Project project, Gradle gradle) {
        final IProcess[] process = findProcess(project);
        if(null == process){
            return;
        }
        for (IProcess aProcess:process){
            if(null == aProcess){
                continue;
            }
            aProcess.buildStarted(project,gradle);
            LogMe.D("buildStarted：" + project.getName() + "==process==" + aProcess);
        }
    }

    @Override
    protected void buildFinished(Project project, BuildResult buildResult) {
        final IProcess[] process = findProcess(project);
        if(null == process){
            return;
        }
        for (IProcess aProcess:process){
            if(null == aProcess){
                continue;
            }
            aProcess.buildFinished(project,buildResult);
            LogMe.D("buildFinished：" + project.getName() + "==process==" + aProcess);
        }
    }

    @Override
    protected Pair<String, Class<? extends BaseExtension>>[] getMyExtension() {
        return new Pair[]{
                new Pair<>(Constants.sAppLibExtensionName, AppLibEx.class),
                new Pair<>(Constants.sInjectExtensionName, InjectEx.class)
        };
    }

    private IProcess[] findProcess(Project project) {
        final BaseExtension[] exs = findPluginEx(project);
        if (null == exs) {
            return null;
        }
        IProcess[] processes = processMap.get(project);
        if (null == processes) {
            processes = new IProcess[exs.length];
            int i = 0;
            for (BaseExtension aBaseEx : exs) {
                IProcessFactory factory = processFactoryProvider.createFactory(project, aBaseEx);
                if (null == factory) {
                    continue;
                }
                IProcess aProcess = factory.createProcess(project, aBaseEx);
                processes[i] = aProcess;
                LogMe.D("创建Process：" + aProcess.toString());
            }
            processMap.put(project, processes);
        }
        return processes;
    }

    private BaseExtension[] findPluginEx(Project project) {
        final Pair<String, Class<? extends BaseExtension>>[] allEx = getMyExtension();
        if (null == allEx) {
            return null;
        }
        final Project rootProject = project.getRootProject();
        final BaseExtension[] result = new BaseExtension[allEx.length];
        int i = 0;
        for (Pair<String, Class<? extends BaseExtension>> aPair : allEx) {
            BaseExtension aEx = project.getExtensions().findByType(aPair.getValue());
            if (null == aEx) {
                aEx = rootProject.getExtensions().findByType(aPair.getValue());
            }
            result[i] = aEx;
            i++;
            LogMe.D(project.getName()+"参数："+aEx);
        }
        insertDefault(result, project);
        return result;
    }

    private void insertDefault(BaseExtension[] extensions, Project project) {
        if (null == extensions) {
            return;
        }
        for (BaseExtension baseExtension : extensions) {
            if (null == baseExtension) {
                continue;
            }
            if (null == baseExtension.moduleName || baseExtension.moduleName.trim().length() == 0) {
                baseExtension.moduleName = project.getName();
            }
            if(baseExtension instanceof AppLibEx){
                AppLibEx appLibEx = (AppLibEx) baseExtension;
                if (appLibEx.mainAppName == null || appLibEx.mainAppName.trim().length() == 0) {
                    appLibEx.mainAppName = Constants.sDefaultAppName;
                }
            }
        }
    }

    /*private IProcess findProcess(Project project) {
        IProcess process = processMap.get(project);
        if (null == process) {
            AppLibEx appLibEx = findPluginEx(project);
            process = appLibFactory.createProcess(project, appLibEx);
            processMap.put(project, process);
            LogMe.D("创建Process");
        }
        return process;
    }*/

    /*private BaseExtension findPluginEx(Project project) {
        AppLibEx resultEx = project.getExtensions().findByType(getMyExtension());
        final AppLibEx rootEx = project.getRootProject().getExtensions().findByType(getMyExtension());
        if (null == rootEx) {
            return null;
        }
        if (rootEx.mainAppName == null || rootEx.mainAppName.trim().length() == 0) {
            rootEx.mainAppName = Constants.sDefaultAppName;
        }
        if (null == resultEx) {
            resultEx = new AppLibEx();
        }
        resultEx.runType = rootEx.runType;
        resultEx.mainAppName = rootEx.mainAppName;
        resultEx.buildType = rootEx.buildType;
        resultEx.libName = rootEx.libName;

        if (null == resultEx.moduleName || resultEx.moduleName.trim().length() == 0) {
            resultEx.moduleName = project.getName();
        }
        LogMe.D("Project的Extension：" + resultEx);
        return resultEx;
    }*/
}
