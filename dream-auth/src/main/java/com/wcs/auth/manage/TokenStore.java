package com.wcs.auth.manage;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wcs.api.auth.bo.UserInfoInTokenBO;
import com.wcs.api.auth.constant.SysTypeEnum;
import com.wcs.api.auth.vo.TokenInfoVO;
import com.wcs.auth.service.LoginService;
import com.wcs.common.cache.constant.CacheNames;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.security.bo.TokenInfoBO;
import com.wcs.common.util.PrincipalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: wcs
 * @create: 2023-01-09 9:26
 * @description:
 */
@Component
@RefreshScope
@Slf4j
public class TokenStore {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final RedisSerializer<Object> redisSerializer;
    private final StringRedisTemplate stringRedisTemplate;
    private final LoginService loginService;

    public TokenStore(RedisTemplate<Object, Object> redisTemplate, RedisSerializer<Object> redisSerializer,
                      StringRedisTemplate stringRedisTemplate, LoginService loginService) {
        this.redisTemplate = redisTemplate;
        this.redisSerializer = redisSerializer;
        this.stringRedisTemplate = stringRedisTemplate;
        this.loginService = loginService;
    }

    /**
     * 将用户的部分信息存储在token中，并返回token信息
     * @param userInfoInToken 用户在token中的信息
     * @return token信息
     */
    public TokenInfoBO storeAccessToken(UserInfoInTokenBO userInfoInToken) {
        TokenInfoBO tokenInfoBO = new TokenInfoBO();
        //请求token
        String accessToken = IdUtil.simpleUUID();
        //刷新的token
        String refreshToken = IdUtil.simpleUUID();
        //存储用户信息
        tokenInfoBO.setUserInfoInToken(userInfoInToken);
        //设置token过期时间
        tokenInfoBO.setAccessExpiresIn(getExpiresIn(userInfoInToken.getSysType(),true));
        tokenInfoBO.setRefreshExpiresIn(getExpiresIn(userInfoInToken.getSysType(),false));


        //获取key 示例： njydzq_oauth:uid_to_access:1:10001
        String uidToAccessKeyStr = getUidToAccessKey(getApprovalKey(userInfoInToken));
        //示例 ：njydzq_oauth:token:access:sdlfsdhjfhefieondfsoksdl
        String accessKeyStr = getAccessKey(accessToken);
        //示例 ：njydzq_oauth:token:refresh_to_access:lwekewllwekkewkksdllds
        String refreshToAccessKeyStr = getRefreshToAccessKey(refreshToken);

        // 一个用户会登陆很多次，每次登陆的token都会存在 uid_to_access里面
        // 但是每次保存都会更新这个key的时间，而key里面的token有可能会过期，过期就要移除掉
        List<String> existsAccessTokens = new ArrayList<>();
        // 新的token数据
        existsAccessTokens.add(accessToken + StrUtil.COLON + refreshToken);

        //获取key数据长度
        Long size = redisTemplate.opsForSet().size(uidToAccessKeyStr);
        if (size != null && size != 0) {
            //随机移除size个数据,并返回这些移除的数据
            List<String> tokenInfoBoList = stringRedisTemplate.opsForSet().pop(uidToAccessKeyStr, size);
            if (tokenInfoBoList != null) {
                for (String accessTokenWithRefreshToken : tokenInfoBoList) {
                    String[] accessTokenWithRefreshTokenArr = accessTokenWithRefreshToken.split(StrUtil.COLON);
                    String accessTokenData = accessTokenWithRefreshTokenArr[0];
                    if (stringRedisTemplate.hasKey(getAccessKey(accessTokenData))) {
                        //添加旧的token
                        existsAccessTokens.add(accessTokenWithRefreshToken);
                    }
                }
            }
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            byte[] uidKey = uidToAccessKeyStr.getBytes(StandardCharsets.UTF_8);
            byte[] refreshKey = refreshToAccessKeyStr.getBytes(StandardCharsets.UTF_8);
            byte[] accessKey = accessKeyStr.getBytes(StandardCharsets.UTF_8);

            for (String existsAccessToken : existsAccessTokens) {
                connection.sAdd(uidKey, existsAccessToken.getBytes(StandardCharsets.UTF_8));
            }

            // 通过uid + sysType 保存access_token，当需要禁用用户的时候，可以根据uid + sysType 禁用用户
            connection.expire(uidKey, tokenInfoBO.getAccessExpiresIn());

            // 通过refresh_token获取用户的access_token从而刷新token
            connection.setEx(refreshKey, tokenInfoBO.getRefreshExpiresIn(),
                    (accessToken+"_"+userInfoInToken.getUserId()+"_"+userInfoInToken.getSysType()).getBytes(StandardCharsets.UTF_8));

            // 通过access_token保存用户的租户id，用户id，uid
            connection.setEx(accessKey, tokenInfoBO.getAccessExpiresIn(), Objects.requireNonNull(redisSerializer.serialize(userInfoInToken)));

            return null;
        });

        // 返回给前端是加密的token
        tokenInfoBO.setAccessToken(encryptToken(accessToken,userInfoInToken.getSysType()));
        tokenInfoBO.setRefreshToken(encryptToken(refreshToken,userInfoInToken.getSysType()));

        return tokenInfoBO;
    }

    /**
     * 获取失效时间
     * @param sysType
     * @param isAccessToken
     */
    private int getExpiresIn(int sysType,boolean isAccessToken) {
        // 3600秒
        int expiresIn = 3600;

        // 小程序过期时间 1小时
        if (Objects.equals(sysType, SysTypeEnum.MINIPROGRAM.value())) {
//			expiresIn = isAccessToken?expiresIn * 2 : expiresIn * 24 * 15;
//			expiresIn = isAccessToken?15 * 2 : expiresIn * 24 * 15;
            expiresIn = isAccessToken?expiresIn * 24 * 15 : expiresIn * 24 * 15;
        }
        // pc后台token过期时间 2小时
        if (Objects.equals(sysType, SysTypeEnum.PCMANAGER.value())) {
//			expiresIn = isAccessToken?expiresIn * 2 : expiresIn * 24;
//			expiresIn = isAccessToken?15 * 2 : expiresIn * 24;
            expiresIn = isAccessToken?expiresIn * 24 : expiresIn * 24;
        }
        return expiresIn;
    }

    /**
     * 根据accessToken 获取用户信息
     * @param accessToken accessToken
     * @param needDecrypt 是否需要解密
     * @return 用户信息
     */
    public ResponseResult<UserInfoInTokenBO> getUserInfoByAccessToken(String accessToken, boolean needDecrypt) {
        if (StrUtil.isBlank(accessToken)) {
            return ResponseResult.showFailMsg("accessToken is blank");
        }
        String realAccessToken;
        if (needDecrypt) {
            ResponseResult<String> decryptTokenEntity = decryptToken(accessToken,true);
            if (!decryptTokenEntity.isSuccess()) {
                if (decryptTokenEntity.getCode().equals(ResponseEnum.TOKEN_EXPIRED.value())){
                    return ResponseResult.fail(ResponseEnum.ACCESS_TOKEN_EXPIRED);
                }
                return ResponseResult.transform(decryptTokenEntity);
            }
            realAccessToken = decryptTokenEntity.getData();
        }else {
            realAccessToken = accessToken;
        }
        UserInfoInTokenBO userInfoInTokenBO = (UserInfoInTokenBO) redisTemplate.opsForValue()
                .get(getAccessKey(realAccessToken));

        if (userInfoInTokenBO == null) {
            return ResponseResult.fail(ResponseEnum.ACCESS_TOKEN_EXPIRED);
        }
        return ResponseResult.success(userInfoInTokenBO);
    }

    /**
     * 刷新token，并返回新的token
     * @param refreshToken
     * @return
     */
    public ResponseResult<TokenInfoBO> refreshToken(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            return ResponseResult.showFailMsg("refreshToken is blank");
        }
        ResponseResult<String> decryptTokenEntity = decryptToken(refreshToken,false);
        //token解密失败或超时
        if (!decryptTokenEntity.isSuccess()) {
            if (decryptTokenEntity.getCode().equals(ResponseEnum.TOKEN_EXPIRED.value())){
                return ResponseResult.fail(ResponseEnum.REFRESH_TOKEN_EXPIRED);
            }
            return ResponseResult.transform(decryptTokenEntity);
        }
        String realRefreshToken = decryptTokenEntity.getData();
        String accessToken = stringRedisTemplate.opsForValue().get(getRefreshToAccessKey(realRefreshToken));

        if (StrUtil.isBlank(accessToken)) {
            return ResponseResult.fail(ResponseEnum.REFRESH_TOKEN_EXPIRED);
        }
        String[] accessTokenArr = accessToken.split("_");
        if (accessTokenArr.length <= 1){
            return ResponseResult.fail(ResponseEnum.REFRESH_TOKEN_EXPIRED);
        }
        accessToken = accessTokenArr[0];
        //用户id
        Long userId = Long.valueOf(accessTokenArr[1]);
        //系统类型
        Integer sysType = Integer.valueOf(accessTokenArr[2]);
        UserInfoInTokenBO userInfoInTokenBO = loginService.getUserByUseId(userId,sysType);

        // 删除旧的refresh_token
        stringRedisTemplate.delete(getRefreshToAccessKey(realRefreshToken));
        // 删除旧的access_token
        stringRedisTemplate.delete(getAccessKey(accessToken));
        // 保存一份新的token
        TokenInfoBO tokenInfoBO = storeAccessToken(userInfoInTokenBO);

        return ResponseResult.success(tokenInfoBO);
    }

    /**
     * 删除全部的token
     */
    public void deleteAllToken(String appId, Long uid) {
        String uidKey = getUidToAccessKey(getApprovalKey(appId, uid));
        Long size = redisTemplate.opsForSet().size(uidKey);
        if (size == null || size == 0) {
            return;
        }
        List<String> tokenInfoBoList = stringRedisTemplate.opsForSet().pop(uidKey, size);

        if (CollUtil.isEmpty(tokenInfoBoList)) {
            return;
        }

        for (String accessTokenWithRefreshToken : tokenInfoBoList) {
            String[] accessTokenWithRefreshTokenArr = accessTokenWithRefreshToken.split(StrUtil.COLON);
            String accessToken = accessTokenWithRefreshTokenArr[0];
            String refreshToken = accessTokenWithRefreshTokenArr[1];
            redisTemplate.delete(getRefreshToAccessKey(refreshToken));
            redisTemplate.delete(getAccessKey(accessToken));
        }
        redisTemplate.delete(uidKey);

    }

    private static String getApprovalKey(UserInfoInTokenBO userInfoInToken) {
        return getApprovalKey(userInfoInToken.getSysType().toString(), userInfoInToken.getUserId());
    }

    private static String getApprovalKey(String appId, Long uid) {
        return uid == null?  appId : appId + StrUtil.COLON + uid;
    }

    //加密token
    private String encryptToken(String accessToken,Integer sysType) {
        return Base64.encode(accessToken + System.currentTimeMillis() + sysType);
    }

    /**
     * 解密token
     * @param data
     * @return
     */
    private ResponseResult<String> decryptToken(String data,boolean isAccessToken) {
        String decryptStr;
        String decryptToken;
        try {
            //解码后字符串
            decryptStr = Base64.decodeStr(data);
            decryptToken = decryptStr.substring(0,32);
            // 创建token的时间，token使用时效性，防止攻击者通过一堆的尝试找到aes的密码，虽然aes是目前几乎最好的加密算法
            long createTokenTime = Long.parseLong(decryptStr.substring(32,45));
            // 系统类型
            int sysType = Integer.parseInt(decryptStr.substring(45));
            // token的过期时间
            int expiresIn = getExpiresIn(sysType,isAccessToken);
            long second = 1000L;
            if (System.currentTimeMillis() - createTokenTime > expiresIn * second) {
                return ResponseResult.fail(ResponseEnum.TOKEN_EXPIRED);
            }
        }catch (Exception e) {
            log.error(e.getMessage());
//			return ResponseResult.showFailMsg("token 格式有误");
            return ResponseResult.fail(ResponseEnum.TOKEN_EXPIRED);
        }

        // 防止解密后的token是脚本，从而对redis进行攻击，uuid只能是数字和小写字母
        if (!PrincipalUtil.isSimpleChar(decryptToken)) {
//			return ResponseResult.showFailMsg("token 格式有误");
            return ResponseResult.fail(ResponseEnum.TOKEN_EXPIRED);
        }
        return ResponseResult.success(decryptToken);
    }

    public String getAccessKey(String accessToken) {
        return CacheNames.ACCESS + accessToken;
    }

    public String getUidToAccessKey(String approvalKey) {
        return CacheNames.UID_TO_ACCESS + approvalKey;
    }

    public String getRefreshToAccessKey(String refreshToken) {
        return CacheNames.REFRESH_TO_ACCESS + refreshToken;
    }

    /**
     * 获取token信息
     * @param userInfoInToken
     * @return
     */
    public TokenInfoVO storeAndGetVo(UserInfoInTokenBO userInfoInToken) {
        TokenInfoBO tokenInfoBO = storeAccessToken(userInfoInToken);

        TokenInfoVO tokenInfoVO = new TokenInfoVO();
        tokenInfoVO.setAccessToken(tokenInfoBO.getAccessToken());
        tokenInfoVO.setRefreshToken(tokenInfoBO.getRefreshToken());
        tokenInfoVO.setExpiresIn(tokenInfoBO.getAccessExpiresIn());
        return tokenInfoVO;
    }

    /**
     * 更新token中信息
     */
    public void updateUserInfoByUserId(Long uid,String appId,UserInfoInTokenBO userInfoInTokenBO) {
        String uidKey = getUidToAccessKey(getApprovalKey(appId, uid));
        Set<String> tokenInfoBoList = stringRedisTemplate.opsForSet().members(uidKey);
        if (tokenInfoBoList == null || tokenInfoBoList.size() == 0) {
            return;
        }

        for (String accessTokenWithRefreshToken : tokenInfoBoList) {
            String[] accessTokenWithRefreshTokenArr = accessTokenWithRefreshToken.split(StrUtil.COLON);
            String accessKey = this.getAccessKey(accessTokenWithRefreshTokenArr[0]);
            UserInfoInTokenBO oldUserInfoInTokenBO = (UserInfoInTokenBO) redisTemplate.opsForValue().get(accessKey);
            if (oldUserInfoInTokenBO == null) {
                continue;
            }
            oldUserInfoInTokenBO.setUserType(userInfoInTokenBO.getUserType());
            oldUserInfoInTokenBO.setCompanyId(userInfoInTokenBO.getCompanyId());
            oldUserInfoInTokenBO.setCompanyType(userInfoInTokenBO.getCompanyType());
            redisTemplate.opsForValue().set(accessKey, oldUserInfoInTokenBO ,getExpiresIn(oldUserInfoInTokenBO.getSysType(),true), TimeUnit.SECONDS);
        }
    }

    /**
     * 删除token
     */
    public void deleteToken(String refreshToken) {
        ResponseResult<String> decryptTokenEntity = decryptToken(refreshToken,false);
        //token解密失败或超时
        if (!decryptTokenEntity.isSuccess()) {
            return;
        }
        String realRefreshToken = decryptTokenEntity.getData();
        String accessToken = stringRedisTemplate.opsForValue().get(getRefreshToAccessKey(realRefreshToken));

        if (StrUtil.isBlank(accessToken)) {
            return;
        }
        String[] accessTokenArr = accessToken.split("_");
        accessToken = accessTokenArr[0];
        // 删除旧的refresh_token
        stringRedisTemplate.delete(getRefreshToAccessKey(realRefreshToken));
        // 删除旧的access_token
        stringRedisTemplate.delete(getAccessKey(accessToken));
    }

}
