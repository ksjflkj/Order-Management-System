package org.example.ordermanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.ordermanagement.vo.UserDishListVO;
import org.example.ordermanagement.vo.UserMerchantDetailVO;
import org.example.ordermanagement.dto.UserMerchantQueryDTO;
import org.example.ordermanagement.vo.UserMerchantVO;


import java.util.List;

public interface UserBrowseService {

    IPage<UserMerchantVO> pageMerchants(UserMerchantQueryDTO queryDTO);

    UserMerchantDetailVO getMerchantDetail(Long merchantId);

    List<UserDishListVO> listMerchantDishes(Long merchantId);
}
