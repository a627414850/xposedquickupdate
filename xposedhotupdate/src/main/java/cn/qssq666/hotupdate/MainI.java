package cn.qssq666.hotupdate;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface MainI {

    public abstract void handleLoadPackageFromCache(final XC_LoadPackage.LoadPackageParam loadPackageParam);

    public abstract void handleLoadPackageFromOrigin(final XC_LoadPackage.LoadPackageParam loadPackageParam);

}
