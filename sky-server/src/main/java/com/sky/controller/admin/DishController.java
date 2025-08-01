package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO ) {
        log.info("新增菜品{}",dishDTO);
        //新增菜品，删除缓存数据
        Long categoryId = dishDTO.getCategoryId();
        String key = "dish_" + categoryId;
        redisTemplate.delete(key);

        dishService.insert(dishDTO);
        return Result.success();
    }
    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 根据ID来删除菜品
     */
    @DeleteMapping
    @ApiOperation("根据ID来删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品{}", ids);
        dishService.delById(ids);

        deleteCache("dish_*");

        return Result.success();
    }
    /**
     * 根据菜品ID查询菜品
     */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品ID查询菜品")
    public Result<DishVO>  getById(@PathVariable Long id){
        log.info("查询菜品{}", id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }
    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品{}", dishDTO);
        dishService.updateWithFlavors(dishDTO);

        deleteCache("dish_*");

        return Result.success();
    }
    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")

    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        return Result.success(dishService.getByCategoryId(categoryId));
    }
    /**
     * 起售、停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("套餐起售、停售：{}", id);
        dishService.startOrStop(status, id);

        deleteCache("dish_*");

        return Result.success();
    }
    /**
     * 删除缓存中的菜品数据
     */
    private void deleteCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }



}
