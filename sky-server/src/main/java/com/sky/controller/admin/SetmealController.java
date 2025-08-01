package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id查询套餐
     *  @param id
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("查询套餐{}", id);
        return Result.success(setmealService.getById(id));

    }
    /**
     * 新增套餐
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId") //key:setmealCache::id
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐{}", setmealDTO);
        setmealService.insert(setmealDTO);
        return Result.success();
    }
    /**
     * 修改套餐
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
    /**
     * 分页查询套餐
     */
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除套餐
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }
    /**
     * 套餐起售、停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("套餐起售、停售：{}", id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
