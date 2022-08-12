package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.BasePlugin;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.extension.AppLibEx;
import com.zfun.funmodule.processplug.extension.BuildTypeEx;
import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import com.zfun.funmodule.processplug.process.EmptyProcess;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.Pair;
import com.zfun.funmodule.util.Util;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//扩展方法，两步搞定：
//1，定义一个Extension，需要继承自BaseExtension，并修改 getMyExtension() 方法，将定义的Extension添加进来。
//2，在 FactoryProvider#createFactory() 方法来生成你的IProcessFactory。
//3，实现IProcess来实现梦想吧。

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
        LogMe.D("beforeEvaluate == " + project.getName());
        final IProcess[] process = findProcess(project);
        if (null == process) {
            return;
        }
        for (IProcess aProcess : process) {
            if (null == aProcess) {
                continue;
            }
            LogMe.D("beforeEvaluate == " + project.getName() +" == 执行aProcess："+aProcess.getClass().getSimpleName()+" beforeEvaluate()");
            aProcess.beforeEvaluate(project);
        }
    }

    @Override
    protected void afterEvaluate(Project project) {
        LogMe.D("afterEvaluate == " + project.getName());
        final IProcess[] process = findProcess(project);
        if (null == process) {
            return;
        }
        for (IProcess aProcess : process) {
            if (null == aProcess) {
                continue;
            }
            LogMe.D("afterEvaluate == " + project.getName() +" == 执行aProcess："+aProcess+" afterEvaluate()");
            aProcess.afterEvaluate(project);
        }
    }

    @Override
    protected void projectsEvaluated(Gradle gradle) {
        final Project rootProject = gradle.getRootProject();
        final Set<Project> projects = rootProject.getAllprojects();
        for (Project aProject:projects){
            LogMe.D("projectsEvaluated == " + aProject.getName());
            final IProcess[] process = findProcess(aProject);
            if (null == process) {
                return;
            }
            for (IProcess aProcess : process) {
                if (null == aProcess) {
                    continue;
                }
                LogMe.D("projectsEvaluated == " + aProject.getName() +" == 执行aProcess："+aProcess+" projectsEvaluated()");
                aProcess.projectsEvaluated(aProject);
            }
        }
    }

    @Override
    protected void buildFinished(BuildResult buildResult) {
        if(null == buildResult || null == buildResult.getGradle()){
            return;
        }
        final Project rootProject = buildResult.getGradle().getRootProject();
        final Set<Project> projects = rootProject.getAllprojects();
        for (Project aProject : projects) {
            LogMe.D("buildFinished == " + aProject.getName());
            final IProcess[] process = findProcess(aProject);
            if (null == process) {
                return;
            }
            for (IProcess aProcess : process) {
                if (null == aProcess) {
                    continue;
                }
                LogMe.D("buildFinished == " + aProject.getName() +" == 执行aProcess："+aProcess+" buildFinished()");
                aProcess.buildFinished(aProject, buildResult);
            }
        }
    }

    @Override
    protected Pair<String, Class<? extends BaseExtension>>[] getMyExtension() {
        return new Pair[]{
                new Pair<>(Constants.sAppLibExtensionName, AppLibEx.class),
                new Pair<>(Constants.sInjectExtensionName, InjectEx.class),
                new Pair<>(Constants.sMultiChannelExName, MultiChannelEx.class),
                new Pair<>(Constants.sDebugExtensionName, BuildTypeEx.class)
        };
    }

    @Nullable
    private IProcess[] findProcess(Project project) {
        LogMe.D("findProcess："+project.getName());
        if (!processMap.containsKey(project)) {
            final BaseExtension[] exs = findPluginEx(project);//获取根build.gradle中的配置参数
            if (null == exs) {
                processMap.put(project, new IProcess[0]);
                return null;
            }
            boolean createProcess = false;
            final IProcess[] processes = new IProcess[exs.length];
            int i = 0;
            for (BaseExtension aBaseEx : exs) {
                final IProcessFactory factory = processFactoryProvider.createFactory(project, aBaseEx);
                if (null == factory) {
                    continue;
                }
                final IProcess aProcess = factory.createProcess(project, aBaseEx);
                if(null != aProcess && !(aProcess instanceof EmptyProcess)){
                    createProcess = true;
                }
                processes[i] = aProcess;
                i++;
                LogMe.D("创建Process："+project.getName() + " == " + aProcess.getClass().getSimpleName());
            }
            if (createProcess) {
                processMap.put(project, processes);//将根根build.gradle的参数生成Process设置给子Project
            }
        }
        IProcess[] iProcesses = processMap.get(project);
        if (null == iProcesses || iProcesses.length == 0) {
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
        //根Project配置参数
        final Project rootProject = project.getRootProject();
        final BaseExtension[] tempArr_root = new BaseExtension[allEx.length];
        int i = 0;
        for (Pair<String, Class<? extends BaseExtension>> aPair : allEx) {
            BaseExtension aEx = rootProject.getExtensions().findByType(aPair.getValue());
            if(null != aEx){
                aEx.extensionName = aPair.getKey();
            }
            if (null != aEx && !aEx.isEmpty()) {
                tempArr_root[i] = aEx;
            }
            i++;
        }

        //下面为错误代码 ------------
        /*//查找自己的配置参数，如果有就覆盖掉根Project的配置参数
        final BaseExtension[] tempArr_me = new BaseExtension[allEx.length];
        i = 0;
        for (Pair<String, Class<? extends BaseExtension>> aPair : allEx) {
            BaseExtension aEx = project.getExtensions().findByType(aPair.getValue());
            if (null != aEx && !aEx.isEmpty()) {
                tempArr_me[i] = aEx;
                LogMe.D(project.getName() + "的参数：" + aPair.getKey() + " == " + aEx);
            }
            i++;
        }
        //最终合并
        for (int j = 0; j < allEx.length; j++) {
            BaseExtension meEx = tempArr_me[j];
            if (meEx != null) {
                tempArr_root[j] = meEx;
            }
        }*///------------

        final BaseExtension[] result = Arrays.stream(tempArr_root).filter(Objects::nonNull).toArray(BaseExtension[]::new);
        insertDefault(result, project);
        LogMe.D("根Project的参数大小：" + result.length);
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
            if (baseExtension instanceof AppLibEx) {
                final AppLibEx appLibEx = (AppLibEx) baseExtension;
                if (appLibEx.mainAppName == null || appLibEx.mainAppName.trim().length() == 0) {
                    appLibEx.mainAppName = Constants.sDefaultAppName;
                }
                String[] libNames = appLibEx.libName;
                //给 appLibEx.libName 设置为所有子Project
                if (null == libNames || libNames.length == 0) {
                    final Set<Project> allProject = project.getAllprojects();
                    final int projectSize = allProject.size() - 2;//减去 根工程、主工程
                    if (projectSize > 0) {
                        libNames = new String[allProject.size()];
                        int index = 0;
                        for (Project aProject:allProject){
                            if(Util.isRootProject(aProject)){
                                continue;
                            }
                            if(aProject.getName().equals(appLibEx.mainAppName)){
                                continue;
                            }
                            libNames[index] = aProject.getName();
                            index = index + 1;
                        }
                    }
                }
                appLibEx.libName = libNames;
            }
            LogMe.D("根Project的参数：" + baseExtension.extensionName + " == " + baseExtension);
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
