package com.wechat.autoreply.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.dto.JwtTokenDTO;
import com.wechat.autoreply.dto.LoginDTO;
import com.wechat.autoreply.dto.RegisterDTO;
import com.wechat.autoreply.entity.SysUser;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token
     */
    JwtTokenDTO login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    SysUser register(RegisterDTO registerDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);

    /**
     * 分页查询用户
     *
     * @param page     页码
     * @param size     每页大小
     * @param username 用户名（可选）
     * @return 分页结果
     */
    Page<SysUser> getPageUsers(int page, int size, String username);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新后的用户
     */
    SysUser updateUser(SysUser user);

    /**
     * 删除用户
     *
     * @param userId 用户 ID
     */
    void deleteUser(Long userId);

    /**
     * 更新最后登录时间
     *
     * @param userId 用户 ID
     */
    void updateLastLoginTime(Long userId);
}
