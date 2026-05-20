package com.example.wholesalersend.utils;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * @ClassName: ListViewDialog
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2025/1/2 11:53
 */
public class ListViewDialog extends Dialog {
    private final Context mContext;

    public ListViewDialog(@NonNull Context context) {
        super(context);
        mContext = context;

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
        setHeight();
    }

    private void setHeight() {
        Window window = getWindow();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (window.getDecorView().getHeight() >= (int) (displayMetrics.heightPixels * 0.6)) {
            attributes.height = (int) (displayMetrics.heightPixels * 0.6);
        }
        window.setAttributes(attributes);
    }
}