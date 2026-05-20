package com.example.wholesalersend.activity.sendgoods_fd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.entity.ReplaceLabCodePara;
import com.example.wholesalersend.entity.SalesReplaceLabCodePara;
import com.example.wholesalersend.entity.ScsWebApiInfo;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

/**
 * @ClassName: ReplaceLabCode
 * @Description: 镜片出库绑码
 * @Author: lijin
 * @Date: 2024/12/18 15:18
 */
public class SalesReplaceLabCode extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;
    private Intent gIntent;

    private TextView tv_customr_name,tv_billno,tv_salesdate,tv_salesnum;
    private TextView tv_title,tv_old_mess,tv_old_code,tv_new_code,tv_result,tv_Scannum;
    private EditText et_old_code, et_new_code;

    private String OldCode = "", NewBarcode = "";

    private final int HandRequestFocus = 3;

    private int nSize = 0;//次数

    private String saleOrderNo="",saleOrderDate="",customerName="",custSysCode="",saleOrderNum="";

    private String SalseProductName="";
    private TextView tv_goodname;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesreplacelabcode);
        mContext=this;
        accWeb=new AccessWeb(mContext);
        sysUserInfo=new SysUserInfo(mContext);
        gIntent = getIntent();


        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【镜片出库绑码（按销售单）】 "+sysUserInfo.getAccountSetName());

        tv_result=findViewById(R.id.tv_result);
        tv_Scannum=findViewById(R.id.tv_Scannum);

        saleOrderNo = gIntent.getStringExtra("saleOrderNo");
        saleOrderDate = gIntent.getStringExtra("saleOrderDate");
        customerName = gIntent.getStringExtra("customerName");
        custSysCode = gIntent.getStringExtra("custSysCode");
        saleOrderNum = gIntent.getStringExtra("saleOrderNum");

        tv_customr_name= (TextView) findViewById(R.id.tv_customr_name);
        tv_billno= (TextView) findViewById(R.id.tv_billno);
        tv_salesdate= (TextView) findViewById(R.id.tv_salesdate);
        tv_salesnum= (TextView) findViewById(R.id.tv_salesnum);

        tv_goodname=findViewById(R.id.tv_goodname);

        tv_customr_name.setText(customerName);
        tv_billno.setText(saleOrderNo);
        tv_salesdate.setText(saleOrderDate);
        tv_salesnum.setText(saleOrderNum);


        tv_old_mess = (TextView) findViewById(R.id.tv_old_mess);
        tv_old_code = (TextView) findViewById(R.id.tv_old_code);
        tv_new_code = (TextView) findViewById(R.id.tv_new_code);

        et_old_code = (EditText) findViewById(R.id.et_old_code);
        et_new_code = (EditText) findViewById(R.id.et_new_code);

        et_old_code.setOnKeyListener(new EtBarodeOnkeyListener());
        et_new_code.setOnKeyListener(new EtBarodeOnkeyListener());

        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

    }


    //输入框监听
    class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (v == et_new_code) {
                        if (et_new_code.getText().toString().trim().indexOf("=") != -1||et_new_code.getText().toString().trim().indexOf("http") != -1) {
                            //包含
                            NewBarcode = SomeUtils.InterceptCode(mContext, et_new_code.getText().toString().trim());
                        } else {
                            //不包含
                            NewBarcode =et_new_code.getText().toString().trim();
//                                    SomeUtils.UpdatefirstString(mContext,);
                        }


//                        if (!SomeUtils.isAllNumber(mContext, NewBarcode)) {
//                            MySound.errorSound();
//                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + NewBarcode + "】");
//                            et_new_code.setText("");
//                            return true;
//                        }
                        et_new_code.setText("");
                        tv_new_code.setText("打印码二维码：" + NewBarcode);
                        tv_old_code.setText(tv_old_mess.getText().toString());
                        return true;//截止监听事件，不再后续
                    } else if (v == et_old_code) {

                        if (et_old_code.getText().toString().trim().indexOf("=") != -1||et_old_code.getText().toString().trim().indexOf("http") != -1) {

                            //包含
                            OldCode = SomeUtils.InterceptCode(mContext, et_old_code.getText().toString().trim());
                        } else {
                            //不包含
                            OldCode = SomeUtils.UpdatefirstString(mContext,et_old_code.getText().toString().trim());
                        }

//                        if (!SomeUtils.isAllNumber(mContext, OldCode)) {
//                            MySound.errorSound();
//                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + OldCode + "】");
//                            et_old_code.setText("");
//                            return true;
//                        }

                        et_old_code.setText("");
                        tv_old_code.setText(tv_old_mess.getText().toString() + OldCode);

                        access_send(NewBarcode, OldCode);
                        return false;//不截止监听事件，跳到下一个
                    }


                }
            }

            return false;
        }
    }

    public void access_send(final String newBarcode, final String oldBarcode) {
        if (oldBarcode == null || oldBarcode.isEmpty()) {
            ShowMessage.ShowMsg(handler, HandRequestFocus, "请扫描印刷码二维码,谢谢！");
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();

                    SalesReplaceLabCodePara replaceLabCodePara=new SalesReplaceLabCodePara();
                    replaceLabCodePara.setOriginallyLabCode(newBarcode);
                    replaceLabCodePara.setNewLabCode(oldBarcode);
                    replaceLabCodePara.setMODE("USER");
                    replaceLabCodePara.setBillNo(saleOrderNo);
                    replaceLabCodePara.setBillType("OB");
                    Gson gson=new Gson();

//                    String data = "OriginallyLabCode="+newBarcode+"&NewLabCode="+oldBarcode+"&MODE=USER";//POST请求的参数
                    String trequesturl="ScsWebApi";//默认是正式地址
                    if (sysUserInfo.getAPIEndpoint().equals("SCS2.0Test")){
                        trequesturl="ScsWebApiTest";
                    }
//                    Log.d("main", "loginid: "+sysUserInfo.getLoginid());
//                    ScsWebApiTest
                    String result = request.post("http://"+sysUserInfo.getServerip()+":9521/"+trequesturl+"/PrintLab/BindLabCodeForBill", gson.toJson(replaceLabCodePara),sysUserInfo.getLoginid(),"application/json");//调用我们写的post方法
//                    Log.d("main", "run: "+result);


                    ScsWebApiInfo scsWebApiInfo=gson.fromJson(result,ScsWebApiInfo.class);
                    if (scsWebApiInfo.isSuccess()==true){
//                        Log.d("main", "bind: "+scsWebApiInfo.getData());
                        if (scsWebApiInfo.getData()!=null){
                            String[] restData = scsWebApiInfo.getData().split(",");
                            SalseProductName=restData[3];
                        }

                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess,"ok");
                    }else{
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,"服务器:"+scsWebApiInfo.getMessage());
                    }

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
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();
//                    Log.d("main", "handleMessage: 成功");
                    tv_result.setText("成功");
                    tv_result.setTextColor(Color.GREEN);
//                    tv_error.setText("");
//                    tv_information.setText("型号色号:"+modelm+colors);
                    nSize++;
                    tv_goodname.setText(SalseProductName);
                    tv_Scannum.setText(String.valueOf(nSize));
                    et_new_code.requestFocus();
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
//                    Log.d("main", "handleMessage: 失败");
                    tv_result.setText("失败");
                    tv_result.setTextColor(Color.RED);
                    tv_goodname.setText("");
                    ShowMessage.Show(mContext, msg.obj.toString());
                    et_new_code.setText("");
                    et_new_code.requestFocus();
                    break;

                case HandRequestFocus:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    //这里开启一个延迟线程把光标移到上面，不然可能不起作用。
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_new_code.requestFocus();
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


