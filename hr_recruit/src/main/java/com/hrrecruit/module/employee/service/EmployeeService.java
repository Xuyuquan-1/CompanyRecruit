package com.hrrecruit.module.employee.service;

import com.hrrecruit.entity.Employee;

import java.util.List;

/**
 * 员工管理服务接口
 */
public interface EmployeeService {

    /** 查询全部正式员工列表 */
    List<Employee> getList();

    /** 根据ID查询员工详情 */
    Employee getById(Long id);

    /** 由录用记录生成员工档案 */
    Employee createFromOffer(Long offerId, Employee employee);

    /** 更新员工档案 */
    void update(Long id, Employee employee);

    /** 更新入职资料提交状态 */
    void updateDocuments(Long id, String documentType, Integer status);

    /** 员工提交入职资料（身份证/合同/体检报告等） */
    void submitDocument(Long id, String documentType, String fileUrl);

    /** 删除员工 */
    void delete(Long id);
}