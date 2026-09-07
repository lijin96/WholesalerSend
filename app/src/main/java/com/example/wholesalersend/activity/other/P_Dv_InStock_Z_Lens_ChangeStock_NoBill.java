package com.example.wholesalersend.activity.other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.activity.select.QueryScanLensDetail;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_InStock_Z_ChangeStock_NoBill
 * @Description: 仓库无单镜片调拨
 * @Author: lijin
 * @Date: 2026年8月19日17:10:50
 */
public class P_Dv_InStock_Z_Lens_ChangeStock_NoBill extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_outstock_name, tv_source_billno,
            tv_billno, tv_model_colors, tv_instock_name, tv_goodsid,tv_title;
    private EditText et_barcode;
    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "";
    private String curcount = "0", goodsid = "", mBillNo = "";
    private String instock_id = "", instock_name = "", outstock_id = "", outstock_name = "";
    private String lastSuccessBarcode = "", lStar = "";
    //    private String modelm = "", colors = "";
    private String Spherical= "", Cylinder = "",Refractivity="";//球镜柱镜折射率


    private final int Lic_SelectModel = 2;

    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_instock_z_lens_changestock_nobill);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

        //把以前扫描的数据清空
//        try {
//            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        ((Button) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid))
                .setOnClickListener(new BtnSelectProductClick());

        tv_outstock_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_instock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_source_billno = (TextView) findViewById(R.id.tv_source_billno);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【仓库无单镜片调拨】 "+sysUserInfo.getAccountSetName());

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        instock_id = gIntent.getStringExtra("instock_id");
        instock_name = gIntent.getStringExtra("instock_name");
        outstock_id = gIntent.getStringExtra("outstock_id");
        outstock_name = gIntent.getStringExtra("outstock_name");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统
        sysUserInfo.setIsDownload(true);

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_outstock_name.setText(outstock_name);
        tv_instock_name.setText(instock_name);
        tv_source_billno.setText("");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		sysUserInfo.SaveConfigString("searchProductSql", "");
//		send.interrupt();
//		try {
//			send.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
    }

    /**
     * 处理逻辑的handler
     */
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
//                    MySound.scanSound();
//				tv_model_colors.setText(modelm + "-" + colors);
//				tv_curqty.setText(curcount);
//				tv_totalqty.setText(nScanCount);

//                    tv_source_billno.setText(mBillNo);
//                    try {
//                        ScanDataDao.updateDataAndUi(mContext, tv_model_colors, tv_curqty, tv_totalqty, tv_billno, curcount, goodsid, modelm, colors, mBillNo);
//
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                        ShowMessage.Show(mContext, e.getMessage());
//                    }
//				tv_goodsid.setText("("+goodsid+")");

                    MySound.scanSound();
//                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_model_colors.setText(Refractivity+ "  S"+Spherical + " C" + Cylinder);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_source_billno != null) {
                        tv_source_billno.setText(mBillNo);//调拨单号显示
                    }
                    tv_goodsid.setText("(" + goodsid + ")");

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[1];
                    mark[0] = "调拨单：" + mBillNo;

                    if (sysUserInfo.getOldVersion().equals("T8")) {

                        printbill.prints("         仓库无单调拨", mark, sacnDataList, sysUserInfo.getUserName());

                    } else {

                        printbill.print(P_Dv_InStock_Z_Lens_ChangeStock_NoBill.this, "         仓库无单调拨", mark, sacnDataList, sysUserInfo.getUserName());

                    }
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    }


    /**
     * 返回按钮监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //连续按返回按钮两次就退出界面
                if (SomeUtils.isDoubleClick(mContext, true)) {
                    finish();
                }
                return true;
            case KeyEvent.KEYCODE_MINUS:

        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {
                case Lic_SelectModel:
//                    goodsid = data.getStringExtra("goodsid");
//                    modelm = data.getStringExtra("modelm");
//                    colors = data.getStringExtra("colors");
//                    String productinfo = modelm + "-" + colors;
//                    tv_model_colors.setText(productinfo);
//                    tv_goodsid.setText("(" + goodsid + ")");
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

//	private class SendDatas extends Thread
//	{
//		@Override
//		public void run() {
//			try
//			{
//				while(!Thread.currentThread().isInterrupted()){
//					if(codesList.size()>0)
//					{
//						if (access_send(codesList.get(0).toString())){
//							codesList.remove(0);
//						}
//						else
//						{
//							codesList.remove(0);
//						}
//					}
//
//				}
//
//			}
//			catch(Exception e)
//			{
//				Log.d("main","thread end");
//			}
//
//		}
//	}

    // 请求服务
    @SuppressLint("NewApi")
    private void access_send(final String contents) {

        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {


                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;
                    para.setBarcode(contents);
                    para.setGoodsId("");
                    para.setSoCompId(outstock_id);
                    para.setDeCompId(instock_id);
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);

//                    result = accWeb.P_Dv_Scan("P_Dv_InStock_Z_ChangeStock_NoBill", para.toJson());

                    result =accWeb.PostAPIStringInterface("AndroidDv/ChangeStockLensNoBill", para.toJson());

//                    Log.d("main---", result);

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
//                        {"goodsCode":"迪士尼9800C1","modelm":"9800","color":"C1","goodsCount":1,"barcode":"0019045171222553","billNo":"IO-1746072603130002","billCount":1}
//                    {
//                        "goodsCode": "string",
//                            "modelm": "string",
//                            "color": "string",
//                            "goodsCount": 0,
//                            "barcode": "string",
//                            "billNo": "string",
//                            "billCount": 0,
//                            "stockName": "string",
//                            "restValueCcs": "string"
//                    }
                    JSONObject jsonObject = new JSONObject(result);
                    nSize++;
                    goodsid = jsonObject.optString("goodsCode");

                    Refractivity = jsonObject.getString("refractiveIndex");
                    Spherical =jsonObject.getString("diopter");
                    Cylinder = jsonObject.getString("astigmatism");

                    lastSuccessBarcode = jsonObject.optString("barcode");

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = jsonObject.optString("billNo");
                    }
                    Log.d("main---", mBillNo);
                    if (Integer.parseInt(nScanCount) < Integer.parseInt(jsonObject.optString("billCount"))) {
                        curcount = jsonObject.optString("goodsCount");
                        nScanCount = jsonObject.optString("billCount");
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                    lStar = "";


                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    /**
     * 输入框监听
     */
    private class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode = "";
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1 ){
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tBarcode = et_barcode.getText().toString().trim();
                    }

                    tv_show_code.setText(tBarcode);
                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                        et_barcode.setText("");
                        return true;
                    }
                    access_send(tBarcode);
                    et_barcode.setText("");

                }
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * 明细按钮监听类
     */
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext,
                    QueryScanLensDetail.class);
            intent.putExtra("mBillNo", scanBillno);
            startActivity(intent);
        }
    }

    /**
     * 打印按钮监听类
     */
    private class BtnPrintClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MyProgressDialog.show(mContext, "正在打印...", true, true);
            Thread sendprint = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), scanBillno);
                        if (sacnDataList.size() == 0) {
                            ShowMessage.ShowMsg(handler, 9, "没有可打印的数据");
                            return;
                        }

                        ShowMessage.ShowMsg(handler, 8, "打印");
                    } catch (Exception e) {
                        ShowMessage.ShowMsg(handler, 9, e.getMessage());
                    }

                }
            });
            sendprint.start();


        }
    }

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
     * 选择型号色号按钮监听
     */
    private class BtnSelectProductClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(mContext, SelectProductModelColor.class);
//            startActivityForResult(intent, Lic_SelectModel);
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


