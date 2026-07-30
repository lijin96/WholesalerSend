package com.example.wholesalersend.activity.sendgoods_fd;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.example.wholesalersend.entity.ApiResponse;
import com.example.wholesalersend.entity.GoodsMarkLabelScan;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.utils.DisplayUtil;
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
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_OutStock_Z_L_NoBill_NoInStock
 * @Description: 无单无入库分店发货
 * @Author: lijin
 * @Date: 2021/3/10 14:03
 */
public class P_Dv_OutStock_Z_L_NoBill_NoInStock extends Activity {

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

    private String scanBillno = "", mBillNo = "", company_id = "", company_name = "",TraderAlias_name="";;
    private String curcount = "0", goodsid = "", stock_id = "", retail_id="",stock_name;
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销发货的参数-次数

    //撤销的型号色号，产品id，单号，当前型号数量，合计
    private String Cancelmodelm = "", Cancelcolors = "", Cancelgoodsid = "", CancelmBillNo = "",
            Cancelcurcount = "";

    //	private List<String> codesList= new ArrayList<String>();
    //
    //	Thread send ;

    private Button btn_p_dv_outstock_z_l_nobill_noinstock_revoke;
    private PopupWindow mPopWindow;


    private RelativeLayout relayout_ccsstore;//ccs门店数据展示
    private TextView tv_ccsstore_name;//ccs门店名称展示
    private TextView tv_ccsstore_str;//CCS门店名称提示

    private TextView tv_brand_name;//CCS品牌名称

    private String TraderSysId="",StoreSysId="";//SCS客户系统代号  SCS门店系统代号

    private String CcsCustName="",CcsStoreName="";//当前绑定的CCS客户或者门店

    private TextView bth_choosebrand;//选择品牌
    private TextView btn_ChooseCCSData;//选择CCS门店

    private String BrandCode="",BrandName="";

    private final int Lic_SelectCCSBrand=10;//选择CCS品牌
    private final int Lic_SelectCCSStore=11;//选择CCS门店
    private final int Lic_SelectCCSCustomer=12;//选择CCS客户
    private final int Lic_SelectCCSCustomerStore=13;//先选择CCS客户在选择门店绑定，没有同步功能


    private String BrandingCode="",AgentCode="",BrandingCustCode="",BrandingStoreCode="";
    private Boolean IsBindCCS=false,IsBindCCScust=false,IsBindCCSstore=false,IsSendToStore=false;

    private TextView tv_lastscannum;//上一次扫描数量
    private Button btn_refresh;//刷新扫描数量

    private RadioGroup radioChange;
    private RadioButton rb_logisticscode,rb_productcode;//物流码还是产品码

    private String SearchBindName="";//TraderAlias是客户别名，Alias是门店别名，如果是要绑到店的话，就用Alias，不到店用TraderAlias；brandingcode是01的时候，带联系人过去

    private String LinkName="";//联系人名称

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_p_dv_outstock_z_l_nobill_noinstock);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

//        //把以前扫描的数据清空
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
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【无单无入库分店发货】 "+sysUserInfo.getAccountSetName());

        company_id = gIntent.getStringExtra("company_id");
        company_name = gIntent.getStringExtra("company_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
        retail_id= gIntent.getStringExtra("retail_id");

        TraderAlias_name= gIntent.getStringExtra("alias_name");
//        Log.d("main", TraderAlias_name);

        TraderSysId=gIntent.getStringExtra("trader_sysid");
        StoreSysId=gIntent.getStringExtra("store_sysid");

        if (gIntent.getStringExtra("link")!=null) {
            LinkName=gIntent.getStringExtra("link");
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(company_name);
        tv_stock_name.setText(stock_name);

        relayout_ccsstore=findViewById(R.id.relayout_ccsstore);
        tv_ccsstore_name=findViewById(R.id.tv_ccsstore_name);
        tv_ccsstore_str=findViewById(R.id.tv_ccsstore_str);
        tv_brand_name=findViewById(R.id.tv_brand_name);

        bth_choosebrand=findViewById(R.id.bth_choosebrand);//选择品牌
        bth_choosebrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(mContext, SelectCCSBrand.class);
                intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_BeInStock");
                intent.putExtra("trader_sysid", TraderSysId);
                intent.putExtra("store_sysid", StoreSysId);
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

        btn_p_dv_outstock_z_l_nobill_noinstock_revoke = (Button) findViewById(R.id.btn_p_dv_outstock_z_l_nobill_noinstock_revoke);
        btn_p_dv_outstock_z_l_nobill_noinstock_revoke.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

//                showPopListView();

            }
        });


        rb_logisticscode=findViewById(R.id.radio_logisticscode);
        rb_productcode=findViewById(R.id.radio_productcode);
        rb_logisticscode.setChecked(true);

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

        tv_lastscannum=findViewById(R.id.tv_lastscannum);
        String ChooseBillnum=gIntent.getStringExtra("scanBillNum");
        String ChooseBill=gIntent.getStringExtra("scanBillNo");

        btn_refresh=findViewById(R.id.btn_refresh);//刷新
        btn_refresh.setOnClickListener(new BtRefreshClick());


        if (ChooseBillnum!=null&&!ChooseBillnum.equals("")){
            tv_lastscannum.setText("上次扫描："+ChooseBillnum);
        }

        if (ChooseBill!=null&&!ChooseBill.equals("")){
            mBillNo=ChooseBill;
            tv_billno.setText(mBillNo);
        }

    }

    /**
     * 弹出撤销扫码的输入框
     */
//    private void showPopListView() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View contentView = inflater.inflate(R.layout.select_pop, null);
//        View list = LayoutInflater.from(this).inflate(
//                R.layout.new_p_dv_outstock_z_l_nobill_noinstock, null);
//        if (mPopWindow == null) {
//            mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        }
//
//        mPopWindow.setFocusable(true);
//        mPopWindow.setOutsideTouchable(false);
//
//        mPopWindow.getContentView().setFocusable(true); // 这个很重要
//        mPopWindow.getContentView().setFocusableInTouchMode(true);
//
//
//        ColorDrawable dw = new ColorDrawable(0x00000000);
//        //设置SelectPicPopupWindow弹出窗体的背景
//        mPopWindow.setBackgroundDrawable(dw);
//
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
//        //		mPopWindow.update();
//
//
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
//                        tv_show_code.setText(ed_code.getText().toString().trim());
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

                    //							Barcode：扫描的条码(必传)
                    //			                 GoodsId：产品编号(传空)
                    //			                 SoCompId：总公司代号(固定值：00)(传空)
                    //			                 DeCompId：代理商代号(传空)
                    //			                 OaSuserId：扫描人员代号(必传)
                    //			                 StockId：仓库代号(传空)
                    //			                 ScanSn：扫描序号(必传)
                    //			                 ScanBillNo：扫描单号(必传)
                    //			                 BillNo：发货单号(传空)
                    //			                 SourceBillNo：来源单号(传空)
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
                    //							lastSuccessBarcode = rest[4].trim();

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

        sysUserInfo.SaveConfigString("searchProductSql", "");
        //		send.interrupt();
        //		try {
        //			send.join();
        //		} catch (InterruptedException e) {
        //			e.printStackTrace();
        //		}
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
                    break;
                case ShowMessage.HandScanError:
                    MyProgressDialog.close();

                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 5:
                    MyProgressDialog.close();

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

                    if (!IsBindCCS&&!IsBindCCScust&&!IsSendToStore){
                        relayout_ccsstore.setVisibility(View.GONE);
                    }else {
                        relayout_ccsstore.setVisibility(View.VISIBLE);
                        if (IsSendToStore) {
                            tv_ccsstore_str.setText("CCS分销店：");
                            if (CcsCustName.equals("")) {
                                tv_ccsstore_name.setText("");
                            } else {
                                if (CcsStoreName.equals("")) {
                                    //如果未绑定分销店就不显示分销店名称
                                    tv_ccsstore_name.setText("");
                                } else {
                                    tv_ccsstore_name.setText(CcsStoreName);
                                }
                            }
                        } else {
                            tv_ccsstore_str.setText("CCS零售商：");
                            if (CcsCustName.equals("")) {
                                tv_ccsstore_name.setText("");
                            } else {
                                tv_ccsstore_name.setText(CcsCustName);
                            }
                        }
                    }
                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[3];
                    mark[0] = "发货单：" + mBillNo;
                    mark[1] = "分销店：" + tv_company_name.getText().toString();
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {

                        printbill.prints("    无单无入库直销发货", mark, sacnDataList, sysUserInfo.getUserName());

                    } else {

                        printbill.print(P_Dv_OutStock_Z_L_NoBill_NoInStock.this, "    无单无入库直销发货", mark, sacnDataList, sysUserInfo.getUserName());

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
                    tv_model_colors.setText(productinfo);
                    tv_goodsid.setText("(" + goodsid + ")");
                    break;
                case Lic_SelectCCSBrand:
                    //选择品牌后返回
                    BrandCode=data.getStringExtra("BrandCode");
                    BrandName=data.getStringExtra("BrandName");
                    BrandingCode=data.getStringExtra("BrandingCode");
                    tv_brand_name.setText(BrandName);

                    GetScsCustStoreRelate(TraderSysId,StoreSysId,BrandCode,"");
                    break;
                case Lic_SelectCCSStore:
                    CcsCustName=data.getStringExtra("CCScustName");
                    CcsStoreName=data.getStringExtra("CCSstoreName");
                    tv_ccsstore_name.setText(CcsStoreName);
                    break;
                case Lic_SelectCCSCustomer:
                    CcsCustName=data.getStringExtra("CCScustName");
                    tv_ccsstore_name.setText(CcsCustName);
                    break;
                case Lic_SelectCCSCustomerStore:
                    CcsCustName=data.getStringExtra("CCScustName");
                    CcsStoreName=data.getStringExtra("CCSstoreName");
                    tv_ccsstore_name.setText(CcsStoreName);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void GetScsCustStoreRelate(final String tCustSysCode, final String tStoreSysCode, final String tBrandCode, final String tBillNo) {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> data = accWeb.GetScsCustStoreRelate(tCustSysCode,tStoreSysCode,tBrandCode,tBillNo);

//                    Log.d("main","GetScsCustStoreRelate-"+data.toString());
                    if (data.size()>0) {
                        IsBindCCS = (Boolean) data.get(0).get("NeedBind");
                        IsBindCCScust = (Boolean) data.get(0).get("CustBind");
                        IsBindCCSstore = (Boolean) data.get(0).get("StoreBind");
                        IsSendToStore = (Boolean) data.get(0).get("IsSendToStore");

                        if (BrandingCode.equals("01")){
                            SearchBindName = (String) data.get(0).get("TraderAlias");
                        }else {
                            if (IsSendToStore) {
                                SearchBindName = (String) data.get(0).get("StoreAlias");
                            } else {
                                SearchBindName = (String) data.get(0).get("TraderAlias");
                            }
                        }

//                        BrandingCode= (String) data.get(0).get("BrandingCode");

                        AgentCode= (String) data.get(0).get("AgentCode");
                        BrandingCustCode= (String) data.get(0).get("BrandingCustCode");
                        BrandingStoreCode=(String) data.get(0).get("BrandingStoreCode");

                        TraderSysId=(String) data.get(0).get("CustSysCode");
                        StoreSysId=(String) data.get(0).get("StoreSysCode");

                        CcsCustName=(String) data.get(0).get("CcsCustName");
                        CcsStoreName=(String) data.get(0).get("CcsStoreName");

                    }
//                    Log.d("main","IsBindCCS-"+IsBindCCS);
//                    Log.d("main","IsBindCCScust-"+IsBindCCScust);
//                    Log.d("main","IsBindCCSstore-"+IsBindCCSstore);

                    ShowMessage.ShowMsg(handler, 6, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler,ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
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
    private void access_send(final String contents) {

        if (!sysUserInfo.getLoginType().equals("CCS")&&!sysUserInfo.getMainAccount().equals("U_1000820")){
            if (BrandCode.equals("")){
                ShowMessage.ShowMsg(handler, "请先选择品牌");
                return;
            }

            if (IsBindCCS&&(!IsBindCCScust||!IsBindCCSstore)){
                if (IsSendToStore){
                    if (CcsStoreName.equals("")){
                        ShowMessage.ShowMsg(handler, "请先绑定CCS客户门店");
                        return;
                    }
                }else{
                    if (CcsCustName.equals("")){
                        ShowMessage.ShowMsg(handler, "请先绑定CCS客户");
                        return;
                    }
                }
            }
        }


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
                    para.setSoCompId("00");
                    para.setDeCompId(retail_id);
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_id);
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo("");
                    para.setStoreId(company_id);


                    result = accWeb.P_Dv_Scan("P_Dv_OutStock_D_F_NoBill_NoInStock", para.toJson());
                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
                    nSize++;
                    if (contents.startsWith("P")) {
                        JSONArray listjson = new JSONArray(result);
                        //                "GoodsId": "C00001",
                        //                "Modelm": "1357",
                        //                "Colors": "C01",
                        //                "CurNum": "80",
                        //                "PackNumber": "P200917000001",
                        //                "TranLno" :"DX-00-200000001"

                        for (int i = 0; i < listjson.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) listjson.opt(i);
                            JSONObject jsonObject2 = (JSONObject) listjson.opt(0);
                            modelm = jsonObject2.getString("Modelm");
                            colors = jsonObject2.getString("Colors");
                            curcount = jsonObject2.getString("CurNum");
                            //					lastSuccessBarcode = rest[4].trim();
                            if (mBillNo == null || mBillNo.isEmpty()) {
                                mBillNo = jsonObject2.getString("TranLno");
                            }
                            if (listjson.length() == 1) {
                                nScanCount = jsonObject1.getString("CurNum");
                            } else {
                                nScanCount = String.valueOf(Integer.parseInt(curcount) + Integer.parseInt(jsonObject1.getString("CurNum")));
                            }
                        }
                    } else {
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号
                        String[] rest = result.split(",");
                        if (rest.length < 6) {
                            MySound.errorSound();
                            ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                            return;
                        }
                        //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号
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
                            ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,"镜架发货只能选择非镜片产品");
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

//                    String firstCode=tBarcode.substring(0, 2);
//                    if (firstCode.equals("28")&&tBarcode.length()==15){
                    if (rb_productcode.isChecked()==true){
                        //商品码 可查询型号色号
                        DownLoadDataThread(tBarcode);
                        et_barcode.setText("");
                    }else {
                        tv_show_code.setText(tBarcode);
                        if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                            MySound.errorSound();
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                            et_barcode.setText("");
                            return true;
                        }
                        if (!tBarcode.isEmpty()) {
                            access_send(tBarcode);
                            et_barcode.setText("");
                        }
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
            intent.putExtra("aim", "P_Dv_OutStock_D_F_NoBill_NoInStock");
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

