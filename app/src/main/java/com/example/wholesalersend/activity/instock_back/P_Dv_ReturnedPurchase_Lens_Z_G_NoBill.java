package com.example.wholesalersend.activity.instock_back;

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
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.activity.select.QueryScanLensDetail;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.activity.select.SelectUpstreamBillno;
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
 * @ClassName: P_Dv_ReturnedPurchase_Lens_Z_G_NoBill
 * @Description: 无单镜片入库退回
 * @Author: lijin
 * @Date: 2024年3月27日14:21:03
 */
public class P_Dv_ReturnedPurchase_Lens_Z_G_NoBill extends Activity {

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
    private String Spherical= "", Cylinder = "",Refractivity="";//球镜柱镜折射率

    private final int Lic_SelectModel = 2;

    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

//	private List<String> codesList= new ArrayList<String>();
//	Thread send ;

    private TextView tv_upstream_billno;//显示上游单号
    private TextView choose_upstream_billno;//选择上游单号

    private String UpstreamBrand="";//上游单号品牌
    private String UpstreamApplyNo="";//上游单号

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_returnedpurchase_lens_z_g_nobill);

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

        tv_upstream_billno=findViewById(R.id.tv_upstream_billno);
        choose_upstream_billno=findViewById(R.id.choose_upstream_billno);
        choose_upstream_billno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, SelectUpstreamBillno.class);
                startActivityForResult(intent, 3);
            }
        });
//        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
//        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【无单镜片入库退回】 "+sysUserInfo.getAccountSetName());

        supplier_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
//        tv_company_name.setText(supplier_name);
//        tv_stock_name.setText(stock_name);

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

                    tv_model_colors.setText(Refractivity+ "  S"+Spherical + " C" + Cylinder);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);
                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
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
                    mark[0] = "退库单：" + mBillNo;
                    //			mark[1] = "供应商："+tv_company_name.getText().toString();
                    //			mark[2] = "仓   库 ："+tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {
                        printbill.prints("         无单入库退回", mark, sacnDataList, sysUserInfo.getUserName());
                    } else {
                        printbill.print(P_Dv_ReturnedPurchase_Lens_Z_G_NoBill.this, "         无单入库退回", mark, sacnDataList, sysUserInfo.getUserName());
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
                case 3:
                    //选择上游单号后返回
                    UpstreamApplyNo=data.getStringExtra("applyNo");
                    UpstreamBrand=data.getStringExtra("brandName");
                    tv_upstream_billno.setText(UpstreamApplyNo);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 请求服务
    private void access_send(final String contents) {
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

                    //					 Barcode：扫描的条码(必传)
                    //	                 GoodsId：入库的产品(不传)
                    //	                 SoCompId：供应商代号(不传)
                    //	                 DeCompId：总公司代号(固定值：00)
                    //	                 OaSuserId：扫描人员代号(必传)
                    //	                 StockId：仓库代号(不传)
                    //	                 ScanSn：扫描序号(必传)
                    //	                 scanBillno：扫描单号(必传)
                    //	                 BillNo：入库单号(首次扫码传空，成功再扫码时传返回的入库单号)
                    //	                 SourceBillNo：来源单号(传空)

                    para.setBarcode(contents);
                    para.setGoodsId("");//>>>goodsid
                    para.setSoCompId("");//>>>supplier_id
                    para.setDeCompId("00");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId("");//>>>stock_id
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo("");
                    para.setDeliveryId(UpstreamApplyNo);//上游单号

                    //镜片无单入库退回：先本系统校验；有上游单号则调上游 return.agent，成功后再落库
                    result =accWeb.PostAPIStringInterface("AndroidDv/ReturnedPurchase_Z_G_Lens_NoBill", para.toJson());
//                        Log.d("main--",result);

                    JSONObject jsonObject = new JSONObject(result);
                    nSize++;
                    goodsid = jsonObject.optString("goodsCode");

                    Refractivity = jsonObject.optString("refractiveIndex");//折射率
                    Spherical = jsonObject.optString("diopter");//球镜
                    Cylinder =jsonObject.optString("astigmatism");//柱镜
                    lastSuccessBarcode = jsonObject.optString("barcode");

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = jsonObject.optString("billNo");
                    }
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

            Intent intent = new Intent(mContext,QueryScanLensDetail.class);
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

