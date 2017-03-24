package com.wtf.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wtf.BuildConfig;
import com.wtf.R;
import com.wtf.utils.PermissionUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by liyan on 2016/12/12.
 */

public class UpdateDialog {
    private String versionName;
    private String download;
    private Activity context;



    /* 记录进度条数量 */
    private int progress;
    private AlertDialog alertDialog;
    private boolean cancelUpdate = false;
    private String savePath;
    private ProgressBar mProgress;
    private TextView tv_percent;
    private DialogHandler dialogHandler;


    public UpdateDialog(String versionName, String download, Activity context){
        this.versionName=versionName;
        this.download=download;
        this.context=context;
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            PermissionUtil.verifyStoragePermissions(context, 0);
        }
    }
    public void showUpdateDialog() {
        new AlertDialog.Builder(context)
                .setTitle("软件升级")
                .setMessage("检测到最新版本V" + versionName + "，请及时更新")
                //当点确定按钮时从服务器上下载 新的apk 然后安装
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelUpdate = false;
                        showDownloadDialog();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showDownloadDialog() {
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.show();
        dialogHandler=new DialogHandler(this);
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.update_progress);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mProgress = (ProgressBar) window.findViewById(R.id.pb_update);
        tv_percent = (TextView) window.findViewById(R.id.tv_update_percent);
        Button btn_cancel = (Button) window.findViewById(R.id.btn_update_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                cancelUpdate = true;
            }
        });

        new downloadApk().start();
    }

    private class downloadApk extends Thread {
        @Override
        public void run() {
            try {
                URL url = new URL(download);
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    savePath = Environment.getExternalStorageDirectory() + "/download";
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();
                    File file = new File(savePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(savePath, "SmartMattress_" + versionName + ".apk");
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    // 缓存
                    byte[] buffer = new byte[1024];
                    int len;
                    int total = 0;
                    do {
                        len = is.read(buffer);
                        total += len;
                        // 计算进度条位置
                        progress = (int) (((float) total / length) * 100);
                        // 更新进度
                        dialogHandler.sendEmptyMessage(0);
                        if (len <= 0) {
                            // 下载完成
                            dialogHandler.sendEmptyMessage(1);
                            break;
                        }
                        // 写入文件
                        fos.write(buffer, 0, len);
                    } while (!cancelUpdate);// 点击取消就停止下载
                    fos.close();
                    is.close();
                } else {

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class DialogHandler extends Handler {
        WeakReference<UpdateDialog> updateDialog;

        DialogHandler(UpdateDialog dialog) {
            updateDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateDialog theDialog = updateDialog.get();
            switch (msg.what) {
                // 正在下载
                case 0:
                    // 设置进度条位置
                    theDialog.mProgress.setProgress(theDialog.progress);
                    theDialog.tv_percent.setText(theDialog.progress + "%");
                    break;
                case 1:
                    // 取消下载对话框显示
                    theDialog.alertDialog.dismiss();
                    // 安装文件
                    theDialog.installApk();
                    break;
            }
        }
    }

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkFile = new File(savePath, "SmartMattress_" + versionName + ".apk");
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
