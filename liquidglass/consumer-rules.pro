# Liquid-glass component kit (com.liquidglass)
# The engine comes from io.github.kyant0:backdrop:1.0.6 (Apache-2.0) as a Maven dependency.
#
# 保留规则：套件与引擎在运行时按类名查找（shader 缓存、反射构造），
# 混淆后会导致玻璃效果静默失效或启动崩溃。

-keep class com.liquidglass.** { *; }
-keep class com.kyant.backdrop.** { *; }
-keep class com.kyant.shapes.** { *; }
