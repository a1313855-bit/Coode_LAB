package com.example.demo.model;
<<<<<<< HEAD
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@Setter
@Entity
@Table(name="users")
@NoArgsConstructor
@AllArgsConstructor
public class User{
	
	@Id
	@Column(name="user_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer userId;

	@Column(nullable=true , length = 100)
	private String email;

	@Column(nullable=true , length = 200)
	private String password;

	@Column(nullable = true , length = 45)
	private String name;

	@Column(nullable = true , length = 45)
	private String phone;

	@Column(name="creditcard" ,nullable = true , length = 45)
	private String creditCard;

	@Column(nullable = true , length = 45)
	private String status;

	@Column(nullable = true , length = 45)
	private String gender;

	@Column(nullable = true , length = 45)
	private String picture;

	@Column
	private LocalDate birthday;

	@CreationTimestamp
	@Column(updatable=false,name="created_at")
	private LocalDateTime createdAt;

	@Column(name="updated_at")
	private LocalDateTime updatedAt;

	/*
	*=================================
	*User 1 : 1 Cart
	*=================================
	*/

	@OneToOne(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true,
		targetEntity = Cart.class)
	@JsonManagedReference("user-cart")
	private Cart cart;

	/*
	*=================================
	*User 1 : N Order
	*=================================
	*/
	@OneToMany(mappedBy = "user",
			fetch = FetchType.LAZY,
			targetEntity = Order.class
	)
	//@JsonManagedReference("user-orders")
	private List<Order> order = new ArrayList<>();

	/*
	*=================================
	*User 1 : N Outfit
	*=================================
	*/
	@OneToMany(mappedBy = "user")
	private List<Outfit> outfit = new ArrayList<>();

	/*
	*=================================
	*User 1 : N ReturnRequest
	*=================================
	*/
	@OneToMany(mappedBy = "user")
	private List<ReturnRequest> returnRequests = new ArrayList<>();


=======

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

	@Column(name = "email", length = 100, nullable = false)
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
>>>>>>> Maple

}