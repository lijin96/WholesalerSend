package com.example.wholesalersend.activity.other;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Lens_Z_D_NoBill;
import com.example.wholesalersend.activity.select.QueryScanLensDetail;
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
 * @ClassName: P_Dv_ReturnedPurchase_L_D_Z_Lens
 * @Description: 镜片退货直通车
 * @Author: lijin
 * @Date: 2026/7/17 11:43
 */
public class P_Dv_ReturnedPurchase_L_D_Z_Lens extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name,
            tv_billno, tv_model_colors, tv_stock_name, tv_goodsid;
    private EditText et_barcode;
    private TextView tv_show_code;

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "",company_syscode="", company_id = "", company_name = "",store_name="";//门店名称
    private String curcount = "0", goodsid = "", stock_id = "", stock_name;
    private String lastSuccessBarcode = "", lStar = "";
    //    private String modelm = "", colors = "";
    private String Spherical= "", Cylinder = "",Refractivity="";//球镜柱镜折射率

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数
    private int cSize = 0;//撤销的参数-次数

    private TextView tv_title,tv_lastscannum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_p_dv_returnedpurchase_l_d_z_lens);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【镜片退货直通车】 "+sysUserInfo.getAccountSetName());

        tv_lastscannum=findViewById(R.id.tv_lastscannum);

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


//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

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

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        company_id = gIntent.getStringExtra("company_id");
        company_syscode=gIntent.getStringExtra("company_syscode");

        company_name = gIntent.getStringExtra("company_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "S1T" + SomeUtils.RandomScanOrder();// 系统

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(company_name);
        tv_stock_name.setText(stock_name);

        String ChooseBill=gIntent.getStringExtra("scanBillNo");
        String ChooseBillnum=gIntent.getStringExtra("scanBillNum");

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
                    tv_model_colors.setText(Refractivity+ "  S"+Spherical + " C" + Cylinder);

                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);

//                    tv_company_name.setText(company_name);

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
                    String[] mark = new String[3];
                    mark[0] = "退货单：" + mBillNo;
                    mark[1] = "客户名称：" + tv_company_name.getText().toString();
//                    mark[2] = "门店名称：" + store_name;
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {
                        printbill.prints("       镜片退货直通车", mark, sacnDataList, sysUserInfo.getUserName());
                    } else {
                        printbill.print(P_Dv_ReturnedPurchase_L_D_Z_Lens.this, "镜片退货直通车", mark, sacnDataList, sysUserInfo.getUserName());
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
                    map.put("SoCompId", company_syscode);//来源单位编码
                    map.put("DeCompId", "");//目标单位编码
                    map.put("OaSuserId", sysUserInfo.getUserid());
                    map.put("StockId", stock_id);//仓库代号
                    map.put("ScanSn", String.valueOf(nSize));//序号
                    map.put("ScanBillNo", scanBillno);//扫描单号
                    map.put("BillNo", mBillNo);//单号

                    String tListData = accWeb.PostAPIStringInterface("AndroidDv/P_Dv_ReturnedPurchase_L_D_Z_Lens",gson.toJson(map));
                    JSONObject jsonObject = new JSONObject(tListData);
                    nSize++;
                    // true;产品编号,型号,色号,当前型号数量,当前扫描的条码,退货单号，当前扫码总数量，客户代号，客户名称
                    goodsid =jsonObject.optString("goodsCode");
                    Refractivity =jsonObject.optString("refractiveIndex");
                    Spherical = jsonObject.optString("diopter");
                    Cylinder =jsonObject.optString("astigmatism");

                    lastSuccessBarcode = jsonObject.optString("barcode");

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = jsonObject.optString("billNo");
                    }

                    if (Integer.parseInt(nScanCount) < Integer.parseInt( jsonObject.optString("billCount","0"))) {
                        curcount =jsonObject.optString("goodsCount");
                        nScanCount =  jsonObject.optString("billCount","0");
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
//            Intent intent = new Intent(mContext, SelectProductModelColor.class);
//            startActivityForResult(intent, Lic_SelectModel);
//        }
//    }

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

