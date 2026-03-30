package com.example.bank.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TransactionRecord {
    private int transactionId;
    private int accountNo;
    private String transactionType;
    private BigDecimal amount;
    private Timestamp transactionDate;
    private String note;

    // getters/setters
    public int getTransactionId(){return transactionId;} public void setTransactionId(int id){this.transactionId=id;}
    public int getAccountNo(){return accountNo;} public void setAccountNo(int a){this.accountNo=a;}
    public String getTransactionType(){return transactionType;} public void setTransactionType(String t){this.transactionType=t;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal m){this.amount=m;}
    public Timestamp getTransactionDate(){return transactionDate;} public void setTransactionDate(Timestamp t){this.transactionDate=t;}
    public String getNote(){return note;} public void setNote(String n){this.note=n;}
}