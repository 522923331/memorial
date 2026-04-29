package com.ruoyi.memorial.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysUserService;

/**
 * 小程序/H5 登录相关API（无需认证）
 */
@RestController
@RequestMapping("/api")
public class WxMiniLoginController {

    private static final String SMS_CODE_PREFIX = "sms:phone:";

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public AjaxResult sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (StringUtils.isEmpty(phone) || phone.length() != 11) {
            return AjaxResult.error("手机号格式不正确");
        }

        // 生成6位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        // 存入Redis，有效期5分钟
        String key = SMS_CODE_PREFIX + phone;
        redisCache.setCacheObject(key, code, 5, TimeUnit.MINUTES);

        // P0阶段：直接返回验证码方便测试，生产环境应通过短信服务发送
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        return AjaxResult.success("验证码已发送", data);
    }

    /**
     * 手机号验证码登录
     */
    @PostMapping("/phoneLogin")
    public AjaxResult phoneLogin(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");

        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            return AjaxResult.error("手机号和验证码不能为空");
        }

        // 从Redis获取验证码
        String key = SMS_CODE_PREFIX + phone;
        String cachedCode = redisCache.getCacheObject(key);
        if (cachedCode == null) {
            return AjaxResult.error("验证码已过期，请重新获取");
        }
        if (!code.equals(cachedCode)) {
            return AjaxResult.error("验证码错误");
        }

        // 验证通过，删除验证码
        redisCache.deleteObject(key);

        // 通过手机号查找用户
        SysUser user = userMapper.selectUserByPhonenumber(phone);
        if (user == null) {
            // 自动注册
            user = new SysUser();
            user.setUserName("user_" + phone);
            user.setNickName("用户" + phone.substring(7));
            user.setPhonenumber(phone);
            user.setPassword(SecurityUtils.encryptPassword("123456"));
            user.setStatus("0");
            userService.insertUser(user);
            // 重新查询获取完整信息
            user = userMapper.checkPhoneUnique(phone);
        }

        // 检查用户状态
        if ("1".equals(user.getStatus())) {
            return AjaxResult.error("用户已被停用");
        }

        // 获取权限并生成token
        Set<String> permissions = permissionService.getMenuPermission(user);
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissions);
        String token = tokenService.createToken(loginUser);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return AjaxResult.success(data);
    }

    /**
     * 获取当前用户信息（需要token）
     */
    @GetMapping("/userInfo")
    public AjaxResult getUserInfo(HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser == null) {
            return AjaxResult.error(401, "请先登录");
        }

        SysUser user = loginUser.getUser();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("nickName", user.getNickName());
        data.put("phonenumber", user.getPhonenumber());
        data.put("avatar", user.getAvatar());
        data.put("sex", user.getSex());
        return AjaxResult.success(data);
    }
}
