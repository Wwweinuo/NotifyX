-- ============================================================
-- NotifyX 生日/节日邮件提醒系统 - 数据库初始化脚本（PostgreSQL）
-- 
-- 使用方法：
--   1. 创建数据库：CREATE DATABASE notifyx;
--   2. 连接数据库后执行本脚本建表
--   3. 再执行 data.sql 插入初始数据
--
-- 说明：
--   项目中 JPA ddl-auto:update 会自动建表，
--   因此正常启动项目时无需手动执行此脚本。
--   本脚本仅供手动建库或数据库迁移时参考使用。
-- ============================================================

-- ===========================================
-- 1. 联系人表（contacts）
-- 存储需要生日提醒的联系人信息
-- ===========================================
CREATE TABLE IF NOT EXISTS contacts (
    id              BIGSERIAL    PRIMARY KEY,                    -- 主键，自增
    name            VARCHAR(50)  NOT NULL,                       -- 联系人姓名
    relationship    VARCHAR(30),                                 -- 与用户关系（父母/朋友/同学/同事/恋人/亲戚/其他）
    birthday_type   VARCHAR(10)  NOT NULL,                       -- 生日类型：SOLAR（公历）/ LUNAR（农历）
    birthday_date   VARCHAR(10),                                 -- 公历生日日期，格式 MM-DD（仅公历类型使用）
    lunar_month     INTEGER,                                     -- 农历月份 1-12（仅农历类型使用）
    lunar_day       INTEGER,                                     -- 农历日 1-30（仅农历类型使用）
    is_leap_month   BOOLEAN      DEFAULT false,                  -- 是否闰月（仅农历类型使用）
    enabled         BOOLEAN      DEFAULT true,                   -- 是否启用提醒
    notify_email    VARCHAR(100),                                -- 提醒接收邮箱（为空则使用系统默认邮箱）
    remark          VARCHAR(200),                                -- 备注
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,      -- 创建时间
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP       -- 更新时间
);

COMMENT ON TABLE contacts IS '联系人表 - 存储需要生日提醒的联系人信息';
COMMENT ON COLUMN contacts.birthday_type IS '生日类型：SOLAR=公历，LUNAR=农历';
COMMENT ON COLUMN contacts.birthday_date IS '公历生日日期，格式MM-DD，仅birthday_type=SOLAR时使用';
COMMENT ON COLUMN contacts.lunar_month IS '农历月份(1-12)，仅birthday_type=LUNAR时使用';
COMMENT ON COLUMN contacts.lunar_day IS '农历日(1-30)，仅birthday_type=LUNAR时使用';
COMMENT ON COLUMN contacts.is_leap_month IS '是否闰月，仅农历类型时有效';
COMMENT ON COLUMN contacts.notify_email IS '提醒接收邮箱，为空时使用email_config表中的默认邮箱';

-- ===========================================
-- 2. 节假日表（holidays）
-- 存储系统内置及用户自定义的节日信息
-- 支持三种类型：固定公历、固定农历、动态规则
-- ===========================================
CREATE TABLE IF NOT EXISTS holidays (
    id                   BIGSERIAL    PRIMARY KEY,               -- 主键，自增
    name                 VARCHAR(50)  NOT NULL UNIQUE,            -- 节日名称（唯一约束）
    type                 VARCHAR(20)  NOT NULL,                   -- 节日类型：FIXED_SOLAR / FIXED_LUNAR / DYNAMIC_RULE
    fixed_date           VARCHAR(10),                             -- 固定公历日期，格式 MM-DD（仅 FIXED_SOLAR 类型）
    lunar_month          INTEGER,                                 -- 农历月份 1-12（仅 FIXED_LUNAR 类型）
    lunar_day            INTEGER,                                 -- 农历日 1-30（仅 FIXED_LUNAR 类型）
    dynamic_rule         VARCHAR(100),                            -- 动态规则描述，如"5月第2个星期日"（仅 DYNAMIC_RULE 类型）
    dynamic_month        INTEGER,                                 -- 动态规则：月份（仅 DYNAMIC_RULE 类型）
    dynamic_week_ordinal INTEGER,                                 -- 动态规则：第几个星期（仅 DYNAMIC_RULE 类型）
    dynamic_day_of_week  INTEGER,                                 -- 动态规则：星期几 1=周一..7=周日（仅 DYNAMIC_RULE 类型）
    enabled              BOOLEAN      DEFAULT true,               -- 是否启用提醒
    notify_email         VARCHAR(100),                            -- 提醒接收邮箱（为空则使用系统默认邮箱）
    created_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    updated_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP   -- 更新时间
);

COMMENT ON TABLE holidays IS '节假日表 - 存储系统内置及用户自定义的节日信息';
COMMENT ON COLUMN holidays.type IS '节日类型：FIXED_SOLAR=固定公历日期，FIXED_LUNAR=固定农历日期，DYNAMIC_RULE=动态规则计算';
COMMENT ON COLUMN holidays.dynamic_rule IS '动态规则的中文描述，如：5月第2个星期日（母亲节）';
COMMENT ON COLUMN holidays.dynamic_day_of_week IS '星期几：1=周一, 2=周二, ..., 7=周日';

-- ===========================================
-- 3. 提醒记录表（reminder_records）
-- 记录每次提醒的发送结果，用于追踪和防重复
-- ===========================================
CREATE TABLE IF NOT EXISTS reminder_records (
    id              BIGSERIAL    PRIMARY KEY,                    -- 主键，自增
    reminder_type   VARCHAR(20)  NOT NULL,                       -- 提醒类型：BIRTHDAY（生日）/ HOLIDAY（节日）
    target_name     VARCHAR(50)  NOT NULL,                       -- 提醒对象名称（联系人姓名或节日名称）
    target_date     DATE         NOT NULL,                       -- 目标日期（生日/节日的实际日期）
    email_to        VARCHAR(100) NOT NULL,                       -- 接收邮箱
    email_subject   VARCHAR(200),                                -- 邮件主题
    email_content   TEXT,                                        -- 邮件正文内容
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',     -- 发送状态：SUCCESS / FAILED / PENDING
    failure_reason  VARCHAR(500),                                -- 失败原因（仅状态为FAILED时记录）
    retry_count     INTEGER      DEFAULT 0,                      -- 重试次数（最多3次）
    sent_at         TIMESTAMP,                                   -- 实际发送时间
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP       -- 记录创建时间
);

COMMENT ON TABLE reminder_records IS '提醒记录表 - 记录每次提醒邮件的发送结果';
COMMENT ON COLUMN reminder_records.reminder_type IS '提醒类型：BIRTHDAY=生日提醒，HOLIDAY=节日提醒';
COMMENT ON COLUMN reminder_records.status IS '发送状态：PENDING=待发送，SUCCESS=发送成功，FAILED=发送失败';
COMMENT ON COLUMN reminder_records.retry_count IS '重试次数，最大3次，超过则标记为FAILED';

-- ===========================================
-- 4. 邮件配置表（email_config）
-- 存储 SMTP 邮件服务器配置，系统仅维护一条记录
-- ===========================================
CREATE TABLE IF NOT EXISTS email_config (
    id               BIGSERIAL    PRIMARY KEY,                   -- 主键，自增
    smtp_host        VARCHAR(100) NOT NULL,                      -- SMTP 服务器地址（如 smtp.qq.com）
    smtp_port        INTEGER      NOT NULL DEFAULT 587,          -- SMTP 端口（587=STARTTLS, 465=SSL）
    username         VARCHAR(100) NOT NULL,                      -- 发件人邮箱账号
    password         VARCHAR(200) NOT NULL,                      -- 邮箱授权码（非登录密码）
    from_name        VARCHAR(50),                                -- 发件人显示名称
    default_to_email VARCHAR(100),                               -- 默认接收邮箱（联系人/节日未指定时使用）
    enabled          BOOLEAN      DEFAULT true                   -- 是否启用邮件发送
);

COMMENT ON TABLE email_config IS '邮件配置表 - 存储SMTP邮件服务器配置，系统仅维护一条记录';
COMMENT ON COLUMN email_config.password IS '邮箱授权码，非登录密码。QQ邮箱需在设置中开启SMTP并获取授权码';
COMMENT ON COLUMN email_config.default_to_email IS '默认接收邮箱，当联系人或节日未单独配置接收邮箱时使用此地址';

-- ===========================================
-- 5. 定时任务配置表（scheduler_config）
-- 存储定时任务的调度配置，支持运行时动态修改 Cron 表达式
-- ===========================================
CREATE TABLE IF NOT EXISTS scheduler_config (
    id                  BIGSERIAL    PRIMARY KEY,                -- 主键，自增
    task_name           VARCHAR(50)  NOT NULL UNIQUE,             -- 任务名称（唯一标识，如 DAILY_REMINDER）
    cron_expression     VARCHAR(50)  NOT NULL,                    -- Cron 表达式（如 0 0 8 * * ? 表示每天8:00）
    description         VARCHAR(200),                             -- 任务描述
    enabled             BOOLEAN      DEFAULT true,                -- 是否启用该定时任务
    last_execution_time TIMESTAMP,                                -- 上次执行时间
    next_execution_time TIMESTAMP,                                -- 下次预计执行时间
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP    -- 配置更新时间
);

COMMENT ON TABLE scheduler_config IS '定时任务配置表 - 支持通过前端动态修改Cron表达式，修改后无需重启自动生效';
COMMENT ON COLUMN scheduler_config.task_name IS '任务唯一标识名称，如DAILY_REMINDER';
COMMENT ON COLUMN scheduler_config.cron_expression IS 'Spring Cron表达式，6位格式：秒 分 时 日 月 周';
COMMENT ON COLUMN scheduler_config.enabled IS '是否启用，禁用后定时任务将暂停执行';




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
