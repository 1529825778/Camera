# Camera
安装Android Studio 需要安装SDK组件，可以先跳过安装SDK部分，设置http proxy完成SDK下载。
如果可以翻墙，则可以直接在Android Studio中下载（一般在安装过程中会完成SDK的下载），如果没有可以在如下界面中下载SDK和NDK
![image](https://user-images.githubusercontent.com/64718289/131598846-b5cdbb72-b66b-4d87-8fa7-923555055015.png)

安装完成后，为了确保版本一致，以及能够通过镜像下载编译运行需要的插件，修改project下的build_gradle文件，classpath后面的数字修改为android studio版本号
![image](https://user-images.githubusercontent.com/64718289/131598056-7121b038-9326-4dbe-bd4d-29d18147302a.png)
如果不能翻墙，下载ndk压缩文件解压，并将ndk添加到系统环境变量中、下载安装Cmake，cmake版本与Gardle Scripts下Module的build.gradle对应最后在Gradle Scripts下的local.properities添加SDK、ndk、cmake路径（如果是通过Android Studio下载的ndk和sdk,不用添加ndk和sdk路径）
![image](https://user-images.githubusercontent.com/64718289/131598978-975b5585-8cc8-44bd-9a79-123f5ffc85bb.png)
安装下载VulkanSDK，官网下载相应版本，安装安装完成后需要配置环境变量。在此电脑=>高级系统设置=>环境变量的系统变量部分的Path添加V u l k a n {\rm Vulkan}Vulkan的b i n {\rm bin}bin目录。使用命令行窗口测试是否安装成功：
![image](https://user-images.githubusercontent.com/64718289/131767086-c4592c90-2a82-43d8-963b-c7921e3afe6f.png)

