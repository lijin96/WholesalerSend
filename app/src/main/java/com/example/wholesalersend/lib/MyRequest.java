package com.example.wholesalersend.lib;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * @ClassName: MyRequest
 * @Description: WebApi接口请求
 * @Author: lijin
 * @Date: 2024/12/19 8:57
 */

public class MyRequest {
    private static final String SECRET_KEY = "d303f040ab6fbaa0"; // 门店代号


    //WebApi POST请求
    public String post(String url1, String data,String tloginid,String tContentType) throws Exception {
        try {
            URL url = new URL(url1);
            HttpURLConnection Connection = (HttpURLConnection) url.openConnection();//创建连接
            Connection.setRequestMethod("POST");
            Connection.setConnectTimeout(3000);
            Connection.setReadTimeout(3000);
            Connection.setDoInput(true);
            Connection.setDoOutput(true);
            Connection.setUseCaches(false);
            Connection.setRequestProperty("Content-Type",tContentType);

            if (!tloginid.equals("")){
                Connection.setRequestProperty("Authorization","Bearer "+tloginid);
            }
//            Connection.setRequestProperty("User-Agent","Apifox/1.0.0 (https://apifox.com)");
//            Connection.setRequestProperty("Cookie","ASP.NET_SessionId=vbjc1p3qtg0ixvaw4k1lzazn");
//            Connection.setRequestProperty("Accept","*/*");
//            Connection.setRequestProperty("Connection","keep-alive");
//            Connection.setRequestProperty("Host","test.4006889521.cn:9521");

            Connection.connect();
            DataOutputStream dos = new DataOutputStream(Connection.getOutputStream());
            String title = data;//这里是POST请求需要的参数字符串类型，例如"id=1&data=2"
            dos.write(title.getBytes());
            dos.flush();
            dos.close();//写完记得关闭
            int responseCode = Connection.getResponseCode();
//            Log.d("main", "post: "+responseCode);
            if (responseCode == Connection.HTTP_OK) {//判断请求是否成功
                InputStream inputStream = Connection.getInputStream();
//                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//                byte[] bytes = new byte[1024];
//                int length = 0;
//                while ((length = inputStream.read(bytes)) != -1) {
//                    arrayOutputStream.write(bytes, 0, length);
//                    arrayOutputStream.flush();
//                }//读取响应体的内容
//                String s = arrayOutputStream.toString();
//                return s;//返回请求到的内容，字符串形式
                int hasRead=0;
                byte[]buf =new byte[1024];
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                //循环读取
                while (true){
                    hasRead=inputStream.read(buf);
                    if(hasRead==-1){
                        break;
                    }
                    bos.write(buf,0,hasRead);
                }
                inputStream.close();
                return bos.toString();
            } else {
                throw new Exception(responseCode+" "+url1+" "+Connection.getResponseMessage());
            }
        } catch (IOException e) {
            throw new Exception("服务器:(IOException网络超时)"+e);
        } catch (Exception e) {
            throw new Exception("服务器:服务器连接失败"+e.getMessage());
        }
    }


    public static String readMyInputStream(InputStream is) {
        byte[] result;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer))!=-1) {
                baos.write(buffer,0,len);
            }
            is.close();
            baos.close();
            result = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            String errorStr = "获取数据失败。";
            return errorStr;
        }
        return new String(result);
    }

    //WebApi GET请求
    public String get(String url1,String tloginid,String tQuery,String tStartTime,String tContentType)throws Exception {
        try {
            String lastUrl = url1 + "?Query="+ URLEncoder.encode(tQuery, "utf-8")+"&StartTime="+tStartTime;

            URL url = new URL(lastUrl);
            HttpURLConnection Connection = (HttpURLConnection) url.openConnection();
            Connection.setRequestMethod("GET");
            Connection.setConnectTimeout(3000);
            Connection.setReadTimeout(3000);
            Connection.setRequestProperty("Content-Type",tContentType);

            if (!tloginid.equals("")){
                Connection.setRequestProperty("Authorization","Bearer "+tloginid);
            }

            Connection.connect();
//            DataOutputStream dos = new DataOutputStream(Connection.getOutputStream());
//            String title = data;//这里是POST请求需要的参数字符串类型，例如"id=1&data=2"
//            dos.write(title.getBytes());
//            dos.flush();
//            dos.close();//写完记得关闭
            int responseCode = Connection.getResponseCode();
//            Log.d("main", "post: "+responseCode);
            if (responseCode == Connection.HTTP_OK) {//判断请求是否成功
                InputStream inputStream = Connection.getInputStream();
//                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//                byte[] bytes = new byte[1024];
//                int length = 0;
//                while ((length = inputStream.read(bytes)) != -1) {
//                    arrayOutputStream.write(bytes, 0, length);
//                    arrayOutputStream.flush();
//                }//读取响应体的内容
//                String s = arrayOutputStream.toString();
//                return s;//返回请求到的内容，字符串形式
                int hasRead=0;
                byte[]buf =new byte[1024];
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                //循环读取
                while (true){
                    hasRead=inputStream.read(buf);
                    if(hasRead==-1){
                        break;
                    }
                    bos.write(buf,0,hasRead);
                }
                inputStream.close();
                return bos.toString();
            } else {
                throw new Exception(responseCode+" "+lastUrl+" "+Connection.getResponseMessage());
            }

//            int responseCode = Connection.getResponseCode();
//            if (responseCode == Connection.HTTP_OK) {
//                InputStream inputStream = Connection.getInputStream();
//                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//                byte[] bytes = new byte[1024];
//                int length = 0;
//                while ((length = inputStream.read(bytes)) != -1) {
//                    arrayOutputStream.write(bytes, 0, length);
//                    arrayOutputStream.flush();//强制释放缓冲区
//                }
//                String s = arrayOutputStream.toString();
//                return s;
//            } else {
//                return "-1";
//            }
        } catch (IOException e) {
            throw new Exception("服务器:(IOException网络超时)"+e);
        } catch (Exception e) {
            throw new Exception("服务器:服务器连接失败"+e.getMessage());
        }
    }


    //WebApi GET请求
    public String getV1(String url1,String tloginid,String tContentType)throws Exception {
        try {
            Map<String, String> signParams = getSignParams(url1);
            // 将签名参数添加到URL
            String signedUrl = addSignParamsToUrl(url1, signParams);
//            Log.d("main", signedUrl);
            URL url = new URL(signedUrl);
            HttpURLConnection Connection = (HttpURLConnection) url.openConnection();
            Connection.setRequestMethod("GET");
            Connection.setConnectTimeout(3000);
            Connection.setReadTimeout(3000);
            Connection.setRequestProperty("Content-Type",tContentType);

            if (!tloginid.equals("")){
                Connection.setRequestProperty("Authorization","Bearer "+tloginid);
            }

            Connection.connect();
//            DataOutputStream dos = new DataOutputStream(Connection.getOutputStream());
//            String title = data;//这里是POST请求需要的参数字符串类型，例如"id=1&data=2"
//            dos.write(title.getBytes());
//            dos.flush();
//            dos.close();//写完记得关闭
            int responseCode = Connection.getResponseCode();
//            Log.d("main", "get: "+responseCode);
            if (responseCode == Connection.HTTP_OK) {//判断请求是否成功
                InputStream inputStream = Connection.getInputStream();
//                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//                byte[] bytes = new byte[1024];
//                int length = 0;
//                while ((length = inputStream.read(bytes)) != -1) {
//                    arrayOutputStream.write(bytes, 0, length);
//                    arrayOutputStream.flush();
//                }//读取响应体的内容
//                String s = arrayOutputStream.toString();
//                return s;//返回请求到的内容，字符串形式
                int hasRead=0;
                byte[]buf =new byte[1024];
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                //循环读取
                while (true){
                    hasRead=inputStream.read(buf);
                    if(hasRead==-1){
                        break;
                    }
                    bos.write(buf,0,hasRead);
                }
                inputStream.close();
                return bos.toString();
            } else {
                throw new Exception(responseCode+" "+url1+" "+Connection.getResponseMessage());
            }

//            int responseCode = Connection.getResponseCode();
//            if (responseCode == Connection.HTTP_OK) {
//                InputStream inputStream = Connection.getInputStream();
//                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//                byte[] bytes = new byte[1024];
//                int length = 0;
//                while ((length = inputStream.read(bytes)) != -1) {
//                    arrayOutputStream.write(bytes, 0, length);
//                    arrayOutputStream.flush();//强制释放缓冲区
//                }
//                String s = arrayOutputStream.toString();
//                return s;
//            } else {
//                return "-1";
//            }
        } catch (IOException e) {
            throw new Exception("服务器:(IOException网络超时)"+e);
        } catch (Exception e) {
            throw new Exception("服务器:服务器连接失败"+e.getMessage());
        }
    }



    /**
     * 获取签名参数
     *
     * @param url 原始URL
     * @return 包含签名参数的Map
     */
    public static Map<String, String> getSignParams(String url) {
        Map<String, String> signParams = new HashMap<>();

        // 添加时间戳
        long timestamp = System.currentTimeMillis() / 1000;
        signParams.put("x-time", String.valueOf(timestamp));

        // 添加版本号
        signParams.put("x-version", "1.0");

        // 计算签名
        String hash = calculateSignature(url, signParams);
        signParams.put("x-hash", hash);

        return signParams;
    }

    /**
     * 计算签名
     *
     * @param url 原始URL
     * @param signParams 签名参数
     * @return SHA256签名
     */
    private static String calculateSignature(String url, Map<String, String> signParams) {
        try {
            // 提取URL中的查询参数
            Map<String, String> queryParams = extractQueryParams(url);

            // 合并所有参数（查询参数 + 签名参数）
            Map<String, String> allParams = new TreeMap<>();
            allParams.putAll(queryParams);
            allParams.putAll(signParams);

            // 构建待签名字符串
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }

            // 添加密钥
            sb.append("&key=").append(SECRET_KEY);

            // 计算SHA256哈希
            return sha256(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("计算签名失败", e);
        }
    }

    /**
     * 从URL中提取查询参数
     *
     * @param url 完整URL
     * @return 查询参数Map
     */
    private static Map<String, String> extractQueryParams(String url) {
        Map<String, String> params = new HashMap<>();

        // 查找查询字符串开始位置
        int queryStart = url.indexOf('?');
        if (queryStart == -1) {
            return params; // 没有查询参数
        }

        // 提取查询字符串
        String queryString = url.substring(queryStart + 1);

        // 分割参数
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                params.put(key, value);
            }
        }

        return params;
    }

    /**
     * 计算SHA256哈希
     *
     * @param input 输入字符串
     * @return SHA256哈希值
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 将签名参数添加到URL
     *
     * @param originalUrl 原始URL
     * @param signParams 签名参数
     * @return 带签名的URL
     */
    public static String addSignParamsToUrl(String originalUrl, Map<String, String> signParams) {
        StringBuilder urlBuilder = new StringBuilder(originalUrl);

        // 检查URL是否已有查询参数
        boolean hasQuery = originalUrl.contains("?");

        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            // 添加分隔符
            urlBuilder.append(hasQuery ? "&" : "?");
            hasQuery = true;

            try {
                // URL编码参数
                String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
                String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
                urlBuilder.append(encodedKey).append("=").append(encodedValue);
            } catch (UnsupportedEncodingException e) {
                // 使用默认字符集重试
                try {
                    String encodedKey = URLEncoder.encode(entry.getKey(), "ISO-8859-1");
                    String encodedValue = URLEncoder.encode(entry.getValue(), "ISO-8859-1");
                    urlBuilder.append(encodedKey).append("=").append(encodedValue);
                } catch (UnsupportedEncodingException ex) {
                    // 如果还是失败，直接添加（不推荐）
                    urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }

        return urlBuilder.toString();
    }
}
