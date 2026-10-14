# 神领物流——微服务物流管理平台

一个适合 Java 后端求职展示的精简物流系统，重点体现微服务、Redis、RabbitMQ、MySQL、JWT 和异常处理。

## 技术栈

- Java 17、Spring Boot 3.2.4
- Spring Cloud 2023.0.1、Spring Cloud Alibaba 2023.0.1.0
- Spring Cloud Gateway、Nacos、OpenFeign
- MySQL 8、MyBatis-Plus、Redis、RabbitMQ
- Maven、JUnit 5、Docker Compose

## 模块说明

| 模块 | 作用 |
| --- | --- |
| `sl-express-common` | 统一返回、业务异常、公共事件 |
| `sl-express-gateway` | 统一入口、JWT 校验、路由转发 |
| `sl-express-user` | 注册、登录、用户信息、地址能力基础 |
| `sl-express-order` | 创建、查询、取消订单、Redis 缓存、订单状态 |
| `sl-express-transport` | 消费订单事件、运输任务、状态流转、物流轨迹 |
| `sl-express-web` | 轻量接口演示页面，非核心业务模块 |

## 启动基础设施

先停止本机已经占用 3307、6379、5672、8848 端口的服务，然后执行：

```bash
docker compose up -d
docker compose ps
```

服务地址：

- Nacos：http://127.0.0.1:8848/nacos
- RabbitMQ：http://127.0.0.1:15672，账号 `guest`，密码 `guest`
- MySQL：`127.0.0.1:3307`，数据库 `wuliu`
- Redis：`127.0.0.1:6379`

MySQL 初始化脚本会按文件名顺序执行 `docs/sql` 下的 SQL。已有 MySQL 数据卷时，新增 SQL 不会自动重复执行，可手动导入：

```bash
MYSQL_PWD=root mysql --protocol=tcp -h127.0.0.1 -P3307 -uroot \\
  < docs/sql/006_route.sql
```

## 编译

```bash
mvn clean test
```

## 启动后端

在项目根目录分别启动以下模块：

```bash
java -jar sl-express-gateway/target/sl-express-gateway-1.0.0-SNAPSHOT.jar
USER_DB_PASSWORD=root java -jar sl-express-user/target/sl-express-user-1.0.0-SNAPSHOT.jar
ORDER_DB_PASSWORD=root java -jar sl-express-order/target/sl-express-order-1.0.0-SNAPSHOT.jar
TRANSPORT_DB_PASSWORD=root java -jar sl-express-transport/target/sl-express-transport-1.0.0-SNAPSHOT.jar
```

端口依次为：Gateway `8090`、User `8081`、Order `8082`、Transport `8083`。

## 核心演示流程

1. 注册或使用演示账号：`13800138000 / 123456`
2. 登录获取 JWT
3. 通过 Gateway 创建订单，并携带唯一 `X-Request-Id`
4. RabbitMQ 发布 `order.created` 事件
5. Transport 消费事件并创建 `WAIT_ASSIGN` 运输任务
6. 快递员领取任务
7. 依次更新运输任务状态 `PICKING_UP`、`IN_TRANSIT`、`DELIVERING`、`SIGNED`；订单服务中的对应状态为 `PICKED_UP`、`TRANSPORTING`、`DELIVERING`、`SIGNED`
8. 查询订单详情和物流轨迹

主要接口：

```text
POST /api/user/login
POST /api/order
GET  /api/order/{orderNo}
POST /api/order/{orderNo}/cancel
POST /api/transport/tasks/{orderNo}/claim
POST /api/transport/tasks/{orderNo}/status
GET  /api/transport/tasks/{orderNo}/tracks
GET  /api/transport/routes/shortest?start=WAREHOUSE&end=CUSTOMER
```

## 关键设计

- 创建订单使用 Redis Lua 脚本，以 `userId + X-Request-Id` 防重复提交。
- 订单详情缓存 10 分钟，状态更新和取消订单时删除缓存。
- RabbitMQ 使用 Topic Exchange 解耦订单服务和运输服务。
- 消费记录通过 `(message_type, message_key)` 唯一索引实现幂等。
- 消费失败最多重试 3 次，最终进入死信队列。
- Feign 状态同步失败后写入补偿表，每 30 秒自动重试，最多 10 次。
- 运输状态使用枚举限制非法跳转。
- OpenFeign 同步运输状态到订单服务，并配置调用超时。
- 路线规划从 `route_node`、`route_edge` 读取有向图，使用 Dijkstra 求最短路径；示例数据返回 `38.000 km`。
- 配送异常助手由后端负责权限校验、订单事实整理和规则判断，Dify/DeepSeek 只负责把结构化事实生成自然语言建议。

## 面试重点

可以重点说明：缓存一致性、Lua 原子性、消息重复消费、数据库唯一索引、状态机、Gateway 鉴权、Feign 超时与服务降级、全局异常处理和 Docker 化部署。
