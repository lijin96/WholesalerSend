package com.example.wholesalersend.entity;

/**
 * @ClassName: AccountSet
 * @Description: 账套实体类
 * @Author: lijin
 * @Date: 2023/10/23 16:21
 */
public class AccountSet {
    private String AccountSetId;//账套代号
    private String AccountSetName;//账套名称
    private String DataBaseName;//数据库名称

    public AccountSet() {
    }

    public AccountSet(String accountSetId, String accountSetName, String dataBaseName) {
        AccountSetId = accountSetId;
        AccountSetName = accountSetName;
        DataBaseName = dataBaseName;
    }

    public String getAccountSetId() {
        return AccountSetId;
    }

    public void setAccountSetId(String accountSetId) {
        AccountSetId = accountSetId;
    }

    public String getAccountSetName() {
        return AccountSetName;
    }

    public void setAccountSetName(String accountSetName) {
        AccountSetName = accountSetName;
    }

    public String getDataBaseName() {
        return DataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        DataBaseName = dataBaseName;
    }

    @Override
    public String toString() {
        return "AccountSet{" +
                "AccountSetId='" + AccountSetId + '\'' +
                ", AccountSetName='" + AccountSetName + '\'' +
                ", DataBaseName='" + DataBaseName + '\'' +
                '}';
    }
}
