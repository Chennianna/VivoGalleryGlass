package com.example.vivogalleryglass;

import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"com.vivo.gallery".equals(lpparam.packageName)) return;
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
                            bottomBar.setBackgroundColor(Color.parseColor("#33FFFFFF"));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                RenderEffect blurEffect = RenderEffect.createBlurEffect(50f, 50f, Shader.TileMode.MIRROR);
                                bottomBar.setRenderEffect(blurEffect);
                            }
                            bottomBar.setClipToOutline(true);
                            bottomBar.setOutlineProvider(new android.view.ViewOutlineProvider() {
                                @Override
                                public void getOutline(View view, android.graphics.Outline outline) {
                                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + 40, 40f);
                                }
                            });
                        }
                    }
            );
        } catch (Throwable t) {
            android.util.Log.e("VivoGlass", "Hook失败: " + t.getMessage());
        }
    }
}
