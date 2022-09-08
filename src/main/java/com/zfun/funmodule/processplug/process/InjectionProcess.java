package com.zfun.funmodule.processplug.process;

import com.zfun.funmodule.Constants;
import com.zfun.funmodule.processplug.IProcess;
import com.zfun.funmodule.processplug.ProjectFileRestoreMgr;
import com.zfun.funmodule.processplug.extension.InjectEx;
import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.LogMe;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.BuildResult;
import org.gradle.api.Project;

import java.io.File;
import java.util.Map;

//负责处理样板代码注入到Gradle中
public class InjectionProcess implements IProcess {
    private String oriBuildFile;
    private boolean injected;

    //group1
    private final Map<String, String> codeFileMap;

    //group2
    private final String onlyCodeFile;
    private final String[] excludeProjectName;


    public InjectionProcess(InjectEx injectEx) {
        codeFileMap = null == injectEx ? null : injectEx.injectCode;
        onlyCodeFile = null == injectEx ? null : injectEx.injectCodePath;
        excludeProjectName = null == injectEx ? null : injectEx.excludeProjectName;
    }

    @Override
    public void beforeEvaluate(Project project) {
        String codeFilePath = null == codeFileMap ? "" : codeFileMap.get(project.getName());
        if (StringUtils.isEmpty(codeFilePath) && !useCodeFileMapParams()) {//如果设置了 InjectEx#injectCode 属性，则忽略 InjectEx#injectCodePath 属性
            codeFilePath = onlyCodeFile;
        }
        if (StringUtils.isEmpty(codeFilePath)) {
            return;
        }

        boolean canContinue = true;
        if (!useCodeFileMapParams()) {
            if (null != excludeProjectName) {
                for (String excludeName : excludeProjectName) {
                    if (StringUtils.isEmpty(excludeName)) continue;
                    if (excludeName.equals(project.getName())) {
                        canContinue = false;
                        break;
                    }
                }
            }
        }
        if (!canContinue) {
            return;
        }

        final File oriFile = project.getBuildFile();
        oriBuildFile = oriFile.getAbsolutePath();
        try {
            final File insertCodeFile = new File(project.getRootDir(),codeFilePath);
            ProjectFileRestoreMgr.saveFile(project, oriBuildFile);
            LogMe.D_Divider(project.getName(),"：开始从声明的文件==" + insertCodeFile.getAbsoluteFile() + "==注入gradle代码");
            final String text = FileUtil.getText(oriFile);
            final StringBuilder stringBuilder = new StringBuilder(text);
            final String injectText = FileUtil.getText(insertCodeFile);
            stringBuilder.append("\n").append(Constants.sCommentsStart).append("\n").append(injectText).append("\n").append(Constants.sCommentsEnd);
            FileUtil.write(oriFile, stringBuilder.toString());
            //
            FileUtil.copyRealUsedFile2Temp(oriFile,project);
        } catch (Exception e) {
            throw new RuntimeException("InjectionProcess == 向 " + project.getName() + " build.gradle文件注入代码失败：" + e.getMessage());
        }
        injected = true;
    }

    @Override
    public void afterEvaluate(Project project) {
        if (!injected) {
            return;
        }
        try {
            ProjectFileRestoreMgr.restoreFile(project, oriBuildFile);
        } catch (Exception e) {
            throw new RuntimeException("InjectionProcess == 还原 " + project.getName() + " build.gradle文件失败：" + e.getMessage());
        }
    }

    @Override
    public void projectsEvaluated(Project project) {

    }

    @Override
    public void buildFinished(Project project, BuildResult buildResult) {

    }

    private boolean useCodeFileMapParams() {
        return null != codeFileMap && codeFileMap.size() > 0;
    }
}
