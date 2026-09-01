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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

// ========== hibernate ==========
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

	// ╔═══════╗
	// ║ Field ║
	// ╚═══════╝
	@Id
	@Column(name = "user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(name = "email", length = 100, nullable = false , unique = true)
	private String email;

	@Column(name = "password", length = 200, nullable = false)
	private String password;

	@Column(name = "name", length = 50)
	private String name;

	@Column(name = "phone", length = 50)
	private String phone;

	@Column(name = "creditcard", length = 50)
	private String creditCard;

	@Column(name = "status", length = 50)
	private String status;

	@Column(name = "gender", length = 50)
	private String gender;

	@Column(name = "picture", length = 255)
	private String picture;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Column(name = "birthday")
	private LocalDate birthday;

	@CreationTimestamp
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// ╔═════════════╗
	// ║ Foreign key ║
	// ╚═════════════╝

	// 一對一 : One:"User" To One:"Cart"
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Cart.class, fetch = FetchType.LAZY)
	private Cart cart;

	// 一對多 : One:"User" To Many:"Order"
	@OneToMany(mappedBy = "user", targetEntity = Order.class, fetch = FetchType.LAZY)
	private List<Order> order = new ArrayList<>();

	// 一對多 : One:"User" To Many:"Outfit"
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, targetEntity = Outfit.class, fetch = FetchType.LAZY)
	private List<Outfit> outfit = new ArrayList<>();

	// 一對多 : One:"User" To Many:"ReturnRequest"
	@OneToMany(mappedBy = "user", targetEntity = ReturnRequest.class, fetch = FetchType.LAZY)
	private List<ReturnRequest> returnRequest = new ArrayList<>();

}