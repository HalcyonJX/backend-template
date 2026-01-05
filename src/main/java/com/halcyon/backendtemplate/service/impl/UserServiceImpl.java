package com.halcyon.backendtemplate.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.halcyon.backendtemplate.constant.UserConstant;
import com.halcyon.backendtemplate.exception.BusinessException;
import com.halcyon.backendtemplate.exception.ErrorCode;
import com.halcyon.backendtemplate.exception.ThrowUtils;
import com.halcyon.backendtemplate.model.entity.User;
import com.halcyon.backendtemplate.model.enums.UserRoleEnum;
import com.halcyon.backendtemplate.model.vo.LoginUserVO;
import com.halcyon.backendtemplate.service.UserService;
import com.halcyon.backendtemplate.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author 张嘉鑫
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-01-06 00:18:03
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword,checkPassword),
                ErrorCode.PARAMS_ERROR,"参数不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR,"用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR,"用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        //2.检查是否重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount,userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0,ErrorCode.PARAMS_ERROR,"账号重复");
        //3.加密
        String encryptPassword = getEncryptPassword(userPassword);
        //4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("小明");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        return user.getId();
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "Hal";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword),
                ErrorCode.PARAMS_ERROR,"参数不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR,"用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR,"用户密码过短");
        //2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount,userAccount);
        queryWrapper.eq(User::getUserPassword,encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //用户不存在
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //3.记录用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //先判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null,
                ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        //从数据库中查
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        ThrowUtils.throwIf(currentUser == null,
                ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }
}




