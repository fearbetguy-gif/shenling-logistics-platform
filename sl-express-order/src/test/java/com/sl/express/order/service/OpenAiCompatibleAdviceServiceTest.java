package com.sl.express.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sl.express.order.config.AssistantAiProperties;
import com.sl.express.order.service.impl.OpenAiCompatibleAdviceService;
import com.sl.express.order.vo.AssistantContextVO;
import com.sl.express.order.vo.DeliveryRuleVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.http.HttpMethod.POST;

class OpenAiCompatibleAdviceServiceTest {

    @Test
    void shouldReadAnswerFromDifyChatResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AssistantAiProperties properties = properties();
        OpenAiCompatibleAdviceService service = new OpenAiCompatibleAdviceService(
                builder, properties, new ObjectMapper());

        server.expect(requestTo("http://dify.test/v1/chat-messages"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(jsonPath("$.response_mode").value("blocking"))
                .andExpect(jsonPath("$.inputs.order_context").isString())
                .andRespond(withSuccess("{\"answer\":\"建议联系网点确认揽收安排。\"}", MediaType.APPLICATION_JSON));

        String answer = service.generate(context(), List.of(rule()));

        assertThat(answer).isEqualTo("建议联系网点确认揽收安排。");
        server.verify();
    }

    @Test
    void shouldFallbackWhenDifyIsUnavailable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleAdviceService service = new OpenAiCompatibleAdviceService(
                builder, properties(), new ObjectMapper());
        server.expect(requestTo("http://dify.test/v1/chat-messages"))
                .andRespond(withServerError());

        assertThat(service.generate(context(), List.of(rule()))).isEmpty();
        server.verify();
    }

    private AssistantAiProperties properties() {
        AssistantAiProperties properties = new AssistantAiProperties();
        properties.setEnabled(true);
        properties.setProvider("dify");
        properties.setBaseUrl("http://dify.test/v1");
        properties.setApiKey("test-key");
        return properties;
    }

    private AssistantContextVO context() {
        return new AssistantContextVO(null, List.of());
    }

    private DeliveryRuleVO rule() {
        return new DeliveryRuleVO("LONG_WAIT_PICKUP", "长时间未揽收",
                "超过24小时未揽收", "联系网点确认");
    }
}
