package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    Page<Application> selectPageWithFilter(Page<Application> page,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("jobId") Long jobId,
                                           @Param("tag") String tag,
                                           @Param("candidateId") Long candidateId);

    Long countByJobId(@Param("jobId") Long jobId);
}