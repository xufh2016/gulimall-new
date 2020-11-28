package com.coolfish.gmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.ware.entity.FeightTemplateEntity;

import java.util.Map;

/**
 * 运费模板
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-28 11:11:16
 */
public interface FeightTemplateService extends IService<FeightTemplateEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

