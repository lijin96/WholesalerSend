package com.example.wholesalersend.activity.instock_in;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_Bill_BeInStock;
import com.example.wholesalersend.entity.BoxTag;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.lib.bluetooth.BluetoothManager;
import com.example.wholesalersend.lib.bluetooth.BluetoothService;
import com.example.wholesalersend.lib.bluetooth.BluetoothUtil;
import com.example.wholesalersend.lib.bluetooth.DeviceListActivity;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: Holyes_Dv_Box_InStock_NoBill
 * @Description: 无单盒标入库
 * @Author: lijin
 * @Date: 2025/8/11 9:55
 */
public class Holyes_Dv_Box_InStock_NoBill extends Activity {

    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo;//
    private Para para = new Para();
    private MyHandler handler;
    private Intent gIntent;

    private PrintUtil printbill;

    private TextView tv_curqty, tv_totalqty, tv_company_name,
            tv_billno, tv_model_colors, tv_stock_name, tv_goodsid,tv_box_totalqty;
    private EditText et_barcode;

    private TextView tv_show_code;
    private TextView tv_title;//标题

    private TextView btn_list;//查看明细

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private String scanBillno = "", mBillNo = "", supplier_id = "", supplier_name = "";
    private String goodsid = "", stock_id = "", stock_name="";
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private String supplier_syscode="",stock_syscode="",goods_syscode="";

    private final int Lic_SelectModel = 2;
    private String curcount = "0",Boxcount = "0",nScanCount = "0";//型号数量 盒装数 合计数
    private int nSize = 0;//次数

    private CheckBox check_boxlabel_name;//是否重打盒标

    private TextView tv_connect_state,btn_connect;//连接蓝牙名称，点击连接蓝牙
    private String connectedDeviceName, connectedDeviceAddress;//连接的蓝牙设备名和蓝牙地址

    private boolean isConnectedBluetooth = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    private boolean isTest = true;//是否测试，测试的话不需要连接蓝牙打印机。输出log.i盒标.编译的时候要false

    private BoxTag boxTag;

    private final int HandSuccessUpdateTextUi = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holyes_dv_box_instock_nobill);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();

        sysUserInfo = new SysUserInfo(getApplicationContext());
        printbill = new PrintUtil();

        //把以前扫描的数据清空
        try {
            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【无单盒标入库】"+sysUserInfo.getAccountSetName());


        check_boxlabel_name=findViewById(R.id.check_boxlabel_name);
        check_boxlabel_name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                check_boxlabel_name.setChecked(isChecked);
            }
        });

        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid))
                .setOnClickListener(new BtnSelectProductClick());

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);

        tv_box_totalqty=findViewById(R.id.tv_box_totalqty);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        supplier_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");//接口传的是仓库系统代号
        stock_name = gIntent.getStringExtra("stock_name");

        supplier_syscode=gIntent.getStringExtra("supplier_syscode");

        if (gIntent.getStringExtra("stock_syscode")!=null) {
            stock_syscode = gIntent.getStringExtra("stock_syscode");
        }

        if (gIntent.getStringExtra("scanBillNo")!=null){
            mBillNo=gIntent.getStringExtra("scanBillNo");
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        scanBillno = sysUserInfo.getUserid() + "ZR" + SomeUtils.RandomScanOrder();// 系统

        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());

        tv_totalqty.setText("0");
        tv_curqty.setText("0");
        tv_billno.setText("");
        if (!mBillNo.equals("")) {
            tv_billno.setText(mBillNo);
        }

        tv_stock_name.setText(stock_name);
        tv_company_name.setText(supplier_name);

        btn_list=findViewById(R.id.btn_list);
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, QueryScanDetail.class);
                intent.putExtra("mBillNo", scanBillno);
                startActivity(intent);
//               BoxTag boxTag1 = new BoxTag("123456789", "品牌", "系列", "型号", "色号", "12", sysUserInfo.getUserCode(), "日期","仓库");
//               printBoxCode(boxTag1);
            }
        });

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
                boolean isAutoConnecting = bluetoothManager.initBluetoothServiceAndAutoConnect(this, handler, true);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sysUserInfo.SaveConfigString("searchProductSql", "");
//		send.interrupt();
//		try {
//			send.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
    }


    /**
     * 处理逻辑的handler
     */
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MySound.scanSound();
                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);
                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    tv_box_totalqty.setText(Boxcount);
                    tv_goodsid.setText("(" + goodsid + ")");
                    tv_company_name.setText(supplier_name);

//                    SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
//                    boxTag = new BoxTag(BoxNoCode, "", "", modelm, colors, BoxNoNum, sysUserInfo.getUserCode(), simpleDateFormat.format(new Date()).substring(0, 10),tv_stock_name.getText().toString().trim());
//                    printBoxCode(boxTag);
                    if (check_boxlabel_name.isChecked()){
                        //如果要重打盒标
                        doP_Dv_InStock_PackBox_Search(msg.obj.toString());
                    }

                    break;
                case ShowMessage.HandScanError:
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;
                case 11:
                    MyProgressDialog.close();
                    String[] mark = new String[3];
                    mark[0] = "入库单：" + mBillNo;
                    mark[1] = "供应商：" + tv_company_name.getText().toString();
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

//                    printbill.print(Holyes_Dv_Box_InStock_NoBill.this, "无单盒标入库", mark, sacnDataList, sysUserInfo.getUserid());
                    if (sysUserInfo.getOldVersion().equals("T8")) {
                        printbill.prints("    无单盒标入库", mark, sacnDataList, sysUserInfo.getUserName());
                    } else {
                        printbill.print(Holyes_Dv_Box_InStock_NoBill.this, "    无单盒标入库", mark, sacnDataList, sysUserInfo.getUserName());

                    }
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
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
                case HandSuccessUpdateTextUi:
                    if (isConnectedBluetooth) {
                        printBoxCode(boxTag);
                    } else {
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "未连接蓝牙，请先连接蓝牙");
                    }
                    break;


                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }


    /**
     * 查找盒标信息
     */
    public void doP_Dv_InStock_PackBox_Search(final String tTempBoxNo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String  result = accWeb.P_Dv_InStock_PackBox_Search(sysUserInfo.getLoginid(), tTempBoxNo);
                    JSONObject jsonObject = new JSONObject(result);
                    //BoxNo,SerialName,Num,UserCode,BrandName,Model,Color,PackDate;
                    String  BoxNo = jsonObject.getString("BoxNo");
                    String BrandName = jsonObject.getString("BrandName");
                    String SerialName = jsonObject.getString("SerialName");
                    String Model = jsonObject.getString("Model");
                    String color = jsonObject.getString("Color");
                    String Num = jsonObject.getString("Num");
                    String UserCode = sysUserInfo.getUserName();//jsonObject.getString("UserCode");
                    String PackDate = jsonObject.getString("PackDate");
                    String StockName = jsonObject.getString("StockName");
                    PackDate = PackDate.replace("/", ".");//把日期格式转换一下
                    boxTag = new BoxTag(BoxNo, BrandName, SerialName, Model, color, Num, UserCode, PackDate,StockName);

                    ShowMessage.ShowMsg(handler, HandSuccessUpdateTextUi, "");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 返回按钮监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //连续按返回按钮两次就退出界面
                if (SomeUtils.isDoubleClick(mContext, true)) {
                    finish();
                }
                return true;
            case KeyEvent.KEYCODE_MINUS:

        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {
                case Lic_SelectModel:
                    goodsid = data.getStringExtra("goodsid");
                    modelm = data.getStringExtra("modelm");
                    colors = data.getStringExtra("colors");
                    goods_syscode=data.getStringExtra("goodssyscode");
                    String productinfo = modelm + "-" + colors;
                    String produvtid = "（" + goodsid + ")";
                    tv_model_colors.setText(productinfo);
                    tv_goodsid.setText(produvtid);
                    break;

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
                            bluetoothManager.initBluetoothService(mContext, handler);
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
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
            bluetoothManager.initBluetoothServiceAndAutoConnect(this, handler, true);
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



//	private class SendDatas extends Thread
//	{
//		@Override
//		public void run() {
//			try
//			{
//				while(!Thread.currentThread().isInterrupted()){
//					if(codesList.size()>0)
//					{
//						if (access_send(codesList.get(0).toString())){
//							codesList.remove(0);
//						}
//						else
//						{
//							codesList.remove(0);
//						}
//					}
//
//				}
//
//			}
//			catch(Exception e)
//			{
//				Log.d("main","thread end");
//			}
//
//		}
//	}


    // 请求服务
    private void access_send(final String contents) {
        if (goodsid.equals("")) {
            MySound.errorSound();
            ShowMessage.ShowMsg(handler, "请先选择产品");
            return;
        }
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;

//                    BoxNo：盒标码(必传)
//                    StockId：仓库代号(必传)
//                    OaSuserId：扫描人员代号(必传)
//                    ScanSn：扫描序号(必传)
//                    ScanBillNo：扫描单号(必传)
//                    BillNo：总公司入库单号(首次传空，后续传接口返回值)

                    Gson gson=new Gson();

                    HashMap<Object, Object> boxmap = new HashMap<Object, Object>();
                    boxmap.put("BoxNo", contents);
                    boxmap.put("StockId", stock_id);
                    boxmap.put("SoCompId", supplier_syscode);
                    boxmap.put("GoodsId", goods_syscode);
                    boxmap.put("OaSuserId", sysUserInfo.getUserid());
                    boxmap.put("ScanSn", String.valueOf(nSize));
                    boxmap.put("ScanBillNo", scanBillno);
                    boxmap.put("BillNo", mBillNo);

//                    Log.d("main",boxmap.toString());
                    result = accWeb.P_Dv_Scan("Holyes_Dv_PackBox_InStock", gson.toJson(boxmap));

//                    Log.d("main",result);

                    JSONArray listjson = new JSONArray(result);
                    List<Map<String, Object>> WeedOutList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        Map<String, Object> map1 = new HashMap<String, Object>();
//                        map1.put("Modelm", jsonObject2.optString("Modelm"));
//                        map1.put("Colors", jsonObject2.optString("Colors"));

                        map1.put("GoodsNum", jsonObject2.optString("GoodsNum"));
                        map1.put("TotalNum", jsonObject2.optString("TotalNum"));
                        map1.put("BoxNum", jsonObject2.optString("BoxNum"));

//                        map1.put("BrandName", jsonObject2.optString("BrandName"));
                        map1.put("BillNo", jsonObject2.optString("BillNo"));

//                        map1.put("GoodsId", jsonObject2.optString("GoodsId"));

//                        map1.put("SupplierName", jsonObject2.optString("SupplierName"));
//                        map1.put("SupplierId", jsonObject2.optString("SupplierId"));

                        WeedOutList.add(map1);
                    }

                    if (WeedOutList.size() ==0) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "服务器返回参数不足 " + result);
                        return;
                    }
                    nSize++;
                    //true;产品编号,型号,色号,当前型号数量,当前扫描的条码,入库单号
//                    goodsid = WeedOutList.get(0).get("GoodsId").toString();
//                    modelm = WeedOutList.get(0).get("Modelm").toString();
//                    colors = WeedOutList.get(0).get("Colors").toString();
                    curcount = WeedOutList.get(0).get("GoodsNum").toString();
                    Boxcount=WeedOutList.get(0).get("BoxNum").toString();


                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = WeedOutList.get(0).get("BillNo").toString();;
                    }
                    if (Integer.parseInt(nScanCount) < Integer.parseInt(WeedOutList.get(0).get("TotalNum").toString())) {
                        nScanCount = WeedOutList.get(0).get("TotalNum").toString();
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess, contents);
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,e.getMessage());
                    lStar = SomeUtils.isNotFromServiceError(e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    /**
     * 输入框监听
     */
    private class EtBarodeOnkeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_ENTER) {

//
//                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm.isActive()) {
//                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
//                }



                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode = "";

                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    } else {
                        //不包含
                        tBarcode = SomeUtils.UpdatefirstString(mContext,et_barcode.getText().toString().trim());
                    }


                    tv_show_code.setText(tBarcode);
                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                        MySound.errorSound();
                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode + "】");
                        et_barcode.setText("");
                        return true;
                    }
                    if (check_boxlabel_name.isChecked()&&!isConnectedBluetooth) {
                        //如果勾选了重打盒标，但是没有连接蓝牙
                        ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "未连接蓝牙，重打盒标请先连接蓝牙");
                        return true;
                    }
                    access_send(tBarcode);
                    et_barcode.setText("");
                }
                return true;
            } else {
                return false;
            }
        }
    }



    /**
     * 打印按钮监听类
     */
    private class BtnPrintClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MyProgressDialog.show(mContext, "正在打印...", false, true);
            Thread sendprint = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), scanBillno);
                        if (sacnDataList.size() == 0) {
                            ShowMessage.ShowMsg(handler, 9, "没有可打印的数据");
                            return;
                        }

                        ShowMessage.ShowMsg(handler, 11, "打印");
                    } catch (Exception e) {
                        ShowMessage.ShowMsg(handler, 9, e.getMessage());
                    }

                }
            });
            sendprint.start();


        }
    }

    /**
     * 退出按钮监听类
     */
    private class BtnExitClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (SomeUtils.isDoubleClick(mContext, true)) {
                finish();
            }
        }
    }

    /**
     * 选择型号色号按钮监听
     */
    private class BtnSelectProductClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, SelectProductModelColor.class);
            startActivityForResult(intent, Lic_SelectModel);
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    public void printBoxCode(BoxTag boxTag) {
        if (boxTag == null) {
            ShowMessage.ShowMsg(handler, ShowMessage.HandScanError, "没有数据可以打印");
            return;
        }

        String message = "";
        if (isFileExists("lab_" + sysUserInfo.getEnterpriseId().toString() + ".txt")) {
            if (sysUserInfo.getEnterpriseId().toString().equals("11")&&boxTag.getModel().equals("")&&boxTag.getColor().equals("")){
                message = SomeUtils.readAssetsTxt(mContext, "lab_11_a");
            }else {
                message = SomeUtils.readAssetsTxt(mContext, "lab_" + sysUserInfo.getEnterpriseId().toString());
            }
        } else {
//			message = SomeUtils.readFileString(mContext,BluetoothUtil.BoxTagModel);
            message = SomeUtils.readAssetsTxt(mContext, "lab_00");
//            message = SomeUtils.readAssetsTxt(mContext, "lab_jb");
        }
//        Log.d("main",message);
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
            }else if (sysUserInfo.getEnterpriseId().toString().equals("11")){
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%U", boxTag.getUserCode());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%S", boxTag.getSerialName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
                message = message.replace("%D", boxTag.getPackDate());
            }else{
                //51
                message = message.replace("%BOX", boxTag.getBoxNo());
                message = message.replace("%B", boxTag.getBrandName());
                message = message.replace("%M", boxTag.getModel());
                message = message.replace("%C", boxTag.getColor());
                message = message.replace("%N", boxTag.getNum());
                message = message.replace("%D", boxTag.getStockName());
            }
        } else {
            message = message.replace("%BOX", boxTag.getBoxNo());
            message = message.replace("%U", boxTag.getUserCode());
            message = message.replace("%B", boxTag.getBrandName());
            message = message.replace("%S", boxTag.getSerialName());
            message = message.replace("%M", boxTag.getModel());
            message = message.replace("%C", boxTag.getColor());
            message = message.replace("%N", boxTag.getNum());
            message = message.replace("%D", boxTag.getStockName());;
        }
//        Log.d("main", message);
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
}

