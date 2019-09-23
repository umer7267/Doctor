package com.healthtracker.doctor.model;

public class Registration {
    public String name;
    public String email;
    public String country;
    public String phone;
    public String password;

    public Registration(){

    }

    public Registration(String name, String email, String country, String phone, String password) {
        this.name = name;
        this.email = email;
        this.country = country;
        this.phone = phone;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
