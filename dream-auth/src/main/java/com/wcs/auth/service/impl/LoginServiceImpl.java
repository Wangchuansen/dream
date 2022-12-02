package com.wcs.auth.service.impl;

import com.wcs.api.core.feign.leaf.SegmentFeignClient;
import com.wcs.auth.mapper.UserMapper;
import com.wcs.auth.model.User;
import com.wcs.auth.service.LoginService;
import com.wcs.common.constant.LeafKey;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author: wcs
 * @create: 2022-08-12 17:28
 * @description:
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SegmentFeignClient segmentFeignClient;

    @Override
    public ResponseResult<Map<String, Object>> login(String mobile,String code) {
        String defaultCode = mobile.substring(7);

        if (!defaultCode.equals(code)) {
            return ResponseResult.fail(ResponseEnum.CODE_PHONE_MSG_ERROR);
        }

        User user = getUserByMobile(mobile);



//        UserInfoInTokenBO userInfoInTokenBO = new UserInfoInTokenBO();
//        userInfoInTokenBO.setUserId(user.getUserId());
//        userInfoInTokenBO.setSysType(SysTypeEnum.MINIPROGRAM.value());
//
//        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInTokenBO);
//        BeanUtil.copyProperties(tokenInfoVO,result);
//        return ResponseResult.success(result);

        return null;

    }

    private User getUserByMobile(String mobile) {
        //用手机号查询是否注册
        User user = userMapper.queryByMobile(mobile);
        //查询不到未注册
        if (user == null){
            user = User.builder()
                    .userId(segmentFeignClient.getSegmentId(LeafKey.leaf_user_id_key).getData())
                    .mobile(mobile)
                    .updateTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
//            if (StrUtil.isNotEmpty(invitationUserId)){
//                Long userId = user.getUser_id();
//                userMapper.insertInvitation(Long.valueOf(invitationUserId),userId);
//            }
        }

        return user;
    }

    @Override
    public ResponseResult<User> userInfo() {
        String[] roles = {"admin"};
//        User user = new User();
//        user.setName("zs");
//        user.setRoles(roles);
//        user.setIntroduction("I am a super administrator");
//        user.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
//        Map<String,Object> map = new HashMap<>();
//        map.put("admin-token",user);
        return ResponseResult.success();
    }
}
