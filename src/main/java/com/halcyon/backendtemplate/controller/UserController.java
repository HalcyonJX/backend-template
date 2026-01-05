package com.halcyon.backendtemplate.controller;

import com.halcyon.backendtemplate.common.BaseResponse;
import com.halcyon.backendtemplate.common.ResultUtils;
import com.halcyon.backendtemplate.exception.ErrorCode;
import com.halcyon.backendtemplate.exception.ThrowUtils;
import com.halcyon.backendtemplate.model.dto.user.UserRegisterRequest;
import com.halcyon.backendtemplate.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR,"参数不能为空");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }
}