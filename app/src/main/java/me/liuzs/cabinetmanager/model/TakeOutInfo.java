package me.liuzs.cabinetmanager.model;

import java.util.LinkedList;
import java.util.List;

public class TakeOutInfo {
    public final List<TakeOutItemInfo> items = new LinkedList<>();
    public String createTime;
    public String devId;
    public String devName;
    public String outLabId;
    public String labName;
    public String outNo;
    public int outId;
    public String tankId;
    public String tankName;
    public String userName;
    public String address;
}
