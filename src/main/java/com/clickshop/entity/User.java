package com.clickshop.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

@Entity(name = "users")
public class User {

	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "uno")
	private int id;

	private String name;
	private String uname;
	private String email;
	private String contact;
	private String address;
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	public enum Role {
		SUPER_ADMIN, ADMIN, USER
	};

	public enum Status {
		ACTIVE, INACTIVE
	};

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Wishlist> wishlist = new ArrayList<>();

	// Transient field for isadmin(not stored in the database)
	@Transient
	private boolean isAdmin;

	@OneToMany(mappedBy = "user")
	@JsonIgnore // Prevents serialization of the orders field
	private List<OrderItem> orders;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference(value = "user-cart")
	private List<Cart> cart = new ArrayList<>();

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public User() {

	}

	public User(String name, String email, String uname, String password, String contact, String address) {
		super();
		this.name = name;
		this.uname = uname;
		this.email = email;
		this.password = password;
		this.contact = contact;
		this.address = address;
	}

	public User(int id, String name, String email, String uname, String contact, String address) {
		this.id = id;
		this.name = name;
		this.uname = uname;
		this.email = email;
		this.contact = contact;
		this.address = address;
	}

	public User(int id, String name, String email, String password) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public List<Wishlist> getWishlist() {
		return wishlist;
	}

	public void setWishlist(List<Wishlist> wishlist) {
		this.wishlist = wishlist;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Cart> getCart() {
		return cart;
	}

	public void setCart(List<Cart> cart) {
		this.cart = cart;
	}

	public List<OrderItem> getOrders() {
		return orders;
	}

	public void setOrders(List<OrderItem> orders) {
		this.orders = orders;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", uname=" + uname + ", email=" + email + ", password="
				+ password + ", phone=" + contact + ", address=" + address + ", cart=" + cart + "]";
	}

}
