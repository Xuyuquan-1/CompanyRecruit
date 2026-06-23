package com.hrrecruit.module.resume.controller;

import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.Result;
import com.hrrecruit.common.utils.OssUtil;
import com.hrrecruit.entity.Resume;
import com.hrrecruit.module.resume.service.ResumeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 简历管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final OssUtil ossUtil;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 分页查询简历列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('resume:list')")
    public Result<PageResult<Resume>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) Integer parseStatus,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String tag,
                                           @RequestParam(required = false) Long jobId,
                                           @RequestParam(required = false) Integer applicationStatus) {
        Page<Resume> page = new Page<>(pageNum, pageSize);
        return Result.success(resumeService.getPageList(page, parseStatus, keyword, tag, jobId, applicationStatus));
    }

    /**
     * 查询简历详情
     */
    @GetMapping("/{id}")
    public Result<Resume> getById(@PathVariable Long id) {
        return Result.success(resumeService.getById(id));
    }

    /**
     * 获取当前用户的简历列表（应聘者专用）
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('resume:list', 'ROLE_CANDIDATE')")
    public Result<java.util.List<Resume>> getMyResumes() {
        return Result.success(resumeService.getMyResumes());
    }

    /**
     * 上传简历文件（仅应聘者）
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('ROLE_CANDIDATE')")
    public Result<Resume> upload(@RequestParam("file") MultipartFile file) {
        Resume resume = resumeService.upload(file);
        return Result.success("上传成功", resume);
    }

    /**
     * 触发AI解析简历（仅HR/管理员）
     */
    @PostMapping("/{id}/parse")
    @PreAuthorize("hasAuthority('resume:manage')")
    public Result<Resume> parse(@PathVariable Long id) {
        Resume resume = resumeService.parse(id);
        return Result.success("解析完成", resume);
    }

    /**
     * 修正解析后的结构化数据（仅HR/管理员）
     */
    @PutMapping("/{id}/parsed-data")
    @PreAuthorize("hasAuthority('resume:manage')")
    public Result<Void> updateParsedData(@PathVariable Long id, @RequestBody Map<String, Object> parsedData) {
        resumeService.updateParsedData(id, parsedData);
        return Result.successMsg("数据修正成功");
    }

    /**
     * 删除简历（仅HR/管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('resume:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return Result.successMsg("删除成功");
    }

    /**
     * 下载/查看简历文件
     * 支持本地存储的文件和OSS文件
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('resume:list')")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws IOException {
        Resume resume = resumeService.getById(id);
        String filePath = resume.getFilePath();

        byte[] fileBytes;
        String contentType;

        // 如果是 OSS 地址，通过 OSS SDK 下载
        if (filePath != null && filePath.startsWith("http")) {
            try {
                fileBytes = ossUtil.download(filePath);
                // 根据文件扩展名设置 Content-Type
                String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
                contentType = getContentTypeByExtension(extension);
            } catch (Exception e) {
                log.error("OSS文件下载失败: {}", filePath, e);
                return ResponseEntity.status(500).body(null);
            }
        } else {
            // 本地存储文件
            Path actualPath = Paths.get(uploadPath, filePath.replace("/uploads/", ""));
            if (!Files.exists(actualPath)) {
                return ResponseEntity.notFound().build();
            }
            fileBytes = Files.readAllBytes(actualPath);
            contentType = Files.probeContentType(actualPath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        }

        String encodedFilename = URLEncoder.encode(resume.getOriginalFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .body(fileBytes);
    }

    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentTypeByExtension(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}