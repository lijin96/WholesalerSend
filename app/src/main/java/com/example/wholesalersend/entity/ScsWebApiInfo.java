package com.example.wholesalersend.entity;

/**
 * @ClassName: ScsWebApiInfo
 * @Description: webapi返回数据解析
 * @Author: lijin
 * @Date: 2024/12/19 13:45
 */
public class ScsWebApiInfo {
    private int Code;
    private String Message;
    private String Data;
    private boolean success;

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ScsWebApiInfo{" + "Code=" + Code + ", Message='" + Message + '\'' + ", Data='" + Data + '\'' + ", success=" + success + '}';
    }
}
