package com.phoenixone.controller;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.phoenixone.model.Account;
import com.phoenixone.model.Transaction;

@RestController
@RequestMapping("phoenixone/api/v1/account")
public class AccountController {
	
	@Autowired JdbcTemplate jdbcTemplate;

	private static final long LIMIT = 100000000L;
	private static long last = 0;
	
	@GetMapping("/")
	public List<Account> getAccounts() {
		String sql = "SELECT * FROM ACCOUNTS";
		return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Account.class));
	}
	
	@GetMapping("/{customerNumber}")
	public ResponseEntity<?> getAccount(@PathVariable Long customerNumber) {
		System.out.println("Customer Number---"+customerNumber);
		
		//try to get account
		Account myAccount = null;
		List<Transaction> myTransactions = null;
		try {
			myAccount = jdbcTemplate.queryForObject("SELECT * FROM ACCOUNTS WHERE CUSTOMERNUMBER = ?",
					BeanPropertyRowMapper.newInstance(Account.class), new Object[]{customerNumber});
		} catch (Exception e) {
			HashMap<String, Object> map1 = new HashMap<>();
			map1.put("transactionStatusCode", 401);
			map1.put("transactionStatusDescription", "Customer not found");
			return new ResponseEntity<>(map1, HttpStatus.NOT_FOUND);
		}
		
		//try to get transactions record
		try {
			myTransactions = jdbcTemplate.query("SELECT SUM(AMOUNT) FROM TRANSACTIONS WHERE ACCOUNTNUMBER = ?",
					BeanPropertyRowMapper.newInstance(Transaction.class), new Object[]{myAccount.getAccountNumber()});
		} catch (Exception e) {
			System.out.println("No transactions yet");
		}
		
		HashMap<String, Object> map1 = new HashMap<>();
		map1.put("customerNumber", myAccount.getCustomerNumber());
		map1.put("customerName", myAccount.getCustomerName());
		map1.put("customerMobile", myAccount.getCustomerMobile());
		map1.put("customerEmail", myAccount.getCustomerEmail());
		map1.put("address1", myAccount.getAddress1());
		map1.put("address2", myAccount.getAddress2());
		HashMap<String, Object> map2 = new HashMap<>();
		map2.put("accountNumber", myAccount.getAccountNumber());
		if (myAccount.getAccountType().equals("S"))
			map2.put("accountType", "Savings");
		if (myAccount.getAccountType().equals("C"))
			map2.put("accountType", "Checking");
		float totalBalance = 0;
		if (myTransactions != null) {
			for (int i = 0; i < myTransactions.size(); i++) {
				totalBalance = totalBalance + myTransactions.get(i).getAmount();
			}
		}
		map2.put("availableBalance", totalBalance);
		map1.put("savings", map2);
		map1.put("transactionStatusCode", 302);
		map1.put("transactionStatusDescription", "Customer Account found");
		return new ResponseEntity<>(map1, HttpStatus.FOUND);
	}
	
	@PostMapping("/")
	public ResponseEntity<?> create(@RequestBody Account newAccount) {
		//validate fields
		String errorMessage = validateFields(newAccount);
		
		//return response on error on validation of fields
		if (errorMessage != null) {
			HashMap<String, Object> map1 = new HashMap<>();
			map1.put("transactionStatusCode", 400);
			map1.put("transactionStatusDescription", errorMessage);
			return new ResponseEntity<>(map1, HttpStatus.BAD_REQUEST);
		}
		
		//generate customer number
		long customerNumber = generateCustomerNumber();
		System.out.println("Customer Number---"+customerNumber);
		
		//generate account number
		int accountNumber = 10001;
		Account lastAccount = null;
		try {
			lastAccount = jdbcTemplate.queryForObject("SELECT * FROM ACCOUNTS ORDER BY ID DESC LIMIT 1", 
					BeanPropertyRowMapper.newInstance(Account.class));
		} catch (Exception e) {
			System.out.println("No records in the account table yet");
		}
		if (lastAccount != null) {
			int lastAccountNumber = Integer.parseInt(lastAccount.getAccountNumber());
			System.out.println("Last Account Number---"+lastAccountNumber);
			accountNumber = lastAccountNumber+1;
		}
		System.out.println("Account Number---"+accountNumber);
		
		//save new account
		int updatedRow = jdbcTemplate.update("INSERT INTO ACCOUNTS (customerNumber, customerName, "
				+ "customerMobile, customerEmail, address1, address2, accountType, accountNumber) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?)",
				customerNumber, newAccount.getCustomerName(), newAccount.getCustomerMobile(),
				newAccount.getCustomerEmail(), newAccount.getAddress1(), newAccount.getAddress2(),
				newAccount.getAccountType(), accountNumber);
		
		//return success response
		System.out.println("Insert data---"+(updatedRow==1));
		if (updatedRow == 1) {
			HashMap<String, Object> map1 = new HashMap<>();
			map1.put("customerNumber", customerNumber);
			map1.put("transactionStatusCode", 201);
			map1.put("transactionStatusDescription", "Customer account created");
			return new ResponseEntity<>(map1, HttpStatus.CREATED);
		} else {
			HashMap<String, Object> map1 = new HashMap<>();
			map1.put("transactionStatusCode", 400);
			map1.put("transactionStatusDescription", "Error in creating a new customer account");
			return new ResponseEntity<>(map1, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	private String validateFields(Account newAccount) {
		String customerName = newAccount.getCustomerName();
		String customerMobile = newAccount.getCustomerMobile();
		String customerEmail = newAccount.getCustomerEmail();
		String address1 = newAccount.getAddress1();
		String address2 = newAccount.getAddress2();
		String accountType = newAccount.getAccountType();
		
		System.out.println("customerName---" + customerName);
		System.out.println("customerMobile---" + customerMobile);
		System.out.println("customerEmail---" + customerEmail);
		System.out.println("address1---" + address1);
		System.out.println("address2---" + address2);
		System.out.println("accountType---" + accountType);
		
		//check required fields
		if (customerName == null || customerName.isEmpty() || customerName.trim().isEmpty()) {
			return "Customer Name is required field";
		}
		else if (customerMobile == null || customerMobile.isEmpty() || customerMobile.trim().isEmpty()) {
			return "Customer Mobile is required field";
		}
		else if (customerEmail == null || customerEmail.isEmpty() || customerEmail.trim().isEmpty()) {
			return "Customer Email is required field";
		}
		else if (address1 == null || address1.isEmpty() || address1.trim().isEmpty()) {
			return "Address 1 is required field";
		}
		else if (accountType == null || accountType.isEmpty() || accountType.trim().isEmpty()) {
			return "Account Type is required field";
		}
		
		//email validation
		String emailRegex = "^(.+)@(\\S+)$";
		boolean validEmail = Pattern.compile(emailRegex).matcher(customerEmail).matches();
		System.out.println("Email validation---"+validEmail);
		if (!validEmail) {
			return "Email is invalid";
		}
		
		//check accountType
		boolean validAccoutType = !accountType.equals("S") && !accountType.equals("C");
		System.out.println("Account Type Validation---"+ validAccoutType);
		if (validAccoutType) {
			return "Account Type is invalid";
		}
		
		return null;
	}
	
	private long generateCustomerNumber() {
	  long n = System.currentTimeMillis() % LIMIT;
	  if ( n <= last ) {
	    n = (last + 1) % LIMIT;
	  }
	  return last = n;
	}
}
