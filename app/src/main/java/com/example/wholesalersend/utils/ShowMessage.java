package com.example.wholesalersend.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wholesalersend.R;


/**
 * @ClassName: ShowMessage
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:28
 */
public class ShowMessage {
    public static final int HandShowMessage=0;
    public static final int HandScanSuccess=1;
    public static final int HandScanError=2;
    public static final int HandInitProgress=3;
    public static final int HandCloseLoading=4;
    public static final int HandSuccess=20;
    public static final int HandFailed=21;
    public static final int HandMakeDressBox=22;
    public static final int HandUploadDetail=23;


    /**toast 显示方法
     * @param context 上下文菜单
     * @param msg 消息
     * */
    public static void Show(Context context,String msg)
    {
        Toast toast =  Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP , 0, 300);
        toast.show();
    }


    /**toast 显示方法
     * @param hand
     * @param tMsg 显示的消息
     * */
    public static void ShowMsg(Handler hand, String tMsg)
    {
        Message msg = new Message();
        msg.what = HandShowMessage;
        msg.obj = tMsg;
        hand.sendMessage(msg);
    }

    /**用handler发送消息
     * @param hand Handler
     * @param Model 发送消息类型
     * @param tMsg 消息
     * */
    public static void ShowMsg(Handler hand,int Model,String tMsg)
    {
        Message msg = new Message();
        msg.what = Model;
        msg.obj = tMsg;
        hand.sendMessage(msg);
    }

    public static void ShowMsg(Handler hand,int Model,Object tMsg)
    {
        Message msg = new Message();
        msg.what = Model;
        msg.obj = tMsg;
        hand.sendMessage(msg);
    }




    /**自定义信息显示框
     * @param context 上下文菜单
     * @param title 发送消息标题
     * @param msg 消息  */
    public static void MessageBox(Context context, String title, String msg)
    {
        TextView tvMessage = new TextView(context);
        tvMessage.setGravity(Gravity.CENTER);
        tvMessage.setTextColor(Color.WHITE);
        tvMessage.setTextSize(20);
        tvMessage.setPadding(10, 0, 10, 10);
        tvMessage.setText(msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setIcon(R.drawable.scs);
        builder.setTitle(title);
//		builder.setMessage(msg);
        builder.setView(tvMessage);
        builder.setPositiveButton("确定",null);
        builder.create().show();
    }

    /**自定义信息显示框
     * @param context 上下文菜单
     * @param 温馨提示
     * @param msg 消息
     * @param onCancelListener */
    public static void MessageBox(Context context, String 温馨提示, String msg,
                                  DialogInterface.OnCancelListener onCancelListener)
    {

        TextView tvMessage = new TextView(context);
        tvMessage.setGravity(Gravity.CENTER);
        tvMessage.setTextColor(Color.WHITE);
        tvMessage.setTextSize(20);
        tvMessage.setPadding(10, 0, 10, 10);
        tvMessage.setText(msg);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.scs);
        builder.setTitle("温馨提示");
//		builder.setMessage(msg);
        builder.setView(tvMessage);
        builder.setPositiveButton("确定",null);

        builder.create().show();
    }

    /**自定义信息显示框
     * @param context 上下文菜单
     * @param title 发送消息标题
     * @param msg 消息
     * @param OkClick  确定按钮事件
     * */
    public static void MessageBox(Context context,String title,String msg,DialogInterface.OnClickListener OkClick)
    {
        Dialog builder = new AlertDialog.Builder(context,R.style.Base_Theme_AppCompat_Light_Dialog)
        .setIcon(R.drawable.scs)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton("确定",OkClick).setCancelable(false)
        .create();
        builder.show();
        if (builder.getWindow() != null) {
            WindowManager.LayoutParams lp = builder.getWindow().getAttributes();
            lp.width = 600; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            builder.getWindow().setAttributes(lp);
        }
    }


    /**自定义布局提示框
     *  @param context 上下文菜单
     * @param title 发送消息标题
     * @param msg 消息

     * */
//    public static void MyDiloag(Context context,String title,String msg)
//    {
//        final Dialog dialog = new Dialog(context);
////    	 dialog.setTitle("我是标题");
//        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.showmessage_mydiloag);
//        ImageButton btn1 =  (ImageButton) dialog.findViewById(R.id.imageButton1);
//        btn1.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                dialog.dismiss();
//            }
//        });;
//
//        TextView txtmsg =(TextView) dialog.findViewById(R.id.lblTip);
//
//
//        txtmsg.setText(msg);
//        dialog.show();
//
//
//
//    }


    /**自定义信息显示框
     * @param context 上下文菜单
     * @param title 发送消息标题
     * @param msg 消息
     * @param OkClick  确定按钮事件
     * */
    public static void MessageBox(Context context,String title,String msg,DialogInterface.OnClickListener OkClick,int MessageWidth)
    {
        Dialog builder = new AlertDialog.Builder(context,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.mipmap.scs)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定",OkClick).setCancelable(false)
                .create();
        builder.show();
        if (builder.getWindow() != null) {

            WindowManager.LayoutParams lp = builder.getWindow().getAttributes();
            lp.width = MessageWidth; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            builder.getWindow().setAttributes(lp);
        }
//        if (builder.getWindow() != null) {
//            WindowManager.LayoutParams lp = builder.getWindow().getAttributes();
//            lp.width = 600; // 宽度，可根据屏幕宽度进行计算
//            lp.gravity = Gravity.CENTER;
//            builder.getWindow().setAttributes(lp);
//        }
    }


    /**自定义信息显示框
     * @param context 上下文菜单
     * @param title 发送消息标题
     * @param OkClick  确定按钮事件
     * @param EscClick 取消按钮事件   */
    public static void MessageBox(Context context, String title,String msg,String suretitle,String canceltitle, DialogInterface.OnClickListener OkClick, DialogInterface.OnClickListener EscClick)
    {
        Dialog  builder = new AlertDialog.Builder(context,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.scs)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(suretitle,OkClick)
                .setNegativeButton(canceltitle,EscClick)
                .create();
        builder.show();
        if (builder.getWindow() != null) {
            WindowManager.LayoutParams lp = builder.getWindow().getAttributes();
            lp.width = 600; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            builder.getWindow().setAttributes(lp);
        }

    }

}

