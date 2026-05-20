package com.example.wholesalersend.entity;

/**
 * @ClassName: SyncStores
 * @Description: 同步门店
 * @Author: lijin
 * @Date: 2025/1/3 13:42
 */
public class SyncStores {

    private String EditMode; //编辑状态(A-新增；M-修改)
    private String CutCode; //SCS客户代号
    private String StoreId;//SCS门店代号
    private String StoreName; //SCS门店别名
    private String CustLink;//联系人
    private String CustMobile;//手机号
    private String BrandName;//品牌
    private String ProviceName; //所在省份名称
    private String CityName;//所在城市名称
    private String CountyName;//所在区县
    private String CustAddr;//公司地址
    private String LicenceNo;//营业执照
    private String CorpName;//SCS门店名称
    private String CcsStoreId;


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

    public String getStoreId() {
        return StoreId;
    }

    public void setStoreId(String storeId) {
        StoreId = storeId;
    }

    public String getStoreName() {
        return StoreName;
    }

    public void setStoreName(String storeName) {
        StoreName = storeName;
    }

    public String getCustLink() {
        return CustLink;
    }

    public void setCustLink(String custLink) {
        CustLink = custLink;
    }

    public String getCustMobile() {
        return CustMobile;
    }

    public void setCustMobile(String custMobile) {
        CustMobile = custMobile;
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

    public String getCcsStoreId() {
        return CcsStoreId;
    }

    public void setCcsStoreId(String ccsStoreId) {
        CcsStoreId = ccsStoreId;
    }

    public SyncStores() {
        super();
    }

    public SyncStores(String editMode, String cutCode, String storeId, String storeName,
                      String custLink, String custMobile, String brandName, String proviceName,
                      String cityName, String countyName, String custAddr, String licenceNo,
                      String corpName, String ccsStoreId) {
        EditMode = editMode;
        CutCode = cutCode;
        StoreId = storeId;
        StoreName = storeName;
        CustLink = custLink;
        CustMobile = custMobile;
        BrandName = brandName;
        ProviceName = proviceName;
        CityName = cityName;
        CountyName = countyName;
        CustAddr = custAddr;
        LicenceNo = licenceNo;
        CorpName = corpName;
        CcsStoreId = ccsStoreId;
    }
}
