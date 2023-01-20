package com.phoenixone.model;

public class Transaction {
	private Integer id;
	private String accountNumber;
	private String description;
	private float amount;
	
	public Transaction() {}

	public Transaction(Integer id, String accountNumber, String description, float amount) {
		this.id = id;
		this.accountNumber = accountNumber;
		this.description = description;
		this.amount = amount;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
	
}
