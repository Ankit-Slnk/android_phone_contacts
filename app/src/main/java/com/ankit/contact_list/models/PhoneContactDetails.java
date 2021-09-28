package com.ankit.contact_list.models;

public class PhoneContactDetails {
    public String name, phone;
    public boolean isHeader;

    public PhoneContactDetails() {

    }

    public PhoneContactDetails(String name, String phone, boolean isHeader) {
        this.name = name;
        this.phone = phone;
        this.isHeader = isHeader;
    }
}
