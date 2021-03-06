package com.coolfish.gmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-02 16:58:16
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

