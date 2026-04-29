package org.example.ordermanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.ordermanagement.dto.DishQueryDTO;
import org.example.ordermanagement.dto.DishSaveDTO;
import org.example.ordermanagement.dto.DishStatusDTO;
import org.example.ordermanagement.entity.Dish;

public interface DishService {

    IPage<Dish> pageMyDishes(String username, DishQueryDTO queryDTO);

    void saveDish(String username, DishSaveDTO dto);

    void updateDishStatus(String username, DishStatusDTO dto);
}
