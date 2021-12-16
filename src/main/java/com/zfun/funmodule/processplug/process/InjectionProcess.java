package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

import java.io.File;
import java.util.Map;

//负责处理样板代码注入到Gradle中
public class InjectionProcess implements IProcess {
    private File oriFile;
    private File desFile;
    private boolean injected;

    private final Map<String,String> codeFile;

    public InjectionProcess(InjectEx injectEx){
        codeFile = null == injectEx?null:injectEx.injectCode;
    }

    @Override
    public void beforeEvaluate(Project project) {
        String codeFilePath = null == codeFile?"":codeFile.get(project.getName());
        if(null == codeFilePath || codeFilePath.trim().length() == 0){
            return;
        }

        File tempDir = FileUtil.getTempFileDir(project);
        File copyDesFile = new File(tempDir,project.getName()+"_inject_gradle");
        copyDesFile.mkdirs();
        desFile = new File(copyDesFile, Constants.sBuildGradleName);
        oriFile = project.getBuildFile();
        if(desFile.exists()){
            desFile.delete();
        }
        try {
            FileUtil.copy(oriFile,desFile);
            LogMe.D(project.getName() +"：开始从声明的gradle_code文件=="+codeFilePath+ "==注入gradle代码");
            final String text = FileUtil.getText(oriFile);
            final StringBuilder stringBuilder = new StringBuilder(text);
            final String injectText = FileUtil.getText(new File(codeFilePath));
            stringBuilder.append("\n").append(injectText);
            FileUtil.write(oriFile,stringBuilder.toString());
            LogMe.D("注入后的gradle代码：\n"+FileUtil.getText(oriFile));
        } catch (Exception e) {
            //
        }
        injected = true;
    }

    @Override
    public void afterEvaluate(Project project) {

    }

    @Override
    public void buildStarted(Project project, Gradle gradle) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {
        if(!injected){
            return;
        }
        try {
            if(oriFile.exists()){
                oriFile.delete();
            }
            FileUtil.copy(desFile, oriFile);
            LogMe.D("还原后的gradle代码："+FileUtil.getText(oriFile));
        }catch (Exception e){
            //
        }
    }
}
