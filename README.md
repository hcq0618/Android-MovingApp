Android-ForkPackageManager
==========================

包含应用搬家功能的开源库  include package move feature's lib

1、源码中的aidl是从Android系统源码中提取出来的 包名不要变 否则再次修改并打包后不能编译通过

2、主要是将反射系统功能的源码打成jar包 放在项目里 运行的时候复制到data目录下 
前提是手机已root 就可以调用系统的app_process进程来执行这个jar 于是就有权限能执行反射系统功能的代码了

3、关于app_process：作用是可以调起其他java程序 
例如am和pm命令行 虽然是执行/system/bin下的相应脚本 但源码是/framework下的am.jar pm.jar 
打开脚本发现 他本质还是通过app_process执行这两个jar jar里面是java程序 执行时是直接调用main函数

4、前提一定需要手机已root 但不用申请额外权限或系统签名

5、由于Android dalvik虚拟机无法解释执行class文件 只能dex文件 所以到处jar包时还需要用到sdk里的dx工具转换成dex
具体可参考网上相关资料，比如：

动态加载jar/dex
http://www.cnblogs.com/over140/archive/2011/11/23/2259367.html

6、app_process用法可以google
大致是命令行执行
su 
export LD_LIBRARY=bin环境变量
export CLASSPATH=jar包全路径
exec app_process /system/bin 完整类路径 传给main函数的参数列表

即要搬家的包名和搬家到的位置(0:自动 1:内存 2:sd) 可以作为main函数的参数传进去

7、后续有时间还会新增功能
