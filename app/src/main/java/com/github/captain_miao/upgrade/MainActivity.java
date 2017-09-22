package com.github.captain_miao.upgrade;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.example.captain_miao.grantap.CheckAnnotatePermission;
import com.example.captain_miao.grantap.annotation.PermissionCheck;
import com.example.captain_miao.grantap.annotation.PermissionDenied;
import com.example.captain_miao.grantap.annotation.PermissionGranted;
import com.github.captain_miao.library.upgrade.UpgradeApk;

public class MainActivity extends Activity {
    private static final String UPGRADE_URL = "http://pkg3.fir.im/a80ee078a1127262ac953dc59d5fb14648626d14.apk?attname=upgrade_apk.apk_1.0.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    AlertDialog mDialog;
    public void onUpgrade(View view) {
        requestPermission();
    }

    // 版本升级
    private void showUpgradeDialog() {
        requestPermission();
    }

    @PermissionCheck
    String[] WRITE_EXTERNAL_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void requestPermission() {
        CheckAnnotatePermission
                .from(this, this)
                .check();
    }

    @PermissionGranted()
    public void permissionGranted() {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("Upgrade apk")
                    .setMessage(
                            Html.fromHtml("<font color=#ff0000>"
                                    + "1. ba la ba la<br />"
                                    + "2. ba la ba la<br />"
                                    + "3. ba la ba la<br />"
                                    + "</font>"))
                    .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UpgradeApk.upgrade(MainActivity.this, UPGRADE_URL);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
        mDialog.show();
    }

    @PermissionDenied()
    public void permissionDenied() {
        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog != null){
            mDialog.dismiss();
        }
    }
}
