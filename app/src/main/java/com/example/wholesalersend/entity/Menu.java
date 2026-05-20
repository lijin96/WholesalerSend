package com.example.wholesalersend.entity;

/**
 * @ClassName: Menu
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2025/7/25 10:04
 */
import java.util.List;

public class Menu {
    private String menuCode;
    private String menuName;
    private String parentCode;
    private boolean showStatus;
    private double sortNo;
    private List<Menu> children;

    // Getters and Setters
    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public boolean isShowStatus() { return showStatus; }
    public void setShowStatus(boolean showStatus) { this.showStatus = showStatus; }

    public double getSortNo() { return sortNo; }
    public void setSortNo(double sortNo) { this.sortNo = sortNo; }

    public List<Menu> getChildren() { return children; }
    public void setChildren(List<Menu> children) { this.children = children; }

    @Override
    public String toString() {
        return "Menu{" +
                "menuCode='" + menuCode + '\'' +
                ", menuName='" + menuName + '\'' +
                ", children=" + children +
                '}';
    }
}