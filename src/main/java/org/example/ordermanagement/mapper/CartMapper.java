package org.example.ordermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.ordermanagement.entity.Cart;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
