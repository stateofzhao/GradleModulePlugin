package com.zfun.funmodule.processplug;

import com.zfun.funmodule.util.FileUtil;
import com.zfun.funmodule.util.StringUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 对原文件进行备份与还原，每个Project的文件都只会备份一次（{@link #saveFile(Project, String)}方法多次调用只有首次调用起作用），
 * 还原后方可再次备份（{@link #restoreFile(Project, String)}调用会，再次调用 {@link #saveFile(Project, String)}可起作用）。
 * <p>
 * 备份文件存储于 项目根目录的 【.idea/projectName/fileName】所以，目前针对同名文件（即使路径不同）只能备份一次，先这样，后续再优化！！
 */
public class ProjectFileRestoreMgr {
    private static final Map<Project, FileOpt> projectFileOptMap = new Hashtable<>();

    /**
     * @return 返回备份文件的全名
     * */
    public static String saveFile(Project project, String filePath) throws IOException {
        final FileOpt fileOpt = getProjectFileOpt(project);
        return fileOpt.saveFile(filePath);
    }

    public static void restoreFile(Project project, String filePath) throws IOException {
        final FileOpt fileOpt = getProjectFileOptCanNull(project);
        if(null == fileOpt){
            return;
        }
        fileOpt.restoreFile(filePath);
    }

    //不为空
    private static synchronized FileOpt getProjectFileOpt(Project project) {
        FileOpt fileOpt = projectFileOptMap.get(project);
        if (null == fileOpt) {
            fileOpt = new FileOpt(project, null);
            projectFileOptMap.put(project, fileOpt);
        }
        return fileOpt;
    }

    //可以为null
    private static synchronized FileOpt getProjectFileOptCanNull(Project project) {
        return projectFileOptMap.get(project);
    }

    private static class FileOpt {
        private final Project project;
        private final String copyDirPath;
        private final Set<SavedFileData> savedFiles = Collections.synchronizedSet(new HashSet<>());

        /**
         * @param copyDirPath 可以为空
         */
        public FileOpt(Project project, String copyDirPath) {
            this.project = project;
            if (StringUtils.isEmpty(copyDirPath)) {
                copyDirPath = FileUtil.getTempFileDir(project).getAbsolutePath();
            }
            this.copyDirPath = copyDirPath;
        }

        /**
         * @return 返回备份文件的全名
         * */
        //fixme 备份文件存储于 项目根目录的 【.idea/projectName/fileName】所以，目前针对同名文件（即使路径不同）只能备份一次，先这样，后续再优化！！
        public synchronized String saveFile(String filePath) throws IOException {
            final SavedFileData containsSaveFileDate = getContainsSavedFileData(filePath);
            if (containsSaveFileDate != null) {//已经保存过了不再保存
                return containsSaveFileDate.desFilePath;
            }
            final File oriFile = new File(filePath);
            final String fileName = oriFile.getName();
            final File desParentDir = new File(copyDirPath, project.getName());
            if (!desParentDir.exists()) {
                desParentDir.mkdirs();
            }
            final File desFile = new File(desParentDir.getAbsolutePath(), fileName);
            if (desFile.exists()) {
                desFile.delete();
            }
            FileUtil.copy(oriFile, desFile);
            //
            final SavedFileData savedFileData = new SavedFileData(filePath, desFile.getAbsolutePath());
            savedFiles.add(savedFileData);
            return savedFileData.desFilePath;
        }

        public synchronized void restoreFile(String filePath) throws IOException {
            SavedFileData savedFileData = null;
            for (SavedFileData aItem : savedFiles) {
                if (aItem.isOriPath(filePath)) {
                    savedFileData = aItem;
                    break;
                }
            }
            if (null == savedFileData) {
                return;
            }
            final File oriFile = new File(savedFileData.oriFilePath);
            if (oriFile.exists()) {
                oriFile.delete();
            }
            FileUtil.copy(new File(savedFileData.desFilePath), oriFile);
            savedFiles.remove(savedFileData);
        }

        private synchronized boolean isContains(String oriFilePath) {
            final SavedFileData savedFileData = getContainsSavedFileData(oriFilePath);
            return null != savedFileData;
        }

        private synchronized SavedFileData getContainsSavedFileData(String oriFilePath) {
            for (SavedFileData savedFileData : savedFiles) {
                if (savedFileData.isOriPath(oriFilePath)) {
                    return savedFileData;
                }
            }
            return null;
        }

        private static class SavedFileData {
            public final String oriFilePath;
            public final String desFilePath;

            private SavedFileData(String oriFilePath, String desFilePath) {
                this.oriFilePath = oriFilePath;
                this.desFilePath = desFilePath;
            }

            private boolean isOriPath(String oriFilePath) {
                if (StringUtils.isEmpty(this.oriFilePath)) {
                    return false;
                }
                return this.oriFilePath.equals(oriFilePath);
            }

            /*@Override
            public boolean equals(Object obj) {
               if(obj instanceof SavedFileData){
                   final SavedFileData savedFileData = (SavedFileData) obj;
                   return StringUtils.isEquals(oriFilePath,savedFileData.oriFilePath) && StringUtils.isEquals(desFilePath,savedFileData.desFilePath);
               }
               return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(oriFilePath,desFilePath);
            }*/
        }//

    }//

}
