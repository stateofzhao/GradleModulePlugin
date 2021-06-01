package com.zfun.funmodule.util;

import com.zfun.funmodule.Constants;
import groovy.util.CharsetToolkit;
import org.gradle.api.Project;

import java.io.*;
import java.nio.file.Files;

public class FileUtil {

    public static String getText(File file) throws IOException {
        CharsetToolkit toolkit = new CharsetToolkit(file);
        return getText(toolkit.getReader());
    }


    public static void write(File file, String text) throws IOException {
        FileWriter writer = null;

        try {
            writer = new FileWriter(file);
            writer.write(text);
            writer.flush();
            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWithWarning(writer);
        }

    }


    public static String getText(BufferedReader reader) throws IOException {
        StringBuilder answer = new StringBuilder();
        char[] charBuffer = new char[8192];

        try {
            int nbCharRead;
            while((nbCharRead = reader.read(charBuffer)) != -1) {
                answer.append(charBuffer, 0, nbCharRead);
            }

            Reader temp = reader;
            reader = null;
            temp.close();
        } finally {
            closeWithWarning(reader);
        }

        return answer.toString();
    }

    public static boolean replaceFileText(File file, String regex, String replacement) throws IOException {
        final String text = FileUtil.getText(file);
        String replaceText = text.replaceAll(regex, replacement);
        boolean changed = !replaceText.equals(text);
        if(changed){
            FileUtil.write(file, replaceText);
        }
        return changed;
    }

    public static void closeWithWarning(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException var2) {
                LogMe.D("Caught exception during close(): " + var2);
            }
        }
    }

    public static String findManifestPath(Project project){
        return project.getProjectDir().getPath()+ Constants.sManifestPath;
    }

    public static void copy(File srcFile, File destFile) throws IOException{
        Files.copy(srcFile.toPath(),destFile.toPath());
    }

    public static void removeManifestLauncherActivity(File manifestFile) throws Exception{
        FileUtil.replaceFileText(manifestFile, "<category android:name=\"android.intent.category.LAUNCHER\" />", "");
    }
}
