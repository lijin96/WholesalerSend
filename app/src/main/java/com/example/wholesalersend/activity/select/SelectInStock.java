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


import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.other.P_Dv_InStock_Z_ChangeStock_NoBill;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.SortListMapComparator;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectInStock
 * @Description: 调入仓选择
 * @Author: lijin
 * @Date: 2021/3/10 9:57
 */
public class SelectInStock extends Activity {
    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;
    private Intent getIntent;
    private SimpleAdapter adapter;

    private Button btn_seach;
    private ListView listview;
    private TextView tv_title, tv_total;
    private EditText et_query_stock;

    private List<Map<String, Object>> list;
    private Map<String, Object> item;

    private String sql = "", searchSql = "";
    private String etStr = "";
    private String lsv_aim = "";

    private int Page=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_stock);

        mContext=this;
        getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");
        accWeb=new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        listview = (ListView) findViewById(R.id.listView1);
        et_query_stock = (EditText) findViewById(R.id.et_query_stock);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_title = (TextView) findViewById(R.id.txt_tile);
        //
        tv_title.setText("请选择调入仓");

        listview.setOnItemClickListener(new listViewClick());
        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page=1;
                list.clear();
                DownLoadDataThread();
            }
        });

        et_query_stock.setOnKeyListener(new View.OnKeyListener() {
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
        DownLoadDataThread();
    }


    private class listViewClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            ListView listView = (ListView) parent;

            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent intent = null;
            intent = new Intent(mContext, P_Dv_InStock_Z_ChangeStock_NoBill.class);
            intent.putExtra("aim", lsv_aim);
            intent.putExtra("outstock_name", getIntent.getStringExtra("outstock_name"));
            intent.putExtra("outstock_id", getIntent.getStringExtra("outstock_id"));
            intent.putExtra("instock_id", (String) item.get("StockId"));
            intent.putExtra("instock_name", (String) item.get("StockName"));
            startActivity(intent);
        }

    }

    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list = accWeb.SCSGetDownLoadStockInfor(et_query_stock.getText().toString(),Page);
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectInStock.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
                    Collections.sort(list, new SortListMapComparator("StockId"));
                    adapter = new SimpleAdapter(mContext, list, R.layout.new_list_select_stock, new String[]
                            {"StockId", "StockName"}, new int[]{R.id.txt_list1, R.id.txt_list2});
                    listview.setAdapter(adapter);
                    tv_total.setText("（共 " + list.size() + " 条）");
                    break;
                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
        }
    }

//    public void freshListView(String msql) {
//        list.clear();
//        list.addAll(SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList(sql, null));
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
