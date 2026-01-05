package com.halcyon.backendtemplate.controller;

import com.halcyon.backendtemplate.common.BaseResponse;
import com.halcyon.backendtemplate.common.ResultUtils;
import com.halcyon.backendtemplate.exception.ErrorCode;
import com.halcyon.backendtemplate.exception.ThrowUtils;
import com.halcyon.backendtemplate.model.dto.user.UserLoginRequest;
import com.halcyon.backendtemplate.model.dto.user.UserRegisterRequest;
import com.halcyon.backendtemplate.model.entity.User;
import com.halcyon.backendtemplate.model.vo.LoginUserVO;
import com.halcyon.backendtemplate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO result = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户接口
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request){
        User result = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(result));
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
}