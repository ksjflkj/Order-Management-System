package org.example.ordermanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.ordermanagement.dto.MerchantApplyDTO;
import org.example.ordermanagement.dto.MerchantAuditDTO;
import org.example.ordermanagement.dto.MerchantQueryDTO;
import org.example.ordermanagement.dto.MerchantStatusDTO;
import org.example.ordermanagement.entity.Merchant;

import java.util.List;

public interface MerchantService {

    IPage<Merchant> pageMerchants(MerchantQueryDTO queryDTO);

    void auditMerchant(MerchantAuditDTO dto);

    void updateMerchantStatus(MerchantStatusDTO dto);

    void applyMerchant(String username, MerchantApplyDTO dto);

    /**
     * 获取用户的所有商家（只有审核通过的）
     */
    List<Merchant> getMyMerchants(String username);

    /**
     * 获取用户的所有商家（包括待审核的）
     */
    List<Merchant> getAllMyMerchants(String username);

    /**
     * 商家更新自己的店铺信息
     */
    void updateMyMerchant(String username, Merchant merchant);

    /**
     * 切换店铺营业状态（上班/打烊）
     * @param username 操作者用户名
     * @param merchantId 店铺ID
     * @param open true=营业 false=打烊
     */
    void toggleShopOpen(String username, Long merchantId, boolean open);
}