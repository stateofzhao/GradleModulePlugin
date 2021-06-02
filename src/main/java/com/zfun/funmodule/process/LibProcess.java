package com.zfun.funmodule.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.IProcess;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.io.File;
import java.io.IOException;

//apply plugin 更改为 com.android.library，去掉Manifest中的launcherActivity
public class LibProcess implements IProcess {
    boolean needRecoverBuildFile = false;
    boolean needRecoverManifestFile = false;

    private File oriManifestFile;
    private File savedManifestFile;

    @Override
    public void beforeEvaluate(final Project project) {
        try {
            needRecoverBuildFile = FileUtil.replaceFileText(project.getBuildFile(), "com\\.android\\.application", "com.android.library");

            //将manifest复制走，用完了再还回来~
            final String moduleName = project.getName();
            File desManifestParentDir = new File(project.getRootDir() + "/" + Constants.sBuildTempFile+"/" + moduleName + "_manifest" );
            if (!desManifestParentDir.exists()) {
                desManifestParentDir.mkdirs();
            }
            final File srcManifest = new File(FileUtil.findManifestPath(project));
            final File desManifest = new File(desManifestParentDir.getAbsolutePath() , Constants.sManifestName);
            oriManifestFile = srcManifest;
            savedManifestFile = desManifest;
            if (desManifest.exists()) {
                desManifest.delete();
            }
            //修改manifest
            try {
                LogMe.D(project.getName() + "：manifestPath==" + srcManifest.toPath());
                LogMe.D(project.getName() + "：desManifestPath==" + desManifest.toPath());
                FileUtil.copy(srcManifest, desManifest);
                LogMe.D(project.getName() + "：开始修改xml文件");
                FileUtil.removeManifestLauncherActivity(srcManifest);
                needRecoverManifestFile = true;
            } catch (Exception e) {
                //
            }
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void afterEvaluate(final Project project) {
    }

    @Override
    public void buildStarted(Project project, Gradle gradle) {
    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {
        //manifest的还原一定要在这里，因为build.gradle解析完毕后 android 插件会根据build.gradle中指定的Manifest文件来进行编译
        try {
            if (needRecoverBuildFile) {
                FileUtil.replaceFileText(project.getBuildFile(), "com\\.android\\.library", "com.android.application");
            }
            if (needRecoverManifestFile) {
                if(oriManifestFile.exists()){
                    oriManifestFile.delete();
                }
                LogMe.D("还原Manifest：savedManifestFile：" + savedManifestFile.toString());
                LogMe.D("还原Manifest：into：" + oriManifestFile.toString());
                FileUtil.copy(savedManifestFile, oriManifestFile);
            }
        } catch (IOException e) {
            //
        }
    }
}
