package com.hrrecruit.module.job.controller;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.Result;
import com.hrrecruit.entity.JobPost;
import com.hrrecruit.module.job.dto.JobPostDTO;
import com.hrrecruit.module.job.dto.JobPostQueryDTO;
import com.hrrecruit.module.job.service.JobPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位管理控制器
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('job:list', 'ROLE_CANDIDATE')")
    public Result<PageResult<JobPost>> page(JobPostQueryDTO queryDTO) {
        return Result.success(jobPostService.getPageList(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<JobPost> getById(@PathVariable Long id) {
        return Result.success(jobPostService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('job:add')")
    public Result<Void> add(@Valid @RequestBody JobPostDTO dto) {
        jobPostService.add(dto);
        return Result.successMsg("新增成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('job:edit')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody JobPostDTO dto) {
        jobPostService.update(id, dto);
        return Result.successMsg("修改成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('job:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        jobPostService.delete(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('job:edit')")
    public Result<Void> publish(@PathVariable Long id) {
        jobPostService.publish(id);
        return Result.successMsg("发布成功");
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('job:edit')")
    public Result<Void> close(@PathVariable Long id) {
        jobPostService.close(id);
        return Result.successMsg("关闭成功");
    }

    @GetMapping("/published")
    public Result<List<JobPost>> getPublished() {
        return Result.success(jobPostService.getPublished());
    }
}