package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrrecruit.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {

    Long countByParseStatus(@Param("parseStatus") Integer parseStatus);

    Page<Resume> selectPageWithFilter(Page<Resume> page,
                                      @Param("uploadBy") Long uploadBy,
                                      @Param("parseStatus") Integer parseStatus,
                                      @Param("keyword") String keyword,
                                      @Param("tag") String tag,
                                      @Param("jobId") Long jobId,
                                      @Param("applicationStatus") Integer applicationStatus);
}