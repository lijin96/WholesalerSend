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
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.utils.DisplayUtil;
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
 * @ClassName: SelectUpstreamBillno
 * @Description: 选择上游单号
 * @Author: lijin
 * @Date: 2026年8月12日17:35:18
 */
public class SelectUpstreamBillno extends Activity {

    private Context mContext;
    private AccessWeb accWeb;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private ListView listview;
    private EditText et_search;
    private TextView tv_total;
    private TextView txt_monomial;
    private Button btn_seach;
    private ImageButton btn_back;

    private List<Map<String, Object>> list;
    private List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> item;

    int Page=1;

    private String lsv_aim = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_upstreambillno);
        lsv_aim = getIntent().getStringExtra("aim");

        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        mContext = this;
        sysUserInfo=new SysUserInfo(mContext);

        listview = (ListView) findViewById(R.id.listView1);
        tv_total = (TextView) findViewById(R.id.tv_total);
        txt_monomial = (TextView) findViewById(R.id.txt_1);

//        btn_back=findViewById(R.id.btn_back);
//        btn_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });


        list = new ArrayList<Map<String, Object>>();

        listview.setOnItemClickListener(new ListViewItemClik());
        et_search = (EditText) findViewById(R.id.et_search);

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

    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(this, "正在获取数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    if ("P_Dv_InStock_Bill".equals(lsv_aim)){
//                        list = accWeb.GetDownLoadScsPurOrder("普通",et_search.getText().toString(),Page,false);
//                    }else if (lsv_aim.equals("P_Dv_InStock_NoDetail_Bill")){
//                        list = accWeb.GetDownLoadScsPurOrder("普通",et_search.getText().toString(),Page,true);
//                    }else if (lsv_aim.equals("P_Dv_InStock_Lens_Bill")){
//                        list = accWeb.GetDownLoadScsPurOrder("镜片",et_search.getText().toString(),Page,false);
//                    }else if (lsv_aim.equals("P_Dv_InStock_Lens_NoDetail_Bill")){
//                        list = accWeb.GetDownLoadScsPurOrder("镜片",et_search.getText().toString(),Page,false);
//                    }
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();
                    String UrlEndpoint="ScsOtherSys";
                    if (sysUserInfo.getAPIEndpoint().trim().equals("SCS2.0Test")){
                        UrlEndpoint="ScsOtherSysTest";
                    }
                    String tQuery=et_search.getText().toString();
                    //请求的域名地址GET
                    String requestUrl="http://scs.holyes.net/"+UrlEndpoint+"/Bolon/GetBOLONReturnApplyList"+ "?Account="+sysUserInfo.getBmpSendUserCode()+"&startDate=&endDate=&status=&finishMark=&sureMark=0&lno="+tQuery+"&Page=1&Limit=200&year=&brand=";
//                            +sysUserInfo.getAccountSetId();

//                    Log.d("main-", sysUserInfo.getLoginid());
//                    Log.d("main-", requestUrl);
                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法

//                    Log.d("main-", result);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject dataObj = jsonObject.getJSONObject("data");
                    JSONArray listjson =dataObj.optJSONArray("pagingData");
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("applyNo", jsonObject2.get("applyNo"));//申请单号
                        map1.put("brandName", jsonObject2.get("brandName"));//品牌名称
                        map1.put("billTypeName", jsonObject2.get("billTypeName"));//类型
                        String tverifyStatus="未审核";
                        if (jsonObject2.get("verifyStatus").toString().equals("1")) {
                            tverifyStatus="已审核";
                        }
                        map1.put("verifyStatus", tverifyStatus);//审核状态

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


    private class ListViewItemClik implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);

            Intent intent = new Intent();            //有单入库

            intent.putExtra("applyNo", (String) item.get("applyNo"));
            intent.putExtra("brandName", (String) item.get("brandName"));
            intent.putExtra("billTypeName", (String) item.get("billTypeName"));
            intent.putExtra("verifyStatus", (String) item.get("verifyStatus"));

            setResult(RESULT_OK, intent);
            finish();
        }
    }


    public void initListView(List<Map<String, Object>> mList) {
        if (mList.size()==0){
            ShowMessage.Show(SelectUpstreamBillno.this,"暂无数据");
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.list_upstreambillno,
                new String[]{"applyNo", "billTypeName", "brandName", "verifyStatus"}, new int[]{R.id.txt_pur_1, R.id.txt_pur_2, R.id.txt_pur_3, R.id.txt_pur_4});
        listview.setAdapter(adapter);
//        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectUpstreamBillno.this, msg.obj.toString());
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


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (lsv_aim == null || lsv_aim.isEmpty()) {
//                return;
//            }
//            if (requestCode == 0) {
//                Intent intent = null;
////                有单入库
//                if ("P_Dv_InStock_Bill".equals(lsv_aim)) {
//                    //如果入库单的仓库id不存在就需要先去选择仓库
//                    if (item.get("StockId")== null || item.get("StockId").equals("")){
//                        intent = new Intent(mContext, SelectStock.class);
//                    }else{
//                        intent = new Intent(mContext, P_Dv_InStock_Bill.class);
//                    }
//
//                }else if("P_Dv_InStock_NoDetail_Bill".equals(lsv_aim)){
//                    //如果入库单的仓库id不存在就需要先去选择仓库
//                    if (item.get("StockId")== null || item.get("StockId").equals("")){
//                        intent = new Intent(mContext, SelectStock.class);
//                    }else{
//                        intent = new Intent(mContext, P_Dv_InStock_Bill_NoDetail.class);
//                    }
//                }else if ("P_Dv_InStock_Lens_Bill".equals(lsv_aim)) {
//                    //如果入库单的仓库id不存在就需要先去选择仓库
//                    if (item.get("StockId")== null || item.get("StockId").equals("")){
//                        intent = new Intent(mContext, SelectStock.class);
//                    }else{
//                        intent = new Intent(mContext, SelectPurOrderLensDetail.class);
//                    }
//                }else if ("P_Dv_InStock_Lens_NoDetail_Bill".equals(lsv_aim)) {
//                    //如果入库单的仓库id不存在就需要先去选择仓库
//                    if (item.get("StockId")== null || item.get("StockId").equals("")){
//                        intent = new Intent(mContext, SelectStock.class);
//                    }else{
//                        intent = new Intent(mContext, SelectPurOrderLensDetail.class);
//                    }
//                }
//                intent.putExtra("purchecklno", (String) item.get("PurOrderNo"));
//                intent.putExtra("saplno", (String) item.get("SapLno"));
//                intent.putExtra("supplier_name", (String) item.get("SupplierName"));
//                intent.putExtra("stock_name", (String) item.get("StockName"));
//                intent.putExtra("supplier_id", (String) item.get("SupplierId"));
//                intent.putExtra("stock_id", (String) item.get("StockId"));
//                intent.putExtra("aim", lsv_aim);
//                startActivity(intent);
//            }
//
//        }
//
//
//    }

    @Override       //这里是实现了自动更新
    protected void onResume() {
        // TODO Auto-generated method stub
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