package com.sl.express.order.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sl.express.order.config.AssistantAiProperties;
import com.sl.express.order.service.AiAdviceService;
import com.sl.express.order.vo.AssistantContextVO;
import com.sl.express.order.vo.DeliveryRuleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@EnableConfigurationProperties(AssistantAiProperties.class)
public class OpenAiCompatibleAdviceService implements AiAdviceService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAdviceService.class);

    private final RestClient restClient;
    private final AssistantAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleAdviceService(RestClient.Builder builder,
                                         AssistantAiProperties properties,
                                         ObjectMapper objectMapper) {
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generate(AssistantContextVO context, List<DeliveryRuleVO> matchedRules) {
        if (!properties.isEnabled() || properties.getApiKey() == null
                || properties.getApiKey().isBlank()) {
            return "";
        }

        try {
            String prompt = buildPrompt(context, matchedRules);
            if ("dify".equalsIgnoreCase(properties.getProvider())) {
                return generateByDify(prompt);
            }
            String body = objectMapper.writeValueAsString(java.util.Map.of(
                    "model", properties.getModel(),
                    "temperature", 0.2,
                    "messages", List.of(
                            java.util.Map.of("role", "system", "content", "你是物流客服助手，只能根据给定订单事实和规则回答，不得编造物流信息。"),
                            java.util.Map.of("role", "user", "content", prompt)
                    )
            ));
            String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception exception) {
            log.warn("大模型调用失败，使用规则建议兜底", exception);
            return "";
        }
    }

    /**
     * Dify Chat 应用使用 /chat-messages 接口，API Key 仍然只保存在订单服务后端。
     * Dify 应用中的输入变量约定为 order_context，便于在工作流中继续扩展规则和知识库。
     */
    private String generateByDify(String prompt) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "inputs", java.util.Map.of("order_context", prompt),
                "query", "请根据订单事实和命中规则生成配送异常解释与处理建议。",
                "response_mode", "blocking",
                "user", "sl-express-order"
        ));
        String response = restClient.post()
                .uri("/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(body)
                .retrieve()
                .body(String.class);
        return objectMapper.readTree(response).path("answer").asText("");
    }

    private String buildPrompt(AssistantContextVO context, List<DeliveryRuleVO> rules) {
        return "订单事实：" + context.order()
                + "；物流轨迹：" + context.tracks()
                + "；命中规则：" + rules
                + "。请用三句话以内说明原因和建议，不要执行任何订单操作。";
    }
}
