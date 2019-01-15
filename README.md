### 导入依赖

```


compile 'cn.qssq666:hollowoutview:0.1'
```

### 修改半透明蒙版颜色
直接拿到view然后getPaint()设置颜色即可。
### 自定义更多透明镂空区域形状
实现下面的接口就可以实现了


```

```
### 使用方法
设置
     MainAbstract._implClass=实现MainAbstract的类.


套路代码
```

public class Main implements IXposedHookLoadPackage {
    private static final String TAG_ = "Main";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        Log.w(TAG_, "HOOKUI_handleLoadPackage" + loadPackageParam.packageName + " " + loadPackageParam.processName + " " + loadPackageParam.isFirstApplication);
        try {
            Log.w(TAG_, "handleLoadPackageFromCache-start");
            MainAbstract._implClass = Main.class.getName();
            QuickUpdatePluginCache.handleLoadPackage(loadPackageParam);
//                doHookEnter(loadPackageParam);
        } catch (Throwable throwable) {
            Log.w(TAG_, "handleLoadPackageFromCache-fail");
            doHookEnter(loadPackageParam);
            throwable.printStackTrace();
        }
    }


    public void handleLoadPackageFromCache(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.w(TAG_, "handleLoadPackageFromCache-call");
        doHookEnter(loadPackageParam);
    }

    public void doHookEnter(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //TODO
    }

}




```

