### v1.3.1

(2023-07-31)

**新增**

* 支持FISCO BCOS 3.+ WASM执行版本的Stub支持。

### v1.3.0

(2023-03-15)

**新增**

* 新增Email验证登陆注册接口
* 新增对FISCO BCOS 3.x的Stub支持

**修复**

* 修复`create_rsa_keypair.sh`脚本在openssl3.+版本下的问题

**更新**

* 更新gson、bouncycastle、netty、spring-boot-starter、mysql-connector、h2database的版本号，以修复安全问题。

### v1.2.1

(2021-12-15)

**修复**

* 修复log4j的漏洞，将其升级至2.15

### v1.2.0

(2021-08-20)

**新增**

* 资源访问控制功能，管理员可通过网页管理台给用户授权可访问的资源

### v1.1.1

(2020-04-02)

**新增**

* 启动时打印版本号

**修复**

* 修复并发条件登录态超时的bug
* 修复登录失败信息未正确返回的bug

### v1.1.0

(2020-02-02)

**新增**

* v1.01.接口uth/changePassword`接口

**更新**

* 升级`spring-boot-xxx`版本，详情参考`build.gradle`修改内容
* 数据库字段加密:
    * t_universal_accounts: sec、token_sec
    * t_chain_accounts: sec
* `SSL`证书格式修改为`RSA`

**修复**

* 修复一些JDK版本偶发`java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException`的问题

### v1.0.1

(2020-01-15)

**功能**

* 启动脚本添加参数，修复新版本JDK无法使用的问题

### v1.0.0

(2020-12-17)

**功能**

* 跨链账户管理：为不同的链账户统一跨链身份(UniversalAccount)
* 跨链路由登录：负责管理跨链路由登录态

**新增**

* 注册与登录：新增注册与登录接口，管理登录态
* 链账户管理：新增链账户生命周期管理包括生成、存储、移除等
