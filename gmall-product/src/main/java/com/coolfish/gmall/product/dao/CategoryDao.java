package com.coolfish.gmall.product.dao;

import com.coolfish.gmall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 18:41:42
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
