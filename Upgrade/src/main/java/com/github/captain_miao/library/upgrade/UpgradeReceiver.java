package com.github.captain_miao.library.upgrade;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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

                        startInstall(context, getRealFilePath(context, Uri.parse(downloadFileUrl)));

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


    private boolean startInstall(Context context, String uri) {
        if(!new File(uri).exists()) {
            Log.v(TAG, " local file has been deleted! ");
            return false;
        }
//        Intent intent = new Intent();
//        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction( Intent.ACTION_VIEW);
//        intent.setDataAndType( uri, "application/vnd.android.package-archive");
//        context.startActivity( intent);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //版本在7.0以上是不能直接通过uri访问的
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            File file = (new File(uri));
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(context, "com.zjhjin.funds.fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
            intent.setDataAndType(Uri.fromFile(new File(uri)),
                    "application/vnd.android.package-archive");
        }

//
//        File file = new File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                , uri.getLastPathSegment());
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        // 由于没有在Activity环境下启动Activity,设置下面的标签
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
//            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
//            Uri apkUri =
//                    FileProvider.getUriForFile(context, "com.zjhjin.funds.fileprovider", file);
//            //添加这一句表示对目标应用临时授权该Uri所代表的文件
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//        } else {
//            intent.setDataAndType(Uri.fromFile(file),
//                    "application/vnd.android.package-archive");
//        }
        context.startActivity(intent);


        return true;
    }


    public String getRealFilePath(Context context, Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
