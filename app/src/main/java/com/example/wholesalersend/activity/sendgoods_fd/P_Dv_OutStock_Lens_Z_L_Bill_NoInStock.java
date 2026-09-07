package com.example.wholesalersend.activity.sendgoods_fd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanLensDetail;
import com.example.wholesalersend.activity.select.SelectLensBillProduct;
import com.example.wholesalersend.entity.ApiResponse;
import com.example.wholesalersend.entity.GoodsMarkLabelScan;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.utils.BillProductUtil;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_OutStock_Lens_Z_D_Bill_NoInStock
 * @Description: 分店无单无入库镜片发货
 * @Author: lijin
 * @Date: 2026年5月20日
 */
public class P_Dv_OutStock_Lens_Z_L_Bill_NoInStock extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name, tv_source_billno,
            tv_billno, tv_model_colors, tv_stock_name, tv_title, tv_goodsid;
    private EditText et_barcode;
    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", supplier_id = "", supplier_name = "";
    private String curcount = "0", goodsid = "", stock_id = "",store_id="", stock_name="", sourceBillNo="";
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;

    private BillProductUtil billProductUtil;

    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销发货的参数-次数

    //撤销的型号色号，产品id，单号，当前型号数量，合计
    private String Cancelmodelm = "", Cancelcolors = "", Cancelgoodsid = "", CancelmBillNo = "",
            Cancelcurcount = "";

    private Button btn_p_dv_outstock_z_l_bill_noinstock_revoke;
    private PopupWindow mPopWindow;

    private RadioGroup radioChange;
//    private RadioButton rb_logisticscode,rb_productcode;//物流码还是产品码

//    private Button bth_choosebrand,btn_ChooseCCSData;//选择品牌、选择CCS客户或者门店

    private TextView tv_brand_name;//CCS品牌名称

    private String TraderSysId="",StoreSysId="";//SCS客户系统代号  SCS门店系统代号

    private String CcsCustName="",CcsStoreName="";//当前绑定的CCS客户或者门店

    private String BrandCode="",BrandName="";

    private final int Lic_SelectCCSBrand=10;//选择CCS品牌
    private final int Lic_SelectCCSStore=11;//选择CCS门店
    private final int Lic_SelectCCSCustomer=12;//选择CCS客户

    private String BrandingCode="",AgentCode="",BrandingCustCode="",BrandingStoreCode="";
    private Boolean IsBindCCS=false,IsBindCCScust=false,IsBindCCSstore=false,IsSendToStore=false;

    private TextView tv_lastscannum;

    private String Spherical= "", Cylinder = "",Refractivity="";//球镜柱镜折射率

    private String lsv_aim="";

    private TextView btn_chooserefractive;//选择折射率


    private AlertDialog AddLuminositydialog;//新增光度的弹窗


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_p_dv_outstock_lens_z_l_bill_noinstock);

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
        ((TextView) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

        tv_lastscannum=findViewById(R.id.tv_lastscannum);

        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid))
                .setOnClickListener(new BtnSelectProductClick());


        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_source_billno = (TextView) findViewById(R.id.tv_source_billno);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【镜片有单无入库分店发货】 "+sysUserInfo.getAccountSetName());

        lsv_aim = getIntent().getStringExtra("aim");
        supplier_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
        sourceBillNo = gIntent.getStringExtra("purchecklno");
//        store_id= gIntent.getStringExtra("store_id");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "SZF" + SomeUtils.RandomScanOrder();// 系统
        sysUserInfo.setIsDownload(true);

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);
        tv_source_billno.setText(sourceBillNo);

//        billProductUtil = new BillProductUtil(mContext);
//        billProductUtil.downloadBillProduct(sourceBillNo);

//        rb_logisticscode=findViewById(R.id.radio_logisticscode);
//        rb_productcode=findViewById(R.id.radio_productcode);
//        rb_logisticscode.setChecked(true);
//
//        rb_logisticscode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                rb_logisticscode.setChecked(true);
//                rb_productcode.setChecked(false);
//            }
//        });
//        rb_productcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                rb_productcode.setChecked(true);
//                rb_logisticscode.setChecked(false);
//            }
//        });
//
//        bth_choosebrand=findViewById(R.id.bth_choosebrand);//选择品牌
//        bth_choosebrand.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent =new Intent(mContext, SelectCCSBrand.class);
//                intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_NoInStock");
//                intent.putExtra("trader_sysid", TraderSysId);
//                intent.putExtra("store_sysid", StoreSysId);
//                startActivityForResult(intent, Lic_SelectCCSBrand);
//            }
//        });
//        btn_ChooseCCSData=findViewById(R.id.btn_ChooseCCSData);//选择CCS数据
//        btn_ChooseCCSData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (BrandCode.equals("")){
//                    ShowMessage.ShowMsg(handler, "请先选择品牌");
//                }else{
//                    Intent bindIntent=new Intent();
//                    if (IsSendToStore){
//                        //如果可以选择到门店，直接跳转到选择门店界面
//                        bindIntent=new Intent(mContext, SelectCCSstore.class);
//
//                    }else{
//                        //否则就只能跳转CCS客户界面
//                        bindIntent=new Intent(mContext, SelectCCScustomer.class);
//                    }
//                    bindIntent.putExtra("aim", "");
//                    bindIntent.putExtra("Agent_Code", AgentCode);
//                    bindIntent.putExtra("Brand_code", BrandingCode);
//                    bindIntent.putExtra("CCSCuts_Code", BrandingCustCode);
//                    bindIntent.putExtra("trader_sysid", TraderSysId);
//                    bindIntent.putExtra("store_sysid", StoreSysId);
//
//                    if (IsSendToStore){
//                        //如果可以选择到门店，直接跳转到选择门店界面
//                        startActivityForResult(bindIntent, Lic_SelectCCSStore);
//                    }else{
//                        //否则就只能跳转CCS客户界面
//                        startActivityForResult(bindIntent, Lic_SelectCCSCustomer);
//                    }
//                }
//            }
//        });
        String ChooseBill=getIntent().getStringExtra("scanBillNo");
        String ChooseBillnum=getIntent().getStringExtra("scanBillNum");

        if (ChooseBill!=null&&!ChooseBill.equals("")){
            mBillNo=ChooseBill;
            tv_billno.setText(mBillNo);
        }

        if (ChooseBillnum!=null&&!ChooseBillnum.equals("")){
//            nScanCount=ChooseBillnum;
//            tv_totalqty.setText(nScanCount);
            tv_lastscannum.setText("上次扫描："+ChooseBillnum);
        }

    }

    /**
     * 弹出撤销扫码的输入框
     */
//    private void showPopListView() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View contentView = inflater.inflate(R.layout.select_pop, null);
//        View list = LayoutInflater.from(this).inflate(
//                R.layout.new_p_dv_outstock_z_l_bill_noinstock, null);
//        if (mPopWindow == null) {
//            mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        }
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
                    MyProgressDialog.close();
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MyProgressDialog.close();
                    MySound.scanSound();
//                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    break;
                case ShowMessage.HandScanError:
//                    MySound.errorSound();
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

                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[3];
                    mark[0] = "发货单：" + mBillNo;
                    mark[1] = "分销店：" + tv_company_name.getText().toString();
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

//                    printbill.print(P_Dv_OutStock_Z_L_Bill_NoInStock.this, "    有单无入库直销发货", mark, sacnDataList, sysUserInfo.getUserid());
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
//                    rb_logisticscode.setChecked(true);
//                    rb_productcode.setChecked(false);
//                    tv_model_colors.setText(modelm + "-" + colors);
//                    tv_goodsid.setText("(" + goodsid + ")");
                    ShowMessage.Show(mContext, "已选择商品");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }


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
                            if (billProductUtil.productIsInBillNo(goods.getGoodsCode())) {
                                modelm=goods.getModelm();
                                colors=goods.getColors();
                                goodsid=goods.getGoodsCode();
                                ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess,"ok");
                            }else{
                                ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,goods.getGoodsName()+"产品未存在配货单中");
                            }
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
     * 返回按钮监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //连续按返回按钮两次就退出界面

                if (mPopWindow != null) {
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1f;
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    getWindow().setAttributes(lp);
                    mPopWindow.dismiss();
                    et_barcode.setFocusable(true);
                    et_barcode.setFocusableInTouchMode(true);
                    et_barcode.requestFocus();
                }
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

                    Cylinder=data.getStringExtra("Astigmatism");
                    Spherical=data.getStringExtra("Diopter");
//                    PurQty=(int)Double.parseDouble(data.getStringExtra("PurQty")) ;
                    goodsid = data.getStringExtra("goodsid");
                    Refractivity= data.getStringExtra("refractive");
//                    tv_refractive.setText(Refractivity);
                    tv_goodsid.setText("(" + goodsid + ") "+Refractivity);
                    tv_model_colors.setText("S "+Spherical + " C " + Cylinder);
//                    tv_totalqty.setText(PurQty+"");
//                    tv_curqty.setText(PurQty+"");

                    break;
                case 3:
//                    GoodsSysCode= data.getStringExtra("GoodsSysCode");

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
    private void access_send(final String contents) {

        if (goodsid.equals("")) {
            ShowMessage.ShowMsg(handler, "请先选择镜片产品");
            return;
        }

        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {


                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

                    //					 Barcode：扫描的条码(必传)
                    //	                 GoodsId：退回的产品(必传)
                    //	                 SoCompId：总公司代号(固定值：00)
                    //	                 DeCompId：代理商代号(必传)
                    //	                 OaSuserId：扫描人员代号(必传)
                    //	                 StockId：仓库代号(必传)
                    //	                 ScanSn：扫描序号(必传)
                    //	                 ScanBillNo：扫描单号(必传)
                    //	                 BillNo：发货单号(首次扫码传空，成功再扫码时传返回的发货单号)
                    //	                 SourceBillNo：来源单号(销售配货单号)(必传)

//                    para.setBarcode(contents);
//                    para.setGoodsId(goodsid);
//                    para.setSoCompId("00");
//                    para.setDeCompId(supplier_id);
//                    para.setOaSuserId(sysUserInfo.getUserid());
//                    para.setStockId(stock_id);
//                    para.setScanSn(String.valueOf(nSize));
//                    para.setScanBillNo(scanBillno);
//                    para.setBillNo(mBillNo);
//                    para.setSourceBillNo(sourceBillNo);
//                    para.setStoreId(store_id);

                    Map<String, Object> requestParams=new HashMap<>();
                    requestParams.put("Barcode", contents);//物流码
                    requestParams.put("GoodsId", goodsid);//产品代号
                    requestParams.put("Diopter", Spherical);//球镜
                    requestParams.put("Astigmatism", Cylinder);//柱镜
                    requestParams.put("SoCompId", "00");//来源单位编码
                    requestParams.put("DeCompId", supplier_id);//目标单位编码
                    requestParams.put("OaSuserId", sysUserInfo.getUserid());//操作员代号
                    requestParams.put("StockId", stock_id);//仓库编码
                    requestParams.put("ScanSn", nSize+"");//扫描序号
                    requestParams.put("ScanBillNo", scanBillno);//扫描单号
                    requestParams.put("BillNo", mBillNo);//单据编号
                    requestParams.put("SourceBillNo", sourceBillNo);//来源单号
                    requestParams.put("DocumentNo", "");//单据编号
                    requestParams.put("StoreId", "");//分店代号
                    requestParams.put("FirstDelivery", "");//是否首次
                    requestParams.put("BrandCode", "");//品牌代号
                    requestParams.put("BatchNo", "");//批号

                    Gson gson=new Gson();

//                    Log.d("main", gson.toJson(requestParams));

                    result =accWeb.PostAPIStringInterface("AndroidDv/ShipLensBillNotInStock", gson.toJson(requestParams));
//                    Log.d("main", result);
                    if (result == "") {
//                        MySound.errorSound();
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }


                    JSONObject jsonObject = new JSONObject(result);
                    nSize++;
//                    goodsid = jsonObject.optString("goodsCode");
//                    goodsid = jsonObject.getString("goodsCode");
//                    Refractivity = jsonObject.getString("refractiveIndex");
//                    Spherical =jsonObject.getString("diopter");
//                    Cylinder = jsonObject.getString("astigmatism");

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = jsonObject.getString("billNo");
                    }

                    if (Integer.parseInt(nScanCount) < Integer.parseInt(jsonObject.getString("billCount"))) {
                        curcount = jsonObject.getString("goodsCount");
                        nScanCount=jsonObject.getString("billCount");
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
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
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
                    if (!tBarcode.isEmpty()) {
//                            MySound.scanSound();
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
            Intent intent = new Intent(mContext, QueryScanLensDetail.class);
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
//    private class BtnSelectProductClick implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            sysUserInfo.setIsDownload(true);//每次选择确认是否下载配货单明细数据
//            Intent intent = new Intent(mContext, SelectBillProduct.class);
//            intent.putExtra("orderno", sourceBillNo);
//            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_BeInStock");
//            startActivityForResult(intent, Lic_SelectModel);
//        }
//    }
    /**
     * 选择单据的镜片商品
     */
    private class BtnSelectProductClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sysUserInfo.setIsDownload(true);//每次选择确认是否下载配货单明细数据
            Intent intent = new Intent(mContext, SelectLensBillProduct.class);
            intent.putExtra("orderno", sourceBillNo);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_L_Bill_NoInStock");
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

