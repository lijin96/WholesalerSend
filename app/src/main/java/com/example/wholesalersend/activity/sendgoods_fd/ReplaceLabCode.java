package com.example.wholesalersend.activity.sendgoods_fd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.entity.ReplaceLabCodePara;
import com.example.wholesalersend.entity.ScsWebApiInfo;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ReplaceLabCode
 * @Description: 镜片出库绑码
 * @Author: lijin
 * @Date: 2024/12/18 15:18
 */
public class ReplaceLabCode extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;

    private TextView tv_title,tv_old_mess,tv_old_code,tv_new_code,tv_result,tv_Scannum;
    private EditText et_old_code, et_new_code;

    private String OldCode = "", NewBarcode = "";

    private final int HandRequestFocus = 3;

    private int nSize = 0;//次数

    private String SalseProductName="";
    private TextView tv_goodname;

    private Button btn_revoke_code;//撤销绑码
    private AlertDialog mAlertDialog;//弹出层

    private int PopWidth=1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replacelabcode);
        mContext=this;
        accWeb=new AccessWeb(mContext);
        sysUserInfo=new SysUserInfo(mContext);

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【镜片出库绑码】 "+sysUserInfo.getAccountSetName());

        tv_result=findViewById(R.id.tv_result);
        tv_Scannum=findViewById(R.id.tv_Scannum);

        tv_old_mess = (TextView) findViewById(R.id.tv_old_mess);
        tv_old_code = (TextView) findViewById(R.id.tv_old_code);
        tv_new_code = (TextView) findViewById(R.id.tv_new_code);

        et_old_code = (EditText) findViewById(R.id.et_old_code);
        et_new_code = (EditText) findViewById(R.id.et_new_code);

        et_old_code.setOnKeyListener(new EtBarodeOnkeyListener());
        et_new_code.setOnKeyListener(new EtBarodeOnkeyListener());

        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());


        tv_goodname=findViewById(R.id.tv_goodname);

        btn_revoke_code=findViewById(R.id.btn_revoke_code);
        btn_revoke_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopListView();//撤销条码
            }
        });
        // 使用示例
        Rect windowRect = getWindowDisplayArea(this);
//        Log.e("main", "窗口区域: " + windowRect.width() + "x" + windowRect.height());
//        Log.e("main", "位置: (" + windowRect.left + "," + windowRect.top + ")");
        if (windowRect.width()<=850){
            PopWidth=700;
        }
    }
    /**
     * 获取窗口显示区域（排除系统UI）
     */
    public static Rect getWindowDisplayArea(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }

    private void showPopListView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.Base_Theme_AppCompat_Light_Dialog);
        mAlertDialog = builder.setCancelable(false).create();
        View dialogView = View.inflate(mContext, R.layout.select_pop, null);
        //设置对话框布局
        mAlertDialog.setView(dialogView);

        final EditText ed_code = (EditText) dialogView.findViewById(R.id.ed_code);
        ed_code.setFocusable(true);
        ed_code.setFocusableInTouchMode(true);
        ed_code.requestFocus();
        ed_code.setHint("请输入要撤销绑码的条码");
        Button btn_cancel = (Button) dialogView.findViewById(R.id.bt_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        Button bt_pop_ok = (Button) dialogView.findViewById(R.id.bt_pop_ok);
        bt_pop_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = ed_code.getText().toString().trim();
                if (code.equals("")) {
                    ShowMessage.Show(mContext, "条码为空，请重新扫描条码");
                    ed_code.setText("");
                } else {
                    ReplaceLabCodeCancel(code);  //撤销绑码
                    ed_code.setText("");
                }
            }
        });

        ed_code.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        String code = ed_code.getText().toString().trim();
                        if (code.equals("")) {
                            ShowMessage.Show(mContext, "条码为空，请重新扫描条码");
                            ed_code.setText("");
                        } else {
                            ReplaceLabCodeCancel(code);  //撤销绑码
                            ed_code.setText("");
                        }
                    }
                    return true;
                }

                return false;
            }

        });
        mAlertDialog.show();

        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = PopWidth; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
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
                    ReplaceLabCodePara replaceLabCodePara=new ReplaceLabCodePara();
                    replaceLabCodePara.setOriginallyLabCode(newBarcode);
                    replaceLabCodePara.setNewLabCode(oldBarcode);
                    replaceLabCodePara.setMODE("USER");
                    Gson gson=new Gson();
                    String data = "OriginallyLabCode="+newBarcode+"&NewLabCode="+oldBarcode+"&MODE=USER";//POST请求的参数

                    String trequesturl="ScsWebApi";//默认是正式地址
                    if (sysUserInfo.getAPIEndpoint().equals("SCS2.0Test")){
                        trequesturl="ScsWebApiTest";
                    }
//                    Log.d("main", gson.toJson(replaceLabCodePara));
                    String result = request.post("http://"+sysUserInfo.getServerip()+":9521/"+trequesturl+"/PrintLab/ReplaceLabCode", gson.toJson(replaceLabCodePara),sysUserInfo.getLoginid(),"application/json");//调用我们写的post方法
//                    Log.d("main", "run: "+result);


                    ScsWebApiInfo scsWebApiInfo=gson.fromJson(result,ScsWebApiInfo.class);
                    if (scsWebApiInfo.isSuccess()==true){
                        if (scsWebApiInfo.getData()!=null){
                            String[] restData = scsWebApiInfo.getData().split(",");
                            SalseProductName=restData[3];
                        }
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess,"ok");
                    }else{
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,"服务器："+scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            "服务器："+e.getMessage());
                }
            }
        }).start();
    }


    //撤销绑码
    public void ReplaceLabCodeCancel(final String tBarcode) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
//
                    Gson gson=new Gson();
//                    String data = "OriginallyLabCode="+newBarcode+"&NewLabCode="+oldBarcode+"&MODE=USER";//POST请求的参数
//                    Log.d("main", "loginid: "+sysUserInfo.getLoginid());

                    String trequesturl="ScsWebApi";//默认是正式地址
                    if (sysUserInfo.getAPIEndpoint().equals("SCS2.0Test")){
                        trequesturl="ScsWebApiTest";
                    }

                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put("OriginallyLabCode", tBarcode);

                    String result = request.post("http://"+sysUserInfo.getServerip()+":9521/"+trequesturl+"/PrintLab/ReplaceLabCodeCancel", gson.toJson(requestParams),sysUserInfo.getLoginid(),"application/json");//调用我们写的post方法
//                    Log.d("main", "run: "+result);

                    ScsWebApiInfo scsWebApiInfo=gson.fromJson(result,ScsWebApiInfo.class);
                    if (scsWebApiInfo.isSuccess()==true){
//                        if (scsWebApiInfo.getData()!=null){
//                            String[] restData = scsWebApiInfo.getData().split(",");
//                            SalseProductName=restData[3];
//                        }
                        ShowMessage.ShowMsg(handler, 5,"ok");
                    }else{
                        ShowMessage.ShowMsg(handler, 4,"服务器："+scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, 4,
                            "服务器："+e.getMessage());
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
                case  4:
                    //撤销绑码失败报错
                    MyProgressDialog.close();
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case  5:
                    //撤销绑码成功
                    MySound.scanSound();
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, "撤销绑码成功");
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


