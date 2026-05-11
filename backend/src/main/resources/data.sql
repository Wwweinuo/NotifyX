-- 初始化节日数据
INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('元旦', 'FIXED_SOLAR', '01-01', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('情人节', 'FIXED_SOLAR', '02-14', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('妇女节', 'FIXED_SOLAR', '03-08', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('劳动节', 'FIXED_SOLAR', '05-01', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('儿童节', 'FIXED_SOLAR', '06-01', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('国庆节', 'FIXED_SOLAR', '10-01', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('圣诞节', 'FIXED_SOLAR', '12-25', NULL, NULL, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 农历节日
INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('春节', 'FIXED_LUNAR', NULL, 1, 1, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('元宵节', 'FIXED_LUNAR', NULL, 1, 15, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('端午节', 'FIXED_LUNAR', NULL, 5, 5, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('七夕节', 'FIXED_LUNAR', NULL, 7, 7, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('中秋节', 'FIXED_LUNAR', NULL, 8, 15, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('重阳节', 'FIXED_LUNAR', NULL, 9, 9, NULL, NULL, NULL, NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 动态规则节日
INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('母亲节', 'DYNAMIC_RULE', NULL, NULL, NULL, '5月第2个星期日', 5, 2, 7, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('父亲节', 'DYNAMIC_RULE', NULL, NULL, NULL, '6月第3个星期日', 6, 3, 7, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO holidays (name, type, fixed_date, lunar_month, lunar_day, dynamic_rule, dynamic_month, dynamic_week_ordinal, dynamic_day_of_week, enabled, created_at, updated_at)
VALUES ('感恩节', 'DYNAMIC_RULE', NULL, NULL, NULL, '11月第4个星期四', 11, 4, 4, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 初始化定时任务配置
INSERT INTO scheduler_config (task_name, cron_expression, description, enabled, updated_at)
VALUES ('DAILY_REMINDER', '0 0 8 * * ?', '每日提醒检查任务', true, NOW())
ON CONFLICT (task_name) DO NOTHING;
