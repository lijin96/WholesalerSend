package com.example.wholesalersend.activity.instock_in;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.CheckLensDegree;
import com.example.wholesalersend.activity.select.QueryScanDetail;
import com.example.wholesalersend.activity.select.QueryScanLensDetail;
import com.example.wholesalersend.activity.select.SelectLensProduct;
import com.example.wholesalersend.activity.select.SelectProductModelColor;
import com.example.wholesalersend.entity.ApiResponse;
import com.example.wholesalersend.entity.GoodsMarkLabelScan;
import com.example.wholesalersend.entity.LensPara;
import com.example.wholesalersend.entity.Para;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.PrintUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * @ClassName: P_Dv_Lens_InStock_NoBill
 * @Description: 无单镜片入库
 * @Author: lijin
 * @Date: 2021/3/10 13:59
 */
public class P_Dv_Lens_InStock_NoBill extends Activity {
    private Context mContext;
    private AccessWeb accWeb;//后台服务工具类
    private SysUserInfo sysUserInfo; //
    private LensPara para = new LensPara();
    //    private UploadPara uploadPara=new UploadPara();
    private PrintUtil printbill;
    private MySound sound;
    private MyHandler handler;
    private Intent gIntent;

    private TextView tv_curqty, tv_totalqty, tv_company_name, tv_source_billno,
            tv_billno, tv_model_colors, tv_stock_name, tv_goodsid,tv_refractive,tv_title;
    private EditText et_barcode;
    private TextView tv_show_code;
    private Button btn_modifyordernum;//修改采购数
    private Button btn_AddLuminosity;//新增光度

    private List<Map<String, Object>> sacnDataList = new ArrayList<Map<String, Object>>();

    private int PurQty=0;
    private String lsv_aim = "";
    private String Refractive="", Astigmatism = "", Diopter = "",ScanQty="",GoodsSysCode="";//柱球镜

    private String scanBillno = "", mBillNo = "", supplier_id = "", supplier_name = "";
    private String curcount = "0", goodsid = "", stock_id = "", stock_name, sourceBillNo;
    private String lastSuccessBarcode = "", lStar = "";
    private String modelm = "", colors = "";

    private final int Lic_SelectModel = 2;
    private String nScanCount = "0";//合计
    private int nSize = 0;//次数

    //	private List<String> codesList= new ArrayList<String>();
//
//	Thread send ;
    private AlertDialog AddLuminositydialog;//新增光度的弹窗
    private AlertDialog Modifydialog;//修改采购订单数的弹窗

    private String dialogType="";//新增还是修改

    private TextView tv_lastscannum;//

    private RadioGroup radioChange;
    private RadioButton rb_logisticscode,rb_productcode;//物流码还是产品码


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_p_dv_instock_lens_nobill);

        mContext = this;
        accWeb = new AccessWeb(this);
        handler = new MyHandler();
        gIntent = getIntent();
        printbill = new PrintUtil();

        sysUserInfo = new SysUserInfo(getApplicationContext());

        //把以前扫描的数据清空
//        try {
//            SqliteDataHelper.getHelper(getApplicationContext()).execSQL("delete from newscandate");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        ((Button) findViewById(R.id.btn_list))
                .setOnClickListener(new BtnListClick());
        ((Button) findViewById(R.id.btn_print))
                .setOnClickListener(new BtnPrintClick());
        ((Button) findViewById(R.id.btn_exit))
                .setOnClickListener(new BtnExitClick());

        //选择型号色号
        ((Button) findViewById(R.id.btn_select_goodsid))
                .setOnClickListener(new BtnSelectProductClick());

        findViewById(R.id.btn_chooserefractive).setOnClickListener(new BtnChooseRefractive());

        findViewById(R.id.btn_AddLuminosity).setOnClickListener(new BtnAddLuminosityData());

        tv_company_name = ((TextView) findViewById(R.id.tv_company_name));
        tv_stock_name = (TextView) findViewById(R.id.tv_stock_name);
        tv_curqty = (TextView) findViewById(R.id.tv_curqty);
        tv_totalqty = (TextView) findViewById(R.id.tv_totalqty);
        tv_billno = (TextView) findViewById(R.id.tv_billno);
        tv_model_colors = (TextView) findViewById(R.id.tv_model_colors);
        tv_source_billno = (TextView) findViewById(R.id.tv_source_billno);
        tv_goodsid = (TextView) findViewById(R.id.tv_goodsid);
        tv_refractive=(TextView) findViewById(R.id.tv_refractive);

        tv_show_code = (TextView) findViewById(R.id.tv_show_code);

        sysUserInfo.setSeachValue("");

        et_barcode = (EditText) findViewById(R.id.et_barcode);
        et_barcode.setOnKeyListener(new EtBarodeOnkeyListener());

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【无单镜片入库】 "+sysUserInfo.getAccountSetName());

        tv_lastscannum=findViewById(R.id.tv_lastscannum);


        lsv_aim = getIntent().getStringExtra("aim");
        supplier_id = gIntent.getStringExtra("supplier_id");
        supplier_name = gIntent.getStringExtra("supplier_name");
        stock_id = gIntent.getStringExtra("stock_id");
        stock_name = gIntent.getStringExtra("stock_name");
        sourceBillNo = gIntent.getStringExtra("purchecklno");
//        Astigmatism=gIntent.getStringExtra("Astigmatism");
//        Diopter=gIntent.getStringExtra("Diopter");
//        Product_id=getIntent().getStringExtra("Product_id");
//        Refractive = getIntent().getStringExtra("refractive");
//        ScanQty=gIntent.getStringExtra("ScanQty");

//        PurQty=(int)Double.parseDouble(gIntent.getStringExtra("PurQty")) ;
//        tv_model_colors.setText("S "+Diopter + " C " + Astigmatism);
        tv_model_colors.setText("请选择球柱镜");
//        tv_goodsid.setText(Product_id);
        tv_refractive.setText(Refractive);

        scanBillno = sysUserInfo.getUserid() + "S" + SomeUtils.RandomScanOrder();// 系统
//        镜片入库的话，需要在选择折射率的时候就生成扫描单号
//        scanBillno=getIntent().getStringExtra(("scanBillno"));
        sysUserInfo.setIsDownload(false);

        tv_totalqty.setText(PurQty+"");
        tv_curqty.setText("0");
        tv_billno.setText("");
        tv_company_name.setText(supplier_name);
        tv_stock_name.setText(stock_name);
        tv_source_billno.setText(sourceBillNo);

        String ChooseBill=gIntent.getStringExtra("scanBillNo");
        String ChooseBillnum=gIntent.getStringExtra("scanBillNum");

        if (ChooseBill!=null&&ChooseBill!=""){
            mBillNo=ChooseBill;
            tv_billno.setText(mBillNo);
        }

        if (ChooseBillnum!=null&&!ChooseBillnum.equals("")){
//            nScanCount=ChooseBillnum;
//            tv_totalqty.setText(nScanCount);
            tv_lastscannum.setText("上次扫描："+ChooseBillnum);

        }
//		send = new SendDatas();
//		send.start();

        rb_logisticscode=findViewById(R.id.radio_logisticscode);
        rb_productcode=findViewById(R.id.radio_productcode);
        rb_logisticscode.setChecked(true);
//        radioChange.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//
//            }
//        });
        rb_logisticscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rb_logisticscode.setChecked(true);
                rb_productcode.setChecked(false);
            }
        });
        rb_productcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rb_productcode.setChecked(true);
                rb_logisticscode.setChecked(false);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 处理逻辑的handler
     */
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandScanSuccess:
                    MySound.scanSound();

//                    tv_model_colors.setText(modelm + "-" + colors);
                    tv_curqty.setText(curcount);
                    tv_totalqty.setText(nScanCount);
                    if (tv_billno != null) {
                        tv_billno.setText(mBillNo);
                    }
                    break;
                case ShowMessage.HandScanError:
                    MyProgressDialog.close();
                    MySound.errorSound();
                    ShowMessage.Show(mContext, msg.obj.toString());

                    break;

                case 8:
                    MyProgressDialog.close();
                    String[] mark = new String[3];
                    mark[0] = "入库单：" + mBillNo;
                    mark[1] = "供应商：" + tv_company_name.getText().toString();
                    mark[2] = "仓   库 ：" + tv_stock_name.getText().toString();

                    if (sysUserInfo.getOldVersion().equals("T8")) {

                        printbill.prints("           有单镜片入库", mark, sacnDataList, sysUserInfo.getUserName());

                    } else {

                        printbill.print(P_Dv_Lens_InStock_NoBill.this, "           有单镜片入库", mark, sacnDataList, sysUserInfo.getUserName());

                    }
                    break;
                case 9:
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    MyProgressDialog.close();
                    rb_logisticscode.setChecked(true);
                    rb_productcode.setChecked(false);
                    tv_refractive.setText(Refractive);
                    tv_model_colors.setText("S "+Diopter + " C " + Astigmatism);
                    tv_goodsid.setText("(" + goodsid + ")");

                    ShowMessage.Show(mContext, "已选择商品");
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    }


//    /**
//     * 返回按钮监听
//     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                //连续按返回按钮两次就退出界面
//                if (SomeUtils.isDoubleClick(mContext, true)) {
//                    finish();
//                }
//                return true;
//            case KeyEvent.KEYCODE_MINUS:
//
//        }
//        return false;
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {
                case Lic_SelectModel:
//                    goodsid = data.getStringExtra("goodsid");
//                    modelm = data.getStringExtra("modelm");
//                    colors = data.getStringExtra("colors");
//                    String productinfo = modelm + "-" + colors;
//                    tv_model_colors.setText(productinfo);
//                    tv_goodsid.setText("(" + goodsid + ")");

                    Astigmatism=data.getStringExtra("Astigmatism");
                    Diopter=data.getStringExtra("Diopter");
                    PurQty=(int)Double.parseDouble(data.getStringExtra("PurQty")) ;

                    tv_model_colors.setText("S "+Diopter + " C " + Astigmatism);
//                    tv_totalqty.setText(PurQty+"");
                    tv_curqty.setText(PurQty+"");

                    break;
                case 3:
                    GoodsSysCode= data.getStringExtra("GoodsSysCode");
                    goodsid = data.getStringExtra("Product_id");
                    Refractive= data.getStringExtra("refractive");
                    tv_refractive.setText(Refractive);
                    tv_goodsid.setText("(" + goodsid + ")");
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
//


    // 获取产品码对应的商品
    private void DownLoadDataThread(final String tDateCode) {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
//        list = new ArrayList<Map<String, Object>>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();
                    Date now = new Date();
                    //Log.d("main", "loginid: "+sysUserInfo.getLoginid());
                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GoodsMarkLabelScan"+ "?GoodsType="+"&GoodsMarkLabel="+tDateCode;

                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
//                    Log.d("main", result);
                    ApiResponse<GoodsMarkLabelScan> response = gson.fromJson(
                            result,
                            new TypeToken<ApiResponse<GoodsMarkLabelScan>>(){}.getType()
                    );

                    if(response.isSuccess()) { // 假设有isSuccess()方法
                        GoodsMarkLabelScan goods = response.getData();
//                        modelm=goods.getModelm();
//                        colors=goods.getColors();
//                        goodsid=goods.getGoodsCode();


                        if (goods.getGoodsTypeName().equals("镜片")){

                            Astigmatism= goods.getAstigmatism().toString();
                            Diopter=goods.getDiopter().toString();

                            GoodsSysCode= goods.getGoodsSysCode();
                            goodsid = goods.getGoodsCode();
                            Refractive= goods.getRefractiveIndex();
                            ShowMessage.ShowMsg(handler, ShowMessage.HandSuccess,"ok");
                        }else{
                            ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,"镜片入库只能选择镜片产品");
                        }
                    }else{
                        ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,response.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandShowMessage,
                            e.getMessage());
                }
            }
        }).start();
    }


    // 请求服务
    private void access_send(final String contents) {
//        if (goodsid.equals("")) {
//            MySound.errorSound();
//            ShowMessage.ShowMsg(handler, "请先扫描产品ID或者手动选择产品");
//            return;
//        }
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                try {

                    accWeb.mWebId = lStar + contents;
                    para.setBarcode(contents);
                    para.setGoodsId(goodsid);
                    para.setSoCompId(supplier_id);
                    para.setDiopter(Diopter);
                    para.setAstigmatism(Astigmatism);
                    para.setDeCompId("00");
                    para.setOaSuserId(sysUserInfo.getUserid());
                    para.setStockId(stock_id);
                    para.setScanSn(String.valueOf(nSize));
                    para.setScanBillNo(scanBillno);
                    para.setBillNo(mBillNo);
                    para.setSourceBillNo(sourceBillNo);

//                    Log.d("mian",para.toJson());
                    result = accWeb.P_Dv_Scan("P_Dv_InStock_Lens_NoBill", para.toJson());
//                    Log.d("mian",result);

                    if (result == "") {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "网络不给力，请稍后再试！");
                        return;
                    }
                    //true; 产品代号+折射率+球镜+柱镜+此产品球柱镜扫描数量+条码+单据编号+单据扫描数量
                    String[] rest = result.split(",");

                    if (rest.length < 6) {
                        MySound.errorSound();
                        ShowMessage.ShowMsg(handler, "服务器返回参数不足，当前" + rest.length + "位！");
                        return;
                    }
//                    产品代号+折射率+球镜+柱镜+此产品球柱镜扫描数量+条码+单据编号+单据扫描数量
                    nSize++;
                    //goodsid = rest[0].trim();
                    Diopter = rest[2].trim();
                    Astigmatism = rest[3].trim();
                    curcount = rest[4].trim();
                    lastSuccessBarcode = rest[5].trim();

                    if (mBillNo == null || mBillNo.isEmpty()) {
                        mBillNo = rest[6];
                    }
                    if (Integer.parseInt(nScanCount) < Integer.parseInt(rest[7].trim())) {
                        nScanCount = rest[7].trim();
                    }

                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanSuccess, "ok");
                    lStar = "";

                } catch (Exception e) {
                    ShowMessage.ShowMsg(handler, ShowMessage.HandScanError,
                            e.getMessage());
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
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String tBarcode = et_barcode.getText().toString().trim();
                    if (et_barcode.getText().toString().trim().indexOf("=") != -1||et_barcode.getText().toString().trim().indexOf("http") != -1) {
                        //包含
                        tBarcode = SomeUtils.InterceptCode(mContext, et_barcode.getText().toString().trim());
                    }

//                    String firstCode=tBarcode.substring(0, 2);
//                    if (firstCode.equals("28")&&tBarcode.length()==15){
                    if (rb_productcode.isChecked()==true){
                        //商品码 可查询型号色号
                        DownLoadDataThread(tBarcode);
                        et_barcode.setText("");
                    }else{
                        tv_show_code.setText(tBarcode);
                        if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
                            MySound.errorSound();
                            ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode+ "】");
                            et_barcode.setText("");
                            return true;
                        }
                        if (Diopter.equals("")||Astigmatism.equals("")){
                            MySound.errorSound();
                            ShowMessage.Show(mContext,"请先选择球柱镜");
                            et_barcode.setText("");
                            return true;
                        }
                        //接口对接
                        access_send(tBarcode);
                        et_barcode.setText("");;
                    }

//
//                    tv_show_code.setText(tBarcode);
//                    if (!SomeUtils.isAllNumber(mContext, tBarcode)) {
//                        MySound.errorSound();
//                        ShowMessage.Show(mContext, "请扫描正确的物流码【" + tBarcode+ "】");
//                        et_barcode.setText("");
//                        return true;
//                    }
//                    if (Diopter.equals("")||Astigmatism.equals("")){
//                        MySound.errorSound();
//                        ShowMessage.Show(mContext,"请先选择球柱镜");
//                        et_barcode.setText("");
//                        return true;
//                    }
//                    //接口未对接
//                    access_send(tBarcode);
////                    MySound.scanSound();
////                    tv_curqty.setText(""+(Integer.parseInt(ScanQty)+1));
//                    et_barcode.setText("");

                }
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * 明细按钮监听类
     */
    private class BtnListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext,
                    QueryScanLensDetail.class);
            intent.putExtra("mBillNo", scanBillno);
            startActivity(intent);
        }
    }

    /**
     * 打印按钮监听类
     */
    private class BtnPrintClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MyProgressDialog.show(mContext, "正在打印...", true, true);
            Thread sendprint = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        sacnDataList = accWeb.GetDowLoadBilldetail(sysUserInfo.getLoginid(), scanBillno);
                        if (sacnDataList.size() == 0) {
                            ShowMessage.ShowMsg(handler, 9, "没有可打印的数据");
                            return;
                        }

                        ShowMessage.ShowMsg(handler, 8, "打印");
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
     * 选择球柱镜
     */
    private class BtnSelectProductClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(mContext, SelectBillProduct.class);
//            intent.putExtra("orderno", sourceBillNo);
//            intent.putExtra("aim", "P_Dv_InStock_Bill");
//            startActivityForResult(intent, Lic_SelectModel);
            if (goodsid.equals("")){
                ShowMessage.Show(mContext,"请先选择镜片折射率");
            }else {
                Intent intent = new Intent(mContext, CheckLensDegree.class);
//            intent.putExtra("supplier_name", supplier_name);
//            intent.putExtra("stock_name", stock_name);
//            intent.putExtra("supplier_id", supplier_id);
//            intent.putExtra("stock_id", stock_id);
                intent.putExtra("goodssyscode", GoodsSysCode);

                intent.putExtra("Product_id", goodsid);
//            intent.putExtra("GoodsCode", (String) item.get("Product_id"));
                intent.putExtra("scanBillno", scanBillno);
                intent.putExtra("purchecklno", mBillNo);
                intent.putExtra("refractive", Refractive);

                intent.putExtra("aim", lsv_aim);
                startActivityForResult(intent, Lic_SelectModel);
            }
        }
    }

    /**
     * 选择折射率
     */
    private class BtnChooseRefractive implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            intent.setClass(mContext, SelectLensProduct.class);
            intent.putExtra("aim", lsv_aim);
            startActivityForResult(intent, 3);
        }
    }

    /**
     * 新增要采购的光度和数量
     */
    private class BtnAddLuminosityData implements View.OnClickListener {
        @Override
        public void onClick(View v)   {
            if (goodsid.equals("")){
                ShowMessage.Show(mContext,"请先选择镜片折射率");
            }else{
                ShowAddProcureData();
            }
        }
    }

    //添加采购的球柱镜
    private void ShowAddProcureData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);

        builder.setCancelable(false);
        builder.setTitle("新增球柱镜");
        builder.setPositiveButton("新增扫描", null);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            }
        });
        AddLuminositydialog = builder.create();
        View dialogView = View.inflate(mContext, R.layout.addprocure_dialog, null);
        //设置对话框布局
        AddLuminositydialog.setView(dialogView);
        final EditText ed_sphericalmirror=dialogView.findViewById(R.id.ed_sphericalmirror);//球镜
        final EditText ed_cylinder=dialogView.findViewById(R.id.ed_cylinder);//柱镜
//        final EditText ed_addprocure_num = (EditText) dialogView.findViewById(R.id.ed_addprocure_num);//采购订单数
        ed_sphericalmirror.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ed_cylinder.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED|InputType.TYPE_NUMBER_FLAG_DECIMAL);
//        ed_addprocure_num.setText("1");


        ed_sphericalmirror.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    //焦点消失的时候
                    if (!ed_sphericalmirror.getText().toString().trim().equals("")){
                        String sphdegrees=toDecimal(ed_sphericalmirror.getText().toString().trim());
                        ed_sphericalmirror.setText(sphdegrees);
                    }
                }
            }
        });

        ed_cylinder.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    //焦点消失的时候
                    if (!ed_cylinder.getText().toString().trim().equals("")){
                        String cyldegrees=toDecimal(ed_cylinder.getText().toString().trim());
                        ed_cylinder.setText(cyldegrees);
                    }
                }
            }
        });



        AddLuminositydialog.show();
        AddLuminositydialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ed_sphericalmirror.requestFocus();
                ed_cylinder.requestFocus();
//                ed_addprocure_num.requestFocus();
//                ed_addprocure_num.setSelection(ed_addprocure_num.getText().toString().trim().length());

//                String pattern = "^\\d+(\\.00|\\.25|\\.50|\\.75)$";
//                String pattern =  "^-?\\d+(\\\\.00|\\\\.25|\\\\.50|\\\\.75)$";
                String pattern = "^-?\\d+(\\.00|\\.25|\\.50|\\.75)?$";

                String spherical_mirror=ed_sphericalmirror.getText().toString().trim();
                String cylinder=ed_cylinder.getText().toString().trim();
//                String addProcure_num=ed_addprocure_num.getText().toString().trim();
                if (spherical_mirror.equals("")){
                    ShowMessage.Show(mContext,"请输入球镜的度数");
                }else if (cylinder.equals("")){
                    ShowMessage.Show(mContext,"请输入柱镜的度数");
                }else if (!Pattern.matches(pattern, spherical_mirror)){
                    ShowMessage.Show(mContext,"请输入正确的球镜度数");
                }else if(!Pattern.matches(pattern, cylinder)){
                    ShowMessage.Show(mContext,"请输入正确的柱镜度数");
                }else {
//                    dialogType="新增";
//                    UploadDataThread(spherical_mirror,cylinder,Integer.parseInt(addProcure_num));
//                    if (AddLuminositydialog!=null){
//                     AddLuminositydialog.dismiss();
//                    }
                    if (Double.parseDouble(spherical_mirror)==0) {
                        spherical_mirror="0.00";
                    }
                    if (Double.parseDouble(cylinder)==0) {
                        cylinder="0.00";
                    }

                    Diopter = spherical_mirror;
                    Astigmatism = cylinder;
                    tv_model_colors.setText("S "+Diopter + " C " + Astigmatism);
                    if (AddLuminositydialog!=null){
                        AddLuminositydialog.dismiss();
                    }
                }
            }
        });
    }
    public static String toDecimal(String v) {
        Float f = Float.valueOf(v);
//        @SuppressLint("DefaultLocale")
        String format = String.format("%.2f", f);
        return format;
    }





    /**
     * 获取点击事件,是否隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SomeUtils.isNeedHideAndDo(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    //	//设置字体为默认大小，不随系统字体大小改而改变
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		if (newConfig.fontScale != 1)//非默认值
//			getResources();
//		super.onConfigurationChanged(newConfig);
//	}
//
//
//	@Override
//	public Resources getResources() {
//		Resources res = super.getResources();
//		if (res.getConfiguration().fontScale != 1) {//非默认值
//			Configuration newConfig = new Configuration();
//			newConfig.setToDefaults();//设置默认
//			res.updateConfiguration(newConfig, res.getDisplayMetrics());
//		}
//		return res;
//	}
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}


