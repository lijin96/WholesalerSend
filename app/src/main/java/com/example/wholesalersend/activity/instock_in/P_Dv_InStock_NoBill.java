package com.example.wholesalersend.activity.instock_in;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.entity.ApiResponse;
import com.example.wholesalersend.entity.GoodsMarkLabelScan;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_InStock_NoBill
 * @Description: 无单入库
 * @Author: lijin
 * @Date: 2021/3/10 13:59
 */
public class P_Dv_InStock_NoBill extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name,
            tv_billno, tv_model_colors, tv_stock_name, tv_goodsid,tv_title;
    private EditText et_barcode;

    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", supplier_id = "", supplier_name = "";
    private String curcount = "0", goodsid = "", stock_id = "", stock_name;
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

    private TextView tv_lastscannum;//上一次扫描的数量

    private RadioGroup radioChange;
    private RadioButton rb_logisticscode,rb_productcode;//物流码还是产品码

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_instock_nobill);

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

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_goodsid = (TextView) findViewById(R.id.TextViewid);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        sysUserInfo.setSeachValue("");

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【无单入库】 "+sysUserInfo.getAccountSetName());

        tv_lastscannum=findViewById(R.id.tv_lastscannum);

        supplier_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);

        String ChooseBill=gIntent.getStringExtra("scanBillNo");
        String ChooseBillnum=gIntent.getStringExtra("scanBillNum");

        if (ChooseBill!=null&&ChooseBill!=""){
            mBillNo=ChooseBill;
            tv_billno.setText(mBillNo);
        }

        if (ChooseBillnum!=null&&!ChooseBillnum.equals("")){
//            nScanCount=ChooseBillnum;
//            tv_totalqty.setText(nScanCount);
            tv_lastscannum.setText("上次扫描："+ChooseBillnum);

        }

        radioChange=findViewById(R.id.radiogroup_change);
        rb_logisticscode=findViewById(R.id.radio_logisticscode);
        rb_productcode=findViewById(R.id.radio_productcode);
        rb_logisticscode.setChecked(true);
//        radioChange.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//
//            }
//        });
        rb_logisticscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rb_logisticscode.setChecked(true);
                rb_productcode.setChecked(false);
            }
        });
        rb_productcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rb_productcode.setChecked(true);
                rb_logisticscode.setChecked(false);
            }
        });


//		send = new SendDatas();
//		send.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sysUserInfo.SaveConfigString("searchProductSql", "");
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
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MyProgressDialog.close();
                    MySound.scanSound();

                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);
                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    tv_goodsid.setText("(" + goodsid + ")");
                    break;
                case ShowMessage.HandScanError:
                    MyProgressDialog.close();
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[3];
                    mark[0] = "入库单：" + mBillNo;
                    mark[1] = "供应商：" + tv_company_name.getText().toString();
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {

                        printbill.prints("           无单入库", mark, sacnDataList, sysUserInfo.getUserName());

                    } else {

                        printbill.print(P_Dv_InStock_NoBill.this, "           无单入库", mark, sacnDataList, sysUserInfo.getUserName());

                    }
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
                    rb_logisticscode.setChecked(true);
                    rb_productcode.setChecked(false);
                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_goodsid.setText("(" + goodsid + ")");
                    ShowMessage.Show(mContext, "已选择商品");

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
                    goodsid = data.getStringExtra("goodsid");
                    modelm = data.getStringExtra("modelm");
                    colors = data.getStringExtra("colors");
                    String productinfo = modelm + "-" + colors;
                    String produvtid = "（" + goodsid + ")";
                    tv_model_colors.setText(productinfo);
                    tv_goodsid.setText(produvtid);
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

    // 获取产品码对应的商品
    private void DownLoadDataThread(final String tDateCode) {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
//        list = new ArrayList<Map<String, Object>>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();
                    Date now = new Date();
                    //Log.d("main", "loginid: "+sysUserInfo.getLoginid());
                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GoodsMarkLabelScan"+ "?GoodsType="+"&GoodsMarkLabel="+tDateCode;

                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
                    ApiResponse<GoodsMarkLabelScan> response = gson.fromJson(
                            result,
                            new TypeToken<ApiResponse<GoodsMarkLabelScan>>(){}.getType()
                    );

                    if(response.isSuccess()) { // 假设有isSuccess()方法
                        GoodsMarkLabelScan goods = response.getData();
                        if (!goods.getGoodsTypeName().equals("镜片")){
                            modelm=goods.getModelm();
                            colors=goods.getColors();
                            goodsid=goods.getGoodsCode();
                            ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess,"ok");
                        }else{
                            ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,"镜架入库只能选择非镜片产品");
                        }
                    }else{
                        ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,response.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,
                            e.getMessage());
                }
            }
        }).start();
    }



    // 请求服务
    private void access_send(final String contents) {
        if (goodsid.equals("")) {
            MySound.errorSound();
            ShowMessage.ShowMsg(handler, "请先扫描产品条码或者手动选择产品");
            return;

        }
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

                    //					 Barcode：扫描的条码(必传)
                    //	                 GoodsId：入库的产品(必传)
                    //	                 SoCompId：供应商代号(必传)
                    //	                 DeCompId：总公司代号(固定值：00)
                    //	                 OaSuserId：扫描人员代号(必传)
                    //	                 StockId：仓库代号(必传)
                    //	                 ScanSn：扫描序号(必传)
                    //	                 scanBillno：扫描单号(必传)
                    //	                 BillNo：入库单号(首次扫码传空，成功再扫码时传返回的入库单号)
                    //	                 SourceBillNo：来源单号(传空)

                    para.setBarcode(contents);
                    para.setGoodsId(goodsid);
                    para.setSoCompId(supplier_id);

                    para.setDeCompId("00");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_id);
                    //			para.setStockId(URLDecoder.decode(stock_id,"UTF-8"));

                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo("");

                    if (ADevicesManager.isSCSNewInterface){
                        result =accWeb.PostAPIStringInterface("AndroidDv/InStockNoBill", para.toJson());
//                        {"goodsCode":"迪士尼9800C1","modelm":"9800","color":"C1","goodsCount":1,"barcode":"0019045171222553","billNo":"IO-1746072603130002","billCount":1}
                        JSONObject jsonObject = new JSONObject(result);
                        nSize++;
                        goodsid = jsonObject.optString("goodsCode");
                        modelm = jsonObject.optString("modelm");
                        colors =jsonObject.optString("color");
                        //0是物流码 1是产品码
//                        if (rest[7] != null && rest[7].equals("0")) {
                        lastSuccessBarcode = jsonObject.optString("barcode");

                        if (mBillNo == null || mBillNo.isEmpty()) {
                            mBillNo = jsonObject.optString("billNo");
                        }
                        if (Integer.parseInt(nScanCount) < Integer.parseInt(jsonObject.optString("billCount"))) {
                            curcount = jsonObject.optString("goodsCount");
                            nScanCount = jsonObject.optString("billCount");
                        }
//                        } else {
//                            curcount =jsonObject.optString("goodsCount");
//                        }

                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                        lStar = "";
                    }else {

                        result = accWeb.P_Dv_Scan("P_Dv_InStock_NoBill", para.toJson());

                        if (result == "") {
                            MySound.errorSound();
                            ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                            return;
                        }
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号
                        String[] rest = result.split(",");

                        if (rest.length < 6) {
                            MySound.errorSound();
                            ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                            return;
                        }
                        nSize++;
                        //goodsid = rest[0].trim();
                        modelm = rest[1].trim();
                        colors = rest[2].trim();
                        curcount = rest[3].trim();
                        lastSuccessBarcode = rest[4].trim();

                        if (mBillNo == null || mBillNo.isEmpty()) {
                            mBillNo = rest[5];
                        }
                        if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[6].trim())) {
                            nScanCount = rest[6].trim();
                        }
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                        lStar = "";
                    }
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

//
//                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm.isActive()) {
//                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
//                }



                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode = et_barcode.getText().toString().trim();
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    }
//2800056JS008004
//                    String firstCode=tBarcode.substring(0, 2);
                    if (rb_productcode.isChecked()==true){
                        //商品码 可查询型号色号
                        DownLoadDataThread(tBarcode);
                        et_barcode.setText("");
                    }else{
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
                    QueryScanDetail.class);
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
            Intent intent = new Intent(mContext, SelectProductModelColor.class);
            intent.putExtra("aim", "P_Dv_InStock_NoBill");
            startActivityForResult(intent, Lic_SelectModel);
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}

