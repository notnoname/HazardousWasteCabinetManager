package me.liuzs.cabinetmanager.model;

import java.util.List;

public class Cabinet {
    public String id;
    public String name;
    public String code;
    public String type;
    public int capacity;
    public List<User> operators;
    public User owner;
    public Agency org;
}
