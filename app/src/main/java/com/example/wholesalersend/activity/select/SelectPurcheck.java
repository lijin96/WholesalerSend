package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Bill;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Bill_NoDetail;
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
 * @ClassName: SelectPurcheck
 * @Description: 有单入库验收单
 * @Author: lijin
 * @Date: 2023/10/26 17:46
 */
public class SelectPurcheck extends Activity {

    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView txt_monomial;
    private Button btn_seach;

    private List<Map<String, Object>> list;
    private List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    int Page=1;

    private String lsv_aim = "", lsv_etStr = "", lsv_searchSql = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_purcheck);
        lsv_aim = getIntent().getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);
        txt_monomial = (TextView) findViewById(R.id.txt_1);


        list = new ArrayList<Map<String, Object>>();

        listview.setOnItemClickListener(new ListViewItemClik());
        et_search = (EditText) findViewById(R.id.et_search);

        if ("P_Dv_InStock_Bill".equals(lsv_aim)|| lsv_aim.equals("P_Dv_InStock_NoDetail_Bill")) {
            txt_monomial.setText("品检验收单");
        }else{
            txt_monomial.setText("镜片入库单");
        }

        DownLoadDataThread();



        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page=1;
                list.clear();
                DownLoadDataThread();
            }
        });

        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Page=1;
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

//        MyProgressDialog.show(this, "正在获取数据...", false, false);
//        downloadThread = new Thread(new DownLoadDataThread());
//
//        downloadThread.start();
        //输入查询品检单
//        et_search.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//                // TODO Auto-generated method stub
//                lsv_etStr = et_search.getText().toString().trim();
//                if (lsv_etStr.trim().isEmpty()) {
//                    initListView(list);
//                    return;
//                } else {
//                    lsv_searchSql = "select * from newtpurcheck where " +
//                            "purchecklno like '%%" + lsv_etStr + "%%' or " +
//                            "saplno like '%%" + lsv_etStr + "%%' or " +
//                            "supplier_na like '%%" + lsv_etStr + "%%' or " +
//                            "stock_name like '%%" + lsv_etStr + "%%' or " +
//                            "supplier_id like '%%" + lsv_etStr + "%%' or " +
//                            "stock_id like '%%" + lsv_etStr + "%%'";
//                    searchList.clear();
//                    searchList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(lsv_searchSql, null);
//                    initListView(searchList);
//                }
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
//                                          int arg3) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable arg0) {
//                // TODO Auto-generated method stub
//
//            }
//        });
    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(this, "正在获取数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    if ("P_Dv_InStock_Bill".equals(lsv_aim)){
                        list = accWeb.GetDownLoadScsPurOrder("普通",et_search.getText().toString(),Page,false);
                    }else if (lsv_aim.equals("P_Dv_InStock_NoDetail_Bill")){
                        list = accWeb.GetDownLoadScsPurOrder("普通",et_search.getText().toString(),Page,true);
                    }else if (lsv_aim.equals("P_Dv_InStock_Lens_Bill")){
                        list = accWeb.GetDownLoadScsPurOrder("镜片",et_search.getText().toString(),Page,false);
                    }else if (lsv_aim.equals("P_Dv_InStock_Lens_NoDetail_Bill")){
                        list = accWeb.GetDownLoadScsPurOrder("镜片",et_search.getText().toString(),Page,false);
                    }
//
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private class ListViewItemClik implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);
            Intent intent = new Intent(SelectPurcheck.this, SelectSureConfirm.class);
            intent.putExtra("title", "确定选择品检单：" + item.get("PurOrderNo")
                    + "的产品吗？");
            startActivityForResult(intent, 0);
        }
    }

//    private class DownLoadDataThread implements Runnable {
//        @Override
//        public void run() {
//            if (lsv_aim == null || lsv_aim.isEmpty()) {
//                return;
//            }
//            try {
//                //有单入库
//                if ("P_Dv_InStock_Bill".equals(lsv_aim)) {
//                    txt_monomial.setText("品检验收单");
//                    list = accWeb.GetDowLoadPurCheckBill();
//                }
//                //有单入库退回,有单入库退回撤销
//                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Bill")) {
//                    txt_monomial.setText("品检退货单");
//                    list = accWeb.GetDowLoadPurOutBill();
//                }
//                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
//            } catch (Exception e) {
//                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
//            }
//        }
//
//    }


    public void initListView(List<Map<String, Object>> mList) {
//        Collections.sort(mList, new SortListMapComparator("PurOrderNo"));

        if (mList.size()==0){
            ShowMessage.Show(SelectPurcheck.this,"暂无数据");
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.list_purcheck,
                new String[]{"PurOrderNo", "SapLno", "SupplierName", "StockName", "SupplierId", "StockId"}, new int[]{R.id.txt_pur_1, R.id.txt_pur_2, R.id.txt_pur_3, R.id.txt_pur_4, R.id.txt_pur_5, R.id.txt_pur_6});
        listview.setAdapter(adapter);
//        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectPurcheck.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
//                    List<String> sqlList = new ArrayList<String>();
//                    if (list.size() > 0) {
//                        String purchecklno, saplno, supplier_na, stock_name, supplier_id, stock_id, sql;
//                        for (Map<String, Object> map : list) {
//                            purchecklno = (String) map.get("PurCheckLno");
//                            saplno = (String) map.get("SapLno");
//                            supplier_na = (String) map.get("SupplierName");
//                            stock_name = (String) map.get("StockName");
//                            supplier_id = (String) map.get("SupplierId");
//                            stock_id = (String) map.get("StockId");
//                            sql = "insert into newtpurcheck(purchecklno,saplno,supplier_na,stock_name,supplier_id," +
//                                    "stock_id)values('" + purchecklno + "','" + saplno + "','" + supplier_na + "','" + stock_name + "','" + supplier_id + "','" + stock_id + "')";
//                            sqlList.add(sql);
//                        }
//                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
//                        String listsql = "select * from newtpurcheck";
//                        list = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(listsql, null);

                        initListView(list);


//                    }

                    break;
                default:
                    break;
            }
            MyProgressDialog.close();
        }
    }


//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
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
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            if (requestCode == 0) {
                Intent intent = null;
//                有单入库
                if ("P_Dv_InStock_Bill".equals(lsv_aim)) {
                    //如果入库单的仓库id不存在就需要先去选择仓库
                    if (item.get("StockId")== null || item.get("StockId").equals("")){
                        intent = new Intent(mContext, SelectStock.class);
                    }else{
                        intent = new Intent(mContext, P_Dv_InStock_Bill.class);
                    }

                }else if("P_Dv_InStock_NoDetail_Bill".equals(lsv_aim)){
                    //如果入库单的仓库id不存在就需要先去选择仓库
                    if (item.get("StockId")== null || item.get("StockId").equals("")){
                        intent = new Intent(mContext, SelectStock.class);
                    }else{
                        intent = new Intent(mContext, P_Dv_InStock_Bill_NoDetail.class);
                    }
                }else if ("P_Dv_InStock_Lens_Bill".equals(lsv_aim)) {
                    //如果入库单的仓库id不存在就需要先去选择仓库
                    if (item.get("StockId")== null || item.get("StockId").equals("")){
                        intent = new Intent(mContext, SelectStock.class);
                    }else{
                        intent = new Intent(mContext, SelectPurOrderLensDetail.class);
                    }
                }else if ("P_Dv_InStock_Lens_NoDetail_Bill".equals(lsv_aim)) {
                    //如果入库单的仓库id不存在就需要先去选择仓库
                    if (item.get("StockId")== null || item.get("StockId").equals("")){
                        intent = new Intent(mContext, SelectStock.class);
                    }else{
                        intent = new Intent(mContext, SelectPurOrderLensDetail.class);
                    }
                }
//                //有单入库撤销
//                else if ("P_Dv_InStock_Cancel_Bill".equals(lsv_aim)) {
//                    intent = new Intent(SelectPurcheck.this, P_Dv_InStock_Bill_Cancel.class);
//                }
//                //有单入库退回
//                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Bill")) {
//                    intent = new Intent(SelectPurcheck.this, P_Dv_ReturnedPurchase_Z_G_Bill.class);
//                }
//                //入库退回撤销（有单）
//                else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_G_Cancel_Bill")) {
//                    intent = new Intent(SelectPurcheck.this, P_Dv_ReturnedPurchase_Z_G_Bill_Cancel.class);
//                }

                intent.putExtra("purchecklno", (String) item.get("PurOrderNo"));
                intent.putExtra("saplno", (String) item.get("SapLno"));
                intent.putExtra("supplier_name", (String) item.get("SupplierName"));
                intent.putExtra("stock_name", (String) item.get("StockName"));
                intent.putExtra("supplier_id", (String) item.get("SupplierId"));
                intent.putExtra("stock_id", (String) item.get("StockId"));
                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
            }

        }


    }

    @Override       //这里是实现了自动更新
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        Page=1;
//        list=new ArrayList<Map<String, Object>>();
//        DownLoadDataThread();
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