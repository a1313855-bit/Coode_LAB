package com.example.demo.model;
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
	@JsonManagedReference("user-orders")
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



}