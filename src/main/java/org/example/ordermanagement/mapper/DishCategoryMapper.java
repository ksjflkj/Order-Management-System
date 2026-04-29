package org.example.ordermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.ordermanagement.entity.DishCategory;

@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}
