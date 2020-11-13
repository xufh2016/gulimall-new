package com.coolfish.gmall.member.dao;

import com.coolfish.gmall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:08:57
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
