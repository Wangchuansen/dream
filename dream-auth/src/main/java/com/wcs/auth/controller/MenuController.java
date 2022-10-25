package com.wcs.auth.controller;

import com.wcs.common.response.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: wcs
 * @create: 2022-08-14 14:20
 * @description:
 */
@RestController
@RequestMapping("menu")
public class MenuController {

    /**
     * 获取菜单
     */
    @GetMapping("/getMenu")
    public ResponseResult<Map<String,Object>> getMenu() {
        Map<String,Object> result = new HashMap<>();
        int[] arr = {1,2,3,4};
        result.put("userType",2);
        result.put("menuIds",arr);
        result.put("editStatus",1);
        result.put("isInvitation",0);
        return ResponseResult.success(result);
    }
}
