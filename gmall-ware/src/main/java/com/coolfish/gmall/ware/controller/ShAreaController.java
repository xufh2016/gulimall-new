package com.coolfish.gmall.ware.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coolfish.gmall.ware.entity.ShAreaEntity;
import com.coolfish.gmall.ware.service.ShAreaService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.R;



/**
 * 全国省市区信息
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:22:16
 */
@RestController
@RequestMapping("ware/sharea")
public class ShAreaController {
    @Autowired
    private ShAreaService shAreaService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = shAreaService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Integer id){
		ShAreaEntity shArea = shAreaService.getById(id);

        return R.ok().put("shArea", shArea);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ShAreaEntity shArea){
		shAreaService.save(shArea);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody ShAreaEntity shArea){
		shAreaService.updateById(shArea);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
		shAreaService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
