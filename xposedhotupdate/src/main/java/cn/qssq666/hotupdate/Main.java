package cn.qssq666.hotupdate;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by qssq on 2019/1/15 qssq666@foxmail.com
 */
public class Main implements IXposedHookLoadPackage,MainI  {
    private static final String TAG_ = "Main";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        Log.w(TAG_, "HOOKUI_handleLoadPackage" + loadPackageParam.packageName + " " + loadPackageParam.processName + " " + loadPackageParam.isFirstApplication);
        try {
            Log.w(TAG_, "handleLoadPackageFromCache-start");
            MainAbstract._implClass = Main.class.getName();
            QuickUpdatePluginCache.handleLoadPackage(loadPackageParam,this);
//                doHookEnter(loadPackageParam);
        } catch (Throwable throwable) {
            Log.w(TAG_, "handleLoadPackageFromCache-fail");
            doHookEnter(loadPackageParam);
            throwable.printStackTrace();
        }
    }

    /**
     * 动态更新成功的调用
     * @param loadPackageParam
     */
    public void handleLoadPackageFromCache(final XC_LoadPackage.LoadPackageParam loadPackageParam)  {
        Log.w(TAG_, "handleLoadPackageFromCache-call");
        doHookEnter(loadPackageParam);
    }

    /**
     * 动态更新加载失败的时候从这里调用
     * @param loadPackageParam
     */
    @Override
    public void handleLoadPackageFromOrigin(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        doHookEnter(loadPackageParam);
    }

    /**
     * 最终的hook逻辑
     * @param loadPackageParam
     */
    public void doHookEnter(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //TODO
    }

}
