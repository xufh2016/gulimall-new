package com.coolfish.gmall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coolfish.common.utils.HttpUtils;
import com.coolfish.gmall.member.dao.MemberLevelDao;
import com.coolfish.gmall.member.entity.MemberLevelEntity;
import com.coolfish.gmall.member.exception.PhoneException;
import com.coolfish.gmall.member.exception.UsernameException;
import com.coolfish.gmall.member.vo.MemberUserLoginVo;
import com.coolfish.gmall.member.vo.MemberUserRegisterVo;
import com.coolfish.gmall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.Query;

import com.coolfish.gmall.member.dao.MemberDao;
import com.coolfish.gmall.member.entity.MemberEntity;
import com.coolfish.gmall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberUserRegisterVo vo) {
        MemberEntity entity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());
        //由于可以使用username和phone进行登录操作，所以在保存用户名和手机号之前需要检查他们的唯一性,
        //为了让controller感知异常，此处使用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());
        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());
        //密码存储，摘要算法或使用加密算法
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(password);

        baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameException();
        }
    }

    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        //1、去数据库查询

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));

        if (memberEntity == null) {
            //登陆失败
            return null;
        } else {
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            /**
             * matches(CharSequence rawPassword, String encodedPassword)
             * 第一个参数是明文密码即页面传过来的密码，第二个参数为编码后密码即存储到数据库中的密文密码
             */
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        //登陆和注册合并逻辑
        String uid = socialUser.getUid();
        //判断当前社交用户是否已经登陆过系统
        MemberEntity social_uid = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (social_uid != null) {
            //说明这个用户已经注册过了，需要更新令牌和过期时间
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(social_uid.getId());
            memberEntity.setAccessToken(social_uid.getAccessToken());
            memberEntity.setExpiresIn(social_uid.getExpiresIn());
            this.baseMapper.updateById(memberEntity);
            memberEntity.setExpiresIn(social_uid.getExpiresIn());
            memberEntity.setAccessToken(social_uid.getAccessToken());
            return memberEntity;
        } else {
            //没有查到当前社交用户对应的记录就需要注册一个
            MemberEntity entity = new MemberEntity();
            //3、查出当前社交账号的社交账号相关信息
            try {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("access_token", socialUser.getAccess_token());
                hashMap.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/user/show.json", "get", new HashMap<String, String>(), hashMap);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = (String) jsonObject.get("name");
                    String gender = (String) jsonObject.get("gender");
                    entity.setNickname(name);
                    entity.setGender("m".equals(gender) ? 1 : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            entity.setSocialUid(socialUser.getUid());
            entity.setAccessToken(socialUser.getAccess_token());
            entity.setExpiresIn(socialUser.getExpires_in() + "");
            this.baseMapper.insert(entity);
            return entity;
        }
    }

}