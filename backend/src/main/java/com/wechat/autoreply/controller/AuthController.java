package com.wechat.autoreply.controller;

import com.wechat.autoreply.dto.JwtTokenDTO;
import com.wechat.autoreply.dto.LoginDTO;
import com.wechat.autoreply.dto.RegisterDTO;
import com.wechat.autoreply.entity.SysUser;
import com.wechat.autoreply.service.UserService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "用户认证", description = "用户登录注册相关接口")
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<JwtTokenDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtTokenDTO tokenDTO = userService.login(loginDTO);
        return Result.success(tokenDTO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<SysUser> register(@Valid @RequestBody RegisterDTO registerDTO) {
        SysUser user = userService.register(registerDTO);
        return Result.success(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        // 前端删除 Token 即可，服务端可以添加黑名单机制
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息")
    public Result<SysUser> getCurrentUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        SysUser user = userService.getUserByUsername(
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return Result.success(user);
    }

    /**
     * 生成密码 hash（仅用于测试）
     */
    @GetMapping("/generate-hash")
    @Operation(summary = "生成密码hash")
    public Result<String> generateHash(@RequestParam String password) {
        org.springframework.security.crypto.password.PasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        return Result.success(hash);
    }
}
