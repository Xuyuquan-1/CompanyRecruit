package com.hrrecruit.module.employee.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.entity.Employee;
import com.hrrecruit.module.employee.service.EmployeeService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 员工管理控制器（录用 → 入职 → 档案）
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /** 查询全部正式员工列表 */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('employee:list')")
    public Result<List<Employee>> getList(@AuthenticationPrincipal LoginUser loginUser) {
        // 应聘者不能查看员工列表
        if (loginUser.getRoleCodes().contains("ROLE_CANDIDATE")) {
            return Result.error("无权访问员工列表");
        }
        return Result.success(employeeService.getList());
    }

    /** 查询员工详情 */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    /** 由录用记录生成员工档案 */
    @PostMapping("/onboard/{offerId}")
    @PreAuthorize("hasAuthority('employee:onboard')")
    public Result<Employee> onboard(@PathVariable Long offerId, @RequestBody Employee employee) {
        Employee result = employeeService.createFromOffer(offerId, employee);
        return Result.success("员工档案创建成功", result);
    }

    /** 更新员工档案 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Employee employee) {
        employeeService.update(id, employee);
        return Result.successMsg("更新成功");
    }

    /** 更新入职资料提交状态（HR/管理员操作） */
    @PostMapping("/{id}/document")
    @PreAuthorize("hasAuthority('employee:onboard')")
    public Result<Void> updateDocument(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String documentType = (String) params.get("documentType");
        Integer status = ((Number) params.get("status")).intValue();
        employeeService.updateDocuments(id, documentType, status);
        return Result.successMsg("资料状态更新成功");
    }

    /** 员工提交入职资料（身份证/合同/体检报告等） */
    @PostMapping("/{id}/submit-document")
    @PreAuthorize("hasAnyAuthority('employee:submit', 'ROLE_CANDIDATE')")
    public Result<Void> submitDocument(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String documentType = (String) params.get("documentType"); // idCard/contract/medicalReport
        String fileUrl = (String) params.get("fileUrl");
        employeeService.submitDocument(id, documentType, fileUrl);
        return Result.successMsg("资料提交成功");
    }

    /** 删除员工 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:edit')")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.successMsg("删除成功");
    }
}