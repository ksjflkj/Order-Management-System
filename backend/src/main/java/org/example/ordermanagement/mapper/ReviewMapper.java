package org.example.ordermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.ordermanagement.entity.Review;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}