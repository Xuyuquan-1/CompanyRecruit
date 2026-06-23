package com.hrrecruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单权限实体
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID（0表示顶级目录） */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 权限标识，如 job:list */
    private String permission;

    /** 类型：1-目录，2-菜单，3-按钮 */
    private Integer type;

    /** 前端路由路径 */
    private String path;

    /** 图标名称 */
    private String icon;

    /** 排序值（越小越靠前） */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
