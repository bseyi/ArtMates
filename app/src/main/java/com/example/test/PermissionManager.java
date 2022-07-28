package com.example.test;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.test.fragment.ComposeFragment;

public class PermissionManager {
    public static final int REQUEST_WRITE_STORAGE = 112;

    public static final int REQUEST_FINE_LOCATION = 100;

    public static boolean checkWriteStoragePermissions(Activity activity)
    {
        boolean hasPermission = (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
        return hasPermission;
    }

}
