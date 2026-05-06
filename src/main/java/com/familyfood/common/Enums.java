package com.familyfood.common;

public final class Enums {
    private Enums() {
    }

    /**
     * 带中文名称和业务说明的枚举。
     *
     * <p>枚举的系统 code 仍然使用 {@link Enum#name()}，这里不改变 JSON 序列化和数据库存储值。</p>
     */
    public interface DescribedEnum {
        String label();

        String description();
    }

    /**
     * 家庭成员权限角色，用于登录身份、接口鉴权和家庭内权限判断。
     */
    public enum Role implements DescribedEnum {
        ADMIN("家庭管理员", "可管理家庭成员、菜品、系统配置、订单确认和 AI 草稿审核。"),
        MEMBER("普通成员", "可点餐、维护自己的订单、提交意向和查看家庭内可见数据。");

        private final String label;
        private final String description;

        Role(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 通用启停状态，用于家庭、用户、成员等基础数据。
     */
    public enum Status implements DescribedEnum {
        ACTIVE("启用", "记录处于可用状态，可参与正常业务流程。"),
        DISABLED("禁用", "记录已停用，通常不可登录、不可选择或不参与业务流程。");

        private final String label;
        private final String description;

        Status(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 菜品状态，用于菜品目录维护、点餐可见性和 AI 草稿转正式菜品。
     */
    public enum DishStatus implements DescribedEnum {
        ACTIVE("上架", "菜品已上架，普通成员可在点餐时选择。"),
        INACTIVE("下架", "菜品已下架，保留历史数据但普通点餐不再展示。"),
        DRAFT("草稿", "菜品仍在编辑或审核中，尚未进入正式可点餐列表。");

        private final String label;
        private final String description;

        DishStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 餐次类型，用于餐次创建、菜品适用餐别、推荐和菜单计划。
     */
    public enum MealType implements DescribedEnum {
        BREAKFAST("早餐", "早餐餐次或适合早餐供应的菜品。"),
        LUNCH("午餐", "午餐餐次或适合午餐供应的菜品。"),
        DINNER("晚餐", "晚餐餐次或适合晚餐供应的菜品。"),
        SNACK("加餐/夜宵", "非正餐场景，如下午加餐、夜宵或临时小食。"),
        CUSTOM("自定义", "无法归入固定餐别的自定义餐次。");

        private final String label;
        private final String description;

        MealType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 餐次状态，用于控制一个家庭餐次是否还能下单、汇总和结算。
     */
    public enum MealStatus implements DescribedEnum {
        OPEN("可点餐", "餐次开放中，成员可以新增或提交订单。"),
        LOCKED("已锁定", "餐次已停止普通成员新增订单，通常进入确认、汇总或备餐阶段。"),
        COMPLETED("已完成", "餐次已经结束，订单和采购流程已完成。"),
        CANCELLED("已取消", "餐次被取消，相关未完成订单不再继续处理。");

        private final String label;
        private final String description;

        MealStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 个人订单状态。
     *
     * <p>需要确认时：DRAFT -> SUBMITTED -> PENDING_CONFIRM -> CONFIRMED -> COOKING -> COMPLETED。
     * 自动确认时：DRAFT -> SUBMITTED -> CONFIRMED -> COOKING -> COMPLETED。
     * 未完成订单可按权限进入 CANCELLED。</p>
     */
    public enum OrderStatus implements DescribedEnum {
        DRAFT("草稿", "订单正在编辑，尚未提交到餐次。"),
        SUBMITTED("已提交", "订单已提交，系统将按餐次确认策略进入待确认或已确认。"),
        PENDING_CONFIRM("待确认", "餐次需要管理员确认，订单正在等待管理员处理。"),
        CONFIRMED("已确认", "订单已确认，可进入汇总、采购或制作流程。"),
        COOKING("制作中", "订单中的菜品正在制作或备餐。"),
        COMPLETED("已完成", "订单已履约完成，进入历史记录。"),
        CANCELLED("已取消", "订单已取消，不再参与后续制作和采购统计。");

        private final String label;
        private final String description;

        OrderStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * AI 输入来源类型，用于链接解析、素材识别、任务筛选和来源统计。
     */
    public enum SourceType implements DescribedEnum {
        TEXT("纯文字", "用户直接输入的文本、菜谱描述或推荐要求。"),
        WEB("普通网页", "非特定平台的公开网页链接。"),
        DOUYIN("抖音", "抖音视频、图文或短链接来源。"),
        XIAOHONGSHU("小红书", "小红书笔记、图文或短链接来源。"),
        KUAISHOU("快手", "快手视频、图文或短链接来源。"),
        IMAGE("图片/截图", "用户上传或提供的图片、菜单截图、菜谱截图。"),
        UNKNOWN("未识别", "系统无法识别或暂不支持的平台来源。");

        private final String label;
        private final String description;

        SourceType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * AI 异步任务状态。
     *
     * <p>链接解析通常从 PENDING 开始，依次经过解析链接、获取内容、提取文本、
     * AI 抽取、匹配菜品，最终进入 SUCCESS、REVIEW_REQUIRED 或 FAILED。
     * 推荐类任务可直接进入 RECOMMENDING 后完成。</p>
     */
    public enum AiTaskStatus implements DescribedEnum {
        PENDING("待处理", "任务已创建，等待异步处理器执行。"),
        RESOLVING_URL("解析短链", "正在解析短链接、跳转链接或最终内容地址。"),
        FETCHING_CONTENT("获取内容", "正在抓取公开网页、平台内容或用户提供的素材内容。"),
        EXTRACTING_TEXT("提取文本", "正在从标题、正文、字幕或图片 OCR 中提取可分析文本。"),
        AI_EXTRACTING("AI 抽取", "正在调用 AI 将素材结构化为菜品草稿或候选信息。"),
        MATCHING_DISH("匹配菜品", "正在把 AI 抽取结果和已有菜品库做相似匹配。"),
        RECOMMENDING("生成推荐", "正在根据提示词、餐次和偏好生成推荐结果。"),
        REVIEW_REQUIRED("需人工处理", "自动处理结果不足，需要用户补充信息或管理员审核。"),
        SUCCESS("成功", "任务处理成功，结果已写入草稿、推荐或其他业务数据。"),
        FAILED("失败", "任务处理失败，可根据错误码和错误信息决定是否重试。"),
        CANCELLED("已取消", "任务已被用户或系统取消，不再继续执行。");

        private final String label;
        private final String description;

        AiTaskStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * AI 任务类型，用于区分异步任务的业务目的和处理器入口。
     */
    public enum AiTaskType implements DescribedEnum {
        PARSE_LINK("链接/素材解析", "解析链接、文本或图片，抽取菜品草稿。"),
        RECOMMEND("菜品推荐", "根据用户提示、餐次和已有菜品生成推荐。"),
        MENU_PLAN("菜单计划", "根据人数、餐次和偏好生成一组菜单建议。"),
        SHOPPING_SUGGEST("采购建议", "根据菜单、订单或食材信息生成采购建议。");

        private final String label;
        private final String description;

        AiTaskType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 定时任务启停状态，用于系统任务配置和 Quartz 调度控制。
     */
    public enum ScheduledTaskStatus implements DescribedEnum {
        ENABLED("启用", "定时任务配置有效，会被注册到调度器执行。"),
        DISABLED("停用", "定时任务配置保留但不注册或不继续执行。");

        private final String label;
        private final String description;

        ScheduledTaskStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 定时任务单次运行状态，用于记录任务执行日志。
     */
    public enum ScheduledRunStatus implements DescribedEnum {
        RUNNING("运行中", "任务本次执行已经开始，尚未结束。"),
        SUCCESS("成功", "任务本次执行成功完成。"),
        FAILED("失败", "任务本次执行失败，需查看错误信息或日志。");

        private final String label;
        private final String description;

        ScheduledRunStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }

    /**
     * 定时任务调度模式。
     *
     * <p>ONCE 表示只执行一次；INTERVAL 表示按固定间隔重复；
     * DAILY、WEEKLY、MONTHLY 表示按自然日、周、月计划执行；
     * CRON 表示直接使用 Cron 表达式生成调度计划。</p>
     */
    public enum ScheduleMode implements DescribedEnum {
        ONCE("单次", "按指定时间只执行一次。"),
        INTERVAL("间隔", "按固定分钟、小时等间隔重复执行。"),
        DAILY("每天", "每天在指定时间执行。"),
        WEEKLY("每周", "每周在指定星期和时间执行。"),
        MONTHLY("每月", "每月在指定日期和时间执行。"),
        CRON("Cron 表达式", "使用 Cron 表达式定义更复杂的执行计划。");

        private final String label;
        private final String description;

        ScheduleMode(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public String description() {
            return description;
        }
    }
}
