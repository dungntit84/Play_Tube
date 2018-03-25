package com.hteck.playtube.common;

import com.hteck.playtube.activity.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
    public static String readFileContentInAssets(String filePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(MainActivity
                    .getInstance().getAssets().open(filePath), "UTF-8"));

            StringBuilder buf = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                buf.append(mLine);
            }
            return buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean isFileExist(String folder, String filePath) {
        try {
            File fileHandle = new File(folder, filePath);
            return fileHandle.exists();
        } catch (Throwable e) {

        }
        return false;
    }

    public static String readFileContent(String folder, String filePath) {
        FileInputStream rs = null;

        StringBuilder text = new StringBuilder();

        try {
            File fileHandle = new File(folder, filePath);
            if (fileHandle.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(fileHandle));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        return text.toString();
    }

}
