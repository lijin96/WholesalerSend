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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.entity.SyncCustomers;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCCScustomer
 * @Description: 先选择CCS客户然后再选门店
 * @Author: lijin
 * @Date: 2024/12/10 12:00
 */
public class SelectCCScustomerstore extends Activity {
    private Context mContext;

    private Button btn_seach;
    private Button btn_synchronous;//同步按钮
    private EditText et_search;
    private ListView list_customer;

    private List<Map<String, Object>> customerlist = new ArrayList<Map<String, Object>>();

    private AccessWeb accWeb;
    private Handler hand;

    private TextView tv_total;

    private String CCSCustId="";//CCS客户列表选择的id

//    private String lsv_aim = "";


    private String lsv_aim = "",TraderSysId="",StoreSysId="",RetailId="",RetailName="",CompanyName="",CompanyId="",Brandcode="",AgentCode="";

    private String  company_id = "", company_name = "", stock_id = "",retail_id="",retail_name="", stock_name="",TraderAlias_name="",Storealias_name="";//分店退货接收参数

    private String supplier_id="",supplier_name="",purchecklno="",saplno="",store_id="";//有单有入库有明细分店发货  有单有入库无明细分店发货  有单有入库镜片发货

    private String scanBillNo="",scanBillNum="";//扫描单号，扫描数量

    private String CCScustId="",CCSstoreId="",CCScustName="",CCSstoreName="";//CCS选中绑定的客户id，CCS选中绑定的门店id

    private Boolean IsBindCCSstore;

    private final int Lic_SelectSure = 3;

    private SimpleAdapter adapter;

    private Dialog CustomNormalDialog=null,StoreBindNormalDialog=null,StoreNormalDialog=null;

    private SimpleAdapter storeAdapter;

    private String[] Provicelist;//省份集合
    private String[] Citylist;//城市集合

    private TextView tv_traderprovicename,tv_tradercityname;//零售商城市，分销店城市

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

    private RelativeLayout relat_search_title;//搜索提示

    private String LinkName="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ccscustomerstore);

        Intent getIntent = getIntent();
        lsv_aim = getIntent.getStringExtra("aim");

        TraderSysId=getIntent.getStringExtra("trader_sysid");
        StoreSysId=getIntent.getStringExtra("store_sysid");
        Brandcode = getIntent.getStringExtra("Brand_code");
        AgentCode = getIntent.getStringExtra("Agent_Code");

        IsBindCCSstore = getIntent.getBooleanExtra("IsBindCCSstore", true);

        if (getIntent.getStringExtra("LinkName")!=null) {
            LinkName = getIntent.getStringExtra("LinkName");
        }

//        Log.d("main", String.valueOf(IsBindCCSstore));
        if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)||"P_Dv_OutStock_Z_L_NoBill_NoInStock".equals(lsv_aim)) {

            RetailId = getIntent.getStringExtra("retail_id");
            RetailName = getIntent.getStringExtra("retail_name");
            CompanyName = getIntent.getStringExtra("company_name");
            CompanyId = getIntent.getStringExtra("company_id");
            TraderAlias_name = getIntent.getStringExtra("TraderAlias_name");
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

        }else  if(lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_Bill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Z_L_Bill_NoInStock")) {
            //有单有入库有明细分店发货  有单有入库无明细分店发货  有单有入库镜片发货 有单无入库镜架发货
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

//        tv_total=findViewById(R.id.tv_total);

        relat_search_title=findViewById(R.id.relat_search_title);
        list_customer = findViewById(R.id.listview_ccscustomer);

        btn_seach = findViewById(R.id.btn_search);
        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerlist.clear();
                if (adapter!=null) {
                    adapter.notifyDataSetChanged();
                }
                relat_search_title.setVisibility(View.GONE);
                list_customer.setVisibility(View.VISIBLE);
                DownLoadDataThread();
            }
        });

        btn_synchronous=findViewById(R.id.btn_synchronous);
        btn_synchronous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showNewNormalDialog();
                GetScsCustStore();
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
                        list_customer.setVisibility(View.VISIBLE);
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });
        list_customer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                Map<String, Object> item = (Map<String, Object>) listView.getItemAtPosition(i);
                CCSCustId=item.get("CutCode").toString();
                CCScustName=item.get("CustName").toString();
                showStoreListDialog(item);
            }
        });

//        initRecyclerView();
//        DownLoadDataThread();
        GetScsProvice();
        et_search.setText(LinkName);
        if (!LinkName.equals("")){
            DownLoadDataThread();
        }
    }


    //下载CCS客户
    private void DownLoadDataThread() {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    customerlist = accWeb.GetCcsCustomer(Brandcode,AgentCode,et_search.getText().toString());
//                    Log.d("main",Requestedlist.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, "GetCcsCustomer-下载出错" + e.getMessage());
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
                    String result = accWeb.ScsCustBindToCcs(TraderSysId,StoreSysId,tCcsCustCode,tCcsStoreCode,Brandcode,AgentCode);
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
                    Log.d("main", result);
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
                    ShowMessage.ShowMsg(hand, "GetScsCustStore-获取SCS门店详情出错" + e.getMessage());
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
                    },450);
                    break;

                case 5:
                    //获取门店数据显示在同步信息弹窗里
                    MyProgressDialog.close();
                    showNewNormalDialog();
                    break;
                case 6:
                    break;
                case 7:
                    //显示城市弹窗
                    ShowListCityDialog();
                    break;
                case 8:
                    //同步CCS客户功能 成功就跳扫描界面
                    if (CustomNormalDialog!=null){
                        CustomNormalDialog.dismiss();
                    }
                    ShowMessage.MessageBox(mContext, "温馨提示","同步成功",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.putExtra("CCScustName", TraderAlias);
                            intent.putExtra("aim", lsv_aim);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    },450);
                default:
                    break;
            }
            MyProgressDialog.close();
        }
    }

    public void initListView(List<Map<String, Object>> mList) {
//        Collections.sort(list, new SortListMapComparator("PeiGoodLno"));
        adapter = new SimpleAdapter(this, mList, R.layout.select_ccscustomerstore_item, new String[]{
                "CustName", "CutCode","CustLink", "CustTel","CustAddr"}, new int[]{R.id.customer_Name, R.id.customer_id,R.id.customer_Link, R.id.customer_Tel,R.id.corpaddr});
        list_customer.setAdapter(adapter);
        MyProgressDialog.close();
//        tv_total.setText("（共 " + mList.size() + " 条）");
    }

    //是否同步门店数据到CCS
    private void showNewNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        View selectview = LayoutInflater.from(mContext).inflate(R.layout.dialog_syncccscustomer,null);

        final EditText ed_tradername=selectview.findViewById(R.id.ed_tradername);//零售商名称
        final EditText ed_traderlink=selectview.findViewById(R.id.ed_traderlink);//零售商联系人
        final EditText ed_tradertel=selectview.findViewById(R.id.ed_tradertel);//零售商联系电话
        final EditText ed_tradercorpaddr=selectview.findViewById(R.id.ed_tradercorpaddr);//零售商联系地址
        tv_traderprovicename=selectview.findViewById(R.id.tv_traderprovicename);//零售商省份
        tv_tradercityname=selectview.findViewById(R.id.tv_tradercityname);//零售商城市

        Button btn_cancel=selectview.findViewById(R.id.btn_cancel);//关闭
        Button btn_submit=selectview.findViewById(R.id.btn_submit);//提交


        ed_tradername.setText(TraderAlias);
        ed_traderlink.setText(TraderLink );
        ed_tradertel.setText(TraderTel);
        ed_tradercorpaddr.setText(TraderCorpAddr);
        tv_traderprovicename.setText(TraderProviceName);
        tv_tradercityname.setText(TraderCityName);


        CustomNormalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.mipmap.scs)
                .setView(selectview)
                .create();

        CustomNormalDialog.setCanceledOnTouchOutside(false);
        CustomNormalDialog.show();

//        if (CustomNormalDialog.getWindow() != null) {
//            WindowManager.LayoutParams lp = CustomNormalDialog.getWindow().getAttributes();
//            lp.width = 800; // 宽度，可根据屏幕宽度进行计算
//            lp.height = 600; // 宽度，可根据屏幕宽度进行计算
//            lp.gravity = Gravity.CENTER;
//            CustomNormalDialog.getWindow().setAttributes(lp);
//        }
        //关闭
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomNormalDialog.dismiss();
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
                }else {

                    TraderAlias=ed_tradername.getText().toString().trim();

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

//                    SyncStores syncStores=new SyncStores();
//                    syncStores.setEditMode("A");
//                    syncStores.setCutCode(TraderId);
//                    syncStores.setStoreId(StoreId);
//                    syncStores.setStoreName(ed_storename.getText().toString().trim());
//                    syncStores.setCustLink(ed_storelink.getText().toString().trim());
//                    syncStores.setCustMobile(ed_storetel.getText().toString().trim());
//                    syncStores.setBrandName(BrandName);
//                    syncStores.setProviceName(tv_storeprovicename.getText().toString().trim());
//                    syncStores.setCityName(tv_storecityname.getText().toString().trim());
//                    syncStores.setCountyName(StoreCounty);
//                    syncStores.setCustAddr(ed_storecorpadde.getText().toString().trim());
//                    syncStores.setLicenceNo(StoreLicenceNo);
//                    syncStores.setCorpName(StoreName);
//                    syncStores.setCcsStoreId("");

//                    Log.d("main", "tCustJson="+gson.toJson(syncCustomers));
//                    Log.d("main", "tStoreJson ="+gson.toJson(syncStores));

//                    ScsNewSyncToCcs(gson.toJson(syncCustomers),"");//同步接口
                }
            }
        });

        tv_traderprovicename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowListProviceDialog();
            }
        });

        tv_tradercityname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tv_traderprovicename.getText().toString().trim().equals("")){
                    GetScsCity(tv_traderprovicename.getText().toString().trim());
                }else{
                    ShowMessage.Show(mContext, "请先选择零售商省份");
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
                //零售商点开省份
                if (!TraderProviceName.equals(Provice)){
                    tv_traderprovicename.setText(Provice);
                    tv_tradercityname.setText("");
                    TraderCounty="";
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
                //零售商点开城市
                if (!TraderCityName.equals(tcity)){
                    tv_tradercityname.setText(tcity);
                    StoreCounty="";
                }
            }
        });
        listDialog.show();
    }



    //显示客户下的门店列表弹窗
    private void showStoreListDialog(final Map<String, Object> customerItem) {
        Object storeListObj = customerItem.get("StoreList");
        if (!(storeListObj instanceof List)) {
            ShowMessage.Show(mContext, "该客户暂无门店数据");
            return;
        }
        final List<Map<String, Object>> allStores = (List<Map<String, Object>>) storeListObj;
        if (allStores.isEmpty()) {
            ShowMessage.Show(mContext, "该客户暂无门店数据");
            return;
        }

        View selectview = LayoutInflater.from(mContext).inflate(R.layout.dialog_select_ccsstore_list, null);
        TextView tvTitle = selectview.findViewById(R.id.tv_dialog_title);
        final EditText etStoreSearch = selectview.findViewById(R.id.et_store_search);
        Button btnStoreSearch = selectview.findViewById(R.id.btn_store_search);
        Button btnClose = selectview.findViewById(R.id.btn_close);
        final ListView listStore = selectview.findViewById(R.id.listview_store);
//        final TextView tvTotal = selectview.findViewById(R.id.tv_store_total);

        tvTitle.setText("【" + customerItem.get("CustName") + "】门店列表");

        final List<Map<String, Object>> displayList = new ArrayList<>(filterStoreList(allStores, ""));

        Runnable refreshList = new Runnable() {
            @Override
            public void run() {
                storeAdapter = new SimpleAdapter(mContext, displayList, R.layout.select_ccsstore_item,
                        new String[]{"CustName", "CutCode", "StoreName", "StoreId", "CustLink", "CustTel", "CustAddr"},
                        new int[]{R.id.customr_name, R.id.customr_id, R.id.store_name, R.id.store_id, R.id.store_Link, R.id.store_Tel, R.id.corpaddr});
                listStore.setAdapter(storeAdapter);
//                tvTotal.setText("（共 " + displayList.size() + " 条）");
            }
        };
        refreshList.run();

        View.OnClickListener searchListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayList.clear();
                displayList.addAll(filterStoreList(allStores, etStoreSearch.getText().toString()));
                refreshList.run();
            }
        };
        btnStoreSearch.setOnClickListener(searchListener);
        etStoreSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    searchListener.onClick(view);
                    return true;
                }
                return false;
            }
        });

        StoreNormalDialog = new AlertDialog.Builder(mContext, R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.mipmap.scs)
                .setView(selectview)
                .create();
        StoreNormalDialog.setCanceledOnTouchOutside(false);
        StoreNormalDialog.show();

        if (StoreNormalDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = StoreNormalDialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            lp.gravity = Gravity.CENTER;
            StoreNormalDialog.getWindow().setAttributes(lp);
        }

        listStore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView listView = (ListView) adapterView;
                Map<String, Object> storeItem = (Map<String, Object>) listView.getItemAtPosition(i);
                CCScustId = (String) storeItem.get("CutCode");
                CCSstoreId = (String) storeItem.get("StoreId");
                CCScustName = (String) storeItem.get("CustName");
                CCSstoreName = (String) storeItem.get("StoreName");
                StoreNormalDialog.dismiss();
                showBingNormalDialog(CCScustName, CCScustId, CCSstoreName, CCSstoreId);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StoreNormalDialog.dismiss();
            }
        });
    }

    private List<Map<String, Object>> filterStoreList(List<Map<String, Object>> sourceList, String keyword) {
        if (keyword == null || keyword.trim().equals("")) {
            return new ArrayList<>(sourceList);
        }
        String kw = keyword.trim().toLowerCase();
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> store : sourceList) {
            if (matchStore(store, kw)) {
                filtered.add(store);
            }
        }
        return filtered;
    }

    private boolean matchStore(Map<String, Object> store, String kw) {
        String[] fields = {"StoreName", "StoreId", "CustLink", "CustTel", "CustAddr", "CorpName", "CustName", "CutCode"};
        for (String field : fields) {
            Object val = store.get(field);
            if (val != null && val.toString().toLowerCase().contains(kw)) {
                return true;
            }
        }
        return false;
    }


    //是否绑定客户数据到CCS
    private void showBingNormalDialog(String CCScustName, final String CCScustId, String CCSstoreNameStr, final String CCSstoreIdStr){
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
        LinearLayout lin_ccsstorename=selectview.findViewById(R.id.lin_ccsstorename);
        Button btn_cancel=selectview.findViewById(R.id.btn_cancel);//关闭
        Button btn_submit=selectview.findViewById(R.id.btn_submit);//提交

        tv_ccscustname.setText(CCScustName);
        if (CCSstoreNameStr != null && !CCSstoreNameStr.equals("")) {
            lin_ccsstorename.setVisibility(View.VISIBLE);
            tv_ccsstorename.setText(CCSstoreNameStr);
        } else {
            lin_ccsstorename.setVisibility(View.GONE);
        }

        StoreBindNormalDialog = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIcon(R.mipmap.scs)
                .setView(selectview)
                .create();

        StoreBindNormalDialog.setCanceledOnTouchOutside(false);
        StoreBindNormalDialog.show();

//        if (StoreBindNormalDialog.getWindow() != null) {
//            WindowManager.LayoutParams lp = StoreBindNormalDialog.getWindow().getAttributes();
//            lp.width = 650; // 宽度，可根据屏幕宽度进行计算
//            lp.height = 400; // 宽度，可根据屏幕宽度进行计算
//            lp.gravity = Gravity.CENTER;
//            StoreBindNormalDialog.getWindow().setAttributes(lp);
//        }
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
                ScsStoreBindToCcs(CCScustId,CCSstoreId);
//                ScsCustBindToCcs(CCScustId);
            }
        });

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

