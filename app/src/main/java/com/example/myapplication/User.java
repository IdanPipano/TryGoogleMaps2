package com.example.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class User {
    String email, password, userName;
    List<List<Double>> matrix = new ArrayList<>();

    public User(String email, String password, String userName) {
        this.email = email;
        this.password = password;
        this.userName = userName;
        List<Double> row1 = Arrays.asList(1.1, 2., 3.);
        List<Double> row2 = Arrays.asList(-4.5, -5.7, 6.3);
        matrix.add(row1);
        matrix.add(row2);
    }
    
    public User() {

    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public List<List<Double>> getMatrix() {
        return matrix;
    }
}
