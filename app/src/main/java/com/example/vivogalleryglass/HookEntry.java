package com.example.vivogalleryglass;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    private static final String TAG = "VivoGlass";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"com.vivo.gallery".equals(lpparam.packageName)) return;

        android.util.Log.d(TAG, "==== 模块已加载到相册进程中 ====");

        // 方案1：Hook 之前找到的类
        try {
            XposedHelpers.findAndHookMethod(
                    "com.android.gallery3d.vivo.main.base.GalleryBottomBar",
                    lpparam.classLoader,
                    "onAttachedToWindow",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            View bottomBar = (View) param.thisObject;
                            if (bottomBar == null) return;
                            android.util.Log.d(TAG, "方案1：找到 GalleryBottomBar (onAttachedToWindow)");
                            applyGlassEffect(bottomBar);
                        }
                    }
            );
            android.util.Log.d(TAG, "方案1：Hook GalleryBottomBar 成功");
        } catch (Throwable t) {
            android.util.Log.e(TAG, "方案1：Hook GalleryBottomBar 失败: " + t.toString());
        }

        // 方案2：根据之前抓到的 ID (fl_btm_bar_elev) 全局搜索
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    lpparam.classLoader,
                    "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            // 延迟 1 秒等待相册界面完全加载
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                try {
                                    int resId = activity.getResources().getIdentifier("fl_btm_bar_elev", "id", "com.vivo.gallery");
                                    if (resId != 0) {
                                        View bottomBarLayout = activity.findViewById(resId);
                                        if (bottomBarLayout != null) {
                                            android.util.Log.d(TAG, "方案2：通过 ID 找到 fl_btm_bar_elev");
                                            applyGlassEffect(bottomBarLayout);
                                        } else {
                                            android.util.Log.d(TAG, "方案2：findViewById 为空");
                                        }
                                    } else {
                                        android.util.Log.d(TAG, "方案2：找不到 ID fl_btm_bar_elev");
                                    }
                                } catch (Throwable ignored) {}
                            }, 1000);
                        }
                    }
            );
            android.util.Log.d(TAG, "方案2：Hook Activity 成功");
        } catch (Throwable t) {
            android.util.Log.e(TAG, "方案2：Hook Activity 失败: " + t.toString());
        }
    }

    private void applyGlassEffect(View view) {
        try {
            // 使用半透明灰色，更容易看出来有没有生效
            view.setBackgroundColor(Color.parseColor("#66000000"));
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RenderEffect blurEffect = RenderEffect.createBlurEffect(50f, 50f, Shader.TileMode.MIRROR);
                view.setRenderEffect(blurEffect);
            }
            android.util.Log.d(TAG, "==== 玻璃效果已应用！ ====");
        } catch (Throwable t) {
            android.util.Log.e(TAG, "应用玻璃效果失败: " + t.toString());
        }
    }
}
