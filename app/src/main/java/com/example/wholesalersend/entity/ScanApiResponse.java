package com.example.wholesalersend.entity;

/**
 * @ClassName: ScanApiResponse
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2025/8/5 17:19
 */
public class ScanApiResponse<T>  {
    private boolean success;
    private String code;
    private String message;
    private String version;
    private int runTime;
    private T data; // 泛型T用于接收不同类型的数据对象

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}