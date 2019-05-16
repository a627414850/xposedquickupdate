package cn.qssq666.hotupdate;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by qssq on 2019/1/15 qssq666@foxmail.com
 */
public abstract class MainAbstract implements MainI {
    public static String _implClass = MainImpl.class.getName();


    public static String _pluginPackageName;
}
