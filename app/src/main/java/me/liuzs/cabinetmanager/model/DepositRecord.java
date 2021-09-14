package me.liuzs.cabinetmanager.model;

import java.util.ArrayList;
import java.util.List;

public class DepositRecord {
    public final List<DepositItem> items = new ArrayList<DepositItem>();
    public String labId;
    public String labNo;
    public String labName;
    public int putId;
    public String putNo;
    public String userName;
    public String userId;
}
