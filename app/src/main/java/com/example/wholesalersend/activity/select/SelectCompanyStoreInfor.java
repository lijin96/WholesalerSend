package com.example.wholesalersend.activity.select;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Lens_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_NoBill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_BeInStock;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_NoInStock;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_NoBill_BeInStock;
import com.example.wholesalersend.adapter.AccountSetAdapter;
import com.example.wholesalersend.adapter.ScanOrderAdapter;
import com.example.wholesalersend.entity.AccountSet;
import com.example.wholesalersend.entity.ScanApiResponse;
import com.example.wholesalersend.entity.ScanOrder;
import com.example.wholesalersend.entity.StoreInfor;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.HttpPostMultipart;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectCompanyStoreInfor
 * @Description: 选择分销店
 * @Author: lijin
 * @Date: 2021/3/10 9:56
 */
public class SelectCompanyStoreInfor extends Activity {


    private Handler hand;
    private Context mContext;
    private AccessWeb accWeb;
    private SysUserInfo sysUserInfo;
    private SimpleAdapter adapter;
    private StoreInfor selectedStoreInfor;

    private EditText et_search;
    private TextView tv_total, company_store_help;//点击查看帮助
    //	private ExpandableListView elistview_distributor;
    private ListView elistview_distributor;
    private Button btn_search;


    private List<Map<String, Object>> searchDistributorList;

    private String lsv_aim = "";

    private TextView tv_customer_scan;//快速扫码选择客户
    private AlertDialog mAlertDialog;//弹出层

    private StoreInfor distri=null;

    private final int Lic_SelectSure = 3;

    int PopWidth=1000;

    int Page=1;
    List<ScanOrder> scanOrderlist=new ArrayList<ScanOrder>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.select_company_distributor);
        mContext = this;
        hand = new handShowMsg();
        accWeb=new AccessWeb(getApplicationContext());
        sysUserInfo = new SysUserInfo(mContext);
        lsv_aim= getIntent().getStringExtra("aim");
        tv_total = (TextView) findViewById(R.id.tv_total);
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Page=1;
                        searchDistributorList.clear();
                        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });

        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Page=1;
                searchDistributorList.clear();
                MyProgressDialog.show(mContext, "正在获取数据...", true, false);
                DownLoadDataThread();
            }
        });

        sysUserInfo.setSeachValue("");//设置上次搜索的产品名称为空

        elistview_distributor = (ListView) findViewById(R.id.elistview_distributor);

        elistview_distributor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                distri = new StoreInfor();
                distri.setStoreId(searchDistributorList.get(position).get("StoreId").toString());
                distri.setStoreName(searchDistributorList.get(position).get("StoreName").toString());
                distri.setLink(searchDistributorList.get(position).get("Link").toString());
                distri.setTel(searchDistributorList.get(position).get("Tel").toString());
                distri.setCorpAddr(searchDistributorList.get(position).get("CorpAddr").toString());
                distri.setTraderId(searchDistributorList.get(position).get("TraderId").toString());
                distri.setTraderName(searchDistributorList.get(position).get("TraderName").toString());
                distri.setTraderAlias(searchDistributorList.get(position).get("TraderAlias").toString());
                distri.setStoreSysCode(searchDistributorList.get(position).get("StoreSysCode").toString());
                distri.setTraderSysId(searchDistributorList.get(position).get("TraderSysId").toString());
                distri.setStoreAlias(searchDistributorList.get(position).get("StoreAlias").toString());
                CheckCustomerStatus(distri.getTraderSysId());
//                goToOutStock(distri);
            }
        });
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        DownLoadDataThread();

        tv_customer_scan=findViewById(R.id.tv_customer_scan);
        tv_customer_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopListView();
            }
        });
        if (sysUserInfo.getOldVersion().equals("T8")) {
            PopWidth = 700;
        }
//        ShowScanOrderList(scanOrderlist);
        if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")) {
            showPopListView();
            tv_customer_scan.setVisibility(View.VISIBLE);
        }
    }



    //验证客户可用性
    private void CheckCustomerStatus(String tCustSysCode){
        MyProgressDialog.show(mContext, "正在验证客户...", true, false);
        Thread sendVerify = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 请求头
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer "+sysUserInfo.getLoginid());
                    HttpPostMultipart multipart = new HttpPostMultipart("http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/CustomerInfor/CheckCustomerStatus", "utf-8", headers);
                    // post参数
                    multipart.addFormField("CustSysCode", tCustSysCode);//

                    Map<String, String> requestdata=new HashMap<>();
                    requestdata.put("CustSysCode", tCustSysCode);
                    // 返回信息
//                    Log.d("main", tCustSysCode);
                    String multiresponse = multipart.finish(requestdata);
//                    Log.d("main", multiresponse);
                    Gson gson = new GsonBuilder().create();
                    ScanApiResponse response = gson.fromJson(multiresponse, ScanApiResponse.class);
                    if (response.isSuccess()==true){
                        //不用实体类，直接解析
                        ShowMessage.ShowMsg(hand, 6, multiresponse);

                    }else{
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                                "服务器："+response.getMessage());
                    }

                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "服务器："+e.getMessage());
                }
            }
        });
        sendVerify.start();
    }



    private void showPopListView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.Base_Theme_AppCompat_Light_Dialog);
        mAlertDialog = builder.setCancelable(false).create();
        View dialogView = View.inflate(mContext, R.layout.select_pop, null);
        //设置对话框布局
        mAlertDialog.setView(dialogView);

        final EditText ed_code = (EditText) dialogView.findViewById(R.id.ed_code);
        ed_code.setFocusable(true);
        ed_code.setFocusableInTouchMode(true);
        ed_code.requestFocus();
        Button btn_cancel = (Button) dialogView.findViewById(R.id.bt_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setFocusable(true);
                et_search.setFocusableInTouchMode(true);
                mAlertDialog.dismiss();
            }
        });

        Button bt_pop_ok = (Button) dialogView.findViewById(R.id.bt_pop_ok);
        bt_pop_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = ed_code.getText().toString().trim();
                if (code.equals("")) {
                    ShowMessage.Show(mContext, "条码为空，请重新扫描条码");
                    ed_code.setText("");
                } else {
                    DownLoadGetCustByBarCode(code);//获取条码对应客户信息
                    ed_code.setText("");
                }
            }
        });

        ed_code.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        String code = ed_code.getText().toString().trim();
                        if (code.equals("")) {
                            ShowMessage.Show(mContext, "条码为空，请重新扫描条码");
                            ed_code.setText("");
                        } else {
                            DownLoadGetCustByBarCode(code);//获取条码对应客户信息
                            ed_code.setText("");
                        }
                    }
                    return true;
                }

                return false;
            }

        });
        mAlertDialog.show();

        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = PopWidth; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }


    //获取条码对应客户信息
    private void DownLoadGetCustByBarCode(String tBarcode) {
        MyProgressDialog.show(mContext, "正在获取客户信息...", true, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetCustByBarCode?Barcode="+tBarcode+"&CodeType=1";
//                    Log.d("main", requestUrl);
                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
//                    Log.d("main", result);
                    Gson gson=new Gson();
                    ScanApiResponse scsPickApiInfo=gson.fromJson(result,ScanApiResponse.class);
                    if (scsPickApiInfo != null && scsPickApiInfo.isSuccess()) {
                        JSONObject rootObject = new JSONObject(result);
                        String Scandata = rootObject.optString("data");
//                        Log.d("main", Scandata);
                        if (Scandata.equals("null")||Scandata==null||Scandata.equals("")){
                            ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"未查询到条码对应的客户信息，请检查条码");
                        }else {
                            JSONObject OrderDetailsdata = rootObject.getJSONObject("data");
                            String SearchData ="";
                            if (OrderDetailsdata.optString("storeName").equals("null")){
                                ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"未查询到条码对应的客户信息，请检查条码");
                            }else {
                                SearchData = OrderDetailsdata.optString("storeName");
                                ShowMessage.ShowMsg(hand, 5,SearchData);
                            }
                        }
                    }else{
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"服务器："+scsPickApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                            "服务器："+e.getMessage());
                }
            }
        }).start();
    }


    // 请求服务
    private void DownLoadDataThread() {
        searchDistributorList = new ArrayList<Map<String, Object>>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ADevicesManager.isSCSNewInterface){
                        searchDistributorList = accWeb.NewGetDownLoadSCSStoreInfor(et_search.getText().toString(), Page);
                    }else {
                        searchDistributorList = accWeb.GetDownLoadSCSStoreInfor(et_search.getText().toString(), Page);
                    }
//                    Log.d("main",searchDistributorList.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    // 下载扫描单号 请求服务
    private void DownLoadScanOrder(final String tDeSysCode, final String tStoreSysCode, final String tScanType,final String tOrderType) {
        scanOrderlist = new ArrayList<ScanOrder>();
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scanOrderlist = accWeb.GetCurrentScanBill(tDeSysCode,tStoreSysCode,tScanType,tOrderType);
//                    Log.d("main",scanOrderlist.toString());
                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandFailed,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    /**
     * 适配ExpandListView
     * //         * @param menuGroups 零售下面有分销店的集合
     * //         * @param isOpen 是否展开ExpandListView，全部数据时默认不展开，模糊查找时默认展开
     */
    //	public void initExpandListView(List<Retailer> menuGroups,boolean isOpen)
    //	{
    //		distributorCount = 0;
    //		for(Retailer retailer:menuGroups)
    //		{
    //			distributorCount+= retailer.getStoreInforList().size();
    //		}
    //		adapter = new SelectDistributorAdapter(mContext,menuGroups);
    //		elistview_distributor.setAdapter(adapter);
    ////		if(isOpen)
    ////		{
    ////			//默认全部展开
    ////			int groupCount = elistview_distributor.getCount();
    ////			for (int i=0; i<groupCount; i++)
    ////			{
    ////				elistview_distributor.expandGroup(i);
    ////			}
    ////		}
    //		tv_total.setText("（零售共 "+menuGroups.size()+" 条，分销共 "+distributorCount+" 条）");
    //
    //	}
    public void initListView() {
//        Log.d("main",searchDistributorList.toString());
        adapter = new SimpleAdapter(this, searchDistributorList, R.layout.select_distributor_fenxiao_item,
                new String[]{"TraderName","TraderId","StoreId", "StoreName", "Link", "Tel", "CorpAddr",
                }, new int[]{R.id.retailerName,R.id.retailer_id,
                R.id.company_id, R.id.companyName, R.id.companyLink,
                R.id.companyTel, R.id.corpaddr});
        elistview_distributor.setAdapter(adapter);
        tv_total.setText("分销店（共 " + searchDistributorList.size() + " 条）");
    }

    /**
     * 跳转到分销发货界面
     *
     * @param storeInfor
     */
    public void goToOutStock(StoreInfor storeInfor) {
        selectedStoreInfor = storeInfor;
        String ScanType="OutStock";
        if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)||"P_Dv_ReturnedPurchase_Lens_Z_L_NoBill".equals(lsv_aim)||"ReturnedPurchase_D_L_NoBill_SameCust".equals(lsv_aim)) {
            ScanType="OutReturn";
        }
        String tordertype="普通";
        if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock")||lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_NoInStock")) {
            tordertype="镜片";
        }
//
        DownLoadScanOrder(storeInfor.getTraderSysId(),storeInfor.getStoreSysCode(),ScanType,tordertype);
    }


    //选择扫描单号
    private void ShowScanOrderList(){
        View selectview = LayoutInflater.from(mContext).inflate(R.layout.select_scanorder_list,null);
        ListView ScanOrderListView=selectview.findViewById(R.id.List_ScanOrder);
        ScanOrderAdapter scanOrderAdapter=new ScanOrderAdapter(mContext, scanOrderlist);
        ScanOrderListView.setAdapter(scanOrderAdapter);

        TextView tv_new_scanorder= selectview.findViewById(R.id.new_scanorder);


        final Dialog alertDialog6 = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setTitle("选择要扫描的单号")
                .setIcon(R.drawable.scs)
                .setView(selectview)
                .setPositiveButton("新开扫描单", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent sureIntent = new Intent(mContext, SelectSureConfirm.class);
                        sureIntent.putExtra("title", "确定选择分销店：" + selectedStoreInfor.getStoreName() + "？");
                        startActivityForResult(sureIntent, Lic_SelectSure);
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
                Intent sureIntent = new Intent(mContext, SelectSureConfirm.class);
                sureIntent.putExtra("title", "确定选择分销店：" + selectedStoreInfor.getStoreName() + "？");
                startActivityForResult(sureIntent, Lic_SelectSure);
                alertDialog6.dismiss();
            }
        });
        ScanOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


//                ShowMessage.MessageBox(mContext,"提示","是否在该扫描单上添加其他的品牌产品扫码发货？","添加品牌","直接扫描",new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int j) {
//                        Log.d("main","ok");
//                        Intent intent = new Intent(mContext, SelectCCSBrand.class);
//                        if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")) {
//                            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
//                            intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
//                            intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
//
//                            intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());
//                            intent.putExtra("storealias_name", selectedStoreInfor.getStoreAlias());
//
//
//                        }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
//                            intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
//                            intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
//                            intent.putExtra("company_name", selectedStoreInfor.getStoreName());
//                            intent.putExtra("company_id",selectedStoreInfor.getStoreId());
//                            intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
//                            intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
//                            intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());
//                            intent.putExtra("storealias_name", selectedStoreInfor.getStoreAlias());
//
//
//                        }else if (lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_BeInStock")){
//                            //无单有入库分店发货
//                            intent.putExtra("TraderAlias_name", selectedStoreInfor.getTraderAlias());
//                            intent.putExtra("storealias_name", selectedStoreInfor.getStoreAlias());
//
//                        }else if (lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock")){
//                            //镜片无单有入库分店发货
//                            intent.putExtra("TraderAlias_name", selectedStoreInfor.getTraderAlias());
//
//                            intent.putExtra("storealias_name", selectedStoreInfor.getStoreAlias());
//
//                        }
//
//                        intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
//                        intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
//                        intent.putExtra("company_name", selectedStoreInfor.getStoreName());
//                        intent.putExtra("company_id", selectedStoreInfor.getStoreId());
//
//
//
//                        intent.putExtra("scanBillNo", scanOrderlist.get(i).getBillNo());
//                        intent.putExtra("scanBillNum", scanOrderlist.get(i).getBillNum());
//
//                        intent.putExtra("aim", lsv_aim);
//                        startActivity(intent);
//                    }
//
//                },new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int j) {
//                        Log.d("main","no");
                Intent intent = null;
                if (lsv_aim.equals("P_Dv_ReturnedPurchase_Z_L_NoBill")) {
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
                    intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
                    intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id",selectedStoreInfor.getStoreId());
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());

                }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill.class);
                    intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
                    intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id",selectedStoreInfor.getStoreId());
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());

                }else if (lsv_aim.equals("ReturnedPurchase_D_L_NoBill_SameCust")){
                    //镜架跨店退货
                    intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill.class);
                    intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
                    intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id",selectedStoreInfor.getStoreId());
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());

                }else if (lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_BeInStock")){
                    //无单有入库分店发货
                    intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id", selectedStoreInfor.getStoreId());
                    intent.putExtra("TraderAlias_name", selectedStoreInfor.getTraderAlias());
                }else if (lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock")){
                    //镜片无单有入库分店发货
                    intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id", selectedStoreInfor.getStoreId());
                    intent.putExtra("TraderAlias_name", selectedStoreInfor.getTraderAlias());
                }else if (lsv_aim.equals("P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing")){
                    //退货直通车
                    intent = new Intent(mContext, SelectStock.class);
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id", selectedStoreInfor.getStoreId());
                    intent.putExtra("TraderAlias_name", selectedStoreInfor.getTraderAlias());

                }else if (lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_NoInStock")) {
                    intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_NoInStock.class);
                    intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
                    intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id",selectedStoreInfor.getStoreId());
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());
                }else if(lsv_aim.equals("P_Dv_OutStock_Lens_Z_L_NoBill_NoInStock")) {
                    intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_NoInStock.class);
                    intent.putExtra("stock_id", (String) scanOrderlist.get(i).getStockCode());
                    intent.putExtra("stock_name", (String)scanOrderlist.get(i).getStockName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id",selectedStoreInfor.getStoreId());
                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("alias_name", selectedStoreInfor.getTraderAlias());
                }
                intent.putExtra("scanBillNo", scanOrderlist.get(i).getBillNo());
                intent.putExtra("scanBillNum", scanOrderlist.get(i).getBillNum());

                intent.putExtra("trader_sysid", selectedStoreInfor.getTraderSysId());
                intent.putExtra("store_sysid", selectedStoreInfor.getStoreSysCode());

                intent.putExtra("stock_sysid", scanOrderlist.get(i).getStockSysCode());

                intent.putExtra("link", selectedStoreInfor.getLink());

                intent.putExtra("aim", lsv_aim);
                startActivity(intent);
            }

        });



    }



    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(SelectCompanyStoreInfor.this, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
                    initListView();
                    break;

                case ShowMessage.HandScanSuccess://扫描单号列表返回数据
                    MyProgressDialog.close();
                    if (scanOrderlist.size()>0){
                        ShowScanOrderList();
                    }else {
                        Intent sureIntent = new Intent(mContext, SelectSureConfirm.class);
                        sureIntent.putExtra("title", "确定选择分销店：" + selectedStoreInfor.getStoreName() + "？");
                        startActivityForResult(sureIntent, Lic_SelectSure);
                    }
                    break;
                case ShowMessage.HandFailed: //扫描单号返回报错
                    MyProgressDialog.close();
                    Intent sureIntent = new Intent(mContext, SelectSureConfirm.class);
                    sureIntent.putExtra("title", "确定选择分销店：" + selectedStoreInfor.getStoreName() + "？");
                    startActivityForResult(sureIntent, Lic_SelectSure);
                    break;
                case 5:
                    //获取条码对应客户信息成功
                    MyProgressDialog.close();
                    if (mAlertDialog!=null){
                        mAlertDialog.dismiss();
                    }
                    et_search.setText(msg.obj.toString());
                    if (!msg.obj.toString().equals("")){
                        et_search.setSelection(msg.obj.toString().length());
                    }
                    Page=1;
                    searchDistributorList.clear();
                    DownLoadDataThread();
                    break;

                case 6:
                    //验证客户可用性
                    MyProgressDialog.close();
                    try {
                        JSONObject rootObject = new JSONObject( msg.obj.toString());
                        JSONObject OrderDetailsdata = rootObject.getJSONObject("data");
                        String tStatus = OrderDetailsdata.optString("status");
                        String tMessage= OrderDetailsdata.optString("message");
                        //状态为【可用】 (Status: 1)  状态为【不可用】 (Status: 0) 状态为【提醒】 (Status: 2)
                        if (tStatus.equals("1")){
                            //状态为【可用】 (Status: 1)
                            goToOutStock(distri);
                        }else if (tStatus.equals("2")){
                            //状态为【提醒】 (Status: 2) 弹窗功能，用于非强制性的风险提示
                            ShowMessage.MessageBox(mContext, "温馨提示", tMessage, "确定", "取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    goToOutStock(distri);
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                        }else if (tStatus.equals("0")){
                            //状态为【不可用】 (Status: 0)  阻断操作
                            ShowMessage.MessageBox(mContext, "提示", tMessage, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                        }
                    } catch (JSONException e) {
                        ShowMessage.Show(mContext,"JSON解析报错："+e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                default:
                    // MyProgressDialog.close();
                    // ShowMessage.Show(Xundian.this,msg.obj.toString());
                    break;
            }
            MyProgressDialog.close();
            super.handleMessage(msg);
        }
    }


    // item选择事件

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Lic_SelectSure:
//                    Intent intent = new Intent(this, P_Dv_OutStock_D_S_NoBill.class);
//                    intent.putExtra("selectedStoreInfor", selectedStoreInfor);
//                    startActivity(intent);
                    Intent intent = null;
                    //分店无单无入库发货
                    if ("P_Dv_OutStock_Z_L_NoBill_NoInStock".equals(lsv_aim)) {
                        intent = new Intent(mContext, SelectStock.class);
                    }else if ("P_Dv_OutStock_Lens_Z_L_NoBill_NoInStock".equals(lsv_aim)){
                        intent = new Intent(mContext, SelectStock.class);
                    }else
                    //退货直通车
                    if ("P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing".equals(lsv_aim)) {
                        intent = new Intent(mContext, SelectStock.class);
                    }
                    //分店有入库无单发货
                    else if ("P_Dv_OutStock_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                        intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_BeInStock.class);
//                        intent = new Intent(mContext, SelectCCSBrand.class);
                    }
                    //镜片有入库无单分店发货
                    else if ("P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock".equals(lsv_aim)) {
                        intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock.class);
//                        intent = new Intent(mContext, SelectCCSBrand.class);
                    }
                    //分店无单退货
                    else if ("P_Dv_ReturnedPurchase_Z_L_NoBill".equals(lsv_aim)||"ReturnedPurchase_D_L_NoBill_SameCust".equals(lsv_aim)) {
                        intent = new Intent(mContext, SelectStock.class);
                        intent.putExtra("trader_sysid", selectedStoreInfor.getTraderSysId());
                        intent.putExtra("store_sysid", selectedStoreInfor.getStoreSysCode());

                    }else  if (lsv_aim.equals("P_Dv_ReturnedPurchase_Lens_Z_L_NoBill")) {
                        intent = new Intent(mContext, SelectStock.class);
                        intent.putExtra("trader_sysid", selectedStoreInfor.getTraderSysId());
                        intent.putExtra("store_sysid", selectedStoreInfor.getStoreSysCode());

                    }
//                    else if(lsv_aim.equals("P_Dv_OutStock_Z_L_NoBill_NoInStock")){
//                        //镜架无单无入库发货
//                        intent = new Intent(mContext, SelectStock.class);
//                        intent.putExtra("trader_sysid", selectedStoreInfor.getTraderSysId());
//                        intent.putExtra("store_sysid", selectedStoreInfor.getStoreSysCode());
//                    }
                    intent.putExtra("aim", lsv_aim);

                    intent.putExtra("scanBillNo", "");
                    intent.putExtra("scanBillNum", "");

                    intent.putExtra("trader_sysid", selectedStoreInfor.getTraderSysId());
                    intent.putExtra("store_sysid", selectedStoreInfor.getStoreSysCode());

                    intent.putExtra("retail_id", selectedStoreInfor.getTraderId());
                    intent.putExtra("retail_name", selectedStoreInfor.getTraderName());
                    intent.putExtra("company_name", selectedStoreInfor.getStoreName());
                    intent.putExtra("company_id", selectedStoreInfor.getStoreId());
                    intent.putExtra("TraderAlias_name", selectedStoreInfor.getTraderAlias());
                    intent.putExtra("storealias_name", selectedStoreInfor.getStoreAlias());

                    startActivity(intent);

                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//	@Override
//	protected void onResume() {
//		// TODO Auto-generated method stub
//		super.onResume();
//		et_search.setText("");
//		String sql = "select * from storeinfor";
//		//		allRetailList = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);
//		//		//根据全部的零售商查出全部的分销店，封装成适合适配器的格式适配
//		initListView(sql);
//	}
    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
}
