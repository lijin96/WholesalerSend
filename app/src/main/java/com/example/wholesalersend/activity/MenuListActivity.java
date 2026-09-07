package com.example.wholesalersend.activity;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Lens_Z_L_NoBill_Cancel;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill;
import com.example.wholesalersend.activity.backgoods_fd.P_Dv_ReturnedPurchase_Z_L_NoBill_Cancel;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Lens_Z_D_NoBill_Cancel;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Z_D_NoBill;
import com.example.wholesalersend.activity.backgoods_zd.P_Dv_ReturnedPurchase_Z_D_NoBill_Cancel;
import com.example.wholesalersend.activity.instock_back.P_Dv_ReturnedPurchase_Lens_Z_G_NoBill;
import com.example.wholesalersend.activity.instock_back.P_Dv_ReturnedPurchase_Lens_Z_G_NoBill_Cancel;
import com.example.wholesalersend.activity.instock_back.P_Dv_ReturnedPurchase_Z_G_NoBill;
import com.example.wholesalersend.activity.instock_back.P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Bill_Cancel;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Lens_Bill_Cancel;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_Lens_NoBill_Cancel;
import com.example.wholesalersend.activity.instock_in.P_Dv_InStock_NoBill_Cancel;
import com.example.wholesalersend.activity.other.P_Dv_InStock_PackBox_Search;
import com.example.wholesalersend.activity.other.P_Dv_InStock_Z_ChangeCode;
import com.example.wholesalersend.activity.other.P_Dv_InStock_Z_ChangeProduct;
import com.example.wholesalersend.activity.other.P_Dv_RecoverScan_Z;
import com.example.wholesalersend.activity.select.CheckLensDegree;
import com.example.wholesalersend.activity.select.SelectAllPeiBill;
import com.example.wholesalersend.activity.select.SelectAllots;
import com.example.wholesalersend.activity.select.SelectBrandInStockBill;
import com.example.wholesalersend.activity.select.SelectCompanyRetailer;
import com.example.wholesalersend.activity.select.SelectCompanyStoreInfor;
import com.example.wholesalersend.activity.select.SelectLensBill;
import com.example.wholesalersend.activity.select.SelectOutStock;
import com.example.wholesalersend.activity.select.SelectPeiBill;
import com.example.wholesalersend.activity.select.SelectPurcheck;
import com.example.wholesalersend.activity.select.SelectStock;
import com.example.wholesalersend.activity.select.SelectSupplier;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_Bill_Cancel;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Lens_Z_L_NoBill_Cancel;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_Bill_Cancel;
import com.example.wholesalersend.activity.sendgoods_fd.P_Dv_OutStock_Z_L_NoBill_Cancel;
import com.example.wholesalersend.activity.sendgoods_fd.ReplaceLabCode;
import com.example.wholesalersend.activity.sendgoods_fd.SunLensReplaceLabCode;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_Bill_Cancel;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Lens_Z_D_NoBill_Cancel;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_Bill_Cancel;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_NoBill_Cancel;
import com.example.wholesalersend.adapter.ClickMenuListViewAdapter;
import com.example.wholesalersend.entity.MenuChild;
import com.example.wholesalersend.entity.MenuGroup;
import com.example.wholesalersend.lib.SortMenuListComparator;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: MenuListActivity
 * @Description: 折叠菜单界面显示 零售商
 * @Author: lijin
 * @Date: 2023/10/25 16:16
 */
public class MenuListActivity extends Activity {
    private Context mContext;
    private SysUserInfo sysUserInfo;
    private ClickMenuListViewAdapter menuListViewAdapter;

    private Button btn_back, btn_confirm;
    private ExpandableListView eListView;
    private TextView tv_menu_title;

    private ArrayList<MenuGroup> groups;

    private String lsv_aim, parentCode = "01";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list_layout);
        mContext = this;
        sysUserInfo = new SysUserInfo(this);
        eListView = (ExpandableListView) findViewById(R.id.menu_listview);
        lsv_aim = getIntent().getStringExtra("aim");
        tv_menu_title = (TextView) findViewById(R.id.tv_menu_title);
        tv_menu_title.setText(getTitleTvText(lsv_aim) + "功能菜单("+sysUserInfo.getAccountSetName()+")");

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        btn_confirm = (Button) findViewById(R.id.btn_confirm);

        //获取数据
        groups = ininListViewData(lsv_aim);

        eListView.setGroupIndicator(null);//把menuListView的默认箭头去掉
        menuListViewAdapter = new ClickMenuListViewAdapter(MenuListActivity.this, groups, sysUserInfo);
        eListView.setAdapter(menuListViewAdapter);

        //默认全部展开
        int groupCount = eListView.getCount();
        for (int i = 0; i < groupCount; i++) {
            eListView.expandGroup(i);
        }


    }


    /**
     * 跳转到下一个扫描界面
     *
     * @param menucode 菜单编号
     */
    public void goToNextScanPage(String menucode) {
//        Log.i("main", "menucode----"+menucode);
        Intent intent = null;
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---产品入库--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //有单有明细入库
        if ("010101".equals(menucode)) {
            //先选验收单
            intent = new Intent(mContext, SelectPurcheck.class);
            intent.putExtra("aim", "P_Dv_InStock_Bill");
        }else if ("010102".equals(menucode)) {
            //无单入库
            //先选供应商，再选仓库
            intent = new Intent(mContext, SelectSupplier.class);
            intent.putExtra("aim", "P_Dv_InStock_NoBill");
        }else if ("010103".equals(menucode)) {
            //镜片有单有明细入库
//            intent = new Intent(mContext, CheckLensDegree.class);
            intent = new Intent(mContext, SelectPurcheck.class);
            intent.putExtra("aim", "P_Dv_InStock_Lens_Bill");
        }else if ("010104".equals(menucode)) {
            //镜架有单无明细入库
            //先选验收单
            intent = new Intent(mContext, SelectPurcheck.class);
            intent.putExtra("aim", "P_Dv_InStock_NoDetail_Bill");

        }else if ("010105".equals(menucode)) {
            //镜片无单入库

            intent = new Intent(mContext, SelectSupplier.class);
            intent.putExtra("aim", "P_Dv_Lens_InStock_NoBill");
        }
        else if ("010106".equals(menucode)) {
            //品牌码入库

            intent = new Intent(mContext, SelectBrandInStockBill.class);
            intent.putExtra("aim", "P_Dv_BrandCode_InStock");
        }else if ("010109".equals(menucode)){
            //无单盒标入库
//            先选供应商，再选仓库
            intent = new Intent(mContext, SelectSupplier.class);
            intent.putExtra("aim", "Holyes_Dv_Box_InStock_NoBill");
        }
        else if ("010110".equals(menucode)){
            //品牌商装盒入库
            intent = new Intent(mContext, SelectSupplier.class);
            intent.putExtra("aim", "P_Dv_InStock_PackBox_List_NoBill");
        }

        //有单入库撤销
        else if ("010201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_Bill_Cancel.class);
        }
        //无单入库撤销
        else if ("010202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_NoBill_Cancel.class);
        }
        //镜片有单入库撤销
        else if ("010203".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_Lens_Bill_Cancel.class);
        }
        //镜片无单入库撤销
        else if ("010204".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_InStock_Lens_NoBill_Cancel.class);
        }
//        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---入库退回--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//        //入库退回（有单）
//        else if ("020101".equals(menucode)) {
//            intent = new Intent(mContext, SelectPurcheck.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_G_Bill");
//        }
//        入库退回（无单）
        else if ("020102".equals(menucode)) {
            //选择供应商，再选择仓库
            //			intent = new Intent(mContext,SelectSupplier.class);
            //新版退回不需要选择
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_G_NoBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_G_NoBill");
        }
        //镜片入库退回（无单）
        else if ("020103".equals(menucode)) {
            //选择供应商，再选择仓库
            //			intent = new Intent(mContext,SelectSupplier.class);
            //新版退回不需要选择
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_G_NoBill.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Lens_Z_G_NoBill");
        }

//        //有单入库退回撤销
//        else if ("020201".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_G_Bill_Cancel.class);
//        }
//
//
        //入库退回撤销（无单）
        else if ("020202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel.class);
        }
        //镜片入库退回撤销（无单）
        else if ("020203".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_G_NoBill_Cancel.class);
        }
//        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---代销发货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //镜架有单有入库发货
        else if ("030101".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_BeInStock");
        }
        //镜架有单无入库发货
        else if ("030102".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_NoInStock");
        }
//        //镜架无单有入库发货
        else if ("030103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyRetailer.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_NoBill_BeInStock");
        }
//镜架无单无入库发货 030104

        //镜片有单有入库代销发货
        else if ("030105".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_Bill_BeInStock");
        }
//        else if ("030106".equals(menucode)){
//            //镜片代销有单无入库总店发货
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_Bill_NoInStock");
//        }
        //镜片无单有入库发货
        else if ("030107".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyRetailer.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_NoBill_BeInStock");
        }
//        //镜片代销无单无入库发货
//        else if ("030108".equals(menucode)) {
//            intent = new Intent(mContext, SelectCompanyRetailer.class);
//            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_NoBill_NoInStock");//测试跳转到镜片无单无入库的界面
//        }
        else if("030109".equals(menucode)){
            //有单直发零售商
            intent = new Intent(mContext, SelectAllPeiBill.class);
            intent.putExtra("aim", "OutStock_Z_D_L_BillBeInStock");
        }
        //有单代销发货撤销
        else if ("030201".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_D_Bill_Cancel.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_D_Bill_Cancel");
        }
//
        //无单代销发货撤消
        else if ("030202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_D_NoBill_Cancel.class);
        }
        //镜片有单代销发货撤销
        else if ("030203".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_D_Bill_Cancel.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_Bill_Cancel");
        }
        //镜片无单发货撤销
        else if ("030204".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_D_NoBill_Cancel.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_D_NoBill_Cancel");
        }
//
//
//        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---代销退货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
//        //有单无明细代销退货
//        else if ("040101".equals(menucode)) {
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill");
//        }
//        //有单有明细代销退货
//        else if ("040102".equals(menucode)) {
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill_Detail");
//        }
//
        //镜架无单退货
        else if ("040102".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyRetailer.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_NoBill");
        }
        //镜片无单退货
        else if ("040104".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyRetailer.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Lens_Z_D_NoBill");
        }
//        //代销退货补标
//        else if ("040104".equals(menucode)) {
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_ReturnedMendLabel_Z_D_HaveNoBill");
//        }
//        //有单无明细代销退货撤销
//        else if ("040201".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_Bill_Cancel.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill_Cancel");
//        }
//
//
//        //有单有明细代销退货撤销
//        else if ("040202".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_Bill_Detail_Cancel.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_D_Bill_Detail_Cancel");
//        }
        //镜架无单退货撤消
        else if ("040202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_D_NoBill_Cancel.class);
        }
        //镜片无单退货撤消
        else if ("040204".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_D_NoBill_Cancel.class);
        }

//
//        //代销补标撤消
//        else if ("040204".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedMendLabel_HaveNoBill_Cancel.class);
//            intent.putExtra("aim", "040204");
//        }
//
//
//        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---分店发货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
        //分店有入库有单有明细发货
        else if ("050101".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_Bill_BeInStock");
        }
        //分店有入库有单无明细发货
        else if ("050102".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_Bill_BeInStock_NoDetail");
        }
//        //分店有入库无单发货>>>>不选仓库了2018-04-25
        else if ("050103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_BeInStock");
        }
        //镜片有单有入库分店发货
        else if ("050104".equals(menucode)) {
            intent = new Intent(mContext, SelectPeiBill.class);

            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_L_Bill_BeInStock");
        }
        //镜片无单有入库分店发货
        else if ("050105".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock");
        }
        else if ("050106".equals(menucode)) {
            //镜片绑码
            intent = new Intent(mContext, ReplaceLabCode.class);
            intent.putExtra("aim", "ReplaceLabCode");
        }else if("050107".equals(menucode)){
            //镜片出库绑码、按出库单
            intent = new Intent(mContext, SelectLensBill.class);
            intent.putExtra("aim", "SalesReplaceLabCode");
        }
        else if("050108".equals(menucode)){
            //镜架无单无入库分店发货
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_NoInStock");
        }
        else if("050109".equals(menucode)){
            //镜架有单无入库分店发货
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_Bill_NoInStock");
        }else if ("050110".equals(menucode)){
            intent = new Intent(mContext, SunLensReplaceLabCode.class);
            intent.putExtra("aim", "SunLensReplaceLabCode");
        }else if("050113".equals(menucode)){
            //镜片有单无入库分店发货
            intent = new Intent(mContext, SelectPeiBill.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_L_Bill_NoInStock");
        }else if("050114".equals(menucode)){
            //镜片无单无入库分店发货
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_OutStock_Lens_Z_L_NoBill_NoInStock");
        }

//        else if ("050106".equals(menucode)){
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_OutStock_Z_L_S_HaveBill_BeInStock");
//        }
        else if ("050201".equals(menucode)) {
            //有单分店发货撤销
            intent = new Intent(mContext, P_Dv_OutStock_Z_L_Bill_Cancel.class);
        }
//
//        //无单分店撤销发货（无单）
        else if ("050202".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_OutStock_Z_L_NoBill_Cancel.class);
            intent.putExtra("aim", "P_Dv_OutStock_Z_L_NoBill_Cancel");
        }
        else if ("050203".equals(menucode)) {
            //镜片有单分店发货撤销
            intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_Bill_Cancel.class);
        }else if ("050204".equals(menucode)) {
//            镜片无单分店发货撤销
            intent = new Intent(mContext, P_Dv_OutStock_Lens_Z_L_NoBill_Cancel.class);
        }
//
//        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---直销退货--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
//
//        //直营有单无明细退货
//        else if ("060101".equals(menucode)) {
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill");
//        }
//        //有单有明细直销退货
//        else if ("060102".equals(menucode)) {
//            intent = new Intent(mContext, SelectPeiBill.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill_Detail");
//        }
//
        //无单分店选客退货
        else if ("060103".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_NoBill");
        }
//
        //无单分店扫描退货
        else if ("060104".equals(menucode)) {
            intent = new Intent(mContext, SelectStock.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_D_L_NoBill_NoCust");
        }

        //镜片无单选客退货
        else if ("060105".equals(menucode)) {
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Lens_Z_L_NoBill");
        }
//
        //镜片无单扫描退货
        else if ("060106".equals(menucode)) {
            intent = new Intent(mContext, SelectStock.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Lens_D_L_NoBill_NoCust");
        }
//
//
//        //直营有单无明细退货撤销
//        else if ("060201".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_Bill_Cancel.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill_Cancel");
//        }
//        //直营有单有明细退货撤销
//        else if ("060202".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_Bill_Detail_Cancel.class);
//            intent.putExtra("aim", "P_Dv_ReturnedPurchase_Z_L_Bill_Detail_Cancel");
//        }
//
        //无单分店退货撤销
        else if ("060203".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Z_L_NoBill_Cancel.class);
        }
        //镜片无单分店退货撤销
        else if ("060204".equals(menucode)) {
            intent = new Intent(mContext, P_Dv_ReturnedPurchase_Lens_Z_L_NoBill_Cancel.class);
        }
//
//        //直销补标撤消
//        else if ("060204".equals(menucode)) {
//            intent = new Intent(mContext, P_Dv_ReturnedMendLabel_HaveNoBill_Cancel.class);
//            intent.putExtra("aim", "060204");
//        }
        //SCS CCS其他扫描
        else if ("070101".equals(menucode)){
            //镜架有单调拨
            intent = new Intent(mContext, SelectAllots.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeStock_Bill");
        }
        else if ("070102".equals(menucode)){
            //镜架无单调拨
            intent = new Intent(mContext, SelectOutStock.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeStock_NoBill");
        }
        else if ("070103".equals(menucode)){
            //仓库补标
            intent = new Intent(mContext, SelectSupplier.class);
            intent.putExtra("aim", "P_Dv_MendLable_Z");

        }
        else if ("070104".equals(menucode)){
            //退货直通车
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing");
        } else if ("070105".equals(menucode)||"070107".equals(menucode)){
            //补打盒标
            intent = new Intent(mContext, P_Dv_InStock_PackBox_Search.class);
            intent.putExtra("PageType", "othen");
        }else if ("070108".equals(menucode)){
            //定制发货
            intent = new Intent(mContext, SelectAllPeiBill.class);
            intent.putExtra("aim", "SelectAllPeiBill");;
        }else  if ("070109".equals(menucode)){
            //产品换标
            intent = new Intent(mContext, P_Dv_InStock_Z_ChangeCode.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeCode");
        } else  if ("070110".equals(menucode)){
            //吊牌回收
            intent = new Intent(mContext, P_Dv_RecoverScan_Z.class);
            intent.putExtra("aim", "P_Dv_RecoverScan_Z");
        }else if ("070115".equals(menucode)){
            //镜架入库换型号
            intent = new Intent(mContext, P_Dv_InStock_Z_ChangeProduct.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_ChangeProduct");
        } else if ("070116".equals(menucode)){
            //镜架退货直通车
            intent = new Intent(mContext, SelectCompanyRetailer.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_L_D_Z_Frame");
        }
        else if ("070117".equals(menucode)){
            //镜片退货直通车
            intent = new Intent(mContext, SelectCompanyRetailer.class);
            intent.putExtra("aim", "P_Dv_ReturnedPurchase_L_D_Z_Lens");
        }
        else if ("070119".equals(menucode)){
            //无单镜片调拨
            intent = new Intent(mContext, SelectOutStock.class);
            intent.putExtra("aim", "P_Dv_InStock_Z_Lens_ChangeStock_NoBill");
        }
        else if ("070201".equals(menucode)){
            //CCS换货补扫
            intent = new Intent(mContext, SelectStock.class);
            intent.putExtra("aim", "P_Dv_CCSBarcodeStatusWrite");
        }else if ("070120".equals(menucode)){
            //镜架跨店退货
            intent = new Intent(mContext, SelectCompanyStoreInfor.class);
            intent.putExtra("aim", "ReturnedPurchase_D_L_NoBill_SameCust");
        }

        if (intent != null) {
            startActivity(intent);
        }

    }


    /**
     * 获取适配菜单列表的数据
     *
     * @param aim
     * @return
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-14 下午5:20:15
     */
    public ArrayList<MenuGroup> ininListViewData(String aim) {

        /**存放MenuGroup数据的集合*/
        ArrayList<MenuGroup> groupDatas = new ArrayList<MenuGroup>();
        MenuGroup menuGroup;//选项头
        MenuChild child;//子选项
        boolean isChildChecked = false;//子选项是否已经勾选
        List<Map<String, Object>> oneGroupMenuList;//一个主菜单下的子菜单集合
        //菜单小标题数据
        String menuNameArray[] = SomeUtils.mapListToStringArray(getMenuTitle(lsv_aim), "menuname");
        String menuCodeArray[] = SomeUtils.mapListToStringArray(getMenuTitle(lsv_aim), "menucode");
        String parentCode;
        //下面的是子选项数据
        for (int i = 0; i < menuNameArray.length; i++) {
            try {
                parentCode = menuCodeArray[i];
                menuGroup = new MenuGroup(parentCode, menuNameArray[i], false);
                oneGroupMenuList = SqliteDataHelper.getHelper(getApplicationContext()).QueryDbList("select  menucode,menuname,procedurename from menus where parentcode " +
                        " = '" + parentCode + "' and showstatus = '1' ", null);
                for (Map<String, Object> map : oneGroupMenuList) {
                    child = new MenuChild(map.get("menucode").toString(), map.get("menuname").toString(), isChildChecked);
                    menuGroup.addChildrenItem(child);
                }
                groupDatas.add(menuGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //下载的数据可能顺序不对，可能导致菜单位置不对，先排序一下
        Collections.sort(groupDatas, new SortMenuListComparator());
        return groupDatas;

    }

    /**
     * 获取小标题的内容
     *
     * @param aim
     * @return
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-14 上午11:34:53
     */
    public List<Map<String, Object>> getMenuTitle(String aim) {


        String sql = "select menuname,menucode from menus where parentcode = '" + parentCode + "' and showstatus ='1'";
        List<Map<String, Object>> list = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);

        return list;
    }

    public String getTitleTvText(String aim) {
        String title = "";
        if ("instock_in".equals(aim)) {
            title = "产品入库";
            parentCode = "01";

        } else if ("instock_back".equals(aim)) {
            title = "入库退回";
            parentCode = "02";
        } else if ("sendgoods_d".equals(aim)) {
            if (sysUserInfo.getLoginType().equals("CCS")){
                title = "代销发货";
            }else{
                title = "代销发货";
            }
            parentCode = "03";
        } else if ("backgoods_d".equals(aim)) {

            if (sysUserInfo.getLoginType().equals("CCS")){
                title = "代销退货";
            }else{
                title = "代销退货";
            }
            parentCode = "04";

        } else if ("sendgoods_zy".equals(aim)) {
            title = "分店发货";
            parentCode = "05";

        } else if ("backgoods_zy".equals(aim)) {
            title = "分店退货";
            parentCode = "06";
        } else if ("otherscan".equals(aim)) {
            title = "其他扫描";
            parentCode = "07";
        }

        return title;
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



