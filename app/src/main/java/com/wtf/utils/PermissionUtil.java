package com.wtf.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by liyan on 2016/12/29.
 */

public class PermissionUtil {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int SD_PERMISSION = 0;
    private static final int WIFI_PERMISSION = 1;
    private static final int CAMERA_PERMISSION = 2;
    private static String[] SD_PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static String[] WIFI_PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static String[] CAMERA_PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
    };


    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity, int which) {
        // Check if we have write permission
     /*   int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission_group.LOCATION);
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission_group.LOCATION);
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission_group.LOCATION);*/

        switch (which) {
            case SD_PERMISSION:
                ActivityCompat.requestPermissions(
                        activity,
                        SD_PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
                break;
            case WIFI_PERMISSION:
                ActivityCompat.requestPermissions(
                        activity,
                        WIFI_PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
                break;

            case CAMERA_PERMISSION:
                ActivityCompat.requestPermissions(
                        activity,
                        CAMERA_PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
                break;
        }
        /*if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }*/
    }

}
