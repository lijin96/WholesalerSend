package com.example.wholesalersend.entity;

/**
 * @ClassName: ScanOrder
 * @Description: 扫描单号集合
 * @Author: lijin
 * @Date: 2024/1/25 14:15
 */
public class ScanOrder {
    private String BillNo;//单号
    private String BillNum;//单据数量
    private String LastScanTime;//最后一次扫描时间
    private String StockCode;//仓库代号
    private String StockName;//仓库名称

    public ScanOrder() {
    }

    public ScanOrder(String billNo, String billNum, String lastScanTime, String stockCode, String stockName) {
        BillNo = billNo;
        BillNum = billNum;
        LastScanTime = lastScanTime;
        StockCode = stockCode;
        StockName = stockName;
    }

    public String getBillNo() {
        return BillNo;
    }

    public void setBillNo(String billNo) {
        BillNo = billNo;
    }

    public String getBillNum() {
        return BillNum;
    }

    public void setBillNum(String billNum) {
        BillNum = billNum;
    }

    public String getLastScanTime() {
        return LastScanTime;
    }

    public void setLastScanTime(String lastScanTime) {
        LastScanTime = lastScanTime;
    }

    public String getStockCode() {
        return StockCode;
    }

    public void setStockCode(String stockCode) {
        StockCode = stockCode;
    }

    public String getStockName() {
        return StockName;
    }

    public void setStockName(String stockName) {
        StockName = stockName;
    }

    @Override
    public String toString() {
        return "ScanOrder{" +
                "BillNo='" + BillNo + '\'' +
                ", BillNum='" + BillNum + '\'' +
                ", LastScanTime='" + LastScanTime + '\'' +
                ", StockCode='" + StockCode + '\'' +
                ", StockName='" + StockName + '\'' +
                '}';
    }
}
