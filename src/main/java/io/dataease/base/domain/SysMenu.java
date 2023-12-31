package io.dataease.base.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;


/*
 * @Author @Lijian
 * @Description //系统菜单实体类
 * @Date
 * @return
 **/

@Data
public class SysMenu implements Serializable {
    private Long menuId;

    private Long pid;

    private Integer subCount;

    private Integer type;

    private String title;

    private String name;

    private String component;

    private Integer menuSort;

    private String icon;

    private String path;

    private Boolean iFrame;

    private Boolean cache;

    private Boolean hidden;

    private String permission;

    private String createBy;

    private String updateBy;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;


    /**
     * 由于该类型作为HashSet key所以必须重写以下方法
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SysMenu menu = (SysMenu) o;
        return Objects.equals(menuId, menu.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId);
    }



}