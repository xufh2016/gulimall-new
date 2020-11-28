package com.coolfish.gmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.ware.entity.ShAreaEntity;

import java.util.Map;

/**
 * 全国省市区信息
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-28 11:11:16
 */
public interface ShAreaService extends IService<ShAreaEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

