package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Lens_Z_D_NoBill;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Z_D_NoBill;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_NoBill_BeInStock;
import com.example.wholesalersend.adapter.ScanOrderAdapter;
import com.example.wholesalersend.entity.ScanOrder;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCompanyRetailer
 * @Description: 选择零售商
 * @Author: lijin
 * @Date: 2021/3/10 9:56
 */
public class SelectCompanyRetailer extends Activity {


    private Handler hand;
    private AccessWeb accWeb;
    private Context mContext;
    private SimpleAdapter adapter;
    private SysUserInfo sysUserInfo;
    private LayoutInflater inflater;
    private SimpleAdapter spinnerProviceAdapter, spinnerCityAdapter;


    private View alertView;//AlertDialog的布局view
    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView company_retailer_help;//零售商帮助
    private Spinner spinnerProvince, spinnerCity;
    private Button btn_search;

    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();

    private Map<String, Object> item;

    private String lsv_etStr, provicename = "", cityname = "", sql = "";
    private String lsv_aim = "";

    private int provicePosition = 0, cityPosition = 0, lastProvicePosition = 0;//选择的省，市。上次选择的省
    private final int Lic_SelectSure = 3;

    int Page=1;
    List<ScanOrder> scanOrderlist=new ArrayList<ScanOrder>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.select_company_retailer);
        mContext = this;
        accWeb=new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo = new SysUserInfo(getApplicationContext());
        lsv_aim = getIntent().getStringExtra("aim");
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClickListener());
        listview = (ListView) findViewById(R.id.lst_company);
        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);
        btn_search = (Button) findViewById(R.id.btn_search);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page=1;
                dList.clear();
                DownLoadDataThread();
            }
        });

        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Page=1;
                        dList.clear();
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });
//        btn_filter.setOnClickListener(new BtnFilterClick());


//        addMap.put("provicename", "全部");
//        addMap.put("cityname", "全部");
//        getEtStrFreshListView();
        listview.setOnItemClickListener(new ListViewItemClickListener());

//        company_retailer_help = (TextView) findViewById(R.id.company_retailer_help);
//        SpannableString content = new SpannableString(company_retailer_help.getText().toString());
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        content.setSpan(new ForegroundColorSpan(Color.parseColor("#FFCC80")), 0, content.length(), 0);
//        company_retailer_help.setText(content);
//        company_retailer_help.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                Intent helpIntent = new Intent(SelectCompanyRetailer.this, HelpActivity.class);
//                helpIntent.putExtra("help", "companyRetailer");
//                startActivity(helpIntent);
//            }
//        });
        DownLoadDataThread();
    }

    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ADevicesManager.isSCSNewInterface){
                        dList = accWeb.NewGetDownLoadTraderInfor(et_search.getText().toString(), Page);
                    }else {
                        dList = accWeb.GetDownLoadTraderInfor(et_search.getText().toString(), Page);
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    // 下载扫描单号 请求服务
    private void DownLoadScanOrder(final String tDeSysCode, final String tStoreSysCode, final String tScanType, final String tOrderType) {
        scanOrderlist = new ArrayList<ScanOrder>();
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scanOrderlist = accWeb.GetCurrentScanBill(tDeSysCode,tStoreSysCode,tScanType,tOrderType);
//                    Log.d("main",scanOrderlist.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandFailed,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }



    public void initListView() {
//        dList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null);
        adapter = new SimpleAdapter(this, dList, R.layout.lst_company_item,
                new String[]{"TraderName", "Link", "Tel", "TraderId", "CorpAddr",
                }, new int[]{
                R.id.companyName, R.id.companyLink, R.id.companyTel,
                R.id.company_id, R.id.corpaddr});
        listview.setAdapter(adapter);
        tv_total.setText("（共 " + dList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(SelectCompanyRetailer.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
                    initListView();
                    break;
                case ShowMessage.HandScanSuccess://扫描单号列表返回数据
                    MyProgressDialog.close();
                    if (scanOrderlist.size()>0){
                        ShowScanOrderList();
                    }else {
                        Intent sureIntent = new Intent(SelectCompanyRetailer.this, SelectSureConfirm.class);
                        sureIntent.putExtra("title", "确定选择客户：" + (String) item.get("TraderName") + "？");
                        startActivityForResult(sureIntent, Lic_SelectSure);
                    }
                    break;
                case ShowMessage.HandFailed: //扫描单号返回报错
                    MyProgressDialog.close();
                    Intent sureIntent = new Intent(SelectCompanyRetailer.this, SelectSureConfirm.class);
                    sureIntent.putExtra("title", "确定选择客户：" + (String) item.get("TraderName") + "？");
                    startActivityForResult(sureIntent, Lic_SelectSure);
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private void ShowScanOrderList(){

        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_scanorder_list,null);
        ListView ScanOrderListView=selectview.findViewById(R.id.List_ScanOrder);
        ScanOrderAdapter scanOrderAdapter=new ScanOrderAdapter(mContext, scanOrderlist);
        ScanOrderListView.setAdapter(scanOrderAdapter);

        TextView tv_new_scanorder= selectview.findViewById(R.id.new_scanorder);

        final Dialog alertDialog6 = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setTitle("选择要扫描的单号")
                .setIcon(R.drawable.scs)
                .setView(selectview)
                .setPositiveButton("新开扫描单", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent sureIntent = new Intent(SelectCompanyRetailer.this, SelectSureConfirm.class);
                        sureIntent.putExtra("title", "确定选择客户：" + (String) item.get("TraderName") + "？");
                        startActivityForResult(sureIntent, Lic_SelectSure);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).create();
        alertDialog6.show();

        if (alertDialog6.getWindow() != null) {
            WindowManager.LayoutParams lp = alertDialog6.getWindow().getAttributes();
            if (sysUserInfo.getOldVersion().equals("T8")){
                lp.width = 600; // 宽度，可根据屏幕宽度进行计算
            }else{
                lp.width = 800; // 宽度，可根据屏幕宽度进行计算
            }

            lp.gravity = Gravity.CENTER;
            alertDialog6.getWindow().setAttributes(lp);
        }

        tv_new_scanorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sureIntent = new Intent(SelectCompanyRetailer.this, SelectSureConfirm.class);
                sureIntent.putExtra("title", "确定选择客户：" + (String) item.get("TraderName") + "？");
                startActivityForResult(sureIntent, Lic_SelectSure);
                alertDialog6.dismiss();
            }
        });

        ScanOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = null;
                //无单无入库总店发货
                if ("P_Dv_OutStock_Z_D_NoBill_NoInStock".equals(lsv_aim)) {
                    intent = new Intent(mContext, SelectStock.class);
                }
                //镜架总店有入库无单发货/无单有入库的不用选仓库了》》》
                else if ("P_Dv_OutStock_Z_D_NoBill_BeInStock".equals(lsv_aim)) {
                    //					intent = new Intent(SelectCompanyD.this,SelectStock.class);
                    intent = new Intent(mContext, P_Dv_OutStock_Z_D_NoBill_BeInStock.class);
                }else if (lsv_aim.equals("P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock")){
                    //镜片无单有入库发货
                    intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock.class);
                }else if ("P_Dv_ReturnedPurchase_Z_D_NoBill".equals(lsv_aim)) {
                    //代销镜架无单退货
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_NoBill.class);
                    intent.putExtra("stock_id", item.get("StockCode").toString());
                    intent.putExtra("stock_name", item.get("StockName").toString());
//                    scanOrder.setStockCode(jsonObject2.getString("StockCode"));
//                    scanOrder.setStockName(jsonObject2.getString("StockName"));
                }else if ("P_Dv_ReturnedPurchase_Lens_Z_D_NoBill".equals(lsv_aim)){
                    //代销镜片无单退货
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_D_NoBill.class);

                    intent.putExtra("stock_id", item.get("StockCode").toString());
                    intent.putExtra("stock_name", item.get("StockName").toString());
                }
                intent.putExtra("aim", lsv_aim);

                intent.putExtra("company_name", item.get("TraderName").toString());
                intent.putExtra("company_id", item.get("TraderId").toString());
                intent.putExtra("alias_name", item.get("Alias").toString());

                intent.putExtra("scanBillNo", scanOrderlist.get(i).getBillNo());
                intent.putExtra("scanBillNum", scanOrderlist.get(i).getBillNum());
                startActivity(intent);
                finish();
            }
        });
    }


    class EtTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            getEtStrFreshListView();
        }

    }


    public void getEtStrFreshListView() {
        if (provicename.equals("全部")) {
            provicename = "";
        }
        if (cityname.equals("全部")) {
            cityname = "";
        }
        lsv_etStr = et_search.getText().toString().trim();
        if (lsv_etStr.isEmpty()) {
            sql = "select * from newretail where agentid='" + sysUserInfo.getCompanyid() + "' and provicename like '%%" + provicename + "%%' and " +
                    "cityname like '%%" + cityname + "%%' ";
        } else {
            sql = "select traderid,tradername,link,tel,corpaddr" +
                    " from newretail where agentid='" + sysUserInfo.getCompanyid() + "' and " +
                    "provicename like '%%" + provicename + "%%' and " +
                    "cityname like '%%" + cityname + "%%' and " +

                    "(traderid like '%%" + lsv_etStr + "%%' or " +
                    "tradername like '%%" + lsv_etStr + "%%' or " +
                    "link like '%%" + lsv_etStr + "%%' or " +
                    "tel like '%%" + lsv_etStr + "%%' or " +
                    "corpaddr like '%%" + lsv_etStr + "%%') ";
        }
        initListView();

    }

    //筛选按钮
    class BtnFilterClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            showFilterDialog();
        }

    }

    /**
     * 弹出筛选dialog
     *
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-15 上午11:15:00
     */
    public void showFilterDialog() {

//        initSpinner();
        TextView tv_title = new TextView(mContext);
        tv_title.setPadding(10, 10, 10, 10);
        tv_title.setText("省份城市筛选");
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setTextSize(25);
        tv_title.setTextColor(Color.parseColor("#30C0FF"));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        //		builder.setTitle("省份城市筛选")
        builder.setCustomTitle(tv_title)
                .setView(alertView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getEtStrFreshListView();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private class BtnExitClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }
    }


    // item选择事件
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            String ScanType="OutStock";
            if ("P_Dv_ReturnedPurchase_Lens_Z_D_NoBill".equals(lsv_aim)||"P_Dv_ReturnedPurchase_Z_D_NoBill".equals(lsv_aim)) {
                ScanType="OutReturn";
            }
            String tordertype="普通";
            if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_D_NoBill")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock")) {
                tordertype="镜片";
            }

            DownLoadScanOrder(item.get("CustSysCode").toString(),"",ScanType,tordertype);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    Intent intent = null;
                    //无单无入库总店发货
                    if ("P_Dv_OutStock_Z_D_NoBill_NoInStock".equals(lsv_aim)) {
                        intent = new Intent(mContext, SelectStock.class);
                    }
                    //总店有入库无单发货/无单有入库的不用选仓库了》》》2018-04-25
                    else if ("P_Dv_OutStock_Z_D_NoBill_BeInStock".equals(lsv_aim)) {
                        //					intent = new Intent(SelectCompanyD.this,SelectStock.class);
                        intent = new Intent(mContext, P_Dv_OutStock_Z_D_NoBill_BeInStock.class);
                    }else if (lsv_aim.equals("P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock")){
                        //无单无入库镜片发货
                        intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock.class);
                    }//总店无单退货
                    else if ("P_Dv_ReturnedPurchase_Z_D_NoBill".equals(lsv_aim)) {
                        intent = new Intent(mContext, SelectStock.class);
                    }else if ("P_Dv_ReturnedPurchase_Lens_Z_D_NoBill".equals(lsv_aim)) {
                        //总店镜片无单退货
                        intent = new Intent(mContext, SelectStock.class);
                    }

                    intent.putExtra("aim", lsv_aim);
                    intent.putExtra("company_name", item.get("TraderName").toString());
                    intent.putExtra("company_id", item.get("TraderId").toString());
                    intent.putExtra("alias_name", item.get("Alias").toString());

                    startActivity(intent);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //初始化Spinner
//    public void initSpinner() {
//        inflater = LayoutInflater.from(mContext);
//        alertView = inflater.inflate(R.layout.select_retailer_filter, null);
//        spinnerProvince = (Spinner) alertView.findViewById(R.id.spinner_province_filfter);
//        spinnerCity = (Spinner) alertView.findViewById(R.id.spinner_city_filfter);
//        spinnerProvince.setOnItemSelectedListener(new SpinnerProviceListener());
//        spinnerCity.setOnItemSelectedListener(new SpinnerCityListener());
//
//        spinnerProvince.setPrompt("\t\t\t\t\t\t省份选择");
//        spinnerCity.setPrompt("\t\t\t\t\t\t城市选择");
//
//
//        listProvice.clear();
//        listProvice.add(addMap);
//        listProvice.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select provicename from newretail where agentid='" + sysUserInfo.getCompanyid() + "' group by provicename order by provicename", null));
//        spinnerProviceAdapter = new SimpleAdapter(mContext, listProvice, R.layout.select_model_filter_spinner_item, new String[]{"provicename"}, new int[]{R.id.tv_spinner_item});
//        spinnerProvince.setAdapter(spinnerProviceAdapter);
//        spinnerProvince.setSelection(provicePosition);
//
//    }

    //省份
//    class SpinnerProviceListener implements AdapterView.OnItemSelectedListener {
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view,
//                                   int position, long id) {
//            provicePosition = position;
//            Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
//            provicename = (String) map.get("provicename");
//
//            String sql = "";
//            if ("全部".equals(provicename)) {
//                sql = "select  cityname from newretail where agentid='" + sysUserInfo.getCompanyid() + "' group by cityname ";
//            } else {
//                sql = "select  cityname from newretail where agentid='" + sysUserInfo.getCompanyid() + "' and provicename ='" + provicename + "' group by cityname ";
//            }
//            listCity.clear();
//            listCity.add(addMap);
//            listCity.addAll(SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null));
//            spinnerCityAdapter = new SimpleAdapter(mContext, listCity,
//                    R.layout.select_model_filter_spinner_item, new String[]{"cityname"}, new int[]{R.id.tv_spinner_item});
//            spinnerCity.setAdapter(spinnerCityAdapter);
//            if (position == lastProvicePosition) {
//                spinnerCity.setSelection(cityPosition);
//            } else {
//
//                spinnerCity.setSelection(0);
//            }
//            lastProvicePosition = position;
//            spinnerCityAdapter.notifyDataSetChanged();
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> parent) {
//
//        }
//
//    }
//
//    //城市
//    class SpinnerCityListener implements AdapterView.OnItemSelectedListener {
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view,
//                                   int position, long id) {
//            cityPosition = position;
//            Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
//            cityname = (String) map.get("cityname");
//
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> parent) {
//
//        }
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        getEtStrFreshListView();
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

