package com.coolfish.gmall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coolfish.gmall.coupon.entity.SpuFullReductionEntity;
import com.coolfish.gmall.coupon.service.SpuFullReductionService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.R;



/**
 * 商品满减信息
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:12:01
 */
@RestController
@RequestMapping("coupon/spufullreduction")
public class SpuFullReductionController {
    @Autowired
    private SpuFullReductionService spuFullReductionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuFullReductionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SpuFullReductionEntity spuFullReduction = spuFullReductionService.getById(id);

        return R.ok().put("spuFullReduction", spuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuFullReductionEntity spuFullReduction){
		spuFullReductionService.save(spuFullReduction);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuFullReductionEntity spuFullReduction){
		spuFullReductionService.updateById(spuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		spuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
