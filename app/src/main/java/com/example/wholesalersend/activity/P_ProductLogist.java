package com.example.wholesalersend.activity;


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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.SelectCCSBrand;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_ProductLogist
 * @Description: 物流查询
 * @Author: lijin
 * @Date: 2023/10/25 17:02
 */
public class P_ProductLogist extends Activity {

    private Context mContext;
    private AccessWeb accessWeb;
    private SysUserInfo sysUserInfo;
//    private MySound sound;

    private RadioGroup radio_group,ccsradio_group;
    private EditText et_barcode,et_ccsbarcode;
    private TextView tv_message;
    private TextView tv_title;

    private List<Map<String, Object>> Remarks;

    private final String lsc_Logistics = "1";//1-物流码
    private final String lsc_Security = "2";//2-表示防伪码
    private final String lsc_Integral = "3";//3-表示积分码

    private RadioButton radio_logistics,ccsradio_logistics;//默认选中物流码

    private String tCodeType = lsc_Logistics;//1-物流码;2-表示防伪码;3-表示积分码
    private String tCodeValue = "";//扫描条码
    private String tUnitId;//用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
    private String message = "";

//    private int count = 0;
//    private Handler handlers = new android.os.Handler();
//    private TextView TestSize;
//    private Runnable runnableV1;

    private RelativeLayout Relative_SCSLogistics;//SCS物流显示
    private RelativeLayout Relative_CCSLogistics;//CCS物流显示

    private RadioGroup radio_group_logistics;
    private RadioButton rb_scslogistics,rb_ccslogistics;
    private Boolean IsCCSScanCode=false;

    private String BrandName="",BrandCode="";//品牌名称和品牌代号

    private TextView choose_brand,tv_brand_name;//选择品牌 品牌名称展示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productlogist);

        mContext = this;
        accessWeb = new AccessWeb(mContext);
        sysUserInfo = new SysUserInfo(mContext);
//        sound = MySound.getMySound(this);

        radio_group = (RadioGroup) findViewById(R.id.radio_group);
        ccsradio_group=findViewById(R.id.ccsradio_group);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_ccsbarcode=findViewById(R.id.et_ccsbarcode);
        tv_message = (TextView) findViewById(R.id.tv_message);
        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("物流查询("+sysUserInfo.getAccountSetName()+")");

        radio_logistics=findViewById(R.id.radio_logistics);
        ccsradio_logistics=findViewById(R.id.radio_logistics);


        Relative_SCSLogistics=findViewById(R.id.Relative_SCSLogistics);
        Relative_CCSLogistics=findViewById(R.id.Relative_CCSLogistics);

        radio_group_logistics=findViewById(R.id.radio_group_logistics);
        rb_scslogistics=findViewById(R.id.rb_scslogistics);
        rb_ccslogistics=findViewById(R.id.rb_ccslogistics);


        if (sysUserInfo.getLoginType().equals("CCS")){
            radio_group_logistics.setVisibility(View.GONE);
        }

        choose_brand=findViewById(R.id.choose_brand);
        tv_brand_name=findViewById(R.id.tv_brand_name);

        choose_brand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, SelectCCSBrand.class);
                intent.putExtra("aim", "GetCcsBarcodeTrackingInfor");
                startActivityForResult(intent, 10);
            }
        });



        tUnitId = sysUserInfo.getCompanyid();//总公司"00",大代理商模式就为代理商代号

        radio_group.setOnCheckedChangeListener(new RadioGroupChangeListener());
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());
        et_barcode.requestFocus();
        et_ccsbarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ( i == KeyEvent.KEYCODE_ENTER) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {

                        if (et_ccsbarcode.getText().toString().trim().indexOf("=") != -1||et_ccsbarcode.getText().toString().trim().indexOf("http") != -1) {
                            //包含
                            tCodeValue = SomeUtils.InterceptCode(mContext, et_ccsbarcode.getText().toString().trim());
                        } else {
                            //不包含
                            tCodeValue = et_ccsbarcode.getText().toString().trim();
                        }

                        if (!SomeUtils.isAllNumber(mContext, tCodeValue)) {
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + tCodeValue + "】");
                            et_ccsbarcode.requestFocus();
                            et_ccsbarcode.setText("");
                            return true;
                        }
                        if (BrandCode.equals("")){
                            ShowMessage.Show(mContext, "请先选择品牌后再扫码查询");
                            return true;
                        }
                        ccsaccess_send(tCodeType, tCodeValue);
                        et_ccsbarcode.setText("");

                    }
                }

                return false;
            }
        });

        ccsradio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.ccsradio_logistics:
                        tCodeType = lsc_Logistics;
                        break;
                    case R.id.ccsradio_security:
                        tCodeType = lsc_Security;
                        break;
                    case R.id.ccsradio_integral:
                        tCodeType = lsc_Integral;
                        break;

                    default:
                        break;
                }
            }
        });


        radio_group_logistics.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = radioGroup.getCheckedRadioButtonId();
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rb_scslogistics:
                        Relative_SCSLogistics.setVisibility(View.VISIBLE);
                        Relative_CCSLogistics.setVisibility(View.GONE);
                        IsCCSScanCode=false;
                        BrandName="";
                        BrandCode="";
                        tv_brand_name.setText("");
                        tCodeType="1";
                        radio_logistics.setChecked(true);
                        tv_message.setText("");
                        et_barcode.requestFocus();
                        break;
                    case R.id.rb_ccslogistics:
                        Relative_SCSLogistics.setVisibility(View.GONE);
                        Relative_CCSLogistics.setVisibility(View.VISIBLE);
                        IsCCSScanCode=true;
                        BrandName="";
                        BrandCode="";
                        tv_brand_name.setText("");
                        tCodeType="1";
                        ccsradio_logistics.setChecked(true);
                        tv_message.setText("");
                        et_ccsbarcode.requestFocus();
                        break;
                }
            }
        });


//        TestSize=findViewById(R.id.TestSize);
//
//         runnableV1 = new Runnable() {
//            @Override
//            public void run() {
//                //要做的事情
//                Log.d("mian",count+"");
//
//                TestSize.setText(count+"");
//
//
//                if (!P_ProductLogist.this.isFinishing())//xActivity即为本界面的Activity
//                {
//                    MyProgressDialog.show(mContext, count+"", false, true);
//                }
//
//
//                access_send(tCodeType, "3002925119021022", tUnitId);
//                //为什么是>=2，因为count为0时执行一次M，1时执行一次M，2时执行一次M，然后移除，不再执行(共执行3次M)
//                if (count >= 18000) {
//                    handlers.removeCallbacks(this);//删除指定Runnable ，停止运行
//                } else {
//                    handlers.postDelayed(this, 1000);//一秒后执行
//                    count++;
//                }
//            }
//        };
//
//        handlers.post(runnableV1);//启动定时器



//        et_barcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId == EditorInfo.IME_ACTION_SEND||actionId== EditorInfo.IME_ACTION_DONE ||event.getKeyCode()==KeyEvent.KEYCODE_ENTER&&v.getText()!=null&& event.getAction() == KeyEvent.ACTION_DOWN){
//                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1 {
//                        //包含
//                        tCodeValue = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
//                    } else {
//                        //不包含
//                        tCodeValue = et_barcode.getText().toString().trim();
//                    }
//
//                    if (!SomeUtils.isAllNumber(mContext, tCodeValue)) {
//                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tCodeValue + "】");
//                        et_barcode.requestFocus();
//                        et_barcode.setText("");
//                        return false;
//                    }
//                    access_send(tCodeType, tCodeValue, tUnitId);
//                    et_barcode.setText("");
//                    return false;
//                }
//                return false;//返回true，保留软键盘;false，隐藏软键盘
//            }
//        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {

                case 10:
                    //选择品牌后返回
                    BrandCode=data.getStringExtra("BrandCode");
                    BrandName=data.getStringExtra("BrandName");
                    tv_brand_name.setText(BrandName);
                    break;


            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //输入框监听
    class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
//            Log.d("回车===",keyCode+"");
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					tCodeValue = et_barcode.getText().toString().trim();

                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tCodeValue = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tCodeValue = et_barcode.getText().toString().trim();
                    }


                    if (!SomeUtils.isAllNumber(mContext, tCodeValue)) {
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tCodeValue + "】");
                        et_barcode.requestFocus();
                        et_barcode.setText("");
                        return true;
                    }

                    access_send(tCodeType, tCodeValue, tUnitId);
                    et_barcode.setText("");

                }
            }

            return false;
        }
    }

    /**
     * @param tCodeType  查码类型(其值为：1或2或3，说明：1-物流码;2-表示防伪码;3-表示积分码)
     * @param tCodeValue 查码内容,扫描条码
     */
    public void ccsaccess_send(final String tCodeType, final String tCodeValue) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    message = "";
                    Remarks = accessWeb.GetCcsBarcodeTrackingInfor(BrandCode, BrandName, tCodeType, tCodeValue);
                    for (Map<String, Object> m : Remarks) {
                        message += m.get("Remark") + "\r\n";
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                } catch (Exception e) {
                    message = e.getMessage();
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * @param tCodeType  查码类型(其值为：1或2或3，说明：1-物流码;2-表示防伪码;3-表示积分码)
     * @param tCodeValue 查码内容,扫描条码
     * @param tUnitId    用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
     */
    public void access_send(final String tCodeType, final String tCodeValue, final String tUnitId) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    message = "";
                    Remarks = accessWeb.P_ProductLogist(tCodeType, tCodeValue, tUnitId);
                    for (Map<String, Object> m : Remarks) {
                        message += m.get("Remark") + "\r\n";
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                } catch (Exception e) {
                    message = e.getMessage();
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ShowMessage.HandScanSuccess:
//                    MySound.scanSound();
//                    MyProgressDialog.close();
                    MySound.scanSound();

                    tv_message.setText(message);
//                    et_barcode.requestFocus();
                    if (IsCCSScanCode){
                        et_ccsbarcode.requestFocus();
                    }else{
                        et_barcode.requestFocus();
                    }
                    break;
                case ShowMessage.HandScanError:
//                    MySound.errorSound();
//                    MyProgressDialog.close();
                    MySound.errorSound();
                    tv_message.setText(message);
                    et_barcode.setText("");
                    et_ccsbarcode.setText("");
                    if (IsCCSScanCode){
                        et_ccsbarcode.requestFocus();
                    }else{
                        et_barcode.requestFocus();
                    }
                    break;
            }
        }
    };


    /**
     * 单选监听
     */
    class RadioGroupChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                case R.id.radio_logistics:
                    tCodeType = lsc_Logistics;
                    break;
                case R.id.radio_security:
                    tCodeType = lsc_Security;
                    break;
                case R.id.radio_integral:
                    tCodeType = lsc_Integral;
                    break;

                default:
                    break;
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (handlers != null) {
//            handlers.removeCallbacks(runnableV1);
//        }
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



