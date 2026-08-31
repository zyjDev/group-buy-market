# 拼团营销系统 · group-buy-market

<div align="center">

![JDK](https://img.shields.io/badge/JDK-8-007396?logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/Spring%20Boot-2.7.12-6DB33F?logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-2.1.4-FE4648)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![DDD](https://img.shields.io/badge/Architecture-DDD-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-green)

基于 **DDD 分层架构**的拼团营销系统，覆盖优惠试算、锁单成团、退单解锁的完整营销链路。

</div>

---

## 项目简介

拼团是电商营销中最典型的玩法之一：用户开团后邀请他人参团，达到目标人数即成团并享受优惠价，未成团则自动退款。本项目以此为业务背景，用 **DDD（领域驱动设计）四层架构**搭建一套可演进的营销系统，把「优惠试算」「成团判定」「库存锁定」这些核心规则收敛在领域层，而不是散落在 Controller 和 SQL 里。

工程基于 [小傅哥 bugstack](https://bugstack.cn) 的《拼团营销系统》教程做个人实现与改造，目前处于**初始脚手架阶段**：数据层（DAO / PO）与工程骨架已就绪，领域模型与业务链路将随学习进度逐步填充。

> 本项目为学习实践性质，非生产级系统。教程出处与致谢见文末。

## 当前进度

| 层次 | 内容 | 状态 |
| --- | --- | --- |
| 工程骨架 | 六模块 DDD 分层、统一响应体、异常枚举、线程池配置 | ✅ 已完成 |
| 数据层 | `group_buy_activity` / `group_buy_discount` 两张表及对应 DAO、PO、Mapper | ✅ 已完成 |
| 触发层 | `trigger` 模块（http / job / listener） | 🚧 占位，尚未实现 Controller |
| 领域层 | `domain` 模块（model / service / adapter） | 🚧 占位，尚未实现领域模型 |
| 业务链路 | 优惠试算、锁单成团、退单解锁 | ⬜ 待开发 |

当前阶段**还没有任何 HTTP 接口**，应用能正常启动即代表工程跑通；数据层可通过两个 DAO 测试类验证。

## 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 8 | 编译与运行均须 JDK 8，见下方注意事项 |
| Spring Boot | 2.7.12 | 基础框架 |
| MyBatis | 2.1.4 | ORM，XML 方式管理 SQL |
| MySQL Connector | 8.0.22 | 数据库驱动 |
| Guava | 32.1.3-jre | 本地缓存、工具类 |
| Fastjson | 2.0.28 | JSON 序列化 |
| Lombok | — | 简化 PO / 枚举代码 |
| Maven | 3.9+ | 构建工具 |

## 工程结构

采用 DDD 四层架构，依赖方向自上而下单向收敛：

```
group-buy-market/
├── group-buy-market-api              # 接口层：对外 DTO、统一响应体 Response
├── group-buy-market-trigger          # 触发层：HTTP 接口、定时任务、消息监听（占位）
├── group-buy-market-domain           # 领域层：聚合、实体、值对象、领域服务（占位）
├── group-buy-market-infrastructure   # 基础设施层：DAO、PO、外部服务网关、Redis（部分占位）
├── group-buy-market-types            # 通用层：常量、枚举、自定义异常
└── group-buy-market-app              # 启动层：启动类、配置、Mapper XML、日志
```

各模块职责与依赖关系：

| 模块 | 职责 | 依赖 |
| --- | --- | --- |
| `api` | 定义对外契约（DTO、响应体），不含业务逻辑 | `types` |
| `trigger` | 接收外部请求，做参数校验与协议转换后调用领域层 | `api`、`domain`、`types` |
| `domain` | 核心业务规则所在，通过 `adapter/port` 反向依赖基础设施 | `types` |
| `infrastructure` | 数据库访问、缓存、外部接口调用，实现领域层定义的端口 | `domain` |
| `types` | 全局常量、响应码枚举、异常定义，无业务依赖 | — |
| `app` | 装配各层、Spring Boot 启动入口、环境配置 | `trigger`、`infrastructure` |

## 数据模型

两张配置表，均位于 `docs/dev-ops/mysql/sql/group_buy_market.sql`。

**`group_buy_activity` 拼团活动配置**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `activity_id` | bigint | 活动 ID（唯一） |
| `activity_name` | varchar | 活动名称 |
| `source` / `channel` | varchar | 来源 / 渠道，用于区分流量入口 |
| `goods_id` | varchar | 参与拼团的商品 ID |
| `discount_id` | varchar | 关联的折扣配置 ID |
| `group_type` | tinyint | 拼团方式：0 自动成团、1 达成目标拼团 |
| `take_limit_count` | int | 单人参与次数限制 |
| `target` | int | 成团目标人数 |
| `valid_time` | int | 拼团时长（分钟） |
| `status` | tinyint | 活动状态：0 创建、1 生效、2 过期、3 废弃 |
| `tag_id` / `tag_scope` | varchar | 人群标签规则，用于限定可见 / 可参与人群 |

**`group_buy_discount` 折扣配置**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `discount_id` | int | 折扣 ID（唯一） |
| `discount_name` / `discount_desc` | varchar | 折扣标题与描述 |
| `discount_type` | tinyint | 折扣类型：0 base、1 tag |
| `market_plan` | varchar | 营销优惠计划：ZJ 直减、MJ 满减、N 元购 |
| `market_expr` | varchar | 优惠表达式，如 `20` 表示直减 20 元 |
| `tag_id` | varchar | 人群标签，限定特定人群可享 |

## 快速开始

### 1. 环境要求

| 依赖 | 版本 | 备注 |
| --- | --- | --- |
| JDK | **8** | 必须为 8，JDK 21 下编译会失败 |
| Maven | 3.9+ | — |
| MySQL | 8.0 | 亦可用仓库内的 Docker Compose 起库 |

### 2. 初始化数据库

```bash
mysql -u root -p < docs/dev-ops/mysql/sql/group_buy_market.sql
```

脚本包含 `CREATE DATABASE`，会创建 `group_buy_market` 库、两张表，并插入一条测试折扣数据。

### 3. 配置数据库连接

仓库只提供配置模板，先复制再填写自己的账号密码：

```bash
cp group-buy-market-app/src/main/resources/application-dev.yml.example \
   group-buy-market-app/src/main/resources/application-dev.yml
```

然后编辑 `application-dev.yml`，确认数据库地址、库名与账号密码正确：

```yaml
spring:
  datasource:
    username: root
    password: your_password
    url: jdbc:mysql://127.0.0.1:3306/group_buy_market?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&useSSL=true
```

> `application-dev.yml` 已被 `.gitignore` 忽略，不会被提交，可放心填写真实密码。

### 4. 编译与启动

```bash
# 编译（须在 JDK 8 环境下）
export JAVA_HOME=/path/to/jdk8
mvn clean compile

# 启动应用，默认端口 8091
mvn -pl group-buy-market-app -am spring-boot:run
```

也可以直接在 IDEA 中打开根 `pom.xml`，将 **Project SDK 设为 1.8**，运行 `group-buy-market-app` 下的 `Application.java`。看到日志输出 `Started Application in ... seconds` 即启动成功。

打包运行：

```bash
mvn clean package -DskipTests
java -jar group-buy-market-app/target/group-buy-market-app.jar
```

### 5. 验证

```bash
# 跑数据层测试，确认 DAO 与数据库连接正常
mvn test -pl group-buy-market-app -am \
  -DskipTests=false -DfailIfNoTests=false \
  -Dtest=GroupBuyActivityDaoTest,GroupBuyDiscountDaoTest
```

测试位于 `group-buy-market-app/src/test/java/cn/bugstack/test/infrastructure/dao/`，会打印两张表的查询结果。

> 工程默认跳过单元测试（根 pom 的 `skipTests` 默认为 `true`），跑测试时需显式加 `-DskipTests=false`。也可以直接在 IDEA 中右键运行这两个测试类。

## Docker 部署

仓库提供了一套容器化配置，对应教程后期的「部署上线」章节。学习调试阶段建议本地直连 MySQL，不必启用。

| 文件 | 用途 |
| --- | --- |
| `docs/dev-ops/docker-compose-environment.yml` | 一键起 MySQL(13306)、Redis(16379)、phpMyAdmin(8899)、Redis 管理台(8081) |
| `group-buy-market-app/Dockerfile`、`build.sh` | 构建应用镜像 |
| `docs/dev-ops/docker-compose-app.yml` | 以镜像方式运行应用 |
| `docs/dev-ops/app/start.sh`、`stop.sh` | 容器启停脚本 |

```bash
# 启动依赖环境
docker compose -f docs/dev-ops/docker-compose-environment.yml up -d
```

> 注意：`docker-compose-environment.yml` 把 MySQL 映射到宿主机 **13306** 端口，而 `application-dev.yml` 默认连 **3306**，两者不一致。启用 Docker 环境后需同步修改配置端口或调整 compose 的端口映射。

## 开发计划

- [x] DDD 六模块工程骨架
- [x] 拼团活动、折扣配置的数据层与单元测试
- [ ] HTTP 接口层：拼团活动查询、试算入口
- [ ] 领域层：优惠试算的聚合与领域服务
- [ ] 引入 Redis，实现成团缓存与库存锁定
- [ ] 责任链模式重构折扣计算，支持多营销玩法叠加
- [ ] 消息驱动：成团通知、超时退单

## 常见问题

**必须用 JDK 8 吗？**

是的。根 `pom.xml` 锁定了 `maven-compiler-plugin 3.0`，在 JDK 21 下会报 `NoSuchFieldError: JCTree$JCImport.qualid`。编译前请把 `JAVA_HOME` 切到 JDK 8，或在 IDEA 的 Project SDK 中指定 1.8。

**Git Bash 里 `mvn` 报 `ClassNotFoundException: plexus.classworlds.launcher.Launcher`？**

这是 MSYS 路径转换被禁用导致的：环境变量 `MSYS_NO_PATHCONV` 与 `MSYS2_ARG_CONV_EXCL` 会让 Maven 脚本生成的 POSIX 路径原样传给 Windows 的 `java.exe`，从而加载不到 jar。与 `JAVA_HOME` 是否含空格无关。

```bash
# 临时绕过
( unset MSYS_NO_PATHCONV MSYS2_ARG_CONV_EXCL; mvn clean compile )
```

或在 `~/.bashrc` 中加一个只作用于 mvn 的包装函数：

```bash
mvn() {
  ( unset MSYS_NO_PATHCONV MSYS2_ARG_CONV_EXCL; command mvn "$@" )
}
```

**应用启动后访问 8091 返回 404？**

正常。当前阶段尚未实现任何 Controller，启动日志出现 `Started Application` 即代表工程正常。

## 参考与致谢

本项目学习并实践自 [小傅哥 bugstack](https://github.com/fuzhengwei) 的《拼团营销系统》DDD 教程，工程脚手架基于其 `xfg-frame-archetype`。感谢原作者的开源分享。

- DDD 教程：[https://bugstack.cn/md/road-map/ddd.html](https://bugstack.cn/md/road-map/ddd.html)
- 原仓库：[https://github.com/fuzhengwei/group-buy-market](https://github.com/fuzhengwei/group-buy-market)

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
