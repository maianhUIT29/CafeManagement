package com.example.cafemanagement.model;

public class ShiftConfigModel {
    private String name;
    private String start;
    private String end;

    public ShiftConfigModel() {}

    // Getter và Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }
}