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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Lens_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Lens_Z_D_NoBill;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Z_D_NoBill;
import com.example.wholesalersend.activity.instock_in.Holyes_Dv_Box_InStock_NoBill;
import com.example.wholesalersend.activity.instock_in.P_Dv_BrandCode_InStock;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Bill;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Bill_NoDetail;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_NoBill;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_PackBox_List_NoBill;
import com.example.wholesalersend.activity.instock_in.P_Dv_Lens_InStock_NoBill;
import com.example.wholesalersend.activity.other.P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing;
import com.example.wholesalersend.activity.other.P_Dv_MendLable_Z;
import com.example.wholesalersend.activity.other.P_Dv_ReturnedPurchase_L_D_Z_Frame;
import com.example.wholesalersend.activity.other.P_Dv_ReturnedPurchase_L_D_Z_Lens;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_NoBill_NoInStock;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.SortListMapComparator;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectStock
 * @Description: 选择仓库
 * @Author: lijin
 * @Date: 2023/10/26 16:15
 */
public class SelectStock extends Activity {

    private AccessWeb accWeb;
    private Handler hand;
    private Intent getIntent;
    private SimpleAdapter adapter;
    private Context mContext;

    private Button btn_seach;
    private ListView listview;
    private TextView tv_title, tv_total;
    private EditText et_query_stock;

    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    private String sql = "", searchSql = "";
    private String etStr = "";
    private String lsv_aim = "";

    private int Page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_stock);
        mContext = this;

        getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        listview = (ListView) findViewById(R.id.listView1);
        et_query_stock = (EditText) findViewById(R.id.et_query_stock);
        tv_total = (TextView) findViewById(R.id.tv_total);

        tv_title = (TextView) findViewById(R.id.txt_tile);
//
        if (getIntent.getStringExtra("title") != null && !getIntent.getStringExtra("title").isEmpty()) {
            tv_title.setText(getIntent().getStringExtra("title"));
        }

        btn_seach = findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page = 1;
                list.clear();
                DownLoadDataThread();
            }
        });
        listview.setOnItemClickListener(new listViewClick());
        et_query_stock.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Page = 1;
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

    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    list = accWeb.SCSGetDownLoadStockInfor(et_query_stock.getText().toString(),
//                            Page);
                    if (ADevicesManager.isSCSNewInterface){
                        list = accWeb.NewSCSGetDownLoadStockInfor(et_query_stock.getText().toString(),Page);
                    }else{
                        list = accWeb.SCSGetDownLoadStockInfor(et_query_stock.getText().toString(),Page);
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private class listViewClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent intent = null;


            //有单有明细入库
            if ("P_Dv_InStock_Bill".equals(lsv_aim)) {
                intent = new Intent(SelectStock.this, P_Dv_InStock_Bill.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if ("P_Dv_InStock_NoDetail_Bill".equals(lsv_aim)) {
                //有单无明细入库
                intent = new Intent(SelectStock.this, P_Dv_InStock_Bill_NoDetail.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if ("P_Dv_InStock_NoBill".equals(lsv_aim)) {
                //无单入库
                intent = new Intent(SelectStock.this, P_Dv_InStock_NoBill.class);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if ("P_Dv_InStock_Lens_Bill".equals(lsv_aim)) {
                //镜片有单入库
                intent = new Intent(SelectStock.this, SelectPurOrderLensDetail.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if ("P_Dv_Lens_InStock_NoBill".equals(lsv_aim)) {
                //镜片无单入库
                intent = new Intent(SelectStock.this, P_Dv_Lens_InStock_NoBill.class);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            }else if (lsv_aim.equals("Holyes_Dv_Box_InStock_NoBill")){
                //无单盒标入库
                intent = new Intent(SelectStock.this, Holyes_Dv_Box_InStock_NoBill.class);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_syscode", getIntent.getStringExtra("supplier_syscode"));
                intent.putExtra("stock_id", (String) item.get("StockSysCode"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("stock_syscode", (String) item.get("StockSysCode"));
            } else if (lsv_aim.equals("P_Dv_BrandCode_InStock")) {
                //品牌码入库
                intent = new Intent(SelectStock.this, P_Dv_BrandCode_InStock.class);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_syscode", getIntent.getStringExtra("supplier_syscode"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("stock_syscode", (String) item.get("StockSysCode"));
            }else if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                //品牌商 装盒入库跳转
                intent = new Intent(SelectStock.this, P_Dv_InStock_PackBox_List_NoBill.class);
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_syscode", getIntent.getStringExtra("supplier_syscode"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("stock_syscode", (String) item.get("StockSysCode"));
            } else if (lsv_aim.equals("P_Dv_OutStock_Z_D_Bill_BeInStock")) {
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Z_D_Bill_BeInStock.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if (lsv_aim.equals("P_Dv_OutStock_Z_D_Bill_NoInStock")) {
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Z_D_Bill_NoInStock.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock")) {
                //分店有单有入库发货
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("store_id", (String) item.get("StoreId"));
            } else if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_NoInStock")) {
                //分店有单无入库发货
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Z_L_Bill_NoInStock.class);
                intent.putExtra("purchecklno", getIntent.getStringExtra("purchecklno"));
                intent.putExtra("saplno", getIntent.getStringExtra("saplno"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("store_id", (String) item.get("StoreId"));
            }
            //代销无入库无单发货
            else if (lsv_aim.equals("P_Dv_OutStock_Z_D_NoBill_NoInStock")) {
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Z_D_NoBill_NoInStock.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));

            }else if (lsv_aim.equals("P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock")){
                //无单无入库镜片代销发货
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
            }
            //分店无入库无单发货
            else if (lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_NoInStock")) {
                intent = new Intent(SelectStock.this, P_Dv_OutStock_Z_L_NoBill_NoInStock.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
                intent.putExtra("retail_id", getIntent.getStringExtra("retail_id"));

                intent.putExtra("alias_name", getIntent.getStringExtra("TraderAlias_name"));

                intent.putExtra("trader_sysid",  getIntent.getStringExtra("trader_sysid"));
                intent.putExtra("store_sysid",getIntent.getStringExtra("store_sysid"));
                intent.putExtra("link",getIntent.getStringExtra("link"));
            }
            //仓库补标
            else if (lsv_aim.equals("P_Dv_MendLable_Z")) {
                intent = new Intent(SelectStock.this, P_Dv_MendLable_Z.class);

                intent.putExtra("supplier_syscode", getIntent.getStringExtra("supplier_syscode"));
                intent.putExtra("supplier_id", getIntent.getStringExtra("supplier_id"));
                intent.putExtra("supplier_name", getIntent.getStringExtra("supplier_name"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_syscode", (String) item.get("StockSysCode"));

                intent.putExtra("stock_name", (String) item.get("StockName"));
            }
//            //入库退回（无单）
//            else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_NoBill")) {
//                intent = new Intent(SelectStock.this, P_Dv_ReturnedPurchase_Z_G_NoBill.class);
//            }
//
            //代销无单退货
            else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_D_NoBill")) {
                intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_NoBill.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
//                intent.putExtra("alias_name", getIntent.getStringExtra("alias_name"));
            }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_D_NoBill")) {
                //镜片代销无单退货
                intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_D_NoBill.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
            }

            //分店无单退货
            else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")) {
                intent = new Intent(SelectStock.this, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
//                intent = new Intent(mContext, SelectCCSBrand.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
                intent.putExtra("retail_id", getIntent.getStringExtra("retail_id"));
                intent.putExtra("retail_name", getIntent.getStringExtra("retail_name"));
                intent.putExtra("alias_name", getIntent.getStringExtra("alias_name"));

                intent.putExtra("trader_sysid",  getIntent.getStringExtra("trader_sysid"));
                intent.putExtra("store_sysid",getIntent.getStringExtra("store_sysid"));


            } else if (lsv_aim.equals("P_Dv_ReturnedPurchase_D_L_NoBill_NoCust")) {
                intent = new Intent(SelectStock.this, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            } else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
                intent = new Intent(SelectStock.this, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
//                intent = new Intent(mContext, SelectCCSBrand.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
                intent.putExtra("retail_id", getIntent.getStringExtra("retail_id"));
                intent.putExtra("retail_name", getIntent.getStringExtra("retail_name"));
                intent.putExtra("alias_name", getIntent.getStringExtra("alias_name"));

                intent.putExtra("trader_sysid",  getIntent.getStringExtra("trader_sysid"));
                intent.putExtra("store_sysid",getIntent.getStringExtra("store_sysid"));

            }else if(lsv_aim.equals("P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing")){
                //退货直通车
                intent = new Intent(SelectStock.this, P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing.class);

                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
                intent.putExtra("retail_id", getIntent.getStringExtra("retail_id"));
                intent.putExtra("retail_name", getIntent.getStringExtra("retail_name"));
                intent.putExtra("alias_name", getIntent.getStringExtra("alias_name"));

                intent.putExtra("trader_sysid",  getIntent.getStringExtra("trader_sysid"));
                intent.putExtra("store_sysid",getIntent.getStringExtra("store_sysid"));


            } else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_D_L_NoBill_NoCust")) {
                intent = new Intent(SelectStock.this, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            }else  if(lsv_aim.equals("P_Dv_CCSBarcodeStatusWrite")){
                //CCS换货补标
                intent = new Intent(SelectStock.this, SelectCCSBrand.class);
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("stock_name", (String) item.get("StockName"));

            } else if("P_Dv_ReturnedPurchase_L_D_Z_Frame".equals(lsv_aim)){
                //镜架退货直通车
                intent = new Intent(mContext, P_Dv_ReturnedPurchase_L_D_Z_Frame.class);
                intent.putExtra("company_syscode", getIntent.getStringExtra("company_syscode"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
                intent.putExtra("stock_id", (String) item.get("StockSysCode"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            }
            else if( "P_Dv_ReturnedPurchase_L_D_Z_Lens".equals(lsv_aim)){
                //镜片退货直通车
                intent = new Intent(mContext, P_Dv_ReturnedPurchase_L_D_Z_Lens.class);
                intent.putExtra("company_syscode", getIntent.getStringExtra("company_syscode"));
                intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
                intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
                intent.putExtra("stock_id", (String) item.get("StockSysCode"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
            }
//
//            //入库换型号
//            else if (lsv_aim.equals("P_Dv_InStock_Z_ChangeProduct")) {
//                intent = new Intent(SelectStock.this, P_Dv_InStock_Z_ChangeProduct.class);
//            }

//            //代理或直营
//            intent.putExtra("company_name", getIntent.getStringExtra("company_name"));
//            intent.putExtra("company_id", getIntent.getStringExtra("company_id"));
            intent.putExtra("aim", lsv_aim);
            startActivity(intent);
            finish();
        }
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(SelectStock.this, msg.obj.toString());

                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
//                    Log.i("main", list.toString());
                    Collections.sort(list, new SortListMapComparator("StockId"));
                    adapter = new SimpleAdapter(mContext, list, R.layout.new_list_select_stock,
                            new String[]{"StockId", "StockName"}, new int[]{R.id.txt_list1,
                            R.id.txt_list2});
                    listview.setAdapter(adapter);
                    tv_total.setText("（共 " + list.size() + " 条）");
                    break;
                default:
//                    MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }

        }
    }

//    public void freshListView(String msql) {
//        list.clear();
//        list.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(msql, null));
//        Collections.sort(list, new SortListMapComparator("stock_id"));
//        adapter.notifyDataSetChanged();
//        tv_total.setText("（共 " + list.size() + " 条）");
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



