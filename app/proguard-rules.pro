# R8 / ProGuard 规则
# 液态玻璃套件与底层引擎依赖反射与 shader 缓存查找，混淆后会导致
# 启动 Activity 崩溃或玻璃效果静默失效。

# ---- 液态玻璃组件套 ----
-keep class com.liquidglass.** { *; }

# ---- 底层引擎（backdrop / shapes）----
-keep class com.kyant.backdrop.** { *; }
-keep class com.kyant.shapes.** { *; }

# ---- 保留运行时着色器相关的类名与方法签名 ----
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
