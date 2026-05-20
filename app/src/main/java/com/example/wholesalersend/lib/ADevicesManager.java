package com.example.wholesalersend.lib;


import android.device.DeviceManager;

/**
 * @ClassName: ADevicesManager
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:27
 */
public class ADevicesManager {

    //>>>>>>>>>	箭头包裹中的设置大多是在初次运行时起效  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**是否需要下载菜单 true:下载；false:不下载  */
    public static final boolean isDownloadMenu = true;//true:下载；false:不下载

    /**是否添加上一些下载不到的菜单来测试的，默认为false,不添加 */
    public static final boolean isAddTestMenu = false ;//true添加上一些下载不到的菜单来测试的，默认为false

    public static final boolean isSCSNewInterface = false;//是否测试新接口2.0迭代接口


    /** 是否截取条码前两位品牌验证,截取就true,不截取就false*/
    public static final boolean isSubBrandTwo = false ;//是否截取前两位品牌验证,截取就true,不截取就false,这个不只在初次运行时有效

    /** 手机是否可以测试运行，home键是否屏蔽的，true真机下才能运行，模拟器运行会报错，false 手机上会报错，默认为false */
    public final static boolean isMobileTest = true;//true:在手机上可以运行;false在手机上不可以运行，默认为false,

    /**
     * 是否新系统
     * */
    public static final boolean NewSystem = true ;

    /**
     * 设置Home键和返回键是否能够用
     * */
    public static void SetKey(Boolean On_Off)
    {
        if(isMobileTest)
            return ;
        DeviceManager DM = new DeviceManager();
        //旧系统
        assert(!NewSystem);
        //		 DM.switchHomeKey(On_Off);
        //		 DM.switchStatusBar(On_Off);

        //新系统
        assert(NewSystem);
        DM.enableHomeKey(On_Off);//屏解锁
        DM.enableStatusBar(On_Off);
        return ;
    }

    public static void setCurrentTime(long time)
    {
        if(isMobileTest)
            return ;

        DeviceManager DM = new DeviceManager();

        DM.setCurrentTime(time);

        return ;
    }
//
    public static String getDeviceId()
    {
        if(isMobileTest)
            return "1234567890";
        DeviceManager DM = new DeviceManager();

        return DM.getDeviceId();
    }
}
