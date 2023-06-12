package com.zfun.funmodule;

import com.zfun.funmodule.process.FactoryProvider;
import com.zfun.funmodule.process.IProcess;
import com.zfun.funmodule.process.IProcessFactory;
import com.zfun.funmodule.process.extension.AppLibEx;
import com.zfun.funmodule.process.extension.BuildTypeEx;
import com.zfun.funmodule.process.extension.InjectEx;
import com.zfun.funmodule.process.extension.MultiChannelEx;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.Pair;
import com.zfun.funmodule.util.Util;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.*;

//此插件只可以配置到根目录的build.gradle上！
//
//一次构建（一个Gradle的完整生命周期）是从一个task开始，不管这个task多么简单，都会走完整的Gradle生命周期。
// 当然task如果依赖其它task，那么会解析所有task的依赖然后从最头的task开执行。
//
//1.哪个build.gradle文件中 apply 了插件，哪个build.gradle执行到 apply plugin: 'com.zfun.funmodule' 这行代码时，
// 就会执行到 apply的插件的 apply(Project)方法；
//2.每个build.gradle apply的插件都对应一个自己的Plugin实例，互不干扰，例如，在同一个根Project中 Project A的build.gradle 中apply了插件A，
// Project B的build.gradle也apply插件A，那么A和B的插件都独立存在互相不干扰；
//3.不 apply 插件的build.gradle，不会触发 插件的 apply(Project)方法；
//4.一个插件实体一种类型的Extension只有一个实体对象，比如，在根工程中的build.gradle中apply了插件A，然后在根工程的build.gradle中配置了A的扩展参数Extension 1，
// 之后又在其子工程的build.gradle中重新配置了A的Extension 1，此时根build.gradle中配置的Extension就会被覆盖。
// 【这里容易混淆，并不是哪个build.gradle配置Extension，就是哪个build.gradle拥有此Extension，始终只有apply插件的那个build.gradle拥有此Extension】
public class ModulesConfigPlugin implements Plugin<Project> {
    private final Map<Project, IProcess[]> mProcessMap = new HashMap<>();
    private final FactoryProvider processFactoryProvider = new FactoryProvider();

    @Override
    public void apply(Project project) {//梦开始的地方
        if (!Util.isRootProject(project)) { //只有 根build.gradle 起作用
            return;
        }
        LogMe.D("ModulePlugin插件入口apply ==== " + project.getName());
        //一个"串行"build.gradle上只能存在一个同名的Extension，比如此Extension，已经在 根build.gradle 创建了，
        // 那么就不能在 子build.gradle 上面再次创建与此同名的Extension了。<br>

        //由于上面的判断，只对根 build.gradle 起作用，所以下面这个Extension只有根build.gradle有，
        // 所有在子build.gradle 对此Extension的赋值都是对根build.gradle Extension值的覆盖。<br>

        //在 apply() 方法中是获取不到 Extension 的值的！但是Task中可以（doFirst()中也不可以获取到）
        for (Pair<String, Class<? extends BaseExtension>> aPair : getMyExtension()) {
            //一旦创建，project就含有了创建的Extension，用户在build.gradle中的调用只是给其设置值，
            // 如果用户没有调用那么获取的Extension就是默认值。
            project.getExtensions().create(aPair.getKey(), aPair.getValue(), project);
        }

        //init，rootProject解析完毕后才会开始解析其子Project
        project.afterEvaluate(rootProject -> {
            final BaseExtension[] extensions = findPluginEx(rootProject);
            final Set<Project> allProjects = rootProject.getSubprojects();
            allProjects.forEach(subProject -> {
                final IProcess[] processes = createProcess(extensions, subProject);
                mProcessMap.put(subProject, processes);
                LogMe.D(subProject.getName() + "添加Process：", false);
                for (int i = 0; i < processes.length; i++) {
                    final IProcess aP = processes[i];
                    if (i == processes.length - 1) {
                        LogMe.D(aP.getClass().getSimpleName()+"。", true);
                    } else {
                        LogMe.D(aP.getClass().getSimpleName() + "、", false);
                    }
                }
                LogMe.D("\n", false);
            });
        });

        //
        project.getGradle().projectsEvaluated(gradle -> {
            final Set<Project> allProjects = project.getSubprojects();
            allProjects.forEach(subProject -> {
                final IProcess[] processes = mProcessMap.get(subProject);
                for (final IProcess aProcess : processes) {
                    aProcess.projectsEvaluated(subProject);
                }
            });
        });

        //notify
        project.getGradle().buildFinished(buildResult -> {
            final Set<Project> allProjects = project.getSubprojects();
            allProjects.forEach(subProject -> {
                final IProcess[] processes = mProcessMap.get(subProject);
                for (final IProcess aProcess : processes) {
                    aProcess.buildFinished(subProject, buildResult);
                }
            });
        });

        //notify
        project.getSubprojects().forEach(subProject -> {
            subProject.beforeEvaluate(p -> {
                final IProcess[] processes = mProcessMap.get(p);
                for (final IProcess aProcess : processes) {
                    aProcess.beforeEvaluate(p);
                }
            });
            subProject.afterEvaluate(p -> {
                final IProcess[] processes = mProcessMap.get(p);
                for (final IProcess aProcess : processes) {
                    aProcess.afterEvaluate(p);
                }
            });
        });
    }

    private IProcess[] createProcess(BaseExtension[] extensions, Project project) {
        if (null == extensions) {
            return new IProcess[0];
        }
        final List<IProcess> processList = new ArrayList<>();
        for (BaseExtension aBaseEx : extensions) {
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


    protected Pair[] getMyExtension() {
        return new Pair[]{
                new Pair<>(Constants.sAppLibExtensionName, AppLibEx.class),
                new Pair<>(Constants.sInjectExtensionName, InjectEx.class),
                new Pair<>(Constants.sMultiChannelExName, MultiChannelEx.class),
                new Pair<>(Constants.sDebugExtensionName, BuildTypeEx.class)
        };
    }


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
            //
            if (aEx instanceof BuildTypeEx) {
                LogMe.isDebug = ((BuildTypeEx) aEx).debug;
            }
            //
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

    private void insertDefault(BaseExtension[] extensions, Project project) {
        if (null == extensions) {
            return;
        }
        LogMe.D_Divider(project.getName(), "extension参数--start");
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
            LogMe.D_Divider(project.getName(), baseExtension.extensionName + "：" + baseExtension);
        }
        LogMe.D_Divider(project.getName(), "extension参数--end");
    }
}
