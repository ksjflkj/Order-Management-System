package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ordermanagement.entity.DishCategory;
import org.example.ordermanagement.mapper.DishCategoryMapper;
import org.example.ordermanagement.service.DishCategoryService;
import org.springframework.stereotype.Service;

@Service
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory> implements DishCategoryService {
}
