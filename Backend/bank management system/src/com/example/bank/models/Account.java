package com.example.bank.models;

import java.math.BigDecimal;
import java.sql.Date;

public class Account {
    private int accountNo;
    private int customerId;
    private BigDecimal balance;
    private Date openingDate;
    private String status;

    // getters/setters
    public int getAccountNo(){return accountNo;} public void setAccountNo(int a){this.accountNo=a;}
    public int getCustomerId(){return customerId;} public void setCustomerId(int c){this.customerId=c;}
    public BigDecimal getBalance(){return balance;} public void setBalance(BigDecimal b){this.balance=b;}
    public Date getOpeningDate(){return openingDate;} public void setOpeningDate(Date d){this.openingDate=d;}
    public String getStatus(){return status;} public void setStatus(String s){this.status=s;}
}
