package cn.qssq666.hotupdate;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by qssq on 2018/1/7 qssq666@foxmail.com
 */

public class QuickUpdatePluginCache {


    private static final String TAG = "QuickUpdatePluginCache";
    private static List<String> hostAppPackages = new ArrayList<>();

    static {
        // TODO: Add the package name of application your want to hook!
    }


    /**
     * http://blog.csdn.net/cxmscb/article/details/52435389
     * 安装app以后，系统会在/data/app/下备份了一份.apk文件，通过动态加载这个apk文件，调用相应的方法
     * 这样就可以实现，只需要第一次重启，以后修改hook代码就不用重启了
     * PathClassLoader 不能直接从 zip 包中得到 dex，因此只支持直接操作 dex 文件或者已经安装过的 apk（因为安装过的 apk 在 cache 【 /data/dalvik-cache】中存在缓存的 dex 文件）。
     * <p>
     * DexClassLoader 可以加载外部的 apk、jar 或 dex文件，并且会在指定的 outpath 路径存放其 dex 文件。
     * https://www.jianshu.com/p/22230ed1b6e2
     *
     * @param thisAppPackage   当前app的packageName
     * @param handleHookClass  指定由哪一个类处理相关的hook逻辑
     * @param loadPackageParam 传入XC_LoadPackage.LoadPackageParam参数
     * @throws Throwable 抛出各种异常,包括具体hook逻辑的异常,寻找apk文件异常,反射加载Class异常等
     */
    private static void invokeHandleHookMethod(String thisAppPackage, String handleHookClass, String handleHookMethodName, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
//        File apkFile = findApkFileBySDK(thisAppPackage);//会受其它Xposed模块hook 当前宿主程序的SDK_INT的影响
        File apkFile = findApkFile(thisAppPackage);
        //加载指定的hook逻辑处理类，并调用它的handleHook方法
        PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
        Class<?> cls = Class.forName(handleHookClass, true, pathClassLoader);
        Field implClass = cls.getClass().getField("_implClass");
        String classNameStr = (String) implClass.get(null);
        cls = pathClassLoader.loadClass(classNameStr);
        Object instance = cls.newInstance();
        Method method = cls.getDeclaredMethod(handleHookMethodName, XC_LoadPackage.LoadPackageParam.class);
        method.invoke(instance, loadPackageParam);
    }

    /**
     * 寻找这个Android设备上的当前apk文件,不受其它Xposed模块hook SDK_INT的影响
     *
     * @param thisAppPackage 当前模块包名
     * @return File 返回apk文件
     * @throws FileNotFoundException 在/data/app/下的未找到本模块apk文件,请检查本模块包名配置是否正确.
     *                               具体检查build.gradle中的applicationId和AndroidManifest.xml中的package
     */
    private static File findApkFile(String thisAppPackage) throws FileNotFoundException {
        File apkFile = null;

        if (apkFile == null) {
            Context currentContext = XposeUtil.geSystemContext();
            if (currentContext != null) {
                try {
                    PackageInfo packageInfo = currentContext.getPackageManager().getPackageInfo(thisAppPackage, 0);

                    return new File(packageInfo.applicationInfo.sourceDir);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
//            throw new FileNotFoundException("没在/data/app/下找到文件对应的apk文件");
            return null;
        }
        try {
            apkFile = findApkFileAfterSDK21(thisAppPackage);
        } catch (Exception e) {
            try {
                apkFile = findApkFileBeforeSDK21(thisAppPackage);
            } catch (Exception e2) {
                //忽略这个异常
            }
        }
        if (apkFile == null) {
            throw new FileNotFoundException("没在/data/app/下找到文件对应的apk文件");
        } else {
            Log.w(TAG, "找到apkFile:" + apkFile);
        }
        return apkFile;
    }

    /**
     * 根据当前的SDK_INT寻找这个Android设备上的当前apk文件
     *
     * @param thisAppPackage 当前模块包名
     * @return File 返回apk文件
     * @throws FileNotFoundException 在/data/app/下的未找到本模块apk文件,请检查本模块包名配置是否正确.
     *                               具体检查build.gradle中的applicationId和AndroidManifest.xml中的package
     */
    private File findApkFileBySDK(String thisAppPackage) throws FileNotFoundException {
        File apkFile;
        //当前Xposed模块hook了Build.VERSION.SDK_INT不用担心，因为这是发生在hook之前，不会有影响
        //但是其它的Xposed模块hook了当前宿主的这个值以后，就会有影响了,所以这里没有使用这个方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            apkFile = findApkFileAfterSDK21(thisAppPackage);
        } else {
            apkFile = findApkFileBeforeSDK21(thisAppPackage);
        }
        return apkFile;
    }

    /**
     * 寻找apk文件(api_21之后)
     * 在Android sdk21以及之后，apk文件的路径发生了变化
     *
     * @param packageName 当前模块包名
     * @return File 返回apk文件
     * @throws FileNotFoundException apk文件未找到
     */
    private static File findApkFileAfterSDK21(String packageName) throws FileNotFoundException {
        File apkFile;
        File path = new File(String.format("/data/app/%s-%s", packageName, "1"));
        if (!path.exists()) {
            path = new File(String.format("/data/app/%s-%s", packageName, "2"));
        }
        if (!path.exists() || !path.isDirectory()) {
            throw new FileNotFoundException(String.format("没找到目录/data/app/%s-%s", packageName, "1/2"));
        }
        apkFile = new File(path, "base.apk");
        if (!apkFile.exists() || apkFile.isDirectory()) {
            throw new FileNotFoundException(String.format("没找到文件/data/app/%s-%s/base.apk", packageName, "1/2"));
        }
        return apkFile;
    }

    /**
     * 寻找apk文件(api_21之前)
     *
     * @param packageName 当前模块包名
     * @return File 返回apk文件
     * @throws FileNotFoundException apk文件未找到
     */
    private static File findApkFileBeforeSDK21(String packageName) throws FileNotFoundException {
        File apkFile = new File(String.format("/data/app/%s-%s.apk", packageName, "1"));
        if (!apkFile.exists()) {
            apkFile = new File(String.format("/data/app/%s-%s.apk", packageName, "2"));
        }
        if (!apkFile.exists() || apkFile.isDirectory()) {
            throw new FileNotFoundException(String.format("没找到文件/data/app/%s-%s.apk", packageName, "1/2"));
        }

        return apkFile;
    }


    /**
     * 实际hook逻辑处理类的入口方法 这种方法可能导致某些进程无法hook到。
     */

    public static boolean handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam, MainI main) throws Throwable {
        if (hostAppPackages.contains(loadPackageParam.packageName) || hostAppPackages.isEmpty()) {
                    final String handleHookClass = MainAbstract.class.getName();
                    invokeHandleHookMethod(BuildConfig.APPLICATION_ID, handleHookClass, "handleLoadPackageFromCache", loadPackageParam);
            return true;
        } else {
            main.handleLoadPackageFromOrigin(loadPackageParam);
            return false;
        }
    }
}
