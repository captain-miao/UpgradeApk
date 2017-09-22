package com.github.captain_miao.library.upgrade;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * @author YanLu
 * @since 16/6/25
 */
public class UpgradeApk {
    public static final String LOCAL_DOWNLOAD_ID = "hj_local_download_id";
    private static final String DEFAULT_NAME = "apk";

    public static boolean upgrade(Context context, String url, String fileName){
        if(!TextUtils.isEmpty(url)){
            return upgrade(context, Uri.parse(url), fileName);
        } else {
            return false; // url is empty
        }
    }
    public static boolean upgrade(Context context, String url){
        if(!TextUtils.isEmpty(url)){
            String fileName = getApplicationName(context);
            Uri uri = Uri.parse(url);
            fileName = TextUtils.isEmpty(uri.getLastPathSegment()) ? fileName : uri.getLastPathSegment();
            return upgrade(context, uri, fileName);
        } else {
            return false; // url is empty
        }
    }

    public static boolean upgrade(Context context, Uri uri, String fileName) {
        if (uri != null) {
            fileName = TextUtils.isEmpty(fileName) ? DEFAULT_NAME : fileName;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request =
                    new DownloadManager.Request(uri)
                            .setMimeType("application/vnd.android.package-archive")
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setTitle(uri.getLastPathSegment());
            request.allowScanningByMediaScanner();
            long localDownloadId = downloadManager.enqueue(request);
            return sharedPreferences.edit().putLong(LOCAL_DOWNLOAD_ID, localDownloadId).commit();
        } else {
            return false; // uri is null
        }
    }

    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }
}
