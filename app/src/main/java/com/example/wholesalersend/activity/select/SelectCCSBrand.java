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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Lens_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.ccsother.P_Dv_CCSBarcodeStatusWrite;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.entity.SupplierInfor;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCCSBrand
 * @Description: 选择CCS品牌
 * @Author: lijin
 * @Date: 2023/10/26 11:01
 */
public class SelectCCSBrand extends Activity {
    private Context mContext;
    private LinearLayoutManager mLayoutManager;

//    private List<SupplierInfor> Requestedlist=new ArrayList<SupplierInfor>();

    private Button btn_seach;
    private EditText et_query_supplier;
    private ListView list_brand;

    private List<Map<String, Object>> brandlist = new ArrayList<Map<String, Object>>();

    private int lastVisibleItem = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int Page = 1;

    private AccessWeb accWeb;
    private Handler hand;

    private String lsv_aim = "",TraderSysId="",StoreSysId="",RetailId="",RetailName="",CompanyName="",CompanyId="";//分店发货接收参数

    private String  company_id = "", company_name = "", stock_id = "",retail_id="",retail_name="", stock_name="",TraderAlias_name="",Storealias_name="";//分店退货接收参数

    private String BrandingCode="",AgentCode="",BrandingCustCode="",BrandingStoreCode="";

    private String supplier_id="",supplier_name="",purchecklno="",saplno="",store_id="";//有单有入库有明细分店发货  有单有入库无明细分店发货  有单有入库镜片发货


    private String scanBillNo="",scanBillNum="";//扫描单号，扫描数量

    private String CCSCustomerName="",CCSStoreName="";//绑定的客户名称，绑定的门店名称

    private SupplierInfor ItemInfor;

    private SimpleAdapter customeradapter;//CCS客户适配器
    private List<Map<String, Object>> customerlist = new ArrayList<Map<String, Object>>();//CCS客户列表

    private SimpleAdapter storeadapter;//CCS客户适配器
    private List<Map<String, Object>> storelist = new ArrayList<Map<String, Object>>();

    private Dialog CustomerNormalDialog=null;
    private Dialog StoreNormalDialog=null;

    private String BrandCode="",BrandName="";

//    private final int Lic_SelectSure = 3;

    private SimpleAdapter adapter;

    private Boolean IsBindCCS=false,IsBindCCScust=false,IsBindCCSstore=false,IsSendToStore=false;
//    List<ScanOrder> scanOrderlist=new ArrayList<ScanOrder>();

    private CheckBox Check_IsSytem;
    Boolean IsSytemBrand=true;//是否为系统品牌

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_brand);

        Intent getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");

        if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
            //无单有入库分店发货，镜片无单有入库分店发货
            TraderSysId=getIntent.getStringExtra("trader_sysid");
            StoreSysId=getIntent.getStringExtra("store_sysid");

            RetailId=getIntent.getStringExtra("retail_id");
            RetailName=getIntent.getStringExtra("retail_name");
            CompanyName=getIntent.getStringExtra("company_name");
            CompanyId=getIntent.getStringExtra("company_id");
            TraderAlias_name=getIntent.getStringExtra("TraderAlias_name");
            Storealias_name=getIntent.getStringExtra("storealias_name");

        }else  if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
            //无单选客退货扫描，镜片无单选客退货扫描
            TraderSysId=getIntent.getStringExtra("trader_sysid");
            StoreSysId=getIntent.getStringExtra("store_sysid");

//            company_id = getIntent.getStringExtra("company_id");
//            company_name = getIntent.getStringExtra("company_name");
//            stock_id = getIntent.getStringExtra("stock_id");
//            stock_name = getIntent.getStringExtra("stock_name");
//            retail_id= getIntent.getStringExtra("retail_id");
//            retail_name= getIntent.getStringExtra("retail_name");
//            TraderAlias_name= getIntent.getStringExtra("alias_name");
//            Storealias_name=getIntent.getStringExtra("storealias_name");

        }else if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")){
            //有单有入库无明细分店发货
            supplier_id = getIntent.getStringExtra("supplier_id");
            supplier_name = getIntent.getStringExtra("supplier_name");
            stock_id = getIntent.getStringExtra("stock_id");
            stock_name = getIntent.getStringExtra("stock_name");
            purchecklno = getIntent.getStringExtra("purchecklno");
            saplno=getIntent.getStringExtra("saplno");
            store_id= getIntent.getStringExtra("store_id");
        }else if (lsv_aim.equals("P_Dv_CCSBarcodeStatusWrite")){
            //CCS换码补标
            stock_id = getIntent.getStringExtra("stock_id");
            stock_name = getIntent.getStringExtra("stock_name");
        }

        scanBillNo=getIntent.getStringExtra("scanBillNo");
        scanBillNum=getIntent.getStringExtra("scanBillNum");


        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();

        Check_IsSytem=findViewById(R.id.Check_IsSytem);
        Check_IsSytem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                IsSytemBrand=b;
                DownLoadDataThread(IsSytemBrand);
            }
        });

//        if(lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock")||lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")||lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")) {
//            IsSytemBrand=false;
//        }else{
//            IsSytemBrand=true;
//        }
        Check_IsSytem.setChecked(IsSytemBrand);

        btn_seach = findViewById(R.id.btn_seach);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page = 1;
//                supplierInforAdapter.resetDatas();
                brandlist.clear();
                if (adapter!=null) {
                    adapter.notifyDataSetChanged();
                }
                DownLoadDataThread(IsSytemBrand);
            }
        });

        et_query_supplier = findViewById(R.id.et_query_supplier);
        et_query_supplier.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Page = 1;
                        brandlist.clear();
                        adapter.notifyDataSetChanged();
                        DownLoadDataThread(IsSytemBrand);
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });

//        RecyclerView_supplier=findViewById(R.id.RecyclerView_supplier);
        list_brand = findViewById(R.id.list_brand);
        list_brand.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                Map<String, Object> item = (Map<String, Object>) listView.getItemAtPosition(i);

                Intent intent = new Intent();
                if(lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")){
                    //分店镜架无单有入库发货
//                    intent=new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
                    String brandcode= (String) item.get("BrandCode");
                    String PeiBillNo="";
                    if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")){
                        PeiBillNo=purchecklno;
                    }
                    BrandCode= (String) item.get("BrandCode");
                    BrandName= (String) item.get("BrandName");
                    BrandingCode=(String) item.get("BrandingCode");

                    GetScsCustStoreRelate(TraderSysId,StoreSysId,brandcode,PeiBillNo);
                }else if (lsv_aim.equals("P_Dv_CCSBarcodeStatusWrite")){
                    //CCS换货补标
                    intent=new Intent(mContext, P_Dv_CCSBarcodeStatusWrite.class);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("BrandingCode", (String) item.get("BrandingCode"));
                    intent.putExtra("BrandCode", (String) item.get("BrandCode"));
                    intent.putExtra("BrandName", (String) item.get("BrandName"));
                    startActivity(intent);
                }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")){
                    intent.putExtra("BrandingCode", (String) item.get("BrandingCode"));
                    intent.putExtra("BrandCode", (String) item.get("BrandCode"));
                    intent.putExtra("BrandName", (String) item.get("BrandName"));
                    setResult(RESULT_OK, intent);
                    finish();
                }else{
                    intent.putExtra("BrandingCode", (String) item.get("BrandingCode"));
                    intent.putExtra("BrandCode", (String) item.get("BrandCode"));
                    intent.putExtra("BrandName", (String) item.get("BrandName"));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

        });

//        initRecyclerView();
        DownLoadDataThread(IsSytemBrand);

    }

    // 请求服务
    private void DownLoadDataThread(Boolean tIsSytemBrand) {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    brandlist = accWeb.GetBrandInfor(et_query_supplier.getText().toString(),tIsSytemBrand);
                    Log.d("main",brandlist.toString());
//                    Collections.sort(Requestedlist, new SortListSupplierComparator());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    //判断门店是否已经绑定CCS客户或者CCS门店
    private void GetScsCustStoreRelate(final String tCustSysCode, final String tStoreSysCode, final String tBrandCode, final String tBillNo) {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    List<Map<String, Object>> data = accWeb.GetScsCustStoreRelate(tCustSysCode,tStoreSysCode,tBrandCode,tBillNo);

//                    Log.d("main","GetScsCustStoreRelate-"+data.toString());
                    if (data.size()>0) {
                        IsBindCCS = (Boolean) data.get(0).get("NeedBind");
                        IsBindCCScust = (Boolean) data.get(0).get("CustBind");
                        IsBindCCSstore = (Boolean) data.get(0).get("StoreBind");
                        IsSendToStore = (Boolean) data.get(0).get("IsSendToStore");

                        BrandingCode= (String) data.get(0).get("BrandingCode");
                        AgentCode= (String) data.get(0).get("AgentCode");
                        BrandingCustCode= (String) data.get(0).get("BrandingCustCode");
                        BrandingStoreCode=(String) data.get(0).get("BrandingStoreCode");

                        TraderSysId=(String) data.get(0).get("CustSysCode");
                        StoreSysId=(String) data.get(0).get("StoreSysCode");

                        TraderAlias_name=(String) data.get(0).get("TraderAlias");
                        Storealias_name=(String) data.get(0).get("StoreAlias");

                        CCSCustomerName=(String) data.get(0).get("CustomerName");
                        CCSStoreName=(String) data.get(0).get("StoreName");

                    }
//                    Log.d("main","IsBindCCS-"+IsBindCCS);
//                    Log.d("main","IsBindCCScust-"+IsBindCCScust);
//                    Log.d("main","IsBindCCSstore-"+IsBindCCSstore);

                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

//
//    private void ShowScanOrderList(){
//
//        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_scanorder_list,
//        null);
//        ListView ScanOrderListView=selectview.findViewById(R.id.List_ScanOrder);
//        ScanOrderAdapter scanOrderAdapter=new ScanOrderAdapter(mContext, scanOrderlist);
//        ScanOrderListView.setAdapter(scanOrderAdapter);
//
//        TextView tv_new_scanorder= selectview.findViewById(R.id.new_scanorder);
//
//        final Dialog alertDialog6 = new AlertDialog.Builder(mContext,R.style
//        .Base_Theme_AppCompat_Light_Dialog)
//                .setTitle("选择要扫描的单号")
//                .setIcon(R.drawable.scs)
//                .setView(selectview)
//                .setPositiveButton("新开扫描单", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                        Intent sureIntent = new Intent(SelectCCSBrand.this, SelectSureConfirm
//                        .class);
//                        sureIntent.putExtra("title", "确定选择供应商：" + ItemInfor.getSupplierName() +
//                        "？");
//                        startActivityForResult(sureIntent, Lic_SelectSure);
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                    }
//                }).create();
//        alertDialog6.show();
//
//        if (alertDialog6.getWindow() != null) {
//            WindowManager.LayoutParams lp = alertDialog6.getWindow().getAttributes();
//            lp.width = 800; // 宽度，可根据屏幕宽度进行计算
//            lp.gravity = Gravity.CENTER;
//            alertDialog6.getWindow().setAttributes(lp);
//        }
//
//
//        tv_new_scanorder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent sureIntent = new Intent(SelectCCSBrand.this, SelectSureConfirm.class);
//                sureIntent.putExtra("title", "确定选择供应商：" + (String) ItemInfor.getSupplierName()
//                + "？");
//                startActivityForResult(sureIntent, Lic_SelectSure);
//                alertDialog6.dismiss();
//            }
//        });
//        ScanOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = null;
//                if (lsv_aim.equals("P_Dv_Lens_InStock_NoBill")){
//                    intent= new Intent(mContext, P_Dv_Lens_InStock_NoBill.class);
//                }else{
//                    intent= new Intent(mContext, P_Dv_InStock_NoBill.class);
//                }
////                Intent intent = new Intent();
//                intent.putExtra("supplier_id", ItemInfor.getSupplierId());
//                intent.putExtra("supplier_name", ItemInfor.getSupplierName());
//                intent.putExtra("stock_id", scanOrderlist.get(i).getStockCode());
//                intent.putExtra("stock_name", scanOrderlist.get(i).getStockName());
//                intent.putExtra("scanBillNo", scanOrderlist.get(i).getBillNo());
//                intent.putExtra("scanBillNum", scanOrderlist.get(i).getBillNum());
//
//                intent.putExtra("aim", lsv_aim);
//                startActivity(intent);
//                finish();
//            }
//        });
//    }
//
//    // 下载扫描单号 请求服务
//    private void DownLoadScanOrder(final String tDeSysCode, final String tStoreSysCode, final
//    String tScanType, final String tOrderType) {
//        scanOrderlist = new ArrayList<ScanOrder>();
//        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
//        Thread sendCode = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    scanOrderlist = accWeb.GetBrandInfor(tDeSysCode,tStoreSysCode,tScanType,
//                    tOrderType);
////                    Log.d("main",scanOrderlist.toString());
//                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
//                } catch (Exception e) {
//                    ShowMessage.ShowMsg(hand, ShowMessage.HandFailed,"下载出错" + e.getMessage());
//                }
//            }
//        });
//        sendCode.start();
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//                case Lic_SelectSure:
//                    if (lsv_aim == null || lsv_aim.isEmpty()) {
//                        return;
//                    }
//                    Intent intent = new Intent(this, SelectStock.class);
//
//
//                    intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
//                    intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
//                    intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
//                    intent.putExtra("aim", lsv_aim);
//                    startActivity(intent);
//                    break;
//                default:
//                    break;
//            }
//
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }

//    private void initRecyclerView() {
//        supplierInforAdapter = new SupplierInforAdapter(Requestedlist, mContext, Requestedlist
//        .size() > 0 ? true : false);
//        mLayoutManager = new LinearLayoutManager(mContext);
//        RecyclerView_supplier.setLayoutManager(mLayoutManager);
//        RecyclerView_supplier.setAdapter(supplierInforAdapter);
//
//
//        supplierInforAdapter.setClick(new SupplierInforAdapter.MyClick() {
//            @Override
//            public void click(View v) {
//                if(v != null){
//                    //返回的是“当前Item对应的View在当前列表可视范围内的index”。
////                    int index1 = RecyclerView_supplier.indexOfChild(v);
//                    //返回的是“当前Item对应的View在Adapter数据列表中的位置”。
//                    int index2 = RecyclerView_supplier.getChildAdapterPosition(v);
////                    //返回的是“当前Item对应的View在整个视图列表中的index”。
////                    int index3 = RecyclerView_supplier.getChildLayoutPosition(v);
//
//                    ItemInfor=supplierInforAdapter.getDataList().get(index2);
//                    String tordertype="普通";
//                    if (lsv_aim.equals("P_Dv_Lens_InStock_NoBill")){
//                        tordertype="镜片";
//                    }
//                    DownLoadScanOrder(ItemInfor.getSuppSysCode(),"","InStock",tordertype);
////                    Log.e("===111", supplierInforAdapter.getDataList().get(index2).toString());
////                    Log.e("===111", index2 + "" );
////                    Log.e("===", Requestedlist.toString() );
//                }
//            }
//        });
//
//        RecyclerView_supplier.setItemAnimator(new DefaultItemAnimator());
//
//        RecyclerView_supplier.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (supplierInforAdapter.isFadeTips() == false && lastVisibleItem + 1 ==
//                    supplierInforAdapter.getItemCount()) {
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
////                                updateRecyclerView(supplierInforAdapter.getRealLastPosition()
// , supplierInforAdapter.getRealLastPosition() + 50);
//                            }
//                        }, 500);
//                    }
//
//                    if (supplierInforAdapter.isFadeTips() == true && lastVisibleItem + 2 ==
//                    supplierInforAdapter.getItemCount()) {
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
////                                updateRecyclerView(supplierInforAdapter.getRealLastPosition()
// , supplierInforAdapter.getRealLastPosition() + 50);
//                                DownLoadDataThread();
//                            }
//                        }, 500);
//                    }
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
//
//            }
//        });
//    }

//    private List<SupplierInfor> getDatas(final int firstIndex, final int lastIndex) {
//        List<SupplierInfor> resList = new ArrayList<>();
//        for (int i = firstIndex; i < lastIndex; i++) {
//            if (i < list.size()) {
//                resList.add(list.get(i));
//            }
//        }
//        return resList;
//    }

//    private void updateRecyclerView(int fromIndex, int toIndex) {
//        List<SupplierInfor> newDatas = Requestedlist;
////        Log.d("main","数据长度+"+newDatas.toString());
//        if (newDatas.size() > 0) {
//            Page=Page+1;
//            supplierInforAdapter.updateList(newDatas, true);
//        } else {
//            supplierInforAdapter.updateList(null, false);
//        }
//    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
                    initListView(brandlist);
                    break;
//                case ShowMessage.HandFailed: //扫描单号返回报错
//                    MyProgressDialog.close();
//                    Intent sureIntent = new Intent(SelectCCSBrand.this, SelectSureConfirm.class);
//                    sureIntent.putExtra("title",
//                            "确定选择供应商：" + (String) ItemInfor.getSupplierName() + "？");
//                    startActivityForResult(sureIntent, Lic_SelectSure);
//                    break;
                case  ShowMessage.HandScanSuccess:
                    MyProgressDialog.close();
                    Intent intent=new Intent();
                    intent.putExtra("BrandCode",BrandCode);
                    intent.putExtra("BrandName",BrandName);
                    intent.putExtra("BrandingCode", BrandingCode);
                    setResult(RESULT_OK, intent);
                    finish();
//                    CustomerNormalDialog=null;
//                    StoreNormalDialog=null;
//                    if (IsBindCCS) {
//                        //需要绑定CCS客户或者门店
//                        if(!IsBindCCScust){
////                            ShowCCSCustDialog();
//                            DownLoadCCSCustomerData(BrandingCode,"");
////                            //需要绑定CCS客户
////                            Intent intent=new Intent(mContext,SelectCCScustomer.class);
////                            if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
////                                //无单有入库分店发货，镜片无单有入库分店发货
////                                intent.putExtra("retail_id", RetailId);
////                                intent.putExtra("retail_name", RetailName);
////                                intent.putExtra("company_name", CompanyName);
////                                intent.putExtra("company_id", CompanyId);
////                                intent.putExtra("TraderAlias_name", TraderAlias_name);
////                                intent.putExtra("storealias_name", Storealias_name);
////                            }else  if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
////                                //无单选客退货扫描，镜片无单选客退货扫描
////                                intent.putExtra("company_id", company_id);
////                                intent.putExtra("company_name", company_name);
////                                intent.putExtra("stock_id", stock_id);
////                                intent.putExtra("stock_name", stock_name);
////                                intent.putExtra("retail_id", retail_id);
////                                intent.putExtra("retail_name", retail_name);
////                                intent.putExtra("alias_name", TraderAlias_name);
////                                intent.putExtra("storealias_name", Storealias_name);
////                            }
////                            else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
////                                //有单有入库无明细分店发货
////                                intent.putExtra("purchecklno",purchecklno);
////                                intent.putExtra("saplno", saplno);
////                                intent.putExtra("supplier_name", supplier_name);
////                                intent.putExtra("stock_name", stock_name);
////                                intent.putExtra("supplier_id", supplier_id);
////                                intent.putExtra("stock_id", stock_id);
////                                intent.putExtra("store_id", store_id);
////                                intent.putExtra("TraderAlias_name", TraderAlias_name);
////                                intent.putExtra("storealias_name", Storealias_name);
////                            }
////                            intent.putExtra("trader_sysid",TraderSysId);
////                            intent.putExtra("store_sysid", StoreSysId);
////                            intent.putExtra("Brand_code", BrandingCode);
////                            intent.putExtra("Agent_Code", AgentCode);
////                            intent.putExtra("aim", lsv_aim);
//////                            if (IsSendToStore) {
//////                                intent.putExtra("IsBindCCSstore", IsBindCCSstore);
//////                            }else{
//////                                intent.putExtra("IsBindCCSstore", true);
//////                            }
////                            if (IsSendToStore) {
////                                intent.putExtra("IsBindCCSstore", IsBindCCSstore);
////                            }else{
////                                intent.putExtra("IsBindCCSstore", true);
////                            }
////
////
////                            intent.putExtra("scanBillNo", scanBillNo);
////                            intent.putExtra("scanBillNum", scanBillNum);
////
////                            startActivity(intent);
////                        }
//                        }
//                        else if (IsSendToStore) {
//                            //是否需要绑定CCS门店
//                            if (!IsBindCCSstore) {
//                                DownLoadCCSStoreData("",BrandingCustCode);
//                            }else{
////                                已经绑定CCS门店
//                                Intent intent = null;
//                                if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                                    intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
////                            intent.putExtra("trader_sysid",TraderSysId);
////                            intent.putExtra("store_sysid", StoreSysId);
//                                    intent.putExtra("retail_id", RetailId);
//                                    intent.putExtra("retail_name", RetailName);
//                                    intent.putExtra("company_name", CompanyName);
//                                    intent.putExtra("company_id", CompanyId);
//                                    intent.putExtra("TraderAlias_name", TraderAlias_name);
//                                    intent.putExtra("IsSendToStore", IsSendToStore);
//                                    intent.putExtra("BrandingCustCode", BrandingCustCode);
//                                    intent.putExtra("BrandingStoreCode", BrandingStoreCode);
//                                }
//                                //镜片有入库无单分店发货
//                                else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                                    intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
////                            intent.putExtra("trader_sysid",TraderSysId);
////                            intent.putExtra("store_sysid", StoreSysId);
//                                    intent.putExtra("retail_id", RetailId);
//                                    intent.putExtra("retail_name", RetailName);
//                                    intent.putExtra("company_name", CompanyName);
//                                    intent.putExtra("company_id", CompanyId);
//                                    intent.putExtra("TraderAlias_name", TraderAlias_name);
//
//                                    intent.putExtra("IsSendToStore", IsSendToStore);
//                                    intent.putExtra("BrandingCustCode", BrandingCustCode);
//                                    intent.putExtra("BrandingStoreCode", BrandingStoreCode);
//                                }
//                                else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
//                                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
//                                    intent.putExtra("stock_id", stock_id);
//                                    intent.putExtra("stock_name", stock_name);
//                                    intent.putExtra("company_name", company_name);
//                                    intent.putExtra("company_id", company_id);
//                                    intent.putExtra("retail_id",retail_id);
//                                    intent.putExtra("retail_name", retail_name);
//                                    intent.putExtra("alias_name", TraderAlias_name);
//                                }else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
//                                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
//                                    intent.putExtra("stock_id", stock_id);
//                                    intent.putExtra("stock_name", stock_name);
//                                    intent.putExtra("company_name", company_name);
//                                    intent.putExtra("company_id", company_id);
//                                    intent.putExtra("retail_id",retail_id);
//                                    intent.putExtra("retail_name", retail_name);
//                                    intent.putExtra("alias_name", TraderAlias_name);
//                                }  else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                                    //有单有入库无明细分店发货
//                                    intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                                    intent.putExtra("purchecklno",purchecklno);
//                                    intent.putExtra("saplno", saplno);
//                                    intent.putExtra("supplier_name", supplier_name);
//                                    intent.putExtra("stock_name", stock_name);
//                                    intent.putExtra("supplier_id", supplier_id);
//                                    intent.putExtra("stock_id", stock_id);
//                                    intent.putExtra("store_id", store_id);
//                                }
//                                intent.putExtra("scanBillNo", scanBillNo);
//                                intent.putExtra("scanBillNum", scanBillNum);
//                                intent.putExtra("aim", lsv_aim);
//                                startActivity(intent);
//                            }
//                        }else{
//                            // 不需要绑定CCS门店
//                            Intent intent = null;
//                            if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                                intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
////                            intent.putExtra("trader_sysid",TraderSysId);
////                            intent.putExtra("store_sysid", StoreSysId);
//                                intent.putExtra("retail_id", RetailId);
//                                intent.putExtra("retail_name", RetailName);
//                                intent.putExtra("company_name", CompanyName);
//                                intent.putExtra("company_id", CompanyId);
//                                intent.putExtra("TraderAlias_name", TraderAlias_name);
//
//                                intent.putExtra("IsSendToStore", IsSendToStore);
//                                intent.putExtra("BrandingCustCode", BrandingCustCode);
//                                intent.putExtra("BrandingStoreCode", BrandingStoreCode);
//                            }
//                            //镜片有入库无单分店发货
//                            else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                                intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
////                            intent.putExtra("trader_sysid",TraderSysId);
////                            intent.putExtra("store_sysid", StoreSysId);
//                                intent.putExtra("retail_id", RetailId);
//                                intent.putExtra("retail_name", RetailName);
//                                intent.putExtra("company_name", CompanyName);
//                                intent.putExtra("company_id", CompanyId);
//                                intent.putExtra("TraderAlias_name", TraderAlias_name);
//
//                                intent.putExtra("IsSendToStore", IsSendToStore);
//                                intent.putExtra("BrandingCustCode", BrandingCustCode);
//                                intent.putExtra("BrandingStoreCode", BrandingStoreCode);
//                            }
//                            else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
//                                intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
//                                intent.putExtra("stock_id", stock_id);
//                                intent.putExtra("stock_name", stock_name);
//                                intent.putExtra("company_name", company_name);
//                                intent.putExtra("company_id", company_id);
//                                intent.putExtra("retail_id",retail_id);
//                                intent.putExtra("retail_name", retail_name);
//                                intent.putExtra("alias_name", TraderAlias_name);
//                            }else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
//                                intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
//                                intent.putExtra("stock_id", stock_id);
//                                intent.putExtra("stock_name", stock_name);
//                                intent.putExtra("company_name", company_name);
//                                intent.putExtra("company_id", company_id);
//                                intent.putExtra("retail_id",retail_id);
//                                intent.putExtra("retail_name", retail_name);
//                                intent.putExtra("alias_name", TraderAlias_name);
//                            }  else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                                //有单有入库无明细分店发货
//                                intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                                intent.putExtra("purchecklno",purchecklno);
//                                intent.putExtra("saplno", saplno);
//                                intent.putExtra("supplier_name", supplier_name);
//                                intent.putExtra("stock_name", stock_name);
//                                intent.putExtra("supplier_id", supplier_id);
//                                intent.putExtra("stock_id", stock_id);
//                                intent.putExtra("store_id", store_id);
//                            }
//                            intent.putExtra("scanBillNo", scanBillNo);
//                            intent.putExtra("scanBillNum", scanBillNum);
//
//                            intent.putExtra("aim", lsv_aim);
//
//                            startActivity(intent);
//                        }
//                    }else{
//
////                        ShowChangeBinding();
////
//                        Intent intent = null;
//                        if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                            intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
////                            intent.putExtra("trader_sysid",TraderSysId);
////                            intent.putExtra("store_sysid", StoreSysId);
//
//                            intent.putExtra("retail_id", RetailId);
//                            intent.putExtra("retail_name", RetailName);
//                            intent.putExtra("company_name", CompanyName);
//                            intent.putExtra("company_id", CompanyId);
//                            intent.putExtra("TraderAlias_name", TraderAlias_name);
//
//                            intent.putExtra("IsSendToStore", IsSendToStore);
//                            intent.putExtra("BrandingCustCode", BrandingCustCode);
//                            intent.putExtra("BrandingStoreCode", BrandingStoreCode);
//                        }
//                        //镜片有入库无单分店发货
//                        else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                            intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
////                            intent.putExtra("trader_sysid",TraderSysId);
//                            intent.putExtra("store_sysid", StoreSysId);
//
//                            intent.putExtra("retail_id", RetailId);
//                            intent.putExtra("retail_name", RetailName);
//                            intent.putExtra("company_name", CompanyName);
//                            intent.putExtra("company_id", CompanyId);
//                            intent.putExtra("TraderAlias_name", TraderAlias_name);
//
//                            intent.putExtra("IsSendToStore", IsSendToStore);
//                            intent.putExtra("BrandingCustCode", BrandingCustCode);
//                            intent.putExtra("BrandingStoreCode", BrandingStoreCode);
//                        }
//                        else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
//                            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
//                            intent.putExtra("stock_id", stock_id);
//                            intent.putExtra("stock_name", stock_name);
//                            intent.putExtra("company_name", company_name);
//                            intent.putExtra("company_id", company_id);
//                            intent.putExtra("retail_id",retail_id);
//                            intent.putExtra("retail_name", retail_name);
//                            intent.putExtra("alias_name", TraderAlias_name);
//                        }else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
//                            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
//                            intent.putExtra("stock_id", stock_id);
//                            intent.putExtra("stock_name", stock_name);
//                            intent.putExtra("company_name", company_name);
//                            intent.putExtra("company_id", company_id);
//                            intent.putExtra("retail_id",retail_id);
//                            intent.putExtra("retail_name", retail_name);
//                            intent.putExtra("alias_name", TraderAlias_name);
//                        }  else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                            //有单有入库无明细分店发货
//                            intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                            intent.putExtra("purchecklno",purchecklno);
//                            intent.putExtra("saplno", saplno);
//                            intent.putExtra("supplier_name", supplier_name);
//                            intent.putExtra("stock_name", stock_name);
//                            intent.putExtra("supplier_id", supplier_id);
//                            intent.putExtra("stock_id", stock_id);
//                            intent.putExtra("store_id", store_id);
//                        }
//                        intent.putExtra("scanBillNo", scanBillNo);
//                        intent.putExtra("scanBillNum", scanBillNum);
//
//                        intent.putExtra("aim", lsv_aim);
//
//                        startActivity(intent);
//                    }
                    break;

                case 2:
                    MyProgressDialog.close();
//                    Log.d("main",customerlist.toString());
                    if (CustomerNormalDialog!=null){
                        //弹窗已存在就刷新，客户列表
                        if (customeradapter!=null){
                            customeradapter.notifyDataSetChanged();
                        }
                    }else{
                        //获取CCS客户
                        ShowCCSCustDialog();
                    }
                    break;
                case 3:
                    //同步或绑定CCS客户数据
                    MyProgressDialog.close();
                    if (CustomerNormalDialog!=null){
                        CustomerNormalDialog.dismiss();
                    }
                    if (IsSendToStore) {
                        //是否需要绑定CCS门店
                        if (!IsBindCCSstore) {
                            //需要绑定CCS门店
                            DownLoadCCSStoreData("",BrandingCustCode);
                        } else {
                            Intent scsintent = null;
                            if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                                scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
                                scsintent.putExtra("retail_id", RetailId);
                                scsintent.putExtra("retail_name", RetailName);
                                scsintent.putExtra("company_name", CompanyName);
                                scsintent.putExtra("company_id", CompanyId);
                                scsintent.putExtra("TraderAlias_name", TraderAlias_name);
                                scsintent.putExtra("storealias_name", Storealias_name);

                            }
                            //镜片有入库无单分店发货
                            else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                                scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
                                scsintent.putExtra("retail_id", RetailId);
                                scsintent.putExtra("retail_name", RetailName);
                                scsintent.putExtra("company_name", CompanyName);
                                scsintent.putExtra("company_id", CompanyId);
                                scsintent.putExtra("TraderAlias_name", TraderAlias_name);
                                scsintent.putExtra("storealias_name", Storealias_name);

                            }
                            //分店无单选客退货
                            else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
                                scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);

                                scsintent.putExtra("company_id", company_id);
                                scsintent.putExtra("company_name", company_name);
                                scsintent.putExtra("stock_id", stock_id);
                                scsintent.putExtra("stock_name", stock_name);
                                scsintent.putExtra("retail_id", retail_id);
                                scsintent.putExtra("retail_name", retail_name);
                                scsintent.putExtra("alias_name", TraderAlias_name);
                                scsintent.putExtra("storealias_name", Storealias_name);
                            }
                            //镜片分店无单选客退货
                            else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
                                scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);

                                scsintent.putExtra("company_id", company_id);
                                scsintent.putExtra("company_name", company_name);
                                scsintent.putExtra("stock_id", stock_id);
                                scsintent.putExtra("stock_name", stock_name);
                                scsintent.putExtra("retail_id", retail_id);
                                scsintent.putExtra("retail_name", retail_name);
                                scsintent.putExtra("alias_name", TraderAlias_name);
                            } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                                //有单有入库有明细分店发货
                                scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                                scsintent.putExtra("purchecklno", purchecklno);
                                scsintent.putExtra("saplno", saplno);
                                scsintent.putExtra("supplier_name", supplier_name);
                                scsintent.putExtra("stock_name", stock_name);
                                scsintent.putExtra("supplier_id", supplier_id);
                                scsintent.putExtra("stock_id", stock_id);
                                scsintent.putExtra("store_id", store_id);
                                scsintent.putExtra("storealias_name", Storealias_name);
                            } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                                //有单有入库无明细分店发货
                                scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                                scsintent.putExtra("purchecklno", purchecklno);
                                scsintent.putExtra("saplno", saplno);
                                scsintent.putExtra("supplier_name", supplier_name);
                                scsintent.putExtra("stock_name", stock_name);
                                scsintent.putExtra("supplier_id", supplier_id);
                                scsintent.putExtra("stock_id", stock_id);
                                scsintent.putExtra("store_id", store_id);
                            } else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                                //镜片有单有入库分店发货
                                scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
                                scsintent.putExtra("purchecklno", purchecklno);
                                scsintent.putExtra("saplno", saplno);
                                scsintent.putExtra("supplier_name", supplier_name);
                                scsintent.putExtra("stock_name", stock_name);
                                scsintent.putExtra("supplier_id", supplier_id);
                                scsintent.putExtra("stock_id", stock_id);
                                scsintent.putExtra("store_id", store_id);
                            }
                            scsintent.putExtra("aim", lsv_aim);

                            scsintent.putExtra("scanBillNo", scanBillNo);
                            scsintent.putExtra("scanBillNum", scanBillNum);

                            startActivity(scsintent);
                            finish();
                        }
                    }else{
                        Intent scsintent = null;
                        if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
                            scsintent.putExtra("retail_id", RetailId);
                            scsintent.putExtra("retail_name", RetailName);
                            scsintent.putExtra("company_name", CompanyName);
                            scsintent.putExtra("company_id", CompanyId);
                            scsintent.putExtra("TraderAlias_name", TraderAlias_name);
                            scsintent.putExtra("storealias_name", Storealias_name);

                        }
                        //镜片有入库无单分店发货
                        else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                            scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
                            scsintent.putExtra("retail_id", RetailId);
                            scsintent.putExtra("retail_name", RetailName);
                            scsintent.putExtra("company_name", CompanyName);
                            scsintent.putExtra("company_id", CompanyId);
                            scsintent.putExtra("TraderAlias_name", TraderAlias_name);
                            scsintent.putExtra("storealias_name", Storealias_name);

                        }
                        //分店无单选客退货
                        else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
                            scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);

                            scsintent.putExtra("company_id", company_id);
                            scsintent.putExtra("company_name", company_name);
                            scsintent.putExtra("stock_id", stock_id);
                            scsintent.putExtra("stock_name", stock_name);
                            scsintent.putExtra("retail_id", retail_id);
                            scsintent.putExtra("retail_name", retail_name);
                            scsintent.putExtra("alias_name", TraderAlias_name);
                            scsintent.putExtra("storealias_name", Storealias_name);
                        }
                        //镜片分店无单选客退货
                        else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
                            scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);

                            scsintent.putExtra("company_id", company_id);
                            scsintent.putExtra("company_name", company_name);
                            scsintent.putExtra("stock_id", stock_id);
                            scsintent.putExtra("stock_name", stock_name);
                            scsintent.putExtra("retail_id", retail_id);
                            scsintent.putExtra("retail_name", retail_name);
                            scsintent.putExtra("alias_name", TraderAlias_name);
                        } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                            //有单有入库有明细分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                            scsintent.putExtra("purchecklno", purchecklno);
                            scsintent.putExtra("saplno", saplno);
                            scsintent.putExtra("supplier_name", supplier_name);
                            scsintent.putExtra("stock_name", stock_name);
                            scsintent.putExtra("supplier_id", supplier_id);
                            scsintent.putExtra("stock_id", stock_id);
                            scsintent.putExtra("store_id", store_id);
                            scsintent.putExtra("storealias_name", Storealias_name);
                        } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                            //有单有入库无明细分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                            scsintent.putExtra("purchecklno", purchecklno);
                            scsintent.putExtra("saplno", saplno);
                            scsintent.putExtra("supplier_name", supplier_name);
                            scsintent.putExtra("stock_name", stock_name);
                            scsintent.putExtra("supplier_id", supplier_id);
                            scsintent.putExtra("stock_id", stock_id);
                            scsintent.putExtra("store_id", store_id);
                        } else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                            //镜片有单有入库分店发货
                            scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
                            scsintent.putExtra("purchecklno", purchecklno);
                            scsintent.putExtra("saplno", saplno);
                            scsintent.putExtra("supplier_name", supplier_name);
                            scsintent.putExtra("stock_name", stock_name);
                            scsintent.putExtra("supplier_id", supplier_id);
                            scsintent.putExtra("stock_id", stock_id);
                            scsintent.putExtra("store_id", store_id);
                        }
                        scsintent.putExtra("aim", lsv_aim);

                        scsintent.putExtra("scanBillNo", scanBillNo);
                        scsintent.putExtra("scanBillNum", scanBillNum);

                        startActivity(scsintent);
                        finish();
                    }
                    break;
                case 4:
                    MyProgressDialog.close();

                    if (StoreNormalDialog!=null){
                        //弹窗已存在就刷新，客户列表
                        if (storeadapter!=null){
                            storeadapter.notifyDataSetChanged();
                        }
                    }else{
                        //下载CCS门店数据展示
                        ShowCCSStoreDialog();
                    }
                    break;
                case 5:
                    MyProgressDialog.close();
//                    同步和绑定CCS门店数据
                    if (StoreNormalDialog!=null){
                        StoreNormalDialog.dismiss();
                    }
                    Intent scsintent = null;
                    if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
                        scsintent.putExtra("retail_id", RetailId);
                        scsintent.putExtra("retail_name", RetailName);
                        scsintent.putExtra("company_name", CompanyName);
                        scsintent.putExtra("company_id", CompanyId);
                        scsintent.putExtra("TraderAlias_name", TraderAlias_name);
                        scsintent.putExtra("storealias_name", Storealias_name);

                    }
                    //镜片有入库无单分店发货
                    else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                        scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
                        scsintent.putExtra("retail_id", RetailId);
                        scsintent.putExtra("retail_name", RetailName);
                        scsintent.putExtra("company_name", CompanyName);
                        scsintent.putExtra("company_id", CompanyId);
                        scsintent.putExtra("TraderAlias_name", TraderAlias_name);
                        scsintent.putExtra("storealias_name", Storealias_name);

                    }
                    //分店无单选客退货
                    else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
                        scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);

                        scsintent.putExtra("company_id", company_id);
                        scsintent.putExtra("company_name", company_name);
                        scsintent.putExtra("stock_id", stock_id);
                        scsintent.putExtra("stock_name", stock_name);
                        scsintent.putExtra("retail_id", retail_id);
                        scsintent.putExtra("retail_name", retail_name);
                        scsintent.putExtra("alias_name", TraderAlias_name);
                        scsintent.putExtra("storealias_name", Storealias_name);
                    }
                    //镜片分店无单选客退货
                    else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
                        scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);

                        scsintent.putExtra("company_id", company_id);
                        scsintent.putExtra("company_name", company_name);
                        scsintent.putExtra("stock_id", stock_id);
                        scsintent.putExtra("stock_name", stock_name);
                        scsintent.putExtra("retail_id", retail_id);
                        scsintent.putExtra("retail_name", retail_name);
                        scsintent.putExtra("alias_name", TraderAlias_name);
                    } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                        //有单有入库有明细分店发货
                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
                        scsintent.putExtra("purchecklno", purchecklno);
                        scsintent.putExtra("saplno", saplno);
                        scsintent.putExtra("supplier_name", supplier_name);
                        scsintent.putExtra("stock_name", stock_name);
                        scsintent.putExtra("supplier_id", supplier_id);
                        scsintent.putExtra("stock_id", stock_id);
                        scsintent.putExtra("store_id", store_id);
                        scsintent.putExtra("storealias_name", Storealias_name);
                    } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                        //有单有入库无明细分店发货
                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                        scsintent.putExtra("purchecklno", purchecklno);
                        scsintent.putExtra("saplno", saplno);
                        scsintent.putExtra("supplier_name", supplier_name);
                        scsintent.putExtra("stock_name", stock_name);
                        scsintent.putExtra("supplier_id", supplier_id);
                        scsintent.putExtra("stock_id", stock_id);
                        scsintent.putExtra("store_id", store_id);
                    } else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
                        //镜片有单有入库分店发货
                        scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
                        scsintent.putExtra("purchecklno", purchecklno);
                        scsintent.putExtra("saplno", saplno);
                        scsintent.putExtra("supplier_name", supplier_name);
                        scsintent.putExtra("stock_name", stock_name);
                        scsintent.putExtra("supplier_id", supplier_id);
                        scsintent.putExtra("stock_id", stock_id);
                        scsintent.putExtra("store_id", store_id);
                    }
                    scsintent.putExtra("aim", lsv_aim);

                    scsintent.putExtra("scanBillNo", scanBillNo);
                    scsintent.putExtra("scanBillNum", scanBillNum);

                    startActivity(scsintent);
                    finish();

                    break;
                default:
                    break;
            }

            MyProgressDialog.close();
        }
    }

    public void initListView(List<Map<String, Object>> mList) {
//        Collections.sort(list, new SortListMapComparator("PeiGoodLno"));
        adapter = new SimpleAdapter(this, mList, R.layout.new_list_select_ccsbrand, new String[]{"BrandCode", "BrandName"}, new int[]{R.id.txt_list1, R.id.txt_list2});
        list_brand.setAdapter(adapter);
        MyProgressDialog.close();
//        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    //询问是否需要换绑客户和门店
    private void ShowChangeBinding(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */

        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_changebinding,null);
        TextView tv_ccscustname=selectview.findViewById(R.id.tv_ccscustname);
        TextView tv_changeccscust=selectview.findViewById(R.id.tv_changeccscust);//客户换绑
        TextView tv_ccsstorename=selectview.findViewById(R.id.tv_ccsstorename);
        TextView tv_changeccsstore=selectview.findViewById(R.id.tv_changeccsstore);//门店换绑
        TextView tv_scancode=selectview.findViewById(R.id.tv_scancode);//直接扫码
        LinearLayout lin_store=selectview.findViewById(R.id.lin_store);//是否显示门店数据
        View view_line=selectview.findViewById(R.id.view_line);//下划线

        final Dialog normalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
                .setView(selectview)
                .create();

        normalDialog.show();

        if (normalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = normalDialog.getWindow().getAttributes();
            lp.width = 800; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            normalDialog.getWindow().setAttributes(lp);
        }
        tv_ccscustname.setText(BrandingCustCode);
        tv_ccsstorename.setText(BrandingStoreCode);
        if (!IsSendToStore) {
            lin_store.setVisibility(View.GONE);
            view_line.setVisibility(View.GONE);
        }

        tv_changeccscust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(mContext,SelectCCScustomer.class);
                if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                    //无单有入库分店发货，镜片无单有入库分店发货
                    intent.putExtra("retail_id", RetailId);
                    intent.putExtra("retail_name", RetailName);
                    intent.putExtra("company_name", CompanyName);
                    intent.putExtra("company_id", CompanyId);
                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);
                }else  if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
                    //无单选客退货扫描，镜片无单选客退货扫描
                    intent.putExtra("company_id", company_id);
                    intent.putExtra("company_name", company_name);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("retail_id", retail_id);
                    intent.putExtra("retail_name", retail_name);
                    intent.putExtra("alias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);
                }
                else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                    //有单有入库无明细分店发货
                    intent.putExtra("purchecklno",purchecklno);
                    intent.putExtra("saplno", saplno);
                    intent.putExtra("supplier_name", supplier_name);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("supplier_id", supplier_id);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("store_id", store_id);
                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);
                }
                intent.putExtra("trader_sysid",TraderSysId);
                intent.putExtra("store_sysid", StoreSysId);
                intent.putExtra("Brand_code", BrandingCode);
                intent.putExtra("Agent_Code", AgentCode);
                intent.putExtra("aim", lsv_aim);
                if (IsSendToStore) {
                    intent.putExtra("IsBindCCSstore", false);
                }else{
                    intent.putExtra("IsBindCCSstore", true);
                }
                intent.putExtra("scanBillNo", scanBillNo);
                intent.putExtra("scanBillNum", scanBillNum);

                startActivity(intent);

                normalDialog.dismiss();
            }
        });

        tv_changeccsstore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //需要绑定CCS门店
                Intent intent=new Intent(mContext,SelectCCSstore.class);
                intent.putExtra("aim", lsv_aim);
                intent.putExtra("trader_sysid",TraderSysId);
                intent.putExtra("store_sysid", StoreSysId);
                intent.putExtra("Brand_code", BrandingCode);
                intent.putExtra("Agent_Code", AgentCode);
                intent.putExtra("CCSCuts_Code", BrandingCustCode);

                intent.putExtra("scanBillNo", scanBillNo);
                intent.putExtra("scanBillNum", scanBillNum);


                if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                    //无单有入库分店发货，镜片无单有入库分店发货
                    intent.putExtra("retail_id", RetailId);
                    intent.putExtra("retail_name", RetailName);
                    intent.putExtra("company_name", CompanyName);
                    intent.putExtra("company_id", CompanyId);
                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);

                }else  if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
                    intent.putExtra("company_id", company_id);
                    intent.putExtra("company_name", company_name);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("retail_id", retail_id);
                    intent.putExtra("retail_name", retail_name);
                    intent.putExtra("alias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);

                }
                else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                    //有单有入库无明细分店发货
                    intent.putExtra("purchecklno",purchecklno);
                    intent.putExtra("saplno", saplno);
                    intent.putExtra("supplier_name", supplier_name);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("supplier_id", supplier_id);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("store_id", store_id);
                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                    intent.putExtra("storealias_name", Storealias_name);
                }
                startActivity(intent);
                normalDialog.dismiss();
            }
        });

        tv_scancode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                    intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
//                            intent.putExtra("trader_sysid",TraderSysId);
//                            intent.putExtra("store_sysid", StoreSysId);
                    intent.putExtra("retail_id", RetailId);
                    intent.putExtra("retail_name", RetailName);
                    intent.putExtra("company_name", CompanyName);
                    intent.putExtra("company_id", CompanyId);
                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                }
                //镜片有入库无单分店发货
                else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                    intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
//                            intent.putExtra("trader_sysid",TraderSysId);
//                            intent.putExtra("store_sysid", StoreSysId);
                    intent.putExtra("retail_id", RetailId);
                    intent.putExtra("retail_name", RetailName);
                    intent.putExtra("company_name", CompanyName);
                    intent.putExtra("company_id", CompanyId);
                    intent.putExtra("TraderAlias_name", TraderAlias_name);
                }
                else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("company_name", company_name);
                    intent.putExtra("company_id", company_id);
                    intent.putExtra("retail_id",retail_id);
                    intent.putExtra("retail_name", retail_name);
                    intent.putExtra("alias_name", TraderAlias_name);
                }else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("company_name", company_name);
                    intent.putExtra("company_id", company_id);
                    intent.putExtra("retail_id",retail_id);
                    intent.putExtra("retail_name", retail_name);
                    intent.putExtra("alias_name", TraderAlias_name);
                }  else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
                    //有单有入库无明细分店发货
                    intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
                    intent.putExtra("purchecklno",purchecklno);
                    intent.putExtra("saplno", saplno);
                    intent.putExtra("supplier_name", supplier_name);
                    intent.putExtra("stock_name", stock_name);
                    intent.putExtra("supplier_id", supplier_id);
                    intent.putExtra("stock_id", stock_id);
                    intent.putExtra("store_id", store_id);
                }
                intent.putExtra("scanBillNo", scanBillNo);
                intent.putExtra("scanBillNum", scanBillNum);

                intent.putExtra("aim", lsv_aim);

                startActivity(intent);

                normalDialog.dismiss();
            }
        });

    }

    //显示CCS客户弹出层
    private void ShowCCSCustDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        View selectview = LayoutInflater.from(mContext).inflate(R.layout.activity_select_ccscustomer,null);

        ListView list_customer=selectview.findViewById(R.id.listview_ccscustomer);
        TextView tv_total=selectview.findViewById(R.id.tv_total);
        final EditText edit_CCSCust=selectview.findViewById(R.id.et_search);
        Button btn_seach=selectview.findViewById(R.id.btn_search);
        Button btn_synchronous=selectview.findViewById(R.id.btn_synchronous);

        customeradapter = new SimpleAdapter(mContext, customerlist, R.layout.select_ccscustomer_item, new String[]{"CustName", "CutCode","CustLink", "CustTel","CustAddr"}, new int[]{R.id.customer_Name, R.id.customer_id,R.id.customer_Link, R.id.customer_Tel,R.id.corpaddr});
        list_customer.setAdapter(customeradapter);

        tv_total.setText("（共 " + customerlist.size() + " 条）");


        CustomerNormalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
                .setView(selectview)
                .create();
        CustomerNormalDialog.setCanceledOnTouchOutside(false);
        CustomerNormalDialog.show();

        if (CustomerNormalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = CustomerNormalDialog.getWindow().getAttributes();
            lp.width = 1200; // 宽度，可根据屏幕宽度进行计算
//            lp.height = 900;
            lp.gravity = Gravity.CENTER;
            CustomerNormalDialog.getWindow().setAttributes(lp);
        }
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerlist.clear();
                if (customeradapter!=null) {
                    customeradapter.notifyDataSetChanged();
                }
                DownLoadCCSCustomerData(BrandingCode,edit_CCSCust.getText().toString().trim());
            }
        });

        btn_synchronous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowSyncCCSCustDialog();
            }
        });

        list_customer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                Map<String, Object> item = (Map<String, Object>) listView.getItemAtPosition(i);
                BrandingCustCode=item.get("CutCode").toString();
                ShowBingCCSCustDialog(item.get("CustName").toString(),item.get("CutCode").toString());
            }
        });
    }

    //是否同步客户据到CCS
    private void ShowSyncCCSCustDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final EditText editText = new EditText(mContext);

        final AlertDialog normalDialog =
                new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                        .setIcon(R.drawable.ccs)
                        .setTitle("同步提示")
                        .setView(editText)
                        .setCancelable(false)
                        .setMessage("是否同步SCS客户数据至CCS?（客户名称可编辑）")
                        .setPositiveButton("确定", null)
                        .setNegativeButton("关闭", null )
                        .create();
        // 显示
        normalDialog.show();
        editText.setText(TraderAlias_name);
        editText.setSelection(TraderAlias_name.length());//将光标移至文字末尾

        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"保存",Toast.LENGTH_SHORT).show();
                String custname=editText.getText().toString().trim();
                if(custname.equals("")){
                    Toast.makeText(mContext,"请输入要同步的客户名称数据",Toast.LENGTH_SHORT).show();
                }else{
                    ScsNewCustBindToCcs(custname);
                    normalDialog.dismiss();
                }
            }
        });

        normalDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"保存",Toast.LENGTH_SHORT).show();
                normalDialog.dismiss();

            }
        });

    }

    //是否绑定客户数据到CCS
    private void ShowBingCCSCustDialog(String CCScustName, final String CCScustId){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog);
        normalDialog.setIcon(R.drawable.ccs);
        normalDialog.setTitle("绑定提示");
        normalDialog.setMessage("是否绑定CCS客户【"+CCScustName+"】?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScsCustBindToCcs(CCScustId);
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    // SCS客户和CCS客户绑定
    private void ScsCustBindToCcs(final String tCcsCustCode) {
        MyProgressDialog.show(this, "正在绑定客户数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsCustBindToCcs(TraderSysId,tCcsCustCode,BrandingCode,AgentCode);
                    ShowMessage.ShowMsg(hand, 3, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "绑定客户出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    // SCS新客户同步CCS并绑定
    private void ScsNewCustBindToCcs(final String tTraderAlias) {
        MyProgressDialog.show(this, "正在同步客户数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsNewCustBindToCcs(TraderSysId,BrandingCode,AgentCode,tTraderAlias);
                    ShowMessage.ShowMsg(hand, 3, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "同步客户出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    //下载CCS客户
    private void DownLoadCCSCustomerData(final String Brandcode, final String et_search) {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> tlist = accWeb.GetCcsCustomer(Brandcode,AgentCode,et_search);
                    customerlist.addAll(tlist);
//                    Log.d("main",Requestedlist.toString());
                    ShowMessage.ShowMsg(hand, 2, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载CCS客户出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    //显示CCS门店数据
    private void ShowCCSStoreDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        View selectview = LayoutInflater.from(mContext).inflate(R.layout.activity_select_ccsstore,null);

        ListView list_store=selectview.findViewById(R.id.listview_ccsstore);
        TextView tv_total=selectview.findViewById(R.id.tv_total);
        final EditText edit_CCSCust=selectview.findViewById(R.id.et_search);
        Button btn_seach=selectview.findViewById(R.id.btn_search);
        Button btn_synchronous=selectview.findViewById(R.id.btn_synchronous);

        storeadapter = new SimpleAdapter(this, storelist, R.layout.select_ccsstore_item, new String[]{
                "StoreName", "StoreId","CustLink", "CustTel","CustAddr"}, new int[]{R.id.store_name, R.id.store_id,R.id.store_Link, R.id.store_Tel,R.id.corpaddr});
        list_store.setAdapter(storeadapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + storelist.size() + " 条）");

        StoreNormalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
                .setView(selectview)
                .create();

        StoreNormalDialog.setCanceledOnTouchOutside(false);
        StoreNormalDialog.show();

        if (StoreNormalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = StoreNormalDialog.getWindow().getAttributes();
            lp.width = 1200; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            StoreNormalDialog.getWindow().setAttributes(lp);
        }
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storelist.clear();
                if (storeadapter!=null) {
                    storeadapter.notifyDataSetChanged();
                }
                DownLoadCCSStoreData(edit_CCSCust.getText().toString().trim(),BrandingCustCode);
            }
        });

        btn_synchronous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowSyncCCSStoreDialog();
            }
        });

        list_store.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                Map<String, Object> item = (Map<String, Object>) listView.getItemAtPosition(i);
                showBingStoreDialog((String) item.get("StoreName"), (String) item.get("CutCode"), (String) item.get("StoreId"));
            }
        });
    }


    //是否同步门店数据到CCS
    private void ShowSyncCCSStoreDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final EditText editText = new EditText(mContext);

        final AlertDialog normalDialog =new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
                .setTitle("同步提示")
                .setView(editText)
                .setCancelable(false)
                .setMessage("是否同步SCS门店数据至CCS?（门店名称可编辑）")
                .setPositiveButton("确定",null)
                .setNegativeButton("关闭", null)
                .create();
        // 显示
        normalDialog.show();

        editText.setText(Storealias_name);
        editText.setSelection(Storealias_name.length());//将光标移至文字末尾


        normalDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"保存",Toast.LENGTH_SHORT).show();
                {
                    String storename=editText.getText().toString().trim();
                    if(storename.equals("")){
                        Toast.makeText(mContext,"请输入要同步的门店名称数据",Toast.LENGTH_SHORT).show();
                    }else{
//                        Toast.makeText(mContext,storename,Toast.LENGTH_SHORT).show();
                        ScsNewStoreBindToCcs(storename);
                        normalDialog.dismiss();
                    }
                }
            }
        });

        normalDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"保存",Toast.LENGTH_SHORT).show();
                normalDialog.dismiss();

            }
        });

    }

    //是否绑定客户数据到CCS
    private void showBingStoreDialog(String CCScustName, final String CCScustId, final String CCSstoreId){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog);
        normalDialog.setIcon(R.drawable.ccs);
        normalDialog.setTitle("绑定提示");
        normalDialog.setMessage("是否绑定CCS门店【"+CCScustName+"】?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScsStoreBindToCcs(CCScustId,CCSstoreId);
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }




    //获取CCS门店数据
    private void DownLoadCCSStoreData(final String et_search, final String CCSCutsCode) {
        MyProgressDialog.show(this, "正在获取数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> tlist = accWeb.GetCcsStore(BrandingCode,AgentCode,et_search,CCSCutsCode);
                    storelist.addAll(tlist);
//                    Log.d("main",Requestedlist.toString());
                    ShowMessage.ShowMsg(hand, 4, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载CCS门店出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    // SCS门店和CCS门店绑定
    private void ScsStoreBindToCcs(final String tCcsCustCode,final String tCcsStoreCode) {
        MyProgressDialog.show(this, "正在绑定门店数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsStoreBindToCcs(TraderSysId,StoreSysId,tCcsCustCode,tCcsStoreCode,BrandingCode,AgentCode);
                    ShowMessage.ShowMsg(hand, 5, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "绑定门店出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    // SCS新门店同步CCS并绑定
    private void ScsNewStoreBindToCcs(final String tAlias) {
        MyProgressDialog.show(this, "正在同步门店数据...", false, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsNewStoreBindToCcs(StoreSysId,BrandingCode,AgentCode,tAlias);
                    ShowMessage.ShowMsg(hand, 5, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "同步门店出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
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

//class SortListSupplierComparator implements Comparator<SupplierInfor> {
//
//    @Override
//    public int compare(SupplierInfor map1, SupplierInfor map2) {
//
//        //供应商代号不相同比较供应商代号
//        if(!map1.getSupplierId().toString().equals(map2.getSupplierId().toString())){
//            return map1.getSupplierId().toString().compareToIgnoreCase(map2.getSupplierId()
//            .toString());
//        }
//        //供应商代号相同比较供应商名称
//        else{
//            return map1.getSupplierName().toString().compareToIgnoreCase(map2.getSupplierName()
//            .toString());
//        }
//    }
//
//}
//
