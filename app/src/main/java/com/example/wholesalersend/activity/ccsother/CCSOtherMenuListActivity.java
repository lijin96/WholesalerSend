package com.example.wholesalersend.activity.ccsother;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.SelectAllots;
import com.example.wholesalersend.activity.select.SelectOutStock;
import com.example.wholesalersend.activity.select.SelectStock;
import com.example.wholesalersend.activity.select.SelectSupplier;
import com.example.wholesalersend.adapter.OtherMenuListAdapter;
import com.example.wholesalersend.entity.MenuChild;
import com.example.wholesalersend.entity.MenuGroup;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.SysUserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CCSOtherMenuListActivity
 * @Description: CCS其他菜单
 * @Author: lijin
 * @Date: 2025/3/10 11:30
 */
public class CCSOtherMenuListActivity extends Activity {


    private Context mContext;
    private SysUserInfo sysUserInfo;
    private OtherMenuListAdapter menuListViewAdapter;

    private Button btn_back, btn_confirm;
    private ListView eListView;
    private TextView tv_menu_title;

    private ArrayList<MenuGroup> groups;

    private String lsv_aim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.setDefaultDisplay(this);
        setContentView(R.layout.activity_other_menu_list);
        mContext = this;
        sysUserInfo = new SysUserInfo(this);
        eListView = (ListView) findViewById(R.id.menu_listview);
        tv_menu_title = (TextView) findViewById(R.id.tv_menu_title);
        tv_menu_title.setText("CCS其他扫描功能菜单("+sysUserInfo.getAccountSetName()+")");
        lsv_aim = getIntent().getStringExtra("aim");
        //获取数据
        groups = ininListViewData(lsv_aim);
        menuListViewAdapter = new OtherMenuListAdapter(mContext, groups);
        eListView.setAdapter(menuListViewAdapter);
        eListView.setOnItemClickListener(new CCSOtherMenuListActivity.OnItemClick());
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        btn_confirm = (Button) findViewById(R.id.btn_confirm);

    }

    class OnItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            MenuGroup menuGroup = (MenuGroup) parent.getItemAtPosition(position);

            goToNextScanPage(menuGroup.getMenuCode());
        }

    }

    /**
     * 跳转到下一个扫描界面
     *
     * @param menucode 菜单编号
     */
    public void goToNextScanPage(String menucode) {
        Intent intent = null;
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---其他扫描--->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//        //CCS换货补标
        if ("0901".equals(menucode)) {
            intent = new Intent(mContext, SelectStock.class);
            intent.putExtra("aim", "P_Dv_CCSBarcodeStatusWrite");
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
        String title[] = getMenuTitle(aim);
        String parentCode;
        //下面的是子选项数据
        for (int i = 0; i < title.length; i++) {
            try {
                parentCode = SqliteDataHelper.getHelper(getApplicationContext()).execSQLString("select menucode from menus where menuname ='" + title[i] + "'");
                menuGroup = new MenuGroup(parentCode, title[i], false);
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
    public String[] getMenuTitle(String aim) {

        String parentCode = "09";
//        if ("otherscan".equals(aim)) {
//            //			title = new String[]{"仓库调拨","其他"};
//        }

        String sql = "select menuname,menucode from menus where parentcode = '" + parentCode + "' and showstatus ='1'";
        List<Map<String, String>> list = SqliteDataHelper.getHelper(mContext).exeselect(sql);
        String title[] = new String[list.size()];
        for (int i = 0; i < title.length; i++) {
            title[i] = list.get(i).get("menuname").toString();
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

