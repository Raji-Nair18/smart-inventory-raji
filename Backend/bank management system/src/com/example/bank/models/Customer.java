package com.example.bank.models;

import java.sql.Date;

public class Customer {
    private int customerId;
    private String name;
    private String address;
    private String phone;
    private String email;
    private Date dateOfBirth;
    private String accountType;

    // getters/setters
    public int getCustomerId(){return customerId;} public void setCustomerId(int id){this.customerId=id;}
    public String getName(){return name;} public void setName(String n){this.name=n;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public Date getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(Date d){this.dateOfBirth=d;}
    public String getAccountType(){return accountType;} public void setAccountType(String at){this.accountType=at;}
}