package com.familyfood.shopping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "采购条目保存请求")
public record ItemRequest(
        @Schema(description = "采购项名称", example = "鸡蛋")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "采购数量", example = "6")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,
        @Schema(description = "单位", example = "个")
        @Size(max = 30)
        String unit,
        @Schema(description = "分类", example = "蛋奶")
        @Size(max = 60)
        String category,
        @Schema(description = "备注", example = "买新鲜日期")
        @Size(max = 500)
        String note
) {
}
