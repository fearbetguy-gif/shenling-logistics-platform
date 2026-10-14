# 配送异常助手接入 Dify

## 1. 在 Dify 创建应用

创建一个 Chat 应用，提示词可以使用：

```text
你是物流配送异常助手。
你只能根据输入的订单事实、物流轨迹和命中规则回答，不得编造状态、时间或处理结果。
请用三句话以内回答：发生了什么、为什么判断为异常、用户下一步应该怎么做。
如果需要修改订单、赔付或创建工单，只能说明“需要人工确认”，不能声称已经执行。
```

在应用中声明输入变量：

```text
order_context
```

发布应用后复制应用 API Key。

## 2. 启动订单服务时配置

### 2.1 导入知识库

将 `docs/dify/delivery-exception-rules.md` 上传到 Dify 知识库，并在 Chat 应用中启用该知识库检索。建议把检索结果用于补充处理规范，但订单状态和物流轨迹仍以 Java 订单服务传入的事实为准。

### 2.2 Dify Cloud 配置

```bash
ASSISTANT_AI_ENABLED=true \
ASSISTANT_AI_PROVIDER=dify \
ASSISTANT_AI_BASE_URL=https://api.dify.ai/v1 \
ASSISTANT_AI_API_KEY=你的Dify应用APIKey \
java -jar sl-express-order/target/sl-express-order-1.0.0-SNAPSHOT.jar
```

自部署 Dify 时，把 `ASSISTANT_AI_BASE_URL` 换成你的 Dify 地址，例如：

```text
http://127.0.0.1/v1
```

## 3. 调用链

```text
前端输入订单号
→ Gateway
→ 订单服务查询订单和物流轨迹
→ 规则引擎匹配配送异常
→ 订单服务调用 Dify /chat-messages
→ 返回智能解释
→ 前端展示规则依据和模型建议
```

API Key 只放在订单服务环境变量中，不能放到 Vue 前端。
