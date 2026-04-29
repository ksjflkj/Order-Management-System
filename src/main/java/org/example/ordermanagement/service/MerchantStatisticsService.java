package org.example.ordermanagement.service;

import org.example.ordermanagement.vo.MerchantStatisticsVO;

public interface MerchantStatisticsService {
    MerchantStatisticsVO getStatistics(String username);
}
