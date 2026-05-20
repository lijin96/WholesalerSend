package com.example.wholesalersend.activity.instock_in;

import android.app.Activity;
import android.app.AlertDialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.other.P_Dv_InStock_PackBox_Search;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.entity.BoxTag;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.entity.ScanApiResponse;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.HttpPostMultipart;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.bluetooth.BluetoothManager;
import com.example.wholesalersend.lib.bluetooth.BluetoothService;
import com.example.wholesalersend.lib.bluetooth.BluetoothUtil;
import com.example.wholesalersend.lib.bluetooth.DeviceListActivity;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.NumberUtils;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: P_Dv_InStock_PackBox_List_NoBill
 * @Description: 装盒入库（品牌商）
 * @Author: lijin
 * @Date: 2026年4月9日10:20:38
 */
public class P_Dv_InStock_PackBox_List_NoBill extends Activity  {

    private Context mContext;
    private SysUserInfo sysUserInfo;
    private AccessWeb accessWeb;

    private String GoodsBrand="",GoodsSeries="";//品牌和系列
    private String GoodsModelm="",GoodsColor="",GoodsId="";//产品型号 产品色号 产品id
    private String BoxNoCode="";//盒标码
    private String OneCount="0",AllCount="0",AllBox="0";//当前型号已装数 当前已装产品总数 当前已装盒数

    private String connectedDeviceName, connectedDeviceAddress;//连接的蓝牙设备名和蓝牙地址

    private String scanBillno = "";//扫描单号
    private int nSize = 0;//扫描序号

    private String lStar = "";

    private EditText et_barcode;//条码输入框

    private LinearLayout Linear_model_color;//型号色号界面

    private TextView tv_model_color,tv_productid;//型号色号 产品id
    private TextView tv_BoxNo_Code;//盒标码展示
    private TextView tv_packing_number;//显示设置的盒装数

    private TextView tv_stock;//仓库

    private TextView tv_one_count,tv_all_count,tv_all_box;//当前型号已装数 当前已装产品总数 当前已装盒数
    private TextView tv_box_count;//当前盒已装数量

    private TextView tv_title;//标题

    private Button btn_select_model_color;

    private Button btn_set_packing_number,btn_eliminate,btn_history_eliminate;//选择型号色号 设置盒装数 剔除当前盒 剔除历史盒

    private TextView tv_supplier_str;//供应商

    private Button btn_again_print_boxcode;//打印盒标

    private Button btn_test_packing;//测试打印


    private Button btn_connect;//连接蓝牙
    private TextView tv_connect_state;//蓝牙连接状态

    private String BoxNoNum="12";//盒装数 默认12个，必须有盒标码才可以修改数量
    private String BoxActNum="0";//已装数量

    private final int Lic_SelectModel = 2;//选择产品
    private final int Lic_Eliminate = 3;//剔除

    private final int HandPackBoxSuccess = 9;//扫描成功，返回盒标码等参数
    private final int HandToaskErrorMsg = 10;//弹出toask，报错声音
    private final int HandShowWaitingDialog = 11;//弹出请稍候的dialog
    private final int HandCloseWaitingDialog = 12;//弹出请稍候的dialog

    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    private boolean isTest = true;//是否测试，测试的话不需要连接蓝牙打印机。输出log.i盒标.编译的时候要false

    private BoxTag boxTag;

    private String Supplierid="",Suppliersyscode="",Suppliername="";
    private String StockId="",StockSysCode="",StockName="";

    private Button btn_finish;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_packing_instock);//旧版品牌商装盒入库界面
        setContentView(R.layout.new_p_dv_instock_packing);

        mContext=this;
        sysUserInfo=new SysUserInfo(mContext);
        accessWeb=new AccessWeb(mContext);

        Supplierid=getIntent().getStringExtra("supplier_id");
        Suppliersyscode=getIntent().getStringExtra("supplier_syscode");
        Suppliername=getIntent().getStringExtra("supplier_name");

        StockId=getIntent().getStringExtra("stock_id");
        StockSysCode=getIntent().getStringExtra("stock_syscode");
        StockName=getIntent().getStringExtra("stock_name");

        // 若从选择未完成盒标列表进入，恢复该盒标状态以便继续扫码
        String boxNo = getIntent().getStringExtra("BoxNo");
        if (boxNo != null && !boxNo.isEmpty()) {
            BoxNoCode = boxNo;
            String packingNum = getIntent().getStringExtra("PackingNum");
            if (packingNum != null) BoxNoNum = packingNum;
            String actNum = getIntent().getStringExtra("BoxActNum");
            if (actNum != null) BoxActNum = actNum;OneCount=actNum;
            String gid = getIntent().getStringExtra("GoodsId");
            if (gid != null) GoodsId = gid;
            String model = getIntent().getStringExtra("Modelm");
            if (model != null) GoodsModelm = model;
            String colors = getIntent().getStringExtra("Colors");
            if (colors != null) GoodsColor = colors;

            String stockname = getIntent().getStringExtra("StockName");
            if (stockname != null) StockName = stockname;
            String stockcode = getIntent().getStringExtra("StockCode");
            if (stockcode != null) StockId = stockcode;
            String stocksyscode = getIntent().getStringExtra("StockSysCode");
            if (stocksyscode != null) StockSysCode = stocksyscode;
        }

        scanBillno = sysUserInfo.getUserid() + "SZR" + SomeUtils.RandomScanOrder();// 系统

        initView();

        if (BoxNoCode != null && !BoxNoCode.isEmpty()) {
            tv_BoxNo_Code.setText(BoxNoCode);
            tv_packing_number.setText(BoxNoNum);
            tv_box_count.setText(BoxActNum);
            tv_model_color.setText(GoodsModelm+" "+GoodsColor);
            tv_productid.setText(GoodsId);
        }
    }
    private void initView(){

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

//        tv_title=findViewById(R.id.tv_title);
//        tv_title.setText("【装盒入库】 "+sysUserInfo.getAccountSetName());

        btn_finish=findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SomeUtils.clickKeyBack();
            }
        });

        tv_supplier_str=findViewById(R.id.tv_supplier_str);
        tv_supplier_str.setText(Suppliername);

        tv_stock=findViewById(R.id.tv_stock);
        tv_stock.setText(StockName);

        btn_test_packing = (Button) findViewById(R.id.btn_test_packing);
        btn_test_packing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 1; i++) {
//                    int j = (int) (Math.random() * 900) + 100;
                    BoxTag   testBoxTag = new BoxTag("A201807300000" + i, "测试", "合力思测试", "测试", "test", "1", sysUserInfo.getUserName(), "2024-12-03","仓库名称1");
                    printBoxCode(testBoxTag);
                }
            }
        });
//
//        Linear_model_color=findViewById(R.id.Linear_model_color);
//        Linear_model_color.setVisibility(View.VISIBLE);

        btn_select_model_color=findViewById(R.id.btn_select_model_color);
        btn_select_model_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!BoxNoCode.equals("")&&Integer.parseInt(BoxNoNum)>Integer.parseInt(BoxActNum)){
                    ShowMessage.Show(mContext, "当前装盒任务未扫码完成，请继续扫码或者切换盒装数");
                }else{
                    Intent intent = new Intent(mContext, SelectProductModelColor.class);
                    intent.putExtra("aim", "P_Dv_InStock_PackBox_List_NoBill");
                    startActivityForResult(intent, Lic_SelectModel);
                }
            }
        });

        btn_again_print_boxcode=findViewById(R.id.btn_again_print_boxcode);//打印盒标
        btn_again_print_boxcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, P_Dv_InStock_PackBox_Search.class);
                intent.putExtra("PageType","warehous");
                startActivity(intent);
            }
        });

        et_barcode=findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarcodeOnKeyListener());

        tv_packing_number=findViewById(R.id.tv_packing_number);
        tv_packing_number.setText(BoxNoNum);

        btn_set_packing_number=findViewById(R.id.btn_set_packing_number);
        btn_set_packing_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BoxNoCode.equals("")){
                    ShowMessage.Show(mContext, "请先扫码装盒生成盒标码后，再修改装盒数");
                }else {
                    ShowSetNumberDialog();
                }
            }
        });

        btn_eliminate=findViewById(R.id.btn_eliminate);
        btn_eliminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BoxNoCode.equals("")) {
                    ShowMessage.Show(mContext, "请先扫码装盒生成盒标码后，再点击剔除");
                }else{
                    Intent eIntent = new Intent(mContext, FactoryEliminateActivity.class);
                    eIntent.putExtra("PackBoxNoCode", BoxNoCode);
                    eIntent.putExtra("ScanBillno", scanBillno);
                    eIntent.putExtra("supplier_id", Supplierid);
                    eIntent.putExtra("aim", "P_Dv_InStock_PackBox_List_NoBill");
                    startActivityForResult(eIntent, Lic_Eliminate);
                }
            }
        });
        btn_history_eliminate=findViewById(R.id.btn_history_eliminate);
        btn_history_eliminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BoxNoCode!=""){
                    ShowMessage.Show(mContext,"请先完成当前装盒入库");
                }else {
                    Intent eIntent = new Intent(mContext, FactoryHistoryEliminateActivity.class);
                    eIntent.putExtra("ScanBillno", scanBillno);
                    eIntent.putExtra("supplier_id", Supplierid);
                    eIntent.putExtra("aim", "P_Dv_InStock_PackBox_List_NoBill");
                    startActivityForResult(eIntent, 4);
                }
            }
        });

        tv_BoxNo_Code=findViewById(R.id.tv_BoxNo_Code);
        tv_one_count=findViewById(R.id.tv_one_count);
        tv_box_count=findViewById(R.id.tv_box_count);
        tv_all_count=findViewById(R.id.tv_all_count);
        tv_all_box=findViewById(R.id.tv_all_box);


        tv_model_color=findViewById(R.id.tv_model_color);
        tv_productid=findViewById(R.id.tv_productid);

        tv_connect_state=findViewById(R.id.tv_connect_state);

        btn_connect=findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_connect.getText().equals("连接")) {
                    Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                    startActivityForResult(serverIntent, BluetoothUtil.REQUEST_CONNECT_DEVICE);
                } else {
                    bluetoothManager.disconnectCurrentConnection();
                    // 立即更新界面状态并清理设备信息
                    btn_connect.setText("连接");
                    tv_connect_state.setText("未连接");
                    tv_connect_state.setTextColor(Color.RED);
                    isConnectedBluetooth = false;
                    connectedDeviceName = "";
                    connectedDeviceAddress = "";
                }
            }
        });


        // 使用全局蓝牙管理器
        bluetoothManager = BluetoothManager.getInstance();
        mBluetoothAdapter = bluetoothManager.getBluetoothAdapter();

        if (bluetoothManager.isBluetoothAvailable()) {
            // 如果已经连接，则不需要重新初始化
            if (!bluetoothManager.isBluetoothConnected()) {
                // 自动连接上次保存的装盒入库界面的蓝牙设备（使用 usePackingDevice = true）
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(mContext, mHandler,true);
                if (isAutoConnecting) {
                    // 正在自动连接，更新UI状态
                    connectedDeviceName = sysUserInfo.getPackingBluetoothName();
                    tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                    tv_connect_state.setTextColor(Color.BLACK);
                    btn_connect.setText("连接");
                } else {
                    // 没有保存的设备地址，显示未连接状态
                    tv_connect_state.setText("未连接");
                    tv_connect_state.setTextColor(Color.RED);
                    btn_connect.setText("连接");
                }
            } else {
                // 已经连接，从BluetoothManager获取当前连接的设备信息
                String currentDeviceName = bluetoothManager.getConnectedDeviceName();
                String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();

                if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                    connectedDeviceName = currentDeviceName;
                    connectedDeviceAddress = currentDeviceAddress;
                } else {
                    // 如果BluetoothManager中的设备名称为空，使用保存的装盒入库界面设备信息
                    if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                        connectedDeviceName = sysUserInfo.getPackingBluetoothName();
                        connectedDeviceAddress = sysUserInfo.getPackingBluetoothAddress();
                    }
                    if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                        connectedDeviceName = "未知设备";
                    }
                }
                // 直接更新UI状态
                isConnectedBluetooth = true;
                tv_connect_state.setText("已连接:" + connectedDeviceName);
                tv_connect_state.setTextColor(Color.parseColor("#008000"));
                btn_connect.setText("断开");
            }
        } else {
            ShowMessage.Show(mContext, "蓝牙未打开或不可用，请到系统设置中检查");
        }

//        Holyes_Dv_Factory_GetUnFillBox();//读取上一次未完成的装盒入库任务
    }

    /**
     * 读取未满盒盒标码信息（上一次未完成扫码入库的装盒）
     */
    public void GetUnFillBox() {
        MyProgressDialog.show(mContext, "正在获取上一次任务", true, true);


        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();

                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetUnFillBox"+ "?SoCompId="+Supplierid+"&OaSuserId="+sysUserInfo.getUserid();

                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法

                    SalesScsWebApiInfo scsWebApiInfo=gson.fromJson(result,SalesScsWebApiInfo.class);
                    if(scsWebApiInfo.isSuccess()) { // 假设有isSuccess()方法

//                        JSONArray listjson = new JSONArray(scsWebApiInfo.getData());
                        List<Map<String, Object>> PackBoxlist = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < scsWebApiInfo.getData().size(); i++) {
                            JSONObject jsonObject2 = (JSONObject) scsWebApiInfo.getData().get(i);
                            Map<String, Object> map1 = new HashMap<String, Object>();
                            map1.put("BoxNo", jsonObject2.optString("BoxNo"));
                            map1.put("SetNum", jsonObject2.optString("SetNum"));
                            map1.put("ActNum", jsonObject2.optString("ActNum"));
//                      "SetNum":"盒装数",
//                      "ActNum":"实装数",
                            map1.put("GoodsId", jsonObject2.optString("GoodsId"));
                            map1.put("Modelm", jsonObject2.optString("Modelm"));
                            map1.put("Colors", jsonObject2.optString("Colors"));
                            PackBoxlist.add(map1);
                        }

//                    [{"BoxNo":"A202508010000002","SetNum":3,"ActNum":2,"GoodsId":"90450C1","Modelm":"90450","Colors":"C1"}]

                        if (PackBoxlist.size()>0){
                            BoxNoNum=PackBoxlist.get(0).get("SetNum").toString();
                            BoxNoCode=PackBoxlist.get(0).get("BoxNo").toString();

                            OneCount=PackBoxlist.get(0).get("ActNum").toString();

                            BoxActNum=PackBoxlist.get(0).get("ActNum").toString();

                            GoodsId=PackBoxlist.get(0).get("GoodsId").toString();
                            GoodsModelm=PackBoxlist.get(0).get("Modelm").toString();
                            GoodsColor=PackBoxlist.get(0).get("Colors").toString();

                            ShowMessage.ShowMsg(mHandler, 5, result);

                        }else{
                            ShowMessage.ShowMsg(mHandler, 7, "暂无上一次任务");
                        }
                    }else{
                        ShowMessage.ShowMsg(mHandler, 7,scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, 7, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 扫描装盒入库
     */
    public void InStock_PackBox(final String tBarcode) {
        MyProgressDialog.show(mContext, "正在扫描装盒", true, true);

        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 请求头
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer "+sysUserInfo.getLoginid());
                    HttpPostMultipart multipart = new HttpPostMultipart("http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/InStock_PackBox", "utf-8", headers);
//                    Log.d("main","http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/UDIScan");

                    // post参数
                    multipart.addFormField("Barcode", tBarcode);//条码
                    multipart.addFormField("BoxNo", BoxNoCode);
                    multipart.addFormField("PackingNum",String.valueOf(BoxNoNum));
                    multipart.addFormField("SoCompId",Supplierid);//供应商编码
                    multipart.addFormField("StockId",StockId);//仓库系统编码
                    multipart.addFormField("GoodsId",GoodsId);//产品系统编码
                    multipart.addFormField("OaSuserId",sysUserInfo.getUserCode());
                    multipart.addFormField("ScanSn",String.valueOf(nSize));
                    multipart.addFormField("ScanBillNo",scanBillno);//扫描单号

                    Map<String, String> requestdata=new HashMap<>();
                    requestdata.put("Barcode", tBarcode);//条码
                    requestdata.put("BoxNo", BoxNoCode);
                    requestdata.put("PackingNum",String.valueOf(BoxNoNum));
                    requestdata.put("SoCompId",Supplierid);//供应商编码
                    requestdata.put("StockId",StockId);//仓库系统编码
                    requestdata.put("GoodsId",GoodsId);//产品系统编码
                    requestdata.put("OaSuserId",sysUserInfo.getUserCode());
                    requestdata.put("ScanSn",String.valueOf(nSize));
                    requestdata.put("ScanBillNo",scanBillno);//扫描单号
//                    Log.d("main", requestdata.toString());
                    // 返回信息
                    String multiresponse = multipart.finish(requestdata);
//                    Log.d("main", multiresponse);
                    Gson gson = new GsonBuilder().create();
                    ScanApiResponse response = gson.fromJson(multiresponse, ScanApiResponse.class);
                    if (response.isSuccess()==true){
                        //不用实体类，直接解析
                        JSONObject rootObject = new JSONObject(multiresponse);
                        JSONObject OrderDetailsdata = rootObject.getJSONObject("data");

                        List<Map<String, Object>>  packBoxResultList = new ArrayList<Map<String, Object>>();
                        Map<String, Object> map1 = new HashMap<String, Object>();
                        map1.put("BoxNo", OrderDetailsdata.optString("boxNo"));//盒标码
                        map1.put("Barcode", OrderDetailsdata.optString("barcode"));//物流码
                        map1.put("Modelm", OrderDetailsdata.optString("modelm"));//型号
                        map1.put("Colors", OrderDetailsdata.optString("colors"));//色号
                        map1.put("GoodsNum",  NumberUtils.toIntString(OrderDetailsdata.optString("goodsNum")));//当前型号数量
                        map1.put("TotalNum", NumberUtils.toIntString(OrderDetailsdata.optString("totalNum")));//当前合计数
                        map1.put("BoxNum", NumberUtils.toIntString(OrderDetailsdata.optString("boxNum")));//当前装盒成功数

                        map1.put("SetNum", NumberUtils.toIntString(OrderDetailsdata.optString("setNum")));//设置盒装数
                        map1.put("ActNum", NumberUtils.toIntString(OrderDetailsdata.optString("actNum")));//当前盒已装数

                        packBoxResultList.add(map1);


                        if (packBoxResultList.size()>0){
                            nSize++;
                            BoxNoCode=packBoxResultList.get(0).get("BoxNo").toString();//盒标码

                            OneCount=packBoxResultList.get(0).get("GoodsNum").toString();//当前型号数量
                            AllCount=packBoxResultList.get(0).get("TotalNum").toString();//合计数
                            AllBox=packBoxResultList.get(0).get("BoxNum").toString();//当前装盒成功数
                            BoxActNum=packBoxResultList.get(0).get("ActNum").toString();

                            ShowMessage.ShowMsg(mHandler, HandPackBoxSuccess, "");

                        }else{
                            ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "【装盒入库】失败：扫描返回无数据" +multiresponse);
                        }
                    }else{
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg,"【装盒入库】失败："+response.getMessage());
                    }

                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "【装盒入库】失败：" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    /**
     * 设置盒装数
     */
    public void SetFillBoxNum(final String tBoxBarcode,final String tPackingNum) {
        MyProgressDialog.show(mContext, "正在设置盒装数", true, true);

        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 请求头
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer "+sysUserInfo.getLoginid());
                    HttpPostMultipart multipart = new HttpPostMultipart("http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/SetFillBoxNum", "utf-8", headers);
//                    Log.d("main","http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/UDIScan");

                    // post参数
                    multipart.addFormField("BoxNo", tBoxBarcode);
                    multipart.addFormField("PackingNum", tPackingNum);
                    multipart.addFormField("SoCompId",Supplierid);//供应商编码
                    multipart.addFormField("OaSuserId",sysUserInfo.getUserid());//

                    Map<String, String> requestdata=new HashMap<>();
                    requestdata.put("BoxNo", tBoxBarcode);
                    requestdata.put("PackingNum", tPackingNum);
                    requestdata.put("SoCompId",Supplierid);//供应商编码
                    requestdata.put("OaSuserId",sysUserInfo.getUserCode());
//                    Log.d("main", requestdata.toString());
                    // 返回信息
                    String multiresponse = multipart.finish(requestdata);
//                    Log.d("main", multiresponse);
                    Gson gson = new GsonBuilder().create();
                    ScanApiResponse response = gson.fromJson(multiresponse, ScanApiResponse.class);
                    if (response.isSuccess()==true){
                        //不用实体类，直接解析
                        JSONObject rootObject = new JSONObject(multiresponse);
//                        JSONObject OrderDetailsdata = rootObject.getJSONObject("data");
                        JSONArray listjson = rootObject.getJSONArray("data");
//                        JSONArray listjson = new JSONArray(rootObject.getJSONArray("data"));
                        List<Map<String, Object>> unFillBoxList = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < listjson.length(); i++) {
                            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                            Map<String, Object> map1 = new HashMap<String, Object>();
                            map1.put("BoxNo", jsonObject2.optString("boxNo"));
                            map1.put("SetNum", jsonObject2.optString("setNum"));
                            map1.put("ActNum", jsonObject2.optString("actNum"));
                            map1.put("Modelm", jsonObject2.optString("modelm"));
                            map1.put("Colors", jsonObject2.optString("colors"));
                            map1.put("GoodsId", jsonObject2.optString("goodsId"));
                            unFillBoxList.add(map1);
                        }

                        if (unFillBoxList.size()>0){
                            BoxNoNum=unFillBoxList.get(0).get("SetNum").toString();
                            BoxActNum=unFillBoxList.get(0).get("ActNum").toString();
                            ShowMessage.ShowMsg(mHandler,  8, BoxNoNum);
                        }else{
                            ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "修改盒装数接口返回空"+multiresponse);
                        }
                    }else{
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "修改盒装数接口报错："+multiresponse);
                    }

                } catch (Exception e) {
                    ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, e.getMessage());
                }
            }
        });
        sendCode.start();
    }


    private class EtBarcodeOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    String barcodeStr = et_barcode.getText().toString().trim();

                    String barcodeStr = "";

                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        barcodeStr = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        barcodeStr = SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    }

                    et_barcode.setText("");

                    if (GoodsId.isEmpty()) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先选择产品");
                        return true;
                    }
                    if (Integer.parseInt(BoxNoNum)  == 0) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先设置盒装数量");
                        return true;
                    }
                    if (!bluetoothManager.isBluetoothConnected()) {
                        ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "请先连接蓝牙打印机");
                        return true;
                    }

                    if (barcodeStr.startsWith("A")) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + barcodeStr + "】");
                        return true;
                    }
                    if (!SomeUtils.isAllNumber(mContext, barcodeStr)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + barcodeStr + "】");
                        return true;
                    }
                    if (!SomeUtils.TextJudgmentSize(barcodeStr)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请先扫描正确的条码");
                        return false;
                    } else {
//                        addScanPackingBarcode(barcodeStr);
                        //扫描装盒
                        InStock_PackBox(barcodeStr);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case 5:
                    // 检查是否是蓝牙消息（MESSAGE_TOAST）
                    if (msg.getData() != null && msg.getData().containsKey(BluetoothUtil.TOAST)) {
                        // 处理连接失败等错误消息
                        String toastMessage = msg.getData().getString(BluetoothUtil.TOAST);
                        if (toastMessage != null && !toastMessage.isEmpty()) {
                            ShowMessage.Show(mContext, toastMessage);
                            // 连接失败时更新界面状态
                            tv_connect_state.setText("未连接");
                            tv_connect_state.setTextColor(Color.RED);
                            btn_connect.setText("连接");
                            isConnectedBluetooth = false;
                        }
                    } else {
                        //获取上一次未完成的装盒入库任务
                        MyProgressDialog.close();

                        tv_packing_number.setText(BoxNoNum);
                        tv_BoxNo_Code.setText(BoxNoCode);

                        tv_one_count.setText(OneCount);
                        tv_box_count.setText(BoxActNum);

//                        tv_model_color.setText(GoodsModelm+" "+GoodsColor);
//                        tv_productid.setText(GoodsId);
                    }
                    break;
                case 7:
                    //获取上一次未完成的装盒入库任务不存在或者报错时候，跳转选择入库产品界面
                    MyProgressDialog.close();
//                    Intent intent = new Intent(mContext, SelectFactoryGoods.class);
//                    startActivityForResult(intent, Lic_SelectModel);
                    break;

                case 8:
                    //设置盒装数
                    MyProgressDialog.close();

                    int tSacnNum=Integer.parseInt(BoxActNum);//当前盒已扫码数量
                    int tSetnum=Integer.parseInt(BoxNoNum);//设置的装盒数量

                    if (tSacnNum==tSetnum){
                        //判断修改的数量，如果等于已扫码的数量，就要有询问提示，马上打标出来，上传数据，清除本地数量
                        tv_packing_number.setText(BoxNoNum);

                        AllBox=String.valueOf(Integer.parseInt(AllBox)+1);
                        tv_all_box.setText(AllBox);

                        ShowMessage.Show(mContext,"正在打印盒标，请稍后！！！");

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                        boxTag = new BoxTag(BoxNoCode, GoodsBrand, GoodsSeries, GoodsModelm, GoodsColor, BoxNoNum, sysUserInfo.getUserName(), simpleDateFormat.format(new Date()).substring(0, 10),StockName);

                        printBoxCode(boxTag);

                        BoxNoCode="";
                        tv_BoxNo_Code.setText(BoxNoCode);

                    }else if (tSacnNum<tSetnum){
                        //判断修改的数量，如果大于已扫码的数量，累计已扫码数量，就要以当前已扫码的数量为准
                        tv_packing_number.setText(BoxNoNum);
                    }else {
                        tv_packing_number.setText(BoxNoNum);
                    }

                    break;

                case HandPackBoxSuccess:
                    //装盒扫码成任务
                    //				loading.Close();
                    MySound.scanSound();
                    MyProgressDialog.close();

                    tv_BoxNo_Code.setText(BoxNoCode);

                    tv_box_count.setText(BoxActNum);

                    tv_one_count.setText(OneCount);
                    tv_all_count.setText(AllCount);
                    tv_all_box.setText(AllBox);

                    if (Integer.parseInt(BoxActNum)==Integer.parseInt(BoxNoNum))
                    {

                        ShowMessage.Show(mContext,"正在打印盒标，请稍后！！！");

                        SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                        boxTag = new BoxTag(BoxNoCode, GoodsBrand, GoodsSeries, GoodsModelm, GoodsColor, BoxNoNum, sysUserInfo.getUserName(), simpleDateFormat.format(new Date()).substring(0, 10),StockName);
                        printBoxCode(boxTag);

                        BoxNoCode="";
                        tv_BoxNo_Code.setText(BoxNoCode);
                    }


                    break;

                case HandToaskErrorMsg:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    MyProgressDialog.close();
                    break;

                case BluetoothUtil.MESSAGE_DEVICE_NAME:
                    // 获取连接的设备名称和地址
                    String deviceName = msg.getData().getString(BluetoothUtil.DEVICE_NAME);
                    String deviceAddress = msg.getData().getString(BluetoothUtil.DEVICE_ADDRESS);

                    // 确保设备名称不为空，如果为空则使用设备地址
                    if (deviceName == null || deviceName.isEmpty()) {
                        deviceName = deviceAddress;
                    }
                    if (deviceName == null || deviceName.isEmpty()) {
                        deviceName = "未知设备";
                    }

                    connectedDeviceName = deviceName;
                    if (deviceAddress != null && !deviceAddress.isEmpty()) {
                        connectedDeviceAddress = deviceAddress;
                    } else {
                        // 如果消息中没有设备地址，从BluetoothManager获取
                        connectedDeviceAddress = bluetoothManager.getConnectedDeviceAddress();
                    }

                    // 更新UI显示（不依赖连接状态检查，因为消息顺序可能不确定）
                    tv_connect_state.setText("已连接:" + connectedDeviceName);
                    tv_connect_state.setTextColor(Color.parseColor("#008000"));
                    btn_connect.setText("断开");
                    isConnectedBluetooth = true;

                    // 保存连接信息到sysUserInfo（使用装盒入库界面的存储方法）
                    sysUserInfo.setPackingBluetoothName(connectedDeviceName);
                    sysUserInfo.setPackingBluetoothAddress(connectedDeviceAddress);
                    break;

                case BluetoothUtil.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            // 连接成功，立即更新界面
                            // 从BluetoothManager获取设备信息（优先使用）
                            String currentDeviceName = bluetoothManager.getConnectedDeviceName();
                            String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();

                            if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                                connectedDeviceName = currentDeviceName;
                                connectedDeviceAddress = currentDeviceAddress;
                            } else {
                                // 如果BluetoothManager中的设备名称为空，使用已设置的装盒入库界面设备信息
                                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                                    connectedDeviceName = sysUserInfo.getPackingBluetoothName();
                                    connectedDeviceAddress = sysUserInfo.getPackingBluetoothAddress();
                                }
                                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                                    connectedDeviceName = "未知设备";
                                }
                            }

                            // 立即更新界面状态（Handler回调已在主线程）
                            btn_connect.setText("断开");
                            tv_connect_state.setText("已连接:" + connectedDeviceName);
                            tv_connect_state.setTextColor(Color.parseColor("#008000"));
                            isConnectedBluetooth = true;

                            // 保存连接信息到sysUserInfo（使用装盒入库界面的存储方法）
                            sysUserInfo.setPackingBluetoothName(connectedDeviceName);
                            sysUserInfo.setPackingBluetoothAddress(connectedDeviceAddress);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            // 只有在没有设备名称的情况下才显示"正在连接"
                            if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                                btn_connect.setText("连接");
                                tv_connect_state.setText("正在连接...");
                                tv_connect_state.setTextColor(Color.BLACK);
                            } else {
                                btn_connect.setText("连接");
                                tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                                tv_connect_state.setTextColor(Color.BLACK);
                            }
                            isConnectedBluetooth = false;
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            btn_connect.setText("连接");
                            tv_connect_state.setText("未连接");
                            tv_connect_state.setTextColor(Color.RED);
                            isConnectedBluetooth = false;
                            break;
                    }
                    break;
                case HandShowWaitingDialog:
                    MyProgressDialog.show(mContext, msg.obj.toString(), true, false);
                    break;
                case HandCloseWaitingDialog:
                    MyProgressDialog.close();
                    break;

                default:
                    break;
            }
            return false;
        }
    });
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {

                case BluetoothUtil.REQUEST_CONNECT_DEVICE:
                    if (resultCode == Activity.RESULT_OK) {
                        connectedDeviceAddress = data.getStringExtra("deviceAddress");
                        connectedDeviceName = data.getStringExtra("deviceName");

                        // 处理设备名称为空的情况
                        if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                            connectedDeviceName = connectedDeviceAddress;
                        }

                        // 确保蓝牙服务已初始化
                        if (bluetoothManager.getBluetoothService() == null) {
                            bluetoothManager.initBluetoothService(mContext, mHandler);
                        }

                        // 如果服务初始化失败，提示用户
                        if (bluetoothManager.getBluetoothService() == null) {
                            ShowMessage.Show(mContext, "蓝牙服务初始化失败，请检查蓝牙是否已打开");
                            tv_connect_state.setText("未连接");
                            tv_connect_state.setTextColor(Color.RED);
                            btn_connect.setText("连接");
                            return;
                        }

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(connectedDeviceAddress);

                        // 立即更新界面状态为"正在连接"
                        isConnectedBluetooth = false;
                        tv_connect_state.setText("正在连接:" + connectedDeviceName + "...");
                        tv_connect_state.setTextColor(Color.BLACK);
                        btn_connect.setText("连接");

                        // 开始连接
                        boolean connectStarted = bluetoothManager.connect(device);
                        if (!connectStarted) {
                            // 连接失败，服务未初始化
                            ShowMessage.Show(mContext, "蓝牙服务未初始化，无法连接");
                            tv_connect_state.setText("未连接");
                            tv_connect_state.setTextColor(Color.RED);
                            btn_connect.setText("连接");
                            isConnectedBluetooth = false;
                        }
                    }
                    break;

                case Lic_SelectModel:
//                    Log.d("main", GoodsId);
//                    Log.d("main", data.getStringExtra("GoodsId"));
                    if (!GoodsId.equals(data.getStringExtra("goodsid"))){
                        GoodsBrand=data.getStringExtra("brandName");
                        GoodsSeries=data.getStringExtra("prodType");
                        GoodsModelm=data.getStringExtra("modelm");
                        GoodsColor=data.getStringExtra("colors");
                        GoodsId=data.getStringExtra("goodsid");

                        tv_model_color.setText(GoodsModelm+" "+GoodsColor);
                        tv_productid.setText(GoodsId);
                        BoxActNum="0";
                        OneCount="0";
                        tv_box_count.setText(BoxActNum);
                        tv_one_count.setText(OneCount);
                    }
                    break;
                case Lic_Eliminate:
                    OneCount=data.getStringExtra("GoodsNum");
                    AllCount=data.getStringExtra("TotalNum");
                    AllBox=data.getStringExtra("BoxNum");
                    BoxActNum=data.getStringExtra("BoxActNum");
                    if (BoxActNum.equals("0")){
                        BoxNoCode="";//如果当前盒已经剔除完了，那么盒标码就要清空
                        tv_BoxNo_Code.setText(BoxNoCode);
                    }
                    tv_one_count.setText(OneCount);
                    tv_all_count.setText(AllCount);
                    tv_all_box.setText(AllBox);
                    tv_box_count.setText(BoxActNum);
                    break;

                case 4:
                    GoodsBrand="";GoodsSeries="";
                    GoodsModelm=data.getStringExtra("Modelm");
                    GoodsColor=data.getStringExtra("Colors");
                    GoodsId=data.getStringExtra("GoodsId");
                    BoxNoNum=data.getStringExtra("PackingNum");
                    BoxNoCode=data.getStringExtra("BoxNo");

                    OneCount=data.getStringExtra("GoodsNum");
                    AllCount=data.getStringExtra("TotalNum");
                    AllBox=data.getStringExtra("BoxNum");

                    BoxActNum=data.getStringExtra("BoxActNum");

                    tv_one_count.setText(OneCount);
                    tv_all_count.setText(AllCount);
                    tv_all_box.setText(AllBox);

                    tv_model_color.setText(GoodsModelm+" "+GoodsColor);
                    tv_productid.setText(GoodsId);
                    tv_BoxNo_Code.setText(BoxNoCode);
                    tv_packing_number.setText(BoxNoNum);

                    tv_box_count.setText(BoxActNum);

                    if (BoxActNum.equals("0")){
                        BoxNoCode="";//如果当前盒已经剔除完了，那么盒标码就要清空
                        tv_BoxNo_Code.setText(BoxNoCode);
                    }

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    /**
     * 弹出设置数量的dialog
     */
    public void ShowSetNumberDialog() {
        TextView tv_title = new TextView(mContext);
        tv_title.setPadding(10, 10, 10, 10);
        tv_title.setText("盒装数量设置");
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setTextSize(25);
        tv_title.setTextColor(Color.parseColor("#30C0FF"));

        LayoutInflater mInflater = LayoutInflater.from(mContext);
        View view = mInflater.inflate(R.layout.set_packing_num_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.et_number);
        SomeUtils.moveFocus(editText);
        editText.setSingleLine(true);
        editText.setText(BoxNoNum);
        editText.setGravity(Gravity.CENTER);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        builder.setCustomTitle(tv_title)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String numberStr = editText.getText().toString().trim();
                        if (numberStr.isEmpty()) {
                            ShowMessage.Show(mContext, "输入不能为空");
                            return;
                        }
                        //当前盒已扫码数量
                        int SacnNum=Integer.parseInt(BoxActNum);
                        //设置数量
                        int Setnum=Integer.parseInt(numberStr);
                        if (SacnNum>0){
                            //判断修改的数量，如果小于已扫码的数量，是修改不成功
                            if (SacnNum>Setnum){
                                Toast.makeText(mContext,"当前已扫码数量大于设置数量，修改失败",Toast.LENGTH_SHORT).show();
                                MySound.errorSound();
                            }else if (SacnNum==Setnum){
                                //判断修改的数量，如果等于已扫码的数量，就要有询问提示，马上打标出来，上传数据，清除本地数量
                                SetFillBoxNum(BoxNoCode,numberStr);
                            }else if (SacnNum<Setnum){
                                //判断修改的数量，如果大于已扫码的数量，累计已扫码数量，就要以当前已扫码的数量为准);
                                SetFillBoxNum(BoxNoCode,numberStr);
                            }
                        }else{
                            SetFillBoxNum(BoxNoCode,numberStr);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 延迟更新蓝牙状态，确保蓝牙服务已经初始化完成
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 只有在不是正在连接状态时才更新，避免覆盖"正在连接"的显示
                if (bluetoothManager != null && bluetoothManager.getBluetoothService() != null) {
                    int state = bluetoothManager.getBluetoothState();
                    // 如果正在连接中，不更新状态，让连接过程自然完成
                    if (state != BluetoothService.STATE_CONNECTING) {
                        updateBluetoothConnectionStatus();
                    }
                } else {
                    updateBluetoothConnectionStatus();
                }
            }
        }, 200); // 延迟200ms，给更多时间让蓝牙服务初始化

        // 再次延迟检查，确保状态同步
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 只有在不是正在连接状态时才更新，避免覆盖"正在连接"的显示
                if (bluetoothManager != null && bluetoothManager.getBluetoothService() != null) {
                    int state = bluetoothManager.getBluetoothState();
                    // 如果正在连接中，不更新状态，让连接过程自然完成
                    if (state != BluetoothService.STATE_CONNECTING) {
                        updateBluetoothConnectionStatus();
                    }
                } else {
                    updateBluetoothConnectionStatus();
                }
            }
        }, 500); // 延迟500ms再次检查
    }

    /**
     * 更新蓝牙连接状态
     */
    private void updateBluetoothConnectionStatus() {
        if (bluetoothManager == null) {
            return;
        }

        // 检查蓝牙服务是否存在
        if (bluetoothManager.getBluetoothService() == null) {
            // 如果蓝牙服务不存在，尝试重新初始化（使用装盒入库界面的蓝牙设备）
            bluetoothManager.initBluetoothServiceAndAutoConnect(this, mHandler, true);
        }

        if (bluetoothManager.isBluetoothConnected()) {
            // 如果蓝牙已连接，更新界面状态
            String currentDeviceName = bluetoothManager.getConnectedDeviceName();
            String currentDeviceAddress = bluetoothManager.getConnectedDeviceAddress();

            if (currentDeviceName != null && !currentDeviceName.isEmpty()) {
                connectedDeviceName = currentDeviceName;
                connectedDeviceAddress = currentDeviceAddress;
            } else {
                // 如果BluetoothManager中的设备名称为空，使用保存的装盒入库界面设备信息
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = sysUserInfo.getPackingBluetoothName();
                    connectedDeviceAddress = sysUserInfo.getPackingBluetoothAddress();
                }
                if (connectedDeviceName == null || connectedDeviceName.isEmpty()) {
                    connectedDeviceName = "未知设备";
                }
            }

            // 更新UI状态
            isConnectedBluetooth = true;
            tv_connect_state.setText("已连接:" + connectedDeviceName);
            tv_connect_state.setTextColor(Color.parseColor("#008000"));
            btn_connect.setText("断开");
        } else {
            // 如果蓝牙未连接，更新界面状态
            isConnectedBluetooth = false;
            tv_connect_state.setText("未连接");
            tv_connect_state.setTextColor(Color.RED);
            btn_connect.setText("连接");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注意：不要在这里 stop，因为是全局的蓝牙服务，其他界面可能还在使用
        // 如果需要断开，应该在应用退出时调用
    }


    //打印模板
    public void printBoxCode(BoxTag boxTag) {
//        ShowMessage.ShowMsg(mHandler,HandToaskErrorMsg,boxTag.toString());

        if (boxTag == null) {
            ShowMessage.ShowMsg(mHandler, HandToaskErrorMsg, "没有数据可以打印");
            return;
        }
        String message = "";
        if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            message = SomeUtils.readAssetsTxt(mContext, "lab_" + sysUserInfo.getEnterpriseId().toString());
        } else {
            message = SomeUtils.readAssetsTxt(mContext, "lab_00");
        }
        sendMessage(message, boxTag);
    }


    private boolean isFileExists(String filename) {
        AssetManager assetManager = getAssets();
        try {
            String[] names = assetManager.list("");
            for (int i = 0; i < names.length; i++) {
                //	            LogUtil.e(names[i]);
                if (names[i].equals(filename.trim())) {
                    System.out.println(filename + "存在");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(filename + "不存在");
            return false;
        }
        System.out.println(filename + "不存在");
        return false;
    }
    /**
     * 发送给蓝牙打印
     * @param message 模本字符串
     * @param boxTag  要打印的数据封装成的类
     */
    private void sendMessage(String message, BoxTag boxTag) {
        if (!bluetoothManager.isBluetoothConnected()) {
            ShowMessage.Show(mContext, "未连接蓝牙");
            return;
        }
        if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            //52万新  53帕兰德
            if (sysUserInfo.getEnterpriseId().toString().equals("52")||sysUserInfo.getEnterpriseId().toString().equals("53")){
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
//                message = message.replace("%D", boxTag.getPackDate());
            }else if (sysUserInfo.getEnterpriseId().toString().equals("12")){
//                12邦维 用汉印IT4S打印机打印
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
//                message = message.replace("%D", boxTag.getPackDate());
            }else if (sysUserInfo.getEnterpriseId().toString().equals("76")||sysUserInfo.getEnterpriseId().toString().equals("74")||sysUserInfo.getEnterpriseId().toString().equals("00")){
//                逸夫和阿塔那都需要加仓库，品牌代号是76和74
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
                message = message.replace("%D", boxTag.getStockName());
            }else{
                //51
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
                message = message.replace("%D", boxTag.getPackDate());
            }
        } else {
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getStockName());
        }

        //byte[] send = readFileByte();
        byte[] send;
        try {
            send = message.getBytes("GBK");
            bluetoothManager.write(send);
            ShowMessage.Show(mContext, "打印成功");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
}
