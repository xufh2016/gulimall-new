package com.coolfish.gmall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coolfish.gmall.product.entity.CategoryEntity;
import com.coolfish.gmall.product.service.CategoryService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.R;


/**
 * 商品三级分类
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 18:41:42
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表，查出所有分类以及子分类，并以树状结构组装
     */
    @RequestMapping("/list/tree")
    public R list() {
        List<CategoryEntity> categoryEntityList = categoryService.listWithTree();


        return R.ok().put("data", categoryEntityList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 批量修改
     * springMVC会自动将实体类封装成对象数组传递到后台中
     */
    @RequestMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] category) {
        System.out.println(category.length + "--------------------------------");
        if (category.length > 0) {
            categoryService.updateBatchById(Arrays.asList(category));
            return R.ok();
        } else {
            return R.error("尚未选择任何数据");
        }
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateCascader(category);

        return R.ok();
    }

    /**
     * 删除
     *
     * @RequestBody : 获取请求体里面的内容，只有post请求才有请求体
     * springmvc会自动将请求体里的数据转为对应的对象
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] catIds) {

        if ((catIds.length > 0)) {
            categoryService.removeMenuByIds(Arrays.asList(catIds));
            return R.ok();
        } else {
            return R.error();
        }


    }

}
