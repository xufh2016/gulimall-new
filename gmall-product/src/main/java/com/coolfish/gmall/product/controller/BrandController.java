package com.coolfish.gmall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.coolfish.common.valid.AddGroup;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coolfish.gmall.product.entity.BrandEntity;
import com.coolfish.gmall.product.service.BrandService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.R;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * 品牌
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 18:41:42
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
//        if (result.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            result.getFieldErrors().forEach(fieldError -> {
//                String defaultMessage = fieldError.getDefaultMessage();
//                String field = fieldError.getField();
//                map.put(field,defaultMessage);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        }else{
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody BrandEntity brand) {
        if (null != brand) {
            brandService.updateDetail(brand);
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 修改status
     */
    @RequestMapping("/update/status")
    public R updateStatus(@RequestBody BrandEntity brand) {
        if (null != brand) {
            brandService.updateById(brand);
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
