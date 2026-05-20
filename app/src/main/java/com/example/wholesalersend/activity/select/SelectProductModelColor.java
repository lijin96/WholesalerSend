package com.example.wholesalersend.activity.select;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SelectProductModelColor
 * @Description: 选择型号色号
 * @Author: lijin
 * @Date: 2023/10/27 15:15
 */
public class SelectProductModelColor extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Context context;
    private SysUserInfo sysUserInfo;
    private AccessWeb accWeb;
    private Handler hand;

    private SimpleAdapter adapter;

    private ListView listView;//列数据的listview
    private EditText et_search;//搜索
    private TextView tv_count, tv_Allcount, tv_selected_model_colors,tv_title;//统计数据条数，已选型号色号
    private Button btn_cancel, btn_ok, select_model_btn_next,btn_seach;


//    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    private List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();
    private Map<String, Object> itemMap;//listview的item

    private String lsv_aim = "";

    int tPage=1;
    private String pagegoodsid = "", pagemodelm = "", pagecolors = "",pagerefractivity="",pagesysgoodsid="",pagegoodscategory="",pagegoodsbrand="";//产品id 产品型号 产品色号 产品折射率 产品系统id 产品品类
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.new_select_product_model_color);
        context = this;
        sysUserInfo = new SysUserInfo(this);
        if (getIntent().getStringExtra("aim")!=null) {
            lsv_aim = getIntent().getStringExtra("aim");
        }
        accWeb=new AccessWeb(getApplicationContext());
        hand=new handShowMsg();
        listView = (ListView) findViewById(R.id.select_model_listview);
        listView.setOnItemClickListener(this);
        btn_cancel = (Button) findViewById(R.id.select_model_btn_cancel);
        btn_ok = (Button) findViewById(R.id.select_model_btn_ok);
        btn_seach=findViewById(R.id.btn_seach);
        tv_count=findViewById(R.id.select_model_tv_count);

        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("【选择产品】 "+sysUserInfo.getAccountSetName());

        tv_selected_model_colors = (TextView) findViewById(R.id.selected_model_tv);

        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);

        select_model_btn_next = (Button) findViewById(R.id.select_model_btn_next);
        select_model_btn_next.setOnClickListener(this);

        et_search = (EditText) findViewById(R.id.select_model_et_search);
        et_search.setText(sysUserInfo.getSeachValue());
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        tPage=1;
                        searchList.clear();
                        sysUserInfo.setSeachValue(et_search.getText().toString().trim());
                        DownLoadDataThread();
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            }
        });

        btn_seach.setOnClickListener(this);

        DownLoadDataThread();
    }

    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(this, "正在获取数据...", true, false);
        searchList= new ArrayList<Map<String, Object>>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String tGoodsTypeName="普通";
                    if (lsv_aim.equals("P_Dv_MendLable_Z")){
                        tGoodsTypeName="";
                    }
//                    searchList = accWeb.GetDownSCSLoadGoodsInfor(et_search.getText().toString(),tPage,tGoodsTypeName);
                    if (ADevicesManager.isSCSNewInterface){
                        searchList = accWeb.NewGetDownSCSLoadGoodsInfor(et_search.getText().toString(), tPage, tGoodsTypeName);
                    }else {
                        searchList = accWeb.GetDownSCSLoadGoodsInfor(et_search.getText().toString(), tPage, tGoodsTypeName);
                    }
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    /**
     * 刷新产品列表
     *
     */
    public void freshListView(List<Map<String, Object>> dataList) {
//        if (tPage==1){
//            searchList.clear();
//        }
        if(dataList.size()==0){
            tv_count.setText("当前" + searchList.size() + " 条");
            Toast.makeText(context, "暂无数据", Toast.LENGTH_SHORT).show();
            if (adapter!=null){
                adapter.notifyDataSetChanged();
            }

        }else{
//            if (searchList.size()>0){
//                searchList.addAll(dataList);
//                if (adapter!=null) {
//                    adapter.notifyDataSetChanged();
//                }
//            }else {
//                searchList.addAll(dataList);
            //把集合数据先排序
//            Collections.sort(list, new SortListMapComparator("goodsid"));
            adapter = new SimpleAdapter(context, searchList, R.layout.new_select_product_model_color_listview_item,
                    new String[]{"GoodsId", "GoodsDescription", "ProdType","BrandName","RefractiveIndex"},
                    new int[]{R.id.tv_goodsid, R.id.tv_product_descr, R.id.tv_prodtype,R.id.tv_brandname,R.id.tv_refractivity});
            listView.setAdapter(adapter);
        }

        tv_count.setText("当前" + searchList.size() + " 条");
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_model_btn_cancel:
                if (btn_cancel.getText().toString().equals("取消")) {
                    tv_selected_model_colors.setText("（已选：）");

                    pagecolors="";
                    pagemodelm="";
                    pagecolors="";

                    btn_cancel.setText("返回");
                }
                //返回
                else {
                    finish();
                }

                break;
            case R.id.select_model_btn_ok:
                if ((pagemodelm + pagecolors).isEmpty()) {
                    SomeUtils.showToask(context, "未选择");
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("goodsid", pagegoodsid);
                    intent.putExtra("modelm", pagemodelm);
                    intent.putExtra("goodssyscode", pagesysgoodsid);
                    intent.putExtra("goodscategory", pagegoodscategory);

                    intent.putExtra("prodType", pagegoodscategory);
                    intent.putExtra("brandName", pagegoodsbrand);

                    intent.putExtra("colors", pagecolors);
                    intent.putExtra("refractivity", pagerefractivity);

                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;

            case R.id.select_model_btn_next:
                if (searchList.size()<200){
                    Toast.makeText(context, "数据已全部加载", Toast.LENGTH_SHORT).show();
                }else{
                    tPage=tPage+1;
                    DownLoadDataThread();
                }
                break;
            case R.id.btn_seach:
                tPage=1;
                searchList.clear();
                sysUserInfo.setSeachValue(et_search.getText().toString().trim());
                DownLoadDataThread();
                break;



            default:
                break;
        }
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    if (tPage>1){
                        tPage=tPage-1;
                    }
                    if (adapter!=null) {
                        adapter.notifyDataSetChanged();
                    }
                    ShowMessage.Show(context, msg.obj.toString());
                    break;
                case ShowMessage.HandSuccess:
                    freshListView(searchList);
                    break;
                default:
                    break;
            }

            MyProgressDialog.close();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        itemMap = (Map<String, Object>) adapterView.getItemAtPosition(position);
//        Log.d("main",itemMap.toString());
        pagegoodsid = (String) itemMap.get("GoodsId");
        pagesysgoodsid= (String) itemMap.get("GoodsSysCode");

        pagerefractivity= (String) itemMap.get("RefractiveIndex");
        pagegoodscategory= (String) itemMap.get("ProdType");
        pagegoodsbrand=(String) itemMap.get("BrandName");

        if (!pagegoodscategory.equals("镜片")){
            pagemodelm = (String) itemMap.get("Modelm");
            pagecolors = (String) itemMap.get("Colors");
            tv_selected_model_colors.setText("（已选：" + pagemodelm + "-" + pagecolors + "）");
        }else{
            pagemodelm = (String) itemMap.get("GoodsId");
            pagecolors =pagerefractivity+'-'+(String) itemMap.get("Breed");
            tv_selected_model_colors.setText("（已选：" + pagemodelm + "-" + pagecolors + "）");
        }
//        tv_selected_model_colors.setText("（已选：" + pagemodelm + "-" + pagecolors + "）");
        btn_cancel.setText("取消");
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
