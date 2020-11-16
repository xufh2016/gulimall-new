package com.coolfish.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 18:41:42
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
