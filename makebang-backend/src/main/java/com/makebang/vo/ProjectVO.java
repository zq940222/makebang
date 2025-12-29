package com.makebang.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.makebang.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private Integer categoryId;

    private String categoryName;

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    /**
     * 预算显示文本
     */
    private String budgetText;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    private List<String> skillRequirements;

    private List<String> attachmentUrls;

    private Integer status;

    private String statusText;

    private Integer viewCount;

    private Integer bidCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 发布者信息
     */
    private UserVO user;

    /**
     * 格式化预算文本
     */
    public String getBudgetText() {
        if (budgetMin == null && budgetMax == null) {
            return "面议";
        }
        if (budgetMin != null && budgetMax != null) {
            return String.format("¥%s - %s", formatMoney(budgetMin), formatMoney(budgetMax));
        }
        if (budgetMin != null) {
            return String.format("¥%s起", formatMoney(budgetMin));
        }
        return String.format("不超过¥%s", formatMoney(budgetMax));
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return Project.Status.getDesc(status != null ? status : 0);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            return amount.divide(new BigDecimal("10000")).stripTrailingZeros().toPlainString() + "万";
        }
        return amount.stripTrailingZeros().toPlainString();
    }
}
