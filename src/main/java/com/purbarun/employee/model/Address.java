package com.purbarun.employee.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

@Entity(name = "addresses")
public class Address {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Street address is required")
    @Column(name = "street")
    private String street;
    
    @Column(name = "suite")
    private String suite;
    
    @NotBlank(message = "City is required")
    @Column(name = "city")
    private String city;
    
    @NotBlank(message = "State is required")
    @Column(name = "state")
    private String state;
    
    @NotBlank(message = "Zip code is required")
    @Column(name = "zip_code")
    private String zipCode;
    
    @Column(name = "country")
    private String country;
    
    @NotBlank(message = "Address type is required")
    @Column(name = "address_type")
    private String addressType; // HOME, WORK, SHIPPING, BILLING
    
    @Column(name = "is_primary")
    private Boolean isPrimary;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
	public Address() {
		// Default constructor for JPA
	}

	public Address(Long id, @NotBlank(message = "Street address is required") String street, String suite,
			@NotBlank(message = "City is required") String city, @NotBlank(message = "State is required") String state,
			@NotBlank(message = "Zip code is required") String zipCode, String country,
			@NotBlank(message = "Address type is required") String addressType, Boolean isPrimary,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.street = street;
		this.suite = suite;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.country = country;
		this.addressType = addressType;
		this.isPrimary = isPrimary;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getSuite() {
		return suite;
	}

	public void setSuite(String suite) {
		this.suite = suite;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public Boolean getIsPrimary() {
		return isPrimary;
	}

	public void setIsPrimary(Boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public java.time.LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(java.time.LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public java.time.LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
    
}
