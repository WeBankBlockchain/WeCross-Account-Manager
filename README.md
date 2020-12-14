![](docs/images/menu_logo_wecross.png)

# WeCross-Account-Manager

[![CodeFactor](https://www.codefactor.io/repository/github/webankblockchain/WeCross-Account-Manager/badge)](https://www.codefactor.io/repository/github/webankblockchain/WeCross-Account-Manager) [![Build Status](https://travis-ci.org/WeBankBlockchain/WeCross-Account-Manager.svg?branch=dev)](https://travis-ci.org/WeBankBlockchain/WeCross-Account-Manager) [![Latest release](https://img.shields.io/github/release/WeBankBlockchain/WeCross-Account-Manager.svg)](https://github.com/WeBankBlockchain/WeCross-Account-Manager/releases/latest) 
[![License](https://img.shields.io/github/license/WeBankBlockchain/WeCross-Account-Manager)](https://www.apache.org/licenses/LICENSE-2.0) [![Language](https://img.shields.io/badge/Language-Java-blue.svg)](https://www.java.com)

WeCross Account Manager是[WeCross](https://github.com/WeBankBlockchain/WeCross)跨链账户管理服务。

## 关键特性

- 跨链账户生命周期管理
- 负责跨链路由的账户注册和登录，管理登录态

## 部署使用

* 可直接下载WeCross跨链账户管理服务压缩包，然后解压并使用。具体请参考[部署和使用文档](https://wecross.readthedocs.io/zh_CN/latest/docs/tutorial/networks.html#id4)

## 源码编译

**环境要求**:

  - [JDK8及以上](https://www.oracle.com/java/technologies/javase-downloads.html)
  - Gradle 5.0及以上

**编译命令**:

```bash
$ cd WeCross-Account-Manager
$ ./gradlew assemble
```

如果编译成功，将在当前目录的dist/apps目录下生成跨链账户管理服务jar包。

## 贡献说明

欢迎参与WeCross社区的维护和建设：

- 提交代码(Pull requests)，可参考[代码贡献流程](CONTRIBUTING.md)以及[wiki指南](https://github.com/WeBankBlockchain/WeCross/wiki/%E8%B4%A1%E7%8C%AE%E4%BB%A3%E7%A0%81)
- [提问和提交BUG](https://github.com/WeBankBlockchain/WeCross-Account-Manager/issues/new)

希望在您的参与下，WeCross会越来越好！

## 社区
联系我们：wecross@webank.com

## License

WeCross Account Manager的开源协议为Apache License 2.0，详情参考[LICENSE](./LICENSE)。
