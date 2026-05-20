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
import com.example.wholesalersend.activity.instock_in.P_Dv_BrandCode_InStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_NoInStock;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.SortListMapComparator;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectPeiBill
 * @Description: 选择品牌码入库单
 * @Author: lijin
 * @Date: 2024年11月15日11:16:19
 */
public class SelectBrandInStockBill extends Activity implements View.OnClickListener {

    private Context mContext;
    private AccessWeb accWeb;
    private Thread downloadThread;
    private Handler hand;

    private Button btn_seach;
    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView tv_trade, tv_code, tv_title;
    private Button btn_addbrandbill;//

    private List<Map<String, Object>> list;
    private Map<String, Object> item;

    private SimpleAdapter adapter;

    private String lsv_aim = "", lsv_etStr = "", lsv_searchSql = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_brandinstockbill);
        lsv_aim = getIntent().getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
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

        listview.setOnItemClickListener(new ListViewItemClik());


        btn_seach = findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                DownLoadDataThread();
            }
        });

        btn_addbrandbill = findViewById(R.id.btn_addbrandbill);
        btn_addbrandbill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SelectSupplier.class);
                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
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
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent intent = new Intent(mContext, P_Dv_BrandCode_InStock.class);
//
            intent.putExtra("purchecklno", (String) item.get("BillNo"));

            intent.putExtra("supplier_name", (String) item.get("SupplierName"));
            intent.putExtra("stock_name", (String) item.get("StockName"));
            intent.putExtra("supplier_syscode", (String) item.get("SuppSysCode"));
            intent.putExtra("stock_syscode", (String) item.get("StockSysCode"));
            intent.putExtra("BillNum", (String) item.get("BillNum"));

            intent.putExtra("aim", lsv_aim);
            startActivity(intent);


//            Intent intent = new Intent(SelectBrandInStockBill.this, SelectSureConfirm.class);
//            if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_D_Bill")
//                    || lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_Bill")
//                    || lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_Bill_Detail")
//                    || lsv_aim.equals("P_Dv_ReturnedPurchase_Z_D_Bill_Detail")
//                    || lsv_aim.equals("P_Dv_L_Return_Z_Bill")
//            ) {
//                intent.putExtra("title", "确定选择退货配货单：" + item.get("purchecklno")
//                        + "的产品吗？");
//            } else {
//                intent.putExtra("title", "确定选择订单：" + item.get("PeiGoodLno")
//                        + "的产品吗？");
//            }
//            startActivityForResult(intent, 0);
        }
    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        list = new ArrayList<Map<String, Object>>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    list = accWeb.GetBrandCurrentScanBill("BrandInStock", "普通");

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
        adapter = new SimpleAdapter(this, mList, R.layout.list_brandinstock, new String[]{"BillNo"
                , "BillDate", "SupplierName", "StockName", "SuppSysCode", "StockSysCode"},
                new int[]{R.id.txt_pur_1, R.id.txt_pur_2, R.id.txt_pur_3, R.id.txt_pur_4,
                        R.id.txt_pur_5, R.id.txt_pur_6});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectBrandInStockBill.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    if (list.size() > 0) {
                        initListView(list);
                    } else {
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        ShowMessage.Show(SelectBrandInStockBill.this, "暂无数据");
                    }
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
//            case R.id.btnok:
//                Intent intent = new Intent();
//
//                intent.putExtra("purchecklno", (String) item.get("purchecklno"));
//                intent.putExtra("saplno", (String) item.get("saplno"));
//                intent.putExtra("supplier_name", (String) item.get("supplier_na"));
//                intent.putExtra("stock_name", (String) item.get("stock_name"));
//                intent.putExtra("supplier_id", (String) item.get("supplier_id"));
//                intent.putExtra("stock_id", (String) item.get("stock_id"));
//
//                setResult(RESULT_OK, intent);
//                this.finish();
//                break;
//            case R.id.btncancle:
//                setResult(RESULT_CANCELED);
//                this.finish();
//                break;
//            default:
//                break;
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (lsv_aim == null || lsv_aim.isEmpty()) {
//                return;
//            }
//            if (requestCode == 0) {
//
//            }
//
//        }
//
//
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


