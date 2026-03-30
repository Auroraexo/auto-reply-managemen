package com.wechat.autoreply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.dto.JwtTokenDTO;
import com.wechat.autoreply.dto.LoginDTO;
import com.wechat.autoreply.dto.RegisterDTO;
import com.wechat.autoreply.entity.SysUser;
import com.wechat.autoreply.exception.BusinessException;
import com.wechat.autoreply.mapper.SysUserMapper;
import com.wechat.autoreply.service.UserService;
import com.wechat.autoreply.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public JwtTokenDTO login(LoginDTO loginDTO) {
        System.out.println("========== 开始登录 ==========");
        System.out.println("用户名：" + loginDTO.getUsername());
        System.out.println("密码：" + loginDTO.getPassword());
        log.info("开始登录，用户名：{}", loginDTO.getUsername());
        
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            System.out.println("用户不存在");
            log.warn("用户不存在：{}", loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        System.out.println("找到用户，ID：" + user.getId());
        System.out.println("数据库密码hash：" + user.getPassword());
        log.info("找到用户，ID：{}，密码hash：{}", user.getId(), user.getPassword().substring(0, 20));

        // 验证密码
        try {
            System.out.println("开始验证密码...");
            boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
            System.out.println("密码验证结果：" + matches);
            log.info("密码验证结果：{}", matches);
            if (!matches) {
                System.out.println("密码不匹配");
                log.warn("密码错误：{}", loginDTO.getUsername());
                throw new BusinessException("用户名或密码错误");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("密码验证异常：" + e.getMessage());
            log.error("密码验证异常", e);
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("用户已被禁用：{}", loginDTO.getUsername());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 更新最后登录时间
        updateLastLoginTime(user.getId());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功：{}", loginDTO.getUsername());

        return JwtTokenDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(RegisterDTO registerDTO) {
        // 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, registerDTO.getUsername());
        SysUser existUser = sysUserMapper.selectOne(wrapper);

        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname())
                ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(StringUtils.hasText(registerDTO.getRole()) ? registerDTO.getRole() : "USER");
        user.setStatus(1);

        sysUserMapper.insert(user);
        log.info("用户注册成功：{}", registerDTO.getUsername());

        return user;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    @Override
    public Page<SysUser> getPageUsers(int page, int size, String username) {
        Page<SysUser> userPage = new Page<>(page, size);
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        return sysUserMapper.selectPage(userPage, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateUser(SysUser user) {
        SysUser existUser = sysUserMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        existUser.setNickname(user.getNickname());
        existUser.setEmail(user.getEmail());
        existUser.setPhone(user.getPhone());
        existUser.setAvatar(user.getAvatar());

        sysUserMapper.updateById(existUser);
        return existUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        sysUserMapper.deleteById(userId);
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            sysUserMapper.updateById(user);
        }
    }
}
