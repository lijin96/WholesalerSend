package com.example.wholesalersend.lib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.wholesalersend.R;
import com.example.wholesalersend.utils.SysUserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @ClassName: UpdateManager
 * @Description: 升级APK工具类
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class UpdateManager {

    private Context mContext;

    private Dialog downloadDialog;
    private String savePath = "";
    private String saveFileName = "";

    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private static final int DOWN_ERROR = 3;

    private int progress;
    private Thread downLoadThread;
    public boolean interceptFlag = false;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build();

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    if (mProgress != null) {
                        mProgress.setProgress(progress);
                    }
                    break;
                case DOWN_OVER:
                    if (downloadDialog != null && downloadDialog.isShowing()) {
                        downloadDialog.dismiss();
                    }
                    String sizeTip = msg.obj == null ? "" : msg.obj.toString();
                    if (sizeTip.length() > 0) {
                        Toast.makeText(mContext, sizeTip, Toast.LENGTH_LONG).show();
                    }
                    installAPK();
                    break;
                case DOWN_ERROR:
                    if (downloadDialog != null && downloadDialog.isShowing()) {
                        downloadDialog.dismiss();
                    }
                    String err = msg.obj == null ? "下载失败" : msg.obj.toString();
                    Toast.makeText(mContext, err, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
        File ext = Environment.getExternalStorageDirectory();
        savePath = ext != null ? ext.getAbsolutePath() + File.separator : "/sdcard/";
        saveFileName = savePath + "scs_dev.apk";
    }

    public void checkUpdateInfo(String title) {
        showNoticeDialog(title);
    }

    @SuppressLint("NewApi")
    private void showNoticeDialog(final String title) {
        SysUserInfo sysinfo = new SysUserInfo(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(title);
        builder.setMessage(sysinfo.getUpdateMsg());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog(title);
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @SuppressLint("NewApi")
    public void showDownloadDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(title);

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            File apkFile = new File(saveFileName);
            Response response = null;
            InputStream is = null;
            FileOutputStream fos = null;

            try {
                SysUserInfo sysinfo = new SysUserInfo(mContext);
                String apkUrl = sysinfo.getApkUrl();
                if (apkUrl == null || apkUrl.trim().length() == 0) {
                    notifyError("下载地址为空");
                    return;
                }

                File dir = new File(savePath);
                if (!dir.exists() && !dir.mkdirs()) {
                    notifyError("无法创建保存目录");
                    return;
                }
                if (apkFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    apkFile.delete();
                }

                // identity：禁止 gzip，避免 Content-Length 与实际解压长度不一致导致残包
                Request request = new Request.Builder()
                        .url(apkUrl.trim())
                        .header("Accept-Encoding", "identity")
                        .header("Connection", "close")
                        .get()
                        .build();

                response = httpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    notifyError("下载失败，服务器返回:" + response.code());
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    notifyError("下载失败：响应为空");
                    return;
                }

                long contentLength = body.contentLength();
                is = body.byteStream();
                fos = new FileOutputStream(apkFile);

                long count = 0;
                byte[] buf = new byte[16 * 1024];
                while (!interceptFlag) {
                    int numread = is.read(buf);
                    if (numread == -1) {
                        break;
                    }
                    fos.write(buf, 0, numread);
                    count += numread;
                    if (contentLength > 0) {
                        progress = (int) (count * 100 / contentLength);
                        if (progress > 100) {
                            progress = 100;
                        }
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                    }
                }
                fos.flush();
                try {
                    fos.getFD().sync();
                } catch (IOException ignored) {
                }

                if (interceptFlag) {
                    deleteQuietly(apkFile);
                    notifyError("已取消下载");
                    return;
                }

                long fileLen = apkFile.length();
                if (contentLength > 0 && fileLen != contentLength) {
                    deleteQuietly(apkFile);
                    notifyError("下载不完整(" + fileLen + "/" + contentLength + ")，请重试");
                    return;
                }
                if (count != fileLen) {
                    deleteQuietly(apkFile);
                    notifyError("写入校验失败(" + count + "/" + fileLen + ")");
                    return;
                }
                if (!isCompleteApk(apkFile)) {
                    deleteQuietly(apkFile);
                    notifyError("安装包不完整或已损坏(" + fileLen + "字节)，请重试");
                    return;
                }

                makeApkWorldReadable(apkFile);

                progress = 100;
                mHandler.sendEmptyMessage(DOWN_UPDATE);
                Message ok = mHandler.obtainMessage(DOWN_OVER,
                        "下载完成:" + fileLen + "字节");
                mHandler.sendMessage(ok);
            } catch (Exception e) {
                deleteQuietly(apkFile);
                notifyError("下载失败:" + (e.getMessage() == null ? "网络异常" : e.getMessage()));
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ignored) {
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ignored) {
                }
                // OkHttp 3.2 无 Response.close()，关闭 body 即可释放连接
                if (response != null && response.body() != null) {
                    response.body().close();
                }
            }
        }
    };

    private void notifyError(String msg) {
        mHandler.sendMessage(mHandler.obtainMessage(DOWN_ERROR, msg));
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    /**
     * 校验完整 APK：文件头 PK\003\004，且文件末尾存在 ZIP EOCD(PK\005\006)
     * 残包通常只有文件头，没有完整目录结束标记。
     */
    private boolean isCompleteApk(File file) {
        if (file == null || !file.exists() || file.length() < 22) {
            return false;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            // 文件头
            raf.seek(0);
            int b0 = raf.read();
            int b1 = raf.read();
            int b2 = raf.read();
            int b3 = raf.read();
            if (!(b0 == 'P' && b1 == 'K' && b2 == 3 && b3 == 4)) {
                return false;
            }
            // EOCD 在文件末尾最多 64KB + 22 字节注释范围内
            long fileLen = raf.length();
            int maxComment = 0xFFFF;
            int searchLen = (int) Math.min(fileLen, maxComment + 22);
            byte[] buf = new byte[searchLen];
            raf.seek(fileLen - searchLen);
            raf.readFully(buf);
            for (int i = searchLen - 22; i >= 0; i--) {
                if (buf[i] == 'P' && buf[i + 1] == 'K' && buf[i + 2] == 5 && buf[i + 3] == 6) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void downloadApk() {
        interceptFlag = false;
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 旧系统 PackageInstaller 与当前应用不在同一 UID，
     * 若 APK 仅应用可读，安装器会读失败并提示“解析包时出现问题”。
     */
    private void makeApkWorldReadable(File apkFile) {
        try {
            // readable/writable by others
            apkFile.setReadable(true, false);
            apkFile.setWritable(true, false);
            apkFile.setExecutable(false, false);
        } catch (Exception ignored) {
        }
        try {
            Runtime.getRuntime().exec(new String[]{"chmod", "644", apkFile.getAbsolutePath()}).waitFor();
        } catch (Exception ignored) {
        }
    }

    private void installAPK() {
        File apkFile = new File(saveFileName);
        if (!apkFile.exists()) {
            Toast.makeText(mContext, "安装包文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isCompleteApk(apkFile)) {
            deleteQuietly(apkFile);
            Toast.makeText(mContext, "安装包无效或已损坏，请重新下载", Toast.LENGTH_LONG).show();
            return;
        }

        makeApkWorldReadable(apkFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(mContext,
                    "com.example.wholesalersend.fileprovider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            // Android 7 以下用 file://，必须保证安装器能读到该文件
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mContext,
                    "无法打开安装界面:" + (e.getMessage() == null ? "" : e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }
}
