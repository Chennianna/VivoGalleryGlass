package com.example.vivogalleryglass;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 只有进入相册进程才继续
        if (!"com.vivo.gallery".equals(lpparam.packageName)) return;

        android.util.Log.d("VivoGlass", "模块已成功注入相册进程！");

        // Hook 所有的 Activity 的 onResume
        XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        if (activity == null) return;

                        // 在主线程弹个 Toast，并把整个页面背景变红
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                Toast.makeText(activity, "模块已生效！", Toast.LENGTH_SHORT).show();
                                // 强制把整个 Activity 的背景变成红色
                                activity.getWindow().getDecorView().setBackgroundColor(Color.RED);
                                android.util.Log.d("VivoGlass", "已把相册背景变红");
                            } catch (Throwable t) {
                                android.util.Log.e("VivoGlass", "变红失败: " + t.toString());
                            }
                        });
                    }
                }
        );
    }
}
