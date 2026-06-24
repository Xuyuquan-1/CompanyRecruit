-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        9.2.0 - MySQL Community Server - GPL
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  12.1.0.6537
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 recruitment_db 的数据库结构
CREATE DATABASE IF NOT EXISTS `recruitment_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `recruitment_db`;

-- 导出  表 recruitment_db.ai_chat_history 结构
CREATE TABLE IF NOT EXISTS `ai_chat_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `question` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户问题',
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AI回答',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_user` (`user_id`),
  CONSTRAINT `fk_chat_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话历史表';

-- 正在导出表  recruitment_db.ai_chat_history 的数据：~3 rows (大约)
DELETE FROM `ai_chat_history`;
INSERT INTO `ai_chat_history` (`id`, `user_id`, `question`, `answer`, `create_time`) VALUES
	(1, 2, '如何发布一个新岗位？', '请进入岗位管理页面，点击"新增岗位"按钮，填写岗位名称、部门、职责和要求等信息后保存，状态为"草稿"。确认无误后可点击"发布"。', '2026-06-09 09:00:00'),
	(2, 2, '我需要筛选Java开发的简历', '已在简历管理模块为您打开筛选功能，您可以在搜索栏输入"Java"或选择标签"Java"进行快速筛选。', '2026-06-09 09:01:00'),
	(3, 3, '面试评价怎么写？', '您可以在面试记录中点击“填写评价”，系统会根据您输入的关键词自动生成规范的评价报告。', '2026-06-09 09:30:00');

-- 导出  表 recruitment_db.application 结构
CREATE TABLE IF NOT EXISTS `application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '应聘记录ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `resume_id` bigint NOT NULL COMMENT '简历ID',
  `candidate_id` bigint DEFAULT NULL COMMENT '应聘者用户ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待筛选 1-通过筛选 2-面试中 3-待确认Offer 4-不录用 5-已接受Offer(待入职) 6-已入职 7-候选人撤回',
  `result` tinyint NOT NULL DEFAULT '0' COMMENT '结果归集：0-处理中 1-录用成功 2-应聘失败（仅当status=4或6时允许非0）',
  `refuse_type` tinyint DEFAULT NULL COMMENT '失败细分：1-简历淘汰 2-面试淘汰 3-候选人拒Offer 4-材料不合格 5-录用审批驳回 6-候选人主动撤回 7-岗位关闭终止',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签',
  `remark` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `match_score` decimal(5,2) DEFAULT NULL COMMENT '智能匹配度分数（0-100）',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_resume` (`job_id`,`resume_id`),
  KEY `idx_app_status` (`status`),
  KEY `idx_app_job` (`job_id`),
  KEY `idx_result_status` (`result`,`status`),
  KEY `idx_status_result` (`status`,`result`),
  KEY `idx_refuse_type` (`refuse_type`),
  KEY `fk_app_resume` (`resume_id`),
  KEY `fk_app_candidate` (`candidate_id`),
  CONSTRAINT `fk_app_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_app_job` FOREIGN KEY (`job_id`) REFERENCES `job_post` (`id`),
  CONSTRAINT `fk_app_resume` FOREIGN KEY (`resume_id`) REFERENCES `resume` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应聘记录表';

-- 正在导出表  recruitment_db.application 的数据：~11 rows (大约)
DELETE FROM `application`;
INSERT INTO `application` (`id`, `job_id`, `resume_id`, `candidate_id`, `status`, `result`, `refuse_type`, `tags`, `remark`, `match_score`, `apply_time`, `update_time`, `deleted`) VALUES
	(14, 3, 13, 6, 4, 2, 1, NULL, 'bad', NULL, '2026-06-24 09:45:50', '2026-06-24 09:45:50', 0),
	(15, 2, 14, 6, 4, 2, 2, NULL, NULL, NULL, '2026-06-24 09:48:57', '2026-06-24 09:48:57', 0),
	(17, 6, 14, 6, 4, 2, 3, NULL, NULL, NULL, '2026-06-24 10:10:03', '2026-06-24 10:10:03', 0),
	(18, 1, 14, 6, 4, 2, 2, NULL, NULL, NULL, '2026-06-24 10:11:16', '2026-06-24 10:11:16', 0),
	(21, 2, 15, 4, 6, 1, NULL, NULL, NULL, NULL, '2026-06-24 10:47:02', '2026-06-24 10:47:02', 0),
	(22, 6, 15, 4, 3, 0, NULL, NULL, NULL, NULL, '2026-06-24 11:15:38', '2026-06-24 11:15:38', 0),
	(23, 3, 15, 4, 1, 0, NULL, NULL, 'good', NULL, '2026-06-24 14:04:19', '2026-06-24 14:04:19', 0);

-- 导出  表 recruitment_db.employee 结构
CREATE TABLE IF NOT EXISTS `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工档案ID',
  `offer_id` bigint DEFAULT NULL COMMENT '关联录用记录ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属部门',
  `position` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '岗位',
  `expected_join_date` date DEFAULT NULL COMMENT '预计入职日期',
  `actual_join_date` date DEFAULT NULL COMMENT '实际入职日期',
  `join_date` date DEFAULT NULL COMMENT '入职日期',
  `id_card_status` tinyint DEFAULT '0' COMMENT '身份证：0-未提交，1-已提交',
  `contract_status` tinyint DEFAULT '0' COMMENT '合同：0-未提交，1-已提交',
  `medical_report_status` tinyint DEFAULT '0' COMMENT '体检报告：0-未提交，1-已提交',
  `profile_data` json DEFAULT NULL COMMENT '员工详细档案',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-离职，1-在职，2-入职中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_offer_id` (`offer_id`),
  CONSTRAINT `fk_employee_offer` FOREIGN KEY (`offer_id`) REFERENCES `offer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工档案表';

-- 正在导出表  recruitment_db.employee 的数据：~2 rows (大约)
DELETE FROM `employee`;
INSERT INTO `employee` (`id`, `offer_id`, `name`, `phone`, `email`, `department`, `position`, `expected_join_date`, `actual_join_date`, `join_date`, `id_card_status`, `contract_status`, `medical_report_status`, `profile_data`, `status`, `create_time`, `update_time`) VALUES
	(5, 7, '王五', '13900000001', 'wangwu@email.com', '产品部', '产品经理', '2026-06-25', '2026-06-15', NULL, 1, 1, 1, NULL, 1, '2026-06-24 11:03:58', '2026-06-24 11:03:58');

-- 导出  表 recruitment_db.interview 结构
CREATE TABLE IF NOT EXISTS `interview` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  `application_id` bigint NOT NULL COMMENT '应聘记录ID',
  `interview_time` datetime NOT NULL COMMENT '面试时间',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '面试地点/线上链接',
  `interviewer_id` bigint DEFAULT NULL COMMENT '面试官ID',
  `interviewer_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '面试官姓名',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待面试 1已面试 2已取消',
  `result` tinyint DEFAULT '0' COMMENT '面试结果：0未定 1通过 2不通过',
  `evaluation` text COLLATE utf8mb4_unicode_ci COMMENT '面试评价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_interview_app` (`application_id`),
  KEY `idx_interviewer` (`interviewer_id`),
  CONSTRAINT `fk_interview_app` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`),
  CONSTRAINT `fk_interviewer_user` FOREIGN KEY (`interviewer_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试表';

-- 正在导出表  recruitment_db.interview 的数据：~4 rows (大约)
DELETE FROM `interview`;
INSERT INTO `interview` (`id`, `application_id`, `interview_time`, `location`, `interviewer_id`, `interviewer_name`, `status`, `result`, `evaluation`, `create_time`, `update_time`) VALUES
	(6, 15, '2026-06-24 10:00:00', '201', NULL, 'lxt', 2, 0, NULL, '2026-06-24 09:51:11', '2026-06-24 09:51:11'),
	(8, 17, '2026-06-24 12:00:00', '101', NULL, 'lxt', 1, 1, 'good', '2026-06-24 10:10:51', '2026-06-24 10:10:51'),
	(9, 18, '2026-06-24 14:00:00', '101', NULL, 'lxt', 1, 2, 'bad', '2026-06-24 10:11:41', '2026-06-24 10:11:41'),
	(12, 21, '2026-06-24 10:48:22', '131', NULL, 'lxt', 1, 1, 'good', '2026-06-24 10:47:34', '2026-06-24 10:47:34'),
	(13, 22, '2026-06-25 00:00:00', '144', NULL, 'lxt', 1, 1, 'good\n', '2026-06-24 11:16:02', '2026-06-24 11:16:02');

-- 导出  表 recruitment_db.job_post 结构
CREATE TABLE IF NOT EXISTS `job_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `department` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '岗位职责',
  `requirements` text COLLATE utf8mb4_unicode_ci COMMENT '岗位要求',
  `headcount` int DEFAULT '1' COMMENT '招聘人数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-草稿，1-已发布，2-已关闭',
  `create_user` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-否，1-是',
  PRIMARY KEY (`id`),
  KEY `idx_job_status` (`status`),
  KEY `idx_job_department` (`department`),
  KEY `fk_job_user` (`create_user`),
  CONSTRAINT `fk_job_user` FOREIGN KEY (`create_user`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘岗位表';

-- 正在导出表  recruitment_db.job_post 的数据：~6 rows (大约)
DELETE FROM `job_post`;
INSERT INTO `job_post` (`id`, `title`, `department`, `description`, `requirements`, `headcount`, `status`, `create_user`, `create_time`, `update_time`, `deleted`) VALUES
	(1, 'Java开发工程师', '技术部', '负责后端服务的设计与开发，参与系统架构优化。', '1. 3年以上Java开发经验；2. 熟悉Spring Boot、MyBatis；3. 熟悉MySQL、Redis。', 2, 1, 2, '2026-04-10 09:00:00', '2026-06-24 09:29:15', 0),
	(2, '产品经理', '产品部', '负责产品规划、需求分析及项目管理。', '1. 2年以上互联网产品经验；2. 具备数据分析能力；3. 沟通协调能力强。', 1, 1, 2, '2026-04-15 10:00:00', '2026-06-24 09:29:15', 0),
	(3, '前端开发工程师', '技术部', '负责Web前端页面开发，与后端协作实现产品功能。', '1. 熟练使用Vue.js/React；2. 了解前端工程化；3. 2年以上前端经验。', 3, 1, 2, '2026-05-01 08:30:00', '2026-06-24 09:29:15', 0),
	(4, '市场专员', '市场部', '负责市场推广活动策划与执行。', '1. 1年以上市场营销经验；2. 文案能力优秀；3. 执行力强。', 1, 2, 3, '2026-05-10 14:00:00', '2026-06-24 09:29:15', 0),
	(5, '数据分析师', '数据部', '负责业务数据分析，输出数据报告。', '1. 统计学或计算机相关专业；2. 熟悉SQL、Python；3. 有数据可视化经验。', 1, 0, 2, '2026-06-01 09:00:00', '2026-06-24 09:29:15', 0),
	(6, '测试工程师', '技术部', '负责产品功能测试、性能测试，撰写测试报告。', '1. 2年以上测试经验；2. 熟悉自动化测试工具；3. 了解CI/CD流程。', 2, 1, 2, '2026-05-20 16:00:00', '2026-06-24 09:29:15', 0);

-- 导出  表 recruitment_db.offer 结构
CREATE TABLE IF NOT EXISTS `offer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '录用ID',
  `application_id` bigint NOT NULL COMMENT '应聘记录ID',
  `offer_time` datetime DEFAULT NULL COMMENT '发送录用通知时间',
  `expected_join_date` date DEFAULT NULL COMMENT '预计入职日期',
  `status` tinyint DEFAULT '0' COMMENT 'Offer状态：0待确认 1已接受 2已拒绝 3已入职',
  `docs_submitted` json DEFAULT NULL COMMENT '入职资料提交情况',
  `salary` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '薪资待遇',
  `benefits` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '福利说明',
  `remark` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_offer_app` (`application_id`),
  CONSTRAINT `fk_offer_app` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录用表';

-- 正在导出表  recruitment_db.offer 的数据：~3 rows (大约)
DELETE FROM `offer`;
INSERT INTO `offer` (`id`, `application_id`, `offer_time`, `expected_join_date`, `status`, `docs_submitted`, `salary`, `benefits`, `remark`, `create_time`, `update_time`) VALUES
	(4, 17, '2026-06-24 10:16:04', '2026-06-25', 2, NULL, '10k', '无偿加班', '无', '2026-06-24 10:16:04', '2026-06-24 10:16:04'),
	(7, 21, '2026-06-24 10:47:51', '2026-06-25', 3, '{"contract": true, "idCardBack": true, "contractUrl": "https://companyrecruit.oss-cn-beijing.aliyuncs.com/upload/09e5b437-3472-4c39-8e6e-9fb29aab6ab0.pdf", "idCardFront": true, "idCardBackUrl": "https://companyrecruit.oss-cn-beijing.aliyuncs.com/upload/4a2e8bae-234e-4b54-abc0-0f6593904f7f.pdf", "medicalReport": true, "idCardFrontUrl": "https://companyrecruit.oss-cn-beijing.aliyuncs.com/upload/348ea37c-e074-4075-b2c6-700330de1973.pdf", "medicalReportUrl": "https://companyrecruit.oss-cn-beijing.aliyuncs.com/upload/f6cb9172-b2bd-4bcf-8331-bbd0992e6eca.pdf"}', '14k', '无', '无', '2026-06-24 10:47:51', '2026-06-24 10:47:51'),
	(8, 22, '2026-06-24 11:16:20', '2026-06-25', 0, NULL, '13', '无', 'good', '2026-06-24 11:16:20', '2026-06-24 11:16:20');

-- 导出  表 recruitment_db.resume 结构
CREATE TABLE IF NOT EXISTS `resume` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径（本地或OSS）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `upload_by` bigint DEFAULT NULL COMMENT '上传人ID',
  `parse_status` tinyint DEFAULT '0' COMMENT '解析状态：0-待解析，1-已解析，2-解析失败',
  `parsed_json` json DEFAULT NULL COMMENT '解析后结构化数据',
  `vector_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对应向量库文档ID',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简历技能标签（逗号分隔）',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_parse_status` (`parse_status`),
  KEY `idx_resume_upload_by` (`upload_by`),
  KEY `idx_resume_tags` (`tags`),
  CONSTRAINT `fk_resume_user` FOREIGN KEY (`upload_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- 正在导出表  recruitment_db.resume 的数据：~12 rows (大约)
DELETE FROM `resume`;
INSERT INTO `resume` (`id`, `original_filename`, `file_path`, `file_size`, `upload_by`, `parse_status`, `parsed_json`, `vector_id`, `tags`, `upload_time`, `update_time`) VALUES
	(13, '刘一.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/dcfa5780-ca0a-4d6f-8822-dbc545d06dea.pdf', 748361, 6, 1, '{"name": "刘一", "email": "Liuyi@163.com", "major": "计算机科学与技术", "phone": "15678398796", "school": "聊城大学", "skills": "Java, JVM, Spring, SpringMVC, SpringBoot, Spring Cloud Alibaba, Nacos, OpenFeign, Gateway, Sentinel, Seata, MyBatis, MyBatis-Plus, MySQL, Redis, RocketMQ, Kafka, XXL-Job, Elasticsearch, Maven, Gradle, Git, Linux, Docker, Apifox, SkyWalking, Postman", "education": "本科", "experience": "2年", "expectedSalary": "", "graduationYear": "2022", "currentPosition": "Java开发工程师"}', NULL, 'Java, JVM, Spring, SpringMVC, SpringBoot, Spring Cloud Alibaba, Nacos, OpenFeign, Gateway, Sentinel, Seata, MyBatis, MyBatis-Plus, MySQL, Redis, RocketMQ, Kafka, XXL-Job, Elasticsearch, Maven, Gradle, Git, Linux, Docker, Apifox, SkyWalking, Postman', '2026-06-24 09:36:00', '2026-06-24 09:36:00'),
	(14, '郑十.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/24d67d6e-8c96-4861-8336-768b1c3b552a.pdf', 755479, 6, 1, '{"name": "郑十", "email": "Zhengshi@163.com", "major": "数字媒体技术", "phone": "13256389456", "school": "郑州大学", "skills": "PS,AI,PR,AE,剪映,C4D,Excel,PPT,飞书,钉钉,Canva,HTML,CSS,新抖,蝉妈妈", "education": "硕士", "experience": "2024.07-至今 曦曦商贸有限公司 市场专员; 2023.07-2023.09 建文文化传媒公司 市场实习生", "expectedSalary": "10k", "graduationYear": "2020", "currentPosition": "市场专员"}', NULL, 'PS,AI,PR,AE,剪映,C4D,Excel,PPT,飞书,钉钉,Canva,HTML,CSS,新抖,蝉妈妈', '2026-06-24 09:37:02', '2026-06-24 09:37:02'),
	(15, '王五.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/f7d5e26d-77bd-45a0-bf0a-57c0d9cdae93.pdf', 552251, 4, 1, '{"name": "王五", "email": "852747830@qq.com", "major": "软件工程", "phone": "18266789559", "school": "山东理工大学", "skills": "Java,Spring,Spring MVC,Spring Boot,Spring Cloud Alibaba,Nacos,Gateway,OpenFeign,Sentinel,Seata,MyBatis,MyBatis-Plus,MySQL,Redis,Elasticsearch,RocketMQ,Kafka,XXL-Job,Maven,Git,Docker,Apifox,SkyWalking,Spring Security", "education": "本科", "experience": "4年", "expectedSalary": "", "graduationYear": "2023", "currentPosition": "Java开发工程师"}', NULL, 'Java,Spring,Spring MVC,Spring Boot,Spring Cloud Alibaba,Nacos,Gateway,OpenFeign,Sentinel,Seata,MyBatis,MyBatis-Plus,MySQL,Redis,Elasticsearch,RocketMQ,Kafka,XXL-Job,Maven,Git,Docker,Apifox,SkyWalking,Spring Security', '2026-06-24 10:26:44', '2026-06-24 10:26:44');

-- 导出  表 recruitment_db.sys_menu 结构
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `permission` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限标识',
  `type` tinyint NOT NULL COMMENT '类型：1-目录，2-菜单，3-按钮',
  `path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端路由',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 正在导出表  recruitment_db.sys_menu 的数据：~30 rows (大约)
DELETE FROM `sys_menu`;
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `permission`, `type`, `path`, `icon`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
	(1, 0, '岗位管理', 'job', 1, '/job', 'briefcase', 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(2, 1, '岗位列表', 'job:list', 2, '/job/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(3, 1, '新增岗位', 'job:add', 3, NULL, NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(4, 1, '编辑岗位', 'job:edit', 3, NULL, NULL, 3, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(5, 1, '删除岗位', 'job:delete', 3, NULL, NULL, 4, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(6, 0, '简历管理', 'resume', 1, '/resume', 'file-text', 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(7, 6, '简历列表', 'resume:list', 2, '/resume/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(8, 6, '上传简历', 'resume:upload', 3, NULL, NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(9, 6, '简历管理操作', 'resume:manage', 3, NULL, NULL, 3, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(10, 0, '应聘筛选', 'application', 1, '/application', 'filter', 3, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(11, 10, '应聘列表', 'application:list', 2, '/application/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(12, 10, '安排面试', 'application:interview', 3, NULL, NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(13, 0, '面试管理', 'interview', 1, '/interview', 'users', 4, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(14, 13, '面试列表', 'interview:list', 2, '/interview/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(15, 13, '填写评价', 'interview:evaluate', 3, NULL, NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(16, 0, '录用管理', 'offer', 1, '/offer', 'check-circle', 5, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(17, 16, '录用列表', 'offer:list', 2, '/offer/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(18, 0, '统计分析', 'report', 1, '/report', 'bar-chart', 6, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(19, 18, '招聘报表', 'report:list', 2, '/report/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(20, 18, '数据导出', 'report:export', 3, NULL, NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(21, 0, '系统管理', 'system', 1, '/system', 'settings', 7, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(22, 21, '用户管理', 'system:user', 2, '/system/user', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(23, 21, '角色管理', 'system:role', 2, '/system/role', NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(24, 21, '审计日志', 'system:audit', 2, '/system/audit', NULL, 3, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(25, 0, 'AI助手', 'ai', 1, '/ai', 'message-circle', 8, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(28, 0, '员工管理', 'employee', 1, '/employee', 'user-check', 9, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(29, 28, '员工列表', 'employee:list', 2, '/employee/list', NULL, 1, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(30, 28, '员工入职', 'employee:onboard', 3, NULL, NULL, 2, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(31, 28, '编辑员工', 'employee:edit', 3, NULL, NULL, 3, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(32, 28, '员工资料提交', 'employee:submit', 3, NULL, NULL, 4, 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15');

-- 导出  表 recruitment_db.sys_role 结构
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 正在导出表  recruitment_db.sys_role 的数据：~3 rows (大约)
DELETE FROM `sys_role`;
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `create_time`, `update_time`) VALUES
	(1, 'ROLE_ADMIN', '管理员', '系统管理员，拥有所有权限', 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(2, 'ROLE_RECRUITER', '招聘人员', '负责发布岗位、筛选简历、安排面试', 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15'),
	(3, 'ROLE_CANDIDATE', '应聘者', '可上传简历、查看应聘状态', 1, '2026-06-24 09:29:15', '2026-06-24 09:29:15');

-- 导出  表 recruitment_db.sys_role_menu 结构
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 正在导出表  recruitment_db.sys_role_menu 的数据：~58 rows (大约)
DELETE FROM `sys_role_menu`;
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
	(1, 1),
	(1, 2),
	(1, 3),
	(1, 4),
	(1, 5),
	(1, 6),
	(1, 7),
	(1, 8),
	(1, 9),
	(1, 10),
	(1, 11),
	(1, 12),
	(1, 13),
	(1, 14),
	(1, 15),
	(1, 16),
	(1, 17),
	(1, 18),
	(1, 19),
	(1, 20),
	(1, 21),
	(1, 22),
	(1, 23),
	(1, 24),
	(1, 25),
	(1, 28),
	(1, 29),
	(1, 30),
	(1, 31),
	(1, 32),
	(2, 1),
	(2, 2),
	(2, 3),
	(2, 4),
	(2, 6),
	(2, 7),
	(2, 8),
	(2, 9),
	(2, 10),
	(2, 11),
	(2, 12),
	(2, 13),
	(2, 14),
	(2, 15),
	(2, 16),
	(2, 17),
	(2, 18),
	(2, 19),
	(2, 20),
	(2, 25),
	(2, 28),
	(2, 29),
	(2, 30),
	(3, 6),
	(3, 7),
	(3, 8),
	(3, 10),
	(3, 11);

-- 导出  表 recruitment_db.sys_user 结构
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 正在导出表  recruitment_db.sys_user 的数据：~10 rows (大约)
DELETE FROM `sys_user`;
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `avatar`, `status`, `dept_id`, `last_login_time`, `create_time`, `update_time`, `deleted`) VALUES
	(1, 'admin', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '系统管理员', '13800000001', 'admin@company.com', NULL, 1, 1, '2026-06-08 09:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(2, 'hr_zhang', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '张HR', '13800000002', 'zhanghr@company.com', NULL, 1, 2, '2026-06-09 08:30:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(3, 'hr_li', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '李HR', '13800000003', 'lihr@company.com', NULL, 1, 2, '2026-06-09 08:45:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(4, 'candidate_wang', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '王五', '13900000001', 'wangwu@email.com', NULL, 1, NULL, '2026-06-05 10:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(5, 'candidate_zhao', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '赵六', '13900000002', 'zhaoliu@email.com', NULL, 1, NULL, '2026-06-06 11:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(6, 'candidate_sun', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '孙七', '13900000003', 'sunqi@email.com', NULL, 1, NULL, '2026-06-07 14:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(7, 'candidate_zhou', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '周八', '13900000004', 'zhouba@email.com', NULL, 1, NULL, '2026-06-07 15:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(8, 'candidate_wu', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '吴九', '13900000005', 'wujiu@email.com', NULL, 1, NULL, '2026-06-08 09:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(9, 'candidate_zheng', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '郑十', '13900000006', 'zhengshi@email.com', NULL, 1, NULL, '2026-06-08 10:00:00', '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0),
	(10, 'candidate_liu', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '刘一', '13900000007', 'liuyi@email.com', NULL, 0, NULL, NULL, '2026-06-24 09:29:15', '2026-06-24 09:29:15', 0);

-- 导出  表 recruitment_db.sys_user_role 结构
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 正在导出表  recruitment_db.sys_user_role 的数据：~9 rows (大约)
DELETE FROM `sys_user_role`;
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
	(1, 1),
	(2, 2),
	(3, 2),
	(4, 3),
	(5, 3),
	(6, 3),
	(7, 3),
	(8, 3),
	(9, 3);

-- 导出  触发器 recruitment_db.trg_application_status_check 结构
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `trg_application_status_check` BEFORE UPDATE ON `application` FOR EACH ROW BEGIN
    -- 1. 终态（4/6）不可回退
    IF OLD.status IN (4, 6) AND NEW.status NOT IN (4, 6) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '终态记录不允许回退到非终态';
    END IF;

    -- 2. 不录用必须填写失败原因
    IF NEW.status = 4 AND NEW.refuse_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '不录用状态必须指定失败原因(refuse_type)';
    END IF;

    -- 3. 不录用状态 result 必须为 2
    IF NEW.status = 4 AND NEW.result != 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '不录用状态的result必须为2(应聘失败)';
    END IF;

    -- 4. 已入职状态 result 必须为 1
    IF NEW.status = 6 AND NEW.result != 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '已入职状态的result必须为1(录用成功)';
    END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
