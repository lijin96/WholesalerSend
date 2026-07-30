package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.example.wholesalersend.R;
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
 * @ClassName: SelectBillProduct
 * @Description: 查看定制单据产品（镜架镜片产品）
 * @Author: lijin
 * @Date: 2026年4月8日16:57:01
 */
public class SelectAllBillProduct extends Activity {
    private Handler hand;
    private AccessWeb accWeb;
    private Thread downloadDetail;
    private SysUserInfo sysUserInfo;

    private EditText et_search;
    private TextView tv_total;
    private ListView listview;

    Map<String, Object> item;
    private List<Map<String, Object>> dataList, searchList;

    private String lsv_etStr, lsv_searchSql, lsv_type, orderno, lsv_aim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_all_bill_product);
        sysUserInfo = new SysUserInfo(this);
        listview = (ListView) findViewById(R.id.listView1);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_total = (TextView) findViewById(R.id.tv_total);

        listview.setOnItemClickListener(new listViewClick());

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        lsv_type = getIntent().getStringExtra("type");
        orderno = getIntent().getStringExtra("orderno");
        lsv_aim = getIntent().getStringExtra("aim");

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });


        dataList = new ArrayList<Map<String, Object>>();
        searchList = new ArrayList<Map<String, Object>>();

//        if (sysUserInfo.getIsDownload()) {
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newtpeinomx");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyProgressDialog.show(this, "正在下载单据明细……", true, false);
        downloadDetail = new Thread(new DownloadPeiDetailRunnable());
        downloadDetail.start();
//            sysUserInfo.setIsDownload(false);
//        } else {
//            dataList.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select * from newtpeinomx", null));
//            initListView(dataList);
//        }

    }
    private class listViewClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.putExtra("goodsid", (String) item.get("goodsid"));
            intent.putExtra("modelm", (String) item.get("modelm"));
            intent.putExtra("colors", (String) item.get("colors"));
            intent.putExtra("noscanqty", (String) item.get("noscanqty"));//数量
            intent.putExtra("amount", (String) item.get("amount"));//数量
            intent.putExtra("brandName", (String) item.get("brandName"));//数量
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, null);
        finish();
    }

    private class DownloadPeiDetailRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (orderno == null || orderno.equalsIgnoreCase("")) {
                    ShowMessage.ShowMsg(hand, "单据为空");
                    return;
                }

                ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
                HashMap<Object, Object> map = new HashMap<Object, Object>();
                map.put("OrderNo", orderno);
                para.add(map);
                String tListData = accWeb.GetAPIStringInterface("AndroidDv/GetSaleOrderDetail",para);

                JSONArray listjson = new JSONArray(tListData);
                for (int i = 0; i < listjson.length(); i++) {
                    JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                    Map<String, Object> map1 = new HashMap<String, Object>();
                    map1.put("goodsSysCode", jsonObject2.optString("goodsSysCode"));
                    map1.put("goodsCode", jsonObject2.optString("goodsCode"));
                    map1.put("goodsName", jsonObject2.optString("goodsName"));
                    map1.put("goodsTypeName", jsonObject2.optString("goodsTypeName"));
                    map1.put("brandName", jsonObject2.optString("brandName"));
                    map1.put("seriesName", jsonObject2.optString("seriesName"));
                    map1.put("modelm", jsonObject2.optString("modelm"));
                    map1.put("colors", jsonObject2.optString("colors"));
                    map1.put("saleOrderQty", jsonObject2.optString("saleOrderQty"));
                    map1.put("unShipNum", jsonObject2.optString("unShipNum"));
                    map1.put("diopter", jsonObject2.optString("diopter"));
                    map1.put("astigmatism", jsonObject2.optString("astigmatism"));
                    map1.put("eyeDirection", jsonObject2.optString("eyeDirection"));
                    map1.put("refractiveIndex", jsonObject2.optString("refractiveIndex"));

                    dataList.add(map1);
                }

                if (dataList.size() == 0) {
                    ShowMessage.ShowMsg(hand, "当前没有数据下载");
                    return;
                }
                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
            }
        }
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectAllBillProduct.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
//                    Log.d("main",dataList.toString());
                    initListView(dataList);
                    break;
                default:
                    MyProgressDialog.close();
                    break;
            }
            MyProgressDialog.close();
        }
    }

    public void initListView(List<Map<String, Object>> list) {
//        Collections.sort(list, new SortListMapComparator("goodsid"));
        SimpleAdapter adapter = new SimpleAdapter(SelectAllBillProduct.this, list,
                R.layout.list_all_lens_detail, new String[]{"brandName","goodsCode", "refractiveIndex","eyeDirection", "modelm", "colors","diopter","astigmatism", "unShipNum"},
                new int[]{R.id.txt_brand,R.id.txt_list1,R.id.txt_refractive,R.id.txt_remark, R.id.txt_model,R.id.txt_color,R.id.txt_list2, R.id.txt_list3, R.id.txt_list4});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（ 共" + list.size() + "条）");
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
