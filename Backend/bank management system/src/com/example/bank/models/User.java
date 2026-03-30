package com.example.bank.models;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;

    // constructors/getters/setters
    public User() {}
    public User(int id,String u,String p,String r){this.userId=id;this.username=u;this.password=p;this.role=r;}
    public int getUserId(){return userId;} public void setUserId(int id){this.userId=id;}
    public String getUsername(){return username;} public void setUsername(String u){this.username=u;}
    public String getPassword(){return password;} public void setPassword(String p){this.password=p;}
    public String getRole(){return role;} public void setRole(String r){this.role=r;}
}