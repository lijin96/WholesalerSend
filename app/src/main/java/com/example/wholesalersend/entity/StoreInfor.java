package com.example.wholesalersend.entity;

import java.io.Serializable;

/**
 * @ClassName: StoreInfor
 * @Description: 分销商，分销店
 * @Author: lijin
 * @Date: 2021/3/6 14:18
 */
public class StoreInfor implements Serializable{
    private static final long serialVersionUID = 1L;
    private String StoreId;//分销店代号
    private String StoreName;//分销店名称
    private String StoreAlias;//分销店别名
    private String StoreSysCode;//分销店系统代号
    private String Link;//联系人
    private String Tel;//电话
    private String CorpAddr;//地址
    private String TraderId;//零售店代号（上级代号）
    private String TraderName;//零售店名称
    private String TraderAlias;//零售店别名
    private String TraderSysId;//零售店系统代号


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
    public String getLink() {
        return Link;
    }
    public void setLink(String link) {
        Link = link;
    }
    public String getTel() {
        return Tel;
    }
    public void setTel(String tel) {
        Tel = tel;
    }
    public String getCorpAddr() {
        return CorpAddr;
    }
    public void setCorpAddr(String corpAddr) {
        CorpAddr = corpAddr;
    }
    public String getTraderId() {
        return TraderId;
    }
    public void setTraderId(String traderId) {
        TraderId = traderId;
    }

    public String getTraderName() {
        return TraderName;
    }

    public void setTraderName(String traderName) {
        TraderName = traderName;
    }

    public String getTraderAlias() {
        return TraderAlias;
    }

    public void setTraderAlias(String traderAlias) {
        TraderAlias = traderAlias;
    }

    public String getStoreSysCode() {
        return StoreSysCode;
    }

    public void setStoreSysCode(String storeSysCode) {
        StoreSysCode = storeSysCode;
    }

    public String getTraderSysId() {
        return TraderSysId;
    }

    public void setTraderSysId(String traderSysId) {
        TraderSysId = traderSysId;
    }

    public String getStoreAlias() {
        return StoreAlias;
    }

    public void setStoreAlias(String storeAlias) {
        StoreAlias = storeAlias;
    }

    public StoreInfor() {
        super();
        // TODO Auto-generated constructor stub
    }
    public StoreInfor(String storeId, String storeName, String link,
                      String tel, String corpAddr, String traderId) {
        super();
        StoreId = storeId;
        StoreName = storeName;
        Link = link;
        Tel = tel;
        CorpAddr = corpAddr;
        TraderId = traderId;
    }

    public StoreInfor(String storeId, String storeName, String link, String tel, String corpAddr, String traderId, String traderName, String traderAlias) {
        StoreId = storeId;
        StoreName = storeName;
        Link = link;
        Tel = tel;
        CorpAddr = corpAddr;
        TraderId = traderId;
        TraderName = traderName;
        TraderAlias = traderAlias;
    }

    public StoreInfor(String storeId, String storeName, String storeAlias, String storeSysCode,
                      String link, String tel, String corpAddr, String traderId,
                      String traderName, String traderAlias, String traderSysId) {
        StoreId = storeId;
        StoreName = storeName;
        StoreAlias = storeAlias;
        StoreSysCode = storeSysCode;
        Link = link;
        Tel = tel;
        CorpAddr = corpAddr;
        TraderId = traderId;
        TraderName = traderName;
        TraderAlias = traderAlias;
        TraderSysId = traderSysId;
    }

    @Override
    public String toString() {
        return "StoreInfor{" + "StoreId='" + StoreId + '\'' + ", StoreName='" + StoreName + '\'' + ", StoreAlias='" + StoreAlias + '\'' + ", StoreSysCode='" + StoreSysCode + '\'' + ", Link='" + Link + '\'' + ", Tel='" + Tel + '\'' + ", CorpAddr='" + CorpAddr + '\'' + ", TraderId='" + TraderId + '\'' + ", TraderName='" + TraderName + '\'' + ", TraderAlias='" + TraderAlias + '\'' + ", TraderSysId='" + TraderSysId + '\'' + '}';
    }
}

