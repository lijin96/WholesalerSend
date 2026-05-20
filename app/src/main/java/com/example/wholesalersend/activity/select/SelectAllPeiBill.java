package com.example.wholesalersend.activity.select;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.other.P_Dv_OutStock_Z_D_L_Bill_BeInStock;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectAllPeiBill
 * @Description: 定制代销零售发货配货单
 * @Author: lijin
 * @Date: 2026年4月8日14:49:36
 */
public class SelectAllPeiBill extends Activity implements View.OnClickListener {

    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private Button btn_seach;
    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView tv_trade, tv_code, tv_title;

    private List<Map<String, Object>> list;
    private Map<String, Object> item;

    private SimpleAdapter adapter;

    private String lsv_aim = "", lsv_etStr = "", lsv_searchSql = "";

    private List<Map<String, Object>> CustStoreRelate=new ArrayList<Map<String, Object>>();

    private String BrandingCode="",AgentCode="",BrandingCustCode="",TraderSysId="",StoreSysId="",TraderAlias_name="",Storealias_name="";;
    private Boolean IsBindCCS=false,IsBindCCScust=false,IsBindCCSstore=false,IsSendToStore=false;

    private Boolean IsCustomized=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_allpeibill);
        lsv_aim = getIntent().getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        sysUserInfo=new SysUserInfo(mContext);
        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_title = (TextView) findViewById(R.id.textView1);
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newtpurcheck");
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_trade = (TextView) findViewById(R.id.txt_pur_3);
        tv_code = (TextView) findViewById(R.id.txt_pur_5);

        tv_trade.setText("代理商");
        tv_code.setText("代理商ID");

        listview.setOnItemClickListener(new ListViewItemClik());

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                DownLoadDataThread();
            }
        });


        et_search = (EditText) findViewById(R.id.et_search);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        list.clear();
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });
        DownLoadDataThread();
    }

    private class ListViewItemClik implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);

            if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_NoInStock")){
                CustStoreRelate.clear();
                GetScsCustStoreRelate("","","", (String) item.get("PeiGoodLno"));
            }else
//                if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")){
//                Intent intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                intent.putExtra("saplno", (String) item.get("SapLno"));
//                intent.putExtra("supplier_name", (String) item.get("AgentName"));
//                intent.putExtra("stock_name", (String) item.get("StockName"));
//                intent.putExtra("supplier_id", (String) item.get("AgentId"));
//                intent.putExtra("stock_id", (String) item.get("StockId"));
//                intent.putExtra("store_id", (String) item.get("StoreId"));
//                intent.putExtra("aim", lsv_aim);
//                startActivity(intent);
//
//            } else
               {
                JumpInterface();
            }
        }
    }

    //跳转界面
    private void  JumpInterface(){
        Intent intent = new Intent(mContext, P_Dv_OutStock_Z_D_L_Bill_BeInStock.class);
        intent.putExtra("saleOrderNo", (String) item.get("saleOrderNo"));

        intent.putExtra("customerName", (String) item.get("customerName"));
        intent.putExtra("customerCode", (String) item.get("customerCode"));
        intent.putExtra("custSysCode", (String) item.get("custSysCode"));
        intent.putExtra("storeName", (String) item.get("storeName"));
        intent.putExtra("storeCode", (String) item.get("storeCode"));
        intent.putExtra("storeSysCode", (String) item.get("storeSysCode"));
        intent.putExtra("retailerName", (String) item.get("retailerName"));
        intent.putExtra("retailerCode", (String) item.get("retailerCode"));
        intent.putExtra("retailerSysCode", (String) item.get("retailerSysCode"));


        intent.putExtra("aim", lsv_aim);
        startActivity(intent);
    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        list = new ArrayList<Map<String, Object>>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
                    HashMap<Object, Object> map = new HashMap<Object, Object>();
                    map.put("StartTime", "");
                    map.put("EndTime", "");
                    map.put("Custom", IsCustomized);
                    map.put("OrderType", "");
                    map.put("IsManuDirectSend", "");
                    para.add(map);
//                    Log.d("main", para.toString());
                    String tListData = accWeb.GetAPIStringInterface("AndroidDv/GetSaleOrderList",para);
//                    Log.d("main", tListData);
                    JSONArray listjson = new JSONArray(tListData);
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("saleOrderNo", jsonObject2.optString("saleOrderNo"));
                        map1.put("saleOrderDate", jsonObject2.optString("saleOrderDate"));
                        map1.put("solNo", jsonObject2.optString("solNo"));
                        map1.put("customerName", jsonObject2.optString("customerName"));
                        map1.put("customerCode", jsonObject2.optString("customerCode"));
                        map1.put("custSysCode", jsonObject2.optString("custSysCode"));
                        map1.put("storeName", jsonObject2.optString("storeName"));
                        map1.put("storeCode", jsonObject2.optString("storeCode"));
                        map1.put("storeSysCode", jsonObject2.optString("storeSysCode"));
                        map1.put("retailerName", jsonObject2.optString("retailerName"));
                        map1.put("retailerCode", jsonObject2.optString("retailerCode"));
                        map1.put("retailerSysCode", jsonObject2.optString("retailerName"));
                        list.add(map1);
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    public void initListView(List<Map<String, Object>> mList) {
//        Collections.sort(list, new SortListMapComparator("PeiGoodLno"));

        adapter = new SimpleAdapter(this, mList, R.layout.list_all_purcheck,
                new String[]{"saleOrderNo", "customerName","customerCode", "retailerName", "retailerCode"}, new int[]{R.id.txt_pur_1,  R.id.txt_pur_3, R.id.txt_pur_5,R.id.txt_retail,R.id.txt_retail_id});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectAllPeiBill.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    if (list.size() > 0) {
                        initListView(list);
                    }else{
                        if (adapter!=null) {
                            adapter.notifyDataSetChanged();
                        }
                        ShowMessage.Show(SelectAllPeiBill.this, "暂无数据");
                    }
                    break;
                case ShowMessage.HandScanSuccess:
                    MyProgressDialog.close();
//                    if (CustStoreRelate.size()>0) {
//                        showBrandDialog();
//                    }else{
                    JumpInterface();
//                    }
                    break;
                default:
                    break;
            }

            MyProgressDialog.close();
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            if (requestCode == 0) {
            }
        }
    }

    //判断门店是否已经绑定CCS客户或者CCS门店
    private void GetScsCustStoreRelate(final String tCustSysCode, final String tStoreSysCode, final String tBrandCode, final String tBillNo) {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> data = accWeb.GetScsCustStoreRelate(tCustSysCode,tStoreSysCode,tBrandCode,tBillNo);

                    for (int i = 0; i <data.size() ; i++) {
                        if ((Boolean)data.get(i).get("NeedBind")){
                            CustStoreRelate.add(data.get(i));
                        }
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    //当前单据是否存在多品牌
//    private void showBrandDialog(){
//        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_bindbrand_list,null);
//        ListView BindBrandListView=selectview.findViewById(R.id.List_BindBrand);
//        SimpleAdapter adapter = new SimpleAdapter(mContext,CustStoreRelate, R.layout.new_list_select_bindbrand, new String[]{
//                "BrandingCode", "BrandName"}, new int[]{R.id.txt_list1, R.id.txt_list2});
//        BindBrandListView.setAdapter(adapter);
//
//        final Dialog normalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
//                .setIcon(R.mipmap.scs)
//                .setView(selectview)
//                .setPositiveButton("前往扫描", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                        Intent scsintent = null;
//                        if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                            //有单有入库有明细分店发货
//                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
//                        }
//                        else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                            //有单有入库无明细分店发货
//                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                        }
//                        else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                            //镜片有单有入库分店发货
//                            scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
//                        }
//                        else if ("P_Dv_OutStock_Z_L_Bill_NoInStock".equals(lsv_aim)) {
//                            //镜片有单无入库分店发货
////                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_NoInStock.class);
//                            if (item.get("StockId")== null || item.get("StockId").equals("")){
//                                scsintent = new Intent(mContext, SelectStock.class);
//                            }else{
//                                scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_NoInStock.class);
//                            }
//                        }
//
//                        scsintent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                        scsintent.putExtra("saplno", (String) item.get("SapLno"));
//                        scsintent.putExtra("supplier_name", (String) item.get("AgentName"));
//                        scsintent.putExtra("stock_name", (String) item.get("StockName"));
//                        scsintent.putExtra("supplier_id", (String) item.get("AgentId"));
//                        scsintent.putExtra("stock_id", (String) item.get("StockId"));
//                        scsintent.putExtra("store_id", (String) item.get("StoreId"));
//
//
//                        scsintent.putExtra("aim", lsv_aim);
//                        startActivity(scsintent);
//                    }
//                }).create();
//
//        normalDialog.show();
//
//        if (normalDialog.getWindow() != null) {
//            WindowManager.LayoutParams lp = normalDialog.getWindow().getAttributes();
//            lp.width = 800; // 宽度，可根据屏幕宽度进行计算
//            lp.gravity = Gravity.CENTER;
//            normalDialog.getWindow().setAttributes(lp);
//        }
//
//        BindBrandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                IsBindCCS = (Boolean) CustStoreRelate.get(i).get("NeedBind");
//                IsBindCCScust = (Boolean)CustStoreRelate.get(i).get("CustBind");
//                IsBindCCSstore = (Boolean) CustStoreRelate.get(i).get("StoreBind");
//                IsSendToStore = (Boolean) CustStoreRelate.get(i).get("IsSendToStore");
//
//                BrandingCode= (String) CustStoreRelate.get(i).get("BrandingCode");
//                AgentCode= (String) CustStoreRelate.get(i).get("AgentCode");
//                BrandingCustCode= (String) CustStoreRelate.get(i).get("BrandingCustCode");
//
//                TraderSysId= (String) CustStoreRelate.get(i).get("CustSysCode");
//                StoreSysId=(String) CustStoreRelate.get(i).get("StoreSysCode");
//
//                TraderAlias_name=(String) CustStoreRelate.get(i).get("TraderAlias");
//                Storealias_name=(String) CustStoreRelate.get(i).get("StoreAlias");
//
//                if(!IsBindCCScust){
//                    //需要绑定CCS客户
//                    Intent intent=new Intent(mContext,SelectCCScustomer.class);
//
//                    intent.putExtra("Brand_code", BrandingCode);
//                    intent.putExtra("Agent_Code", AgentCode);
//                    intent.putExtra("trader_sysid",TraderSysId);
//                    intent.putExtra("store_sysid", StoreSysId);
//
//                    if (IsSendToStore) {
//                        intent.putExtra("IsBindCCSstore", IsBindCCSstore);
//                    }else{
//                        intent.putExtra("IsBindCCSstore", false);
//                    }
//
//                    intent.putExtra("TraderAlias_name", TraderAlias_name);
//                    intent.putExtra("storealias_name", Storealias_name);
//                    intent.putExtra("CCSCuts_Code", BrandingCustCode);
//
//
//                    intent.putExtra("scanBillNo", "");
//                    intent.putExtra("scanBillNum", "");
//
//                    intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                    intent.putExtra("saplno", (String) item.get("SapLno"));
//                    intent.putExtra("supplier_name", (String) item.get("AgentName"));
//                    intent.putExtra("stock_name", (String) item.get("StockName"));
//                    intent.putExtra("supplier_id", (String) item.get("AgentId"));
//                    intent.putExtra("stock_id", (String) item.get("StockId"));
//                    intent.putExtra("store_id", (String) item.get("StoreId"));
//                    intent.putExtra("aim", lsv_aim);
//
//                    startActivity(intent);
//
//                }else if (IsSendToStore) {
//                    //需要绑定CCS门店
//                    if (!IsBindCCSstore) {
//                        //当前未绑定CCS门店
//                        Intent intent = new Intent(mContext, SelectCCSstore.class);
//
//                        intent.putExtra("Brand_code", BrandingCode);
//                        intent.putExtra("Agent_Code", AgentCode);
//                        intent.putExtra("CCSCuts_Code", BrandingCustCode);
//                        intent.putExtra("trader_sysid", TraderSysId);
//                        intent.putExtra("store_sysid", StoreSysId);
//
//                        intent.putExtra("TraderAlias_name", TraderAlias_name);
//                        intent.putExtra("storealias_name", Storealias_name);
//
//                        intent.putExtra("scanBillNo", "");
//                        intent.putExtra("scanBillNum", "");
//
//                        intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                        intent.putExtra("saplno", (String) item.get("SapLno"));
//                        intent.putExtra("supplier_name", (String) item.get("AgentName"));
//                        intent.putExtra("stock_name", (String) item.get("StockName"));
//                        intent.putExtra("supplier_id", (String) item.get("AgentId"));
//                        intent.putExtra("stock_id", (String) item.get("StockId"));
//                        intent.putExtra("store_id", (String) item.get("StoreId"));
//                        intent.putExtra("aim", lsv_aim);
//                        startActivity(intent);
//                    }else{
//                        Intent scsintent = null;
//                        if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                            //有单有入库有明细分店发货
//                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
//                        }
//                        else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                            //有单有入库无明细分店发货
//                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                        }
//                        else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                            //镜片有单有入库分店发货
//                            scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
//                        }
//
//                        scsintent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                        scsintent.putExtra("saplno", (String) item.get("SapLno"));
//                        scsintent.putExtra("supplier_name", (String) item.get("AgentName"));
//                        scsintent.putExtra("stock_name", (String) item.get("StockName"));
//                        scsintent.putExtra("supplier_id", (String) item.get("AgentId"));
//                        scsintent.putExtra("stock_id", (String) item.get("StockId"));
//                        scsintent.putExtra("store_id", (String) item.get("StoreId"));
//
//
//                        scsintent.putExtra("aim", lsv_aim);
//                        startActivity(scsintent);
//                    }
//                }else{
//                    // TODO Auto-generated method stub
//                    Intent scsintent = null;
//                    if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                        //有单有入库有明细分店发货
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
//                    }
//                    else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                        //有单有入库无明细分店发货
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                    }
//                    else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                        //镜片有单有入库分店发货
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
//                    }
//
//                    scsintent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                    scsintent.putExtra("saplno", (String) item.get("SapLno"));
//                    scsintent.putExtra("supplier_name", (String) item.get("AgentName"));
//                    scsintent.putExtra("stock_name", (String) item.get("StockName"));
//                    scsintent.putExtra("supplier_id", (String) item.get("AgentId"));
//                    scsintent.putExtra("stock_id", (String) item.get("StockId"));
//                    scsintent.putExtra("store_id", (String) item.get("StoreId"));
//
//
//                    scsintent.putExtra("aim", lsv_aim);
//                    startActivity(scsintent);
//                }
//                normalDialog.dismiss();
//            }
//        });
//
////        normalDialog.setPositiveButton("确定",
////                new DialogInterface.OnClickListener() {
////                    @Override
////                    public void onClick(DialogInterface dialog, int which) {
////
////                    }
////                });
////        normalDialog.setNegativeButton("关闭",
////                new DialogInterface.OnClickListener() {
////                    @Override
////                    public void onClick(DialogInterface dialog, int which) {
////                        //...To-do
////                    }
////                });
//        // 显示
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


