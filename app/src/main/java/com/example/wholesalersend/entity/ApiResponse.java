package com.example.wholesalersend.entity;

/**
 * @ClassName: ApiResponse
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2025/6/17 10:05
 */
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private String version;
    private long runTime;
    private T data; // 泛型用于嵌套对象

    // 必须有无参构造函数
    public ApiResponse() {}

    // Getter/Setters（实际解析不需要，但使用需要）
    public T getData() {
        return data;
    }

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

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public void setData(T data) {
        this.data = data;
    }
}