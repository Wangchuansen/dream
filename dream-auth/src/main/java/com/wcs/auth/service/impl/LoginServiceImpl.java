package com.wcs.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.wcs.api.auth.bo.UserInfoInTokenBO;
import com.wcs.api.auth.constant.SysTypeEnum;
import com.wcs.api.auth.vo.LoginInfoVo;
import com.wcs.api.auth.vo.TokenInfoVO;
import com.wcs.api.core.feign.leaf.SegmentFeignClient;
import com.wcs.auth.config.LoginConfig;
import com.wcs.auth.dto.LoginParamDto;
import com.wcs.auth.manage.TokenStore;
import com.wcs.auth.mapper.UserMapper;
import com.wcs.auth.model.User;
import com.wcs.auth.service.LoginService;
import com.wcs.auth.vo.UserVo;
import com.wcs.common.constant.Constant;
import com.wcs.common.constant.LeafKey;
import com.wcs.common.exception.GlobalException;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wcs
 * @create: 2022-08-12 17:28
 * @description:
 */
@Service
public class LoginServiceImpl implements LoginService {

//    @Autowired
//    private WxFeignClient wxFeignClient;
    @Autowired
    private SegmentFeignClient segmentFeignClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private LoginConfig loginConfig;
//    @Autowired
//    private CompanyMapper companyMapper;

//    @Override
//    public ResponseResult<LoginInfoVo> loginMiniProgram(String phoneCode, String invitationUserId) {
//        LoginInfoVo result = new LoginInfoVo();
//        String mobile = wxFeignClient.getuserphonenumber(phoneCode).getData();
//        //获取手机号失败
//        if (StrUtil.isBlankIfStr(mobile)){
//            return ResponseResult.fail(ResponseEnum.CODE_NOT_REGISTER);
//        }
//
//        //获取注册信息
//        UserVo user = getUserByMobile(result,mobile,invitationUserId, SysTypeEnum.MINIPROGRAM.value());
//        UserInfoInTokenBO userInfoInTokenBO = builderUserInfo(user,SysTypeEnum.MINIPROGRAM.value());
//
//        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInTokenBO);
//        BeanUtil.copyProperties(tokenInfoVO,result);
//        return ResponseResult.success(result);
//    }

    /**
     * 构建 token信息
     */
    private UserInfoInTokenBO builderUserInfo(UserVo user, Integer sysType){
        UserInfoInTokenBO userInfoInTokenBO = new UserInfoInTokenBO();
        userInfoInTokenBO.setUserId(user.getUserId());
        userInfoInTokenBO.setSysType(sysType);
        userInfoInTokenBO.setUserType(user.getUserType());
        userInfoInTokenBO.setProgram(user.getProgram());
        userInfoInTokenBO.setCompanyId(user.getCompanyId());
        userInfoInTokenBO.setCompanyType(user.getCompanyType());
        return userInfoInTokenBO;
    }

    @Override
    public ResponseResult<LoginInfoVo> loginMiniMobile(LoginParamDto loginParamDto) {

        String mobile = loginParamDto.getMobile();
        String code = loginParamDto.getCode();
        String invitationUserId = loginParamDto.getInvitationUserId();

        LoginInfoVo result = new LoginInfoVo();

        String defaultCode = mobile.substring(7);
        //验证码
        String s = redisTemplate.opsForValue().get("mobile_login" + mobile);
        if ((StrUtil.isNotEmpty(s) && s.equals(code))
                || (loginConfig.getIsLastFour() && defaultCode.equals(code))){
            UserVo user = getUserByMobile(result,mobile,invitationUserId,loginParamDto.getSysType());
            //pc端目前仅限isv登录
            if (loginParamDto.getSysType().equals(SysTypeEnum.PCMANAGER.value())
                    && (user == null || user.getCompanyType() == null)){
                throw new GlobalException("该账号未开通ISV权限");
            }

            UserInfoInTokenBO userInfoInTokenBO = builderUserInfo(user,loginParamDto.getSysType());

            TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInTokenBO);
            BeanUtil.copyProperties(tokenInfoVO,result);
            return ResponseResult.success(result);
        }
        return ResponseResult.fail(ResponseEnum.CODE_PHONE_MSG_ERROR);
    }

    /**
     * 根据手机号获取用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVo getUserByMobile(LoginInfoVo loginInfoVo, String mobile, String invitationUserId,Integer sysType){
        synchronized (mobile) {
//            Company company = null;
            //用手机号查询是否注册
//            User user = userMapper.selByMobile(mobile);
            User user = null;
            loginInfoVo.setIsNewUser(user == null ? 1 : 0);

            //查询不到未注册
            if (user == null) {
                //小程序登录自动注册
                if (SysTypeEnum.MINIPROGRAM.value().equals(sysType)) {
                    user = User.builder()
                            .userId(segmentFeignClient.getSegmentId(LeafKey.leaf_user_id_key).getData())
                            .mobile(mobile)
                            .userType(Constant.USER_TYPE0)
                            .updateTime(LocalDateTime.now())
                            .build();
                    userMapper.insert(user);
                    if (StrUtil.isNotEmpty(invitationUserId)) {
                        Long userId = user.getUserId();
//                        userMapper.insertInvitation(Long.valueOf(invitationUserId), userId);
                    }
                } else {
                    return null;
                }
            } else {
//                company = companyMapper.queryByUserId(user.getUserId());
//                //用户类型为企业用户 且查不到企业信息，为客服人员
//                if (user.getUserType() == 1 && company == null) {
//                    company = companyMapper.queryByEmpUserId(user.getUserId());
//                }

            }
            UserVo result = new UserVo();
            BeanUtil.copyProperties(user,result);

//            if (company != null){
//                result.setCompanyId(company.getCompanyId());
//                result.setCompanyType(company.getCompanyType());
//            }

            return result;
        }

    }

    /**
     * 根据id获取用户信息
     */
    @Override
    public UserInfoInTokenBO getUserByUseId(Long userId,Integer sysType){
        //用手机号查询是否注册
//        User user = userMapper.queryById(userId);
//        if (user == null){
//            throw new GlobalException(ResponseEnum.REFRESH_TOKEN_EXPIRED);
//        }
//        Company company = companyMapper.queryByUserId(user.getUserId());
//        //用户类型为企业用户 且查不到企业信息，为客服人员
//        if(user.getUserType() == 1 && company == null){
//            company = companyMapper.queryByEmpUserId(user.getUserId());
//        }

        UserVo userVo = new UserVo();
//        BeanUtil.copyProperties(user,userVo);
//        if (company != null){
//            userVo.setCompanyId(company.getCompanyId());
//            userVo.setCompanyType(company.getCompanyType());
//        }

        return builderUserInfo(userVo,sysType);
    }
}

