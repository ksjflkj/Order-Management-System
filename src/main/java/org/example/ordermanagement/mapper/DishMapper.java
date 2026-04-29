package org.example.ordermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.ordermanagement.entity.Dish;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}