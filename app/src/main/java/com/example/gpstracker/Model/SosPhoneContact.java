package com.example.gpstracker.Model;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class SosPhoneContact {
    private String Name;
    private String Phone;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SosPhoneContact(){}

    public SosPhoneContact(String name, String phone) {
        Name = name;
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}
