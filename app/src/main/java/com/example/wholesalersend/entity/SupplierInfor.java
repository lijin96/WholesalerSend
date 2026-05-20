package com.example.wholesalersend.entity;

/**
 * @ClassName: SupplierInfor
 * @Description: 供应商
 * @Author: lijin
 * @Date: 2023/10/26 10:24
 */
public class SupplierInfor {
    private String SuppSysCode;//供应商系统代号
    private String SupplierId; //供应商代号
    private String SupplierName; //供应商名称
    private String Uprecndate;

    public SupplierInfor() {
    }


    public SupplierInfor(String suppSysCode, String supplierId, String supplierName, String uprecndate) {
        SuppSysCode = suppSysCode;
        SupplierId = supplierId;
        SupplierName = supplierName;
        Uprecndate = uprecndate;
    }

    public String getSuppSysCode() {
        return SuppSysCode;
    }

    public void setSuppSysCode(String suppSysCode) {
        SuppSysCode = suppSysCode;
    }

    public String getSupplierId() {
        return SupplierId;
    }

    public void setSupplierId(String supplierId) {
        SupplierId = supplierId;
    }

    public String getSupplierName() {
        return SupplierName;
    }

    public void setSupplierName(String supplierName) {
        SupplierName = supplierName;
    }

    public String getUprecndate() {
        return Uprecndate;
    }

    public void setUprecndate(String uprecndate) {
        Uprecndate = uprecndate;
    }

    @Override
    public String toString() {
        return "SupplierInfor{" +
                "SuppSysCode='" + SuppSysCode + '\'' +
                ", SupplierId='" + SupplierId + '\'' +
                ", SupplierName='" + SupplierName + '\'' +
                ", Uprecndate='" + Uprecndate + '\'' +
                '}';
    }
}
