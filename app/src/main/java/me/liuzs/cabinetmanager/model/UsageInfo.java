package me.liuzs.cabinetmanager.model;

import java.util.LinkedList;
import java.util.List;

public class UsageInfo {
    public final List<UsageItemInfo> items = new LinkedList<>();
    public String createTime;
    public String labName;
    public String putId;
    public String putLabId;
    public String putNo;
    public int tenantId;
    public String userName;

    public String useAddress;
    public String building;
    public String roomNum;
    //    public String devId;
    public String tankId;
}
