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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Lens_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.entity.SyncCustomers;
import com.example.wholesalersend.entity.SyncStores;
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
 * @ClassName: SelectCCSstore
 * @Description: 选择CCS门店
 * @Author: lijin
 * @Date: 2024/12/10 12:00
 */
public class SelectCCSstore extends Activity {

    private Context mContext;

    private Button btn_seach;
    private Button btn_synchronous;//同步按钮

    private EditText et_search;
    private ListView list_store;

    private List<Map<String, Object>> customerlist = new ArrayList<Map<String, Object>>();

    private TextView tv_total;

    private AccessWeb accWeb;
    private Handler hand;
    private SysUserInfo sysUserInfo;

    private String lsv_aim = "",TraderSysId="",StoreSysId="",RetailId="",RetailName="",CompanyName="",CompanyId="",Brandcode="",AgentCode="",CCSCutsCode="",Storealias_name="";

    private String  company_id = "", company_name = "", stock_id = "",retail_id="",retail_name="", stock_name="",TraderAlias_name="";//分店退货接收参数

    private String supplier_id="",supplier_name="",purchecklno="",saplno="",store_id="";//有单有入库有明细分店发货  有单有入库无明细分店发货  有单有入库镜片发货

    private String scanBillNo="",scanBillNum="";//扫描单号，扫描数量


    private final int Lic_SelectSure = 3;

    private SimpleAdapter adapter;

    private String StockId="",StockName="";//换货补标 选择仓库


    private String CCScustId="",CCSstoreId="",CCScustName="",CCSstoreName="";//CCS选中绑定的客户id，CCS选中绑定的门店id

    private Dialog StoreNormalDialog =null,StoreBindNormalDialog=null;

    private String[] Provicelist;//省份集合
    private String[] Citylist;//城市集合

    private String Citytype;//显示是零售商还是分销店的城市
    private TextView tv_traderprovicename,tv_storeprovicename,tv_tradercityname,tv_storecityname;//零售商城市，分销店城市

    //-----------------用于同步提交的时候的数据---------------------------
    private String StoreId="";   //门店代号
    private String StoreName="";		//门店名称
    private String StoreLink="";		 	//联系人（门店）
    private String StoreTel="";		   //电话（门店）
    private String StoreCorpAdde="";	// 地址（门店）
    private String Alias="";	//门店别名
    private String StoreProviceName="";	//省份（门店）
    private String StoreCityName="";   //城市（门店）
    private String StoreCounty="";	//区县（门店）
    private String TraderId="";		   //客户代号
    private String TraderName="";	//客户名称
    private String TraderLink="";//客户联系人
    private String TraderSaleId="";   //业务员代号（客户）
    private String TraderSaleName="";		//业务员名称（客户）
    private String TraderTel="";	   //电话（客户）
    private String TraderCorpAddr="";  //地址（客户）
    private String TraderProviceName="";		//省份（客户）
    private String TraderCityName="";  //城市（客户）
    private String TraderCounty="";	//区县（客户）
    private String TraderAlias="";	   //客户别名
    private String BrandName="";  //品牌
    private String CustSysCode="";   //客户系统代号
    private String StoreSysCode="";	   //门店系统代号
    private String TraderLicenceNo="";//客户营业执照号
    private String StoreLicenceNo="";//门店营业执照号

    private RelativeLayout relat_search_title;//搜索提示布局

    private String LinkName="";//模糊搜索的数据

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ccsstore);

        Intent getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");
        TraderSysId=getIntent.getStringExtra("trader_sysid");
        StoreSysId=getIntent.getStringExtra("store_sysid");

        Brandcode=getIntent.getStringExtra("Brand_code");
        AgentCode=getIntent.getStringExtra("Agent_Code");
        CCSCutsCode=getIntent.getStringExtra("CCSCuts_Code");

        if (getIntent.getStringExtra("LinkName")!=null) {
            LinkName = getIntent.getStringExtra("LinkName");
        }

        if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
            RetailId=getIntent.getStringExtra("retail_id");
            RetailName=getIntent.getStringExtra("retail_name");
            CompanyName=getIntent.getStringExtra("company_name");
            CompanyId=getIntent.getStringExtra("company_id");
            TraderAlias_name=getIntent.getStringExtra("TraderAlias_name");
            Storealias_name=getIntent.getStringExtra("storealias_name");


        }else  if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")||lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
            //无单选客退货扫描，镜片无单选客退货扫描
            company_id = getIntent.getStringExtra("company_id");
            company_name = getIntent.getStringExtra("company_name");
            stock_id = getIntent.getStringExtra("stock_id");
            stock_name = getIntent.getStringExtra("stock_name");
            retail_id= getIntent.getStringExtra("retail_id");
            retail_name= getIntent.getStringExtra("retail_name");
            TraderAlias_name= getIntent.getStringExtra("alias_name");
            Storealias_name=getIntent.getStringExtra("storealias_name");

        }else  if (lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock")) {
            //有单有入库有明细分店发货  有单有入库无明细分店发货  有单有入库镜片发货
            supplier_id = getIntent.getStringExtra("supplier_id");
            supplier_name = getIntent.getStringExtra("supplier_name");
            stock_id = getIntent.getStringExtra("stock_id");
            stock_name = getIntent.getStringExtra("stock_name");
            purchecklno = getIntent.getStringExtra("purchecklno");
            saplno=getIntent.getStringExtra("saplno");
            store_id= getIntent.getStringExtra("store_id");
            TraderAlias_name = getIntent.getStringExtra("TraderAlias_name");
            Storealias_name=getIntent.getStringExtra("storealias_name");
        }

        scanBillNo=getIntent.getStringExtra("scanBillNo");
        scanBillNum=getIntent.getStringExtra("scanBillNum");

        mContext = this;
        accWeb = new AccessWeb(getApplicationContext());
        hand = new handShowMsg();
        sysUserInfo=new SysUserInfo(mContext);

        tv_total=findViewById(R.id.tv_total);

        relat_search_title=findViewById(R.id.relat_search_title);
        list_store = findViewById(R.id.listview_ccsstore);

        btn_synchronous=findViewById(R.id.btn_synchronous);
        btn_synchronous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showNormalDialog();
                GetScsCustStore();
            }
        });

        btn_seach = findViewById(R.id.btn_search);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerlist.clear();
                if (adapter!=null) {
                    adapter.notifyDataSetChanged();
                }
                relat_search_title.setVisibility(View.GONE);
                list_store.setVisibility(View.VISIBLE);
                DownLoadDataThread();
            }
        });

        et_search = findViewById(R.id.et_search);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        customerlist.clear();
                        if (adapter!=null) {
                            adapter.notifyDataSetChanged();
                        }
                        relat_search_title.setVisibility(View.GONE);
                        list_store.setVisibility(View.VISIBLE);
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });

        et_search.setText(LinkName);

//        RecyclerView_supplier=findViewById(R.id.RecyclerView_supplier);

        list_store.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                Map<String, Object> item = (Map<String, Object>) listView.getItemAtPosition(i);

                CCScustId= (String) item.get("CutCode");
                CCSstoreId=(String) item.get("StoreId");

                CCScustName=(String) item.get("CustName");
                CCSstoreName=(String) item.get("StoreName");

                showBingNormalDialog((String) item.get("CutCode"), (String) item.get("StoreId"));
//                Intent intent = new Intent();
//                intent.putExtra("BrandingCode", (String) item.get("BrandingCode"));
//                intent.putExtra("BrandCode", (String) item.get("BrandCode"));
//                intent.putExtra("BrandName", (String) item.get("BrandName"));
//
//                setResult(RESULT_OK, intent);
//                finish();
            }
        });

//        initRecyclerView();
//        DownLoadDataThread();
        GetScsProvice();
    }

    //是否同步门店数据到CCS
    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        View selectview = LayoutInflater.from(mContext).inflate(R.layout.dialog_syncccsstore,null);

        final EditText ed_tradername=selectview.findViewById(R.id.ed_tradername);//零售商名称
        final EditText ed_traderlink=selectview.findViewById(R.id.ed_traderlink);//零售商联系人
        final EditText ed_tradertel=selectview.findViewById(R.id.ed_tradertel);//零售商联系电话
        final EditText ed_tradercorpaddr=selectview.findViewById(R.id.ed_tradercorpaddr);//零售商联系地址
        tv_traderprovicename=selectview.findViewById(R.id.tv_traderprovicename);//零售商省份
        tv_tradercityname=selectview.findViewById(R.id.tv_tradercityname);//零售商城市

        final EditText ed_storename=selectview.findViewById(R.id.ed_storename);//分销店名称
        final EditText ed_storelink=selectview.findViewById(R.id.ed_storelink);//分销店联系人
        final EditText ed_storetel=selectview.findViewById(R.id.ed_storetel);//分销店联系电话
        final EditText ed_storecorpadde=selectview.findViewById(R.id.ed_storecorpadde);//分销店联系地址
        tv_storeprovicename=selectview.findViewById(R.id.tv_storeprovicename);//分销店省份
        tv_storecityname=selectview.findViewById(R.id.tv_storecityname);//分销店城市

        Button btn_cancel=selectview.findViewById(R.id.btn_cancel);//关闭
        Button btn_submit=selectview.findViewById(R.id.btn_submit);//提交


        ed_tradername.setText(TraderAlias);
        ed_traderlink.setText(TraderLink );
        ed_tradertel.setText(TraderTel);
        ed_tradercorpaddr.setText(TraderCorpAddr);
        tv_traderprovicename.setText(TraderProviceName);
        tv_tradercityname.setText(TraderCityName);

//        ed_storename.setText(Alias);
        ed_storename.setText(StoreName);
        ed_storelink.setText(StoreLink);
        ed_storetel.setText(StoreTel);
        ed_storecorpadde.setText(StoreCorpAdde);
        tv_storeprovicename.setText(StoreProviceName);
        tv_storecityname.setText(StoreCityName);

        StoreNormalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
                .setView(selectview)
                .create();

        StoreNormalDialog.setCanceledOnTouchOutside(false);
        StoreNormalDialog.show();

        if (StoreNormalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = StoreNormalDialog.getWindow().getAttributes();
//            lp.width = 800; // 宽度，可根据屏幕宽度进行计算
//            lp.height = 700; // 宽度，可根据屏幕宽度进行计算
            if (sysUserInfo.getOldVersion().equals("T8")){
                lp.width = 600; // 宽度，可根据屏幕宽度进行计算
//                lp.height = 600; // 宽度，可根据屏幕宽度进行计算
            }else{
                lp.width = 800; // 宽度，可根据屏幕宽度进行计算
                lp.height = 700; // 宽度，可根据屏幕宽度进行计算
            }
            lp.gravity = Gravity.CENTER;
            StoreNormalDialog.getWindow().setAttributes(lp);
        }
        //关闭
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StoreNormalDialog.dismiss();
            }
        });
        //提交
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ed_tradername.getText().toString().trim().equals("")){
                    ShowMessage.Show(mContext, "请输入零售店名称");
                }else if(tv_traderprovicename.getText().toString().equals("")){
                    ShowMessage.Show(mContext, "请选择零售店省份");
                }else if(tv_tradercityname.getText().toString().equals("")){
                    ShowMessage.Show(mContext, "请选择零售店城市");
                }else if(ed_storename.getText().toString().trim().equals("")){
                    ShowMessage.Show(mContext, "请输入分销店名称");
                }else if(tv_storeprovicename.getText().toString().equals("")){
                    ShowMessage.Show(mContext, "请选择分销店省份");
                }else if(tv_storecityname.getText().toString().equals("")){
                    ShowMessage.Show(mContext, "请选择分销店城市");
                }else{

                    TraderAlias=ed_tradername.getText().toString().trim();
//                    Alias=ed_storename.getText().toString().trim();

                    Gson gson=new Gson();

                    SyncCustomers syncCustomers=new SyncCustomers();
                    syncCustomers.setEditMode("A");
                    syncCustomers.setCutCode(TraderId);
                    syncCustomers.setCustName(ed_tradername.getText().toString().trim());
                    syncCustomers.setCustLink(ed_traderlink.getText().toString().trim());
                    syncCustomers.setCustTel(ed_tradertel.getText().toString().trim());
                    syncCustomers.setCustMobile(ed_tradertel.getText().toString().trim());
                    syncCustomers.setSaleId(TraderSaleId);
                    syncCustomers.setSaleName(TraderSaleName);
                    syncCustomers.setBrandName(BrandName);
                    syncCustomers.setProviceName(tv_traderprovicename.getText().toString().trim());
                    syncCustomers.setCityName(tv_tradercityname.getText().toString().trim());
                    syncCustomers.setCountyName(TraderCounty);
                    syncCustomers.setCustAddr(ed_tradercorpaddr.getText().toString().trim());
                    syncCustomers.setLicenceNo(TraderLicenceNo);
                    syncCustomers.setCorpName(TraderName);
                    syncCustomers.setCcsCustId("");

                    SyncStores syncStores=new SyncStores();
                    syncStores.setEditMode("A");
                    syncStores.setCutCode(TraderId);
                    syncStores.setStoreId(StoreId);
                    syncStores.setStoreName(ed_storename.getText().toString().trim());
                    syncStores.setCustLink(ed_storelink.getText().toString().trim());
                    syncStores.setCustMobile(ed_storetel.getText().toString().trim());
                    syncStores.setBrandName(BrandName);
                    syncStores.setProviceName(tv_storeprovicename.getText().toString().trim());
                    syncStores.setCityName(tv_storecityname.getText().toString().trim());
                    syncStores.setCountyName(StoreCounty);
                    syncStores.setCustAddr(ed_storecorpadde.getText().toString().trim());
                    syncStores.setLicenceNo(StoreLicenceNo);
                    syncStores.setCorpName(Alias);
                    syncStores.setCcsStoreId("");

//                    Log.d("main", "tCustJson="+gson.toJson(syncCustomers));
//                    Log.d("main", "tStoreJson ="+gson.toJson(syncStores));

                    ScsNewSyncToCcs(gson.toJson(syncCustomers),gson.toJson(syncStores));//同步接口
                }
            }
        });

        tv_traderprovicename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Citytype="trader";
                ShowListProviceDialog();

            }
        });

        tv_storeprovicename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Citytype="store";
                ShowListProviceDialog();

            }
        });

        tv_tradercityname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tv_traderprovicename.getText().toString().trim().equals("")){
                    Citytype="trader";
                    GetScsCity(tv_traderprovicename.getText().toString().trim());
                }else{
                    ShowMessage.Show(mContext, "请先选择零售商省份");
                }
            }
        });
        tv_storecityname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tv_storeprovicename.getText().toString().trim().equals("")){
                    Citytype="store";
                    GetScsCity(tv_storeprovicename.getText().toString().trim());
                }else{
                    ShowMessage.Show(mContext, "请先选择分销店省份");
                }
            }
        });
    }


    private void ShowListProviceDialog() {
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog);
        listDialog.setTitle("选择省份");
        listDialog.setItems(Provicelist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String Provice=Provicelist[which];
                if (Citytype.equals("trader")){
                    //零售商点开省份
//                    if (!TraderProviceName.equals(Provice)){
                    tv_traderprovicename.setText(Provice);
                    tv_tradercityname.setText("");
                    TraderCounty="";
                    TraderCityName="";
//                    }
                }else if (Citytype.equals("store")){
                    //分销店点开省份
//                    if (!StoreProviceName.equals(Provice)){
                    tv_storeprovicename.setText(Provice);
                    tv_storecityname.setText("");
                    StoreCityName="";
//                    }
                }
            }
        });
        listDialog.show();
    }

    //显示城市
    private void ShowListCityDialog() {
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog);
        listDialog.setTitle("选择城市");
        listDialog.setItems(Citylist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tcity=Citylist[which];
                if (Citytype.equals("trader")){
                    //零售商点开城市
                    if (!TraderCityName.equals(tcity)){
                        tv_tradercityname.setText(tcity);
                        StoreCounty="";
                    }
                }else if (Citytype.equals("store")){
                    //分销店点开城市
                    if (!StoreCityName.equals(tcity)){
                        tv_storecityname.setText(tcity);
                    }
                }
            }
        });
        listDialog.show();
    }



    //是否绑定客户数据到CCS
    private void showBingNormalDialog(final String CCScustId, final String CCSstoreId){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
//        final AlertDialog.Builder normalDialog =
//                new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog);
//        normalDialog.setIcon(R.drawable.ccs);
//        normalDialog.setTitle("绑定提示");
//        normalDialog.setMessage("是否绑定CCS门店【"+CCScustName+"】?");
//        normalDialog.setPositiveButton("确定",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        ScsCustBindToCcs(CCScustId);
//                    }
//                });
//        normalDialog.setNegativeButton("关闭",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //...To-do
//                    }
//                });
//        // 显示
//        normalDialog.show();

        View selectview = LayoutInflater.from(mContext).inflate(R.layout.dialog_bindingtip,null);

        final TextView tv_ccscustname=selectview.findViewById(R.id.tv_ccscustname);//绑定的客户名称
        final TextView tv_ccsstorename=selectview.findViewById(R.id.tv_ccsstorename);//绑定的门店名称

        Button btn_cancel=selectview.findViewById(R.id.btn_cancel);//关闭
        Button btn_submit=selectview.findViewById(R.id.btn_submit);//提交


        tv_ccscustname.setText(CCScustName);
        tv_ccsstorename.setText(CCSstoreName);


        StoreBindNormalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.drawable.ccs)
                .setView(selectview)
                .create();

        StoreBindNormalDialog.setCanceledOnTouchOutside(false);
        StoreBindNormalDialog.show();

        if (StoreBindNormalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = StoreBindNormalDialog.getWindow().getAttributes();
            lp.width = 650; // 宽度，可根据屏幕宽度进行计算
            lp.height = 400; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            StoreBindNormalDialog.getWindow().setAttributes(lp);
        }
        //关闭
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StoreBindNormalDialog.dismiss();
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScsCustBindToCcs(CCScustId);
            }
        });

    }




    //获取CCS门店数据
    private void DownLoadDataThread() {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    customerlist = accWeb.GetCcsStore(Brandcode,AgentCode,et_search.getText().toString(),CCSCutsCode);
//                    Log.d("main",Requestedlist.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    // SCS客户和CCS客户绑定
    private void ScsCustBindToCcs(final String tCcsCustCode) {
        MyProgressDialog.show(this, "正在绑定客户数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsCustBindToCcs(TraderSysId,tCcsCustCode,Brandcode,AgentCode);
                    ShowMessage.ShowMsg(hand, 1, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "绑定客户出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    // SCS门店和CCS门店绑定
    private void ScsStoreBindToCcs(final String tCcsCustCode,final String tCcsStoreCode) {
        MyProgressDialog.show(this, "正在绑定客户数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsStoreBindToCcs(TraderSysId,StoreSysId,tCcsCustCode,tCcsStoreCode,Brandcode,AgentCode);
                    ShowMessage.ShowMsg(hand, 3, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "绑定门店出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }



    //获取SCS具体客户和门店信息
    private void GetScsCustStore() {
        MyProgressDialog.show(this, "正在获取门店数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.GetScsCustStore(StoreSysId);
//                    Log.d("main", result);
                    JSONObject listjson = new JSONObject(result);
                    JSONArray jsonArray = listjson.getJSONArray("Data");
                    StoreId=jsonArray.getJSONObject(0).getString("StoreId");   //门店代号
                    StoreName=jsonArray.getJSONObject(0).getString("StoreName");	//门店名称
                    StoreLink=jsonArray.getJSONObject(0).getString("StoreLink");		 	//联系人（门店）
                    StoreTel=jsonArray.getJSONObject(0).getString("StoreTel");		   //电话（门店）
                    StoreCorpAdde=jsonArray.getJSONObject(0).getString("StoreCorpAdde");	// 地址（门店）
                    Alias=jsonArray.getJSONObject(0).getString("Alias");	//门店别名
                    StoreProviceName=jsonArray.getJSONObject(0).getString("StoreProviceName");	//省份（门店）
                    StoreCityName=jsonArray.getJSONObject(0).getString("StoreCityName"); //城市（门店）
                    StoreCounty=jsonArray.getJSONObject(0).getString("StoreCounty");	//区县（门店）
                    TraderId=jsonArray.getJSONObject(0).getString("TraderId");		   //客户代号
                    TraderName=jsonArray.getJSONObject(0).getString("TraderName");	//客户名称
                    TraderLink=jsonArray.getJSONObject(0).getString("TraderLink");
                    TraderSaleId=jsonArray.getJSONObject(0).getString("TraderSaleId");   //业务员代号（客户）
                    TraderSaleName=jsonArray.getJSONObject(0).getString("TraderSaleName");	//业务员名称（客户）
                    TraderTel=jsonArray.getJSONObject(0).getString("TraderTel");	   //电话（客户）
                    TraderCorpAddr=jsonArray.getJSONObject(0).getString("TraderCorpAddr"); //地址（客户）
                    TraderProviceName=jsonArray.getJSONObject(0).getString("TraderProviceName");	//省份（客户）
                    TraderCityName=jsonArray.getJSONObject(0).getString("TraderCityName");  //城市（客户）
                    TraderCounty=jsonArray.getJSONObject(0).getString("TraderCounty");	//区县（客户）
                    TraderAlias=jsonArray.getJSONObject(0).getString("TraderAlias");  //客户别名
                    BrandName=jsonArray.getJSONObject(0).getString("BrandName");  //品牌
                    CustSysCode=jsonArray.getJSONObject(0).getString("CustSysCode");  //客户系统代号
                    StoreSysCode=jsonArray.getJSONObject(0).getString("StoreSysCode");	   //门店系统代号

                    TraderLicenceNo=jsonArray.getJSONObject(0).getString("TraderLicenceNo");	//客户营业执照号
                    StoreLicenceNo=jsonArray.getJSONObject(0).getString("StoreLicenceNo");	//门店营业执照号

                    ShowMessage.ShowMsg(hand, 5, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "获取门店详情出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }



    //获取省份
    private void GetScsProvice() {
        MyProgressDialog.show(this, "正在获取省份数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.GetScsProvice();
//                    Log.d("main", result);

                    JSONObject listjson = new JSONObject(result);
                    JSONArray jsonArray = listjson.getJSONArray("Data");
                    Provicelist= new String[jsonArray.length()];
                    for (int i = 0; i <jsonArray.length(); i++) {
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        String tProvice=jsonArray.getJSONObject(i).getString("ProviceName");
                        Provicelist[i]=tProvice;
                    }
                    ShowMessage.ShowMsg(hand, 6, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "获取省份出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    private void GetScsCity(final String tProviceName) {
        MyProgressDialog.show(this, "正在获取城市数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.GetScsCity(tProviceName);
//                    Log.d("main", result);
                    JSONObject listjson = new JSONObject(result);
                    JSONArray jsonArray = listjson.getJSONArray("Data");
                    Citylist= new String[jsonArray.length()];
                    for (int i = 0; i <jsonArray.length(); i++) {
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        String tCity=jsonArray.getJSONObject(i).getString("CityName");
                        Citylist[i]=tCity;
                    }
                    ShowMessage.ShowMsg(hand, 7, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "获取城市出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    // 同步SCS数据到CCS并绑定
    private void ScsNewSyncToCcs(final String tCustJson, final String tStoreJson) {
        MyProgressDialog.show(this, "正在同步数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = accWeb.ScsNewSyncToCcs(Brandcode,AgentCode,tCustJson,tStoreJson);
                    ShowMessage.ShowMsg(hand, 8, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "同步数据出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    // SCS新门店同步CCS并绑定
//    private void ScsNewStoreBindToCcs(final String tAlias) {
//        MyProgressDialog.show(this, "正在同步门店数据...", false, false);
//        Thread sendCode = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String result = accWeb.ScsNewStoreBindToCcs(StoreSysId,Brandcode,AgentCode,tAlias);
//                    ShowMessage.ShowMsg(hand, 3, "success");
//                } catch (Exception e) {
//                    ShowMessage.ShowMsg(hand, "同步门店出错" + e.getMessage());
//                }
//            }
//        });
//        sendCode.start();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
                    if (lsv_aim == null || lsv_aim.isEmpty()) {
                        return;
                    }
//                    Intent intent = new Intent(this, SelectStock.class);
//                    intent.putExtra("supplier_id", (String) ItemInfor.getSupplierId());
//                    intent.putExtra("supplier_syscode", (String) ItemInfor.getSuppSysCode());
//                    intent.putExtra("supplier_name", (String) ItemInfor.getSupplierName());
//                    intent.putExtra("aim", lsv_aim);
//                    startActivity(intent);
                    break;
                default:
                    break;
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    initListView(customerlist);
                    break;
                case 1:
                    //绑定客户成功后绑定门店
                    ScsStoreBindToCcs(CCScustId,CCSstoreId);
                    break;
                case 3:
                    if (StoreBindNormalDialog!=null){
                        StoreBindNormalDialog.dismiss();
                    }
                    ShowMessage.MessageBox(mContext, "温馨提示","绑定成功",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent scsintent = new Intent();
                            scsintent.putExtra("CCScustName", CCScustName);
                            scsintent.putExtra("CCSstoreName", CCSstoreName);
                            scsintent.putExtra("aim", lsv_aim);
                            setResult(RESULT_OK, scsintent);
                            finish();
                        }
                    });

                    //绑定CCS门店 成功就跳扫描界面
//                    Intent scsintent = new Intent();
//                    if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
//                        scsintent.putExtra("retail_id", RetailId);
//                        scsintent.putExtra("retail_name", RetailName);
//                        scsintent.putExtra("company_name", CompanyName);
//                        scsintent.putExtra("company_id", CompanyId);
//                        scsintent.putExtra("TraderAlias_name", TraderAlias_name);
//                    }
//                    //镜片有入库无单分店发货
//                    else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
//                        scsintent.putExtra("retail_id", RetailId);
//                        scsintent.putExtra("retail_name", RetailName);
//                        scsintent.putExtra("company_name", CompanyName);
//                        scsintent.putExtra("company_id", CompanyId);
//                        scsintent.putExtra("TraderAlias_name", TraderAlias_name);
//                    }
//                    //分店无单选客退货
//                    else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)) {
//                        scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
//
//                        scsintent.putExtra("company_id", company_id);
//                        scsintent.putExtra("company_name", company_name);
//                        scsintent.putExtra("stock_id", stock_id);
//                        scsintent.putExtra("stock_name", stock_name);
//                        scsintent.putExtra("retail_id", retail_id);
//                        scsintent.putExtra("retail_name", retail_name);
//                        scsintent.putExtra("alias_name", TraderAlias_name);
//                    }
//                    //镜片分店无单选客退货
//                    else if ("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)) {
//                        scsintent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
//
//                        scsintent.putExtra("company_id", company_id);
//                        scsintent.putExtra("company_name", company_name);
//                        scsintent.putExtra("stock_id", stock_id);
//                        scsintent.putExtra("stock_name", stock_name);
//                        scsintent.putExtra("retail_id", retail_id);
//                        scsintent.putExtra("retail_name", retail_name);
//                        scsintent.putExtra("alias_name", TraderAlias_name);
//                    } else if ("P_Dv_OutStock_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                        //有单有入库有明细分店发货
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock.class);
//                        scsintent.putExtra("purchecklno",purchecklno);
//                        scsintent.putExtra("saplno", saplno);
//                        scsintent.putExtra("supplier_name", supplier_name);
//                        scsintent.putExtra("stock_name", stock_name);
//                        scsintent.putExtra("supplier_id", supplier_id);
//                        scsintent.putExtra("stock_id", stock_id);
//                        scsintent.putExtra("store_id", store_id);
//                    }
//                    else if ("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail".equals(lsv_aim)) {
//                        //有单有入库无明细分店发货
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail.class);
//                        scsintent.putExtra("purchecklno",purchecklno);
//                        scsintent.putExtra("saplno", saplno);
//                        scsintent.putExtra("supplier_name", supplier_name);
//                        scsintent.putExtra("stock_name", stock_name);
//                        scsintent.putExtra("supplier_id", supplier_id);
//                        scsintent.putExtra("stock_id", stock_id);
//                        scsintent.putExtra("store_id", store_id);
//                    }
//                    else if ("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock".equals(lsv_aim)) {
//                        //镜片有单有入库分店发货
//                        scsintent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_BeInStock.class);
//                        scsintent.putExtra("purchecklno",purchecklno);
//                        scsintent.putExtra("saplno", saplno);
//                        scsintent.putExtra("supplier_name", supplier_name);
//                        scsintent.putExtra("stock_name", stock_name);
//                        scsintent.putExtra("supplier_id", supplier_id);
//                        scsintent.putExtra("stock_id", stock_id);
//                        scsintent.putExtra("store_id", store_id);
//                    }
//
//                    scsintent.putExtra("scanBillNo", scanBillNo);
//                    scsintent.putExtra("scanBillNum", scanBillNum);
//                    scsintent.putExtra("CCScustName", CCScustName);
//                    scsintent.putExtra("CCSstoreName", CCSstoreName);
//                    scsintent.putExtra("aim", lsv_aim);
//                    setResult(RESULT_OK, scsintent);
//                    finish();

                    break;
                case 5:
                    //获取门店数据显示在同步信息弹窗里
                    MyProgressDialog.close();
                    showNormalDialog();
                    break;
                case 6:
                    //获取城市
//                    Citytype="trader";
//                    ShowListProviceDialog();
                    break;
                case 7:
                    //显示城市弹窗
                    ShowListCityDialog();
                    break;
                case 8:
                    //同步CCS门店功能 成功就跳扫描界面
                    if (StoreNormalDialog!=null){
                        StoreNormalDialog.dismiss();
                    }

                    ShowMessage.MessageBox(mContext, "温馨提示","同步成功",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.putExtra("CCScustName", TraderAlias);
                            intent.putExtra("CCSstoreName", StoreName);
                            intent.putExtra("aim", lsv_aim);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    break;

                default:
                    break;
            }

            MyProgressDialog.close();
        }
    }

    public void initListView(List<Map<String, Object>> mList) {
//        Collections.sort(list, new SortListMapComparator("PeiGoodLno"));
        if (mList.size()==0){
            ShowMessage.Show(mContext, "暂无数据");
            return;
        }
        adapter = new SimpleAdapter(this, mList, R.layout.select_ccsstore_item, new String[]{"CustName","CutCode","StoreName", "StoreId","CustLink", "CustTel","CustAddr"}, new int[]{R.id.customr_name,R.id.customr_id,R.id.store_name, R.id.store_id,R.id.store_Link, R.id.store_Tel,R.id.corpaddr});
        list_store.setAdapter(adapter);
        MyProgressDialog.close();
        tv_total.setText("（共 " + mList.size() + " 条）");
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
