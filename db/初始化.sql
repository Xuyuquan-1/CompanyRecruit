-- ============================================================
-- 人力资源招聘管理系统 - 完整数据库初始化脚本（含 resume.tags 字段）
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- 合并日期：2026-06-23
-- ============================================================

CREATE DATABASE IF NOT EXISTS recruitment_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE recruitment_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 删表（逆序）
DROP TABLE IF EXISTS sys_event_failure;
DROP TABLE IF EXISTS ai_chat_history;
DROP TABLE IF EXISTS sys_audit_log;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS offer;
DROP TABLE IF EXISTS interview;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS job_post;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 表结构创建
-- ============================================================

-- 系统用户表
CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
                          real_name VARCHAR(50) COMMENT '真实姓名',
                          phone VARCHAR(20) COMMENT '手机号',
                          email VARCHAR(100) COMMENT '邮箱',
                          avatar VARCHAR(255) COMMENT '头像URL',
                          status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                          dept_id BIGINT COMMENT '部门ID',
                          last_login_time DATETIME COMMENT '最后登录时间',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
                          UNIQUE KEY uk_username (username),
                          KEY idx_phone (phone),
                          KEY idx_email (email),
                          KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 角色表
CREATE TABLE sys_role (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
                          role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
                          role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
                          description VARCHAR(200) COMMENT '角色描述',
                          status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 菜单权限表
CREATE TABLE sys_menu (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
                          parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
                          name VARCHAR(50) NOT NULL COMMENT '菜单名称',
                          permission VARCHAR(100) COMMENT '权限标识',
                          type TINYINT NOT NULL COMMENT '类型：1-目录，2-菜单，3-按钮',
                          path VARCHAR(200) COMMENT '前端路由',
                          icon VARCHAR(100) COMMENT '图标',
                          sort_order INT DEFAULT 0 COMMENT '排序',
                          status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
                               role_id BIGINT NOT NULL COMMENT '角色ID',
                               menu_id BIGINT NOT NULL COMMENT '菜单ID',
                               PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               role_id BIGINT NOT NULL COMMENT '角色ID',
                               PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 招聘岗位表
CREATE TABLE job_post (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
                          title VARCHAR(100) NOT NULL COMMENT '岗位名称',
                          department VARCHAR(100) NOT NULL COMMENT '部门',
                          description TEXT COMMENT '岗位职责',
                          requirements TEXT COMMENT '岗位要求',
                          headcount INT DEFAULT 1 COMMENT '招聘人数',
                          status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已关闭',
                          create_user BIGINT COMMENT '创建人ID',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
                          KEY idx_job_status (status),
                          KEY idx_job_department (department),
                          CONSTRAINT fk_job_user FOREIGN KEY (create_user) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘岗位表';

-- 简历表（已包含 tags 字段及索引）
CREATE TABLE resume (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
                        original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
                        file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径（本地或OSS）',
                        file_size BIGINT COMMENT '文件大小（字节）',
                        upload_by BIGINT COMMENT '上传人ID',
                        parse_status TINYINT DEFAULT 0 COMMENT '解析状态：0-待解析，1-已解析，2-解析失败',
                        parsed_json JSON COMMENT '解析后结构化数据',
                        vector_id VARCHAR(100) COMMENT '对应向量库文档ID',
                        tags VARCHAR(500) COMMENT '简历技能标签（逗号分隔）',           -- 新增字段
                        upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        KEY idx_resume_parse_status (parse_status),
                        KEY idx_resume_upload_by (upload_by),
                        KEY idx_resume_tags (tags),                                    -- 新增索引
                        CONSTRAINT fk_resume_user FOREIGN KEY (upload_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- 应聘记录表
CREATE TABLE application (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '应聘记录ID',
                             job_id BIGINT NOT NULL COMMENT '岗位ID',
                             resume_id BIGINT NOT NULL COMMENT '简历ID',
                             candidate_id BIGINT COMMENT '应聘者用户ID',
                             status TINYINT NOT NULL DEFAULT 0 COMMENT '主状态：0待筛选 1简历不通过 2待面试 3面试不通过 4Offer待确认 5候选人拒Offer 6待提交材料 7材料阶段淘汰 8待录用 9录用审批驳回 10已入职',
                             result TINYINT NOT NULL DEFAULT 0 COMMENT '结果归集：0处理中 1录用成功 2应聘失败',
                             refuse_type TINYINT DEFAULT NULL COMMENT '失败细分：1简历淘汰 2面试淘汰 3候选人拒Offer 4材料不合格 5录用审批驳回',
                             tags VARCHAR(500) COMMENT '标签',
                             remark VARCHAR(1000) COMMENT '备注',
                             match_score DECIMAL(5,2) COMMENT '智能匹配度分数（0-100）',
                             apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
                             UNIQUE KEY uk_job_resume (job_id, resume_id),
                             KEY idx_app_status (status),
                             KEY idx_app_job (job_id),
                             KEY idx_result_status (result, status),
                             CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES job_post(id),
                             CONSTRAINT fk_app_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
                             CONSTRAINT fk_app_candidate FOREIGN KEY (candidate_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应聘记录表';

-- 面试表
CREATE TABLE interview (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试ID',
                           application_id BIGINT NOT NULL COMMENT '应聘记录ID',
                           interview_time DATETIME NOT NULL COMMENT '面试时间',
                           location VARCHAR(200) COMMENT '面试地点/线上链接',
                           interviewer_id BIGINT COMMENT '面试官ID',
                           interviewer_name VARCHAR(50) COMMENT '面试官姓名',
                           status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待面试 1面试通过 2面试不通过 3已取消',
                           evaluation TEXT COMMENT '面试评价',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                           update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           KEY idx_interview_app (application_id),
                           KEY idx_interviewer (interviewer_id),
                           CONSTRAINT fk_interview_app FOREIGN KEY (application_id) REFERENCES application(id),
                           CONSTRAINT fk_interviewer_user FOREIGN KEY (interviewer_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试表';

-- 录用表
CREATE TABLE offer (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '录用ID',
                       application_id BIGINT NOT NULL COMMENT '应聘记录ID',
                       offer_time DATETIME COMMENT '发送录用通知时间',
                       expected_join_date DATE COMMENT '预计入职日期',
                       status TINYINT DEFAULT 0 COMMENT 'Offer状态：0待确认 1已接受 2已拒绝',
                       docs_submitted JSON COMMENT '入职资料提交情况',
                       salary VARCHAR(200) COMMENT '薪资待遇',
                       benefits VARCHAR(500) COMMENT '福利说明',
                       remark VARCHAR(1000) COMMENT '备注',
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       UNIQUE KEY uk_offer_app (application_id),
                       CONSTRAINT fk_offer_app FOREIGN KEY (application_id) REFERENCES application(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录用表';

-- 员工档案表
CREATE TABLE employee (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工档案ID',
                          offer_id BIGINT COMMENT '关联录用记录ID',
                          name VARCHAR(50) NOT NULL COMMENT '员工姓名',
                          phone VARCHAR(20) COMMENT '手机号',
                          email VARCHAR(100) COMMENT '邮箱',
                          department VARCHAR(100) COMMENT '所属部门',
                          position VARCHAR(100) COMMENT '岗位',
                          expected_join_date DATE COMMENT '预计入职日期',
                          actual_join_date DATE COMMENT '实际入职日期',
                          docs_status JSON COMMENT '资料提交状态',
                          profile_data JSON COMMENT '员工详细档案',
                          status TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职，2-入职中',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY uk_offer_id (offer_id),
                          CONSTRAINT fk_employee_offer FOREIGN KEY (offer_id) REFERENCES offer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工档案表';

-- 操作审计日志表
CREATE TABLE sys_audit_log (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
                               user_id BIGINT COMMENT '操作人ID',
                               username VARCHAR(50) COMMENT '操作人用户名',
                               operation VARCHAR(100) NOT NULL COMMENT '操作类型',
                               method VARCHAR(200) COMMENT '请求方法',
                               request_params TEXT COMMENT '请求参数',
                               response_result TEXT COMMENT '返回结果（脱敏后）',
                               ip_address VARCHAR(50) COMMENT 'IP地址',
                               user_agent VARCHAR(500) COMMENT '浏览器UA',
                               execute_time BIGINT COMMENT '执行耗时(ms)',
                               status TINYINT DEFAULT 1 COMMENT '状态：1-成功，0-异常',
                               error_msg TEXT COMMENT '异常信息',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                               KEY idx_audit_user (user_id),
                               KEY idx_audit_operation (operation),
                               KEY idx_audit_time (create_time),
                               CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- AI对话历史表
CREATE TABLE ai_chat_history (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
                                 user_id BIGINT COMMENT '用户ID',
                                 question VARCHAR(2000) NOT NULL COMMENT '用户问题',
                                 answer TEXT NOT NULL COMMENT 'AI回答',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
                                 KEY idx_chat_user (user_id),
                                 CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话历史表';

-- 事件失败记录表
CREATE TABLE sys_event_failure (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
                                   event_type VARCHAR(100) NOT NULL COMMENT '事件类型',
                                   event_body JSON NOT NULL COMMENT '事件体',
                                   error_message TEXT COMMENT '失败原因',
                                   retry_count INT DEFAULT 0 COMMENT '已重试次数',
                                   status TINYINT DEFAULT 0 COMMENT '状态：0-待重试，1-重试成功，2-最终失败',
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   KEY idx_event_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件失败记录表';

-- ============================================================
-- 初始数据
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 角色
INSERT INTO sys_role (id, role_code, role_name, description) VALUES
                                                                 (1, 'ROLE_ADMIN', '管理员', '系统管理员，拥有所有权限'),
                                                                 (2, 'ROLE_RECRUITER', '招聘人员', '负责发布岗位、筛选简历、安排面试'),
                                                                 (3, 'ROLE_CANDIDATE', '应聘者', '可上传简历、查看应聘状态');

-- 2. 菜单
INSERT INTO sys_menu (id, parent_id, name, permission, type, path, icon, sort_order) VALUES
                                                                                         (1, 0, '岗位管理', 'job', 1, '/job', 'briefcase', 1),
                                                                                         (2, 1, '岗位列表', 'job:list', 2, '/job/list', NULL, 1),
                                                                                         (3, 1, '新增岗位', 'job:add', 3, NULL, NULL, 2),
                                                                                         (4, 1, '编辑岗位', 'job:edit', 3, NULL, NULL, 3),
                                                                                         (5, 1, '删除岗位', 'job:delete', 3, NULL, NULL, 4),
                                                                                         (6, 0, '简历管理', 'resume', 1, '/resume', 'file-text', 2),
                                                                                         (7, 6, '简历列表', 'resume:list', 2, '/resume/list', NULL, 1),
                                                                                         (8, 6, '上传简历', 'resume:upload', 3, NULL, NULL, 2),
                                                                                         (9, 6, '简历管理操作', 'resume:manage', 3, NULL, NULL, 3),
                                                                                         (10, 0, '应聘筛选', 'application', 1, '/application', 'filter', 3),
                                                                                         (11, 10, '应聘列表', 'application:list', 2, '/application/list', NULL, 1),
                                                                                         (12, 10, '安排面试', 'application:interview', 3, NULL, NULL, 2),
                                                                                         (13, 0, '面试管理', 'interview', 1, '/interview', 'users', 4),
                                                                                         (14, 13, '面试列表', 'interview:list', 2, '/interview/list', NULL, 1),
                                                                                         (15, 13, '填写评价', 'interview:evaluate', 3, NULL, NULL, 2),
                                                                                         (16, 0, '录用管理', 'offer', 1, '/offer', 'check-circle', 5),
                                                                                         (17, 16, '录用列表', 'offer:list', 2, '/offer/list', NULL, 1),
                                                                                         (18, 0, '统计分析', 'report', 1, '/report', 'bar-chart', 6),
                                                                                         (19, 18, '招聘报表', 'report:list', 2, '/report/list', NULL, 1),
                                                                                         (20, 18, '数据导出', 'report:export', 3, NULL, NULL, 2),
                                                                                         (21, 0, '系统管理', 'system', 1, '/system', 'settings', 7),
                                                                                         (22, 21, '用户管理', 'system:user', 2, '/system/user', NULL, 1),
                                                                                         (23, 21, '角色管理', 'system:role', 2, '/system/role', NULL, 2),
                                                                                         (24, 21, '审计日志', 'system:audit', 2, '/system/audit', NULL, 3),
                                                                                         (25, 0, 'AI助手', 'ai', 1, '/ai', 'message-circle', 8),
                                                                                         (28, 0, '员工管理', 'employee', 1, '/employee', 'user-check', 9),
                                                                                         (29, 28, '员工列表', 'employee:list', 2, '/employee/list', NULL, 1),
                                                                                         (30, 28, '员工入职', 'employee:onboard', 3, NULL, NULL, 2),
                                                                                         (31, 28, '编辑员工', 'employee:edit', 3, NULL, NULL, 3),
                                                                                         (32, 28, '员工资料提交', 'employee:submit', 3, NULL, NULL, 4);

-- 3. 角色菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
                                                 (1, 6), (1, 7), (1, 8), (1, 9),
                                                 (1, 10), (1, 11), (1, 12),
                                                 (1, 13), (1, 14), (1, 15),
                                                 (1, 16), (1, 17),
                                                 (1, 18), (1, 19), (1, 20),
                                                 (1, 21), (1, 22), (1, 23), (1, 24),
                                                 (1, 25),
                                                 (1, 28), (1, 29), (1, 30), (1, 31), (1, 32);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (2, 1), (2, 2), (2, 3), (2, 4),
                                                 (2, 6), (2, 7), (2, 8), (2, 9),
                                                 (2, 10), (2, 11), (2, 12),
                                                 (2, 13), (2, 14), (2, 15),
                                                 (2, 16), (2, 17),
                                                 (2, 18), (2, 19), (2, 20),
                                                 (2, 25),
                                                 (2, 28), (2, 29), (2, 30);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (3, 6), (3, 7), (3, 8),
                                                 (3, 10), (3, 11);

-- 4. 用户
INSERT INTO sys_user (id, username, password, real_name, phone, email, status, dept_id, last_login_time, deleted) VALUES
                                                                                                                      (1, 'admin', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '系统管理员', '13800000001', 'admin@company.com', 1, 1, '2026-06-08 09:00:00', 0),
                                                                                                                      (2, 'hr_zhang', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '张HR', '13800000002', 'zhanghr@company.com', 1, 2, '2026-06-09 08:30:00', 0),
                                                                                                                      (3, 'hr_li', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '李HR', '13800000003', 'lihr@company.com', 1, 2, '2026-06-09 08:45:00', 0),
                                                                                                                      (4, 'candidate_wang', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '王五', '13900000001', 'wangwu@email.com', 1, NULL, '2026-06-05 10:00:00', 0),
                                                                                                                      (5, 'candidate_zhao', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '赵六', '13900000002', 'zhaoliu@email.com', 1, NULL, '2026-06-06 11:00:00', 0),
                                                                                                                      (6, 'candidate_sun', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '孙七', '13900000003', 'sunqi@email.com', 1, NULL, '2026-06-07 14:00:00', 0),
                                                                                                                      (7, 'candidate_zhou', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '周八', '13900000004', 'zhouba@email.com', 1, NULL, '2026-06-07 15:00:00', 0),
                                                                                                                      (8, 'candidate_wu', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '吴九', '13900000005', 'wujiu@email.com', 1, NULL, '2026-06-08 09:00:00', 0),
                                                                                                                      (9, 'candidate_zheng', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '郑十', '13900000006', 'zhengshi@email.com', 1, NULL, '2026-06-08 10:00:00', 0),
                                                                                                                      (10, 'candidate_liu', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '刘一', '13900000007', 'liuyi@email.com', 0, NULL, NULL, 0);

-- 5. 用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
                                                 (1, 1), (2, 2), (3, 2),
                                                 (4, 3), (5, 3), (6, 3), (7, 3), (8, 3), (9, 3);

-- 6. 招聘岗位
INSERT INTO job_post (id, title, department, description, requirements, headcount, status, create_user, create_time, deleted) VALUES
                                                                                                                                  (1, 'Java开发工程师', '技术部', '负责后端服务的设计与开发，参与系统架构优化。', '1. 3年以上Java开发经验；2. 熟悉Spring Boot、MyBatis；3. 熟悉MySQL、Redis。', 2, 1, 2, '2026-04-10 09:00:00', 0),
                                                                                                                                  (2, '产品经理', '产品部', '负责产品规划、需求分析及项目管理。', '1. 2年以上互联网产品经验；2. 具备数据分析能力；3. 沟通协调能力强。', 1, 1, 2, '2026-04-15 10:00:00', 0),
                                                                                                                                  (3, '前端开发工程师', '技术部', '负责Web前端页面开发，与后端协作实现产品功能。', '1. 熟练使用Vue.js/React；2. 了解前端工程化；3. 2年以上前端经验。', 3, 1, 2, '2026-05-01 08:30:00', 0),
                                                                                                                                  (4, '市场专员', '市场部', '负责市场推广活动策划与执行。', '1. 1年以上市场营销经验；2. 文案能力优秀；3. 执行力强。', 1, 2, 3, '2026-05-10 14:00:00', 0),
                                                                                                                                  (5, '数据分析师', '数据部', '负责业务数据分析，输出数据报告。', '1. 统计学或计算机相关专业；2. 熟悉SQL、Python；3. 有数据可视化经验。', 1, 0, 2, '2026-06-01 09:00:00', 0),
                                                                                                                                  (6, '测试工程师', '技术部', '负责产品功能测试、性能测试，撰写测试报告。', '1. 2年以上测试经验；2. 熟悉自动化测试工具；3. 了解CI/CD流程。', 2, 1, 2, '2026-05-20 16:00:00', 0);

-- 7. 简历（已预填充 tags 字段）
INSERT INTO resume (id, original_filename, file_path, file_size, upload_by, parse_status, parsed_json, vector_id, tags, upload_time) VALUES
                                                                                                                                         (1, '张三_Java开发工程师.pdf', '/files/resume/zhangsan_java.pdf', 256000, 2, 1,
                                                                                                                                          '{"name":"张三","phone":"13811112222","email":"zhangsan@email.com","education":[{"school":"清华大学","major":"计算机科学","degree":"硕士","startDate":"2018-09","endDate":"2021-06"}],"work_experience":[{"company":"ABC科技","position":"Java开发工程师","startDate":"2021-07","endDate":"2023-08","description":"负责后端服务开发，使用Spring Boot框架，参与订单系统重构。"}],"skills":["Java","Spring Boot","MySQL","Redis","微服务"]}',
                                                                                                                                          'vec_001', 'Java,Spring Boot,MySQL,Redis,微服务', '2026-04-20 10:00:00'),
                                                                                                                                         (2, '李四_产品经理.docx', '/files/resume/lisi_pm.docx', 145000, 4, 1,
                                                                                                                                          '{"name":"李四","phone":"13922223333","email":"lisi@email.com","education":[{"school":"北京大学","major":"信息管理","degree":"本科","startDate":"2017-09","endDate":"2021-06"}],"work_experience":[{"company":"DEF公司","position":"产品助理","startDate":"2021-07","endDate":"2023-12","description":"协助产品总监完成需求文档撰写、原型设计。"}],"skills":["产品设计","Axure","数据分析","沟通协调"]}',
                                                                                                                                          'vec_002', '产品设计,Axure,数据分析,沟通协调', '2026-04-22 11:00:00'),
                                                                                                                                         (3, '王五_前端开发工程师.pdf', '/files/resume/wangwu_fe.pdf', 310000, 5, 1,
                                                                                                                                          '{"name":"王五","phone":"13633334444","email":"wangwu@email.com","education":[{"school":"浙江大学","major":"软件工程","degree":"本科","startDate":"2019-09","endDate":"2023-06"}],"work_experience":[{"company":"GHI互联网","position":"前端开发","startDate":"2023-07","endDate":"2025-12","description":"使用Vue3开发公司官网和管理后台。"}],"skills":["Vue.js","React","JavaScript","HTML/CSS","Webpack"]}',
                                                                                                                                          'vec_003', 'Vue.js,React,JavaScript,HTML/CSS,Webpack', '2026-05-05 08:00:00'),
                                                                                                                                         (4, '赵六_Java开发工程师.docx', '/files/resume/zhaoliu_java.docx', 280000, 6, 1,
                                                                                                                                          '{"name":"赵六","phone":"13744445555","email":"zhaoliu@email.com","education":[{"school":"复旦大学","major":"计算机科学","degree":"硕士","startDate":"2017-09","endDate":"2020-06"}],"work_experience":[{"company":"JKL金融","position":"高级Java开发","startDate":"2020-07","endDate":"2026-02","description":"主导交易系统重构，日均交易量千万级。"}],"skills":["Java","Spring Cloud","Kafka","MySQL","分布式事务"]}',
                                                                                                                                          'vec_004', 'Java,Spring Cloud,Kafka,MySQL,分布式事务', '2026-05-10 09:00:00'),
                                                                                                                                         (5, '孙七_产品经理.pdf', '/files/resume/sunqi_pm.pdf', 220000, 7, 1,
                                                                                                                                          '{"name":"孙七","phone":"13555556666","email":"sunqi@email.com","education":[{"school":"上海交通大学","major":"工商管理","degree":"MBA","startDate":"2020-09","endDate":"2023-06"}],"work_experience":[{"company":"MNO电商","position":"产品经理","startDate":"2018-03","endDate":"2022-12","description":"负责供应链产品线，成功上线3个核心系统。"}],"skills":["产品策略","项目管理","SQL","Figma"]}',
                                                                                                                                          'vec_005', '产品策略,项目管理,SQL,Figma', '2026-05-12 10:00:00'),
                                                                                                                                         (6, '周八_前端开发.pdf', '/files/resume/zhouba_fe.pdf', 195000, 8, 2,
                                                                                                                                          NULL, NULL, NULL, '2026-05-15 14:00:00'),
                                                                                                                                         (7, '吴九_测试工程师.docx', '/files/resume/wujiu_test.docx', 330000, 9, 1,
                                                                                                                                          '{"name":"吴九","phone":"13466667777","email":"wujiu@email.com","education":[{"school":"华中科技大学","major":"计算机科学","degree":"本科","startDate":"2018-09","endDate":"2022-06"}],"work_experience":[{"company":"PQR软件","position":"测试工程师","startDate":"2022-07","endDate":"2026-05","description":"负责自动化测试框架搭建，提高测试效率40%。"}],"skills":["Selenium","JMeter","自动化测试","Java","Linux"]}',
                                                                                                                                          'vec_007', 'Selenium,JMeter,自动化测试,Java,Linux', '2026-05-20 09:00:00'),
                                                                                                                                         (8, '郑十_Java开发.pdf', '/files/resume/zhengshi_java.pdf', 410000, 2, 0,
                                                                                                                                          NULL, NULL, NULL, '2026-06-01 10:00:00'),
                                                                                                                                         (9, '刘一_数据分析师.docx', '/files/resume/liuyi_da.docx', 270000, 2, 1,
                                                                                                                                          '{"name":"刘一","phone":"13377778888","email":"liuyi@email.com","education":[{"school":"南京大学","major":"统计学","degree":"硕士","startDate":"2019-09","endDate":"2022-06"}],"work_experience":[{"company":"STU数据","position":"数据分析师","startDate":"2022-07","endDate":"2025-09","description":"构建用户画像体系，为产品优化提供数据支持。"}],"skills":["Python","SQL","Tableau","机器学习","数据挖掘"]}',
                                                                                                                                          'vec_009', 'Python,SQL,Tableau,机器学习,数据挖掘', '2026-06-02 14:00:00'),
                                                                                                                                         (10, '陈一_Java校招.pdf', '/files/resume/chenyi_java.pdf', 180000, 5, 1,
                                                                                                                                          '{"name":"陈一","phone":"13088889999","email":"chenyi@email.com","education":[{"school":"武汉大学","major":"计算机科学","degree":"本科","startDate":"2022-09","endDate":"2026-06"}],"work_experience":[{"company":"ABC科技（实习）","position":"Java开发实习生","startDate":"2025-07","endDate":"2025-12","description":"参与内部工具开发，编写API接口。"}],"skills":["Java","Spring Boot","MySQL","Git"]}',
                                                                                                                                          'vec_010', 'Java,Spring Boot,MySQL,Git', '2026-06-05 16:00:00'),
                                                                                                                                         (11, '林二_产品经理.pdf', '/files/resume/liner_pm.pdf', 200000, 6, 1,
                                                                                                                                          '{"name":"林二","phone":"13199990000","email":"liner@email.com","education":[{"school":"中山大学","major":"软件工程","degree":"硕士","startDate":"2016-09","endDate":"2019-06"}],"work_experience":[{"company":"UVW科技","position":"高级产品经理","startDate":"2019-07","endDate":"2026-01","description":"负责AI产品线，从0到1打造智能客服产品。"}],"skills":["产品规划","AI产品","敏捷开发","数据分析","项目管理"]}',
                                                                                                                                          'vec_011', '产品规划,AI产品,敏捷开发,数据分析,项目管理', '2026-06-06 10:00:00'),
                                                                                                                                         (12, '黄三_测试工程师.pdf', '/files/resume/huangsan_test.pdf', 290000, 7, 1,
                                                                                                                                          '{"name":"黄三","phone":"13200001111","email":"huangsan@email.com","education":[{"school":"同济大学","major":"软件工程","degree":"本科","startDate":"2018-09","endDate":"2022-06"}],"work_experience":[{"company":"XYZ游戏","position":"测试主管","startDate":"2022-07","endDate":"2026-03","description":"管理5人测试团队，建立自动化测试流程。"}],"skills":["测试管理","自动化测试","性能测试","Python","Selenium"]}',
                                                                                                                                          'vec_012', '测试管理,自动化测试,性能测试,Python,Selenium', '2026-06-07 08:00:00');

-- 8. 应聘记录
INSERT INTO application (id, job_id, resume_id, candidate_id, status, result, refuse_type, tags, match_score, apply_time) VALUES
                                                                                                                              (1, 1, 1, NULL, 4, 0, NULL, '3年经验,211', 92.5, '2026-04-21 09:00:00'),
                                                                                                                              (2, 1, 4, 6, 3, 2, 2, '5年经验,高级,金融背景', 98.2, '2026-05-11 10:00:00'),
                                                                                                                              (3, 1, 8, NULL, 2, 0, NULL, '待解析', NULL, '2026-06-01 10:00:00'),
                                                                                                                              (4, 1, 10, 5, 1, 2, 1, '应届生,实习经验', 72.3, '2026-06-05 16:30:00'),
                                                                                                                              (5, 2, 2, 4, 6, 0, NULL, '1年经验,助理', 65.4, '2026-04-23 09:00:00'),
                                                                                                                              (6, 2, 5, 7, 8, 0, NULL, 'MBA,电商,5年', 91.8, '2026-05-13 11:00:00'),
                                                                                                                              (7, 2, 11, 6, 5, 2, 3, '高级,AI产品,7年', 96.5, '2026-06-06 10:30:00'),
                                                                                                                              (8, 3, 3, 5, 9, 2, 5, '2年经验,Vue', 88.7, '2026-05-06 08:00:00'),
                                                                                                                              (9, 3, 6, 8, 7, 2, 4, '解析失败', NULL, '2026-05-16 14:00:00'),
                                                                                                                              (11, 6, 7, 9, 2, 0, NULL, '自动化测试,3年', 85.4, '2026-05-21 10:00:00'),
                                                                                                                              (12, 6, 12, 7, 10, 1, NULL, '测试主管,5年', 93.1, '2026-06-07 08:30:00');

-- 9. 面试数据
INSERT INTO interview (id, application_id, interview_time, location, interviewer_id, interviewer_name, status, evaluation) VALUES
                                                                                                                               (1, 1, '2026-04-25 10:00:00', '会议室301', 2, '张HR', 1, '技术能力较强，项目经验匹配，建议进入下一轮。'),
                                                                                                                               (2, 6, '2026-05-20 14:00:00', '线上会议', 3, '李HR', 1, '逻辑清晰，产品思维良好，可考虑录用。'),
                                                                                                                               (3, 12, '2026-06-09 15:00:00', '会议室401', 2, '张HR', 0, NULL),
                                                                                                                               (4, 8, '2026-05-10 09:00:00', '线上会议', 3, '李HR', 2, '基础不扎实，技术栈匹配度低，不予通过。');

-- 10. 录用数据
INSERT INTO offer (id, application_id, offer_time, expected_join_date, status, docs_submitted, salary, benefits, remark) VALUES
                                                                                                                             (1, 6, '2026-05-15 09:00:00', '2026-06-01', 1, '{"身份证":true,"合同":true,"体检报告":true}', '18K*14薪', '五险一金、年终奖、带薪年假', '金融背景匹配度高，定级高级开发'),
                                                                                                                             (2, 7, '2026-06-08 16:00:00', '2026-06-15', 2, '{"身份证":false,"合同":false,"体检报告":false}', '20K*14薪', '五险一金、项目奖金、带薪年假', 'AI产品经验丰富，定级高级产品经理'),
                                                                                                                             (3, 12, '2026-05-12 10:00:00', '2026-05-20', 1, '{"身份证":true,"合同":true,"体检报告":true}', '15K*13薪', '五险一金、年终奖、带薪年假', '前端技术栈匹配，定级中级开发');

-- 11. 员工档案
INSERT INTO employee (id, offer_id, name, phone, email, department, position, expected_join_date, actual_join_date, docs_status, profile_data, status) VALUES
                                                                                                                                                           (1, 3, '黄三', '13200001111', 'huangsan@email.com', '技术部', '测试主管', '2026-05-20', '2026-05-20', '{"id_card":1,"contract":1,"medical_report":1}', '{"source":"简历ID12","education":"同济大学本科","skills":["测试管理","自动化测试"]}', 1),
                                                                                                                                                           (2, 1, '孙七', '13555556666', 'sunqi@email.com', '产品部', '高级产品经理', '2026-06-01', '2026-06-01', '{"id_card":1,"contract":1,"medical_report":1}', '{"source":"简历ID5","education":"上海交通大学MBA","skills":["产品策略","项目管理"]}', 1);

-- 12. 审计日志
INSERT INTO sys_audit_log (id, user_id, username, operation, method, request_params, ip_address, execute_time, status, create_time) VALUES
                                                                                                                                        (1, 2, 'hr_zhang', '岗位发布', 'JobController.publish', '{"jobId":1}', '192.168.1.100', 120, 1, '2026-04-20 10:00:00'),
                                                                                                                                        (2, 2, 'hr_zhang', '简历上传', 'ResumeController.upload', '{"filename":"张三_Java开发工程师.pdf"}', '192.168.1.100', 350, 1, '2026-04-20 10:05:00'),
                                                                                                                                        (3, 2, 'hr_zhang', '简历解析', 'ResumeController.parse', '{"resumeId":1}', '192.168.1.100', 2800, 1, '2026-04-20 10:06:00'),
                                                                                                                                        (4, 3, 'hr_li', '面试安排', 'InterviewController.arrange', '{"applicationId":1,"time":"2026-04-25 10:00"}', '192.168.1.101', 80, 1, '2026-04-22 14:00:00'),
                                                                                                                                        (5, 2, 'hr_zhang', '录用发送', 'OfferController.send', '{"applicationId":2}', '192.168.1.100', 200, 1, '2026-05-15 09:00:00'),
                                                                                                                                        (6, 3, 'hr_li', '岗位关闭', 'JobController.close', '{"jobId":4}', '192.168.1.101', 50, 1, '2026-05-25 10:00:00'),
                                                                                                                                        (7, 2, 'hr_zhang', '数据导出', 'ReportController.export', '{"type":"monthly"}', '192.168.1.100', 500, 1, '2026-06-08 17:00:00');

-- 13. AI对话历史
INSERT INTO ai_chat_history (id, user_id, question, answer, create_time) VALUES
                                                                             (1, 2, '如何发布一个新岗位？', '请进入岗位管理页面，点击"新增岗位"按钮，填写岗位名称、部门、职责和要求等信息后保存，状态为"草稿"。确认无误后可点击"发布"。', '2026-06-09 09:00:00'),
                                                                             (2, 2, '我需要筛选Java开发的简历', '已在简历管理模块为您打开筛选功能，您可以在搜索栏输入"Java"或选择标签"Java"进行快速筛选。', '2026-06-09 09:01:00'),
                                                                             (3, 3, '面试评价怎么写？', '您可以在面试记录中点击“填写评价”，系统会根据您输入的关键词自动生成规范的评价报告。', '2026-06-09 09:30:00');

-- 14. 事件失败记录
INSERT INTO sys_event_failure (id, event_type, event_body, error_message, retry_count, status) VALUES
    (1, 'offer_notification', '{"offerId":2,"candidateEmail":"liner@email.com"}', '邮件发送失败：Connection timeout', 2, 0);

SET FOREIGN_KEY_CHECKS = 1;