package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.BasePlugin;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.extension.AppLibEx;
import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.Pair;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

//使用IProcess来处理事务
public class ModulesConfigPlugin extends BasePlugin {
    private final FactoryProvider processFactoryProvider;
    private final Map<Project, IProcess[]> processMap;

    public ModulesConfigPlugin() {
        processFactoryProvider = new FactoryProvider();
        processMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void beforeEvaluate(Project project) {
        LogMe.D("beforeEvaluate == "+project.getName());
        final IProcess[] process = findProcess(project);
        if(null == process){
            return;
        }
        for (IProcess aProcess:process){
            if(null == aProcess){
                continue;
            }
            aProcess.beforeEvaluate(project);
        }
    }

    @Override
    protected void afterEvaluate(Project project) {
        LogMe.D("afterEvaluate == "+project.getName());
        final IProcess[] process = findProcess(project);
        if(null == process){
            return;
        }
        for (IProcess aProcess:process){
            if(null == aProcess){
                continue;
            }
            aProcess.afterEvaluate(project);
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
        }
    }

    @Override
    protected Pair<String, Class<? extends BaseExtension>>[] getMyExtension() {
        return new Pair[]{
                new Pair<>(Constants.sAppLibExtensionName, AppLibEx.class),
                new Pair<>(Constants.sInjectExtensionName, InjectEx.class),
                new Pair<>(Constants.sMultiChannelExName, MultiChannelEx.class)
        };
    }

    @Nullable
    private IProcess[] findProcess(Project project) {
        if (!processMap.containsKey(project)) {
            final BaseExtension[] exs = findPluginEx(project);
            if (null == exs) {
                processMap.put(project, new IProcess[0]);
                return null;
            }
            final IProcess[] processes = new IProcess[exs.length];
            int i = 0;
            for (BaseExtension aBaseEx : exs) {
                IProcessFactory factory = processFactoryProvider.createFactory(project, aBaseEx);
                if (null == factory) {
                    continue;
                }
                IProcess aProcess = factory.createProcess(project, aBaseEx);
                processes[i] = aProcess;
                LogMe.D(project.getName() + " == 创建Process：" + aProcess.getClass().getSimpleName());
            }
            processMap.put(project, processes);
        }
        IProcess[] iProcesses = processMap.get(project);
        if(iProcesses.length == 0){
            return null;
        }
        return iProcesses;
    }

    @Nullable
    private BaseExtension[] findPluginEx(Project project) {
        final Pair<String, Class<? extends BaseExtension>>[] allEx = getMyExtension();
        if (null == allEx) {
            return null;
        }
        final Project rootProject = project.getRootProject();
        final BaseExtension[] tempArr = new BaseExtension[allEx.length];
        int i = 0;
        for (Pair<String, Class<? extends BaseExtension>> aPair : allEx) {
            BaseExtension aEx = rootProject.getExtensions().findByType(aPair.getValue());
            if(null != aEx && !aEx.isEmpty()){
                tempArr[i] = aEx;
                LogMe.D(aPair.getKey()+" == 参数："+aEx);
            }
            i++;
        }
        final BaseExtension[] result = Arrays.stream(tempArr).filter(Objects::nonNull).toArray(BaseExtension[]::new);
        insertDefault(result, project);
        return result;
    }

    private void insertDefault(@Nullable BaseExtension[] extensions, Project project) {
        if (null == extensions) {
            return;
        }
        for (BaseExtension baseExtension : extensions) {
            if (null == baseExtension) {
                continue;
            }
            if(baseExtension instanceof AppLibEx){
                AppLibEx appLibEx = (AppLibEx) baseExtension;
                if (appLibEx.mainAppName == null || appLibEx.mainAppName.trim().length() == 0) {
                    appLibEx.mainAppName = Constants.sDefaultAppName;
                }
            }
            //检测是否开启debug模式
            if(!LogMe.isDebug){
                if(baseExtension.buildType == Constants.BUILD_DEBUG){
                    LogMe.isDebug = true;
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
