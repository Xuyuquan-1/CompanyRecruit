package com.hrrecruit.module.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrrecruit.common.Constants;
import com.hrrecruit.common.PageResult;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.common.utils.OssUtil;
import com.hrrecruit.entity.Resume;
import com.hrrecruit.mapper.ResumeMapper;
import com.hrrecruit.module.resume.service.ResumeService;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 简历管理服务实现
 * 上传→OSS存储 / 解析→AI提取结构化字段
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeMapper resumeMapper;
    private final OssUtil ossUtil;
    private final ChatClient chatClient;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /** 判断文件路径是否为本地存储 */
    private boolean isLocalStorage(String filePath) {
        return filePath != null && !filePath.startsWith("http");
    }

    @Override
    public PageResult<Resume> getPageList(Page<Resume> page, Integer parseStatus, String keyword, String tag, Long jobId, Integer applicationStatus) {
        LoginUser loginUser = getCurrentUser();
        Long uploadBy = null;
        if (loginUser.getRoleCodes().contains("ROLE_CANDIDATE")) {
            uploadBy = loginUser.getUserId();
        }
        Page<Resume> result = resumeMapper.selectPageWithFilter(page, uploadBy, parseStatus, keyword, tag, jobId, applicationStatus);
        return new PageResult<>(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    @Override
    public Resume getById(Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        return resume;
    }

    @Override
    public Resume upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String filePath;

        // 优先尝试 OSS 上传，失败则降级到本地存储
        try {
            filePath = ossUtil.upload(originalFilename, file.getInputStream(), file.getSize());
            log.info("简历上传到OSS成功: {}", filePath);
        } catch (Exception e) {
            log.warn("OSS上传失败，降级到本地存储: {}", e.getMessage());
            filePath = saveToLocal(file, originalFilename);
        }

        LoginUser loginUser = getCurrentUser();

        Resume resume = new Resume();
        resume.setOriginalFilename(originalFilename);
        resume.setFilePath(filePath);
        resume.setFileSize(file.getSize());
        resume.setUploadBy(loginUser.getUserId());
        resume.setParseStatus(Constants.RESUME_PARSE_PENDING);
        resume.setUploadTime(LocalDateTime.now());

        resumeMapper.insert(resume);
        log.info("简历上传成功，路径: {}", filePath);
        return resume;
    }

    /**
     * 将文件保存到本地目录
     */
    private String saveToLocal(MultipartFile file, String originalFilename) {
        try {
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + extension;
            Path dirPath = Paths.get(uploadPath, "resume");
            Files.createDirectories(dirPath);
            Path targetPath = dirPath.resolve(storedName);
            // 使用 InputStream 写入，避免 transferTo 在流已被消费后失败
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath);
            }
            String localPath = "/uploads/resume/" + storedName;
            log.info("文件保存到本地: {}", localPath);
            return localPath;
        } catch (IOException ex) {
            log.error("本地存储也失败", ex);
            throw new BusinessException("简历上传失败，请稍后重试");
        }
    }

    @Override
    public Resume parse(Long id) {
        Resume resume = getById(id);

        // 1. 获取文件字节（兼容 OSS 和本地存储）
        byte[] fileBytes;
        if (isLocalStorage(resume.getFilePath())) {
            fileBytes = readLocalFile(resume.getFilePath());
        } else {
            fileBytes = ossUtil.download(resume.getFilePath());
        }

        // 2. 提取文本
        String text = extractText(resume.getOriginalFilename(), fileBytes);
        if (text == null || text.isBlank()) {
            resume.setParseStatus(Constants.RESUME_PARSE_FAILED);
            resumeMapper.updateById(resume);
            throw new BusinessException("无法从文件中提取文本，请确认文件内容不是纯图片");
        }

        // 3. AI 解析
        try {
            String parsedJson = parseWithAI(text);
            resume.setParsedJson(parsedJson);
            resume.setParseStatus(Constants.RESUME_PARSE_SUCCESS);
            
            // 4. 自动提取技能标签
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> parsedData = mapper.readValue(parsedJson, Map.class);
                if (parsedData.containsKey("skills") && parsedData.get("skills") != null) {
                    String skills = parsedData.get("skills").toString();
                    if (!skills.isEmpty()) {
                        resume.setTags(skills);
                    }
                }
            } catch (Exception e) {
                log.warn("提取技能标签失败: {}", e.getMessage());
            }
            
            log.info("简历AI解析成功, id: {}", id);
        } catch (Exception e) {
            log.error("简历AI解析失败, id: {}", id, e);
            resume.setParseStatus(Constants.RESUME_PARSE_FAILED);
        }

        resumeMapper.updateById(resume);
        return resume;
    }

    /**
     * 读取本地存储的文件字节
     */
    private byte[] readLocalFile(String filePath) {
        try {
            // filePath 形如 /uploads/resume/xxx.pdf，需映射到实际磁盘路径
            Path actualPath = Paths.get(uploadPath, filePath.replace("/uploads/", ""));
            return Files.readAllBytes(actualPath);
        } catch (IOException e) {
            log.error("读取本地文件失败: {}", filePath, e);
            throw new BusinessException("文件读取失败");
        }
    }

    @Override
    public void updateParsedData(Long id, Map<String, Object> parsedData) {
        Resume resume = getById(id);
        try {
            ObjectMapper mapper = new ObjectMapper();
            resume.setParsedJson(mapper.writeValueAsString(parsedData));
            resume.setParseStatus(Constants.RESUME_PARSE_SUCCESS);
            
            // 同步更新技能标签
            if (parsedData.containsKey("skills") && parsedData.get("skills") != null) {
                String skills = parsedData.get("skills").toString();
                if (!skills.isEmpty()) {
                    resume.setTags(skills);
                }
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据格式错误");
        }
        resumeMapper.updateById(resume);
    }

    @Override
    public void delete(Long id) {
        Resume resume = getById(id);
        if (isLocalStorage(resume.getFilePath())) {
            deleteLocalFile(resume.getFilePath());
        } else {
            ossUtil.delete(resume.getFilePath());
        }
        resumeMapper.deleteById(id);
    }

    @Override
    public java.util.List<Resume> getMyResumes() {
        LoginUser loginUser = getCurrentUser();
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUploadBy, loginUser.getUserId())
               .orderByDesc(Resume::getUploadTime);
        return resumeMapper.selectList(wrapper);
    }

    /**
     * 删除本地存储的文件
     */
    private void deleteLocalFile(String filePath) {
        try {
            Path actualPath = Paths.get(uploadPath, filePath.replace("/uploads/", ""));
            Files.deleteIfExists(actualPath);
            log.info("本地文件删除成功: {}", filePath);
        } catch (IOException e) {
            log.warn("本地文件删除失败: {}", filePath, e);
        }
    }

    /**
     * 根据文件后缀提取文本
     */
    private String extractText(String filename, byte[] fileBytes) {
        if (filename == null) return null;
        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) {
            return extractPdfText(fileBytes);
        } else if (lower.endsWith(".docx")) {
            return extractDocxText(fileBytes);
        } else if (lower.endsWith(".doc")) {
            // 老版本 .doc 格式 POI 支持较差，提示用户转成 .docx
            throw new BusinessException("暂不支持旧版 .doc 格式，请转为 .docx 再上传");
        } else if (lower.endsWith(".txt")) {
            return new String(fileBytes);
        }
        throw new BusinessException("仅支持 PDF、DOCX、TXT 格式");
    }

    /**
     * PDFBox 提取PDF文本
     */
    private String extractPdfText(byte[] fileBytes) {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("PDF文本提取失败", e);
            return null;
        }
    }

    /**
     * POI 提取 Word(docx) 文本
     */
    private String extractDocxText(byte[] fileBytes) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(fileBytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        } catch (IOException e) {
            log.error("Word文本提取失败", e);
            return null;
        }
    }

    /**
     * 将提取的文本发送给 Spring AI，要求输出结构化 JSON
     */
    private String parseWithAI(String resumeText) {
        String prompt =
                "你是一名专业的人力资源简历解析助手。\n" +
                "请从以下简历文本中提取关键信息，并以严格的 JSON 格式输出。\n" +
                "如果某个字段找不到，用空字符串 \"\" 表示。\n\n" +
                "输出 JSON 格式：\n" +
                "{\n" +
                "  \"name\": \"姓名\",\n" +
                "  \"phone\": \"手机号\",\n" +
                "  \"email\": \"邮箱\",\n" +
                "  \"education\": \"最高学历\",\n" +
                "  \"school\": \"毕业院校\",\n" +
                "  \"major\": \"专业\",\n" +
                "  \"graduationYear\": \"毕业年份\",\n" +
                "  \"experience\": \"工作年限/经验概述\",\n" +
                "  \"skills\": \"技能标签(逗号分隔)\",\n" +
                "  \"currentPosition\": \"当前职位\",\n" +
                "  \"expectedSalary\": \"期望薪资\"\n" +
                "}\n\n" +
                "只输出 JSON，不要任何解释。\n\n" +
                "简历文本：\n" + resumeText;

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}