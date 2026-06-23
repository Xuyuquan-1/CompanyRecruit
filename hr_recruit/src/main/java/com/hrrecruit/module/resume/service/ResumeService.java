package com.hrrecruit.module.resume.service;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.entity.Resume;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 简历管理服务接口
 */
public interface ResumeService {

    /**
     * 分页查询简历列表
     */
    PageResult<Resume> getPageList(Page<Resume> page, Integer parseStatus, String keyword, String tag, Long jobId, Integer applicationStatus);

    /**
     * 查询简历详情
     */
    Resume getById(Long id);

    /**
     * 上传简历文件并存储
     */
    Resume upload(MultipartFile file);

    /**
     * 触发AI解析简历
     */
    Resume parse(Long id);

    /**
     * 修正解析后的结构化数据
     */
    void updateParsedData(Long id, Map<String, Object> parsedData);

    /**
     * 删除简历
     */
    void delete(Long id);

    /**
     * 获取当前用户的简历列表（应聘者专用）
     */
    java.util.List<Resume> getMyResumes();
}