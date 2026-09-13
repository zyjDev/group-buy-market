# 拼团营销系统 · group-buy-market

<div align="center">

![JDK](https://img.shields.io/badge/JDK-8%20%7C%2017%20%7C%2021-007396?logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/Spring%20Boot-2.7.12-6DB33F?logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-2.1.4-FE4648)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-6.2-DC382D?logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-FF6600?logo=rabbitmq&logoColor=white)
![DDD](https://img.shields.io/badge/Architecture-DDD-blue)

基于 DDD 分层架构的拼团营销系统，覆盖营销试算、锁单、成团结算、退款和库存恢复完整链路。

</div>

## 项目功能

- 拼团活动、折扣、商品和渠道关系查询
- 人群标签可见性与参与资格校验
- 直减、满减、折扣、N 元购优惠试算
- 新团创建、已有团参团、用户参与次数限制
- Redis 库存占用与支付结果幂等
- 拼团进度统计和成团结算
- 未支付、已支付未成团、已支付已成团三种退款类型
- HTTP/MQ 成团通知、本地消息任务和失败补偿
- 超时未支付订单扫描与自动退单
- DCC 动态开关：降级、切量和限流
- Guava 限流、TraceId、Prometheus/Grafana 相关配置

## 技术栈

| 组件 | 版本 | 用途 |
| --- | --- | --- |
| JDK | 8+ | 源码与字节码目标为 Java 8，可使用 JDK 8、17 或 21 构建 |
| Maven | 3.9+ | 多模块构建 |
| Spring Boot | 2.7.12 | Web、配置、任务和依赖装配 |
| MyBatis | 2.1.4 | DAO 与 XML SQL 映射 |
| MySQL | 8.0 | 活动、订单、任务和标签数据 |
| Redis / Redisson | 6.2 / 3.26.0 | 分布式锁、库存、缓存和 DCC |
| RabbitMQ | 3.12 | 成团与退款异步通知 |
| OkHttp | 3.14.9 | HTTP 回调 |
| Fastjson | 2.0.28 | JSON 序列化 |
| Guava | 32.1.3-jre | 本地缓存和限流 |
| Logstash | 可选 | 日志采集 |

## 模块结构

```text
group-buy-market/
├── group-buy-market-api
│   └── 对外 DTO、服务接口、统一 Response
├── group-buy-market-types
│   └── 通用枚举、异常、常量
├── group-buy-market-domain
│   └── 活动、标签、交易领域模型与领域服务
├── group-buy-market-infrastructure
│   └── DAO、Repository、Redis、网关和事件发布实现
├── group-buy-market-trigger
│   └── HTTP Controller、定时任务、MQ Listener
└── group-buy-market-app
    └── 启动类、环境配置、线程池、日志、Mapper XML
```

依赖方向：

```text
app -> trigger -> domain -> types
          |
          +-> infrastructure -> domain
```

`domain` 只定义 Repository/Port 接口，`infrastructure` 提供实现，业务规则不直接依赖数据库和 Redis 细节。

## 核心链路

### 1. 营销试算

`IndexGroupBuyMarketServiceImpl` 通过责任链和异步节点加载活动、折扣、SKU、拼团数据，计算原价、优惠金额、实付金额以及用户可见/可参与状态。

### 2. 锁单

`TradeLockOrderService` 依次执行活动可用性、用户参与次数、组队库存规则，然后创建或加入 `group_buy_order`，写入 `group_buy_order_list`，并使用 Redis 原子占用库存。

### 3. 支付结算

`TradeSettlementOrderService` 校验外部交易单号、SC 黑名单、拼团有效时间和订单状态，更新订单为已完成并推进拼团进度。团队达标后生成本地通知任务并投递 HTTP 或 RabbitMQ 消息。

### 4. 退款和库存恢复

`TradeRefundOrderService` 根据“订单状态 + 团队状态”选择退款策略：

| 场景 | 策略 | 处理 |
| --- | --- | --- |
| 未支付、未成团 | `unpaid_unlock` | 关闭订单、释放锁单量 |
| 已支付、未成团 | `paid_unformed` | 退款、释放锁单和完成量 |
| 已支付、已成团 | `paid_formed` | 退款、维护成团状态并通知业务 |

退款完成后通过 MQ 消费结果恢复 Redis 团队库存。

## 数据表

初始化脚本位于 `docs/dev-ops/mysql/sql/2-29-group_buy_market.sql`，共 10 张表：

| 表 | 用途 |
| --- | --- |
| `group_buy_activity` | 拼团活动配置 |
| `group_buy_discount` | 优惠折扣配置 |
| `group_buy_order` | 团队维度拼团订单 |
| `group_buy_order_list` | 用户订单明细 |
| `sc_sku_activity` | 渠道、商品与活动关系 |
| `sku` | 商品信息 |
| `crowd_tags` | 人群标签 |
| `crowd_tags_detail` | 人群标签明细 |
| `crowd_tags_job` | 人群计算任务 |
| `notify_task` | 本地消息和失败补偿任务 |

## HTTP 接口

服务默认端口为 `8091`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/gbm/index/query_group_buy_market_config` | 查询营销配置和优惠试算 |
| POST | `/api/v1/gbm/trade/lock_market_pay_order` | 创建或加入拼团并锁定库存 |
| POST | `/api/v1/gbm/trade/settlement_market_pay_order` | 支付成功结算 |
| POST | `/api/v1/gbm/trade/refund_market_pay_order` | 退款、退单和库存恢复 |
| GET | `/api/v1/gbm/dcc/update_config` | 动态修改配置 |
| POST | `/api/v1/test/group_buy_notify` | 本地 HTTP 回调测试接口 |

示例请求：

```json
POST /api/v1/gbm/index/query_group_buy_market_config
{
  "userId": "GROUP_BUY01",
  "source": "s01",
  "channel": "c01",
  "goodsId": "9890001"
}
```

```json
POST /api/v1/gbm/trade/lock_market_pay_order
{
  "userId": "user01",
  "teamId": null,
  "activityId": 100123,
  "goodsId": "9890001",
  "source": "s01",
  "channel": "c01",
  "outTradeNo": "202609130001",
  "notifyConfigVO": {
    "notifyType": "MQ"
  }
}
```

`notifyConfigVO.notifyType` 支持 `MQ` 和 `HTTP`；使用 `HTTP` 时必须提供 `notifyUrl`。

```json
POST /api/v1/gbm/trade/settlement_market_pay_order
{
  "userId": "GROUP_BUY03",
  "source": "s01",
  "channel": "c01",
  "outTradeNo": "769515763172",
  "outTradeTime": "2025-04-05T14:55:00+08:00"
}
```

```json
POST /api/v1/gbm/trade/refund_market_pay_order
{
  "userId": "GROUP_BUY05",
  "source": "s01",
  "channel": "c01",
  "outTradeNo": "946916695095"
}
```

动态配置示例：

```text
GET /api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1
GET /api/v1/gbm/dcc/update_config?key=cutRange&value=0
GET /api/v1/gbm/dcc/update_config?key=rateLimiterSwitch&value=close
```

## 本地启动

### 1. 环境要求

- JDK 8、17 或 21
- Maven 3.9+
- MySQL 8.0
- Redis 6+
- RabbitMQ 3.12，端到端验证 MQ 回调时需要

### 2. 初始化数据库

```bash
mysql --host=127.0.0.1 --port=3306 --user=root -p \
  --default-character-set=utf8mb4 \
  < docs/dev-ops/mysql/sql/2-29-group_buy_market.sql
```

脚本会创建 `group_buy_market` 数据库、10 张表，并写入可重复使用的演示数据。

### 3. 配置本地连接

```bash
cp group-buy-market-app/src/main/resources/application-dev.yml.example \
   group-buy-market-app/src/main/resources/application-dev.yml
```

支持以下环境变量覆盖：

| 变量 | 默认值 |
| --- | --- |
| `MYSQL_HOST` / `MYSQL_PORT` | `127.0.0.1` / `3306` |
| `MYSQL_DATABASE` | `group_buy_market` |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | `root` / 配置模板中的占位值 |
| `REDIS_HOST` / `REDIS_PORT` | `127.0.0.1` / `6379` |
| `RABBITMQ_ADDRESS` / `RABBITMQ_PORT` | `127.0.0.1` / `5672` |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | `guest` / 配置模板中的占位值 |

### 4. 启动应用

在 IDEA 中将 Project SDK 设置为 JDK 8 或更高版本，运行 `com.groupbuy.market.Application`。

命令行方式：

```bash
mvn clean package -DskipTests
java -jar group-buy-market-app/target/group-buy-market-app.jar
```

启动成功后访问：

```text
http://127.0.0.1:8091/actuator/health
```

### 5. Docker 启动依赖

`docs/dev-ops/docker-compose-environment.yml` 提供 MySQL、Redis、RabbitMQ 和管理页面。

先在本地复制环境变量模板并修改占位值：

```bash
cp docs/dev-ops/.env.example docs/dev-ops/.env
```

然后启动依赖：

```bash
docker compose --env-file docs/dev-ops/.env -f docs/dev-ops/docker-compose-environment.yml up -d
```

容器端口与本地应用配置不同，启动应用时需要把 `MYSQL_PORT`、`REDIS_PORT` 等变量设置为模板中对应端口。RabbitMQ 管理页面位于 `http://127.0.0.1:15672`，账号和密码取自本地 `.env`。

`.env` 已被忽略，只会在本机生效；不要提交真实密码、令牌或云服务密钥。

## 测试

测试会修改订单状态，重复执行前建议重新导入初始化 SQL。

```bash
mvn test -DskipTests=false \
  -Dspring.rabbitmq.listener.simple.auto-startup=false
```

当前测试覆盖营销试算、锁单、结算、三种退款路径、DAO、Redis 锁、责任链、MQ 端口探测、HTTP 回调和 Controller。

## 注意事项

- 项目源码和字节码目标为 Java 8，可使用 JDK 8、17 或 21 构建。
- 根 POM 默认跳过测试，执行测试时必须显式添加 `-DskipTests=false`。
- RabbitMQ 未启动时可以运行应用和大部分测试，但异步通知会进入本地任务重试。
- 未启动 Logstash 时，`127.0.0.1:4560` 的连接警告不影响业务和测试结果。
- Redis 管理页面：`http://127.0.0.1:8081`；phpMyAdmin：`http://127.0.0.1:8899`。
