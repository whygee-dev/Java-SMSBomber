package com.example.smsbomber;

public class Contact {
    String name, id, number;
    int exchangedSmsCount;

    public Contact(String name, String id, String number) {
        this.name = name;
        this.id = id;
        this.exchangedSmsCount = 0;
        this.number = number;
    }
}
