package com.example.wholesalersend.activity.sendgoods_fd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ReplaceLabCode
 * @Description: 定制镜架出货
 * @Author: lijin
 * @Date: 2026年4月2日17:37:49
 */
public class SunLensReplaceLabCode extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;

    private TextView tv_title,tv_old_mess,tv_old_code,tv_new_code,tv_result,tv_Scannum;
    private EditText et_outstock_code, et_outstock_order;

    private String OldCode = "", OrderNumber = "";//物流码、出库单号

    private String modelm="",colors="";//型号色号

    private final int HandRequestFocus = 3;

    private int nSize = 0;//次数

    private String SalseProductName="";
    private TextView tv_goodname;

    private String scanBillno="";//扫描单号

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunlensreplacelabcode);
        mContext=this;
        accWeb=new AccessWeb(mContext);
        sysUserInfo=new SysUserInfo(mContext);

        scanBillno = sysUserInfo.getUserid() + "S1FS" + SomeUtils.RandomScanOrder();// 系统

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【定制镜架出货】 "+sysUserInfo.getAccountSetName());

        tv_result=findViewById(R.id.tv_result);
        tv_Scannum=findViewById(R.id.tv_Scannum);

        ((TextView) findViewById(R.id.btn_list)).setOnClickListener(new BtnListClick());

        tv_old_mess = (TextView) findViewById(R.id.tv_old_mess);
        tv_old_code = (TextView) findViewById(R.id.tv_old_code);
        tv_new_code = (TextView) findViewById(R.id.tv_new_code);

        et_outstock_code = (EditText) findViewById(R.id.et_outstock_code);
        et_outstock_order = (EditText) findViewById(R.id.et_outstock_order);

        et_outstock_code.setOnKeyListener(new EtBarodeOnkeyListener());//物流码
        et_outstock_order.setOnKeyListener(new EtBarodeOnkeyListener());//出库单号

//        ((Button) findViewById(R.id.btn_exit)).setOnClickListener(new BtnExitClick());

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

        tv_goodname=findViewById(R.id.tv_goodname);

    }

    /**
     * 明细按钮监听类
     */
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mContext, QueryScanDetail.class);
            intent.putExtra("mBillNo", scanBillno);
            startActivity(intent);
        }
    }



    //输入框监听
    class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (v == et_outstock_order) {

                        OrderNumber =et_outstock_order.getText().toString().trim();
//                        et_outstock_order.setText("");
                        tv_new_code.setText("出库单号：" + OrderNumber);
                        tv_old_code.setText(tv_old_mess.getText().toString());
                        return false;//不截止监听事件，跳到下一个
                    } else if (v == et_outstock_code) {
                        if (et_outstock_code.getText().toString().trim().indexOf("=") != -1||et_outstock_code.getText().toString().trim().indexOf("http") != -1) {
                            //包含
                            OldCode = SomeUtils.InterceptCode(mContext, et_outstock_code.getText().toString().trim());
                        } else {
                            //不包含
                            OldCode = SomeUtils.UpdatefirstString(mContext,et_outstock_code.getText().toString().trim());
                        }
                        et_outstock_code.setText("");
                        tv_old_code.setText(tv_old_mess.getText().toString() + OldCode);
                        access_send(OrderNumber, OldCode);
                        return true;//截止监听事件，不再后续
                    }
                }
            }

            return false;
        }
    }



    public void access_send(final String tOrder, final String tBarcode) {
        if (tBarcode == null || tBarcode.isEmpty()) {
            MySound.errorSound();
            ShowMessage.ShowMsg(handler, HandRequestFocus, "请扫描物流码,谢谢！");
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Map<String, Object> requestParams=new HashMap<>();
                    requestParams.put("ObGoodsNo", tOrder);//出库单号
                    requestParams.put("Barcode", tBarcode);//物流码
                    requestParams.put("ScanBillNo", scanBillno);//扫描单号
                    requestParams.put("ScanSn", nSize+"");//扫描序号
                    requestParams.put("BrandCode", "");//品牌代号
                    Gson gson=new Gson();
//                    Log.d("main", requestParams.toString());
                    String  result =accWeb.PostAPIStringInterface("AndroidDv/OutStockExistingBillScan", gson.toJson(requestParams));
                    JSONObject jsonObject = new JSONObject(result);

                    modelm=jsonObject.optString("modelm");
                    colors=jsonObject.optString("color");

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess,"ok");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
                }
            }
        }).start();
    }


    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ShowMessage.HandScanSuccess://成功
                    MySound.scanSound();
//                    Log.d("main", "handleMessage: 成功");
//                    tv_result.setText("成功");
//                    tv_result.setTextColor(Color.GREEN);
//                    tv_error.setText("");
//                    tv_information.setText("型号色号:"+modelm+colors);
                    nSize++;
                    tv_goodname.setText(modelm+colors);
                    tv_Scannum.setText(String.valueOf(nSize));
                    et_outstock_code.requestFocus();
                    break;
                case ShowMessage.HandScanError://报错
                    MySound.errorSound();
//                    Log.d("main", "handleMessage: 失败");
//                    tv_result.setText("失败");
//                    tv_result.setTextColor(Color.RED);
                    tv_goodname.setText("");
                    ShowMessage.Show(mContext, msg.obj.toString());
                    if (msg.obj.toString().indexOf("出库单不存在")>-1){
                        et_outstock_order.setText("");
                        et_outstock_code.setText("");
                        et_outstock_order.requestFocus();
                    }else{
                        et_outstock_code.setText("");
                        et_outstock_code.requestFocus();
                    }
                    break;

                case HandRequestFocus:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    //这里开启一个延迟线程把光标移到上面，不然可能不起作用。
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_outstock_code.requestFocus();
                        }
                    }, 200);
                    break;
                default:
                    break;
            }

        }

        ;
    };


    /**
     * 退出按钮监听类
     */
    private class BtnExitClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(mContext, true)) {
                finish();
            }
        }
    }


    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            if (configuration != null && configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f;//这里只设置字体，故不使用下面注释的方法
//	                configuration.setToDefaults();
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            }
        }
        return resources;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}


