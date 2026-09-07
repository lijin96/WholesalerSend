package com.example.wholesalersend.activity.backgoods_fd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.activity.select.SelectCCSBrand;
import com.example.wholesalersend.activity.select.SelectCCScustomer;
import com.example.wholesalersend.activity.select.SelectCCScustomerstore;
import com.example.wholesalersend.activity.select.SelectCCSstore;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_ReturnedPurchase_Z_L_NoBill
 * @Description: 分店无单退货
 * @Author: lijin
 * @Date: 2021/3/10 10:24
 */
public class P_Dv_ReturnedPurchase_Z_L_NoBill extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name,
            tv_billno, tv_model_colors, tv_stock_name, tv_goodsid,tv_retail_store,tv_title;
    private EditText et_barcode;
    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", company_id = "", company_name = "", store_name="";
    private String curcount = "0", goodsid = "", stock_id = "",StockSysCode="",retail_id="",retail_name="", stock_name="",TraderAlias_name="";
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";
    private String Scan_aim="";//跳转过来的模式

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销的参数-次数

    //撤销的型号色号，产品id，单号，当前型号数量，合计
    private String Cancelmodelm = "", Cancelcolors = "", Cancelgoodsid = "", CancelmBillNo = "",
            Cancelcurcount = "";

//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    private Button btn_p_dv_returnedpurchase_z_l_nobill_revoke;
    private PopupWindow mPopWindow;

    private TextView tv_lastscannum;

    private Button btn_refresh;//刷新

    private TextView tv_brand_name;//品牌名称
    private TextView bth_choosebrand;//选择品牌

    private RelativeLayout relayout_ccsstore;//选择ccs客户布局

    private TextView btn_ChooseCCSData;//选择CCS门店/客户

    private String BrandCode="",BrandName="",BrandingCode="";

    private String CcsCustName="",CcsStoreName="";//当前绑定的CCS客户或者门店

    private final int Lic_SelectCCSBrand=10;//选择CCS品牌
    private final int Lic_SelectCCSStore=11;//选择CCS门店
    private final int Lic_SelectCCSCustomer=12;//选择CCS客户
    private final int Lic_SelectCCSCustomerStore=13;//先选择CCS客户在选择门店绑定，没有同步功能

    private String AgentCode="",BrandingCustCode="",BrandingStoreCode="";
    private Boolean IsBindCCS=false,IsBindCCScust=false,IsBindCCSstore=false,IsSendToStore=false;

    private String TraderSysId="",StoreSysId="";//SCS客户系统代号  SCS门店系统代号

    private TextView tv_ccsstore_str,tv_ccsstore_name;//绑定客户还是门店。显示绑定的数据


    private String SearchBindName="";//TraderAlias是客户别名，Alias是门店别名，如果是要绑到店的话，就用Alias，不到店用TraderAlias；brandingcode是01的时候，带联系人过去

    private String LinkName="";//联系人名称


    private Button btn_abnormal_change;//异常条码变更

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_p_dv_returnedpurchase_z_l_nobill);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();


        sound =MySound.getMySound(getApplicationContext());

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
//        ((Button) findViewById(R.id.btn_select_goodsid))
//                .setOnClickListener(new BtnSelectProductClick());

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_title=(TextView) findViewById(R.id.tv_title);
        tv_retail_store=findViewById(R.id.tv_retail_store);

        relayout_ccsstore=findViewById(R.id.relayout_ccsstore);

        tv_ccsstore_str=findViewById(R.id.tv_ccsstore_str);
        tv_ccsstore_name=findViewById(R.id.tv_ccsstore_name);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        company_id = gIntent.getStringExtra("company_id");
        company_name = gIntent.getStringExtra("company_name");
        stock_id = gIntent.getStringExtra("stock_id");
        StockSysCode= gIntent.getStringExtra("stock_sysid");
        stock_name = gIntent.getStringExtra("stock_name");
        retail_id= gIntent.getStringExtra("retail_id");
        retail_name= gIntent.getStringExtra("retail_name");
        TraderAlias_name= gIntent.getStringExtra("alias_name");

//        TraderSysId=gIntent.getStringExtra("trader_sysid");
//        StoreSysId=gIntent.getStringExtra("store_sysid");
        if (gIntent.getStringExtra("trader_sysid")!=null) {
            TraderSysId = gIntent.getStringExtra("trader_sysid");
        }
        if (gIntent.getStringExtra("store_sysid")!=null) {
            StoreSysId = gIntent.getStringExtra("store_sysid");
        }

        Scan_aim= gIntent.getStringExtra("aim");
//        if (Scan_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")){
//            tv_title.setText("【分店无单选客退货】 "+sysUserInfo.getAccountSetName());
//            relayout_ccsstore.setVisibility(View.VISIBLE);
//        }else{
//            tv_title.setText("【分店无单扫描退货】 "+sysUserInfo.getAccountSetName());
//            relayout_ccsstore.setVisibility(View.GONE);
//        }
        if (Scan_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")){
            tv_title.setText("【分店无单选客退货】 "+sysUserInfo.getAccountSetName());
//            relayout_ccsstore.setVisibility(View.VISIBLE);
        }else if (Scan_aim.equals("ReturnedPurchase_D_L_NoBill_SameCust")){
            tv_title.setText("【镜架跨店退货】 "+sysUserInfo.getAccountSetName());
        }
        else{
            tv_title.setText("【分店无单扫描退货】 "+sysUserInfo.getAccountSetName());
//            relayout_ccsstore.setVisibility(View.GONE);
        }
        tv_lastscannum=findViewById(R.id.tv_lastscannum);

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
//        tv_company_name.setText(company_name);
        tv_company_name.setText(company_name);
        tv_stock_name.setText(stock_name);
        tv_retail_store.setText(retail_name);

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

//		send = new SendDatas();
//		send.start();
        btn_p_dv_returnedpurchase_z_l_nobill_revoke = (Button) findViewById(R.id.btn_p_dv_returnedpurchase_z_l_nobill_revoke);
        btn_p_dv_returnedpurchase_z_l_nobill_revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
//                showPopListView();
            }
        });

        btn_refresh=findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new BtRefreshClick());

        tv_brand_name=findViewById(R.id.tv_brand_name);
        bth_choosebrand=findViewById(R.id.bth_choosebrand);
        bth_choosebrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(mContext, SelectCCSBrand.class);
                intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_NoBill");
                intent.putExtra("trader_sysid", TraderSysId);
                intent.putExtra("store_sysid",StoreSysId);
                startActivityForResult(intent, Lic_SelectCCSBrand);
            }
        });

        btn_ChooseCCSData=findViewById(R.id.btn_ChooseCCSData);//选择CCS数据
        btn_ChooseCCSData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BrandCode.equals("")){
                    ShowMessage.ShowMsg(handler, "请先选择品牌");
                }else{
                    Intent bindIntent=new Intent();
                    if (BrandingCode.equals("01")) {
                        //雅瑞接口必须先搜索客户后，再去选择门店绑定
                        bindIntent = new Intent(mContext, SelectCCScustomerstore.class);
                    }else {
                        if (IsSendToStore) {
                            //如果可以选择到门店，直接跳转到选择门店界面
                            bindIntent = new Intent(mContext, SelectCCSstore.class);

                        } else {
                            //否则就只能跳转CCS客户界面
                            bindIntent = new Intent(mContext, SelectCCScustomer.class);
                        }
                    }
                    bindIntent.putExtra("aim", "");
                    bindIntent.putExtra("Agent_Code", AgentCode);
                    bindIntent.putExtra("Brand_code", BrandingCode);
                    bindIntent.putExtra("CCSCuts_Code", BrandingCustCode);
                    bindIntent.putExtra("trader_sysid", TraderSysId);
                    bindIntent.putExtra("store_sysid", StoreSysId);
//                    Log.d("main", BrandingCode);
                    bindIntent.putExtra("LinkName", SearchBindName);
//                    }
                    if (BrandingCode.equals("01")){
                        startActivityForResult(bindIntent, Lic_SelectCCSCustomerStore);
                    }else {
                        if (IsSendToStore) {
                            //如果可以选择到门店，直接跳转到选择门店界面
                            startActivityForResult(bindIntent, Lic_SelectCCSStore);
                        } else {
                            //否则就只能跳转CCS客户界面
                            startActivityForResult(bindIntent, Lic_SelectCCSCustomer);
                        }
                    }
                }
            }
        });


        //异常条码变更
        btn_abnormal_change=findViewById(R.id.btn_abnormal_change);
        btn_abnormal_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    /**
     * 弹出撤销扫码的输入框
     */
//    private void showPopListView() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View contentView = inflater.inflate(R.layout.select_pop, null);
//        View list = LayoutInflater.from(this).inflate(
//                R.layout.new_p_dv_returnedpurchase_z_l_nobill, null);
//        if (mPopWindow == null) {
//            mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        }
//
//
//        mPopWindow.setFocusable(true);
//        mPopWindow.setOutsideTouchable(false);
//        mPopWindow.setBackgroundDrawable(null);
//        mPopWindow.getContentView().setFocusable(true); // 这个很重要
//        mPopWindow.getContentView().setFocusableInTouchMode(true);
//
//        ColorDrawable dw = new ColorDrawable(0x00000000);
//        //设置SelectPicPopupWindow弹出窗体的背景
//        mPopWindow.setBackgroundDrawable(dw);
//
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.alpha = 0.5f;
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        getWindow().setAttributes(lp);
//
//        mPopWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
//        mPopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        mPopWindow.showAtLocation(
//                list,
//                Gravity.CENTER, 0, 0);
//        mPopWindow.update();
//        et_barcode.setFocusable(false);
//        final EditText ed_code = (EditText) contentView.findViewById(R.id.ed_code);
//        ed_code.setFocusable(true);
//        ed_code.setFocusableInTouchMode(true);
//        ed_code.requestFocus();
//        ed_code.setHint("请扫描要撤销的条码");
//        Button btn_cancel = (Button) contentView.findViewById(R.id.bt_cancel);
//        btn_cancel.setText("关闭");
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                WindowManager.LayoutParams lp = getWindow().getAttributes();
//                lp.alpha = 1f;
//                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                getWindow().setAttributes(lp);
//                mPopWindow.dismiss();
//                et_barcode.setFocusable(true);
//                et_barcode.setFocusableInTouchMode(true);
//                et_barcode.requestFocus();
//            }
//        });
//        ed_code.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_ENTER) {
//                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                        String code = ed_code.getText().toString().trim();
//                        if (code.equals("")) {
//                            ed_code.setText("");
//                            ShowMessage.Show(mContext, "请重新扫描条码");
//
//                        } else if (!SomeUtils.TextJudgmentSize(code)) {
//                            ed_code.setText("");
//                            ShowMessage.Show(mContext, "请扫描正确的条码");
//
//                        } else {
//                            //发货撤销
//                            ed_code.setText("");
//                            startThreadCheckCode(code);
//
//                        }
//                    }
//                    return true;
//                }
//
//                return false;
//            }
//
//        });
//
//    }

    //发货撤销功能
    private void startThreadCheckCode(final String code) {
        Thread startCode = new Thread(new Runnable() {

            @Override
            public void run() {
                String result = "";
                try {

                    accWeb.mWebId = lStar + code;

//					Barcode：扫描的条码(必传)
//	                 GoodsId：产品编号(传空)
//	                 SoCompId：总公司代号(固定值：00)(传空)
//	                 DeCompId：代理商代号(传空)
//	                 OaSuserId：扫描人员代号(必传)
//	                 StockId：仓库代号(传空)
//	                 ScanSn：扫描序号(必传)
//	                 ScanBillNo：扫描单号(必传)
//	                 BillNo：发货单号(传空)
//	                 SourceBillNo：来源单号(传空)
                    para.setBarcode(code);
                    para.setGoodsId("");
                    para.setSoCompId("");
                    para.setDeCompId("");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId("");

                    para.setScanSn(String.valueOf(cSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo("");
                    para.setSourceBillNo("");

                    result = accWeb.P_Dv_Scan("P_Dv_CurrentTradeCancel", para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                    }
                    //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
                    String[] rest = result.split(",");

                    if (rest.length < 6) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");

                    }
                    cSize++;
                    //					true;产品编号,型号,色号,当前型号数量,当前扫描的条码,发货单号
                    Cancelgoodsid = rest[0].trim();
                    Cancelmodelm = rest[1].trim();
                    Cancelcolors = rest[2].trim();

                    Cancelcurcount = rest[3].trim();
//					lastSuccessBarcode = rest[4].trim();


                    CancelmBillNo = rest[5];


                    nScanCount = rest[6];

                    ShowMessage.ShowMsg(handler, 5, "ok");
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());

                }
            }
        });
        startCode.start();
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
                    MySound.scanSound();

                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    tv_company_name.setText(company_name);
                    tv_retail_store.setText(retail_name);

                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    tv_goodsid.setText("(" + goodsid + ")");
                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 5:
                    MySound.scanSound();
                    tv_model_colors.setText(Cancelmodelm + "-" + Cancelcolors);
                    tv_curqty.setText(Cancelcurcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_billno != null) {
                        tv_billno.setText(CancelmBillNo);
                    }
                    tv_goodsid.setText("(" + Cancelgoodsid + ")");

                    ShowMessage.Show(mContext, "撤销成功");
                    break;

                case 6:
                    //选择品牌后读取当前绑定情况
                    MyProgressDialog.close();

                    if (!IsBindCCS){
                        relayout_ccsstore.setVisibility(View.GONE);
                    }else {
                        relayout_ccsstore.setVisibility(View.VISIBLE);
                        if (IsSendToStore) {
                            //显示分店数据
                            tv_ccsstore_str.setText("CCS分销店：");
                            if (CcsStoreName.equals("")&&BrandingStoreCode.equals("")) {
                                tv_ccsstore_name.setText("");
                            } else {
                                tv_ccsstore_name.setText(CcsStoreName+"("+BrandingStoreCode+")");
                            }
                        } else {
                            tv_ccsstore_str.setText("CCS零售商：");
                            if (CcsCustName.equals("")&&BrandingCustCode.equals("")) {
                                tv_ccsstore_name.setText("");
                            } else {
                                tv_ccsstore_name.setText(CcsCustName+"("+BrandingCustCode+")");
                            }
                        }
                    }
                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[5];
                    mark[0] = "退货单：" + mBillNo;
                    mark[1] = "客户名称：" + tv_retail_store.getText().toString();
                    mark[2] = "客户别名：" + TraderAlias_name;
                    mark[3] = "门店名称：" + tv_company_name.getText().toString();
                    mark[4] = "仓   库 ：" + tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {

                        printbill.prints("         退货单", mark, sacnDataList, sysUserInfo.getUserName());

                    } else {

                        printbill.print(P_Dv_ReturnedPurchase_Z_L_NoBill.this, "         退货单", mark, sacnDataList, sysUserInfo.getUserName());

                    }
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case 10:
                    //刷新
                    int refreshNum=0;
                    for (Map<String, Object> map : sacnDataList)
                    {
                        curcount=map.get("curcount").toString();
                        refreshNum = refreshNum + Integer.valueOf(map.get("curcount").toString());
                    }
                    tv_curqty.setText(curcount);
                    nScanCount=refreshNum+"";
                    tv_totalqty.setText(refreshNum+"");
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    }


    /**
     * 刷新按钮监听类
     */
    private class BtRefreshClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MyProgressDialog.show(mContext, "正在刷新...", true, true);
            Thread sendprint = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), scanBillno);
                        if (sacnDataList.size() == 0) {
                            ShowMessage.ShowMsg(handler, 9, "没有可刷新的数据");
                            return;
                        }

                        ShowMessage.ShowMsg(handler, 10, "刷新");
                    } catch (Exception e) {
                        e.printStackTrace();
                        ShowMessage.ShowMsg(handler, 9, e.getMessage());
                    }

                }
            });
            sendprint.start();
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


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (resultCode == RESULT_OK) {
//            if (data == null) {
//                return;
//            }
//            switch (requestCode) {
//                case Lic_SelectModel:
//                    goodsid = data.getStringExtra("goodsid");
//                    modelm = data.getStringExtra("modelm");
//                    colors = data.getStringExtra("colors");
//                    String productinfo = modelm + "-" + colors;
//                    tv_model_colors.setText(productinfo);
//                    tv_goodsid.setText("(" + goodsid + ")");
//                    break;
//            }
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }

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
    private void access_send(final String contents) {
        if ((!sysUserInfo.getLoginType().equals("CCS")||sysUserInfo.getMainAccount().equals("U_1000820"))&&(Scan_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||Scan_aim.equals("ReturnedPurchase_D_L_NoBill_SameCust"))) {
            if (BrandCode.equals("")) {
                MySound.errorSound();
                ShowMessage.ShowMsg(handler, "请先选择品牌");
                return;
            }
            if (IsBindCCS) {
                if (IsSendToStore) {
                    if (CcsStoreName.equals("")&&BrandingCustCode.equals("")) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "请先绑定CCS客户门店");
                        return;
                    }
                } else {
                    if (CcsCustName.equals("")&&BrandingCustCode.equals("")) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "请先绑定CCS客户");
                        return;
                    }
                }
            }
        }
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

                    //					Barcode：扫描的条码(必传)
                    //	                 GoodsId：退回的产品(传空)
                    //	                 SoCompId：代理商代号(必传)
                    //	                 DeCompId：总公司代号(固定值：00)
                    //	                 OaSuserId：扫描人员代号(必传)
                    //	                 StockId：仓库代号(必传)
                    //	                 ScanSn：扫描序号(必传)
                    //	                 ScanBillNo：扫描单号(必传)
                    //	                 BillNo：退货单号(首次扫码传空，成功再扫码时传返回的退货单号)
                    //	                 SourceBillNo：来源单号(销售退货单号)(传空)


                    Gson gson=new Gson();
                    HashMap<Object, Object> map = new HashMap<Object, Object>();
                    map.put("Barcode", contents);//条码
                    map.put("GoodsId", "");//产品id
                    map.put("Diopter", "");//球镜
                    map.put("Astigmatism", "");//柱镜
                    map.put("SoCompId", retail_id);//来源单位编码
                    map.put("DeCompId", "00");//目标单位编码
                    map.put("OaSuserId", sysUserInfo.getUserid());
                    map.put("StockId", stock_id);//仓库代号
                    map.put("StockSysCode", StockSysCode);//仓库代号
                    map.put("ScanSn", String.valueOf(nSize));//序号
                    map.put("StoreId", company_id);//分店代号
                    map.put("ScanBillNo", scanBillno);//扫描单号
                    map.put("BillNo", mBillNo);//单号
                    map.put("BrandCode", BrandCode);//品牌代号

//                    Log.d("main--", gson.toJson(map));

                    if (Scan_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")){
//                        result = accWeb.P_Dv_Scan("P_Dv_ReturnedPurchase_D_L_NoBill", para.toNoBillJson());
                        result =accWeb.PostAPIStringInterface("AndroidDv/ReturnedPurchase_D_L_NoBill", gson.toJson(map));

                    }else if (Scan_aim.equals("ReturnedPurchase_D_L_NoBill_SameCust")){
                        //镜架跨店退货
                        result =accWeb.PostAPIStringInterface("AndroidDv/ReturnedPurchase_D_L_NoBill_SameCust", gson.toJson(map));
                    }else{
//                        result = accWeb.P_Dv_Scan("P_Dv_ReturnedPurchase_D_L_NoBill_NoCust", para.toNoBillJson());
                        result =accWeb.PostAPIStringInterface("AndroidDv/ReturnedPurchase_D_L_NoBill_NoCust", gson.toJson(map));
                    }
//                    Log.d("main--",result);
                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "返回内容为空，请检查后台返回格式"+result);
                        return;
                    }

                    JSONObject jsonObject=new JSONObject(result);
                    nSize++;


                    goodsid =jsonObject.optString("goodsCode").trim();
                    modelm = jsonObject.optString("modelm").trim();
                    colors = jsonObject.optString("color").trim();


                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = jsonObject.optString("billNo");
                    }

                    if (Integer.parseInt(nScanCount) < Integer.parseInt(jsonObject.optString("billCount").trim())) {
                        curcount = jsonObject.optString("goodsCount").trim();
                        nScanCount = jsonObject.optString("billCount").trim();
                    }

                    if (!Scan_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")){
                        retail_name= jsonObject.optString("storeName");
                        TraderAlias_name=jsonObject.optString("businessName");
                        company_name=jsonObject.optString("customerName");
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {

                case Lic_SelectCCSBrand:
                    //选择品牌后返回
                    BrandCode=data.getStringExtra("BrandCode");
                    BrandName=data.getStringExtra("BrandName");
                    BrandingCode=data.getStringExtra("BrandingCode");
                    AgentCode=data.getStringExtra("AgentCode");
                    tv_brand_name.setText(BrandName);
                    if (Scan_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||Scan_aim.equals("ReturnedPurchase_D_L_NoBill_SameCust")) {

                        if (BrandingCode.equals("")){
//                        relayout_ccscustomer.setVisibility(View.GONE);
                            relayout_ccsstore.setVisibility(View.GONE);
                        }else{
                            GetScsCustStoreRelate(TraderSysId, StoreSysId, BrandingCode, AgentCode);
                        }
                    }
                    break;
                case Lic_SelectCCSStore:
//                    CcsCustName=data.getStringExtra("CCScustName");
//                    CcsStoreName=data.getStringExtra("CCSstoreName");
//                    tv_ccsstore_name.setText(CcsStoreName);
                    GetScsCustStoreRelate(TraderSysId,StoreSysId,BrandingCode,AgentCode);
                    break;
                case Lic_SelectCCSCustomer:
//                    CcsCustName=data.getStringExtra("CCScustName");
//                    tv_ccsstore_name.setText(CcsCustName);
                    GetScsCustStoreRelate(TraderSysId,StoreSysId,BrandingCode,AgentCode);
                    break;
                case Lic_SelectCCSCustomerStore:
//                    CcsCustName=data.getStringExtra("CCScustName");
//                    CcsStoreName=data.getStringExtra("CCSstoreName");
//                    tv_ccsstore_name.setText(CcsStoreName);
                    GetScsCustStoreRelate(TraderSysId,StoreSysId,BrandingCode,AgentCode);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void GetScsCustStoreRelate(final String tCustSysCode, final String tStoreSysCode, final String tBrandCode, final String tAgentCode) {
//        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> data = accWeb.GetScsCustStoreRelate(tCustSysCode,tStoreSysCode,tBrandCode,tAgentCode);
//                    Log.d("main","GetScsCustStoreRelate-"+data.toString());
                    if (data.size()>0) {
                        IsBindCCS = (Boolean) data.get(0).get("NeedBind");//是否需要绑定CCS客户或者门店
                        IsSendToStore = (Boolean) data.get(0).get("IsSendToStore");//true就要同步客户+门店，false同步客户就行
                        //需要绑定到ccs门店
                        if (BrandingCode.equals("01")){
                            SearchBindName = (String) data.get(0).get("TraderAlias");
                        }else {
                            if (IsSendToStore) {
                                SearchBindName = (String) data.get(0).get("StoreAlias");
                            } else {
                                SearchBindName = (String) data.get(0).get("TraderAlias");
                            }
                        }
                        BrandingCustCode= (String) data.get(0).get("BrandingCustCode");////ccs客户代号
                        BrandingStoreCode=(String) data.get(0).get("BrandingStoreCode");//ccs门店代号

                        CcsCustName=(String) data.get(0).get("CcsCustName");//ccs客户名称
                        CcsStoreName=(String) data.get(0).get("CcsStoreName");//ccs门店名称
                    }
                    ShowMessage.ShowMsg(handler, 6, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler,ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
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

                    String tBarcode = et_barcode.getText().toString().trim();
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
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

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null) {
            Configuration configuration = resources.getConfiguration();
            if (configuration != null && configuration.fontScale != 1.0f) {
                configuration.fontScale = 1.0f;//这里只设置字体，故不使用下面注释的方法
//		                configuration.setToDefaults();
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

