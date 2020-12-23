package com.coolfish.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.product.entity.SkuInfoEntity;
import com.coolfish.gmall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 16:58:16
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId);
}

