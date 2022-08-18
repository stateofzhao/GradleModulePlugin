package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.ManifestEditor;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

//1，apply plugin 更改为 com.android.library；
//2，删除build.gradle中的android{applicationId}；
//3，去掉Manifest中的launcherActivity；
public class LibProcess implements IProcess {
    boolean needRecoverBuildFile = false;
    boolean needRecoverManifestFile = false;

    private File oriManifestFile;
    private File savedManifestFile;

    private File oriBuildFile;
    private File savedBuildFile;

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

            //将build.gradle复制走，用完了再还回来~
            final String moduleName = project.getName();
            File desParentDir = new File(project.getRootDir() + "/" + Constants.sBuildTempFile+"/" + moduleName + "_build.gradle" );
            if (!desParentDir.exists()) {
                desParentDir.mkdirs();
            }
            final File srcBuildGradle = project.getBuildFile();
            final File desBuildGradle = new File(desParentDir.getAbsolutePath() , Constants.sBuildGradleName);
            oriBuildFile = srcBuildGradle;
            savedBuildFile = desBuildGradle;
            if (desBuildGradle.exists()) {
                desBuildGradle.delete();
            }
            FileUtil.copy(srcBuildGradle, desBuildGradle);

            final String oriText = FileUtil.getText(project.getBuildFile());
            //1，更改application插件->lib插件
            String replaceText = oriText.replaceAll("'com\\.android\\.application'", "'com.android.library'"+Constants.sComments + "：com.android.application -> com.android.library");
            replaceText = replaceText.replaceAll("\"com\\.android\\.application\"", "'com.android.library'"+Constants.sComments + "：com.android.application -> com.android.library");
            //2，移除applicationId
            replaceText = StringUtils.editGradleText(Constants.sDefaultApplicationIdRegexPre,"applicationId",replaceText,null);
            if(!oriText.equals(replaceText)){
                needRecoverBuildFile = true;
                LogMe.D("Lib移除application插件和applicationId后：");
                LogMe.D(replaceText);
                LogMe.D("==========================");
            }
            if(needRecoverBuildFile){
                FileUtil.write(project.getBuildFile(), replaceText);
            }
            //将manifest复制走，用完了再还回来~
            File tempDir = FileUtil.getTempFileDir(project);
            File desManifestParentDir = new File(tempDir,moduleName + "_manifest" );
            if (!desManifestParentDir.exists()) {
                desManifestParentDir.mkdirs();
            }
            final File srcManifest = new File(FileUtil.findManifestPath(project));
            final File desManifest = new File(desManifestParentDir.getAbsolutePath() , Constants.sManifestName);
            oriManifestFile = srcManifest;
            savedManifestFile = desManifest;
            if(!srcManifest.exists()){
                return;
            }
            if (desManifest.exists()) {
                desManifest.delete();
            }
            //3，修改manifest
            try {
                LogMe.D(project.getName() + "：manifestPath==" + oriManifestFile.toPath());
                LogMe.D(project.getName() + "：desManifestPath==" + desManifest.toPath());
                FileUtil.copy(srcManifest, desManifest);
                LogMe.D(project.getName() + "：开始修改xml文件 - ManifestEditor");
                new ManifestEditor(savedManifestFile.getAbsolutePath(),oriManifestFile.getAbsolutePath()).transform(new ManifestEditor.RemoveActivityLauncher());
                needRecoverManifestFile = true;
                LogMe.D("修改后的Manifest：" +  FileUtil.getText(oriManifestFile));
            } catch (Exception e) {
                throw new RuntimeException("LibProcess == 更改 "+project.getName() +" manifest文件：去掉Activity的launcher属性 【失败】："+e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("LibProcess == 更改 "+project.getName() +" build.gradle文件【失败】："+e.getMessage());
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
                if(oriBuildFile.exists()){
                    oriBuildFile.delete();
                }
                FileUtil.copy(savedBuildFile, oriBuildFile);
            }
            if (needRecoverManifestFile) {
                if(oriManifestFile.exists()){
                    oriManifestFile.delete();
                }
                FileUtil.copy(savedManifestFile, oriManifestFile);
                LogMe.D("Manifest 还原完成");
            }
        } catch (IOException e) {
            throw new RuntimeException("LibProcess == 还原 "+project.getName() +" build.gradle/Manifest文件【失败】："+e.getMessage());
        }
    }
}
