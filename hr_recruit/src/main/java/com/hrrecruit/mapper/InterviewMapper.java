package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrrecruit.entity.Interview;
import com.hrrecruit.module.interview.dto.InterviewQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {

    Page<Interview> selectPageWithDetail(Page<Interview> page,
                                         @Param("query") InterviewQueryDTO queryDTO);
}