package com.example.wholesalersend.entity;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ScsWebApiInfo
 * @Description: webapi返回数据解析
 * @Author: lijin
 * @Date: 2024/12/19 13:45
 */
public class SalesScsWebApiInfo {
    private int code;
    private String message;
    private List<Map<String, Object>> data;
    private boolean success;
    private String version;
    private int runTime;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public boolean isSuccess() {
        return success;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    @Override
    public String toString() {
        return "SalesScsWebApiInfo{" + "code=" + code + ", message='" + message + '\'' + ", " +
                "data='" + data + '\'' + ", success=" + success + ", version='" + version + '\'' + ", runTime=" + runTime + '}';
    }
}
