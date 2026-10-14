package com.sl.express.order.service.impl;

import com.sl.express.order.client.TransportServiceClient;
import com.sl.express.order.service.AiAdviceService;
import com.sl.express.order.service.DeliveryAssistantService;
import com.sl.express.order.service.DeliveryRuleService;
import com.sl.express.order.service.OrderQueryService;
import com.sl.express.order.vo.AssistantContextVO;
import com.sl.express.order.vo.AssistantAdviceVO;
import com.sl.express.order.vo.AssistantTrackVO;
import com.sl.express.order.vo.DeliveryRuleVO;
import com.sl.express.order.vo.OrderSummaryVO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class DeliveryAssistantServiceImpl implements DeliveryAssistantService {

    private final OrderQueryService orderQueryService;
    private final TransportServiceClient transportServiceClient;
    private final DeliveryRuleService deliveryRuleService;
    private final AiAdviceService aiAdviceService;

    public DeliveryAssistantServiceImpl(OrderQueryService orderQueryService,
                                        TransportServiceClient transportServiceClient,
                                        DeliveryRuleService deliveryRuleService,
                                        AiAdviceService aiAdviceService) {
        this.orderQueryService = orderQueryService;
        this.transportServiceClient = transportServiceClient;
        this.deliveryRuleService = deliveryRuleService;
        this.aiAdviceService = aiAdviceService;
    }

    @Override
    public AssistantContextVO getOrderContext(Long userId, String orderNo) {
        // 订单服务先校验订单归属，避免用户查询其他人的订单。
        OrderSummaryVO order = orderQueryService.getByOrderNo(userId, orderNo);
        List<AssistantTrackVO> tracks = Collections.emptyList();
        try {
            var result = transportServiceClient.listTracks(orderNo);
            if (result != null && result.code() == 200 && result.data() != null) {
                tracks = result.data();
            }
        } catch (Exception ignored) {
            // 轨迹服务暂时不可用时，仍返回订单基本信息。
        }
        return new AssistantContextVO(order, tracks);
    }

    @Override
    public AssistantAdviceVO getAdvice(Long userId, String orderNo) {
        AssistantContextVO context = getOrderContext(userId, orderNo);
        List<DeliveryRuleVO> matchedRules = deliveryRuleService.listEnabledRules()
                .stream()
                .filter(rule -> matches(rule.exceptionType(), context))
                .toList();

        String summary = matchedRules.isEmpty()
                ? "当前未发现明确的配送异常，请继续关注物流轨迹。"
                : "检测到 " + matchedRules.size() + " 个配送异常，请按建议处理。";
        String generatedAnswer = aiAdviceService.generate(context, matchedRules);
        return new AssistantAdviceVO(orderNo, !matchedRules.isEmpty(), matchedRules,
                summary, generatedAnswer);
    }

    /** 规则编码是稳定的机器标识，conditionText 只用于给人阅读，不直接解析中文文本。 */
    private boolean matches(String exceptionType, AssistantContextVO context) {
        OrderSummaryVO order = context.order();
        long hours = Duration.between(order.createdAt(), LocalDateTime.now()).toHours();
        return switch (exceptionType) {
            case "LONG_WAIT_PICKUP" -> "WAIT_PICKUP".equals(order.status()) && hours >= 24;
            case "LONG_IN_TRANSIT" -> ("TRANSPORTING".equals(order.status())
                    || "IN_TRANSIT".equals(order.status())) && hours >= 48;
            case "DELIVERY_DELAY" -> "DELIVERING".equals(order.status()) && hours >= 24;
            case "NO_TRACK" -> !context.tracks().isEmpty() ? false : hours >= 1;
            default -> false;
        };
    }
}
