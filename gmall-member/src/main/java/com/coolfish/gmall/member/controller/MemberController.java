package com.coolfish.gmall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.coolfish.common.exception.BizCodeEnum;
import com.coolfish.gmall.member.exception.PhoneException;
import com.coolfish.gmall.member.exception.UsernameException;
import com.coolfish.gmall.member.feign.CouponFeignService;
import com.coolfish.gmall.member.vo.MemberUserLoginVo;
import com.coolfish.gmall.member.vo.MemberUserRegisterVo;
import com.coolfish.gmall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.coolfish.gmall.member.entity.MemberEntity;
import com.coolfish.gmall.member.service.MemberService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.R;


/**
 * 会员
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:08:57
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    /**
     * 获取优惠券
     */
    @RequestMapping("/coupons")
    public R testCoupon() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("sfsdfs");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok("success").put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberUserRegisterVo vo) {
        try {
            memberService.regist(vo);
        } catch (UsernameException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        } catch (PhoneException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberUserLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
//        return memberEntity!=null? R.ok():R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMessage());
        if (memberEntity != null) {
            //todo 登录成功处理
            return R.ok();
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser){
        MemberEntity memberEntity = memberService.login(socialUser);
        if (memberEntity==null) {
            return R.error();
        }else {
            return R.ok();
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
