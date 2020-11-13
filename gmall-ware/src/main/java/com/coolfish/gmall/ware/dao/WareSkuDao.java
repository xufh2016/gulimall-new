package com.coolfish.gmall.ware.dao;

import com.coolfish.gmall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:22:16
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
