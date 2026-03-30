package com.wechat.autoreply.constant;

/**
 * 角色常量
 */
public class RoleConstants {

    /**
     * 运营者（管理员）
     * 拥有后台管理权限
     */
    public static final String OPERATOR = "OPERATOR";

    /**
     * 角色中文名称映射
     */
    public static String getRoleName(String role) {
        switch (role) {
            case OPERATOR:
                return "运营者";
            default:
                return "未知角色";
        }
    }
}
