package com.coolfish.gmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.member.entity.MemberCollectSpuEntity;

import java.util.Map;

/**
 * 会员收藏的商品
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:08:57
 */
public interface MemberCollectSpuService extends IService<MemberCollectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

