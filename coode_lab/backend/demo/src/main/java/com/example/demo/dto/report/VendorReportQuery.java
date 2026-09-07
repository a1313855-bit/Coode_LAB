package com.example.demo.dto.report;

// ========== Lombok ==========
import lombok.Getter;
import lombok.Setter;

// ========== Jakarta Validation ==========
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

// ========== Spring ==========
import org.springframework.format.annotation.DateTimeFormat;

// ========== Java ==========
import java.time.LocalDate;

@Getter
@Setter
public class VendorReportQuery {

    // ╔══════════════════════════════════════╗
    // ║ Mode 1 : 預設制（粒度 + 參考日期）  ║
    // ╚══════════════════════════════════════╝

    @Pattern(regexp = "DAY|WEEK|MONTH|QUARTER|YEAR",
             message = "period 只能是 DAY, WEEK, MONTH, QUARTER, YEAR")
    private String period;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    // ╔══════════════════════════════════════╗
    // ║ Mode 2 : 自訂制（明確起訖）        ║
    // ╚══════════════════════════════════════╝

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate start;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

    // ╔══════════════════════════════════════╗
    // ║ 共用                                ║
    // ╚══════════════════════════════════════╝

    @Min(1)
    @Max(10)
    private Integer limit = 5;

    // 只能選一種模式；自訂制必須同時給 start 與 end 且 start <= end
    @AssertTrue(message = "參數組合不合法：請使用 period+date，或同時提供 start、end（且 start <= end）")
    public boolean isModeValid() {
        boolean preset = period != null || date != null;
        boolean custom = start != null || end != null;

        if (preset && custom) {
            return false;
        }
        if (custom) {
            return start != null && end != null && !start.isAfter(end);
        }
        return true;
    }
}