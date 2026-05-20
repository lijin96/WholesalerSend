package com.example.wholesalersend.entity;

/**
 * @ClassName: GoodsMarkLabelScan
 * @Description: java类作用描述
 * @Author: lijin 获取对象接口
 * @Date: 2025/6/17 10:07
 */
public class GoodsMarkLabelScan {
    private String goodsSysCode;
    private String goodsCode;
    private String goodsName;
    private String goodsTypeName;
    private String brandName;
    private String seriesName;
    private String modelm;
    private String refractiveIndex;
    private String colors;
    private String membraneType;
    private Object diopter; // 根据实际类型替换
    private Object astigmatism;

    public String getGoodsSysCode() {
        return goodsSysCode;
    }

    public void setGoodsSysCode(String goodsSysCode) {
        this.goodsSysCode = goodsSysCode;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsTypeName() {
        return goodsTypeName;
    }

    public void setGoodsTypeName(String goodsTypeName) {
        this.goodsTypeName = goodsTypeName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getModelm() {
        return modelm;
    }

    public void setModelm(String modelm) {
        this.modelm = modelm;
    }

    public String getRefractiveIndex() {
        return refractiveIndex;
    }

    public void setRefractiveIndex(String refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getMembraneType() {
        return membraneType;
    }

    public void setMembraneType(String membraneType) {
        this.membraneType = membraneType;
    }

    public Object getDiopter() {
        return diopter;
    }

    public void setDiopter(Object diopter) {
        this.diopter = diopter;
    }

    public Object getAstigmatism() {
        return astigmatism;
    }

    public void setAstigmatism(Object astigmatism) {
        this.astigmatism = astigmatism;
    }
}