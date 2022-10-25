package com.wcs.auth.mapper;

import com.wcs.auth.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户(dr_user)表数据库访问层
 * @author wcs
 * @since 2022-09-08 15:03:28
 */
@Mapper
public interface UserMapper {

    User queryByMobile(String mobile);

    void insert(User user);
}

