package io.dataease.base.mapper;

import io.dataease.base.domain.SysUser;
import io.dataease.base.domain.SysUserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper {
    long countByExample(SysUserExample example);

    int deleteByExample(SysUserExample example);

    int deleteByPrimaryKey(Long userId);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    List<SysUser> selectByExample(SysUserExample example);

    SysUser selectByPrimaryKey(Long userId);

    int updateByExampleSelective(@Param("record") SysUser record, @Param("example") SysUserExample example);

    int updateByExample(@Param("record") SysUser record, @Param("example") SysUserExample example);


    //根据SysUser中的id值进行更新，只更新不为null的值
    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);
}