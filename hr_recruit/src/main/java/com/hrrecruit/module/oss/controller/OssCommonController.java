package com.hrrecruit.module.oss.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.common.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用OSS文件上传/下载控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/osscommon")
@RequiredArgsConstructor
public class OssCommonController {

    private final AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * 接收 MultipartFile，UUID生成唯一文件名，调用 AliOssUtil.upload()，返回文件URL
     *
     * @param file 上传的文件
     * @return 包含文件URL的Result
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成唯一对象名：目录/UUID.扩展名
        String objectName = "upload/" + UUID.randomUUID().toString() + extension;

        try {
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("objectName", objectName);
            result.put("originalFilename", originalFilename);
            return Result.success("上传成功", result);
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFilename, e);
            return Result.error("文件上传失败");
        }
    }

    /**
     * 获取下载链接
     * 接收 objectName，验证路径穿越攻击，调用 getSignedUrl() 生成1小时有效签名URL
     *
     * @param objectName OSS对象名称
     * @return 包含签名URL的Result
     */
    @GetMapping("/downloadUrl")
    public Result<Map<String, String>> downloadUrl(@RequestParam("objectName") String objectName) {
        // 安全校验：防止路径穿越攻击（只禁止 ..，允许正常的 / 路径分隔符）
        if (objectName.contains("..")) {
            return Result.error("非法的文件路径");
        }

        try {
            String signedUrl = aliOssUtil.getSignedUrl(objectName, 3600);
            Map<String, String> result = new HashMap<>();
            result.put("downloadUrl", signedUrl);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取下载链接失败: {}", objectName, e);
            return Result.error("获取下载链接失败");
        }
    }
}
