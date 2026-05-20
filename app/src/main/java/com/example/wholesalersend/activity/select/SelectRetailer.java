package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCompanyRetailer
 * @Description: 代理商版本 选择零售商
 * @Author: lijin
 * @Date: 2026年4月10日15:53:28
 */
public class SelectRetailer extends Activity {


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

    private List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    private String lsv_etStr="";

    private final int Lic_SelectSure = 3;

    private String lsv_aim = "",company_id="";

    private Button btn_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.select_company_retailer);
        mContext = this;
        hand = new handShowMsg();
        accWeb=new AccessWeb(mContext);
        sysUserInfo = new SysUserInfo(mContext);

        lsv_aim = getIntent().getStringExtra("aim");
        company_id=getIntent().getStringExtra("agentId");

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
        listview = (ListView) findViewById(R.id.lst_company);
        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);

        lsv_aim = getIntent().getStringExtra("aim");

        listview.setOnItemClickListener(new ListViewItemClickListener());

        btn_search=findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dList.clear();
                DownLoadDataThread();
            }
        });

        DownLoadDataThread();

    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        dList = new ArrayList<Map<String, Object>>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Gson gson=new Gson();
//                    ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
                    HashMap<Object, Object> map = new HashMap<Object, Object>();
                    map.put("Query", et_search.getText().toString().trim());
                    map.put("AtrribAreaId", company_id);
                    map.put("ProviceName", "");
                    map.put("CityName", "");
                    map.put("County", "");
                    map.put("CustomerName", "");
                    map.put("CustomerCode", "");
                    map.put("SaleName", "");
                    map.put("Mobile", "");
                    map.put("Tel", "");
                    map.put("Link", "");
                    map.put("Sort", "");
                    map.put("PriAccountId", "");
                    map.put("TradeState", "");
                    map.put("CreateDate", "");
                    map.put("IsDelSelfAccountSet", false);
                    map.put("CustomerKind", "");
                    map.put("Page", 1);
                    map.put("Limit", 200);

//                    para.add(map);
                    String tListData = accWeb.PostAPIStringInterface("AgentCustomerInfor/GetAgentCustomerInfor",gson.toJson(map));
//                    Log.d("main", tListData);
                    JSONObject jsonObject = new JSONObject(tListData);
                    JSONArray listjson =jsonObject.optJSONArray("pagingData");
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("customerName", jsonObject2.get("customerName"));//名称
                        map1.put("link", jsonObject2.get("link"));//联系人
                        map1.put("tel", jsonObject2.get("tel"));//电话
                        map1.put("mobile", jsonObject2.get("mobile"));
                        map1.put("corpAddr", jsonObject2.get("corpAddr"));//公司地址
                        map1.put("custSysCode", jsonObject2.get("custSysCode"));//系统代号
                        map1.put("customerCode", jsonObject2.get("customerCode"));

                        dList.add(map1);
                    }

                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }



    public void initListView() {
        adapter = new SimpleAdapter(this, dList, R.layout.lst_company_item,
                new String[]{"customerName", "link", "tel", "customerCode", "corpAddr",
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
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess: // 返回成功
                    MyProgressDialog.close();
                    if (dList.size()>0){
                        initListView();
                    }else{
                        ShowMessage.Show(mContext, "未查询到数据");
                    }
                    break;
                default:
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;

            }
            MyProgressDialog.close();
            super.handleMessage(msg);
        }
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
            Intent intent = new Intent();
            intent.putExtra("company_na", item.get("customerName").toString());
            intent.putExtra("company_id", item.get("customerCode").toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

