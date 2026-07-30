package com.example.wholesalersend.activity.other;

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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanMakeLabelDetail;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.entity.LensPara;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @ClassName: P_Dv_MendLable_Z
 * @Description: 仓库补标
 * @Author: lijin
 * @Date: 2021/3/10 14:01
 */
public class P_Dv_MendLable_Z extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private LensPara para = new LensPara();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private Button btn_batch_num;//点击输入批号
    private LinearLayout linear_batch_num;
    private TextView tv_curqty, tv_totalqty, tv_company_name, tv_source_billno, tv_billno,
            tv_model_colors, tv_stock_name, tv_title, tv_goodsid, tv_text_refractivity;
    private EditText et_barcode;
    private TextView tv_show_code;
    private Button btn_revoke;//撤销按钮
    private List<Map<String, Object>> MendLableNolist = new ArrayList<Map<String, Object>>();//补标单号
    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();//补标扫描

    private String scanBillno = "", mBillNo = "", supplier_id = "", supplier_syscode = "",
            supplier_name = "";
    private String curcount = "0", goodsid = "", goodssyscode = "", goodscategory = "", stock_id
            = "", stock_name = "", sourceBillNo = "", stock_syscode = "";
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "", Astigmatism = "", Diopter = "", refractivity = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

    private int nCancelSize = 0;//次数

    private PopupWindow mPopWindow;
    private AlertDialog AddLuminositydialog;//新增光度的弹窗
    private AlertDialog alertDialog6;//新增光度的弹窗

//
//	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_mendlable_z);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

        //把以前扫描的数据清空
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Button) findViewById(R.id.btn_list)).setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print)).setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit)).setOnClickListener(new BtnExitClick());

        btn_revoke = findViewById(R.id.btn_revoke);//撤销按钮
        btn_revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBillNo.equals("")) {
                    ShowMessage.Show(mContext,"暂无补标单号，请先补标成功后操作");
                } else {
                    ShowRevokeLabel();
                }
            }
        });

        //选择型号色号
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

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText("【仓库补标】 " + sysUserInfo.getAccountSetName());

        sysUserInfo.setSeachValue("");

        tv_text_refractivity = findViewById(R.id.tv_text_refractivity);
        linear_batch_num = findViewById(R.id.linear_batch_num);

        btn_batch_num = findViewById(R.id.btn_batch_num);
        btn_batch_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (goodsid.equals("")) {
                    ShowMessage.ShowMsg(handler, "请先手动选择产品");
                } else if (!goodscategory.equals("镜片")) {
                    ShowMessage.ShowMsg(handler, "镜片产品才能添加球柱镜");
                } else {
//                    showPopBatchView();
                    ShowAddProcureData();
                }
            }
        });

        supplier_syscode = gIntent.getStringExtra("supplier_syscode");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_syscode = gIntent.getStringExtra("stock_syscode");
        stock_name = gIntent.getStringExtra("stock_name");
//        sourceBillNo = gIntent.getStringExtra("purchecklno");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统
        sysUserInfo.setIsDownload(true);


        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);
//        tv_source_billno.setText(sourceBillNo);

//		send = new SendDatas();
//		send.start();
        DownLoadGetMendLableNoThread();
    }

    // 获取补标单号
    private void DownLoadGetMendLableNoThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MendLableNolist = accWeb.GetMendLableNo(supplier_syscode, stock_syscode);
//                    Log.d("main","获取补标单号"+list.toString());
                    ShowMessage.ShowMsg(handler, ShowMessage.HandCloseLoading, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,
                            "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
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

    //修改域名
    private void ShowRevokeLabel() {

        final View selectview =
                LayoutInflater.from(mContext).inflate(R.layout.dialog_revokerepair_layout, null);

//        TextView tv_domainname = selectview.findViewById(R.id.tv_domainname);
        final EditText ed_revoke_code = selectview.findViewById(R.id.ed_revoke_code);
        ed_revoke_code.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        String tBarcode = ed_revoke_code.getText().toString().trim();
                        if (ed_revoke_code.getText().toString().trim().indexOf("=") != -1 || ed_revoke_code.getText().toString().trim().indexOf("http") != -1) {

                            //包含
                            tBarcode = SomeUtils.InterceptCode(mContext,
                                    ed_revoke_code.getText().toString().trim());
                        }

                        if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                            MySound.errorSound();
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                            ed_revoke_code.setText("");
                            return true;
                        }
                        Access_CancelSend(tBarcode);
                        ed_revoke_code.setText("");

                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        alertDialog6 = new AlertDialog.Builder(mContext,
                R.style.Base_Theme_AppCompat_Light_Dialog).setTitle("撤销仓库补标").setIcon(R.drawable.scs).setView(selectview).setCancelable(false) // 不可通过返回键关闭
//                 .setCanceledOnTouchOutside(false) // 不可通过点击空白区域关闭
                .setPositiveButton("确定撤销", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        ///< 隐藏就显示，显示就隐藏 - 这种有时候再逻辑上会给你带来困扰，如果要强制隐藏，建议用别的方式；不要靠什么Boolean状态来做..
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }).create();

        alertDialog6.show();

        alertDialog6.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ed_revoke_code.getText().toString().trim().equals("")) {
                    Toast.makeText(mContext, "请扫描或输入要撤销的条码", Toast.LENGTH_SHORT).show();
                } else {
//                    sysUserInfo.setServerIp(ed_domainname.getText().toString().trim());

                    Access_CancelSend(ed_revoke_code.getText().toString().trim());
                    ed_revoke_code.setText("");

//                    InputMethodManager imm =
//                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    ///< 隐藏就显示，显示就隐藏 - 这种有时候再逻辑上会给你带来困扰，如果要强制隐藏，建议用别的方式；不要靠什么Boolean状态来做..
//                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//
                }
            }

        });

        if (alertDialog6.getWindow() != null) {
            WindowManager.LayoutParams lp = alertDialog6.getWindow().getAttributes();
//            lp.width = 500; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            WindowManager manager = getWindowManager();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
//            Log.d("main","屏幕宽度："+dm.widthPixels);
            lp.width = (int) (dm.widthPixels * 0.5);
            alertDialog6.getWindow().setAttributes(lp);
        }

    }


    /**
     * 处理逻辑的handler
     */
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandCloseLoading: // 获取补标单号接口返回成功
                    MyProgressDialog.close();
                    if (MendLableNolist.size() > 0) {
                        mBillNo = MendLableNolist.get(0).get("MendLableNo").toString();
                        String totalnum = MendLableNolist.get(0).get("MendLableNum").toString();
                        tv_billno.setText(mBillNo);
                        tv_totalqty.setText(totalnum);
                    }

                    break;
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess: // 撤销仓库补标扫描成功
                    MyProgressDialog.close();
                    MySound.scanSound();
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);
//                    if (alertDialog6!=null){
//                        alertDialog6.dismiss();
//                    }
                    break;
                case ShowMessage.HandScanSuccess://仓库补标扫描成功
                    MyProgressDialog.close();
                    MySound.scanSound();

                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);
                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[1];
//				mark[0] = "发货单："+mBillNo;
//				mark[1] = "代理商："+tv_company_name.getText().toString();
                    mark[0] = "仓   库 ：" + tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {

                        printbill.prints("           仓库补标", mark, sacnDataList,sysUserInfo.getUserName());

                    } else {

                        printbill.print(P_Dv_MendLable_Z.this, "           仓库补标", mark, sacnDataList,sysUserInfo.getUserName());

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
                    goodsid = data.getStringExtra("goodsid");
                    goodssyscode = data.getStringExtra("goodssyscode");
                    goodscategory = data.getStringExtra("goodscategory");
                    modelm = data.getStringExtra("modelm");
                    colors = data.getStringExtra("colors");
                    refractivity = data.getStringExtra("refractivity");

                    Diopter = "";
                    Astigmatism = "";

                    String productinfo = modelm + "-" + colors;
                    if (goodscategory.equals("镜片")) {
                        tv_text_refractivity.setText(refractivity);
                        tv_model_colors.setText("请添加球柱镜");
                    } else {
                        refractivity = "";
                        tv_text_refractivity.setText("");
                        tv_model_colors.setText(productinfo);
                    }


                    tv_goodsid.setText("(" + goodsid + ")");

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

        if (goodssyscode.equals("")) {
            MySound.errorSound();
            ShowMessage.ShowMsg(handler, "请先手动选择产品");
            return;
        }

        if (tv_model_colors.getText().toString().trim().equals("请添加球柱镜")) {
            MySound.errorSound();
            ShowMessage.ShowMsg(handler, "镜片仓库补标请先添加球柱镜");
            return;
        }

        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;
                    //            Barcode：扫描的条码(必传)
                    //            GoodsId：新产品编号(必传)
                    //            SoCompId：供应商代号(必传)
                    //            DeCompId：总公司代号(固定值：00)
                    //            OaSuserId：扫描人员代号(必传)
                    //            StockId：仓库代号(必传)
                    //            ScanSn：扫描序号(必传)
                    //            ScanBillNo：扫描单号(必传)
                    //            BillNo：(不传)改为要传
                    //            SourceBillNo：来源单号(不传)

                    para.setBarcode(contents);
                    para.setGoodsId(goodssyscode);
                    para.setSoCompId(supplier_syscode);
                    para.setDiopter(Diopter);
                    para.setAstigmatism(Astigmatism);
                    para.setDeCompId("00");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_syscode);
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo("");

                    result = accWeb.P_Dv_Scan("P_Dv_MendLable", para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
//                    Log.d("main",result);
//                    镜片：
//                    产品代号+折射率+球镜+柱镜+此产品球柱镜扫描数量+条码+单据编号+批次号+单据扫描数量
//                    镜架：
//                    产品代号+空+型号+色号+此产品球柱镜扫描数量+条码+单据编号+批次号+单据扫描数量
                    String[] rest = result.split(",", -1);

                    //			if (rest.length < 6)
                    //			{
                    //				MySound.errorSound();
                    //				ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前"+ rest.length +
                    //				"位！");
                    //				return false;
                    //			}
                    nSize++;
                    refractivity = rest[1].trim();
                    modelm = rest[2].trim();
                    colors = rest[3].trim();
                    curcount = rest[4].trim();
                    lastSuccessBarcode = rest[5].trim();

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = rest[6];
                    }
                    if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[8].trim())) {
                        nScanCount = rest[8].trim();
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    // 撤销补标扫描 请求服务
    private void Access_CancelSend(final String contents) {

        Thread CancelSendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;
                    // |Barcode|物流码|是||
                    //|GoodsId|产品编码|否，传产品系统代号||
                    //|Diopter|球镜|否||
                    //|Astigmatism|柱镜|否||
                    //|SoCompId|来源单位编码|否，传供应商系统代号||
                    //|DeCompId|目的单位编码|否，传账套号||
                    //|OaSuserId|操作员代号|是||
                    //|StockId|仓库编码|否||
                    //|ScanSn|扫描序号|是||
                    //|ScanBillNo|扫描单号|否||
                    //|BillNo|单据编号|是||
                    //|SourceBillNo|来源单号|||
                    //|DocumentNo|单据编号|||
                    //|StoreId|分店代号|||
                    //|FirstDelivery|是否首次|||

                    para.setBarcode(contents);
                    para.setGoodsId("");
                    para.setSoCompId(supplier_syscode);
                    para.setDiopter("");
                    para.setAstigmatism("");
                    para.setDeCompId("");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_syscode);
                    para.setScanSn(String.valueOf(nCancelSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo("");

                    result = accWeb.P_Dv_Scan("P_Dv_MendLable_Cancel", para.toJson());

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
//                    Log.d("main", "撤销扫描：" + result);
//                    镜片：
//                    产品代号+折射率+球镜+柱镜+此产品球柱镜扫描数量+条码+单据编号+批次号+单据扫描数量
//                    镜架：
//                    产品代号+空+型号+色号+此产品球柱镜扫描数量+条码+单据编号+批次号+单据扫描数量
                    String[] rest = result.split(",", -1);

                    nCancelSize++;
//                    refractivity = rest[1].trim();
//                    modelm = rest[2].trim();
//                    colors = rest[3].trim();
                    curcount = rest[4].trim();
//                    lastSuccessBarcode = rest[5].trim();
//
//                    if (mBillNo == null || mBillNo.isEmpty()) {
//                        mBillNo = rest[6];
//                    }
//                    if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[8].trim())) {
                    nScanCount = rest[8].trim();
//                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess, "ok");
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        });
        CancelSendCode.start();
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
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1 || et_barcode.getText().toString().trim().indexOf("http") != -1) {

                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext,
                                et_barcode.getText().toString().trim());
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

    //添加采购的球柱镜
    private void ShowAddProcureData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
                AlertDialog.THEME_HOLO_LIGHT);

        builder.setCancelable(false);
        builder.setTitle("新增球柱镜");
        builder.setPositiveButton("新增扫描", null);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr =
                        (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            }
        });
        AddLuminositydialog = builder.create();
        View dialogView = View.inflate(mContext, R.layout.addprocure_dialog, null);
        //设置对话框布局
        AddLuminositydialog.setView(dialogView);
        final EditText ed_sphericalmirror = dialogView.findViewById(R.id.ed_sphericalmirror);//球镜
        final EditText ed_cylinder = dialogView.findViewById(R.id.ed_cylinder);//柱镜
//        final EditText ed_addprocure_num = (EditText) dialogView.findViewById(R.id
//        .ed_addprocure_num);//采购订单数
        ed_sphericalmirror.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ed_cylinder.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
//        ed_addprocure_num.setText("1");


        ed_sphericalmirror.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //焦点消失的时候
                    if (!ed_sphericalmirror.getText().toString().trim().equals("")) {
                        String sphdegrees =
                                toDecimal(ed_sphericalmirror.getText().toString().trim());
                        ed_sphericalmirror.setText(sphdegrees);
                    }
                }
            }
        });

        ed_cylinder.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //焦点消失的时候
                    if (!ed_cylinder.getText().toString().trim().equals("")) {
                        String cyldegrees = toDecimal(ed_cylinder.getText().toString().trim());
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
//                ed_addprocure_num.setSelection(ed_addprocure_num.getText().toString().trim()
//                .length());

//                String pattern = "^\\d+(\\.00|\\.25|\\.50|\\.75)$";
//                String pattern =  "^-?\\d+(\\\\.00|\\\\.25|\\\\.50|\\\\.75)$";
                String pattern = "^-?\\d+(\\.00|\\.25|\\.50|\\.75)?$";

                String spherical_mirror = ed_sphericalmirror.getText().toString().trim();
                String cylinder = ed_cylinder.getText().toString().trim();
//                String addProcure_num=ed_addprocure_num.getText().toString().trim();
                if (spherical_mirror.equals("")) {
                    ShowMessage.Show(mContext, "请输入球镜的度数");
                } else if (cylinder.equals("")) {
                    ShowMessage.Show(mContext, "请输入柱镜的度数");
                } else if (!Pattern.matches(pattern, spherical_mirror)) {
                    ShowMessage.Show(mContext, "请输入正确的球镜度数");
                } else if (!Pattern.matches(pattern, cylinder)) {
                    ShowMessage.Show(mContext, "请输入正确的柱镜度数");
                } else {
//                    dialogType="新增";
//                    UploadDataThread(spherical_mirror,cylinder,Integer.parseInt(addProcure_num));
//                    if (AddLuminositydialog!=null){
//                     AddLuminositydialog.dismiss();
//                    }

                    if (Double.parseDouble(spherical_mirror) == 0) {
                        spherical_mirror = "0.00";
                    }
                    if (Double.parseDouble(cylinder) == 0) {
                        cylinder = "0.00";
                    }

                    Diopter = spherical_mirror;
                    Astigmatism = cylinder;
                    tv_model_colors.setText("S " + Diopter + " C " + Astigmatism);
                    if (AddLuminositydialog != null) {
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
     * 明细按钮监听类
     */
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mContext, QueryScanMakeLabelDetail.class);
            intent.putExtra("mBillNo", mBillNo);
            startActivity(intent);
//            Intent intent = new Intent(mContext,
//                    QueryScanDetail.class);
//            Intent intent = null;
//            if (sysUserInfo.getEnterpriseId().equals("00")||sysUserInfo.getEnterpriseId()
//            .equals("08")){
//                intent = new Intent(mContext, QueryScanBatchDetail.class);
//            }else{
//                intent = new Intent(mContext,QueryScanDetail.class);
//            }

//            intent.putExtra("mBillNo", scanBillno);
//            startActivity(intent);
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
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(),
                                scanBillno);
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
            intent.putExtra("aim", "P_Dv_MendLable_Z");
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

