package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrrecruit.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    List<Employee> selectListWithDetail();
}