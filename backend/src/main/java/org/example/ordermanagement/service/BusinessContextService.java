package org.example.ordermanagement.service;

/**
 * 业务数据上下文构建 Service
 * 负责为智能客服构建当前用户的业务上下文信息，
 * 以文本形式注入到 LLM 的 System Prompt 中。
 */
public interface BusinessContextService {

    /**
     * 构建普通用户的业务上下文摘要
     * 包含：最近订单列表、可用优惠券等
     */
    String buildUserContext(String username);

    /**
     * 构建商家的业务上下文摘要
     * 包含：今日订单、营业数据、差评等
     */
    String buildMerchantContext(String username);

    /**
     * 构建管理员的平台全局上下文摘要
     * 包含：平台用户数、商家数、订单总量、营收等
     */
    String buildAdminContext();
}
