### v1.1.0

(2020-02-02)

**新增**

* 添加`/auth/changePassword`接口

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
