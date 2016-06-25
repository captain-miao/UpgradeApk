package com.github.captain_miao.library.upgrade;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

public class UpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = "UpgradeReceiver";


    public UpgradeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String action = intent.getAction();
        long localDownloadId = sharedPreferences.getLong(UpgradeApk.LOCAL_DOWNLOAD_ID, -1);
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

        if(localDownloadId == downloadId && DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

            DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor cursor = downloadManager.query(query);
            if(cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reason = cursor.getInt(columnReason);

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {

                        String downloadFileUrl = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        startInstall(context, Uri.parse(downloadFileUrl));

                    } else if (status == DownloadManager.STATUS_FAILED) {
                        Log.v(TAG, "FAILED!:" + "reason of " + reason);
                    } else if (status == DownloadManager.STATUS_PAUSED) {
                        Log.v(TAG, "PAUSED!:" + "reason of " + reason);
                    } else if (status == DownloadManager.STATUS_PENDING) {
                        Log.v(TAG, "PENDING!");
                    } else if (status == DownloadManager.STATUS_RUNNING) {
                        Log.v(TAG, "RUNNING!");
                    }
                }
                cursor.close();
            }
        } else if(localDownloadId == downloadId && DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)){
            // TODO: 16/6/25
        }
    }


    private boolean startInstall(Context context, Uri uri) {
        if(!new File( uri.getPath()).exists()) {
            Log.v(TAG, " local file has been deleted! ");
            return false;
        }
        Intent intent = new Intent();
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction( Intent.ACTION_VIEW);
        intent.setDataAndType( uri, "application/vnd.android.package-archive");
        context.startActivity( intent);
        return true;
    }
}
