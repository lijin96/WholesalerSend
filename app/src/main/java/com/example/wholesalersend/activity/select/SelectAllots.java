package com.example.wholesalersend.activity.select;

import android.annotation.SuppressLint;
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
import com.example.wholesalersend.activity.other.P_Dv_InStock_Z_ChangeStock_Bill;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectAllots
 * @Description: 选择调拨单
 * @Author: lijin
 * @Date: 2021/3/10 9:55
 */
public class SelectAllots extends Activity implements View.OnClickListener {

    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private Button btn_seach;

    private List<Map<String, Object>> list;
    private Map<String, Object> item;

    private String lsv_aim = "", lsv_etStr = "", lsv_searchSql = "";

    private int Page=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_allots);
        lsv_aim = getIntent().getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);

        list = new ArrayList<Map<String, Object>>();
        listview.setOnItemClickListener(new ListViewItemClik());
        et_search = (EditText) findViewById(R.id.et_query_stock);
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

        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page=1;
                list.clear();
                DownLoadDataThread();
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
            Intent intent = new Intent(SelectAllots.this, SelectSureConfirm.class);
            intent.putExtra("title", "确定选择调拨单：" + item.get("AllotLno")
                    + "的产品吗?");
            startActivityForResult(intent, 0);
        }
    }

    //下载调拨单
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    list = accWeb.GetDowLoadSCSAllots(et_search.getText().toString(),Page);
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }
//    private class DownLoadDataThread implements {
//        @SuppressLint("NewApi")
//        @Override
//        public void run() {
//            if (lsv_aim == null || lsv_aim.isEmpty()) {
//                return;
//            }
//            try {
//                list = accWeb.GetDowLoadAllots();
//                ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
//            } catch (Exception e) {
//                ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
//            }
//        }



    public void initListView(List<Map<String, Object>> mList) {
        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.new_list_allots,
                new String[]{"AllotLno", "SapLno", "OutStockName", "OutStockId", "NoAllotQty", "InStockName", "InStockId"}, new int[]{R.id.txt_1, R.id.txt_pur_1, R.id.txt_pur_2, R.id.txt_pur_6, R.id.txt_pur_4, R.id.txt_pur_3, R.id.txt_pur_5});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectAllots.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    if (list.size() > 0) {
                        initListView(list);
                    }else{
                        ShowMessage.Show(mContext,"暂无调拨单数据");
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
//                intent.putExtra("purchecklno", (String) item.get("allotlno"));
//                intent.putExtra("saplno", (String) item.get("saplno"));
//                intent.putExtra("supplier_name", (String) item.get("outstockname"));
//                intent.putExtra("stock_name", (String) item.get("instockname"));
//                intent.putExtra("supplier_id", (String) item.get("noallotqty"));
//                intent.putExtra("stock_id", (String) item.get("outstockid"));
//                intent.putExtra("instockid", (String) item.get("instockid"));
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


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            if (requestCode == 0) {
                Intent intent = null;
                intent = new Intent(SelectAllots.this, P_Dv_InStock_Z_ChangeStock_Bill.class);
                intent.putExtra("purchecklno", (String) item.get("AllotLno"));
                intent.putExtra("saplno", (String) item.get("SapLno"));
                intent.putExtra("supplier_name", (String) item.get("OutStockName"));
                intent.putExtra("stock_name", (String) item.get("InStockName"));
                intent.putExtra("noallotqty", (String) item.get("NoAllotQty"));
                intent.putExtra("supplier_id", (String) item.get("OutStockId"));
                intent.putExtra("stock_id", (String) item.get("InStockId"));
                startActivity(intent);
            }

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

