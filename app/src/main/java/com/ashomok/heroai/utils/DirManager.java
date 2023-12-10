package com.ashomok.heroai.utils;

import android.os.Environment;

import com.ashomok.heroai.R;

import java.io.File;

/**
 * Created by Devlomi on 12/08/2017.
 */

//this class will manage all create file name
//and it contains all folder paths for all types (image,video etc..)
public enum DirManager {
    ;

    private static final String APP_FOLDER_NAME = MyApp.context().getString(R.string.app_folder_name);


    //Main App Folder: /sdcard/FireApp/
    public static String mainAppFolder() {
        File file;
        if (BuildVerUtil.isApi29OrAbove()) {
            file = new File(MyApp.context().getExternalFilesDir(null) + "/" + DirManager.APP_FOLDER_NAME + "/");
        } else {
            file = new File(Environment.getExternalStorageDirectory() + "/" + DirManager.APP_FOLDER_NAME + "/");
        }
        //if the directory is not exists create it
        if (!file.exists())
            file.mkdir();


        return file.getAbsolutePath();
    }


    public static File getDatabasesFolder() {
        //if the status was downloaded from other users we will save it in the hidden folder ".Statuses"
        //otherwise we will save as a normal received image or video
        File file = new File(DirManager.mainAppFolder() + "/" + "Databases");

        if (!file.exists())
            file.mkdirs();

        return file;


    }
}
