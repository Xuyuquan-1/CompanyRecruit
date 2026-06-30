-- --------------------------------------------------------
-- 新增报表展示数据（修正版 - 确保无重复键冲突）
-- 生成时间：2026-06-29
-- 说明：
--   1. 先清理之前可能插入的新增数据（id范围保护）
--   2. 重新插入10个新用户、10份简历、25条申请、15条面试、8条录用、2条员工
--   3. 所有ID均为新增，不修改/删除原有数据
-- --------------------------------------------------------

USE `recruitment_db`;

-- =============================================
-- 0. 清理已插入的新增数据（安全删除，不影响原有数据）
--    删除范围：用户>=13, 简历>=36, 申请>=48, 面试>=29, 录用>=17, 员工>=7
-- =============================================
DELETE FROM `employee` WHERE `id` >= 7;
DELETE FROM `offer` WHERE `id` >= 17;
DELETE FROM `interview` WHERE `id` >= 29;
DELETE FROM `application` WHERE `id` >= 48;
DELETE FROM `resume` WHERE `id` >= 36;
DELETE FROM `sys_user_role` WHERE `user_id` >= 13;
DELETE FROM `sys_user` WHERE `id` >= 13;

-- =============================================
-- 1. 新增10个候选人用户（id: 13~22）
-- =============================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `avatar`, `status`, `dept_id`, `last_login_time`, `create_time`, `update_time`, `deleted`) VALUES
    (13, 'candidate_alpha', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '欧阳明', '13900000210', 'ouyangming@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (14, 'candidate_beta', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '慕容雪', '13900000211', 'murongxue@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (15, 'candidate_gamma', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '令狐冲', '13900000212', 'linghuchong@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (16, 'candidate_delta', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '杨过', '13900000213', 'yangguo@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (17, 'candidate_epsilon', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '小龙女', '13900000214', 'xiaolongnv@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (18, 'candidate_zeta', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '韦小宝', '13900000215', 'weixiaobao@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (19, 'candidate_eta', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '乔峰', '13900000216', 'qiaofeng@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (20, 'candidate_theta', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '段誉', '13900000217', 'duanyu@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (21, 'candidate_iota', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '虚竹', '13900000218', 'xuzhu@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0),
    (22, 'candidate_kappa', '$2a$10$jAsVSXEiWFrfG3CfumkwX.X50aMPEJEy.U.QJxTYpwhiIVIX0ZABu', '黄蓉', '13900000219', 'huangrong@email.com', NULL, 1, NULL, NULL, NOW(), NOW(), 0);

-- =============================================
-- 2. 为新用户分配角色（ROLE_CANDIDATE, role_id=3）
-- =============================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    (13, 3), (14, 3), (15, 3), (16, 3), (17, 3),
    (18, 3), (19, 3), (20, 3), (21, 3), (22, 3);

-- =============================================
-- 3. 新增简历（id: 36~45）
-- =============================================
INSERT INTO `resume` (`id`, `original_filename`, `file_path`, `file_size`, `upload_by`, `parse_status`, `parsed_json`, `vector_id`, `tags`, `upload_time`, `update_time`) VALUES
    (36, '欧阳明_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_36_ouyang.pdf', 512000, 13, 1, '{"name":"欧阳明","email":"ouyangming@email.com","major":"计算机科学","phone":"13900000210","school":"北京大学","skills":"Java,Spring,MySQL","education":"本科","experience":"3年","expectedSalary":"18k","graduationYear":"2020","currentPosition":"Java开发"}', NULL, 'Java,Spring,MySQL', '2026-01-05 09:00:00', '2026-01-05 09:00:00'),
    (37, '慕容雪_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_37_murong.pdf', 480000, 14, 1, '{"name":"慕容雪","email":"murongxue@email.com","major":"统计学","phone":"13900000211","school":"复旦大学","skills":"Python,SQL,Tableau","education":"硕士","experience":"2年","expectedSalary":"15k","graduationYear":"2021","currentPosition":"数据分析"}', NULL, 'Python,SQL,Tableau', '2026-01-10 10:00:00', '2026-01-10 10:00:00'),
    (38, '令狐冲_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_38_linghu.pdf', 560000, 15, 1, '{"name":"令狐冲","email":"linghuchong@email.com","major":"软件工程","phone":"13900000212","school":"浙江大学","skills":"Vue,React,JavaScript","education":"本科","experience":"4年","expectedSalary":"20k","graduationYear":"2019","currentPosition":"前端开发"}', NULL, 'Vue,React,JavaScript', '2026-01-15 11:00:00', '2026-01-15 11:00:00'),
    (39, '杨过_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_39_yang.pdf', 430000, 16, 1, '{"name":"杨过","email":"yangguo@email.com","major":"计算机","phone":"13900000213","school":"南京大学","skills":"Selenium,JMeter,自动化","education":"本科","experience":"3年","expectedSalary":"14k","graduationYear":"2020","currentPosition":"测试工程师"}', NULL, 'Selenium,JMeter,自动化', '2026-01-20 14:00:00', '2026-01-20 14:00:00'),
    (40, '小龙女_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_40_xiao.pdf', 500000, 17, 1, '{"name":"小龙女","email":"xiaolongnv@email.com","major":"市场营销","phone":"13900000214","school":"武汉大学","skills":"营销策划,文案,新媒体","education":"本科","experience":"2年","expectedSalary":"10k","graduationYear":"2021","currentPosition":"市场专员"}', NULL, '营销策划,文案,新媒体', '2026-02-01 09:00:00', '2026-02-01 09:00:00'),
    (41, '韦小宝_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_41_wei.pdf', 620000, 18, 1, '{"name":"韦小宝","email":"weixiaobao@email.com","major":"计算机科学","phone":"13900000215","school":"华中科技大学","skills":"Java,Spring Cloud,Microservices","education":"硕士","experience":"5年","expectedSalary":"22k","graduationYear":"2018","currentPosition":"后端架构师"}', NULL, 'Java,Spring Cloud,Microservices', '2026-02-10 10:00:00', '2026-02-10 10:00:00'),
    (42, '乔峰_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_42_qiao.pdf', 470000, 19, 1, '{"name":"乔峰","email":"qiaofeng@email.com","major":"计算机","phone":"13900000216","school":"西安电子科技大学","skills":"Vue3,React,TypeScript,Node.js","education":"本科","experience":"3年","expectedSalary":"16k","graduationYear":"2020","currentPosition":"前端开发"}', NULL, 'Vue3,React,TypeScript,Node.js', '2026-02-20 15:00:00', '2026-02-20 15:00:00'),
    (43, '段誉_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_43_duan.pdf', 390000, 20, 1, '{"name":"段誉","email":"duanyu@email.com","major":"网络工程","phone":"13900000217","school":"电子科技大学","skills":"Linux,Docker,K8s,CI/CD","education":"本科","experience":"4年","expectedSalary":"18k","graduationYear":"2019","currentPosition":"运维工程师"}', NULL, 'Linux,Docker,K8s,CI/CD', '2026-03-01 09:00:00', '2026-03-01 09:00:00'),
    (44, '虚竹_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_44_xu.pdf', 350000, 21, 1, '{"name":"虚竹","email":"xuzhu@email.com","major":"设计","phone":"13900000218","school":"中央美院","skills":"PS,AI,Sketch,Figma","education":"本科","experience":"2年","expectedSalary":"12k","graduationYear":"2021","currentPosition":"UI设计师"}', NULL, 'PS,AI,Sketch,Figma', '2026-03-10 10:00:00', '2026-03-10 10:00:00'),
    (45, '黄蓉_简历.pdf', 'https://companyrecruit.oss-cn-beijing.aliyuncs.com/resume/report_45_huang.pdf', 440000, 22, 1, '{"name":"黄蓉","email":"huangrong@email.com","major":"工商管理","phone":"13900000219","school":"中山大学","skills":"销售,市场分析,客户管理","education":"本科","experience":"3年","expectedSalary":"11k","graduationYear":"2020","currentPosition":"销售经理"}', NULL, '销售,市场分析,客户管理', '2026-03-15 14:00:00', '2026-03-15 14:00:00');

-- =============================================
-- 4. 新增应聘申请（id: 48~72）
--    确保每个 (job_id, resume_id) 组合唯一
--    每个简历投递的岗位均不同
-- =============================================
INSERT INTO `application` (`id`, `job_id`, `resume_id`, `candidate_id`, `status`, `result`, `refuse_type`, `tags`, `remark`, `match_score`, `apply_time`, `update_time`, `deleted`) VALUES
    -- 欧阳明 (13, resume36) 投递岗位1, 2
    (48, 1, 36, 13, 0, 0, NULL, '官网投递', NULL, 85.00, '2026-01-06 09:30:00', '2026-01-06 09:30:00', 0),
    (57, 2, 36, 13, 5, 0, NULL, '内推', NULL, 72.00, '2026-02-28 16:00:00', '2026-02-28 16:00:00', 0),

    -- 慕容雪 (14, resume37) 投递岗位3, 4, 5
    (49, 3, 37, 14, 1, 0, NULL, '内推', NULL, 78.00, '2026-01-12 10:00:00', '2026-01-12 10:00:00', 0),
    (58, 4, 37, 14, 0, 0, NULL, '招聘网站', NULL, 68.00, '2026-03-05 09:00:00', '2026-03-05 09:00:00', 0),
    (67, 5, 37, 14, 5, 0, NULL, '官网投递', NULL, 70.00, '2026-04-22 11:00:00', '2026-04-22 11:00:00', 0),

    -- 令狐冲 (15, resume38) 投递岗位1, 3, 6
    (50, 1, 38, 15, 2, 0, NULL, '招聘网站', NULL, 82.00, '2026-01-18 11:00:00', '2026-01-18 11:00:00', 0),
    (60, 3, 38, 15, 2, 0, NULL, '内推', NULL, 92.00, '2026-03-15 14:30:00', '2026-03-15 14:30:00', 0),
    (68, 6, 38, 15, 0, 0, NULL, '内推', NULL, 84.00, '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0),

    -- 杨过 (16, resume39) 投递岗位2, 4, 6
    (51, 2, 39, 16, 3, 0, NULL, '校招', NULL, 70.00, '2026-01-22 14:00:00', '2026-01-22 14:00:00', 0),
    (61, 4, 39, 16, 3, 0, NULL, '校招', NULL, 74.00, '2026-03-20 09:00:00', '2026-03-20 09:00:00', 0),
    (69, 6, 39, 16, 4, 2, 3, '招聘网站', '候选人拒绝Offer', 75.00, '2026-05-08 10:30:00', '2026-05-08 10:30:00', 0),

    -- 小龙女 (17, resume40) 投递岗位1, 4, 5
    (52, 1, 40, 17, 1, 0, NULL, '官网投递', NULL, 55.00, '2026-01-28 09:00:00', '2026-01-28 09:00:00', 0),
    (64, 4, 40, 17, 1, 0, NULL, '内推', NULL, 88.00, '2026-04-08 14:00:00', '2026-04-08 14:00:00', 0),
    (70, 5, 40, 17, 5, 0, NULL, '官网投递', NULL, 82.00, '2026-05-12 14:00:00', '2026-05-12 14:00:00', 0),

    -- 韦小宝 (18, resume41) 投递岗位2, 3, 6
    (53, 2, 41, 18, 3, 0, NULL, '内推', NULL, 90.00, '2026-02-05 10:30:00', '2026-02-05 10:30:00', 0),
    (66, 3, 41, 18, 3, 0, NULL, '校招', NULL, 90.00, '2026-04-18 16:00:00', '2026-04-18 16:00:00', 0),
    (71, 6, 41, 18, 6, 1, NULL, '校招', NULL, 95.00, '2026-05-18 09:00:00', '2026-05-18 09:00:00', 0),

    -- 乔峰 (19, resume42) 投递岗位1, 2, 4
    (54, 1, 42, 19, 1, 0, NULL, '招聘网站', NULL, 88.00, '2026-02-12 14:00:00', '2026-02-12 14:00:00', 0),
    (62, 2, 42, 19, 4, 2, 2, '招聘网站', '面试未通过', 79.00, '2026-03-25 11:30:00', '2026-03-25 11:30:00', 0),
    (72, 4, 42, 19, 1, 0, NULL, '官网投递', NULL, 76.00, '2026-05-22 11:30:00', '2026-05-22 11:30:00', 0),

    -- 段誉 (20, resume43) 投递岗位1, 2, 6
    (55, 1, 43, 20, 1, 0, NULL, '官网投递', NULL, 76.00, '2026-02-18 09:30:00', '2026-02-18 09:30:00', 0),
    (63, 2, 43, 20, 0, 0, NULL, '官网投递', NULL, 65.00, '2026-04-02 10:00:00', '2026-04-02 10:00:00', 0),
    (65, 6, 43, 20, 2, 0, NULL, '招聘网站', NULL, 81.00, '2026-04-12 09:30:00', '2026-04-12 09:30:00', 0),

    -- 虚竹 (21, resume44) 投递岗位3, 6
    (56, 3, 44, 21, 2, 0, NULL, '校招', NULL, 80.00, '2026-02-22 11:00:00', '2026-02-22 11:00:00', 0),
    (59, 6, 44, 21, 1, 0, NULL, '官网投递', NULL, 85.00, '2026-03-10 10:00:00', '2026-03-10 10:00:00', 0);

-- =============================================
-- 5. 新增面试（id: 29~43）
--    关联状态>=2的申请
-- =============================================
INSERT INTO `interview` (`id`, `application_id`, `interview_time`, `location`, `interviewer_id`, `interviewer_name`, `status`, `result`, `evaluation`, `create_time`, `update_time`) VALUES
    -- 令狐冲 申请50（岗位1）
    (29, 50, '2026-01-20 10:00:00', '201会议室', 3, '李HR', 1, 1, '前端技术符合要求', '2026-01-19 09:00:00', '2026-01-20 10:00:00'),
    -- 杨过 申请51（岗位2）
    (30, 51, '2026-01-25 14:00:00', '301会议室', 3, '李HR', 1, 1, '沟通能力强', '2026-01-24 09:00:00', '2026-01-25 14:00:00'),
    -- 段誉 申请55（岗位1）
    (31, 55, '2026-02-20 09:30:00', '线上腾讯会议', 3, '李HR', 1, 1, '测试基础扎实', '2026-02-19 09:00:00', '2026-02-20 09:30:00'),
    -- 虚竹 申请56（岗位3）
    (32, 56, '2026-02-24 11:00:00', '201会议室', 3, '李HR', 1, 1, '前端基础良好', '2026-02-23 09:00:00', '2026-02-24 11:00:00'),
    -- 欧阳明 申请57（岗位2）
    (33, 57, '2026-03-01 15:00:00', '301会议室', 3, '李HR', 1, 1, '市场经验丰富', '2026-02-28 09:00:00', '2026-03-01 15:00:00'),
    -- 令狐冲 申请60（岗位3）
    (34, 60, '2026-03-18 10:00:00', '线上腾讯会议', 3, '李HR', 1, 1, 'Java功底深厚', '2026-03-17 09:00:00', '2026-03-18 10:00:00'),
    -- 杨过 申请61（岗位4）
    (35, 61, '2026-03-22 14:00:00', '201会议室', 3, '李HR', 1, 1, '产品能力突出', '2026-03-21 09:00:00', '2026-03-22 14:00:00'),
    -- 乔峰 申请62（岗位2）
    (36, 62, '2026-03-28 09:30:00', '301会议室', 3, '李HR', 1, 2, '项目经验不足', '2026-03-27 09:00:00', '2026-03-28 09:30:00'),
    -- 段誉 申请65（岗位6）
    (37, 65, '2026-04-15 10:00:00', '线上腾讯会议', 3, '李HR', 1, 1, '自动化能力强', '2026-04-14 09:00:00', '2026-04-15 10:00:00'),
    -- 韦小宝 申请66（岗位3）
    (38, 66, '2026-04-20 14:00:00', '201会议室', 3, '李HR', 1, 1, '架构能力强', '2026-04-19 09:00:00', '2026-04-20 14:00:00'),
    -- 慕容雪 申请67（岗位5）
    (39, 67, '2026-04-25 09:00:00', '301会议室', 3, '李HR', 1, 1, '管理能力优秀', '2026-04-24 09:00:00', '2026-04-25 09:00:00'),
    -- 杨过 申请69（岗位6）
    (40, 69, '2026-05-10 10:30:00', '线上腾讯会议', 3, '李HR', 1, 1, '测试流程规范', '2026-05-09 09:00:00', '2026-05-10 10:30:00'),
    -- 小龙女 申请70（岗位5）
    (41, 70, '2026-05-15 14:00:00', '201会议室', 3, '李HR', 1, 1, '活动策划能力强', '2026-05-14 09:00:00', '2026-05-15 14:00:00'),
    -- 韦小宝 申请71（岗位6）
    (42, 71, '2026-05-20 09:30:00', '301会议室', 3, '李HR', 1, 1, '测试经验丰富', '2026-05-19 09:00:00', '2026-05-20 09:30:00'),
    -- 乔峰 申请72（岗位4）
    (43, 72, '2026-05-25 11:00:00', '线上腾讯会议', 3, '李HR', 1, 1, '产品思维清晰', '2026-05-24 09:00:00', '2026-05-25 11:00:00');

-- =============================================
-- 6. 新增录用（id: 17~24）
--    关联状态>=3的申请
-- =============================================
INSERT INTO `offer` (`id`, `application_id`, `offer_time`, `expected_join_date`, `status`, `docs_submitted`, `salary`, `benefits`, `remark`, `create_time`, `update_time`) VALUES
    (17, 51, '2026-01-28 10:00:00', '2026-02-01', 3, NULL, '10k', '五险一金', '杨过-市场专员录用', '2026-01-28 10:00:00', '2026-01-28 10:00:00'),
    (18, 56, '2026-02-26 14:00:00', '2026-03-01', 3, NULL, '16k', '五险一金、年终奖', '虚竹-前端开发录用', '2026-02-26 14:00:00', '2026-02-26 14:00:00'),
    (19, 57, '2026-03-03 09:00:00', '2026-03-15', 3, NULL, '11k', '五险一金', '欧阳明-市场专员录用', '2026-03-03 09:00:00', '2026-03-03 09:00:00'),
    (20, 61, '2026-03-24 14:00:00', '2026-04-05', 3, NULL, '15k', '五险一金、年终奖', '杨过-产品经理录用', '2026-03-24 14:00:00', '2026-03-24 14:00:00'),
    (21, 66, '2026-04-22 09:00:00', '2026-05-01', 3, NULL, '22k', '五险一金、股权', '韦小宝-高级Java录用', '2026-04-22 09:00:00', '2026-04-22 09:00:00'),
    (22, 67, '2026-04-27 11:00:00', '2026-05-10', 3, NULL, '14k', '五险一金、年终奖', '慕容雪-产品经理录用', '2026-04-27 11:00:00', '2026-04-27 11:00:00'),
    (23, 70, '2026-05-17 10:30:00', '2026-06-01', 3, NULL, '13k', '五险一金', '小龙女-市场专员录用', '2026-05-17 10:30:00', '2026-05-17 10:30:00'),
    (24, 71, '2026-05-20 12:00:00', '2026-06-01', 3, NULL, '18k', '五险一金、股票期权', '韦小宝-测试开发录用', '2026-05-20 12:00:00', '2026-05-20 12:00:00');

-- =============================================
-- 7. 新增员工（id: 7~8）
--    对应status=6的申请（已入职）
-- =============================================
INSERT INTO `employee` (`id`, `offer_id`, `name`, `phone`, `email`, `department`, `position`, `expected_join_date`, `actual_join_date`, `join_date`, `id_card_status`, `contract_status`, `medical_report_status`, `profile_data`, `status`, `create_time`, `update_time`) VALUES
    (7, 24, '韦小宝', '13900000215', 'weixiaobao@email.com', '技术部', '测试开发工程师', '2026-06-01', '2026-05-28', '2026-05-28', 1, 1, 1, NULL, 1, '2026-05-28 09:00:00', '2026-05-28 09:00:00'),
    (8, 21, '韦小宝', '13900000215', 'weixiaobao@email.com', '技术部', '高级Java开发工程师', '2026-05-01', '2026-04-28', '2026-04-28', 1, 1, 1, NULL, 1, '2026-04-28 09:00:00', '2026-04-28 09:00:00');