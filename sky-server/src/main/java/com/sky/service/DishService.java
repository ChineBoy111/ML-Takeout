package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void insert(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void delById(List<Long> ids);

    DishVO getById(Long id);


    void updateWithFlavors(DishDTO dishDTO);

    List<Dish> getByCategoryId(Long categoryId);

    void startOrStop(Integer status, Long id);

    List<DishVO> listWithFlavor(Dish dish);
}
