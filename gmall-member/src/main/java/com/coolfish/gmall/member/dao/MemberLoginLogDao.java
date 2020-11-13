package com.coolfish.gmall.member.dao;

import com.coolfish.gmall.member.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:08:57
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
