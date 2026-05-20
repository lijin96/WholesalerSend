package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.instock_in.Holyes_Dv_Box_InStock_NoBill;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_NoBill;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_PackBox_List_NoBill;
import com.example.wholesalersend.activity.instock_in.P_Dv_Lens_InStock_NoBill;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.adapter.ScanOrderAdapter;
import com.example.wholesalersend.adapter.SupplierInforAdapter;
import com.example.wholesalersend.adapter.UnFillBoxAdapter;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.entity.ScanOrder;
import com.example.wholesalersend.entity.SupplierInfor;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.SortListMapComparator;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.NumberUtils;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectSupplier
 * @Description: 选择供应商
 * @Author: lijin
 * @Date: 2023/10/26 11:01
 */
public class SelectSupplier extends Activity {
    private Context mContext;
    private LinearLayoutManager mLayoutManager;

    private List<SupplierInfor> Requestedlist = new ArrayList<SupplierInfor>();

    private Button btn_seach;
    private EditText et_query_supplier;
    private RecyclerView RecyclerView_supplier;

    private SupplierInforAdapter supplierInforAdapter;

    private int lastVisibleItem = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int Page = 1;

    private AccessWeb accWeb;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private String lsv_aim = "";

    private SupplierInfor ItemInfor;

    private final int Lic_SelectSure = 3;

    List<ScanOrder> scanOrderlist = new ArrayList<ScanOrder>();

    /** 未完成盒标列表，供有数据时弹窗显示 */
    private List<Map<String, Object>> packBoxListForDialog = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_supplier);

        Intent getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");

        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo=new SysUserInfo(mContext);

        btn_seach = findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page = 1;
                supplierInforAdapter.resetDatas();
                DownLoadDataThread();
            }
        });

        et_query_supplier = findViewById(R.id.et_query_supplier);
        et_query_supplier.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Page = 1;
                        supplierInforAdapter.resetDatas();
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });

        RecyclerView_supplier = findViewById(R.id.RecyclerView_supplier);
        initRecyclerView();
        DownLoadDataThread();

    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(this, "正在获取数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ADevicesManager.isSCSNewInterface){
                        Requestedlist = accWeb.NewSCSGetDownLoadSupplierInfor(et_query_supplier.getText().toString(), Page);
                    }else {
                        Requestedlist = accWeb.SCSGetDownLoadSupplierInfor(et_query_supplier.getText().toString(), Page);
                    }
//                    Log.d("main",Requestedlist.toString());
                    Collections.sort(Requestedlist, new SortListSupplierComparator());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private void ShowScanOrderList() {

        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_scanorder_list,
                null);
        ListView ScanOrderListView = selectview.findViewById(R.id.List_ScanOrder);
        ScanOrderAdapter scanOrderAdapter = new ScanOrderAdapter(mContext, scanOrderlist);
        ScanOrderListView.setAdapter(scanOrderAdapter);

        TextView tv_new_scanorder = selectview.findViewById(R.id.new_scanorder);

        final Dialog alertDialog6 = new AlertDialog.Builder(mContext,
                R.style.Base_Theme_AppCompat_Light_Dialog).setTitle("选择要扫描的单号").setIcon(R.drawable.scs).setView(selectview).setPositiveButton("新开扫描单", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(mContext, SelectStock.class);
//                if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                    intent=new Intent(mContext, P_Dv_InStock_Packing.class);
//                }
                intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        }).create();
        alertDialog6.show();

        if (alertDialog6.getWindow() != null) {
            WindowManager.LayoutParams lp = alertDialog6.getWindow().getAttributes();
            if (sysUserInfo.getOldVersion().equals("T8")){
                lp.width = 600;
            }else {
                lp.width = 800; // 宽度，可根据屏幕宽度进行计算
            }
            lp.gravity = Gravity.CENTER;
            alertDialog6.getWindow().setAttributes(lp);
        }


        tv_new_scanorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SelectStock.class);
//                if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                    intent=new Intent(mContext, P_Dv_InStock_Packing.class);
//                }
                intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
                alertDialog6.dismiss();
            }
        });
        ScanOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = null;
                if (lsv_aim.equals("P_Dv_Lens_InStock_NoBill")) {
                    intent = new Intent(mContext, P_Dv_Lens_InStock_NoBill.class);
                }else if (lsv_aim.equals("Holyes_Dv_Box_InStock_NoBill")){
                    //无单盒标入库
                    intent=new Intent(mContext, Holyes_Dv_Box_InStock_NoBill.class);
                    intent.putExtra("supplier_syscode", ItemInfor.getSuppSysCode());
                } else {
                    intent = new Intent(mContext, P_Dv_InStock_NoBill.class);
                }
//                Intent intent = new Intent();
                intent.putExtra("supplier_id", ItemInfor.getSupplierId());
                intent.putExtra("supplier_name", ItemInfor.getSupplierName());
                intent.putExtra("stock_id", scanOrderlist.get(i).getStockCode());
                intent.putExtra("stock_name", scanOrderlist.get(i).getStockName());
                intent.putExtra("scanBillNo", scanOrderlist.get(i).getBillNo());
                intent.putExtra("scanBillNum", scanOrderlist.get(i).getBillNum());

                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 显示未完成盒标列表弹窗：列表 + 新建盒标按钮
     */
    private void ShowUnFillBoxList() {
        View selectView = LayoutInflater.from(mContext).inflate(R.layout.select_unfill_box_list, null);
        ListView listView = selectView.findViewById(R.id.List_UnFillBox);
        UnFillBoxAdapter adapter = new UnFillBoxAdapter(mContext, packBoxListForDialog);
        listView.setAdapter(adapter);

        TextView btnNewBox = selectView.findViewById(R.id.btn_new_box);

        final Dialog alertDialog = new AlertDialog.Builder(mContext,
                R.style.Base_Theme_AppCompat_Light_Dialog)
                .setTitle("选择未完成的盒标或新建盒标")
                .setIcon(R.mipmap.ic_launcher)
                .setView(selectView)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        alertDialog.show();

        if (alertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
            lp.gravity = Gravity.CENTER;
            alertDialog.getWindow().setAttributes(lp);
        }

        btnNewBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent();
//                if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                    intent = new Intent(mContext, SelectStock.class);
//                }else if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                    intent = new Intent(mContext, P_Dv_InStock_Packing.class);
//                }
                intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> item = packBoxListForDialog.get(i);
                Intent intent =new Intent();
//                if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                    intent = new Intent(mContext, P_Dv_InStock_PackBox_List_NoBill.class);
                    intent.putExtra("StockName",String.valueOf(item.get("StockName")));
                    intent.putExtra("StockCode", String.valueOf(item.get("StockCode")));
                    intent.putExtra("StockSysCode", String.valueOf(item.get("StockSysCode")));
//                }
//                else if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                    intent = new Intent(mContext, P_Dv_InStock_Packing.class);
//                }
//                Intent intent = new Intent(mContext, P_Dv_InStock_Packing.class);
                intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                intent.putExtra("aim", lsv_aim);
                intent.putExtra("BoxNo", String.valueOf(item.get("BoxNo")));
                intent.putExtra("PackingNum", String.valueOf(item.get("SetNum")));
                intent.putExtra("BoxActNum", String.valueOf(item.get("ActNum")));
                intent.putExtra("GoodsId", String.valueOf(item.get("GoodsId")));
                intent.putExtra("Modelm", String.valueOf(item.get("Modelm")));
                intent.putExtra("Colors", String.valueOf(item.get("Colors")));
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
    }

    // 下载扫描单号 请求服务
    private void DownLoadScanOrder(final String tDeSysCode, final String tStoreSysCode,
                                   final String tScanType, final String tOrderType) {
        scanOrderlist = new ArrayList<ScanOrder>();
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    scanOrderlist = accWeb.GetCurrentScanBill(tDeSysCode, tStoreSysCode,
                            tScanType, tOrderType);
//                    Log.d("main",scanOrderlist.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandFailed, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    if (lsv_aim == null || lsv_aim.isEmpty()) {
                        return;
                    }
                    Intent intent = new Intent(this, SelectStock.class);
                    if (lsv_aim.equals("Holyes_Dv_Box_InStock_NoBill")){
                        //无单盒标入库
                        intent=new Intent(mContext, Holyes_Dv_Box_InStock_NoBill.class);
                    }

                    intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                    intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                    intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                    intent.putExtra("aim", lsv_aim);
                    startActivity(intent);
                    break;
                default:
                    break;
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initRecyclerView() {
        supplierInforAdapter = new SupplierInforAdapter(Requestedlist, mContext,
                Requestedlist.size() > 0 ? true : false);
        mLayoutManager = new LinearLayoutManager(mContext);
        RecyclerView_supplier.setLayoutManager(mLayoutManager);
        RecyclerView_supplier.setAdapter(supplierInforAdapter);


        supplierInforAdapter.setClick(new SupplierInforAdapter.MyClick() {
            @Override
            public void click(View v) {
                if (v != null) {
                    //返回的是“当前Item对应的View在当前列表可视范围内的index”。
//                    int index1 = RecyclerView_supplier.indexOfChild(v);
                    //返回的是“当前Item对应的View在Adapter数据列表中的位置”。
                    int index2 = RecyclerView_supplier.getChildAdapterPosition(v);
//                    //返回的是“当前Item对应的View在整个视图列表中的index”。
//                    int index3 = RecyclerView_supplier.getChildLayoutPosition(v);
//                    ItemInfor = supplierInforAdapter.getDataList().get(index2);
//                    if (lsv_aim.equals("P_Dv_BrandCode_InStock")||lsv_aim.equals("P_Dv_MendLable_Z")) {
//                        Intent sureIntent = new Intent(SelectSupplier.this, SelectSureConfirm.class);
//                        sureIntent.putExtra("title",
//                                "确定选择供应商：" + (String) ItemInfor.getSupplierName() + "？");
//                        startActivityForResult(sureIntent, Lic_SelectSure);
//                    }else if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
//                        GetUnFillBox(ItemInfor.getSupplierId());
//                    } else {
//                        String tordertype = "普通";
//                        if (lsv_aim.equals("P_Dv_Lens_InStock_NoBill")) {
//                            tordertype = "镜片";
//                        }
//                        DownLoadScanOrder(ItemInfor.getSuppSysCode(), "", "InStock", tordertype);
//                    }
                    ItemInfor = supplierInforAdapter.getDataList().get(index2);
                    if (lsv_aim.equals("P_Dv_BrandCode_InStock")||lsv_aim.equals("P_Dv_MendLable_Z")||lsv_aim.equals("P_Dv_InStock_Packing")||lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                        if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                            Holyes_Dv_Factory_GetUnFillBox(ItemInfor.getSupplierId());
                        }else if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                            GetUnFillBox(ItemInfor.getSupplierId());
                        }else{
                            Intent intent = new Intent(mContext, SelectStock.class);
//                            if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                                intent=new Intent(mContext, P_Dv_InStock_Packing.class);
//                            }
                            intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                            intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                            intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                            intent.putExtra("aim", lsv_aim);
                            startActivity(intent);
                        }

                    } else {
                        String tordertype = "普通";
                        if (lsv_aim.equals("P_Dv_Lens_InStock_NoBill")) {
                            tordertype = "镜片";
                        }
                        DownLoadScanOrder(ItemInfor.getSuppSysCode(), "", "InStock", tordertype);
                    }
                }
            }
        });

        RecyclerView_supplier.setItemAnimator(new DefaultItemAnimator());

        RecyclerView_supplier.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (supplierInforAdapter.isFadeTips() == false && lastVisibleItem + 1 == supplierInforAdapter.getItemCount()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                updateRecyclerView(supplierInforAdapter.getRealLastPosition(),
//                                supplierInforAdapter.getRealLastPosition() + 50);
                            }
                        }, 500);
                    }

                    if (supplierInforAdapter.isFadeTips() == true && lastVisibleItem + 2 == supplierInforAdapter.getItemCount()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                updateRecyclerView(supplierInforAdapter.getRealLastPosition(),
//                                supplierInforAdapter.getRealLastPosition() + 50);
                                DownLoadDataThread();
                            }
                        }, 500);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

            }
        });
    }

//    private List<SupplierInfor> getDatas(final int firstIndex, final int lastIndex) {
//        List<SupplierInfor> resList = new ArrayList<>();
//        for (int i = firstIndex; i < lastIndex; i++) {
//            if (i < list.size()) {
//                resList.add(list.get(i));
//            }
//        }
//        return resList;
//    }

    private void updateRecyclerView(int fromIndex, int toIndex) {
        List<SupplierInfor> newDatas = Requestedlist;
//        Log.d("main","数据长度+"+newDatas.toString());
        if (newDatas.size() > 0) {
            Page = Page + 1;
            supplierInforAdapter.updateList(newDatas, true);
        } else {
            supplierInforAdapter.updateList(null, false);
        }
    }

    //品牌商获取上一次未完成的任务
    public void GetUnFillBox(String tSupplierid) {
        MyProgressDialog.show(mContext, "正在获取上一次任务", true, true);
        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();

                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetUnFillBox"+ "?SoCompId="+tSupplierid+"&OaSuserId="+sysUserInfo.getUserid();
//                    Log.d("main", requestUrl);
                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
//                    Log.d("main", result);
                    SalesScsWebApiInfo scsWebApiInfo=gson.fromJson(result,SalesScsWebApiInfo.class);
                    if(scsWebApiInfo.isSuccess()) { // 假设有isSuccess()方法

//                        JSONArray listjson = new JSONArray(scsWebApiInfo.getData());
                        List<Map<String, Object>> PackBoxlist = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < scsWebApiInfo.getData().size(); i++) {
                            Map<String, Object> map = scsWebApiInfo.getData().get(i);
                            Map<String, Object> map1 = new HashMap<String, Object>();
                            map1.put("BoxNo", map.get("boxNo"));
                            map1.put("SetNum", NumberUtils.toIntString(map.get("setNum")));
                            map1.put("ActNum",NumberUtils.toIntString(map.get("actNum")));
                            map1.put("GoodsId", map.get("goodsId"));
                            map1.put("Modelm",map.get("modelm"));
                            map1.put("Colors", map.get("colors"));
                            map1.put("StockName",map.get("stockName"));
                            map1.put("StockCode", map.get("stockCode"));
                            map1.put("StockSysCode", map.get("stockSysCode"));
                            PackBoxlist.add(map1);
                        }
                        if (PackBoxlist.size() > 0) {
                            packBoxListForDialog.clear();
                            packBoxListForDialog.addAll(PackBoxlist);
                            ShowMessage.ShowMsg(hand, 5, "success");
                        } else {
                            ShowMessage.ShowMsg(hand, 7, "暂无未完成的任务");
                        }
                    }else{
                        ShowMessage.ShowMsg(hand, 7,scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, 7, e.getMessage());
                }
            }
        }).start();
    }



    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    updateRecyclerView(0, 0);
                    break;
                case ShowMessage.HandScanSuccess://扫描单号列表返回数据
                    MyProgressDialog.close();
                    if (scanOrderlist.size() > 0) {
                        ShowScanOrderList();
                    } else {
//                        Intent sureIntent = new Intent(SelectSupplier.this,
//                                SelectSureConfirm.class);
//                        sureIntent.putExtra("title",
//                                "确定选择供应商：" + (String) ItemInfor.getSupplierName() + "？");
//                        startActivityForResult(sureIntent, Lic_SelectSure);
                        Intent intent = new Intent(mContext, SelectStock.class);
//                        if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                            intent=new Intent(mContext, P_Dv_InStock_Packing.class);
//                        }

                        intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                        intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                        intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                        intent.putExtra("aim", lsv_aim);
                        startActivity(intent);
                    }
                    break;
                case ShowMessage.HandFailed: //扫描单号返回报错
                    MyProgressDialog.close();
//                    Intent sureIntent = new Intent(SelectSupplier.this, SelectSureConfirm.class);
//                    sureIntent.putExtra("title",
//                            "确定选择供应商：" + (String) ItemInfor.getSupplierName() + "？");
//                    startActivityForResult(sureIntent, Lic_SelectSure);
                    Intent intent = new Intent(mContext, SelectStock.class);
//                    if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                        intent=new Intent(mContext, P_Dv_InStock_Packing.class);
//                    }
                    intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                    intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                    intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                    intent.putExtra("aim", lsv_aim);
                    startActivity(intent);
                    break;
                case 5:
                    //装盒入库：有未完成的盒标任务，弹出列表界面供选择或新建盒标
                    MyProgressDialog.close();
                    if (packBoxListForDialog.size()==1){
                        //如果接口当前只显示一条未完成的装盒入库记录，就默认跳转过去
                        Map<String, Object> item = packBoxListForDialog.get(0);
                        Intent packBoxIntent =new Intent();
//                        if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                            packBoxIntent = new Intent(mContext, P_Dv_InStock_PackBox_List_NoBill.class);
                            packBoxIntent.putExtra("StockName",String.valueOf(item.get("StockName")));
                            packBoxIntent.putExtra("StockCode", String.valueOf(item.get("StockCode")));
                            packBoxIntent.putExtra("StockSysCode", String.valueOf(item.get("StockSysCode")));
//                        }
//                        else if (lsv_aim.equals("P_Dv_InStock_Packing")){
//                            packBoxIntent = new Intent(mContext, P_Dv_InStock_Packing.class);
//                        }
                        packBoxIntent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                        packBoxIntent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                        packBoxIntent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                        packBoxIntent.putExtra("aim", lsv_aim);
                        packBoxIntent.putExtra("BoxNo", String.valueOf(item.get("BoxNo")));
                        packBoxIntent.putExtra("PackingNum", String.valueOf(item.get("SetNum")));
                        packBoxIntent.putExtra("BoxActNum", String.valueOf(item.get("ActNum")));
                        packBoxIntent.putExtra("GoodsId", String.valueOf(item.get("GoodsId")));
                        packBoxIntent.putExtra("Modelm", String.valueOf(item.get("Modelm")));
                        packBoxIntent.putExtra("Colors", String.valueOf(item.get("Colors")));
                        startActivity(packBoxIntent);
                    }else{
                        ShowUnFillBoxList();
                    }
                    break;
                case 7:
                    //装盒入库供应商没有未完成的盒标任务就直接跳转
                    MyProgressDialog.close();
                    Intent boxintent =new Intent();
//                    if (lsv_aim.equals("P_Dv_InStock_PackBox_List_NoBill")){
                        boxintent = new Intent(mContext, SelectStock.class);
//                    }
                    boxintent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
                    boxintent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
                    boxintent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
                    boxintent.putExtra("aim", lsv_aim);
                    startActivity(boxintent);
                    break;
                default:
                    break;
            }

            MyProgressDialog.close();
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

class SortListSupplierComparator implements Comparator<SupplierInfor> {

    @Override
    public int compare(SupplierInfor map1, SupplierInfor map2) {

        //供应商代号不相同比较供应商代号
        if (!map1.getSupplierId().toString().equals(map2.getSupplierId().toString())) {
            return map1.getSupplierId().toString().compareToIgnoreCase(map2.getSupplierId().toString());
        }
        //供应商代号相同比较供应商名称
        else {
            return map1.getSupplierName().toString().compareToIgnoreCase(map2.getSupplierName().toString());
        }
    }

}

