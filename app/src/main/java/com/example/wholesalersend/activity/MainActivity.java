package com.example.wholesalersend.activity;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.ccsother.CCSOtherMenuListActivity;
import com.example.wholesalersend.activity.other.OtherMenuListActivity;
import com.example.wholesalersend.adapter.AccountSetAdapter;
import com.example.wholesalersend.entity.AccountSet;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.entity.ScanApiResponse;
import com.example.wholesalersend.entity.User;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.myhandler.BaseHandler;
import com.example.wholesalersend.myhandler.UserLoginHandler;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @ClassName: MainActivity
 * @Description: 主界面
 * @Author: lijin
 * @Date: 2023/10/24 13:49
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private Context mContext;

    private AccessWeb accWeb;

    private TextView tv_brand;
    private Button btn_instock_in, btn_instock_back, btn_sendgoods_zy, btn_backgoods_zy,
            btn_sendgoods_d, btn_backgoods_d, btn_product_logist, btn_otherscan, btn_set, btn_update, btn_function;
    private ImageView ivNet;

    private TextView tv_update;

    private String parentCode = "01";

    private Handler hand;//报错提示

    private boolean isGoToNext = false;

    private SysUserInfo sysUserInfo;

    private List<AccountSet> accounrsetList;

    String Loginresult = "";


    private AccountSet accountSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        sysUserInfo = new SysUserInfo(getApplicationContext());
        accWeb=new AccessWeb(getApplicationContext());
        hand = new handShowMsg();

        tv_brand=findViewById(R.id.texBrand);

        btn_instock_in = (Button) findViewById(R.id.btn_instock_in);
        btn_instock_back = (Button) findViewById(R.id.btn_instock_back);
        btn_sendgoods_zy = (Button) findViewById(R.id.btn_sendgoods_zy);
        btn_backgoods_zy = (Button) findViewById(R.id.btn_backgoods_zy);
        btn_sendgoods_d = (Button) findViewById(R.id.btn_sendgoods_d);
        btn_backgoods_d = (Button) findViewById(R.id.btn_backgoods_d);
        btn_product_logist = (Button) findViewById(R.id.btn_product_logist);
        btn_otherscan = (Button) findViewById(R.id.btn_otherscan);
        btn_set = (Button) findViewById(R.id.btn_set);
        btn_update = (Button) findViewById(R.id.btn_update);
        btn_function = (Button) findViewById(R.id.btn_function);
        ivNet = (ImageView) findViewById(R.id.imageView1);

        if (sysUserInfo.getLoginType().equals("CCS")){
            btn_sendgoods_d.setBackgroundResource(R.drawable.btn_sendgoods_d_background);//代销发货
            btn_backgoods_d.setBackgroundResource(R.drawable.btn_backgoods_d_background);//代销退货
        }else{
            btn_sendgoods_d.setBackgroundResource(R.drawable.btn_new_sendgoods_d_background);//总店发货
            btn_backgoods_d.setBackgroundResource(R.drawable.btn_new_backgoods_d_background);//总店退货
        }

        tv_update=findViewById(R.id.tv_update);

        tv_brand.setText(sysUserInfo.getAccountSetName());

        btn_instock_in.setOnClickListener(this);
        btn_instock_back.setOnClickListener(this);
        btn_sendgoods_zy.setOnClickListener(this);
        btn_backgoods_zy.setOnClickListener(this);
        btn_sendgoods_d.setOnClickListener(this);
        btn_backgoods_d.setOnClickListener(this);
        btn_product_logist.setOnClickListener(this);
        btn_otherscan.setOnClickListener(this);
        btn_set.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_function.setOnClickListener(this);
        tv_update.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, MenuListActivity.class);
        switch (view.getId()) {
            case R.id.btn_instock_in:
                intent.putExtra("aim", "instock_in");
                parentCode = "01";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_instock_back:
                intent.putExtra("aim", "instock_back");
                parentCode = "02";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_sendgoods_d:
                if (sysUserInfo.getLoginType().equals("CCS")) {
                    intent.putExtra("aim", "sendgoods_d");
                    parentCode = "03";
                    isGoToNext = isHasFunctions(parentCode);
                } else{
                    ShowMessage.Show(mContext,"请前往分店发货菜单进行发货操作");
                }

//                isGoToNext=false;
//                Toast.makeText(mContext,"请前往分店发货菜单进行发货操作",Toast.LENGTH_SHORT).show();
//                ShowMessage.Show(mContext,"请前往分店发货菜单进行发货操作");
                break;
            case R.id.btn_backgoods_d:
                if (sysUserInfo.getLoginType().equals("CCS")) {
                    intent.putExtra("aim", "backgoods_d");
                    parentCode = "04";
                    isGoToNext = isHasFunctions(parentCode);
                } else{
                    ShowMessage.Show(mContext,"请前往分店发货菜单进行发货操作");
                }
//                isGoToNext=false;
//                Toast.makeText(mContext,"请前往分店退货菜单进行退货操作",Toast.LENGTH_SHORT).show();
//                ShowMessage.Show(mContext,"请前往分店退货菜单进行退货操作");
                break;
            case R.id.btn_sendgoods_zy:
                intent.putExtra("aim", "sendgoods_zy");
                parentCode = "05";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_backgoods_zy:
                intent.putExtra("aim", "backgoods_zy");
                parentCode = "06";
                isGoToNext = isHasFunctions(parentCode);
                break;

            case R.id.btn_otherscan:
//                intent = new Intent(mContext, OtherMenuListActivity.class);
                intent.putExtra("aim", "otherscan");
                parentCode = "07";
                isGoToNext = isHasFunctions(parentCode);
                break;
            case R.id.btn_product_logist:
                //产品物流查询直接跳转
                intent = new Intent(mContext, P_ProductLogist.class);
                parentCode = "08";
                isGoToNext = isHasFunctions(parentCode);

//                isGoToNext = true;
                break;
            case R.id.btn_set:
                SomeUtils.goToSetting(mContext);
                return;
            case R.id.btn_function:
//			intent = new Intent(mContext,CheckingFunctionActivity.class);
//                isGoToNext = false;//暂时不允许用户勾选菜单功能
                DownLoadDataThread();
                return;
            case R.id.btn_update:
                //CCS功能
//                intent = new Intent(mContext, CCSOtherMenuListActivity.class);
//                intent.putExtra("aim", "ccsotherscan");
//                parentCode = "09";
//                isGoToNext = isHasFunctions(parentCode);
//                updateApp();
                break;
//            case R.id.tv_update:
//                //CCS功能
//                intent = new Intent(mContext, CCSOtherMenuListActivity.class);
//                intent.putExtra("aim", "ccsotherscan");
//                parentCode = "09";
//                isGoToNext = isHasFunctions(parentCode);
//                break;
            default:
                break;
        }

        if (isGoToNext) {
            startActivity(intent);
        } else {
            SomeUtils.showToask(mContext, "抱歉，该功能暂未给您开通！");
        }
    }


    // 请求服务
    private void DownLoadDataThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        accounrsetList=new ArrayList<AccountSet>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    accounrsetList = accWeb.GetAccountSet();
//                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
//                } catch (Exception e) {
//                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
//                }
                try {
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();
                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AccountSet/GetUserAccountSet"+ "?UserCode="+sysUserInfo.getUserCode()+"&EnableStatus=";
//                    Log.d("main", requestUrl);
                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
//                    Log.d("main", result);

                    SalesScsWebApiInfo scsWebApiInfo=gson.fromJson(result,SalesScsWebApiInfo.class);
                    if(scsWebApiInfo.isSuccess()) { // 假设有isSuccess()方法

//                        JSONArray jsonArray = new JSONArray(scsWebApiInfo.getData());
//                        accounrsetList = new ArrayList<AccountSet>();
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject jsonObject2 = (JSONObject) jsonArray.opt(i);
//                            AccountSet accountSet = new AccountSet();
//                            accountSet.setAccountSetName(jsonObject2.optString("accountSetName"));
//                            accountSet.setAccountSetId(jsonObject2.optString("accountSetId"));
//                            accountSet.setDataBaseName(jsonObject2.optString("dataBaseName"));
//                            accounrsetList.add(accountSet);
//                        }

                        accounrsetList = new ArrayList<>();
                        JsonElement dataEl = gson.toJsonTree(scsWebApiInfo.getData());
                        if (dataEl != null && dataEl.isJsonArray()) {
                            JsonArray jsonArray = dataEl.getAsJsonArray();
                            for (JsonElement itemEl : jsonArray) {
                                JsonObject obj = itemEl.getAsJsonObject();
                                AccountSet accountSet = new AccountSet();
                                accountSet.setAccountSetName(obj.has("accountSetName") && !obj.get("accountSetName").isJsonNull()
                                        ? obj.get("accountSetName").getAsString() : "");
                                accountSet.setAccountSetId(obj.has("accountSetId") && !obj.get("accountSetId").isJsonNull()
                                        ? obj.get("accountSetId").getAsString() : "");
                                accountSet.setDataBaseName(obj.has("dataBaseName") && !obj.get("dataBaseName").isJsonNull()
                                        ? obj.get("dataBaseName").getAsString() : "");
                                accounrsetList.add(accountSet);
                            }
                        }

                        ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                    }else{
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                            e.getMessage());
                }

            }
        });
        sendCode.start();
    }


    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    MyProgressDialog.close();
                    ShowMessage.Show(mContext, msg.obj.toString());
                    break;

                case ShowMessage.HandSuccess: //下载账套列表成功
                    MyProgressDialog.close();
                    if (accounrsetList.size()>0) {
                        ShowAccountSet(accounrsetList);
                    }else{
                        Toast.makeText(mContext,"账套暂无数据",Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 5: //切换账套成功
                    MyProgressDialog.close();
//                    ParseData_login(Loginresult);
                    //每次切换账套成功就要清空菜单，重新下载
                    try {
                        String sql = "delete from menus";
                        SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    DownLoadMenusThread();//下载菜单
                    Toast.makeText(mContext, "切换账套成功", Toast.LENGTH_SHORT).show();
                    sysUserInfo.setAccountSetId(accountSet.getAccountSetId());
                    sysUserInfo.setAccountSetName(accountSet.getAccountSetName());
                    tv_brand.setText(sysUserInfo.getAccountSetName());//显示当前登录的账套名称

                    break;
                case 8:
                    MyProgressDialog.close();
                    Toast.makeText(mContext, "下载菜单成功", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }

    //下载菜单
    private void DownLoadMenusThread() {
//        MyProgressDialog.show(mContext, "正在下载菜单...", true, false);
//        list = new ArrayList<Map<String, Object>>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MyRequest request = new MyRequest();
                    Gson gson=new Gson();
                    //请求的域名地址GET
                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetAndroidDvMenu"+ "?showModel=2&PriAccountId=";
//                            +sysUserInfo.getAccountSetId();
                    String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
                    SalesScsWebApiInfo scsWebApiInfo=gson.fromJson(result,SalesScsWebApiInfo.class);
                    if(scsWebApiInfo.isSuccess()) { // 假设有isSuccess()方法
                        if (scsWebApiInfo.getData().size() == 0) {
                            ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                                    "菜单下载失败。");
                        }
//                        Log.d("main-", scsWebApiInfo.getData().toString());
                        // 提取并合并所有菜单（含顶层）及其子菜单（含父级信息）
                        List<Map<String, Object>> allMenusWithParent = extractAllMenusWithParentInfo(scsWebApiInfo.getData(), DEFAULT_CHILDREN_KEY);

                        List<String> sqlList = new ArrayList<String>();
                        String menucode, menuname, parentcode,  procedurename, sql = "";
                        boolean showstatus;
                        for (Map<String, Object> map : allMenusWithParent) {
                            menucode = map.get("menuCode").toString();
                            menuname = map.get("menuName").toString();
                            parentcode = map.get("parentCode").toString();
                            showstatus = (boolean) map.get("showStatus");
                            String statusNum="0";
                            if (showstatus){
                                statusNum="1";
                            }
//                    procedurename = map.get("children").toString();
                            sql = String.format("insert into menus(menucode,menuname,parentcode,showstatus,procedurename) values " +
                                    "('" + menucode + "','" + menuname + "','" + parentcode + "','" + statusNum + "','')");
//                            Log.d("main-", sql);
                            sqlList.add(sql);
                        }
                        //把菜单批量插入数据库
                        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);
                        ShowMessage.ShowMsg(hand, 8,scsWebApiInfo.getMessage());
                    }else{
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,
                            e.getMessage());
                }
            }
        }).start();
    }



    // 默认的 children 字段名（可根据实际需求修改）
    private static final String DEFAULT_CHILDREN_KEY = "children";
    // 新增字段名（记录父级菜单信息）
    private static final String PARENT_MENU_CODE = "parentMenuCode";
    private static final String PARENT_MENU_NAME = "parentMenuName";

    public static List<Map<String, Object>> extractAllMenusWithParentInfo(
            List<Map<String, Object>> menus,
            String childrenKey) {

        // 参数校验
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用队列进行迭代遍历（元素为：当前菜单 + 父级菜单数据）
        Queue<Map.Entry<Map<String, Object>, Map<String, Object>>> queue = new LinkedList<>();
        // 初始时，顶层菜单的父级为 null
        for (Map<String, Object> menu : menus) {
            queue.offer(new AbstractMap.SimpleEntry<>(menu, null));
        }

        List<Map<String, Object>> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            Map.Entry<Map<String, Object>, Map<String, Object>> entry = queue.poll();
            Map<String, Object> currentMenu = entry.getKey();
            Map<String, Object> parentMenu = entry.getValue(); // 父级菜单数据（可能为 null）

            // 复制当前菜单并附加父级信息
            Map<String, Object> copiedCurrent = new HashMap<>(currentMenu);
            if (parentMenu != null) {
                copiedCurrent.put(PARENT_MENU_CODE, parentMenu.get("menuCode"));
                copiedCurrent.put(PARENT_MENU_NAME, parentMenu.get("menuName"));
            } else {
                // 顶层菜单的父级信息设为 null 或空
                copiedCurrent.put(PARENT_MENU_CODE, null);
                copiedCurrent.put(PARENT_MENU_NAME, null);
            }
            // 将当前菜单添加到结果列表
            result.add(copiedCurrent);

            // 使用兼容方法提取 children
            List<Map<String, Object>> children = getChildrenCompat(currentMenu, childrenKey);
            if (children != null && !children.isEmpty()) {
                // 遍历每个子菜单，附加父级信息并加入队列
                for (Map<String, Object> child : children) {
                    // 复制子菜单（避免修改原始数据）
                    Map<String, Object> copiedChild = new HashMap<>(child);
                    // 附加父级菜单信息（当前菜单的 menuCode 和 menuName）
                    copiedChild.put(PARENT_MENU_CODE, currentMenu.get("menuCode"));
                    copiedChild.put(PARENT_MENU_NAME, currentMenu.get("menuName"));
                    // 将处理后的子菜单加入队列（继续处理下一层）
                    queue.offer(new AbstractMap.SimpleEntry<>(copiedChild, currentMenu));
                }
            }
        }

        return result;
    }
    /**
     * 安全提取 children（带类型校验）
     * @param menu 当前菜单对象（Map 格式）
     * @param childrenKey children 字段名
     * @return 包含 children 的 List（若类型不合法则返回 null）
     */
    private static List<Map<String, Object>> getChildrenCompat(
            Map<String, Object> menu,
            String childrenKey) {

        if (menu == null || !menu.containsKey(childrenKey)) {
            return null;
        }

        Object childrenObj = menu.get(childrenKey);
        if (!(childrenObj instanceof List)) {
            return null; // 非 List 类型，跳过
        }

        List<?> childrenList = (List<?>) childrenObj;

        // 手动检查所有元素是否为 Map 类型（替代 Stream API）
        for (Object element : childrenList) {
            if (!(element instanceof Map)) {
                return null; // 发现非 Map 元素，返回 null
            }
        }

        // 安全转换为 List<Map<String, Object>>
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typedChildren = (List<Map<String, Object>>) childrenList;
        return typedChildren;
    }


    private void ShowAccountSet(final List<AccountSet> accountSetList){

        AlertDialog alertDialog6 = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setTitle("选择要切换的账套")
                .setIcon(R.drawable.scs)
                .setAdapter(new AccountSetAdapter(mContext, accountSetList), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        accountSet=accountSetList.get(i);
//                        userlogin();
                        NewSwitchAccount(accountSet.getAccountSetId());
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
//                        Toast.makeText(mContext,"监听到取消按钮",Toast.LENGTH_SHORT).show();
                        accountSetList.clear();
                    }
                }).create();
        alertDialog6.show();
    }

    //新版登录接口
    private void NewSwitchAccount(String tPriAccountId) {
        MyProgressDialog.show(mContext, "正在切换账套...", true, false);
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                // 使用OkHttp库发送POST请求
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new MultipartBody.Builder()
                        .setType(MediaType.parse("multipart/form-data"))
                        .addFormDataPart("PriAccountId", tPriAccountId) //账号
                        .build();
                Map<String, Object> requestParams=new HashMap<>();
                requestParams.put("PriAccountId", tPriAccountId);


                Map<String, Object> signedParams = SomeUtils.GetSignParams(requestParams);
                // 将签名参数拼接到URL
                String ObGoodsUrl = "http://" + sysUserInfo.getServerip() + ":9521/" + sysUserInfo.getAPIEndpoint() + "/Login/SwitchAccount";
                String signedUrl = SomeUtils.appendParamsToUrl(ObGoodsUrl, signedParams);

                Request request = new Request.Builder().url(signedUrl).addHeader("Authorization", "Bearer " + sysUserInfo.getLoginid()).post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 处理网络请求失败
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "服务器："+e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String LoginData=response.body().string();
//                            Log.d("main", LoginData);
                        Gson gson=new Gson();
                        ScanApiResponse scsWebApiInfo=gson.fromJson(LoginData,ScanApiResponse.class);
                        if (scsWebApiInfo.isSuccess()==true){
                            if (scsWebApiInfo.getData()!=null){
//                                    Log.d("main", scsWebApiInfo.getData().toString());
                                try {
                                    JSONObject rootObject = new JSONObject(LoginData);
                                    JSONObject dataObject = rootObject.getJSONObject("data");

                                    sysUserInfo.setLoginid(dataObject.optString("userLoginId", ""));
                                    sysUserInfo.setUsername(dataObject.optString("userName", ""));
                                    sysUserInfo.setStock(dataObject.optString("userDept", ""));
                                    sysUserInfo.setMobile(dataObject.optString("userMobile", ""));
                                    sysUserInfo.setLoginToken(dataObject.optString("token", ""));
                                    sysUserInfo.setLastLoginServerIp(sysUserInfo.getServerip());
                                    sysUserInfo.setAccountSetId(dataObject.optString("priAccountId", ""));
                                    sysUserInfo.setAccountSetName(dataObject.optString("accountSetName", ""));
                                    sysUserInfo.setUserCode(dataObject.optString("userCode", ""));
                                    ShowMessage.ShowMsg(hand, 5,"ok");
                                } catch (JSONException e) {
                                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "解析登录返回格式报错: " + e.getMessage());
                                }
                            }
                        }else {
                            ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"服务器："+ scsWebApiInfo.getMessage());
                        }
                    }
                });

            }
        }).start();
    }


    //登录接口请求
    private void userlogin(){
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Loginresult = accWeb.UserLogin(sysUserInfo.getMobile(), sysUserInfo.getLoginpwd(),accountSet.getAccountSetId());

                    ShowMessage.ShowMsg(hand, 5, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"登录出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    //解析登录返回的数据
    public void ParseData_login(String data) {

        BaseHandler handler = new UserLoginHandler();
        handler.parse(data);
        Vector<?> users = (Vector<?>) BaseHandler.hash.get("users");// hash很重要，所有的东西都在hash中
        User user = (User) users.elementAt(0);
        if (Boolean.parseBoolean(user.getP00()) == false) {
            // 显示错误信息
            Toast.makeText(mContext, user.getP01(), Toast.LENGTH_SHORT).show();
        }
        if (Boolean.parseBoolean(user.getP00()) == true) {
            //Log.d("main",user.toString());
            sysUserInfo.setUserid(user.getP01());
            sysUserInfo.setLoginid(user.getP02());
            sysUserInfo.setUsername(user.getP03());
        }
    }

    /**
     * 菜单功能下面是否还有功能
     */
    public boolean isHasFunctions(String parentCode) {
        String sql = "select * from menus where parentcode ='" + parentCode + "' and showstatus = '1' ";
        List<Map<String, Object>> functionList = SqliteDataHelper.getHelper(mContext).QueryDbList(sql, null);
        if (functionList.size() > 0) {
            return true;
        } else {
            return false;
        }

    }
}
