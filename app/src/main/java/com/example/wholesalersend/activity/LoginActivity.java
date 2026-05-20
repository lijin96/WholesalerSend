package com.example.wholesalersend.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.wholesalersend.R;
import com.example.wholesalersend.activity.select.SelectCompanyRetailer;
import com.example.wholesalersend.activity.select.SelectStock;
import com.example.wholesalersend.activity.select.SelectSureConfirm;
import com.example.wholesalersend.activity.sendgoods_zd.P_Dv_OutStock_Z_D_NoBill_BeInStock;
import com.example.wholesalersend.adapter.AccountSetAdapter;
import com.example.wholesalersend.adapter.ScanOrderAdapter;
import com.example.wholesalersend.entity.AccountSet;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.entity.ScanApiResponse;
import com.example.wholesalersend.entity.ScsWebApiInfo;
import com.example.wholesalersend.entity.User;
import com.example.wholesalersend.lib.ADevicesManager;
import com.example.wholesalersend.lib.AccessWeb;
import com.example.wholesalersend.lib.GetNetStateRunnable;
import com.example.wholesalersend.lib.Loading;
import com.example.wholesalersend.lib.MyRequest;
import com.example.wholesalersend.lib.MySound;
import com.example.wholesalersend.lib.SqliteDataHelper;
import com.example.wholesalersend.myhandler.BaseHandler;
import com.example.wholesalersend.myhandler.UserLoginHandler;
import com.example.wholesalersend.utils.DisplayUtil;
import com.example.wholesalersend.utils.MyProgressDialog;
import com.example.wholesalersend.utils.ShowMessage;
import com.example.wholesalersend.utils.SomeUtils;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
 * @ClassName: LoginActivity
 * @Description: 登录界面
 * @Author: lijin
 * @Date: 2023/10/23 13:44
 */
public class LoginActivity extends Activity {

    private Context mContext;
    private SysUserInfo sysUserInfo;
    private Loading loading = null;//加载框
    private Handler hand;//报错提示
    private Thread loginThread;//登录线程
    private GetNetStateRunnable GetNetRun;//定时网络监控 2.5秒查询一次

    private EditText et_username, et_password;//账号密码
    private TextView tv_versionCode, tv_brand;//版本号,品牌
    private CheckBox checkBox;//是否记住密码
    private LinearLayout lin_login, lin_setting, lin_update, lin_evaluate;//登录、设置、关于、反馈建议
    private TextView tv_login;//登录按钮
    private ImageView ivNet;//检测网络连接的接口

    private List<AccountSet> accounrsetList=new ArrayList<AccountSet>();

    volatile boolean isPaused = true;// 暂停标志

    boolean IsChooseAccount=false;

    private RadioGroup radio_group_type;
    private RadioButton rb_brand_mode,rb_agent_mode,rb_other_mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //用来判断当前Activity是否是第一个activity
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        //设置窗体始终点亮
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //设置软件文字界面大小不跟随系统设置而改变
        DisplayUtil.setDefaultDisplay(this);

        setContentView(R.layout.activity_login);

        mContext = this;
        sysUserInfo = new SysUserInfo(getApplicationContext());
        hand = new handShowMsg();

        MySound.getMySound(this);

        initView();


        // 使用示例
        Rect windowRect = getWindowDisplayArea(this);
//        Log.e("main", "窗口区域: " + windowRect.width() + "x" + windowRect.height());
//        Log.e("main", "位置: (" + windowRect.left + "," + windowRect.top + ")");
        if (windowRect.width()<=850){
            sysUserInfo.setOldVersion("T8");
        }else{
            sysUserInfo.setOldVersion("T9");
        }
    }
    /**
     * 获取窗口显示区域（排除系统UI）
     */
    public static Rect getWindowDisplayArea(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }



    private void initView(){
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        checkBox = (CheckBox) findViewById(R.id.logincheckBox);
        tv_versionCode = (TextView) findViewById(R.id.tv_versionCode);
        tv_brand = ((TextView) findViewById(R.id.texBrand));
        lin_login = (LinearLayout) findViewById(R.id.lin_login);
        tv_login = (TextView) findViewById(R.id.tv_login);
        lin_setting = (LinearLayout) findViewById(R.id.lin_setting);
        lin_update = (LinearLayout) findViewById(R.id.lin_update);
        ivNet = (ImageView) findViewById(R.id.imageView1);

        //记住密码
        if (sysUserInfo.getIfrember()) {
            checkBox.setChecked(sysUserInfo.getIfrember());
            et_username.setText(sysUserInfo.getMobile());
            SomeUtils.moveFocus(et_username);
            et_password.setText(sysUserInfo.getLoginpwd());
            SomeUtils.moveFocus(et_password);
        }
        //显示登录的账套
        tv_brand.setText(sysUserInfo.getAccountSetName());

        //判断默认的登录域名、ip和端口号
        String pServerip = sysUserInfo.getServerip();
        if (!pServerip.isEmpty()) {
            tv_login.setText("登录");
        } else {
            tv_login.setEnabled(false);
        }

        ADevicesManager.SetKey(false);

        // 登录
        lin_login.setOnClickListener(new BtnLoginClick());

        // 修改域名
        lin_setting.setOnClickListener(new BtnSettingClick());

        // 关于
        lin_update.setOnClickListener(new BtnUpdateClick());

        ivNet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SomeUtils.goToSetting(LoginActivity.this);
            }
        });
        tv_versionCode.setText(SomeUtils.getSoftVer(this));

    }

    private class BtnSettingClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(LoginActivity.this, PasswordActivity.class);
//            intent.putExtra("password", "29822832");
//            startActivityForResult(intent, 1);
//            Toast.makeText(mContext,"功能暂未开放",Toast.LENGTH_SHORT).show();
            ShowModifydomain();
        }
    }

    private class BtnUpdateClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, AboutActivity.class);
            startActivity(intent);
        }
    }



    private class BtnLoginClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // 更新webservice URL
            AccessWeb.getHelper(getApplicationContext()).updateurl();
            isPaused = false;

            if (et_username.getText().toString().isEmpty()) {
                Toast.makeText(LoginActivity.this, "请输入账号！", Toast.LENGTH_SHORT)
                        .show();
                et_password.findFocus();
                return;
            }
            if (et_password.getText().toString().isEmpty()) {

                Toast.makeText(LoginActivity.this, "请输入用户密码！", Toast.LENGTH_SHORT).show();
                tv_login.findFocus();
                return;
            }


            try {
                String sql = "delete from menus";
                SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }


//            loading = new Loading(LoginActivity.this, "正在下载...", new Loading.OnLoadingback() {
//                @Override
//                public void back(String name) {
//                    if (loginThread != null && loginThread.isAlive()) {
//                        isPaused = true;
//                        loginThread.interrupt();
//                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "登录被终止了");
//                        loginThread = null;//>>>
//                    }
//                }
//            });
//            loading.Show();
//
//            loginThread = new Thread(new LoginThread());
//            loginThread.start();
            NewUserLogin();
        }
    }





    /**
     * 登录线程
     */
    private class LoginThread implements Runnable {
        @Override
        public void run() {
            try {
//                showTip("创建数据库...");
                AccessWeb.getHelper(getApplicationContext()).updateurl();

                SqliteDataHelper.getHelper(getApplicationContext()).initDatabase();
                //				showTip("获取服务器时间...");
                //				checkSystemDatetime();
                showTip("验证用户身份...");
                if (isPaused)
                    return;
//                downloadMenus();

                if (!userlogin()) {
                    return;
                }
//                GetFirstDeliveryStatus();


                if (!isPaused) // 正常
                {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess, "success");
                } else // 线程被停止a
                {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "登录被终止了");
                }
            } catch (Exception e) {
                ShowMessage.ShowMsg(hand, "登录错误" + e.getMessage());
            }
        }
    }

    // 检查网络状态==============================
    private Thread netState;
    boolean bOnNetState = true;

    private void startCheckNetState() {
        GetNetRun = new GetNetStateRunnable(true, mContext);

        GetNetRun.SetCallBack(new GetNetStateRunnable.GetNetState() {

            @Override
            public void netState_on() {
                online();
            }

            @Override
            public void netState_off() {
                offline();
            }
        });
        netState = new Thread(GetNetRun);
        netState.start();
        GetNetRun.State = true;
    }

    private void stopCheckNetState() {
        //		Log.i("main", "stopCheckNetState");
        GetNetRun.setState(false);
        bOnNetState = false;
        if (netState != null && netState.isAlive()) {
            netState.interrupt();
        }
    }

    private void offline() {
        ShowMessage.ShowMsg(hand, 11, "");
    }

    private void online() {
        ShowMessage.ShowMsg(hand, 12, "");
    }

    //新版登录接口
    private void NewUserLogin() {
        MyProgressDialog.show(mContext, "正在登录...", true, false);
        try {
            AccessWeb.getHelper(getApplicationContext()).updateurl();
            SqliteDataHelper.getHelper(getApplicationContext()).initDatabase();

            String ed_username=et_username.getText().toString().trim();
            String ed_password=et_password.getText().toString().trim();

            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {

                    // 使用OkHttp库发送POST请求
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new MultipartBody.Builder()
                            .setType(MediaType.parse("multipart/form-data"))
                            .addFormDataPart("UserCode", ed_username) //账号
                            .addFormDataPart("UserPass", ed_password) //密码
                            .build();
                    Map<String, Object> requestParams=new HashMap<>();
                    requestParams.put("UserCode", ed_username);
                    requestParams.put("UserPass", ed_password);


                    Map<String, Object> signedParams = SomeUtils.GetSignParams(requestParams);
                    // 将签名参数拼接到URL
                    String ObGoodsUrl = "http://" + sysUserInfo.getServerip() + ":9521/" + sysUserInfo.getAPIEndpoint() + "/Login/Login";
                    String signedUrl = SomeUtils.appendParamsToUrl(ObGoodsUrl, signedParams);

                    Request request = new Request.Builder().url(signedUrl).post(body).build();
//.addHeader("Authorization", "Bearer " + sysUserInfo.getLoginid())
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // 处理网络请求失败
                            ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "服务器："+e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String LoginData=response.body().string();
                            Gson gson=new Gson();
                            ScanApiResponse scsWebApiInfo=gson.fromJson(LoginData,ScanApiResponse.class);
                            if (scsWebApiInfo.isSuccess()==true){
                                if (scsWebApiInfo.getData()!=null){
//                                    Log.d("main", scsWebApiInfo.getData().toString());
                                    try {
                                        JSONObject rootObject = new JSONObject(LoginData);
                                        JSONObject dataObject = rootObject.getJSONObject("data");
                                        sysUserInfo.setUserid(et_username.getText().toString().trim());
                                        sysUserInfo.setLoginid(dataObject.optString("userLoginId", ""));
                                        sysUserInfo.setUsername(dataObject.optString("userName", ""));
                                        sysUserInfo.setStock(dataObject.optString("userDept", ""));
                                        sysUserInfo.setMobile(dataObject.optString("userMobile", ""));
                                        sysUserInfo.setLoginToken(dataObject.optString("token", ""));
                                        sysUserInfo.setLoginpwd(et_password.getText().toString());
                                        sysUserInfo.setIfrember(checkBox.isChecked());
                                        sysUserInfo.setLastLoginServerIp(sysUserInfo.getServerip());
                                        sysUserInfo.setAccountSetId(dataObject.optString("priAccountId", ""));
                                        sysUserInfo.setAccountSetName(dataObject.optString("accountSetName", ""));
                                        sysUserInfo.setUserCode(dataObject.optString("userCode", ""));
                                        ShowMessage.ShowMsg(hand, ShowMessage.HandSuccess,"ok");
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
        } catch (Exception e) {
            ShowMessage.ShowMsg(hand, "登录错误" + e.getMessage());
        }
    }




    //登录接口请求
    private boolean userlogin() throws Exception {
        String result = "";
        String AccountSetId="";
//        if (checkBox.isChecked()){
        if (sysUserInfo.getMobile().equals(et_username.getText().toString().trim())){
            AccountSetId=sysUserInfo.getAccountSetId();//账套id
        }else{
            AccountSetId="";
            sysUserInfo.setAccountSetId("");
            sysUserInfo.setAccountSetName("");
        }
//        }

        result = AccessWeb.getHelper(getApplicationContext()).UserLogin(et_username.getText().toString().trim(), et_password.getText().toString(),AccountSetId);
        return ParseData_login(result);
//         return userloginV1(AccountSetId,et_username.getText().toString().trim(), et_password.getText().toString());
    }


    //登录接口请求
    private boolean userloginV1(String tPriAccountId,String tUserCode,String tUserPass) throws Exception {
        try {
            MyRequest request = new MyRequest();
            String data = "PriAccountId="+tPriAccountId+"&UserCode="+tUserCode+"&UserPass="+tUserPass;//POST请求的参数
//            Log.d("main", "loginid: "+sysUserInfo.getLoginid());
            String result = request.post("http://"+sysUserInfo.getServerip()+":9521/ScsWebApi/Login/Login", data,sysUserInfo.getLoginid(),"application/x-www-form-urlencoded");//调用我们写的post方法

//            Log.d("main", "run: "+result);

            Gson gson=new Gson();
            ScsWebApiInfo scsWebApiInfo=gson.fromJson(result,ScsWebApiInfo.class);
            if (scsWebApiInfo.isSuccess()==true){
                return true;
//                ShowMessage.ShowMsg(hand, ShowMessage.HandScanSuccess,"ok");
            }else{
                ShowMessage.ShowMsg(hand,scsWebApiInfo.getMessage());
                return false;
            }

        } catch (Exception e) {

            ShowMessage.ShowMsg(hand,e.getMessage());
            return false;
        }

    }

    public boolean ParseData_login(String data) {
        if (isPaused) {
            return false;
        }

        BaseHandler handler = new UserLoginHandler();

        handler.parse(data);
        Vector<?> users = (Vector<?>) BaseHandler.hash.get("users");// hash很重要，所有的东西都在hash中

        if (users.size() == 0) {
            return false;
        }
        User user = (User) users.elementAt(0);
        if (user == null) {
            return false;
        }
        if (Boolean.parseBoolean(user.getP00()) == false) {
            // 显示错误信息
            ShowMessage.ShowMsg(hand, user.getP01());
            return false;
        }

        if (Boolean.parseBoolean(user.getP00()) == true) {

//            sysUserInfo.setUserid(user.getP01());
            sysUserInfo.setUserid(et_username.getText().toString().trim());
            sysUserInfo.setLoginid(user.getP02());

            //总公司 代号传00，代理商则代理商代号
//            if ("总公司".equals(user.getP05())) {
//                sysUserInfo.setCompanyid("00");
//            } else {
//                sysUserInfo.setCompanyid(user.getP06());
//            }
            sysUserInfo.setUsername(user.getP03());
            sysUserInfo.setStock(user.getP04());
            sysUserInfo.setMode(user.getP05());
            //sysUserInfo.setCompanyid(user.getP06());
            sysUserInfo.setMobile(et_username.getText().toString().trim());
            sysUserInfo.setLoginpwd(et_password.getText().toString());
            sysUserInfo.setIfrember(checkBox.isChecked());
            sysUserInfo.setLastLoginServerIp(sysUserInfo.getServerip());
            IsChooseAccount=Boolean.parseBoolean(user.getP08());

//            if (user.getP07()!=""&&user.getP07()!=null&&user.getP07()!="null"){
//                try {
//                    JSONArray listjson = new JSONArray(user.getP07());
//                    for (int i = 0; i < listjson.length(); i++) {
//                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
//                        AccountSet accountSet = new AccountSet();
//                        accountSet.setAccountSetName(jsonObject2.getString("AccountSetName"));
//                        accountSet.setAccountSetId(jsonObject2.getString("AccountSetId"));
//                        accountSet.setDataBaseName(jsonObject2.getString("DataBaseName"));
//                        accounrsetList.add(accountSet);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            try {
//                SqliteDataHelper.getHelper(getApplicationContext()).execSQL(String.format("update brandinfo set  username =  '%1$s',pwd = '%2$s',IfRemember = '%3$s'  where businessid = '%4$s' ", et_username.getText().toString().trim(), et_password.getText().toString().trim(), String.valueOf(checkBox.isChecked()), sysUserInfo.getClientId()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return true;
        } else {
            return false;
        }
    }

    // 请求服务
    private void DownLoadAccounThread() {
        MyProgressDialog.show(mContext, "正在获取数据...", true, false);
        accounrsetList=new ArrayList<AccountSet>();
        Thread sendCode = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    accounrsetList =  AccessWeb.getHelper(getApplicationContext()).GetAccountSet();
                    ShowMessage.ShowMsg(hand, 7, "success");
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"下载出错" + e.getMessage());
                }
            }
        });
        sendCode.start();
    }

    private void ShowAccountSet(final List<AccountSet> accountSetList){

        AlertDialog alertDialog6 = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setTitle("选择要登录的账套")
                .setIcon(R.drawable.scs)
                .setAdapter(new AccountSetAdapter(mContext, accountSetList), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
//                        Toast.makeText(mContext, "点的是：" + accountSetList.get(i).getAccountSetId(), Toast.LENGTH_SHORT).show();
                        isPaused = false;
                        sysUserInfo.setAccountSetId(accountSetList.get(i).getAccountSetId());
                        sysUserInfo.setAccountSetName(accountSetList.get(i).getAccountSetName());

                        Thread sendCode = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    String loginresult = AccessWeb.getHelper(getApplicationContext()).UserLogin(et_username.getText().toString().trim(), et_password.getText().toString(),accountSetList.get(i).getAccountSetId());
                                    if (ParseData_login(loginresult)) {

                                        ShowMessage.ShowMsg(hand, 6, "success");
                                    }else{
                                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage, "登录选择账套报错");
                                    }

                                } catch (Exception e) {
                                    ShowMessage.ShowMsg(hand, ShowMessage.HandScanError,
                                            e.getMessage());

                                }
                            }
                        });
                        sendCode.start();


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

    @Override
    protected void onRestart() {
        isPaused = true;
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCheckNetState();
        tv_brand.setText(sysUserInfo.getAccountSetName());
        //		Log.i("main", "onResume--");
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCheckNetState();
        //		Log.i("main", "onStop() --");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 0: //点击关于，点击维护成功后

                stopCheckNetState();
                finish();

                break;
            case 1:
//                Intent intent = new Intent(LoginActivity.this, SysSettingActivity.class);
//                startActivity(intent);
                break;
            case 2:// 从main主界面中返回
                GetNetRun.State = true;
                break;
            case 3:
                break;
        }
    }

    private class handShowMsg extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ShowMessage.HandShowMessage: // 显示错误提示显示
                    if (loading != null)
                        loading.Close();
                    ShowMessage.Show(LoginActivity.this, msg.obj.toString());
                    MyProgressDialog.close();
                    break;

                case ShowMessage.HandSuccess: // 登录成功
//                    if (loading != null)
//                        loading.Close();

//                    if (IsChooseAccount){
//                        DownLoadAccounThread();
//                    }else{
                    DownLoadMenusThread();//登陆成功之后要下载菜单
//                        Intent Accounintent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(Accounintent);
//                    }
                    break;

                case 5:
                    loading.setTipText(msg.obj.toString());
                    break;

                case 6:
                    //每次登陆都要下载菜单
                    DownLoadMenusThread();
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);

                    break;

                case 7:
                    if (accounrsetList.size()>0) {
//                        Log.d("main",accounrsetList.toString());
                        ShowAccountSet(accounrsetList);
                    }else{
                        DownLoadMenusThread();
//                        Intent mainintent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(mainintent);
                    }
                    MyProgressDialog.close();
                    break;

                case 8:
                    //下载菜单后跳转首页
                    MyProgressDialog.close();
                    Intent menuintent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(menuintent);

                    break;

                case 11:// offline
                    ((ImageView) findViewById(R.id.imageView1))
                            .setImageResource(R.drawable.link_state_off);
                    break;
                case 12:// oneline
                    ((ImageView) findViewById(R.id.imageView1))
                            .setImageResource(R.drawable.link_state_on);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }



    private void showTip(String msg) {
        ShowMessage.ShowMsg(hand, 5, msg);
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

                    String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetAndroidDvMenu"+ "?showModel=2&PriAccountId=";
//                            +sysUserInfo.getAccountSetId();
//                    Log.d("main",requestUrl +sysUserInfo.getLoginid());
                    String result = request.getV1(requestUrl,sysUserInfo.getLoginToken(),"text/plain");//调用我们写的Get方法

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
                        ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"GetAndroidDvMenu-"+scsWebApiInfo.getMessage());
                    }
                } catch (Exception e) {
                    ShowMessage.ShowMsg(hand, ShowMessage.HandShowMessage,"GetAndroidDvMenu-"+e.getMessage());
                }
            }
        }).start();
    }



    // 默认的 children 字段名（可根据实际需求修改）
    private static final String DEFAULT_CHILDREN_KEY = "children";
    // 新增字段名（记录父级菜单信息）
    private static final String PARENT_MENU_CODE = "parentMenuCode";
    private static final String PARENT_MENU_NAME = "parentMenuName";

    /**
     * 提取所有非空的 children 数据，并与上一级菜单信息合并到同一个 List<Map>（包含顶层菜单）
     * @param menus 原始菜单列表（List<Map> 格式）
     * @param childrenKey children 字段名（默认 "children"）
     * @return 所有菜单（含顶层）及其子菜单的 Map 元素（平铺到同一 List）
     */
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public static List<Map<String, Object>> extractAllMenusWithParentInfo(
//            List<Map<String, Object>> menus,
//            String childrenKey) {
//
//        // 参数校验
//        if (menus == null || menus.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // 使用队列进行迭代遍历（元素为：当前菜单 + 父级菜单数据）
//        Queue<Map.Entry<Map<String, Object>, Map<String, Object>>> queue = new LinkedList<>();
//        // 初始时，顶层菜单的父级为 null
//        menus.forEach(menu -> queue.offer(new AbstractMap.SimpleEntry<>(menu, null)));
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        while (!queue.isEmpty()) {
//            Map.Entry<Map<String, Object>, Map<String, Object>> entry = queue.poll();
//            Map<String, Object> currentMenu = entry.getKey();
//            Map<String, Object> parentMenu = entry.getValue(); // 父级菜单数据（可能为 null）
//
//            // 复制当前菜单并附加父级信息
//            Map<String, Object> copiedCurrent = new HashMap<>(currentMenu);
//            if (parentMenu != null) {
//                copiedCurrent.put(PARENT_MENU_CODE, parentMenu.get("menuCode"));
//                copiedCurrent.put(PARENT_MENU_NAME, parentMenu.get("menuName"));
//            } else {
//                // 顶层菜单的父级信息设为 null 或空
//                copiedCurrent.put(PARENT_MENU_CODE, null);
//                copiedCurrent.put(PARENT_MENU_NAME, null);
//            }
//            // 将当前菜单添加到结果列表
//            result.add(copiedCurrent);
//
//            // 提取当前菜单的 children（带类型校验）
//            Optional<List<Map<String, Object>>> childrenOpt = getChildren(currentMenu, childrenKey);
//            if (childrenOpt.isPresent()) {
//                List<Map<String, Object>> children = childrenOpt.get();
//
//                // 仅当 children 非空时处理
//                if (!children.isEmpty()) {
//                    // 遍历每个子菜单，附加父级信息并加入队列
//                    for (Map<String, Object> child : children) {
//                        // 复制子菜单（避免修改原始数据）
//                        Map<String, Object> copiedChild = new HashMap<>(child);
//                        // 附加父级菜单信息（当前菜单的 menuCode 和 menuName）
//                        copiedChild.put(PARENT_MENU_CODE, currentMenu.get("menuCode"));
//                        copiedChild.put(PARENT_MENU_NAME, currentMenu.get("menuName"));
//                        // 将处理后的子菜单加入队列（继续处理下一层）
//                        queue.offer(new AbstractMap.SimpleEntry<>(copiedChild, currentMenu));
//                    }
//                }
//            }
//        }
//
//        return result;
//    }

//    /**
//     * 安全提取 children（带类型校验）
//     * @param menu 当前菜单对象（Map 格式）
//     * @param childrenKey children 字段名
//     * @return 包含 children 的 Optional（若类型不合法则返回 empty）
//     */
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private static Optional<List<Map<String, Object>>> getChildren(
//            Map<String, Object> menu,
//            String childrenKey) {
//
//        if (menu == null || !menu.containsKey(childrenKey)) {
//            return Optional.empty();
//        }
//
//        Object childrenObj = menu.get(childrenKey);
//        if (!(childrenObj instanceof List)) {
//            return Optional.empty(); // 非 List 类型，跳过
//        }
//
//        List<?> childrenList = (List<?>) childrenObj;
//        // 检查列表中所有元素是否为 Map 类型（可选严格校验）
//        boolean allElementsAreMaps = childrenList.stream()
//                .allMatch(element -> element instanceof Map);
//
//        if (!allElementsAreMaps) {
//            return Optional.empty(); // 元素非 Map 类型，跳过
//        }
//
//        // 安全转换为 List<Map<String, Object>>
//        @SuppressWarnings("unchecked")
//        List<Map<String, Object>> typedChildren = (List<Map<String, Object>>) childrenList;
//        return Optional.of(typedChildren);
//    }


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
    /**
     * 下载菜单
     */
    public void downloadMenus() {

        try {
            if (SqliteDataHelper.getHelper(getApplicationContext()).execSQLInt("select count(*) from menus") == 0) {
                //从网络下载菜单
                addTestMenu();
            }
//            String sql = "delete from menus";
//            SqliteDataHelper.getHelper(getApplicationContext()).execSQL(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //修改域名
    private void ShowModifydomain(){

        final View selectview = LayoutInflater.from(mContext).inflate(R.layout.dialog_modifydomain_layout,null);

        RadioGroup radio_group_set=selectview.findViewById(R.id.radio_group_set);//单选按钮

        RadioButton radio_scs=selectview.findViewById(R.id.radio_scs);
        RadioButton radio_ccs=selectview.findViewById(R.id.radio_ccs);

        if (sysUserInfo.getLoginType().equals("SCS")){
            radio_scs.setChecked(true);
        }else{
            radio_ccs.setChecked(true);
        }

//        radio_group_set.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                RadioButton radbtnstr = (RadioButton) findViewById(i);
//                Log.d("main", radbtnstr.getText().toString());
//            }
//        });

//        TextView tv_domainname= selectview.findViewById(R.id.tv_domainname);
//        final EditText ed_domainname= selectview.findViewById(R.id.ed_domainname);
//        tv_domainname.setText(sysUserInfo.getServerip());

        final AlertDialog alertDialog6 = new AlertDialog.Builder(mContext,R.style.Base_Theme_AppCompat_Light_Dialog)
                .setTitle("请输入要登录的模式")
                .setIcon(R.mipmap.scs)
                .setView(selectview)
                .setPositiveButton("确定选择", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        ///< 隐藏就显示，显示就隐藏 - 这种有时候再逻辑上会给你带来困扰，如果要强制隐藏，建议用别的方式；不要靠什么Boolean状态来做..
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }).create();
        alertDialog6.show();

        alertDialog6.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sysUserInfo.setServerIp("scs.holyes.net");

                int checkedId = radio_group_set.getCheckedRadioButtonId();

                if (checkedId != -1) {
                    RadioButton checkedRadioButton = selectview.findViewById(checkedId);
                    String selectedText = checkedRadioButton.getText().toString();
//                    int selectedIndex = checkedId; // 因为我们设置了ID为索引
                    sysUserInfo.setLoginType(selectedText);
                    // 处理选择结果
//                    if (sysUserInfo.getLoginType().equals("SCS")){
//                        radio_group_type.setVisibility(View.GONE);
//                    }else if (sysUserInfo.getLoginType().equals("CCS")){
//                        radio_group_type.setVisibility(View.VISIBLE);
//                        if (sysUserInfo.getCCSLoginMode().equals("")||sysUserInfo.getCCSLoginMode().equals("品牌商")){
//                            rb_brand_mode.setChecked(true);
//                        }else if (sysUserInfo.getCCSLoginMode().equals("代理商")){
//                            rb_agent_mode.setChecked(true);
//                        }else{
//                            rb_other_mode.setChecked(true);
//                        }
//                    }
                }
                alertDialog6.dismiss();
            }

        });


        if (alertDialog6.getWindow() != null) {
            WindowManager.LayoutParams lp = alertDialog6.getWindow().getAttributes();
            lp.width = 500; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            alertDialog6.getWindow().setAttributes(lp);
        }

    }



    /**
     * 添加测试菜单
     */
    private void addTestMenu() throws Exception {


        List<String> sqlList = new ArrayList<String>();
        //产品入库
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('产品入库','1','','','01')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('入库','1','','01','0101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单有明细入库','1','P_Dv_InStock_Bill','0101','010101')");
//        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
//                "('镜架有单无明细入库','1','P_Dv_InStock_NoDetail_Bill','0101','010104')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单入库','1','P_Dv_InStock_NoBill','0101','010102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片有单入库','1','P_Dv_InStock_Lens_Bill','0101','010103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单入库','1','P_Dv_Lens_InStock_NoBill','0101','010105')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('品牌码入库','1','P_Dv_BrandCode_InStock','0101','010106')");
//        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
//                "('镜片无单入库','1','P_Lens_InStock_NoBill','0101','010104')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('撤消','1','','01','0102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单入库撤消','1','P_Dv_InStock_Bill_Cancel','0102','010201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单入库撤消','1','P_Dv_InStock_NoBill_Cancel','0102','010202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片有单入库撤消','1','P_Dv_InStock_Lens_Bill_Cancel','0102','010203')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单入库撤消','1','P_Dv_InStock_Lens_NoBill_Cancel','0102','010204')");

        //入库退回
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('入库退回','1','','','02')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('退回','1','','02','0201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单入库退回','1','','0201','020102')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单入库退回','1','P_Dv_ReturnedPurchase_Lens_Z_G_NoBill','0201','020103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('撤消','1','','02','0202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单退回撤消','1','','0202','020202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单退回撤消','1','P_Dv_ReturnedPurchase_Lens_Z_G_NoBill_Cancel','0202','020203')");

        //总店发货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('总店发货','1','','','03')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('发货','1','','03','0301')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单有入库总店发货','1','','0301','030101')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单有入库总店发货','1','','0301','030103')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('撤消','1','','03','0302')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单总店发货撤消','1','','0302','030201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单总店发货撤消','1','','0302','030202')");
//        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
//                "('镜片有单总店发货撤销','1','P_Dv_OutStock_Lens_Z_D_NoBill_Cancel','0302','030203')");


        //总店退货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('总店退货','1','','','04')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('退货','1','','04','0401')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单总店退货','1','','0401','040103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('撤消','1','','04','0402')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单总店退货撤消','1','','0402','040203')");

        //分店发货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('分店发货','1','','','05')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('发货','1','','05','0501')");
//
//        无入库分店发货功能
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单无入库分店发货','1','','0501','050108')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单无入库分店发货','1','','0501','050109')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单有入库有明细分店发货','1','','0501','050101')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单有入库无明细分店发货','1','','0501','050102')");



        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单有入库分店发货','1','','0501','050103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片有单有入库分店发货','1','P_Dv_OutStock_Lens_Z_L_Bill_BeInStock','0501','050104')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单有入库分店发货','1','P_Dv_OutStock_Lens_Z_L_NoBill_BeInStock','0501','050105')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片出库绑码','1','','0501','050106')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片出库绑码(按销售单)','1','','0501','050107')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('发货撤消','1','','05','0502')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单分店发货撤消','1','','0502','050201')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单分店发货撤消','1','','0502','050202')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片有单分店发货撤销','1','P_Dv_OutStock_D_L_Lens_Bill_Cancel','0502','050203')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单分店发货撤销','1','P_Dv_OutStock_Lens_Z_L_NoBill_Cancel','0502','050204')");



        //分店退货
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('分店退货','1','','','06')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('退货','1','','06','0601')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架分店无单选客退货','1','','0601','060103')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架分店无单扫描退货','1','','0601','060104')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单选客退货','1','P_Dv_ReturnedPurchase_Lens_Z_L_NoBill','0601','060105')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单扫描退货','1','P_Dv_ReturnedPurchase_Lens_D_L_NoBill_NoCust','0601','060106')");

        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('撤消','1','','06','0602')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单分店退货撤消','1','','0602','060203')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜片无单分店退货撤销','1','P_Dv_ReturnedPurchase_Lens_Z_L_NoBill_Cancel','0602','060204')");

        //其他功能
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('其他扫描','1','','','07')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架有单调拨','1','','07','0701')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('镜架无单调拨','1','','07','0702')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('仓库补标','1','P_Dv_MendLable_Z','07','0703')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('退货直通车','1','P_Dv_ReturnedPurchase_D_L_NoBill_Fleeing','07','0704')");


        //物流查询
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('产品物流','1','','','08')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('物流查询','1','P_ProductLogist','08','0801')");

        //CCS其他功能
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('其他扫描','1','','','09')");
        sqlList.add("insert into menus(menuname,showstatus,procedurename,parentcode,menucode)values" +
                "('换货补扫','1','P_Dv_CCSBarcodeStatusWrite','09','0901')");

        SqliteDataHelper.getHelper(getApplicationContext()).BatchOperation(sqlList);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //暴龙或者总公司模式返回失效
                return false;
            case KeyEvent.KEYCODE_MINUS:
            case KeyEvent.KEYCODE_HOME:
                return false;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
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
