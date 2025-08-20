-- 插入10个材料示例数据
INSERT INTO material (material_detail, review_point, auto_approval_criteria, is_shared, material_source, processing_method_and_info_access, is_eligible_for_promise, is_valid) VALUES
                                                                                                                                                                                   ('身份证复印件', '检查身份证信息是否清晰完整', '身份证信息完整且清晰', true, 'PERSONAL_SUBMISSION', '在线提交', true, true),
                                                                                                                                                                                   ('户口本复印件', '检查户口本信息是否完整', '户口本信息完整', false, 'SYSTEM_AUTO_SHARED', '系统自动获取', false, true),
                                                                                                                                                                                   ('营业执照副本', '检查营业执照是否在有效期内', '营业执照在有效期内且信息完整', true, 'PERSONAL_SUBMISSION', '在线提交', true, true),
                                                                                                                                                                                   ('学历证明', '检查学历证书真伪', '学历证书通过验证', true, 'PERSONAL_SUBMISSION', '在线提交', false, true),
                                                                                                                                                                                   ('工作经历证明', '检查工作经历证明真实性', '工作经历证明完整', false, 'SYSTEM_AUTO_SHARED', '系统自动获取', false, true),
                                                                                                                                                                                   ('无犯罪记录证明', '检查证明是否有效', '无犯罪记录证明有效', true, 'PERSONAL_SUBMISSION', '在线提交', true, true),
                                                                                                                                                                                   ('健康证明', '检查健康证明是否在有效期内', '健康证明在有效期内', true, 'PERSONAL_SUBMISSION', '在线提交', false, true),
                                                                                                                                                                                   ('社保证明', '检查社保缴纳记录', '社保缴纳记录完整', false, 'SYSTEM_AUTO_SHARED', '系统自动获取', true, true),
                                                                                                                                                                                   ('银行流水', '检查银行流水真实性', '银行流水完整且真实', true, 'PERSONAL_SUBMISSION', '在线提交', false, true),
                                                                                                                                                                                   ('居住证明', '检查居住地址真实性', '居住证明有效', true, 'PERSONAL_SUBMISSION', '在线提交', true, true);

-- 插入5个事项示例数据
INSERT INTO matter (main_item_name, sub_item_name, grandchild_item_name, legal_time_limit, committed_time_limit, approval_level, provincial_department_office, is_valid) VALUES
                                                                                                                                                                             ('交通行政许可', '道路运输经营许可', '客运经营许可', 20, 10, 'PROVINCIAL_MUNICIPAL', 'PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL', true),
                                                                                                                                                                             ('交通行政许可', '道路运输经营许可', '货运经营许可', 15, 7, 'MUNICIPAL', 'PROVINCIAL_PORT_CENTER_CONSTRUCTION', true),
                                                                                                                                                                             ('交通行政许可', '道路运输经营许可', '危险品运输经营许可', 25, 15, 'PROVINCIAL_MUNICIPAL_COUNTY', 'PROVINCIAL_DEPARTMENT_TRANSPORTATION_MANAGEMENT', true),
                                                                                                                                                                             ('建设工程许可', '建筑施工许可', '房屋建筑工程', 30, 20, 'PROVINCIAL_MUNICIPAL', 'PROVINCIAL_HIGHWAY_CENTER_CONSTRUCTION', true),
                                                                                                                                                                             ('环保行政许可', '排污许可', '水污染物排放许可', 20, 10, 'MUNICIPAL_COUNTY', 'PROVINCIAL_DEPARTMENT_POLICY_REGULATIONS', true);

-- 插入事项的经办依据数据
INSERT INTO matter_basis (matter_id, basis) VALUES
                                                (1, '《中华人民共和国道路运输条例》'),
                                                (1, '《道路旅客运输及客运站管理规定》'),
                                                (2, '《中华人民共和国道路运输条例》'),
                                                (2, '《道路货物运输及站场管理规定》'),
                                                (3, '《中华人民共和国道路运输条例》'),
                                                (3, '《道路危险货物运输管理规定》'),
                                                (4, '《中华人民共和国建筑法》'),
                                                (4, '《建筑工程施工许可管理办法》'),
                                                (5, '《中华人民共和国环境保护法》'),
                                                (5, '《排污许可管理条例》');

-- 插入事项与材料的关联数据
INSERT INTO matter_materials (matter_id, material_id) VALUES
                                                          (1, 1),  -- 客运经营许可需要身份证复印件
                                                          (1, 3),  -- 客运经营许可需要营业执照副本
                                                          (1, 6),  -- 客运经营许可需要无犯罪记录证明
                                                          (2, 1),  -- 货运经营许可需要身份证复印件
                                                          (2, 3),  -- 货运经营许可需要营业执照副本
                                                          (2, 9),  -- 货运经营许可需要银行流水
                                                          (3, 1),  -- 危险品运输经营许可需要身份证复印件
                                                          (3, 3),  -- 危险品运输经营许可需要营业执照副本
                                                          (3, 6),  -- 危险品运输经营许可需要无犯罪记录证明
                                                          (3, 7),  -- 危险品运输经营许可需要健康证明
                                                          (4, 1),  -- 房屋建筑工程需要身份证复印件
                                                          (4, 3),  -- 房屋建筑工程需要营业执照副本
                                                          (4, 4),  -- 房屋建筑工程需要学历证明
                                                          (4, 10), -- 房屋建筑工程需要居住证明
                                                          (5, 1),  -- 水污染物排放许可需要身份证复印件
                                                          (5, 3),  -- 水污染物排放许可需要营业执照副本
                                                          (5, 8);  -- 水污染物排放许可需要社保证明
