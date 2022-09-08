package com.zfun.funmodule.processplug;

import com.zfun.funmodule.BaseExtension;
import com.zfun.funmodule.BasePlugin;
import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.extension.AppLibEx;
import com.zfun.funmodule.processplug.extension.BuildTypeEx;
import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.processplug.extension.MultiChannelEx;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.Pair;
import com.zfun.funmodule.util.Util;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import javax.annotation.Nullable;
import java.util.*;
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
        LogMe.D_Divider(project.getName(), "beforeEvaluate--start");
        final IProcess[] process = findProcess(project);
        if (null == process) {
            return;
        }
        for (IProcess aProcess : process) {
            if (null == aProcess) {
                continue;
            }
            aProcess.beforeEvaluate(project);
            LogMe.D_Divider(project.getName(), "执行aProcess--beforeEvaluate：" + aProcess.getClass().getSimpleName());
        }
        LogMe.D_Divider(project.getName(), "beforeEvaluate--end");
    }

    @Override
    protected void afterEvaluate(Project project) {
        LogMe.D_Divider(project.getName(), "afterEvaluate--start");
        final IProcess[] process = findProcess(project);
        if (null == process) {
            return;
        }
        for (IProcess aProcess : process) {
            if (null == aProcess) {
                continue;
            }
            aProcess.afterEvaluate(project);
            LogMe.D_Divider(project.getName(), "执行aProcess--afterEvaluate：" + aProcess.getClass().getSimpleName());
        }
        LogMe.D_Divider(project.getName(), "afterEvaluate--end");
    }

    @Override
    protected void projectsEvaluated(Gradle gradle) {
        LogMe.D_Divider("gradle", "projectsEvaluated--start");
        final Project rootProject = gradle.getRootProject();
        final Set<Project> projects = rootProject.getAllprojects();
        for (Project aProject : projects) {
            final IProcess[] process = findProcess(aProject);
            if (null == process) {
                return;
            }
            for (IProcess aProcess : process) {
                if (null == aProcess) {
                    continue;
                }
                aProcess.projectsEvaluated(aProject);
                LogMe.D_Divider(aProject.getName(), "执行aProcess--projectsEvaluated：" + aProcess.getClass().getSimpleName());
            }
        }
        LogMe.D_Divider("gradle", "projectsEvaluated--end");
    }

    @Override
    protected void buildFinished(BuildResult buildResult) {
        if (null == buildResult || null == buildResult.getGradle()) {
            return;
        }
        LogMe.D_Divider("gradle", "buildFinished--start");
        final Project rootProject = buildResult.getGradle().getRootProject();
        final Set<Project> projects = rootProject.getAllprojects();
        for (Project aProject : projects) {
            final IProcess[] process = findProcess(aProject);
            if (null == process) {
                return;
            }
            for (IProcess aProcess : process) {
                if (null == aProcess) {
                    continue;
                }
                aProcess.buildFinished(aProject, buildResult);
                LogMe.D_Divider(aProject.getName(), "执行aProcess--buildFinished：" + aProcess.getClass().getSimpleName());
            }
        }
        LogMe.D_Divider("gradle", "buildFinished--end");
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
        if (!processMap.containsKey(project)) {
            final IProcess[] processes = createProcess(project);
            processMap.put(project, processes);
        } else {//读取非根build.gradle文件中配置的参数 fixme 这种设计有问题，以后在优化下
            final IProcess[] processes = createProcess(project);
            final IProcess[] existedProcesses = processMap.get(project);
            final List<IProcess> insertList = new ArrayList<>();
            //将新的Process插入到已经存在的Process中
            for (IProcess newProcess : processes) {
                boolean needInsert = true;
                for (IProcess extPrecess : existedProcesses) {
                    if (newProcess.getClass().getName().equals(extPrecess.getClass().getName())) {
                        needInsert = false;
                        break;
                    }
                }
                if(needInsert){
                    insertList.add(newProcess);
                }
            }
            final int insertSize = insertList.size();
            final int existedSize = existedProcesses.length;
            if (insertSize > 0) {
                final IProcess[] resultProcesses = new IProcess[existedSize + insertSize];
                for (int i = 0; i < resultProcesses.length; i++) {
                    if (i < existedSize) {
                        resultProcesses[i] = existedProcesses[i];
                    } else {
                        final int insertPos = i % insertSize;
                        resultProcesses[i] = insertList.get(insertPos);
                    }
                }
                processMap.put(project, resultProcesses);
            }
        }
        IProcess[] iProcesses = processMap.get(project);
        if (null == iProcesses || iProcesses.length == 0) {
            return null;
        }
        for (IProcess iProcess : iProcesses) {
            LogMe.D_Divider(project.getName() , "findProcess：" + iProcess.getClass().getSimpleName());
        }
        return iProcesses;
    }

    private IProcess[] createProcess(Project project) {
        final BaseExtension[] exs = findPluginEx(project);//获取根build.gradle中的配置参数
        if (null == exs) {
            return new IProcess[0];
        }
        final List<IProcess> processList = new ArrayList<>();
        for (BaseExtension aBaseEx : exs) {
            final IProcessFactory factory = processFactoryProvider.createFactory(project, aBaseEx);
            if (null == factory) {
                continue;
            }
            final List<IProcess> projectProcesses = factory.createProcess(project, aBaseEx);
            if (null == projectProcesses || projectProcesses.size() == 0) {
                continue;
            }
            processList.addAll(projectProcesses);
        }
        return processList.toArray(new IProcess[0]);
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
            if (null != aEx) {
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
        return result;
    }

    private void insertDefault(@Nullable BaseExtension[] extensions, Project project) {
        if (null == extensions) {
            return;
        }
        LogMe.D_Divider(project.getName(),"extension参数--start");
        for (BaseExtension baseExtension : extensions) {
            if (null == baseExtension) {
                continue;
            }
            if (baseExtension instanceof AppLibEx) {
                final AppLibEx appLibEx = (AppLibEx) baseExtension;
                String[] libNames = appLibEx.libName;
                //给 appLibEx.libName 设置为所有子Project
                if (null == libNames || libNames.length == 0) {
                    final Set<Project> allProject = project.getAllprojects();
                    final int projectSize = allProject.size() - 2;//减去 根工程、主工程（打包的工程 = AppLibEx.mainAppName 配置的工程）
                    if (projectSize > 0) {
                        libNames = new String[projectSize];
                        int index = 0;
                        for (Project aProject : allProject) {
                            if (Util.isRootProject(aProject)) {
                                continue;
                            }
                            if (aProject.getName().equals(appLibEx.appProjectName)) {
                                continue;
                            }
                            libNames[index] = aProject.getName();
                            index = index + 1;
                        }
                    }
                }
                appLibEx.libName = libNames;
            }
            LogMe.D_Divider(project.getName(),baseExtension.extensionName + "：" + baseExtension);
        }
        LogMe.D_Divider(project.getName(),"extension参数--end");
    }
}
