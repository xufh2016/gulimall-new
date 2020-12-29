package com.coolfish.gmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.member.entity.MemberEntity;
import com.coolfish.gmall.member.exception.PhoneException;
import com.coolfish.gmall.member.exception.UsernameException;
import com.coolfish.gmall.member.vo.MemberUserLoginVo;
import com.coolfish.gmall.member.vo.MemberUserRegisterVo;
import com.coolfish.gmall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:08:57
 */
public interface MemberService extends IService<MemberEntity> {

    /**
     *
     * @param params
     * @return
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     *
     * @param vo
     */
    void regist(MemberUserRegisterVo vo);

    /**
     *
     * @param phone
     * @throws PhoneException
     */
    void checkPhoneUnique(String phone) throws PhoneException;

    /**
     *
     * @param username
     * @throws UsernameException
     */
    void checkUsernameUnique(String username) throws UsernameException;

    MemberEntity login(MemberUserLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

