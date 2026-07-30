package com.example.wholesalersend.activity.sendgoods_zd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.CheckLensDegree;
import com.example.wholesalersend.activity.select.QueryScanLensDetail;
import com.example.wholesalersend.activity.select.SelectLensProduct;
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
import java.util.regex.Pattern;

/**
 * @ClassName: P_Dv_OutStock_Lens_Z_D_Bill_BeInStock
 * @Description: 代销镜片无单无入库发货
 * @Author: lijin
 * @Date: 2024年3月27日14:56:26
 */
public class P_Dv_OutStock_Lens_Z_D_NoBill_NoInStock extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;


    private PopupWindow mPopWindow;


    private TextView tv_curqty, tv_totalqty, tv_company_name, tv_source_billno,
            tv_billno, tv_model_colors, tv_stock_name, tv_goodsid;
    private EditText et_barcode;

    private TextView tv_show_code;

//    private Spinner mSpinner;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", company_id = "", supplier_name = "";
    private String curcount = "0", goodsid = "", stock_id = "", stock_name, sourceBillNo,GoodsSysCode="";
    private String lastSuccessBarcode = "", lStar = "";
//    private String modelm = "", colors = "";

    private String Spherical= "", Cylinder = "",Refractivity="";//球镜柱镜折射率

    private String Delivery_type = "";//发货类型

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销发货的参数-次数

    private TextView tv_title;

    private Button btn_select_goodsid;

    private String lsv_aim="";

    private AlertDialog AddLuminositydialog;//新增光度的弹窗

    private TextView tv_lastscannum;//上一次扫描

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_p_dv_outstock_lens_z_d_nobill_noinstock);

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

//        mSpinner = (Spinner) findViewById(R.id.spinner_type);

        ((TextView) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

        findViewById(R.id.btn_chooserefractive).setOnClickListener(new BtnChooseRefractive());
        findViewById(R.id.btn_AddLuminosity).setOnClickListener(new BtnAddLuminosityData());

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【镜片无单无入库代销发货】 "+sysUserInfo.getAccountSetName());

        tv_lastscannum=findViewById(R.id.tv_lastscannum);

//        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid)).setOnClickListener(new BtnSelectProductClick());

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

        company_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
//        sourceBillNo = gIntent.getStringExtra("purchecklno");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "SZF" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);


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
//        tv_source_billno.setText(sourceBillNo);

//        billProductUtil = new BillProductUtil(mContext);
//        billProductUtil.downloadBillProduct(sourceBillNo);


//		if (sysUserInfo.getEnterpriseId().equals("19")) {
//			mSpinner.setVisibility(View.VISIBLE);
//		}
//        String[] arr = {"选择类型", "首发", "非首发"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.myspinner, arr);
//        mSpinner.setAdapter(adapter);
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                //选择列表项的操作
//                String type = (String) mSpinner.getItemAtPosition(position);//从spinner中获取被选择的数据
//                if (type.equals("选择类型")) {
//                    Delivery_type = "";
//                } else {
//                    Delivery_type = type;
//                }
//
//                TextView tv = (TextView) view;
//
//                tv.setTextSize(14.0f);    //设置大小
//
//                tv.setGravity(Gravity.CENTER_HORIZONTAL);   //设置居中
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                //未选中时候的操作
//            }
//        });

//		send = new SendDatas();
//		send.start();

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
//                    tv_model_colors.setText(modelm + "-" + colors);

//                    tv_model_colors.setText(Refractivity+ "  S"+Spherical + " C" + Cylinder);

                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
//                    tv_goodsid.setText("(" + goodsid + ")");
//                    tv_stock_name.setText(stock_name);

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;


                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[4];
                    mark[0] = "发货单：" + mBillNo;
                    mark[1] = "客户名称：" + tv_company_name.getText().toString();
//                    mark[2] = "客户别名：" + tv_company_name.getText().toString();
                    mark[3] = "仓   库 ：" + tv_stock_name.getText().toString();

//                    printbill.print(P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock.this, "    有单有入库总店发货", mark, sacnDataList, sysUserInfo.getUserid());
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
     * 弹出撤销扫码的输入框
     */
//    private void showPopListView() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View contentView = inflater.inflate(R.layout.select_pop, null);
//        View list = LayoutInflater.from(this).inflate(
//                R.layout.new_p_dv_outstock_z_d_bill_beinstock, null);
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

                    Cylinder=data.getStringExtra("Astigmatism");
                    Spherical=data.getStringExtra("Diopter");

                    tv_model_colors.setText("S "+Spherical + " C " + Cylinder);
                    break;
                case 3:
                    GoodsSysCode= data.getStringExtra("GoodsSysCode");
                    goodsid = data.getStringExtra("Product_id");
                    Refractivity= data.getStringExtra("refractive");
                    tv_goodsid.setText("(" + goodsid + ") "+Refractivity);
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
//                    para.setGoodsId("");
//                    para.setSoCompId("00");
//                    para.setDeCompId(company_id);
//                    para.setOaSuserId(sysUserInfo.getUserid());
//                    para.setStockId(stock_id);
//                    para.setScanSn(String.valueOf(nSize));
//                    para.setScanBillNo(scanBillno);
//                    para.setBillNo(mBillNo);

                    Map<String, Object> requestParams=new HashMap<>();
                    requestParams.put("Barcode", contents);//物流码
                    requestParams.put("GoodsId", goodsid);//产品代号
                    requestParams.put("Diopter", Spherical);//球镜
                    requestParams.put("Astigmatism", Cylinder);//柱镜
                    requestParams.put("SoCompId", "00");//来源单位编码
                    requestParams.put("DeCompId", company_id);//目标单位编码
                    requestParams.put("OaSuserId", sysUserInfo.getUserid());//操作员代号
                    requestParams.put("StockId", stock_id);//仓库编码
                    requestParams.put("ScanSn", nSize+"");//扫描序号
                    requestParams.put("ScanBillNo", scanBillno);//扫描单号
                    requestParams.put("BillNo", mBillNo);//单据编号
                    requestParams.put("SourceBillNo", "");//来源单号
                    requestParams.put("DocumentNo", "");//单据编号
                    requestParams.put("StoreId", "");//分店代号
                    requestParams.put("FirstDelivery", "");//是否首次
                    requestParams.put("BrandCode", "");//品牌代号
                    requestParams.put("BatchNo", "");//批号

                    Gson gson=new Gson();

//                    Log.d("main", gson.toJson(requestParams));

                    result =accWeb.PostAPIStringInterface("AndroidDv/ShipLensNoBillNotInStock", gson.toJson(requestParams));

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

    /** Storage
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

//                    if (billProductUtil.productIsInBillNo(tBarcode)) {
//                        goodsid = tBarcode;
//                        modelm = billProductUtil.getModelColors(goodsid)[0];
//                        colors = billProductUtil.getModelColors(goodsid)[1];
//                        String productinfo = modelm + "-" + colors;
//                        tv_model_colors.setText(productinfo);
//                        tv_goodsid.setText("(" + goodsid + ")");
//                        et_barcode.setText("");
//                        return true;
//                    }

                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode+ "】");
                        et_barcode.setText("");
                        return true;
                    }
//					if (Delivery_type.equals("")) {
//						MySound.errorSound();
//						ShowMessage.Show(mContext, "请选择类型");
//						et_barcode.setText("");
//						return true;
//					}
//
//                    MySound.scanSound();
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
     * 选择球柱镜
     */
    private class BtnSelectProductClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(mContext, SelectBillProduct.class);
//            intent.putExtra("orderno", sourceBillNo);
//            intent.putExtra("aim", "P_Dv_InStock_Bill");
//            startActivityForResult(intent, Lic_SelectModel);
            if (goodsid.equals("")){
                ShowMessage.Show(mContext,"请先选择镜片产品");
            }else {
                Intent intent = new Intent(mContext, CheckLensDegree.class);
//            intent.putExtra("supplier_name", supplier_name);
//            intent.putExtra("stock_name", stock_name);
//            intent.putExtra("supplier_id", supplier_id);
//            intent.putExtra("stock_id", stock_id);
                intent.putExtra("goodssyscode", GoodsSysCode);
                intent.putExtra("Product_id", goodsid);
//            intent.putExtra("GoodsCode", (String) item.get("Product_id"));
                intent.putExtra("scanBillno", scanBillno);
                intent.putExtra("purchecklno", mBillNo);
                intent.putExtra("refractive", Refractivity);

                intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_NoBill_NoInStock");
                startActivityForResult(intent, Lic_SelectModel);
            }
        }
    }
    /**
     * 选择折射率
     */
    private class BtnChooseRefractive implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            intent.setClass(mContext, SelectLensProduct.class);
            intent.putExtra("aim", lsv_aim);
            startActivityForResult(intent, 3);
        }
    }


    /**
     * 新增要采购的光度和数量
     */
    private class BtnAddLuminosityData implements View.OnClickListener {
        @Override
        public void onClick(View v)   {
            if (goodsid.equals("")){
                ShowMessage.Show(mContext,"请先选择镜片产品");
            }else{
                ShowAddProcureData();
            }
        }
    }

    //添加采购的球柱镜
    private void ShowAddProcureData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);

        builder.setCancelable(false);
        builder.setTitle("新增球柱镜");
        builder.setPositiveButton("新增扫描", null);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            }
        });
        AddLuminositydialog = builder.create();
        View dialogView = View.inflate(mContext, R.layout.addprocure_dialog, null);
        //设置对话框布局
        AddLuminositydialog.setView(dialogView);
        final EditText ed_sphericalmirror=dialogView.findViewById(R.id.ed_sphericalmirror);//球镜
        final EditText ed_cylinder=dialogView.findViewById(R.id.ed_cylinder);//柱镜
//        final EditText ed_addprocure_num = (EditText) dialogView.findViewById(R.id.ed_addprocure_num);//采购订单数
        ed_sphericalmirror.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ed_cylinder.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
//        ed_addprocure_num.setText("1");


        ed_sphericalmirror.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    //焦点消失的时候
                    if (!ed_sphericalmirror.getText().toString().trim().equals("")){
                        String sphdegrees=toDecimal(ed_sphericalmirror.getText().toString().trim());
                        ed_sphericalmirror.setText(sphdegrees);
                    }
                }
            }
        });

        ed_cylinder.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    //焦点消失的时候
                    if (!ed_cylinder.getText().toString().trim().equals("")){
                        String cyldegrees=toDecimal(ed_cylinder.getText().toString().trim());
                        ed_cylinder.setText(cyldegrees);
                    }
                }
            }
        });

        AddLuminositydialog.show();
        AddLuminositydialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ed_sphericalmirror.requestFocus();
                ed_cylinder.requestFocus();
//                ed_addprocure_num.requestFocus();
//                ed_addprocure_num.setSelection(ed_addprocure_num.getText().toString().trim().length());

//                String pattern = "^\\d+(\\.00|\\.25|\\.50|\\.75)$";
//                String pattern =  "^-?\\d+(\\\\.00|\\\\.25|\\\\.50|\\\\.75)$";
                String pattern = "^-?\\d+(\\.00|\\.25|\\.50|\\.75)?$";

                String spherical_mirror=ed_sphericalmirror.getText().toString().trim();
                String cylinder=ed_cylinder.getText().toString().trim();
//                String addProcure_num=ed_addprocure_num.getText().toString().trim();
                if (spherical_mirror.equals("")){
                    ShowMessage.Show(mContext,"请输入球镜的度数");
                }else if (cylinder.equals("")){
                    ShowMessage.Show(mContext,"请输入柱镜的度数");
                }else if (!Pattern.matches(pattern, spherical_mirror)){
                    ShowMessage.Show(mContext,"请输入正确的球镜度数");
                }else if(!Pattern.matches(pattern, cylinder)){
                    ShowMessage.Show(mContext,"请输入正确的柱镜度数");
                }else {
//                    dialogType="新增";
//                    UploadDataThread(spherical_mirror,cylinder,Integer.parseInt(addProcure_num));
//                    if (AddLuminositydialog!=null){
//                     AddLuminositydialog.dismiss();
//                    }
                    if (Double.parseDouble(spherical_mirror)==0) {
                        spherical_mirror="0.00";
                    }
                    if (Double.parseDouble(cylinder)==0) {
                        cylinder="0.00";
                    }

                    Spherical = spherical_mirror;
                    Cylinder = cylinder;
                    tv_model_colors.setText("S "+Spherical + " C " + Cylinder);
                    if (AddLuminositydialog!=null){
                        AddLuminositydialog.dismiss();
                    }
                }
            }
        });
    }
    public static String toDecimal(String v) {
        Float f = Float.valueOf(v);
//        @SuppressLint("DefaultLocale")
        String format = String.format("%.2f", f);
        return format;
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

