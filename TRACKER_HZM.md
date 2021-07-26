# 每日任务进度

## 2021.7.11

* 完成 Charge Monitor 的开发

## 2021.7.18

* 使用 Charge Monitor 对收集了几次充电的信息

## 2021.7.20

* 阅读充电驱动的代码

## 2021.7.21

* 尝试给 Redmi Note 10 刷机，遇到以下困难：
  * LineageOS 官方没有支持该机型，尝试使用 Redmi Note 8 的 ROM 进行刷机；
  * 解锁手机需要等待 7 天，尝试先刷机 Nexus 6；

## 2021.7.22

* 使用 LineageOS 官方提供的 ROM 包对 Nexus 6 进行刷机
  * 首先打开 USB 调试和 OEM 锁；
  * 然后进入 fastboot 模式写入官方提供的 Recovery；
  * 按照 Recovery 旁加载 LOS 的 ROM。

## 2021.7.23

* 尝试下载 LineageOS 的源码进行编译再刷机，之后打算阅读源码并修改，遇到以下困难：
  * 在 Windows 下使用 Google 的代码管理工具 repo
  * 下载源码需要 200G 空间
  * 编译需要 Ubuntu 环境
  * repo sync 同步代码时需要翻墙（困难在于翻墙下载 200G 大小的文件）

## 2021.7.25

* 研究了一下翻墙
* 发现实验室 Ubuntu 电脑上的存储空间也不够 200G，买了一块移动硬盘打算在自己电脑上装个 Ubuntu。

* 寻找翻墙的替代方案——使用镜像：
  * 清华 TUNA 的 AOSP 镜像和 LineageOS 挂掉了，无法进行下载；
  * 科大镜像只提供了 ROM 没有提供源码；

## 2021.7.26

* 安装 Ubuntu，配置各种环境和科学上网

* 下载代码

  * 主要按官方提供的流程：https://wiki.lineageos.org/devices/shamu/build

  * 在 repo sync 的时候需要配置 git 的代理

    ```bash
    git config --global http.proxy 127.0.0.1:7890
    git config --global https.proxy 127.0.0.1:7890
    ```

  * 配置完 git 代理后，利用 repo sync定会出现很多 git 仓库同步失败的情况，试了试网上的方法没有效果。后来自己查看 `.repo/manifests/default.xml`，发现一小部分仓库的来源是 Github 上的 LineageOS 仓库，其余全是 AOSP 上的仓库，这部分全在 Google 的仓库里。于是修改 Google 的仓库为科大的 AOSP 镜像，再同步就成功。