package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.ProjectFileRestoreMgr;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.ManifestEditor;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

//fixme build.gradle和 AndroidManifest.xml 都是从默认路径下寻找的，暂不支持变异路径，后续应该添加配置参数来可以配置它们的变异路径
//1，apply plugin 更改为 com.android.library；
//2，删除build.gradle中的android{applicationId}；
//3，去掉Manifest中的launcherActivity；
public class LibProcess implements IProcess {
    boolean needRecoverBuildFile = false;
    boolean needRecoverManifestFile = false;

    String oriBuildFile;
    String oriManifestFile;

    @Override
    public void beforeEvaluate(final Project project) {
        try {
            /*final Map<String,Object>  plugins = project.getConvention().getPlugins();
            LogMe.D("Project："+project.getName()+" ======插件列表：");
            Set<String> keySet = plugins.keySet();
            for (String key:keySet){
                LogMe.D("key == "+key +"  value == "+plugins.get(key));
            }
            LogMe.D("Project："+project.getName()+" ======插件列表End");*/

            final File buildGradleFile = project.getBuildFile();
            oriBuildFile = buildGradleFile.getAbsolutePath();
            final String oriText = FileUtil.getText(buildGradleFile);
            //1，更改application插件->lib插件
            String replaceText = oriText.replaceAll("'com\\.android\\.application'", "'com.android.library'" + Constants.sComments + "：com.android.application -> com.android.library");
            replaceText = replaceText.replaceAll("\"com\\.android\\.application\"", "'com.android.library'" + Constants.sComments + "：com.android.application -> com.android.library");
            //2，移除applicationId
            replaceText = StringUtils.editGradleText(Constants.sDefaultApplicationIdRegexPre, "applicationId", replaceText, null);
            if (!oriText.equals(replaceText)) {
                needRecoverBuildFile = true;
                LogMe.D("Lib移除application插件和applicationId后：");
                LogMe.D(replaceText);
                LogMe.D("==========================");
            }
            if (needRecoverBuildFile) {
                ProjectFileRestoreMgr.saveFile(project, oriBuildFile); //将build.gradle复制走，用完了再还回来~
                FileUtil.write(buildGradleFile, replaceText);
            }

            //***更改Manifest***
            final File srcManifest = new File(FileUtil.findManifestPath(project));
            oriManifestFile = srcManifest.getAbsolutePath();
            if (!srcManifest.exists()) {
                return;
            }
            //3，修改manifest
            try {
                final String savedFile = ProjectFileRestoreMgr.saveFile(project, oriManifestFile);//将manifest复制走，用完了再还回来~
                LogMe.D(project.getName() + "：开始修改xml文件 - ManifestEditor");
                new ManifestEditor(savedFile, srcManifest.getAbsolutePath()).transform(new ManifestEditor.RemoveActivityLauncher());
                needRecoverManifestFile = true;
                LogMe.D("修改后的Manifest：" + FileUtil.getText(srcManifest));
            } catch (Exception e) {
                throw new RuntimeException("LibProcess == 更改 " + project.getName() + " manifest文件：去掉Activity的launcher属性 【失败】：" + e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("LibProcess == 更改 " + project.getName() + " build.gradle文件【失败】：" + e.getMessage());
        }
    }

    @Override
    public void afterEvaluate(final Project project) {
    }

    @Override
    public void projectsEvaluated(Project project) {
    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {
        //manifest的还原一定要在这里，因为build.gradle解析完毕后 android 插件会根据build.gradle中指定的Manifest文件来进行编译
        try {
            if (needRecoverBuildFile) {
                ProjectFileRestoreMgr.restoreFile(project, oriBuildFile);
                LogMe.D("build.gradle 还原完成");
            }
            if (needRecoverManifestFile) {
                ProjectFileRestoreMgr.restoreFile(project, oriManifestFile);
                LogMe.D("Manifest 还原完成");
            }
        } catch (IOException e) {
            throw new RuntimeException("LibProcess == 还原 " + project.getName() + " build.gradle/Manifest文件【失败】：" + e.getMessage());
        }
    }
}
