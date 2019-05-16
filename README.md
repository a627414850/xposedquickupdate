### 导入依赖

```
compile 'cn.qssq666:xposedhotupdate:0.6'
```


### 使用方法
设置
     MainAbstract._implClass=实现MainAbstract的类.
    MainAbstract._pluginPackageName= BuildConfig.APPLICATION_ID;//插件包名

套路代码
```
public class Main implements IXposedHookLoadPackage,MainI  {
    private static final String TAG_ = "Main";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        Log.w(TAG_, "HOOKUI_handleLoadPackage" + loadPackageParam.packageName + " " + loadPackageParam.processName + " " + loadPackageParam.isFirstApplication);
        try {
            Log.w(TAG_, "handleLoadPackageFromCache-start");
                MainAbstract._pluginPackageName= BuildConfig.APPLICATION_ID;
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





```

