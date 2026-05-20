package com.example.wholesalersend.entity;

/**
 * @ClassName: ReplaceLabCode
 * @Description: 绑码请求参数实体类
 * @Author: lijin
 * @Date: 2024/12/20 11:47
 */
public class SalesReplaceLabCodePara {
    private String OriginallyLabCode;
    private String NewLabCode;
    private String MODE;//换标模式:AUTO(自由模式) | USER(用户指定模式)
    private String BillNo;//单号
    private String BillType;//单据类型:IB->入库单;OB->出库单
 



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

    public String getBillNo() {
        return BillNo;
    }

    public void setBillNo(String billNo) {
        BillNo = billNo;
    }

    public String getBillType() {
        return BillType;
    }

    public void setBillType(String billType) {
        BillType = billType;
    }
}
