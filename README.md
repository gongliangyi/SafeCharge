# SafeCharge

## 1 代码下载与编译

参考 LineageOS 的官方教程： https://wiki.lineageos.org/devices/shamu/build

可以先在 https://download.lineageos.org/enchilada 找到对应手机型号和 LineageOS 版本的代号，在根据代号在 Wiki 上找到对应页面，在 Guides 里找到 <u>Build for yourself</u>。

### 1.1 环境需求

#### 1.1.1 系统需求

* Ubuntu 20.04
* 200GB 以上内存空间
* 16 GB 以上 RAM
* 需要翻墙

#### 1.1.2 安装安卓平台工具

从[谷歌官方](https://dl.google.com/android/repository/platform-tools-latest-linux.zip)下载工具包并解压：

```bash
unzip platform-tools-latest-linux.zip -d ~
```

把 **adb** 和 **fastboot** 加入 PATH。编辑 `~/.profile` 并在末尾添加以下内容：

```bash
# add Android SDK platform tools to path
if [ -d "$HOME/platform-tools" ] ; then
    PATH="$HOME/platform-tools:$PATH"
fi
```

#### 1.1.3 安装依赖包

使用 `apt install` 安装以下包：

```bash
bc bison build-essential ccache curl flex g++-multilib gcc-multilib git gnupg gperf imagemagick lib32ncurses5-dev lib32readline-dev lib32z1-dev liblz4-tool libncurses5 libncurses5-dev libsdl1.2-dev libssl-dev libxml2 libxml2-utils lzop pngcrush rsync schedtool squashfs-tools xsltproc zip zlib1g-de
```

 对于早于 20.04 的 Ubuntu 版本还须安装 `libwxgtk3.0-dev`，对于早于 16.04 的版本还需安装 `libwxgtk2.8-dev`。另外，不同 LineageOS 的版本需要安装不同的 Java 版本，具体可参考 LineageOS 官方 Build 教程。

#### 1.1.4 安装 repo

下载 **repo** 并给予执行权限：

```bash
mkdir -p ~/bin
curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
chmod a+x ~/bin/repo
```

编辑 `~/.profile` 把 **repo** 加入 PATH：

```bash
# set PATH so it includes user's private bin if it exists
if [ -d "$HOME/bin" ] ; then
    PATH="$HOME/bin:$PATH"
fi
```

#### 1.1.5 设置 git

```bash
git config --global user.email "you@example.com"
git config --global user.name "Your Name"
```

#### 1.1.6 使用 ccache 加速编译

我编译的时候没有使用，参考官方的教程

### 1.2 下载编译 LineageOS

使用清华镜像可以参考：https://mirrors.tuna.tsinghua.edu.cn/help/lineageOS/，但我下载的时候清华镜像的 AOSP 和 LineageOS 同步 Failed 了（现在清华镜像已经同步成功了），这里再提供一种思路。

#### 1.2.1 下载

创建工作目录，并初始化 **repo**：

```bash
mkdir -p ~/Android/LineageOS
cd ~/Android/LineageOS
repo init -u https://github.com/LineageOS/android.git -b lineage-18.1
```

如果提示网络失败则需要设置 git 的代理（Port 为代理开启的端口）：

```bash
git config --global http.proxy 127.0.0.1:7890
git config --global https.proxy 127.0.0.1:7890
```

同步代码：

```bash
repo sync
```

这一步所使用的时间较长，下载过程中也会出现各种错误：

* `443 timeout` 的错误应该对结果没什么影响；

* 下载时发现没有流量，证明下载卡死，直接 `ctrl+c` 再重新开始下载；

* 由于使用 VPN 网络不稳定，在我下载时还遇到了每次同步时都有一些仓库同步失败，输出的信息穿插在`443 timeout` 之间，会显示几次 `fatal` 并重试之后显示 `error`

  * 看信息可以发现这些仓库大都是 AOSP 的仓库，翻阅 repo 的 manifest 文件（在 `.repo` 文件夹里可以看到 `.manifest` 里面 include 了 `default.xml`）可以看到同步的大部分代码都来自于 AOSP。

  * AOSP 的镜像除了清华还有科大的，我尝试把：

    ```xml
    <remote  name="aosp"
               fetch="android.googlesource.com"
               review="android-review.googlesource.com"
               revision="refs/tags/android-11.0.0_r39" />
    ```

    改成了：

    ```xml
    <remote  name="aosp"
               fetch="git://mirrors.ustc.edu.cn/aosp"
               review="android-review.googlesource.com"
               revision="refs/tags/android-11.0.0_r39" />
    ```

    再次同步很快就成功了。

切换对应的设备（shamu 为设备对应系统代号）：

```bash
source build/envsetup.sh
breakfast shamu
```

提取手机的 blobs，这一步需要把手机连接上电脑并确保打开 USB 调试（可能需要以 ROOT 身份进行 USB调试），进入 `~/android/lineage/device/moto/shamu` 目录后运行 `extract-files.sh`：

```bash
./extract-files.sh
```

我在这里遇到过一次提取失败，原因写在 2.1.2 节，可以使用 `adb devices` 检查设备是否成功连接。

#### 1.2.2 编译

```bash
croot
brunch shamu
```

如果提取 blobs 有问题，在 `brunch` 的时候就会失败。

如果内存不足 16GB 在 `brunch` 开始编译的时候就会有警告，后面会编译失败：

* 我最开始只有 8GB 内存的时候会在编译 `ninja` 时 崩溃，后来关闭 GUI 进程能过。

* 之后会因为 JVM 的堆空间不够崩溃，我试过使用 SWAP 空间和手动指定 JAVA 堆空间：

  ```bash
  export _JAVA_OPTIONS="-Xmx8g"
  ```

  都没有成功。

之后换一根内存条，在 20GB RAM 的环境下直接编译成功没有出现上述问题。

输入 `cd $OUT` 可以进入输出目录，里面有：

1. recovery.img
2. lineage-18.1-xxxxxxxx-UNIFFICIAL-shamu.zip

## 2 刷机

每个手机的流程大致是一样的，只是在解锁手机 bootloader 这一步不同厂商有不同的解锁方式，在 LineageOS 的官网上也有对应的指引。

### 2.1 刷机需求

需要准备以下需求：

* **adb** 和 **fastboot**（在 <u>1.1.2</u> 节中有下载方式）
* 打开设备的 USB 调试
* recovery 文件（有很多 recovery 可以使用，我使用的是 LineageOS 官方提供的）
* 系统文件

### 2.2 解锁 bootloader

首先开启手机的开发者模式，打开 USB 调试和并在 USB 调试的选项里打开 <u>OEM unlock</u>，再通过 USB 连上电脑，使用

```bash
adb devices
```

能看到连上的设备并且设备是 device 的就成功了。

我在这一步会显示 `no permissions`，解决方案是把手机上 USB 偏好设置从<u>不进行数据传输</u>（只充电）切换成<u>文件传输</u>，这时就会有弹窗询问是否允许 USB 调试，允许即可。

在手机开机时按住**电源键**和**音量减**进入 bootloader，也可在终端输入：

```bash
adb reboot bootloader
```

之后手机会进入 bootloader，在电脑上可以使用如下命令查看是否连接成功：

```bash
fastboot devices
```

我在这一步的时候也会显示 `no permissions`，可以用 `sudo` 身份运行：

```bash
sudo $(which fastboot) devices
```

> 上述 **adb** 和 **fastboot** 的 `no permissions` 问题还有一种解决方法，是去设置 udev 规则，在网上搜索这个问题就有类似的解决方案。另外我在 Windows 上刷机的时候会遇到检测不到 bootloader 设备的情况，这是因为没有安装对应的驱动，可以在设备管理器中找到连接的设备搜索对应驱动并安装。

然后解锁 bootloader，输入以下命令后，在手机上选择确定：

```bash
fastboot oem unlock
```

### 2.3 安装 Recovery

进入 fastboot 后输入：

```bash
fastboot flash recovery <recovery_filename>.img
```

### 2.4 利用 Recovery 安装系统

进入 fastboot 后选择 `Recovery mode`，之后依次点击  `Factory reset`、`Format data/factory reset`。

结束之后返回主菜单旁加载 LineageOS 的包：

* 在手机上选择 `Apply update`，再选择 `Apply from ADB`
* 再在电脑上输入 `adb sideload <filename>.zip`

最后返回主菜单选择 `Reboot system now`。

## 3. 修改驱动

* 此处主要针对 Nexus 6，设备信息如下：
  * Qualcomm Snapdragon 805
  * 不修改驱动的情况下，充电电流最大是 **2A**
* 驱动代码在 `/drivers/power/` 文件夹下

### 3.1 确定充电 IC

* 在 `/drivers/power/` 文件夹下有管理电池和充电的许多驱动代码，一直没有找准所以前前后后花了很多时间。

#### 3.1.1 在设备树中查找

可以在 `/arch/arm/boot/dts` 文件夹下查看设备树里驱动的配置，我在 `/arch/arm/boot/dts/qcom` 下搜索了驱动代码里出现的各种充电 IC 名字，发现只出现了 `smb349, smb1357, smb1359` 三个，但这只是一个大体定位的方法，并不能精准定位。

#### 3.1.2

在 `/sys/class/power_supply/` 下各个文件夹对应不同的文件驱动，有些驱动运行时的信息和配置可以在这里找到，如果驱动里实现了 power_supply 的电流限制应该就可以在这里进行配置（*下一步的方向？*）

#### 3.1.3 查看 dmesg

利用 adb 查看手机的 dmesg（若出现 `Permission denied` 可以先输入 `adb root` 切换到 root 模式），发现：

```log
smb135x-charger 0-001c: SMB135X version = smb1359 revision = rev2.1 successfully probed batt=1 dc = 0 usb = 1
```

所以最终定位到驱动 `smb135x-charger.c`。

### 3.2 限制充电电流

#### 3.2.1 初步尝试

首先同时修改了 `static int smb135x_set_dc_chg_current(struct smb135x_chg *chip, int current_ma)` 函数，直接给 `current_ma` 赋值为 `300mA` 编译刷机后发现没什么用。

#### 3.2.2 修改电流

在检查了插上充电线和拔掉时的 dmesg 输出，发现有如下一句：

```log
src_detect_handler: chip->usb_present = 0 usb_present = 1
```

对应代码出现在 `static int src_detect_handler(struct smb135x_chg *chip, u8 rt_stat)` 中，于是猜测类型为 USB 充电，修改 `static int smb135x_set_usb_chg_current(struct smb135x_chg *chip, int current_ma)` 后成功控制电流。

### 3.3 开发控制接口

在 `probe` 函数里有一些在调试模式下创建系统文件的代码，可以参照着创建一个控制电流的系统文件。
