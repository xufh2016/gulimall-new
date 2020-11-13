package com.coolfish.gmall.product.dao;

import com.coolfish.gmall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 16:58:16
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
