package com.example.wholesalersend.entity;

/**
 * @ClassName: ReplaceLabCode
 * @Description: 绑码请求参数实体类
 * @Author: lijin
 * @Date: 2024/12/20 11:47
 */
public class ReplaceLabCodePara {
    private String OriginallyLabCode;
    private String NewLabCode;
    private String MODE;




    public String getOriginallyLabCode() {
        return OriginallyLabCode;
    }

    public void setOriginallyLabCode(String originallyLabCode) {
        OriginallyLabCode = originallyLabCode;
    }

    public String getNewLabCode() {
        return NewLabCode;
    }

    public void setNewLabCode(String newLabCode) {
        NewLabCode = newLabCode;
    }

    public String getMODE() {
        return MODE;
    }

    public void setMODE(String MODE) {
        this.MODE = MODE;
    }
}
