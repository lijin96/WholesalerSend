package com.example.wholesalersend.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.example.wholesalersend.entity.AccountSet;
import com.example.wholesalersend.entity.SalesScsWebApiInfo;
import com.example.wholesalersend.entity.ScanApiResponse;
import com.example.wholesalersend.entity.ScanOrder;
import com.example.wholesalersend.entity.SupplierInfor;
import com.example.wholesalersend.entity.SyncCustomers;
import com.example.wholesalersend.entity.SyncStores;
import com.example.wholesalersend.utils.SysUserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: AccessWeb
 * @Description: 网络请求和接口文件
 * @Author: lijin
 * @Date: 2023年10月23日15:40:48
 */
public class AccessWeb {

    private Context mContext;

    private SysUserInfo sysUserInfo;

    public String mWebId = "", port = "9521";//测试9520，客户的9521



//    private String webservice_url = "http://hs.ccs2008.com:9521/ScsAndroid/Android.asmx";
//    //测试服务，客户的端口是9521
//    private String webdownload_url = "http://hs.ccs2008.com:9521/ScsAndroid/DownLoadWebService" +
//            ".asmx";//9520

    private String webservice_url = "http://scs.holyes.net:9521/ScsAndroid/Android.asmx";
    //测试服务，客户的端口是9521
    private String webdownload_url = "http://scs.holyes.net:9521/ScsAndroid/DownLoadWebService.asmx";//9520

    private String namespace = "http://www.holyes.net/ccsUserLogin/";
//    private String namespace ="http://tempuri.org/";


    public AccessWeb(Context content) {
        this.mContext = content;
        sysUserInfo = new SysUserInfo(content);
        updateurl();
    }

    private static AccessWeb instance;

    public static AccessWeb getHelper(Context context) {

        if (instance == null) {
            instance = new AccessWeb(context);
        }

        return instance;
    }

    /**
     * 更新url，这个方法主要是在输入更换网址的时候要更新网址，否则网址用的还是之前的那个，就会登录失败
     */
    public void updateurl() {

//        port = sysUserInfo.getServerport();
//        webservice_url = String.format("http://%1$s:%2$s/ScsAndroid/Android.asmx",
//                sysUserInfo.getServerip(), port);
//        webdownload_url = String.format("http://%1$s:%2$s/ScsAndroid/DownLoadWebService.asmx",
//                sysUserInfo.getServerip(), port);

        port = sysUserInfo.getServerport();
        webservice_url = String.format("http://%1$s:%2$s/%3$s/Android.asmx",
                sysUserInfo.getServerip(), port,sysUserInfo.getInterfaceAdress());
        webdownload_url = String.format("http://%1$s:%2$s/%3$s/DownLoadWebService.asmx",
                sysUserInfo.getServerip(), port,sysUserInfo.getInterfaceAdress());

//        Log.d("main","web:"+webservice_url);
//        Log.d("main","webload:"+webdownload_url);

    }

    /**
     * 检查服务器网络状态
     *
     * @return
     */
    int i = 0;

    public Boolean CheckServerState() {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        para.add(map);
        try {
            getWebResult("Connectline", para);
//            Log.i("main", "5Connectline-----"+i++);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取下载产品记录数
     *
     * @throws Exception
     */
    public String GetDownLoadGoodsRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadGoodsRecord", para);
    }

    /**
     * 获取下载产品资料
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadGoodsInfor(String cUpdate) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadGoodsInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("BrandName", jsonObject2.getString("BrandName"));
            map1.put("GoodsId", jsonObject2.getString("GoodsId"));
            map1.put("GoodsDescription", jsonObject2.getString("GoodsDescription"));
            map1.put("Modelm", jsonObject2.getString("Modelm"));
            map1.put("Colors", jsonObject2.getString("Colors"));
            map1.put("GoodsYear", jsonObject2.getString("GoodsYear"));
            map1.put("ProdType", jsonObject2.getString("ProdType"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 批发商获取下载产品资料
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownSCSLoadGoodsInfor(String tSearchWord, int tPageIndex,
                                                              String tGoodsTypeName) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 200);
        map.put("tGoodsTypeName", tGoodsTypeName);
        para.add(map);
//        Log.d("main",para.toString());
        String result = downLoadWebResult("GetDownLoadGoodsInfor", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("BrandName", jsonObject2.optString("BrandName"));//品牌
            map1.put("GoodsId", jsonObject2.optString("GoodsId"));//产品id
            map1.put("GoodsSysCode", jsonObject2.optString("GoodsSysCode"));//产品系统id
            map1.put("GoodsDescription", jsonObject2.optString("GoodsDescription"));//描述
            map1.put("Modelm", jsonObject2.optString("Modelm"));//型号
            map1.put("Colors", jsonObject2.optString("Colors"));//色号
            map1.put("GoodsYear", jsonObject2.optString("GoodsYear"));//年份
            map1.put("ProdType", jsonObject2.optString("ProdType"));//品类
            map1.put("Uprecndate", jsonObject2.optString("Uprecndate"));
            map1.put("RefractiveIndex", jsonObject2.optString("RefractiveIndex"));//折射率
            map1.put("Breed", jsonObject2.optString("Breed"));//品种
            map1.put("SeriesName", jsonObject2.optString("SeriesName"));//系列

            list.add(map1);
        }
        return list;
    }


    /**
     * 2.0迭代 获取下载产品资料
     *
     * @throws Exception
     */
    public List<Map<String, Object>> NewGetDownSCSLoadGoodsInfor(String tSearchWord, int tPageIndex,String tGoodsTypeName) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("Query", tSearchWord);
        map.put("Page", tPageIndex);
        map.put("Limit", 200);
//        map.put("tGoodsTypeName", tGoodsTypeName);
        para.add(map);
//        Log.d("main",para.toString());
        String result = GetAPIStringInterface("GoodsInfor/GetGoodsList", para);
//        Log.d("main",result.toString());
        JSONObject jsonObject = new JSONObject(result);
        JSONArray listjson =jsonObject.optJSONArray("pagingData");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("BrandName", jsonObject2.get("brandName"));//品牌
            map1.put("GoodsId", jsonObject2.get("goodsCode"));//产品id
            map1.put("GoodsSysCode", jsonObject2.get("goodsCode"));//产品系统id
            map1.put("GoodsDescription", jsonObject2.get("goodsName"));//描述
            map1.put("Modelm", jsonObject2.get("modelm"));//型号
            map1.put("Colors", jsonObject2.get("colors"));//色号

            map1.put("ColorsRefractiveIndex", jsonObject2.get("colors")+" "+jsonObject2.get("refractiveIndex"));//色号折射率
            map1.put("GoodsYear", jsonObject2.get("goodsYear"));//年份
            map1.put("ProdType", jsonObject2.get("goodsTypeName"));//品类
            map1.put("Uprecndate", jsonObject2.get("modifyDate"));
            map1.put("RefractiveIndex", jsonObject2.get("refractiveIndex"));//折射率
            map1.put("Breed", jsonObject2.get("breed"));//品种
            list.add(map1);
        }
        return list;
    }
    /**
     * 获取下载供应商记录数
     *
     * @throws Exception
     */
    public String GetDownLoadSupplierRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadSupplierRecord", para);
    }

    /**
     * 下载供应商函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadSupplierInfor(String cUpdate) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadSupplierInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载供应商函数
     *
     * @throws Exception
     */
    public List<SupplierInfor> SCSGetDownLoadSupplierInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 100);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadSupplierInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<SupplierInfor> list = new ArrayList<SupplierInfor>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            SupplierInfor map1 = new SupplierInfor();
            map1.setSupplierId(jsonObject2.getString("SupplierId"));
            map1.setSuppSysCode(jsonObject2.getString("SuppSysCode"));
            map1.setSupplierName(jsonObject2.getString("SupplierName"));
            map1.setUprecndate(jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载供应商函数
     * 2.0 迭代接口
     * @throws Exception
     */
    public List<SupplierInfor> NewSCSGetDownLoadSupplierInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("Query", tSearchWord);
//        map.put("tPageIndex", tPageIndex);
//        map.put("tPageNum", 100);
        para.add(map);
        String result = GetAPIStringInterface("SupplierInfor/GetList", para);
//        Log.d("main",result);
//        JSONObject jsonObject = new JSONObject(result);
        JSONArray listjson =new JSONArray(result);
        List<SupplierInfor> list = new ArrayList<SupplierInfor>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            SupplierInfor map1 = new SupplierInfor();
            map1.setSupplierId(jsonObject2.optString("supplierCode"));
            map1.setSuppSysCode(jsonObject2.optString("suppSysCode"));
            map1.setSupplierName(jsonObject2.optString("supplierName"));
            map1.setUprecndate(jsonObject2.optString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }



    /**
     * 获取下载仓库记录数
     *
     * @throws Exception
     */
    public String GetDownLoadStockRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadStockRecord", para);
    }

    /**
     * 下载仓库函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadStockInfor(String cUpdate) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStockInfor", para);

        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StockId", jsonObject2.getString("StockId"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }



    /**
     * 下载仓库函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> SCSGetDownLoadStockInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 50);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStockInfor", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StockSysCode", jsonObject2.getString("StockSysCode"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 迭代下载仓库函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> NewSCSGetDownLoadStockInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 50);
        para.add(map);
        List<Map<String, Object>> result = GetAPIListInterface("Stock/GetList", para);
//        Log.d("main",result.toString());
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < result.size(); i++) {
            Map<String, Object> resultmap = result.get(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StockSysCode", resultmap.get("stockSysCode"));
            map1.put("StockId", resultmap.get("stockCode"));
            map1.put("StockName", resultmap.get("stockName"));
            map1.put("Uprecndate", resultmap.get("modifyDate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载代理商记录数
     *
     * @throws Exception
     */
    public String GetDownLoadAgentRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadAgentRecord", para);
    }

    /**
     * 下载代理商函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadAgentInfor(String cUpdate) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadAgentInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载直营店记录数
     *
     * @throws Exception
     */
    public String GetDownLoadDirectRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadDirectRecord", para);
    }

    /**
     * 下载直营店函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadDirectInfor(String cUpdate) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", cUpdate);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadDirectInfor", para);
        //		Log.d("main","--"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("TraderName", jsonObject2.getString("TraderName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("ProviceName", jsonObject2.getString("ProviceName"));
            map1.put("CityName", jsonObject2.getString("CityName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载零售商记录数
     *
     * @throws Exception
     */
    public String GetDownLoadTraderRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadTraderRecord", para);
    }

    /**
     * 下载零售商函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadTraderInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 200);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadTraderInfor", para);
//        Log.d("main","零售商集合"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("TraderName", jsonObject2.getString("TraderName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("ProviceName", jsonObject2.getString("ProviceName"));
            map1.put("CityName", jsonObject2.getString("CityName"));
//            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("CustSysCode", jsonObject2.getString("CustSysCode"));
            map1.put("Alias", jsonObject2.getString("Alias"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 2.0迭代接口 下载零售商函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> NewGetDownLoadTraderInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("Query", tSearchWord);
        map.put("Page", tPageIndex);
        map.put("Limit", 200);
        para.add(map);
//        Log.d("main","零售商集合"+para.toString());
        String result = GetAPIStringInterface("CustomerInfor/GetCustomerList", para);
//        Log.d("main","代理商集合"+result);
        JSONObject jsonObject = new JSONObject(result);
        JSONArray listjson =jsonObject.optJSONArray("pagingData");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TraderId", jsonObject2.optString("customerCode"));
            map1.put("TraderName", jsonObject2.optString("customerName"));
            map1.put("Link", jsonObject2.optString("link"));
            map1.put("Tel", jsonObject2.optString("tel"));
            map1.put("CorpAddr", jsonObject2.optString("corpAddr"));
            map1.put("ProviceName", jsonObject2.optString("proviceName"));
            map1.put("CityName", jsonObject2.optString("cityName"));
//            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("CustSysCode", jsonObject2.optString("custSysCode"));
            map1.put("Alias", jsonObject2.optString("businessName"));
            map1.put("Uprecndate", jsonObject2.optString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 获取下载分销店记录数
     */
    public String GetDownLoadStoreRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadStoreRecord", para);
    }

    /**
     * 获取下载分销店函数
     */
    public List<Map<String, Object>> GetDownLoadStoreInfor(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStoreInfor", para);
//        Log.d("main","分销店集合"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StoreName", jsonObject2.getString("StoreName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));

            list.add(map1);
        }
        return list;
    }

    /**
     * 获取下载分销店函数
     */
    public List<Map<String, Object>> GetDownLoadSCSStoreInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 200);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStoreInfor", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StoreSysCode", jsonObject2.getString("StoreSysCode"));
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StoreName", jsonObject2.getString("StoreName"));
            map1.put("StoreAlias", jsonObject2.getString("Alias"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("TraderName", jsonObject2.getString("TraderName"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            map1.put("TraderSysId", jsonObject2.getString("TraderSysId"));
            map1.put("TraderAlias", jsonObject2.getString("TraderAlias"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 2.0迭代 获取下载分销店函数
     */
    public List<Map<String, Object>> NewGetDownLoadSCSStoreInfor(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("Query", tSearchWord);
        map.put("Page", tPageIndex);
        map.put("Limit", 200);
        para.add(map);
        String result = GetAPIStringInterface("CustomerInfor/GetStoreChoiceList", para);
//        Log.d("main",result);
        JSONObject jsonObject = new JSONObject(result);
        JSONArray listjson =jsonObject.optJSONArray("pagingData");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StoreSysCode", jsonObject2.optString("storeSysCode"));
            map1.put("StoreId", jsonObject2.optString("storeCode"));
            map1.put("StoreName", jsonObject2.optString("storeName"));
            map1.put("StoreAlias", jsonObject2.optString("Alias"));
            map1.put("Link", jsonObject2.optString("link"));
            map1.put("Tel", jsonObject2.optString("tel"));
            map1.put("CorpAddr", jsonObject2.optString("corpAddr"));
            map1.put("TraderId", jsonObject2.optString("customerCode"));
            map1.put("TraderName", jsonObject2.optString("customerName"));
            map1.put("Uprecndate", jsonObject2.optString("Uprecndate"));
            map1.put("TraderSysId", jsonObject2.optString("custSysCode"));
            map1.put("TraderAlias", jsonObject2.optString("businessName"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 获取下载直营分销店记录数
     */
    public String GetDownLoadStraightShopRecord(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        return downLoadWebResult("GetDownLoadStraightShopRecord", para);
    }

    /**
     * 获取下载直营分销店函数
     */
    public List<Map<String, Object>> GetDownLoadStraightShopInfor(String tCondition) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCondition", tCondition);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadStraightShopInfor", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StoreName", jsonObject2.getString("StoreName"));
            map1.put("Link", jsonObject2.getString("Link"));
            map1.put("Tel", jsonObject2.getString("Tel"));
            map1.put("CorpAddr", jsonObject2.getString("CorpAddr"));
            map1.put("TraderId", jsonObject2.getString("TraderId"));
            map1.put("Uprecndate", jsonObject2.getString("Uprecndate"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载有单入库品检入库表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadScsPurOrder(String tOrderType, String tSearchWord
            , int tPageIndex, boolean tIsNoDetail) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());

        map.put("tOrderType", tOrderType);
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 500);
        map.put("tIsNoDetail", tIsNoDetail);//是否无明细

        para.add(map);
//        Log.d("main","GetDownLoadScsPurOrder"+para.toString());
        String result = downLoadWebResult("GetDownLoadScsPurOrder", para);

        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PurOrderNo", jsonObject2.getString("PurOrderNo"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载品检入库明细函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadScsPurOrderDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDownLoadScsPurOrderDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoInGoodQty"));
            map1.put("amount", jsonObject2.getString("NoInGoodQty"));//增加一个字段
            map1.put("brandName", jsonObject2.getString("BrandName"));//品牌
            list.add(map1);
        }
        return list;
    }


    /**
     * 获取镜片采购订单明细折射率信息（有单入库）
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetScsPurOrderLensDetail(String tBillNo, String tSearchWord) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        map.put("tSearchWord", tSearchWord);
        para.add(map);

        String result = downLoadWebResult("GetScsPurOrderLensDetail", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("GoodsCode", jsonObject2.getString("GoodsCode"));
            map1.put("GoodsName", jsonObject2.getString("GoodsName"));
            map1.put("RefractiveIndex", jsonObject2.getString("RefractiveIndex"));
            map1.put("Modelm", jsonObject2.getString("Modelm"));
            map1.put("Colors", jsonObject2.getString("Colors"));
            map1.put("NoInGoodQty", jsonObject2.getString("NoInGoodQty"));//未入库单
            map1.put("PurQty", jsonObject2.getString("PurQty"));//订单数
            map1.put("GoodsSysCode", jsonObject2.getString("GoodsSysCode"));

            list.add(map1);
        }
        return list;
    }


    /**
     * 获取镜片采购订单明细柱镜列表
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetScsPurOrderAstigmatism(String tBillNo,
                                                               String tGoodsSysCode, String tDiopter) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        map.put("tGoodsSysCode", tGoodsSysCode);
        map.put("tDiopter", tDiopter);
        para.add(map);
//        Log.d("main",para.toString());
        String result = downLoadWebResult("GetScsPurOrderAstigmatism", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Astigmatism", jsonObject2.getString("Astigmatism"));
            map1.put("PurQty", jsonObject2.getString("PurQty"));
            map1.put("ScanQty", jsonObject2.getString("ScanQty"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取镜片采购订单指定柱镜下球镜列表
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetScsPurOrderDiopter(String tBillNo, String tGoodsSysCode,
                                                           String tAstigmatism) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        map.put("tGoodsSysCode", tGoodsSysCode);
        map.put("tAstigmatism", tAstigmatism);
        para.add(map);
//        Log.d("main",para.toString());
        String result = downLoadWebResult("GetScsPurOrderDiopter", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Diopter", jsonObject2.getString("Diopter"));
            map1.put("PurQty", jsonObject2.getString("PurQty"));//采购订单数
            map1.put("ScanQty", jsonObject2.getString("ScanQty"));//已扫描数量
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取无单入库镜片明细柱镜列表
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetScsIoGoodsAstigmatism(String tBillNo,
                                                              String tGoodsSysCode, String tDiopter) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        map.put("tGoodsSysCode", tGoodsSysCode);
        map.put("tDiopter", tDiopter);
        para.add(map);

        String result = downLoadWebResult("GetScsIoGoodsAstigmatism", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Astigmatism", jsonObject2.getString("Astigmatism"));
            map1.put("PurQty", jsonObject2.getString("PurQty"));
            map1.put("ScanQty", jsonObject2.getString("ScanQty"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 获取无单入库镜片明细球镜列表
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetScsIoGoodsDiopter(String tBillNo, String tGoodsSysCode,
                                                          String tAstigmatism) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        map.put("tGoodsSysCode", tGoodsSysCode);
        map.put("tAstigmatism", tAstigmatism);

        para.add(map);
//        Log.d("main",para.toString());
        String result = downLoadWebResult("GetScsIoGoodsDiopter", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Diopter", jsonObject2.getString("Diopter"));
            map1.put("PurQty", jsonObject2.getString("IoGoodsQty"));//采购订单数
            map1.put("ScanQty", jsonObject2.getString("ScanQty"));//已扫描数量
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载品检入库表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadPurCheckBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurCheckBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PurCheckLno", jsonObject2.getString("PurCheckLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载品检入库明细函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadPurCheckDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurCheckDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoInGoodQty"));
            map1.put("amount", jsonObject2.getString("NoInGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载销售配货直发分店表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadDirectStoreInvoiceBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectStoreInvoiceBill", para);
//        Log.d("BeInStock",result.toString());
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("StoreName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("StoreId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售配货代销表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadAgentInvoiceBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAgentInvoiceBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载代销销售订单信息（有单发货）
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadScsRetailSaleOrder(String tSearchWord,
                                                                   int tPageIndex,String tOrderType) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCustType", "Cust");
        map.put("tOrderType", tOrderType);//订单类型镜片、普通
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 200);

        para.add(map);
        String result = downLoadWebResult("GetDownLoadScsSaleOrder", para);
//        Log.d("main", result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();

            map1.put("PeiGoodLno", jsonObject2.getString("SaleOrderNo"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("CustName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("CustId"));
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StockId", jsonObject2.getString("StockId"));

            list.add(map1);
        }
        return list;
    }


    /**
     * 下载分店销售订单信息（有单发货）
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadScsBranchSaleOrder(String tSearchWord,
                                                                   String tOrderType, int tPageIndex, boolean tIsNoDetail) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCustType", "Store");
        map.put("tOrderType", tOrderType);//订单类型镜片、普通
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 200);
        map.put("tIsNoDetail", tIsNoDetail);//是否无明细
        para.add(map);
//        Log.d("main","GetDownLoadScsSaleOrder"+para.toString());
        String result = downLoadWebResult("GetDownLoadScsSaleOrder", para);
//        Log.d("main","GetDownLoadScsSaleOrder"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();

            map1.put("PeiGoodLno", jsonObject2.getString("SaleOrderNo"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("StoreName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("CustId"));
            map1.put("StoreId", jsonObject2.getString("StoreId"));
            map1.put("StockId", jsonObject2.getString("StockId"));

            list.add(map1);
        }
        return list;
    }

    /**
     * 下载镜片分店有单有入库发货明细信息（有单发货）
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetScsSaleOrderLensDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetScsSaleOrderLensDetail", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("GoodsCode", jsonObject2.getString("GoodsCode"));
            map1.put("RefractiveIndex", jsonObject2.getString("RefractiveIndex"));
            map1.put("Material", jsonObject2.getString("Material"));
            map1.put("Diopter", jsonObject2.getString("Diopter"));
            map1.put("Astigmatism", jsonObject2.getString("Astigmatism"));
            map1.put("BrandName", jsonObject2.getString("BrandName"));
            map1.put("NoSendGoodQty", jsonObject2.getString("Nosendgoodqty"));//增加一个数量字段
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载销售配货代销明细函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadAgentInvoiceDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAgentInvoiceDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoSendGoodQty"));
            map1.put("amount", jsonObject2.getString("NoSendGoodQty"));//增加一个数量字段
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售订单明细信息（有单发货）
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDownLoadScsSaleOrderDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);

//        Log.d("main","GetDownLoadScsSaleOrderDetail"+map.toString());
        String result = downLoadWebResult("GetDownLoadScsSaleOrderDetail", para);
//        Log.d("main","GetDownLoadScsSaleOrderDetail"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();

            map1.put("brandName", jsonObject2.getString("BrandName"));
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoSendGoodQty"));
            map1.put("amount", jsonObject2.getString("NoSendGoodQty"));//增加一个数量字段
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售配货直销表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadDirectInvoiceBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectInvoiceBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("TraderName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("TraderId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载销售配货直发分店明细函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadDirectStoreInvoiceDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectStoreInvoiceDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoSendGoodQty"));
            map1.put("amount", jsonObject2.getString("NoSendGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售配货直销明细函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadDirectInvoiceDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectInvoiceDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoSendGoodQty"));
            map1.put("amount", jsonObject2.getString("NoSendGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售退货代销表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadAgentReturnBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAgentReturnBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("AgentName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("AgentId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载销售退货直销表头函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadDirectReturnBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadDirectReturnBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PeiGoodLno", jsonObject2.getString("PeiGoodLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("AgentName", jsonObject2.getString("TraderName"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("AgentId", jsonObject2.getString("TraderId"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载套餐设置函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetPackMealSet() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetPackMealSet", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PackId", jsonObject2.getString("PackId"));
            map1.put("PackName", jsonObject2.getString("PackName"));

            list.add(map1);
        }
        return list;
    }

    /**
     * 下载套餐明细函数
     *
     * @throws Exception
     */
    public List<Map<String, Object>> GetPackMealDetail(String tPackId) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tPackId", tPackId);
        para.add(map);
        String result = downLoadWebResult("GetPackMealDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("GoodsId", jsonObject2.getString("GoodsId"));
            map1.put("BrandName", jsonObject2.getString("BrandName"));
            map1.put("GoodsType", jsonObject2.getString("GoodsType"));
            map1.put("SeriesName", jsonObject2.getString("SeriesName"));
            map1.put("Modelm", jsonObject2.getString("Modelm"));
            map1.put("Colors", jsonObject2.getString("Colors"));
            map1.put("PackNum", String.valueOf(jsonObject2.getInt("PackNum")));
            list.add(map1);
        }
        return list;
    }

    /**
     * 套餐装盒验证
     *
     * @throws Exception
     */
    public String P_Dv_PackDressBoxCheck(String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String result = getWebResult("P_Dv_PackDressBoxCheck", para);

        return result;
    }

    /**
     * 套餐装盒完成上传
     *
     * @throws Exception
     */
    public String P_Dv_PackDressBoxWrite(String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String result = getWebResult("P_Dv_PackDressBoxWrite", para);

        return result;
    }

    /**
     * 套餐装盒补打读取套标信息
     *
     * @throws Exception
     */
    public String GetPackMealBoxLabel(String tBarcode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBarcode", tBarcode);    //已套餐装盒中任一物流码
        para.add(map);
        String result = downLoadWebResult("GetPackMealBoxLabel", para);

        return result;
    }


    /**
     * 扫描操作函数
     *
     * @param methodName 要执行操作的方法名
     *                   P_Dv_InStock_Bill：有单入库
     *                   P_Dv_InStock_Bill_Cancel：有单撤消入库
     *                   P_Dv_InStock_NoBill：无单入库
     *                   P_Dv_InStock_NoBill_Cancel：无单撤消入库
     *                   P_Dv_ReturnedPurchase_Z_G_Bill：有单入库退回
     *                   P_Dv_ReturnedPurchase_Z_G_Bill_Cancel：有单入库退回撤消
     *                   P_Dv_ReturnedPurchase_Z_G_NoBill：无单入库退回
     *                   P_Dv_ReturnedPurchase_Z_G_NoBill_Cancel：无单入库退回撤消
     *                   P_Dv_InStock_Lens_Bill 批发商镜片有单扫描入库
     *                   P_Dv_InStock_Lens_NoBill 批发商镜片无单扫描入库
     * @param methodName 方法名
     * @param tPara      扫描参数
     * @throws Exception
     */
    public String P_Dv_Scan(String methodName, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        if ("P_Dv_RecoverScan_D".equals(methodName))//除了代理回收扫描大写外，其他的几乎小写，实际去服务里看
        {
            map.put("tLoginID", sysUserInfo.getLoginid());
        } else {
            map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        }
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
//        Log.d("main",para.toString());
//        Log.d("main",methodName);
        String result = getWebResult(methodName, para);
//        Log.d("main",result);
        return result;
    }
    //品牌码入库单独接口
    public String P_Dv_Brand_Scan( String methodName, String tPara,String tBrandingCode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        map.put("tBrandingCode", tBrandingCode);
        para.add(map);
//        Log.d("main",para.toString());
        String result = getWebResult(methodName, para);
//        Log.d("main",result);
        return result;
    }



    /**
     * 批发商物流查询
     *
     * @param tCodeType
     * @param tCodeValue
     * @param tUnitId
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-11-30 上午11:29:56
     */
    public List<Map<String, Object>> P_ProductLogist(String tCodeType, String tCodeValue,
                                                     String tUnitId) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tCodeType", tCodeType);
        map.put("tCodeValue", tCodeValue);
        map.put("tUnitId", tUnitId);
        para.add(map);
        String result = getWebResult("P_ProductLogist", para);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        JSONArray listjson = new JSONArray(result);

        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);

            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Remark", jsonObject2.getString("memo"));
            list.add(map1);
        }
        return list;
    }


    /**
     * CCS物流查询
     *
     * @param tCodeType
     * @param tCodeValue
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-11-30 上午11:29:56
     */
//    public List<Map<String, Object>> GetCcsBarcodeTrackingInfor(String tBrandCode, String tBrandName, String tCodeType, String tCodeValue) throws Exception {
//        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
//        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
//        map.put("tBrandCode", tBrandCode);
//        map.put("tBrandName", tBrandName);
//        map.put("tCodeType", tCodeType);//1-物流码2-防伪码3-积分码
//        map.put("tCodeValue", tCodeValue);//条码
//        para.add(map);
////        Log.d("main", para.toString());
//        String result = getWebResult("GetCcsBarcodeTrackingInfor", para);
//
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        JSONArray listjson = new JSONArray(result);
//
//        for (int i = 0; i < listjson.length(); i++) {
//            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
//
//            Map<String, Object> map1 = new HashMap<String, Object>();
//            map1.put("Remark", jsonObject2.getString("memo"));
//            list.add(map1);
//        }
//        return list;
//    }

    /**
     * CCS物流查询
     *
     * @param tCodeType
     * @param tCodeValue
     * @return
     * @throws Exception
     * @author
     * @version
     */
    public String GetCcsBarcodeTrackingInfor(String tBrandCode, String tCodeType, String tCodeValue) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
//        map.put("tBrandCode", tBrandCode);
//        map.put("tBrandName", tBrandName);
//        map.put("tCodeType", tCodeType);//1-物流码2-防伪码3-积分码
//        map.put("tCodeValue", tCodeValue);//条码
        map.put("BrandCode", tBrandCode);
//        map.put("tBrandName", tBrandName);
        map.put("CodeKind", tCodeType);//1-物流码2-防伪码3-积分码
        map.put("CodeValue", tCodeValue);//条码
        para.add(map);
//        Log.d("main--", para.toString());
        String result = GetAPIStringInterface("AndroidDv/GetUpstreamProductLogistics", para);

        return result;
    }


    /**
     * 物流查询
     *
     * @param tBcOrAc  查码类型(其值为：True-物流码;False-表示防伪码)
     * @param tCode    查码内容,扫描条码
     * @param tCompnay 用户所在公司代号(总公司用户为：00，代理商用户为所在代理商代号)
     */
    public List<Map<String, Object>> BarcodeLogistics(String tCode, boolean tBcOrAc,
                                                      String tCompnay) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tCode", tCode);
        map.put("tBcOrAc", tBcOrAc);
        map.put("tCompnay", tCompnay);
        para.add(map);
        String result = getWebResult("BarcodeLogistics", para);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        JSONArray listjson = new JSONArray(result);

        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);

            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("Remark", jsonObject2.getString("memo"));
            list.add(map1);
        }
        return list;
    }


    //获取账套列表
    public List<AccountSet> GetAccountSet() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tUserCode", sysUserInfo.getUserid());//这个id一定要到服务里面去确认大小写，否则可能有错
        para.add(map);
        String result = getWebResult("GetAccountSet", para);

        List<AccountSet> list = new ArrayList<AccountSet>();
        JSONArray listjson = new JSONArray(result);
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            AccountSet accountSet = new AccountSet();
            accountSet.setAccountSetName(jsonObject2.getString("AccountSetName"));
            accountSet.setAccountSetId(jsonObject2.getString("AccountSetId"));
            accountSet.setDataBaseName(jsonObject2.getString("DataBaseName"));
            list.add(accountSet);
        }
        return list;
    }

    //获取发货退货入库扫描单明细
    public List<ScanOrder> GetCurrentScanBill(String tDeSysCode, String tStoreSysCode,
                                              String tScanType, String tOrderType) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tDeSysCode", tDeSysCode);//供应商或者客户系统代号
        map.put("tStoreSysCode", tStoreSysCode);//门店系统代号
        map.put("tScanType", tScanType);//扫描类型
        map.put("tOrderType", tOrderType);//普通还是镜片

        //OutStock              出货扫描
        //OutReturn             退货扫描
        //InStock               入库扫描
        //InReturn              入库退回扫描
        para.add(map);
//        Log.d("main",para.toString());
        String result = downLoadWebResult("GetCurrentScanBill", para);
//        Log.d("main",result);
        List<ScanOrder> list = new ArrayList<ScanOrder>();
        JSONArray listjson = new JSONArray(result);
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            ScanOrder scanOrder = new ScanOrder();
            scanOrder.setBillNo(jsonObject2.getString("BillNo"));
            scanOrder.setBillNum(String.valueOf(jsonObject2.getInt("BillNum")));
            scanOrder.setLastScanTime(jsonObject2.getString("LastScanTime"));
            scanOrder.setStockCode(jsonObject2.getString("StockCode"));
            scanOrder.setStockName(jsonObject2.getString("StockName"));
            scanOrder.setStockSysCode(jsonObject2.getString("StockSysCode"));
            list.add(scanOrder);
        }
        return list;
    }


    //获取品牌码采购入库单
    public List<Map<String, Object>> GetBrandCurrentScanBill(String tScanType, String tOrderType) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("tDeSysCode", "");//供应商或者客户系统代号
        map.put("tStoreSysCode", "");//门店系统代号
        map.put("tScanType", tScanType);//扫描类型
        map.put("tOrderType", tOrderType);//普通还是镜片
        para.add(map);
//        Log.d("mainpara",para.toString());
        String result = downLoadWebResult("GetCurrentScanBill", para);
//        Log.d("main",result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("BillNo", jsonObject2.getString("BillNo"));
            map1.put("BillDate", jsonObject2.getString("BillDate"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("SuppSysCode", jsonObject2.getString("SuppSysCode"));
            map1.put("BillNum",String.valueOf(jsonObject2.getInt("BillNum")));
            map1.put("ReMark", jsonObject2.getString("ReMark"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("StockSysCode", jsonObject2.getString("StockSysCode"));
            list.add(map1);
        }
        return list;
    }


    //下载品牌
    public List<Map<String, Object>> GetBrandInfor(String tQuery,Boolean tIsSytemBrand) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        String isSystembrand="";
        if (tIsSytemBrand){
            isSystembrand= String.valueOf(tIsSytemBrand);
        }
        map.put("Query", tQuery);//查询条件
        map.put("IsPartnerBrand", isSystembrand);//是否合作品牌：true=合作品牌（BrandingCode 不为空）；false=非合作品牌（BrandingCode 为空）；不传则不过滤
        para.add(map);
        String result = instance.GetAPIStringInterface("Brand/GetBrandList", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("BrandingCode", jsonObject2.optString("brandingCode"));
            map1.put("BrandCode", jsonObject2.optString("brandCode"));
            map1.put("BrandName", jsonObject2.optString("brandName"));
            if (jsonObject2.optString("agentCode").equals("null")){
                map1.put("AgentCode", "");
            }else {
                map1.put("AgentCode", jsonObject2.optString("agentCode"));
            }
            map1.put("TradeStatus", jsonObject2.optString("tradeStatus"));
            list.add(map1);
        }
        return list;
    }

    public List<Map<String, Object>> GetCcsCustomer(String tBrandingCode,String tAgentCode,String tQuery) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("BrandingCode", tBrandingCode);//品牌id
        map.put("AgentCode", tAgentCode);//代理商id
        map.put("Query",tQuery);//模糊查找
        map.put("ProviceName", "");
        map.put("CityName", "");
        para.add(map);
//        Log.d("main","GetCcsCustomer-"+para.toString());
//        String result = getWebResult("GetCcsCustomer", para);
        String result = instance.GetAPIStringInterface("AndroidDv/GetUpstreamCustomer", para);
//        Log.d("main","GetCcsCustomer-"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("CutCode", jsonObject2.getString("cutCode"));
            map1.put("CustName", jsonObject2.getString("custName"));
            map1.put("CustLink", jsonObject2.getString("custLink"));
            map1.put("CustTel", jsonObject2.getString("custTel"));
            map1.put("CustAddr", jsonObject2.getString("custAddr"));
            map1.put("SaleName", jsonObject2.getString("saleName"));
            List<Map<String, Object>> storeList = new ArrayList<Map<String, Object>>();
            JSONArray storeArray = jsonObject2.optJSONArray("store");
            if (storeArray != null) {
                for (int j = 0; j < storeArray.length(); j++) {
                    JSONObject storeObj = storeArray.getJSONObject(j);
                    Map<String, Object> storeMap = new HashMap<String, Object>();
                    storeMap.put("CutCode", storeObj.optString("cutCode"));
                    storeMap.put("StoreId", storeObj.optString("storeId"));
                    storeMap.put("StoreName", storeObj.optString("storeName"));
                    storeMap.put("CustName", jsonObject2.optString("custName"));
                    storeMap.put("CustLink", storeObj.optString("custLink"));
                    storeMap.put("CustTel", storeObj.optString("custMobile"));
                    storeMap.put("CustAddr", storeObj.optString("custAddr"));
                    storeMap.put("CorpName", storeObj.optString("corpName"));
                    storeList.add(storeMap);
                }
            }
            map1.put("StoreList", storeList);
            list.add(map1);
        }
        return list;
    }

    //下载CCS门店
    public List<Map<String, Object>> GetCcsStore(String tBrandingCode,String tAgentCode,String tQuery,String tCcsCustCode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());//这个id一定要到服务里面去确认大小写，否则可能有错
        map.put("BrandingCode", tBrandingCode);//品牌id
        map.put("AgentCode", tAgentCode);//代理商id
        map.put("Query",tQuery);//模糊查找
        map.put("ProviceName", "");
        map.put("CityName", "");
        map.put("CustCode", tCcsCustCode);//CCS客户代号
        para.add(map);
//        Log.d("main","GetCcsStore-"+para.toString());
//        String result = getWebResult("GetCcsStore", para);
        String result = instance.GetAPIStringInterface("AndroidDv/GetUpstreamStore", para);
//        Log.d("main","GetCcsStore-"+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("CutCode", jsonObject2.getString("cutCode"));
            map1.put("CustName", jsonObject2.getString("custName"));
            map1.put("StoreName", jsonObject2.getString("storeName"));
            map1.put("StoreId", jsonObject2.getString("storeId"));
            map1.put("CustLink", jsonObject2.getString("custLink"));
            map1.put("CustTel", jsonObject2.getString("custMobile"));
            map1.put("CustAddr", jsonObject2.getString("custAddr"));
            map1.put("CorpName", jsonObject2.getString("corpName"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载入库退出表头函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */
    public List<Map<String, Object>> GetDowLoadPurOutBill() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurOutBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PurCheckLno", jsonObject2.getString("PurCheckLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("SupplierName", jsonObject2.getString("SupplierName"));
            map1.put("SupplierId", jsonObject2.getString("SupplierId"));
            map1.put("StockName", jsonObject2.getString("StockName"));
            map1.put("StockId", jsonObject2.getString("StockId"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 2.0分店发货获取客户、门店是否绑定
     */
    public List<Map<String, Object>> GetScsCustStoreRelate(String tCustSysCode, String tStoreSysCode,String tBrandCode,String tAgentCode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("CustSysCode", tCustSysCode);//SCS客户系统代号
        map.put("StoreSysCode",tStoreSysCode);//SCS门店系统代号
        map.put("BrandingCode",tBrandCode);//品牌商代号
        map.put("AgentCode", tAgentCode);//代理商代号
        para.add(map);
//        Log.d("main-","GetScsCcsBindStatus-"+para.toString());
        String result = instance.GetAPIStringInterface("UpDownLink/GetScsCcsBindStatus", para);
//        Log.d("main-","GetScsCcsBindStatus-"+result);
        JSONObject listjson = new JSONObject(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("NeedBind", listjson.optBoolean("needBind"));//是否绑定
        map1.put("BrandingCode", listjson.optString("brandingCode"));
        map1.put("AgentCode", listjson.optString("agentCode"));
        map1.put("IsSendToStore", listjson.optBoolean("isSendToStore"));//是否发到门店

        map1.put("CustSysCode", listjson.optString("custSysCode"));//scs客户系统代号
        map1.put("StoreSysCode", listjson.optString("storeSysCode"));//scs门店系统代号

        map1.put("TraderAlias", listjson.optString("businessName"));//客户别名
        map1.put("StoreAlias", listjson.optString("storeBusinessName"));//门店别名
        map1.put("CcsCustName", listjson.optString("ccsCustName"));//CCS绑定成功的客户名称
        map1.put("CcsStoreName", listjson.optString("ccsStoreName"));//CCS绑定成功的门店名称

        map1.put("BrandingCustCode", listjson.optString("ccsCustCode"));//CCS绑定成功的客户代号
        map1.put("BrandingStoreCode", listjson.optString("ccsStoreCode"));//CCS绑定成功的门店代号

        map1.put("CustomerName",listjson.optString("customerName"));//用于搜索客户
        map1.put("StoreName", listjson.optString("storeName"));//用于搜索门店

        list.add(map1);
        return list;
    }

//    /**
//     * SCS客户和CCS客户绑定
//     */
//    public String ScsCustBindToCcs(String tCustSysCode, String tCcsCustCode,String tBrandingCode,String tAgentCode) throws Exception {
//        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
//        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
//        map.put("tCustSysCode", tCustSysCode);//客户系统代号
//        map.put("tCcsCustCode", tCcsCustCode);//ccs客户代号
//        map.put("tBrandingCode", tBrandingCode);//ccs品牌商代号
//        map.put("tAgentCode", tAgentCode);//ccs代理商代号
//        para.add(map);
////        Log.d("main","ScsCustBindToCcs"+para.toString());
//
//        String result = getWebResult("ScsCustBindToCcs", para);
//        return result;
//    }

//    /**
//     * SCS客户和CCS客户绑定
//     */
    public String ScsCustBindToCcs(String tCustSysCode, String tStoreSysCode,String tCcsCustCode, String tCcsStoreCode,String tBrandingCode,String tAgentCode) throws Exception {
//        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
//        HashMap<Object, Object> map = new HashMap<Object, Object>();
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("CustSysCode", tCustSysCode);//SCS客户系统代号
        requestParams.put("CcsCustCode", tCcsCustCode);//ccs客户代号
        requestParams.put("StoreSysCode", tStoreSysCode);//SCS 门店系统编码；BrandingInfor.IsSendToStore 为 true 时必填
        requestParams.put("CcsStoreCode", tCcsStoreCode);//CCS 门店代号（BrandingStoreCode）；BrandingInfor.IsSendToStore 为 true 时必填

        requestParams.put("BrandingCode", tBrandingCode);//ccs品牌商代号
        requestParams.put("AgentCode", tAgentCode);//ccs代理商代号
//        para.add(map);

        Gson gson=new Gson();
//        String result = getWebResult("ScsCustBindToCcs", para);
//        Log.d("main-","ScsBindToCcs-"+gson.toJson(requestParams));
        String result = instance.PostAPIStringInterface("UpDownLink/ScsBindToCcs", gson.toJson(requestParams));
//        Log.d("main-",result);
        return result;
    }


//    /**
//     * SCS门店和CCS门店绑定
//     */
//    public String ScsStoreBindToCcs(String tCustSysCode,String tStoreSysCode, String tCcsCustCode,String tCcsStoreCode,String tBrandingCode,String tAgentCode) throws Exception {
//        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
//        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
//        map.put("tCustSysCode", tCustSysCode);//客户系统代号
//        map.put("tStoreSysCode", tStoreSysCode);//门店系统代号
//        map.put("tCcsCustCode", tCcsCustCode);//ccs客户代号
//        map.put("tCcsStoreCode", tCcsStoreCode);//ccs门店代号
//        map.put("tBrandingCode", tBrandingCode);//ccs品牌商代号
//        map.put("tAgentCode", tAgentCode);//ccs代理商代号
//        para.add(map);
////        Log.d("main","ScsStoreBindToCcs"+para.toString());
//
//        String result = getWebResult("ScsStoreBindToCcs", para);
//        return result;
//    }


    /**
     * SCS新客户同步CCS并绑定 已停用
     */
    public String ScsNewCustBindToCcs(String tCustSysCode,String tBrandingCode,String tAgentCode,String tTraderAlias) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCustSysCode", tCustSysCode);//scs客户系统代号
        map.put("tBrandingCode", tBrandingCode);//ccs品牌商代号
        map.put("tAgentCode", tAgentCode);//ccs代理商代号
        map.put("tTraderAlias", tTraderAlias);//scs客户别名
        para.add(map);
//        Log.d("main","ScsNewCustBindToCcs"+para.toString());
        String result = getWebResult("ScsNewCustBindToCcs", para);
        return result;
    }


    /**
     * SCS新门店同步CCS并绑定 已停用
     */
    public String ScsNewStoreBindToCcs(String tStoreSysCode,String tBrandingCode,String tAgentCode,String tAlias) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tStoreSysCode", tStoreSysCode);//scs客户系统代号
        map.put("tBrandingCode", tBrandingCode);//ccs品牌商代号
        map.put("tAgentCode", tAgentCode);//ccs代理商代号
        map.put("tAlias", tAlias);//ccs代理商代号
        para.add(map);
//        Log.d("main","ScsNewStoreBindToCcs"+para.toString());
        String result = getWebResult("ScsNewStoreBindToCcs", para);
        return result;
    }



    /**
     * 同步SCS数据到CCS并绑定 改为2.0版本
     */
    public String ScsNewSyncToCcs(String tBrandingCode,String tAgentCode,String tCustSysCode,String tStoreSysCode,String tCustJson,String tStoreJson) throws Exception {
//        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
//        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", sysUserInfo.getLoginid());
//        map.put("tBrandingCode", tBrandingCode);//ccs品牌商代号
//        map.put("tAgentCode", tAgentCode);//ccs代理商代号
//        map.put("tCustJson", tCustJson);//客户json
//        map.put("tStoreJson", tStoreJson);//门店json
//        para.add(map);
////        Log.d("main","ScsNewSyncToCcs"+para.toString());
//        String result = getWebResult("ScsNewSyncToCcs", para);
//        return result;
        Gson gson=new Gson();
        SyncCustomers customers = gson.fromJson(tCustJson, SyncCustomers.class); // 转成对象
        SyncStores syncStores= null;
        if (tStoreJson != null && !tStoreJson.trim().isEmpty()) {
            syncStores = gson.fromJson(tStoreJson, SyncStores.class);
        } else {
            // 处理空字符串的情况，比如给个默认值
            syncStores = new SyncStores(); // 或者 null
        }

        Map<String, Object> requestParams = new HashMap<>();

        requestParams.put("BrandingCode", tBrandingCode);//ccs品牌商代号
        requestParams.put("AgentCode", tAgentCode);//ccs代理商代号
        requestParams.put("CustSysCode", tCustSysCode);//SCS 客户系统编码
        requestParams.put("StoreSysCode", tStoreSysCode);//SCS 门店系统编码；BrandingInfor.IsSendToStore 为 true 时必填
//        requestParams.put("StoreType", tStoreType);//门店类型：总店 / 分销店；为空时按 SCS 门店资料 StoreType 转换 后台说可以先不传
        requestParams.put("SaleId", "");//业务员代号 默认先传空
        requestParams.put("SaleName", "");//业务员名称 默认先传空

        requestParams.put("EditMode", customers.getEditMode());//编辑状态：A-新增；M-修改
        requestParams.put("CutCode", tCustSysCode);//客户代号（同步至 CCS 时使用 SCS 客户系统编码 CustSysCode）
        requestParams.put("CustName", customers.getCustName());//客户名称
        requestParams.put("CustLink", customers.getCustLink());//客户联系人
        requestParams.put("CustTel", customers.getCustTel());//客户联系电话
        requestParams.put("CustMobile", customers.getCustMobile());//客户手机号码
        requestParams.put("BrandName", customers.getBrandName());//品牌名称
        requestParams.put("ProviceName", customers.getProviceName());//客户省份名称
        requestParams.put("CityName", customers.getCityName());//客户城市名称
        requestParams.put("CountyName", customers.getCountyName());//客户区县名称
        requestParams.put("CustAddr", customers.getCustAddr());//客户地址
        requestParams.put("LicenceNo", customers.getLicenceNo());//客户营业执照号
        requestParams.put("CorpName", customers.getCorpName());//客户企业名称
        requestParams.put("CcsCustId", customers.getCcsCustId());//CCS客户ID

        requestParams.put("StoreCcsStoreId", syncStores.getCcsStoreId());//CCS 门店 ID（同步门店时使用）
        requestParams.put("StoreEditMode", syncStores.getEditMode());//门店编辑状态：A-新增；M-修改；为空时沿用客户 EditMode
        requestParams.put("StoreName", syncStores.getStoreName());//门店名称
        requestParams.put("StoreLink", syncStores.getCustLink());//门店联系人（同步门店时使用）
        requestParams.put("StoreMobile", syncStores.getCustMobile());//门店联系电话（同步门店时使用）
        requestParams.put("StoreBrandName", syncStores.getBrandName());//门店品牌名称（同步门店时使用）
        requestParams.put("StoreProviceName", syncStores.getProviceName());//门店省份名称（同步门店时使用）
        requestParams.put("StoreCityName", syncStores.getCityName());//门店城市名称（同步门店时使用）
        requestParams.put("StoreCountyName", syncStores.getCountyName());//门店区县名称（同步门店时使用）
        requestParams.put("StoreAddr", syncStores.getCustAddr());//门店门店地址名称（同步门店时使用）
        requestParams.put("StoreLicenceNo", syncStores.getLicenceNo());//门店营业执照（同步门店时使用）
        requestParams.put("StoreCorpName", syncStores.getCorpName());//门店企业名称（同步门店时使用）

//        Log.d("main","ScsNewSyncToCcss-"+gson.toJson(requestParams));
        String result = instance.PostAPIStringInterface("UpDownLink/ScsNewSyncToCcs", gson.toJson(requestParams));
//        Log.d("main-",result);
        return result;
    }


    /**
     * 获取SCS具体客户和门店信息（用于同步信息展示）
     */
    public String GetScsCustStore(String tStoreSysCode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tStoreSysCode", tStoreSysCode);//门店系统代号
        para.add(map);
//        Log.d("main","GetScsCustStore"+para.toString());
        String result = getWebResult("GetScsCustStore", para);
//        JSONObject listjson = new JSONObject(result);
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        JSONArray jsonArray = listjson.getJSONArray("Data");
//        for (int i = 0; i <jsonArray.length(); i++) {
//            Map<String, Object> map1 = new HashMap<String, Object>();
//            map1.put("StoreId", jsonArray.getJSONObject(i).getBoolean("StoreId"));//门店大海
//            map1.put("BrandingCode", jsonArray.getJSONObject(i).getString("BrandingCode"));
//            map1.put("AgentCode", jsonArray.getJSONObject(i).getString("AgentCode"));
//            map1.put("CustBind", jsonArray.getJSONObject(i).getBoolean("CustBind"));//CCS客户是否绑定
//            map1.put("StoreBind", jsonArray.getJSONObject(i).getBoolean("StoreBind"));//CCS门店是否绑定
//            map1.put("IsSendToStore", jsonArray.getJSONObject(i).getBoolean("IsSendToStore"));//是否发到门店
//
//            map1.put("CustSysCode", jsonArray.getJSONObject(i).getString("CustSysCode"));
//            map1.put("StoreSysCode", jsonArray.getJSONObject(i).getString("StoreSysCode"));
//            map1.put("BillNo", jsonArray.getJSONObject(i).getString("BillNo"));
//            map1.put("BrandCode", jsonArray.getJSONObject(i).getString("BrandCode"));
//            map1.put("BrandName", jsonArray.getJSONObject(i).getString("BrandName"));
//
//            map1.put("TraderAlias", jsonArray.getJSONObject(i).getString("TraderAlias"));
//            map1.put("StoreAlias", jsonArray.getJSONObject(i).getString("Alias"));
//
//            list.add(map1);
//        }
        return result;
    }


    /**
     * 获取SCS省份
     */
    public String GetScsProvice() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tAreaName", "");//区域 空是全国
        para.add(map);
        String result = getWebResult("GetScsProvice", para);
        return result;
    }
    /**
     * 获取SCS城市
     */
    public String GetScsCity(String tProviceName) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tProviceName", tProviceName);//省份
        para.add(map);
//        Log.d("main","GetScsCity"+para.toString());
        String result = getWebResult("GetScsCity", para);
        return result;
    }


    /**
     * 退货选择客户扫码参数
     */
    public List<Map<String, Object>> P_Dv_SeekCompanyInfo(String tCompanyType, String tBarcode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCompanyType", tCompanyType);
        map.put("tBarcode", tBarcode);
        para.add(map);
        String result = getWebResult("P_Dv_SeekCompanyInfo", para);
        JSONObject listjson = new JSONObject(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            //			JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PId", listjson.getString("PId"));
            map1.put("CompanyId", listjson.getString("CId"));
            map1.put("CompanyName", listjson.getString("CName"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载入库退出明细函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */
    public List<Map<String, Object>> GetDowLoadPurOutDetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadPurOutDetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoOutGoodQty"));
            map1.put("amount", jsonObject2.getString("NoOutGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载仓库调拨单表头函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */
    public List<Map<String, Object>> GetDowLoadAllots() throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAllots", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("AllotLno", jsonObject2.getString("AllotLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("OutStockName", jsonObject2.getString("OutStockName"));
            map1.put("InStockName", jsonObject2.getString("InStockName"));
            map1.put("NoAllotQty", jsonObject2.getString("NoAllotQty"));
            map1.put("OutStockId", jsonObject2.getString("OutStockId"));
            map1.put("InStockId", jsonObject2.getString("InStockId"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 下载仓库调拨单表头函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */
    public List<Map<String, Object>> GetDowLoadSCSAllots(String tSearchWord, int tPageIndex) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSearchWord", tSearchWord);
        map.put("tPageIndex", tPageIndex);
        map.put("tPageNum", 200);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAllots", para);
//        Log.d("main", "GetDowLoadSCSAllots: "+result);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("AllotLno", jsonObject2.getString("AllotLno"));
            map1.put("SapLno", jsonObject2.getString("SapLno"));
            map1.put("OutStockName", jsonObject2.getString("OutStockName"));
            map1.put("InStockName", jsonObject2.getString("InStockName"));
            map1.put("NoAllotQty", jsonObject2.getString("NoAllotQty"));
            map1.put("OutStockId", jsonObject2.getString("OutStockId"));
            map1.put("InStockId", jsonObject2.getString("InStockId"));
            list.add(map1);
        }
        return list;
    }

    /**
     * 下载入库退出明细函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */
    public List<Map<String, Object>> GetDowLoadAllotsdetail(String tBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadAllotsdetail", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("goodsid", jsonObject2.getString("GoodsId"));
            map1.put("goodsdescription", jsonObject2.getString("GoodsDescription"));
            map1.put("modelm", jsonObject2.getString("Modelm"));
            map1.put("colors", jsonObject2.getString("Colors"));
            map1.put("noscanqty", jsonObject2.getString("NoAllotGoodQty"));
            map1.put("amount", jsonObject2.getString("NoAllotGoodQty"));//增加一个字段
            list.add(map1);
        }
        return list;
    }



    /**
     * 用户登录函数
     */
    public String UserLogin(String pUsercode, String pPsw, String AccountSetId) throws Exception {
//        Log.d("main",AccountSetId);
        String result = "";
        // SOAP Action
        String SOAP_ACTION = this.namespace + "UserLogin";//
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(this.namespace, "UserLogin");
        // 设置需调用WebService接口需要传入的参数
        rpc.addProperty("tUsercode", pUsercode);
        rpc.addProperty("tPsw", pPsw);
        rpc.addProperty("tAccountSetId", AccountSetId);
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);

        // HttpTransportSE transport = new HttpTransportSE(this.webservice_url);
        MyAndroidHttpTransport transport = new MyAndroidHttpTransport(this.webservice_url,
                5000);//5秒
//        Log.d("main",this.webservice_url);
        try {
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
//            SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
//            Log.d("main",result);
        } catch (IOException e) {
            throw new Exception("服务器:（IOException网络超时）" + e);
        } catch (Exception e) {
            throw new Exception("服务器:服务器连接失败" + e.getMessage());
        }

        if (result.isEmpty()) {
            throw new Exception("网络超时");
        }
        return result;
    }


    private String getWebResult(String methodName, ArrayList<HashMap<Object, Object>> propertys) throws Exception {

        if (!CheckNetWorkStatus()) {
            throw new Exception("当前网络不可用，请检查设置！");
        }
        String result = "";
        try {
            SoapObject rpc = null;
            rpc = new SoapObject(this.namespace, methodName);
            for (HashMap<Object, Object> info : propertys) {
                Set<Object> set = info.keySet();
                Iterator<Object> iterator = set.iterator();

                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    rpc.addProperty(key, info.get(key));
                }
            }

            String SOAP_ACTION = rpc.getNamespace() + rpc.getName();

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            // HttpTransportSE transport = new
            // HttpTransportSE(this.webservice_url);
//            Log.d("main", this.webservice_url);
            MyAndroidHttpTransport transport;
            transport = new MyAndroidHttpTransport(this.webservice_url, 5000); // set timeout 5s

            transport.debug = true;

            try {
                // 调用WebService
                //				long beforeTime = System.currentTimeMillis();
                transport.call(SOAP_ACTION, envelope);
                //				if(!methodName.equals("Connectline"))
                //				{
                //					//					Log.i("main", methodName+"---请求用时---"+
                //					(System.currentTimeMillis()-beforeTime));
                //				}
                SoapObject object = (SoapObject) envelope.bodyIn;
//				SoapObject object = (SoapObject) envelope.getResponse();

//                int code = Integer.parseInt(object.getProperty("code").toString());

                // 获取返回的结果
                result = object.getProperty(0).toString();

//                Log.d("main--",result);
            } catch (IOException e) {
                throw new Exception("服务器连接超时" + e.getMessage());
            } catch (Exception e) {
                throw e;
            }
            if (result == null || result == "") {
                throw new Exception("网络超时");
            }

            if (result.split(";").length < 2) {
                throw new Exception("发生错误：返回的值格式不正确.\r\n" + result);
            }

            if (result.split(";")[0].equalsIgnoreCase("true")) {
                //如果用户在新增资料时填写的内容有“;”就会造成result被截成多个，要返回后面的全部
                if (result.split(";").length > 2) {
                    String res = "";
                    for (int i = 1; i < result.split(";").length; i++) {
                        res += result.split(";")[i];
                    }
                    return res;
                }
                return result.split(";")[1];
            } else {
                String[] parts = result.split(";", -1); // 保留末尾空字符串
                if (parts.length < 2) {
//                    throw new IllegalStateException("结果格式非法，缺少第二部分：" + result);
                    throw new Exception("发生错误：结果格式非法，缺少报错提示：" + result);
                }
                if (parts[1].equals("")){
                    throw new Exception("发生错误：结果格式非法，缺少报错提示：" + result);
                }
                throw new Exception("服务器：" + parts[1]);
            }
        } catch (Exception e) {
            throw new Exception("发生错误：" + e.getMessage());
        }

    }

    /**
     * 下载服务
     *
     * @param methodName
     * @param propertys
     * @return
     * @throws Exception
     */
    private String downLoadWebResult(String methodName,
                                     ArrayList<HashMap<Object, Object>> propertys) throws Exception {

        if (!CheckNetWorkStatus()) {
            throw new Exception("当前网络不可用，请检查设置！");
        }
        String result = "";
        try {
            SoapObject rpc = null;
            rpc = new SoapObject(this.namespace, methodName);
            for (HashMap<Object, Object> info : propertys) {
                Set<Object> set = info.keySet();
                Iterator<Object> iterator = set.iterator();

                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    rpc.addProperty(key, info.get(key));
                }
            }

            String SOAP_ACTION = rpc.getNamespace() + rpc.getName();

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            // HttpTransportSE transport = new
            // HttpTransportSE(this.webservice_url);
            MyAndroidHttpTransport transport;
            transport = new MyAndroidHttpTransport(this.webdownload_url, 5000); // set timeout 5s

            transport.debug = true;

            try {
                // 调用WebService
                transport.call(SOAP_ACTION, envelope);
                SoapObject object = (SoapObject) envelope.bodyIn;
                //				SoapObject object = (SoapObject) envelope.getResponse();
                // 获取返回的结果
                result = object.getProperty(0).toString();


            } catch (IOException e) {
                throw new Exception("服务器连接超时" + e.getMessage());
            } catch (Exception e) {
                throw new Exception("服务器:" + e);
            }
            if (result == null || result == "") {
                throw new Exception("网络超时");
            }
            if (result.split(";").length < 2) {
                throw new Exception("本地处理:返回的值格式不正确.\r\n" + result);
            }

            //
            if (result.split(";")[0].equalsIgnoreCase("true")) {
                //如果用户在新增资料时填写的内容有“;”就会造成result被截成多个，要返回后面的全部
                if (result.split(";").length > 2) {
                    String res = "";
                    for (int i = 1; i < result.split(";").length; i++) {
                        res += result.split(";")[i];
                    }
                    return res;
                }
                //
                return result.split(";")[1];
            } else {
                throw new Exception("服务器：" + result.split(";")[1]);
            }
        } catch (Exception e) {
            throw new Exception("服务器：" + e.getMessage());
        }

    }

    // 检查3G网络和WiFi的，在这个项目中不需要
    public Boolean CheckNetWorkStatus() {

        boolean netSataus = false;

        ConnectivityManager cwjManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cwjManager.getActiveNetworkInfo() != null) {
            netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
        }

        return netSataus;
    }


    /**
     * 获取菜单函数
     * GetDevMenuInfor
     *
     * @param tBusinessId 品牌代号
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDevMenuInfor(String tBusinessId) throws Exception {
        String result = "";
        // SOAP Action
        String SOAP_ACTION = "http://tempuri.org/GetDevMenuInfor";
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject("http://tempuri.org/", "GetDevMenuInfor");
        // 设置需调用WebService接口需要传入的参数
        rpc.addProperty("tBusinessId", tBusinessId);
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE("http://www.4006889521" +
                ".cn:9516/softreg/WebRegService.asmx");
        // 调用WebService
        transport.call(SOAP_ACTION, envelope);
        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        //			SoapObject object = (SoapObject) envelope.getResponse();
        // 获取返回的结果
        result = object.getProperty(0).toString();

        if (result.split(";")[0].equals("true")) {
            JSONArray jsonArray = new JSONArray(result.split(";")[1]);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> map;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
                map = new HashMap<String, Object>();
                map.put("menucode", jsonObject.getString("MenuCode"));//菜单编号
                map.put("menuname", jsonObject.getString("MenuName"));//菜单名称
                map.put("parentcode", jsonObject.getString("ParentCode"));//父级编号
                map.put("showstatus", jsonObject.getString("ShowStatus"));//菜单显示状态，1显示，0不显示
                map.put("procedurename", jsonObject.getString("ProcedureName"));//存储过程名称

                list.add(map);
            }
            return list;
        } else {
            throw new Exception("服务器：" + result.split(";")[1]);
        }

    }

    /**
     * 判断版本第四代还是第五代
     * 返回True是第五代、返回false是第四代
     *
     * @param tBrandNessCode 品牌代号
     * @return
     * @throws Exception
     */
    public boolean JudgeBrandNessVer(String tBrandNessCode) {
        try {
            String result = "";
            // SOAP Action
            String SOAP_ACTION = "http://tempuri.org/JudgeBrandNessVer";
            // 指定WebService的命名空间和调用的方法名
            SoapObject rpc = new SoapObject("http://tempuri.org/", "JudgeBrandNessVer");
            // 设置需调用WebService接口需要传入的参数
            rpc.addProperty("tBrandNessCode", tBrandNessCode);
            // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            HttpTransportSE transport = new HttpTransportSE("http://www.4006889521" +
                    ".cn:9516/softreg/WebRegService.asmx");
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //			SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString().trim();
//            Log.d("mian","版本号"+Boolean.parseBoolean(result.split(";")[0]));
            return Boolean.parseBoolean(result.split(";")[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }


    /**
     * 判断当前品牌商是否有首发发货功能
     * GettFirstDeliveryStatus
     *
     * @param tBrandNessCode 品牌代号
     * @return
     * @throws Exception
     */
    public String GettFirstDeliveryStatus(String tBrandNessCode) throws Exception {
        try {
            String result = "";
            // SOAP Action
            String SOAP_ACTION = "http://tempuri.org/GettFirstDeliveryStatus";
            // 指定WebService的命名空间和调用的方法名
            SoapObject rpc = new SoapObject("http://tempuri.org/", "GettFirstDeliveryStatus");
            // 设置需调用WebService接口需要传入的参数
            rpc.addProperty("tBrandNessCode", tBrandNessCode);
            // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            envelope.bodyOut = rpc;
            // 设置是否调用的是dotNet开发的WebService
            envelope.dotNet = true;
            // 等价于envelope.bodyOut = rpc;
            envelope.setOutputSoapObject(rpc);
            HttpTransportSE transport = new HttpTransportSE("http://www.4006889521" +
                    ".cn:9516/softreg/WebRegService.asmx");
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //			SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
//            Log.d("main",result);
            if (result == null || result == "") {
                throw new Exception("网络超时");
            }
//            Log.d("main",result.split(";")[0]);
            if (result.split(";").length < 1) {
                throw new Exception("本地处理:返回的值格式不正确.\r\n" + result);
            }

            if (result.split(";")[0].equalsIgnoreCase("true")) {
                //如果用户在新增资料时填写的内容有“;”就会造成result被截成多个，要返回后面的全部
                if (result.split(";").length > 2) {
                    String res = "";
                    for (int i = 1; i < result.split(";").length; i++) {
                        res += result.split(";")[i];
                    }
                    return res;
                }
                return result.split(";")[0];
            } else {
                throw new Exception("服务器：" + result.split(";")[1]);
            }

        } catch (Exception e) {
            throw new Exception("服务器:" + e);
        }
    }


    /**
     * 代理商发货通添加品牌验证服务
     *
     * @param tAccreditCode 品牌验证码
     * @param tEsn          设备序列号
     * @throws Exception
     */
    public Map<String, Object> AccreditCodeLeadingV2(String tAccreditCode, String tEsn) throws Exception {
        String result = "";
        // SOAP Action
        String SOAP_ACTION = "http://tempuri.org/AccreditCodeLeadingV2";
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject("http://tempuri.org/", "AccreditCodeLeadingV2");
        // 设置需调用WebService接口需要传入的参数
        rpc.addProperty("tAccreditCode", tAccreditCode);
        rpc.addProperty("tEsn", tEsn);

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;

        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);
        //				HttpTransportSE transport = new HttpTransportSE(
        //						"http://www.4006889521.cn:9516/softreg/WebRegService.asmx");
        MyAndroidHttpTransport transport = new MyAndroidHttpTransport("http://www.4006889521" +
                ".cn:9516/softreg/WebRegService.asmx", 5000);//5秒
        try {
            // 调用WebService
            transport.call(SOAP_ACTION, envelope);
            // 获取返回的数据
            SoapObject object = (SoapObject) envelope.bodyIn;
            //SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
        } catch (IOException e) {
            throw new Exception("服务器连接超时" + e.getMessage());
        } catch (Exception e) {
            throw new Exception("服务器:" + e.getCause());
        }
        if (result == null || result == "") {
            throw new Exception("网络超时");
        }
        if (result.split(";")[0].equalsIgnoreCase("true")) {
            result = result.split(";")[1];
            JSONArray listjson = new JSONArray(result);
            Map<String, Object> map1 = new HashMap<String, Object>();
            for (int i = 0; i < listjson.length(); i++) {
                JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                map1.put("businessid", jsonObject2.getString("BusinessId"));
                map1.put("brandname", jsonObject2.getString("BrandName"));
                map1.put("serverccip", jsonObject2.getString("ServerCcIp"));
                map1.put("servernetip", jsonObject2.getString("ServerNetIp"));
                map1.put("port", jsonObject2.getString("Port"));
                map1.put("logurl", jsonObject2.getString("LogUrl"));
            }
            return (Map<String, Object>) map1;
        } else {
            throw new Exception("服务器：" + result.split(";")[1]);
        }
    }


    /**
     * 代理商给零售商发货
     */

    public String P_Dv_OutStock_D_L_NoBill(String barcode, String pSoCompid, String pCompid,
                                           String nSN, String Scanbillno, String mBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("pBarcode", barcode);
        map.put("pSoCompid", pSoCompid);
        map.put("pCompid", pCompid);
        map.put("pUserId", sysUserInfo.getUserid());
        map.put("pSN", nSN);
        map.put("pScanBillNo", Scanbillno);
        map.put("mBillNo", mBillNo);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_L_NoBill", para);
        return data;
    }


    /**
     * 代理商给零售商发货  首发
     * <p>
     * tLoginId：用户登录成功的LogId
     * tWebId：当前扫描条码
     * tPara：Josn字符串
     * Barcode：扫描的条码(必传)
     * SoCompId：代理商代号(必传)
     * DeCompId：零售商代号(必传)
     * OaSuserId：扫描人员代号(必传)
     * ScanSn：扫描序号(必传)
     * ScanBillNo：扫描单号(必传)
     * BillNo：发货单号(首次扫码传空，成功再扫码时传返回的发货单号)
     * FirstDelivery：是否首次发货(0-非首次；1-首次)
     */

    public String P_Dv_OutStock_D_L_NoBillv1(String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_L_NoBillv1", para);
        return data;
    }


    /**
     * 代理商给分销店发货
     * <p>
     * tLoginId：用户登录成功的LogId
     * tWebId：当前扫描条码
     * tPara：Josn字符串
     * Barcode：扫描的条码(必传)
     * SoCompId：代理商代号(必传)
     * DeCompId：零售商代号(必传)
     * StoreId： 分店代号
     * OaSuserId：扫描人员代号(必传)
     * ScanSn：扫描序号(必传)
     * ScanBillNo：扫描单号(必传)
     * BillNo：发货到零售商单号(首次扫码传空，成功再扫码时传返回的发货单号)
     * DocumentNo：发货到分销店单号(首次扫码传空，成功再扫码时传返回的发货单号)
     * FirstDelivery：是否首次发货(0-非首次；1-首次)
     */

    public String P_Dv_OutStock_D_S_NoBillv1(String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_S_NoBillv1", para);
        return data;
    }


    /**
     * 代理商给分销店发货
     * tLoginId：用户登录成功的LogId
     * tWebId：当前扫描条码
     * pBarcode：扫描的条码(必传)
     * pSoCompid：代理商代号(必传)
     * pCompid：零售商代号(必传)
     * pShopCompid：分销店代号(必传)
     * pUserId：扫描人员代号(必传)
     * pSN：扫描序号(必传)
     * pScanBillNo：扫描单号(必传)
     * pBillNo：零售商发货单号(首次传空，其他情况传返回的发货单号)
     * pShopBillNo：分销店发货单号(首次传空，其他情况传返回的发货单号)
     */

    public String P_Dv_OutStock_D_S_NoBill(String barcode, String pSoCompid, String pCompid,
                                           String pShopCompid, String nSN, String Scanbillno, String pBillNo, String pShopBillNo) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("pBarcode", barcode);
        map.put("pSoCompid", pSoCompid);
        map.put("pCompid", pCompid);
        map.put("pShopCompid", pShopCompid);
        map.put("pUserId", sysUserInfo.getUserid());
        map.put("pSN", nSN);
        map.put("pScanBillNo", Scanbillno);
        map.put("pBillNo", pBillNo);
        map.put("pShopBillNo", pShopBillNo);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_D_S_NoBill", para);
        return data;
    }

    /**
     * 零售商给代理商退货
     * tLoginId：用户登录成功的LogId
     * tWebId：当前扫描条码
     * pBarcode：扫描的条码(必传)
     * pSoCompid：代理商代号(必传)
     * pCompid：零售商代号(不传)
     * pUserId：扫描人员代号(必传)
     * pSN：扫描序号(必传)
     * pScanBillNo：扫描单号(必传)
     * mBillNo：退货单号(首次传空，其他情况传返回的退货单号)
     */
    public String P_Dv_ReturnedPurchase_D_L_NoBill(String barcode, String pSoCompid,
                                                   String pCompid, String nSN, String Scanbillno, String mBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("pBarcode", barcode);
        map.put("pSoCompid", pSoCompid);
        map.put("pCompid", pCompid);
        map.put("pUserId", sysUserInfo.getUserid());
        map.put("pSN", nSN);
        map.put("pScanBillNo", Scanbillno);
        map.put("mBillNo", mBillNo);
        para.add(map);
        String data = getWebResult("P_Dv_ReturnedPurchase_D_L_NoBill", para);
        return data;
    }

    /**
     * 创建总公司装盒入库任务
     *
     * @param tLoginId
     * @param tWebId
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_CreatePackingJob(String tLoginId, String tWebId, String tPara) throws Exception {
        String result = "";
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tWebId", tWebId);
        map.put("tPara", tPara);
        para.add(map);
        result = getWebResult("P_Dv_InStock_CreatePackingJob", para);

        return result;
    }

    /**
     * 总公司装盒创建临时盒标(没用
     *
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_NewTempBoxNo(String tLoginId, String tBillNo) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tBillNo", tBillNo);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_NewTempBoxNo", para);
        return result;
    }


    /**
     * 总公司装盒检查条码状态(没用
     *
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_PackBarcodeState(String tLoginId, String tTempBoxNo,
                                                String tBarCode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tTempBoxNo", tTempBoxNo);
        map.put("tBarCode", tBarCode);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBarcodeState", para);
        return result;
    }

    /**
     * 创建总公司装盒入库(没用
     *
     * @return
     * @throws Exception
     */
    public String P_Dv_InStock_PackBox_NoBill(String tLoginId, String tWebId, String tTempBoxNo,
                                              String tProduct_id, int tNum) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tWebId", tWebId);
        map.put("tTempBoxNo", tTempBoxNo);
        map.put("tProduct_id", tProduct_id);
        map.put("tNum", tNum);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBox_NoBill", para);
        return result;
    }

    /**
     * 装盒入库
     *
     * @param tLoginId
     * @param tWebId
     * @param tBarCodes
     * @param tProduct_id
     * @param tNum
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-18 下午4:11:57
     */
    public String P_Dv_InStock_PackBox_List_NoBill(String tLoginId, String tWebId,
                                                   String tBarCodes, String tProduct_id, int tNum) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tWebId", tWebId);
        map.put("tBarCodes", tBarCodes);
        map.put("tProduct_id", tProduct_id);
        map.put("tNum", tNum);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBox_List_NoBill", para);
        return result;
    }

    /**
     * 查找盒标
     *
     * @param tLoginId
     * @param tTempBoxNo 条码或盒标
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2018-1-18 下午4:12:15
     */
    public String P_Dv_InStock_PackBox_Search(String tLoginId, String tTempBoxNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tTempBoxNo", tTempBoxNo);
        para.add(map);
        String result = getWebResult("P_Dv_InStock_PackBox_Search", para);
        return result;
    }


    /**
     * 查看单据明细
     *
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadBilldetail(String tLoginId, String tScanBillNo) throws Exception {
//        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
//        HashMap<Object, Object> map = new HashMap<Object, Object>();
//        map.put("tLoginId", tLoginId);
//        map.put("tScanBillNo", tScanBillNo);
//        para.add(map);
//        String result = downLoadWebResult("GetDowLoadBilldetail", para);
//        JSONArray jsonArray = new JSONArray(result);
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        Map<String, Object> maps;
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
//            maps = new HashMap<String, Object>();
//            maps.put("goodsid", jsonObject.getString("GoodsId"));
//            maps.put("modelm", jsonObject.getString("Modelm"));
//            maps.put("colors", jsonObject.getString("Colors"));
//            maps.put("curcount", jsonObject.getString("Num"));
//            maps.put("scandate", jsonObject.getString("ScanDate"));
//            maps.put("brandname", jsonObject.getString("BrandName"));
//            list.add(maps);
//        }
//        return list;

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> requestParams = new HashMap<Object, Object>();
        requestParams.put("ScanBillNo", tScanBillNo);//扫描单号
        para.add(requestParams);
        Gson gson=new Gson();
        String  result =GetAPIStringInterface("AndroidDv/GetBillScanDetail", para);

        JSONArray listjson =new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> maps;
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            maps = new HashMap<String, Object>();
            maps.put("goodsid", jsonObject2.optString("goodsCode"));
            maps.put("modelm", jsonObject2.optString("modelm"));
            maps.put("colors", jsonObject2.optString("colors"));
            maps.put("curcount", jsonObject2.optString("num"));
            maps.put("scandate", jsonObject2.optString("scanDate"));
            maps.put("brandname", jsonObject2.optString("brandName"));
            list.add(maps);
        }
        return list;
    }

    /**
     * 查看镜片单据明细
     *
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetDowLoadLensScanBarcode(String tLoginId,
                                                               String tScanBillNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", tLoginId);
        map.put("tScanBillNo", tScanBillNo);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadLensScanBarcode", para);
//        Log.d("main",result);
        JSONArray jsonArray = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> maps;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
            maps = new HashMap<String, Object>();
            maps.put("GoodsSysCode", jsonObject.getString("GoodsSysCode"));
            maps.put("GoodsCode", jsonObject.getString("GoodsCode"));
            maps.put("RefractiveIndex", jsonObject.getString("RefractiveIndex"));
            maps.put("Diopter", jsonObject.getString("Diopter"));
            maps.put("Astigmatism", jsonObject.getString("Astigmatism"));
            maps.put("Num", jsonObject.getString("Num"));
            maps.put("Scandate", jsonObject.getString("Scandate"));
            maps.put("BrandName", jsonObject.getString("BrandName"));
            list.add(maps);
        }
        return list;
    }


    /**
     * 获取仓库补标单号
     *
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetMendLableNo(String tSuppSysCode, String tStockSysCode) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tSuppSysCode", tSuppSysCode);//供应商系统代号
        map.put("tStockSysCode", tStockSysCode);//仓库系统代号
        para.add(map);
        String result = downLoadWebResult("GetMendLableNo", para);
//        Log.d("main", result);
        JSONArray jsonArray = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> maps;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
            maps = new HashMap<String, Object>();
            maps.put("MendLableNo", jsonObject.getString("MendLableNo"));//补标单号
            maps.put("MendLableNum", jsonObject.getString("MendLableNum"));//补标数量
            list.add(maps);
        }
        return list;
    }


    /**
     * 获取仓库补标明细
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> GetMendLableDetail(String tMendLableNo) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tMendLableNo", tMendLableNo);//补标单号
        para.add(map);
//        Log.d("main","补标明细："+para.toString());
        String result = downLoadWebResult("GetMendLableDetail", para);
//        Log.d("main", result);
        JSONArray jsonArray = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> maps;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
            maps = new HashMap<String, Object>();
            maps.put("GoodsCode", jsonObject.getString("GoodsCode"));//产品id
            maps.put("BrandName", jsonObject.getString("BrandName"));//品牌名称
            maps.put("Specifications", jsonObject.getString("Specifications"));//描述
            maps.put("Num", jsonObject.getString("Num"));//补标数量
            list.add(maps);
        }
        return list;
    }

    /**
     * 总公司无单无入库(直营店分销店)发货
     * tPara    -->Json 格式字符串,包含以下属性：
     * Barcode:物流码(条码)    传值:扫描的条码
     * GoodsId:产品编码             选择的产品编号
     * SoCompId:来源单位编码        选择的直营店
     * DeCompId:目的单位编码        选择的分销店
     * OaSuserId:操作员代号         操作员代号
     * StockId:仓库编码             01
     * ScanSn:扫描序号              流水号
     * ScanBillNo:扫描单号          本地生成（见以前代码）
     * BillNo:单据编号              首次传空，第二次按服务器返回单号传。
     * SourceBillNo:来源单号        空
     * DocumentNo:单据编号          空
     */
    public String P_Dv_OutStock_Z_S_NoBill_NoInStock(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_NoInStock", para);
        return data;
    }

    /**
     * 总公司无单无入库(直营店分销店)发货撤销
     */
    public String P_Dv_OutStock_Z_S_NoBill_NoInStock_Cancel(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_NoInStock_Cancel", para);
        return data;
    }

    /**
     * 总公司无单有入库(直营店分销店)发货
     * Barcode:物流码(条码)    传值:扫描的条码
     * GoodsId:产品编码             空
     * SoCompId:来源单位编码        选择的直营店
     * DeCompId:目的单位编码        选择的分销店
     * OaSuserId:操作员代号         操作员代号
     * StockId:仓库编码             选择的仓库编号
     * ScanSn:扫描序号              流水号
     * ScanBillNo:扫描单号          本地生成（见以前代码）
     * BillNo:单据编号              首次传空，第二次按服务器返回单号传。
     * SourceBillNo:来源单号        空
     * DocumentNo:单据编号          空
     */
    public String P_Dv_OutStock_Z_S_NoBill_InStock(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_InStock", para);
        return data;
    }

    /**
     * 总公司无单有入库(直营店分销店)发货撤销
     */
    public String P_Dv_OutStock_Z_S_NoBill_InStock_Cancel(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_S_NoBill_InStock_Cancel", para);
        return data;
    }

    /**
     * 无单(直营分店)退货
     * Barcode:物流码(条码)    传值:扫描的条码
     * GoodsId:产品编码             空
     * SoCompId:来源单位编码        选择的直营店
     * DeCompId:目的单位编码        选择的分销店
     * OaSuserId:操作员代号         操作员代号
     * StockId:仓库编码             选择的仓库编号
     * ScanSn:扫描序号              流水号
     * ScanBillNo:扫描单号          本地生成（见以前代码）
     * BillNo:单据编号              首次传空，第二次按服务器返回单号传。
     * SourceBillNo:来源单号        空
     * DocumentNo:单据编号          空
     */
    public String P_Dv_ReturnedPurchase_Z_S_NoBill(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_ReturnedPurchase_Z_S_NoBill", para);
        return data;
    }

    /**
     * 无单(直营分店)退货撤消
     */
    public String P_Dv_ReturnedPurchase_Z_S_NoBill_Cancel(String tWebId, String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_ReturnedPurchase_Z_S_NoBill_Cancel", para);
        return data;
    }

    /**
     * 直营分销店发货
     * tLoginId -->登录ID
     * tWebId   -->条码，用做网络令牌
     * tPara    -->Json 格式字符串,包含以下属性：
     * {
     * Barcode:物流码(条码)    传值:物流码(必传)
     * DeCompId:产品编码            分店代号(必传)
     * OaSuserId:操作员代号         操作员代号(必传)
     * StockId:仓库编码             选择的仓库编号(必传)
     * ScanSn:扫描序号              流水号(必传)
     * ScanBillNo:扫描单号          本地生成（见以前代码）
     * SourceBillNo:单据编号        直营店单号，首次传空，第二次按服务器返回单号传。
     * BillNo:单据编号              分店单号，首次传空，第二次按服务器返回单号传。
     * }
     */

    public String P_Dv_OutStock_Z_L_S_NoBill_InStock(String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_OutStock_Z_L_S_NoBill_InStock", para);
        return data;
    }

    /**
     * 下载代理商调货单函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */
    public List<Map<String, Object>> GetDowLoadTransferBill(String tCompanyId) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tCompanyId", tCompanyId);
        para.add(map);
        String result = downLoadWebResult("GetDowLoadTransferBill", para);
        JSONArray listjson = new JSONArray(result);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < listjson.length(); i++) {
            JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("TransferNo", jsonObject2.getString("TransferNo"));
            map1.put("InAgentName", jsonObject2.getString("InAgentName"));
            map1.put("TransferDate", jsonObject2.getString("TransferDate"));
            list.add(map1);
        }
        return list;
    }


    /**
     * 代理商调货单发货函数
     *
     * @return
     * @throws Exception
     * @author van van.shu@magic-point.com
     * @version 创建时间：2017-9-16 下午5:36:53
     */

    public String P_Dv_TransferGoods_D(String tPara) throws Exception {

        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
        String data = getWebResult("P_Dv_TransferGoods_D", para);
        return data;
    }


    /**
     * 读取已装盒标码信息
     * @author
     * @version 创建时间：2025年7月31日16:38:22
     * @return
     * @throws Exception
     */
    public String Holyes_Dv_Factory_GetFillBoxInfor( String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
//        Log.d("mainget", para.toString());
        String data = getWebResult("Holyes_Dv_Factory_GetFillBoxInfor", para);
        return data;
    }

    /**
     * 品牌商 读取已装盒标码信息
     * @author
     * @version 创建时间：2026年3月9日14:10:05
     * @return
     * @throws Exception
     */
    public String GetFillBoxInfor( String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
//        Log.d("mainget", para.toString());
        String data = getWebResult("GetFillBoxInfor", para);
        return data;
    }

    /**
     * 装盒剔除
     * @author
     * @version 创建时间：2025年7月31日16:38:27
     * @return
     * @throws Exception
     */
    public String Holyes_Dv_Factory_WeedOutFillBox( String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
//        Log.d("mainWeedOut", para.toString());
        String data = getWebResult("Holyes_Dv_Factory_WeedOutFillBox", para);
//        Log.d("mainresult", data);
        return data;
    }


    /**
     * 品牌商 装盒剔除
     * @author
     * @version 创建时间：2026年3月9日10:45:22
     * @return
     * @throws Exception
     */
    public String WeedOut( String tPara) throws Exception {
        ArrayList<HashMap<Object, Object>> para = new ArrayList<HashMap<Object, Object>>();
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("tLoginId", sysUserInfo.getLoginid());
        map.put("tWebId", mWebId);
        map.put("tPara", tPara);
        para.add(map);
//        Log.d("mainWeedOut", para.toString());
        String data = getWebResult("WeedOut", para);
//        Log.d("mainresult", data);
        return data;
    }



    //api接口返回的数组对象参数
    public List<Map<String, Object>> GetAPIListInterface(String methodName, ArrayList<HashMap<Object, Object>> params) throws Exception {

        try {
            MyRequest request = new MyRequest();
            Gson gson=new Gson();
            //请求的域名地址GET
//            String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetFillBoxInfor"+ "?BoxNo="+tBoxNoCode+"&SoCompId="+Supplierid+"&OaSuserId="+sysUserInfo.getUserid();
            // 构建请求URL
            String baseUrl = "http://" + sysUserInfo.getServerip() + ":9521/" +
                    sysUserInfo.getAPIEndpoint() + "/" + methodName;

            // 构建参数字符串
            String paramStr = buildParamsStringFromList(params);
            String requestUrl = baseUrl + paramStr;
//                    Log.d("main", requestUrl);
            String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
//            Log.d("main", result);
//            JSONObject jsonObject=
            JSONObject jsonObject = new JSONObject(result);
            // 方法1：使用 getBoolean() - 直接获取boolean值
            boolean jsonsuccess = jsonObject.getBoolean("success");
            if (jsonsuccess){
                SalesScsWebApiInfo scsWebApiInfo=gson.fromJson(result,SalesScsWebApiInfo.class);
//                Log.d("main", scsWebApiInfo.getMessage());
                if(scsWebApiInfo.isSuccess()) { // 假设有isSuccess()方法
                    return scsWebApiInfo.getData();
                }else{
                    throw new Exception("服务器：" + scsWebApiInfo.getMessage());
                }
            }else{
                throw new Exception("服务器：" +jsonObject.optString("message"));
            }
        } catch (Exception e) {
            throw new Exception("服务器：" + e.getMessage());
        }
    }

    public String GetAPIStringInterface(String methodName, ArrayList<HashMap<Object, Object>> params) throws Exception {

        try {
            MyRequest request = new MyRequest();
            Gson gson=new Gson();
            //请求的域名地址GET
//            String requestUrl="http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+"/AndroidDv/GetFillBoxInfor"+ "?BoxNo="+tBoxNoCode+"&SoCompId="+Supplierid+"&OaSuserId="+sysUserInfo.getUserid();
            // 构建请求URL
            String baseUrl = "http://" + sysUserInfo.getServerip() + ":9521/" +
                    sysUserInfo.getAPIEndpoint() + "/" + methodName;

            // 构建参数字符串
            String paramStr = buildParamsStringFromList(params);
            String requestUrl = baseUrl + paramStr;
//                    Log.d("main", requestUrl);
            String result = request.getV1(requestUrl,sysUserInfo.getLoginid(),"text/plain");//调用我们写的Get方法
//            JSONObject jsonObject=
            JSONObject jsonObject = new JSONObject(result);
            // 方法1：使用 getBoolean() - 直接获取boolean值
            boolean jsonsuccess = jsonObject.getBoolean("success");
            if (jsonsuccess){
//                Log.d("main", scsWebApiInfo.getMessage());
                return jsonObject.optString("data");

            }else{
                throw new Exception("服务器：" +jsonObject.optString("message"));
            }
        } catch (Exception e) {
            throw new Exception("服务器：" + e.getMessage());
        }
    }

    /**
     * 从ArrayList<HashMap<Object, Object>>构建参数字符串
     *
     * @param paramList 参数列表
     * @return 参数字符串
     */
    private static String buildParamsStringFromList(ArrayList<HashMap<Object, Object>> paramList) {
        if (paramList == null || paramList.isEmpty()) {
            return "";
        }

        StringBuilder paramBuilder = new StringBuilder("?");

        for (int i = 0; i < paramList.size(); i++) {
            HashMap<Object, Object> paramMap = paramList.get(i);

            if (paramMap != null && !paramMap.isEmpty()) {
                Iterator<Map.Entry<Object, Object>> iterator = paramMap.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Object, Object> entry = iterator.next();
                    String key = entry.getKey() != null ? entry.getKey().toString() : "";
                    Object value = entry.getValue();

                    paramBuilder.append(key).append("=");

                    if (value != null) {
                        // 处理不同类型的值
                        paramBuilder.append(encodeParamValue(value));
                    }

                    // 添加分隔符
                    if (i < paramList.size() - 1 || iterator.hasNext()) {
                        paramBuilder.append("&");
                    }
                }
            }
        }

        // 移除最后一个多余的"&"
        String result = paramBuilder.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * 编码参数值
     *
     * @param value 参数值
     * @return 编码后的字符串
     */
    private static String encodeParamValue(Object value)
    {
        Gson gson=new Gson();
        if (value == null) {
            return "";
        }
        try {
            if (value instanceof String) {
                return URLEncoder.encode((String) value, "UTF-8");
            } else if (value instanceof Number || value instanceof Boolean) {
                return URLEncoder.encode(value.toString(), "UTF-8");
            } else if (value instanceof List) {
                // 处理List类型
                String json = gson.toJson(value);
                return URLEncoder.encode(json, "UTF-8");
            } else if (value instanceof Map) {
                // 处理Map类型
                String json = gson.toJson(value);
                return URLEncoder.encode(json, "UTF-8");
            } else {
                // 处理其他对象类型
                String json = gson.toJson(value);
                return URLEncoder.encode(json, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return value.toString();
        }
    }

    public String PostAPIStringInterface(String methodName, String tPara) throws Exception {

        try {
            // 请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer "+sysUserInfo.getLoginToken());
            HttpPostMultipart multipart = new HttpPostMultipart("http://"+sysUserInfo.getServerip()+":9521/"+sysUserInfo.getAPIEndpoint()+ "/" + methodName, "utf-8", headers);


            JSONObject jsonObject = new JSONObject(tPara);
            Iterator<String> keys = jsonObject.keys();

            // 1. 添加动态参数
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                String paramName = formatParamName(key);
                String paramValue = value != null ? value.toString() : "";
                multipart.addFormField( paramName,paramValue);
            }

            Map<String, String> requestdata=jsonToMapStringWithGson(tPara);

            // 返回信息
            String multiresponse = multipart.finish(requestdata);

            Gson gson = new GsonBuilder().create();
            ScanApiResponse response = gson.fromJson(multiresponse, ScanApiResponse.class);
            if (response.isSuccess()==true){
                //不用实体类，直接解析
                JSONObject rootObject = new JSONObject(multiresponse);
//                JSONObject OrderDetailsdata = rootObject.getJSONObject("data");
                return rootObject.optString("data");

            }else{

                throw new Exception("服务器：" +response.getMessage());
            }

        } catch (Exception e) {
            throw new Exception("服务器：" +e.getMessage());
        }
    }

    /**
     * 格式化参数名（可选）
     * 例如：将"goodsId"转为"GoodsId"
     */
    private static String formatParamName(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }

        // 如果已经是首字母大写，保持原样
        if (Character.isUpperCase(key.charAt(0))) {
            return key;
        }

        // 否则将首字母转为大写
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }


    /**
     * 使用 Gson 转为 Map<String, String>
     */
    private static Map<String, String> jsonToMapStringWithGson(String jsonString) {
        Map<String, String> result = new HashMap<>();
        Gson gson=new Gson();
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> tempMap = gson.fromJson(jsonString, type);

            for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    result.put(key, "");
                } else if (value instanceof String) {
                    result.put(key, (String) value);
                } else if (value instanceof Number) {
//                    result.put(key, value.toString());
                    Number num = (Number) value;
                    if (num.doubleValue() == num.longValue()) {
                        // 如果是整数
                        result.put(key, String.valueOf(num.longValue()));
                    } else {
                        // 如果是小数，保留适当精度
                        result.put(key, String.valueOf(num.doubleValue()));
                    }
                } else if (value instanceof Boolean) {
                    result.put(key, Boolean.toString((Boolean) value));
                } else if (value instanceof List) {
                    result.put(key, gson.toJson(value));
                } else if (value instanceof Map) {
                    result.put(key, gson.toJson(value));
                } else {
                    result.put(key, value.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Gson解析失败: " + e.getMessage(), e);
        }

        return result;
    }


}
