package com.example.demo.model;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// ========== Jakarta Persistence（JPA） ==========
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.math.BigDecimal;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_items")
public class ReturnItem {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "return_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnItemId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "description", length = 255)
    private String description;

    // 退換貨商品照片（base64 data URL，使用 LONGTEXT 儲存較長內容）
    @Column(name = "picture", columnDefinition = "LONGTEXT")
    private String picture;

    @Column(name = "approval_quantity", nullable = false)
    private Integer approvalQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity;

    @Column(name = "refund", precision = 10, scale = 2, nullable = false)
    private BigDecimal refund;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 多對一 : Many="ReturnItem" To One="OrderItem"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @JsonIgnoreProperties("returnItem")
    private OrderItem orderItem;

    @OneToOne(targetEntity = ReturnRequest.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "return_requests_id", nullable = false, unique = true)
    @JsonIgnoreProperties("returnItem")
    private ReturnRequest returnRequest;

    public Stream<ReturnRequest> stream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stream'");
    }
}
