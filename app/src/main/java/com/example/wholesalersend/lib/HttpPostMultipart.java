package com.example.wholesalersend.lib;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpPostMultipart {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private Map<String, String> headerData;

    /**
     * 构造初始化 http 请求，content type设置为multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @param headers
     * @throws IOException
     */
    public HttpPostMultipart(String requestURL, String charset, Map<String, String> headers) throws IOException {
        this.charset = charset;
        this.headerData=headers;
        boundary = UUID.randomUUID().toString();
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);    // indicates POST method
        httpConn.setDoInput(true);

        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
        if (headers != null && headers.size() > 0) {
            Iterator<String> it = headers.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = headers.get(key);
                httpConn.setRequestProperty(key, value);
            }
        }
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

//    /**
//     * 添加form字段到请求
//     *
//     * @param name  field name
//     * @param value field value
//     */
//    public void addFormField(String name, String value) {
//        writer.append("--" + boundary).append(LINE_FEED);
//        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
//        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
//        writer.append(LINE_FEED);
//        writer.append(value).append(LINE_FEED);
//        writer.flush();
//    }

    public void addFormField(String name, String value) throws IOException {
        String header = "--" + boundary + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + name.trim() + "\"" + LINE_FEED +
                "Content-Type: text/plain; charset=" + charset + LINE_FEED +
                LINE_FEED;
        outputStream.write(header.getBytes(charset));
        outputStream.write(value.getBytes(charset));
        outputStream.write(LINE_FEED.getBytes(charset));
        outputStream.flush();
    }



    /**
     * 添加文件
     *
     * @param fieldName
     * @param uploadFile
     * @throws IOException
     */
//    public void addFilePart(String fieldName, File uploadFile)
//            throws IOException {
//        String fileName = uploadFile.getName();
//        writer.append("--" + boundary).append(LINE_FEED);
//        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
//        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
//        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
//        writer.append(LINE_FEED);
//        writer.flush();
//
//        FileInputStream inputStream = new FileInputStream(uploadFile);
//        byte[] buffer = new byte[4096];
//        int bytesRead = -1;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//        outputStream.flush();
//        inputStream.close();
//        writer.append(LINE_FEED);
//        writer.flush();
//    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return String as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
//    public String finish(Map<String, String> requestParams) throws IOException {
//        String response = "";
////        writer.flush();
////        writer.append("--" + boundary + "--").append(LINE_FEED);
////        writer.close();
//
//        try {
//            Map<String, String> signedParams = getSignParams(requestParams);
//            // 发送请求...
//            Log.d("main-signedParams", signedParams.toString());
//
//            String end = "--" + boundary + "--" + LINE_FEED;
//            outputStream.write(end.getBytes(charset)); // 改用字节流写结束符
//            outputStream.flush();
//
//            // checks server's status code first
//            int status = httpConn.getResponseCode();
//
//            if (status == HttpURLConnection.HTTP_OK) {
//                ByteArrayOutputStream result = new ByteArrayOutputStream();
//                byte[] buffer = new byte[1024];
//                int length;
//                while ((length = httpConn.getInputStream().read(buffer)) != -1) {
//                    result.write(buffer, 0, length);
//                }
//                response = result.toString(this.charset);
//                httpConn.disconnect();
//                return response;
//            } else {
//                throw new IOException("Server returned non-OK status: " + status);
//            }
//        } catch (Exception e) {
//            // 处理异常
//            Log.e("SignUtils", "签名生成失败", e);
//            throw new IOException("签名生成失败: " +  e.getMessage());
//        }
//    }


    public String finish(Map<String, String> requestParams) throws IOException {
        String response = "";
        try {
            // 获取签名参数
            Map<String, String> signedParams = getSignParams(requestParams);
//            Log.d("main-signedParams", signedParams.toString());

            // 获取当前URL
            String originalUrl = httpConn.getURL().toString();

            // 将签名参数拼接到URL
            String signedUrl = appendParamsToUrl(originalUrl, signedParams);
//            Log.d("main-signedUrl", signedUrl);

            // 重新创建连接（因为URL已改变）
            URL newUrl = new URL(signedUrl);
            httpConn.disconnect(); // 关闭原连接

            // 创建新连接
            httpConn = (HttpURLConnection) newUrl.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            if (headerData != null && headerData.size() > 0) {
                Iterator<String> it = headerData.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = headerData.get(key);
                    httpConn.setRequestProperty(key, value);
                }
            }

            // 重新获取输出流
            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

            writeMapToRequestBody(requestParams);

            // 写入结束边界
            String end = "--" + boundary + "--" + LINE_FEED;
            outputStream.write(end.getBytes(charset));
            outputStream.flush();

            // 检查服务器状态码
            int status = httpConn.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = httpConn.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                response = result.toString(this.charset);
                httpConn.disconnect();
                return response;
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        } catch (Exception e) {
            Log.e("SignedRequest", "请求处理失败", e);
            throw new IOException("请求处理失败: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (writer != null) writer.close();
            } catch (IOException ex) {
                Log.e("SignedRequest", "关闭流失败", ex);
            }
        }
    }

    /**
     * 将Map集合参数写入请求体
     *
     * @param params Map集合参数
     * @throws IOException
     */
    private void writeMapToRequestBody(Map<String, String> params) throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            // 写入字段开始边界
            writer.append("--" + boundary).append(LINE_FEED);

            // 写入Content-Disposition
            writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                    .append(LINE_FEED);

            // 写入Content-Type
            writer.append("Content-Type: text/plain; charset=" + charset)
                    .append(LINE_FEED);

            // 写入空行分隔头部和内容
            writer.append(LINE_FEED);

            // 写入字段值
            writer.append(value).append(LINE_FEED);

            // 刷新缓冲区
            writer.flush();
        }
    }

    public String appendParamsToUrl(String url, Map<String, String> signedParams) {
        StringBuilder urlBuilder = new StringBuilder(url);

        // 检查URL是否已有查询参数
        boolean hasQuery = url.contains("?");

        for (Map.Entry<String, String> entry : signedParams.entrySet()) {
            // 跳过空值
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            // 添加分隔符
            urlBuilder.append(hasQuery ? "&" : "?");
            hasQuery = true;

            try {
                // URL编码参数
                String encodedKey = URLEncoder.encode(entry.getKey(), charset);
                String encodedValue = URLEncoder.encode(entry.getValue(), charset);
                urlBuilder.append(encodedKey).append("=").append(encodedValue);
            } catch (UnsupportedEncodingException e) {
                // 使用默认字符集重试
                try {
                    String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                    String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                    urlBuilder.append(encodedKey).append("=").append(encodedValue);
                } catch (UnsupportedEncodingException ex) {
                    // 如果还是失败，直接添加（不推荐）
                    urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }

        return urlBuilder.toString();
    }

//    private static final String KEY = "d303f040ab6fbaa0"; // 门店代号
//    private static final String HMAC_KEY = "secure_hmac_key"; // HMAC密钥

    public Map<String, String> getSignParams(Map<String, String> data) {
        // 创建基础参数
        Map<String, String> params = new HashMap<>();
        params.put("x-time", String.valueOf(System.currentTimeMillis())); // 当前时间戳
        params.put("x-version", "1.0"); // 版本号
        // 合并参数
        Map<String, String> tempParams = new HashMap<>(params);
        tempParams.putAll(data);
        // 对键进行排序
        List<String> sortedKeys = new ArrayList<>(tempParams.keySet());
        Collections.sort(sortedKeys);
        // 构建待签名字符串
        StringBuilder sb = new StringBuilder();
        for (String key : sortedKeys) {
            sb.append(key).append("=").append(tempParams.get(key)).append("&");
        }
        // 添加KEY
        sb.append("key=d303f040ab6fbaa0");
        // 计算SHA256哈希
        String hash = sha256(sb.toString());
        // 构建最终结果
        Map<String, String> result = new HashMap<>();
        result.put("x-time", params.get("x-time"));
        result.put("x-version", params.get("x-version"));
        result.put("x-hash", hash);
        return result;
    }

    /**
     * 计算字符串的SHA256哈希值
     */
    private String sha256(String input) {
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
}
