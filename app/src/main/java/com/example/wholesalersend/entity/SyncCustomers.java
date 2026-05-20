package com.example.wholesalersend.entity;

/**
 * @ClassName: SyncCustomers
 * @Description: 同步客户
 * @Author: lijin
 * @Date: 2025/1/3 13:35
 */
public class SyncCustomers {
    private String EditMode;//编辑状态(A-新增；M-修改)
    private String CutCode; //SCS客户代号
    private String CustName; //SCS客户别名
    private String CustLink;//联系人
    private String CustTel; //联系电话
    private String CustMobile; //手机号
    private String SaleId; //业务员代号
    private String SaleName;//业务员姓名
    private String BrandName; //品牌
    private String ProviceName;//所在省份名称
    private String CityName; //所在城市名称
    private String CountyName; //所在区县
    private String CustAddr;//公司地址
    private String LicenceNo;//营业执照
    private String CorpName;//SCS客户名称
    private String CcsCustId;//

    public String getEditMode() {
        return EditMode;
    }

    public void setEditMode(String editMode) {
        EditMode = editMode;
    }

    public String getCutCode() {
        return CutCode;
    }

    public void setCutCode(String cutCode) {
        CutCode = cutCode;
    }

    public String getCustName() {
        return CustName;
    }

    public void setCustName(String custName) {
        CustName = custName;
    }

    public String getCustLink() {
        return CustLink;
    }

    public void setCustLink(String custLink) {
        CustLink = custLink;
    }

    public String getCustTel() {
        return CustTel;
    }

    public void setCustTel(String custTel) {
        CustTel = custTel;
    }

    public String getCustMobile() {
        return CustMobile;
    }

    public void setCustMobile(String custMobile) {
        CustMobile = custMobile;
    }

    public String getSaleId() {
        return SaleId;
    }

    public void setSaleId(String saleId) {
        SaleId = saleId;
    }

    public String getSaleName() {
        return SaleName;
    }

    public void setSaleName(String saleName) {
        SaleName = saleName;
    }

    public String getBrandName() {
        return BrandName;
    }

    public void setBrandName(String brandName) {
        BrandName = brandName;
    }

    public String getProviceName() {
        return ProviceName;
    }

    public void setProviceName(String proviceName) {
        ProviceName = proviceName;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getCountyName() {
        return CountyName;
    }

    public void setCountyName(String countyName) {
        CountyName = countyName;
    }

    public String getCustAddr() {
        return CustAddr;
    }

    public void setCustAddr(String custAddr) {
        CustAddr = custAddr;
    }

    public String getLicenceNo() {
        return LicenceNo;
    }

    public void setLicenceNo(String licenceNo) {
        LicenceNo = licenceNo;
    }

    public String getCorpName() {
        return CorpName;
    }

    public void setCorpName(String corpName) {
        CorpName = corpName;
    }

    public String getCcsCustId() {
        return CcsCustId;
    }

    public void setCcsCustId(String ccsCustId) {
        CcsCustId = ccsCustId;
    }

    public SyncCustomers() {
        super();
    }

    public SyncCustomers(String editMode, String cutCode, String custName, String custLink,
                         String custTel, String custMobile, String saleId, String saleName,
                         String brandName, String proviceName, String cityName, String countyName
            , String custAddr, String licenceNo, String corpName, String ccsCustId) {
        EditMode = editMode;
        CutCode = cutCode;
        CustName = custName;
        CustLink = custLink;
        CustTel = custTel;
        CustMobile = custMobile;
        SaleId = saleId;
        SaleName = saleName;
        BrandName = brandName;
        ProviceName = proviceName;
        CityName = cityName;
        CountyName = countyName;
        CustAddr = custAddr;
        LicenceNo = licenceNo;
        CorpName = corpName;
        CcsCustId = ccsCustId;
    }
}
