package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_fd.SalesReplaceLabCode;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_NoInStock;
import com.example.wholesalersend.entity.ReplaceLabCodePara;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.entity.ScsWebApiInfo;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.SortListMapComparator;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectLensBill
 * @Description: 镜片绑码出库 选择销售单
 * @Author: lijin
 * @Date: 2025/4/10 17:00
 */
public class SelectLensBill extends Activity {

    private Context mContext;
    private AccessWeb accWeb;
    private Thread downloadThread;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_lensbill);
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


        listview.setOnItemClickListener(new ListViewItemClik());

//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");


        btn_seach=findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                DownLoadDataThread("");
            }
        });


        et_search = (EditText) findViewById(R.id.et_search);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        list.clear();
                        DownLoadDataThread( "");
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });
        DownLoadDataThread(getOldDate(-3));
    }

    private class ListViewItemClik implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            ListView listView = (ListView) parent;
            item = (Map<String, Object>) listView.getItemAtPosition(position);


            Intent intent = new Intent(SelectLensBill.this, SelectSureConfirm.class);
            intent.putExtra("title", "确定选择订单：" + item.get("saleOrderNo") + "吗？");
            startActivityForResult(intent, 0);
        }

    }


    // 请求服务
    private void DownLoadDataThread(final String DateNumber) {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        list = new ArrayList<Map<String, Object>>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();

                    Gson gson=new Gson();
                    Date now = new Date();

//                    Log.d("main", "loginid: "+sysUserInfo.getLoginid());
                    String result = request.get("http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/SaleOrder/GetNoCheckOrder", sysUserInfo.getLoginid(),et_search.getText().toString().trim(),DateNumber,"text/plain");//调用我们写的post方法
//                    Log.d("main", "run: "+result);


//                    SalesScsWebApiInfo scsWebApiInfo=gson.fromJson(result,SalesScsWebApiInfo.class);
////                    Log.d("main", "ScsWebApiInfo: "+scsWebApiInfo.toString());
//                    if (scsWebApiInfo.isSuccess()==true){
//
//                        if (gson.toJson(scsWebApiInfo.getData()).length()>0) {
//                            JSONArray listjson = new JSONArray(scsWebApiInfo.getData());
//                            for (int i = 0; i < listjson.length(); i++) {
//                                JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
//                                Map<String, Object> map1 = new HashMap<String, Object>();
//                                map1.put("saleOrderNo", jsonObject2.getString("obGoodsNo"));
//                                map1.put("saleOrderDate", jsonObject2.getString("createDate"));
//                                map1.put("customerName", jsonObject2.getString("customerName"));
//                                map1.put("custSysCode", jsonObject2.getString("custSysCode"));
//                                map1.put("saleOrderNum", jsonObject2.getInt("saleOrderNum"));
//                                list.add(map1);
//                            }
//                        }

                    SalesScsWebApiInfo scsWebApiInfo = gson.fromJson(result, SalesScsWebApiInfo.class);
                    if (scsWebApiInfo.isSuccess()) {
                        JsonElement dataEl = gson.toJsonTree(scsWebApiInfo.getData());
                        if (dataEl != null && dataEl.isJsonArray()) {
                            JsonArray arr = dataEl.getAsJsonArray();
                            for (JsonElement itemEl : arr) {
                                JsonObject obj = itemEl.getAsJsonObject();
                                Map<String, Object> map1 = new HashMap<>();
                                map1.put("saleOrderNo", obj.get("obGoodsNo").getAsString());
                                map1.put("saleOrderDate", obj.get("createDate").getAsString());
                                map1.put("customerName", obj.get("customerName").getAsString());
                                map1.put("custSysCode", obj.get("custSysCode").getAsString());
                                map1.put("saleOrderNum", obj.get("saleOrderNum").getAsInt());
                                list.add(map1);
                            }
                        }

                        ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess,"ok");
                    }else{
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,scsWebApiInfo.getMessage());
                    }

                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                            e.getMessage());
                }
            }
        }).start();
    }

    public static String getOldDate(int distanceDay) {
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) + distanceDay);
        Date endDate = null;
        try {
            endDate = dft.parse(dft.format(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dft.format(endDate);
    }


    public void initListView(List<Map<String, Object>> mList) {
        adapter = new SimpleAdapter(this, mList, R.layout.list_lensbill,
                new String[]{"saleOrderNo", "saleOrderDate", "customerName", "saleOrderNum"}, new int[]{R.id.txt_pur_1, R.id.txt_pur_2, R.id.txt_pur_3, R.id.txt_pur_4});
        listview.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(SelectLensBill.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    if (list.size() > 0) {
                        initListView(list);
                    }else{
                        if (adapter!=null) {
                            adapter.notifyDataSetChanged();
                        }
                        ShowMessage.Show(SelectLensBill.this, "暂无数据");
                    }
                    break;
                case ShowMessage.HandScanSuccess:
                    MyProgressDialog.close();
                    if (CustStoreRelate.size()>0) {
                        showBrandDialog();
                    }else{
                        Intent intent = new Intent(SelectLensBill.this, SelectSureConfirm.class);
                        if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_D_Bill") || lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_Bill") || lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_Bill_Detail") || lsv_aim.equals("P_Dv_ReturnedPurchase_Z_D_Bill_Detail") || lsv_aim.equals("P_Dv_L_Return_Z_Bill")) {
                            intent.putExtra("title", "确定选择退货配货单：" + item.get("purchecklno") + "的产品吗？");
                        } else {
                            intent.putExtra("title", "确定选择订单：" + item.get("PeiGoodLno") + "的产品吗？");
                        }
                        startActivityForResult(intent, 0);
                    }

//                    if (IsBindCCS){
//                        //需要绑定CCS客户或者门店
//                        if(!IsBindCCScust){
//                            //需要绑定CCS客户
//                            Intent intent=new Intent(mContext,SelectCCScustomer.class);
//
//                            intent.putExtra("Brand_code", BrandingCode);
//                            intent.putExtra("Agent_Code", AgentCode);
//                            intent.putExtra("IsBindCCSstore", IsBindCCSstore);
//                            intent.putExtra("trader_sysid",TraderSysId);
//                            intent.putExtra("store_sysid", StoreSysId);
//
//                            intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                            intent.putExtra("saplno", (String) item.get("SapLno"));
//                            intent.putExtra("supplier_name", (String) item.get("AgentName"));
//                            intent.putExtra("stock_name", (String) item.get("StockName"));
//                            intent.putExtra("supplier_id", (String) item.get("AgentId"));
//                            intent.putExtra("stock_id", (String) item.get("StockId"));
//                            intent.putExtra("store_id", (String) item.get("StoreId"));
//                            intent.putExtra("aim", lsv_aim);
//
//                            startActivity(intent);
//
//                        }else if (!IsBindCCSstore){
//                            //需要绑定CCS门店
//                            Intent intent=new Intent(mContext,SelectCCSstore.class);
//
//                            intent.putExtra("Brand_code", BrandingCode);
//                            intent.putExtra("Agent_Code", AgentCode);
//                            intent.putExtra("CCSCuts_Code", BrandingCustCode);
//                            intent.putExtra("trader_sysid",TraderSysId);
//                            intent.putExtra("store_sysid", StoreSysId);
//
//                            intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                            intent.putExtra("saplno", (String) item.get("SapLno"));
//                            intent.putExtra("supplier_name", (String) item.get("AgentName"));
//                            intent.putExtra("stock_name", (String) item.get("StockName"));
//                            intent.putExtra("supplier_id", (String) item.get("AgentId"));
//                            intent.putExtra("stock_id", (String) item.get("StockId"));
//                            intent.putExtra("store_id", (String) item.get("StoreId"));
//                            intent.putExtra("aim", lsv_aim);
//                            startActivity(intent);
//                        }
//                    }else{
//
//                        Intent intent = null;
//                        if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                            //有单有入库有明细分店发货
//                            intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
//                        }
//                        else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                            //有单有入库无明细分店发货
//                            intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                        }
//                        else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                            //镜片有单有入库分店发货
//                            intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
//                        }
//
//                        intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
//                        intent.putExtra("saplno", (String) item.get("SapLno"));
//                        intent.putExtra("supplier_name", (String) item.get("AgentName"));
//                        intent.putExtra("stock_name", (String) item.get("StockName"));
//                        intent.putExtra("supplier_id", (String) item.get("AgentId"));
//                        intent.putExtra("stock_id", (String) item.get("StockId"));
//                        intent.putExtra("store_id", (String) item.get("StoreId"));
//
//
//                        intent.putExtra("aim", lsv_aim);
//
//                        startActivity(intent);
//                    }
                    break;
                default:
                    break;
            }

            MyProgressDialog.close();
        }
    }



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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (lsv_aim == null || lsv_aim.isEmpty()) {
                return;
            }
            if (requestCode == 0) {
                Intent intent = new Intent(SelectLensBill.this, SalesReplaceLabCode.class);


                intent.putExtra("saleOrderNo", (String) item.get("saleOrderNo"));
                intent.putExtra("saleOrderDate", (String) item.get("saleOrderDate"));
                intent.putExtra("customerName", (String) item.get("customerName"));
                intent.putExtra("custSysCode", (String) item.get("custSysCode"));
                intent.putExtra("saleOrderNum", (String) item.get("saleOrderNum").toString());

                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
            }

        }
    }

    //当前单据是否存在多品牌
    private void showBrandDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */

        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_bindbrand_list,null);
        ListView BindBrandListView=selectview.findViewById(R.id.List_BindBrand);
        SimpleAdapter adapter = new SimpleAdapter(mContext,CustStoreRelate, R.layout.new_list_select_bindbrand, new String[]{
                "BrandingCode", "BrandName","AgentCode"}, new int[]{R.id.txt_list1, R.id.txt_list2,R.id.txt_list3});
        BindBrandListView.setAdapter(adapter);

        final Dialog normalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
//        normalDialog.setTitle("绑定品牌提示");
                .setView(selectview)
                .setPositiveButton("前往扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent scsintent = null;
                        if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                            //有单有入库有明细分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                        }
                        else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                            //有单有入库无明细分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                        }
                        else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                            //镜片有单有入库分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
                        }

                        scsintent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
                        scsintent.putExtra("saplno", (String) item.get("SapLno"));
                        scsintent.putExtra("supplier_name", (String) item.get("AgentName"));
                        scsintent.putExtra("stock_name", (String) item.get("StockName"));
                        scsintent.putExtra("supplier_id", (String) item.get("AgentId"));
                        scsintent.putExtra("stock_id", (String) item.get("StockId"));
                        scsintent.putExtra("store_id", (String) item.get("StoreId"));


                        scsintent.putExtra("aim", lsv_aim);
                        startActivity(scsintent);
                    }
                }).create();

        normalDialog.show();

        if (normalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = normalDialog.getWindow().getAttributes();
            lp.width = 800; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            normalDialog.getWindow().setAttributes(lp);
        }

        BindBrandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(mContext,CustStoreRelate.get(i).get("BrandName").toString(),Toast.LENGTH_SHORT).show();
//                Log.d("main",CustStoreRelate.get(i).toString());

                IsBindCCS = (Boolean) CustStoreRelate.get(i).get("NeedBind");
                IsBindCCScust = (Boolean)CustStoreRelate.get(i).get("CustBind");
                IsBindCCSstore = (Boolean) CustStoreRelate.get(i).get("StoreBind");
                IsSendToStore = (Boolean) CustStoreRelate.get(i).get("IsSendToStore");

                BrandingCode= (String) CustStoreRelate.get(i).get("BrandingCode");
                AgentCode= (String) CustStoreRelate.get(i).get("AgentCode");
                BrandingCustCode= (String) CustStoreRelate.get(i).get("BrandingCustCode");



                TraderSysId= (String) CustStoreRelate.get(i).get("CustSysCode");
                StoreSysId=(String) CustStoreRelate.get(i).get("StoreSysCode");

                TraderAlias_name=(String) CustStoreRelate.get(i).get("TraderAlias");
                Storealias_name=(String) CustStoreRelate.get(i).get("StoreAlias");

                if(!IsBindCCScust){
                    //需要绑定CCS客户
                    Intent intent=new Intent(mContext,SelectCCScustomer.class);

                    intent.putExtra("Brand_code", BrandingCode);
                    intent.putExtra("Agent_Code", AgentCode);
                    intent.putExtra("trader_sysid",TraderSysId);
                    intent.putExtra("store_sysid", StoreSysId);

                    if (IsSendToStore) {
                        intent.putExtra("IsBindCCSstore", IsBindCCSstore);
                    }else{
                        intent.putExtra("IsBindCCSstore", false);
                    }

                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);

                    intent.putExtra("scanBillNo", "");
                    intent.putExtra("scanBillNum", "");

                    intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
                    intent.putExtra("saplno", (String) item.get("SapLno"));
                    intent.putExtra("supplier_name", (String) item.get("AgentName"));
                    intent.putExtra("stock_name", (String) item.get("StockName"));
                    intent.putExtra("supplier_id", (String) item.get("AgentId"));
                    intent.putExtra("stock_id", (String) item.get("StockId"));
                    intent.putExtra("store_id", (String) item.get("StoreId"));
                    intent.putExtra("aim", lsv_aim);

                    startActivity(intent);

                }else if (IsSendToStore) {
                    //需要绑定CCS门店
                    if (!IsBindCCSstore) {
                        //当前未绑定CCS门店
                        Intent intent = new Intent(mContext, SelectCCSstore.class);

                        intent.putExtra("Brand_code", BrandingCode);
                        intent.putExtra("Agent_Code", AgentCode);
                        intent.putExtra("CCSCuts_Code", BrandingCustCode);
                        intent.putExtra("trader_sysid", TraderSysId);
                        intent.putExtra("store_sysid", StoreSysId);

                        intent.putExtra("TraderAlias_name", TraderAlias_name);
                        intent.putExtra("storealias_name", Storealias_name);

                        intent.putExtra("scanBillNo", "");
                        intent.putExtra("scanBillNum", "");

                        intent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
                        intent.putExtra("saplno", (String) item.get("SapLno"));
                        intent.putExtra("supplier_name", (String) item.get("AgentName"));
                        intent.putExtra("stock_name", (String) item.get("StockName"));
                        intent.putExtra("supplier_id", (String) item.get("AgentId"));
                        intent.putExtra("stock_id", (String) item.get("StockId"));
                        intent.putExtra("store_id", (String) item.get("StoreId"));
                        intent.putExtra("aim", lsv_aim);
                        startActivity(intent);
                    }else{
                        Intent scsintent = null;
                        if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                            //有单有入库有明细分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                        }
                        else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                            //有单有入库无明细分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                        }
                        else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                            //镜片有单有入库分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
                        }

                        scsintent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
                        scsintent.putExtra("saplno", (String) item.get("SapLno"));
                        scsintent.putExtra("supplier_name", (String) item.get("AgentName"));
                        scsintent.putExtra("stock_name", (String) item.get("StockName"));
                        scsintent.putExtra("supplier_id", (String) item.get("AgentId"));
                        scsintent.putExtra("stock_id", (String) item.get("StockId"));
                        scsintent.putExtra("store_id", (String) item.get("StoreId"));


                        scsintent.putExtra("aim", lsv_aim);
                        startActivity(scsintent);
                    }
                }else{
                    // TODO Auto-generated method stub
                    Intent scsintent = null;
                    if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                        //有单有入库有明细分店发货
                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                    }
                    else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                        //有单有入库无明细分店发货
                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                    }
                    else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                        //镜片有单有入库分店发货
                        scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
                    }

                    scsintent.putExtra("purchecklno", (String) item.get("PeiGoodLno"));
                    scsintent.putExtra("saplno", (String) item.get("SapLno"));
                    scsintent.putExtra("supplier_name", (String) item.get("AgentName"));
                    scsintent.putExtra("stock_name", (String) item.get("StockName"));
                    scsintent.putExtra("supplier_id", (String) item.get("AgentId"));
                    scsintent.putExtra("stock_id", (String) item.get("StockId"));
                    scsintent.putExtra("store_id", (String) item.get("StoreId"));


                    scsintent.putExtra("aim", lsv_aim);
                    startActivity(scsintent);
                }
                normalDialog.dismiss();
            }
        });

//        normalDialog.setPositiveButton("确定",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//        normalDialog.setNegativeButton("关闭",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //...To-do
//                    }
//                });
        // 显示

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



