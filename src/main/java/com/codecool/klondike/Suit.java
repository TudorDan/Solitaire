package com.codecool.klondike;

public enum Suit {
    HEARTS ("hearts", "red", 1),
    DIAMONDS ("diamonds", "red", 2),
    SPADES ("spades", "black", 3),
    CLUBS ("clubs", "black", 4);

    private String name;
    private String color;
    private Integer number;

    private Suit(String name, String color, Integer number) {
        this.name = name;
        this.color = color;
        this.number = number;
    }

    public String getName() {
        return name;
    }
    public String getColor() {
        return color;
    }
    public Integer getNumber() {
        return number;
    }
}
