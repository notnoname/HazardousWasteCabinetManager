package me.liuzs.cabinetmanager.model;

import java.util.LinkedList;
import java.util.List;

/**
 * TankInfo， TankInfo和CabinetInfo合并
 */
public class CabinetInfo {
    public final List<DeviceInfo> devices = new LinkedList<>();
    public String orgId;
    public String orgName;
    public String labId;
    public String labName;
    public String tenantId;
    public String tankName;
    public String tankId;
    public String tankNo;
    /**
     * 1、主控组合式；2、单体式；3、非联网式
     */
    public int tankType = 0;
    public String personMobile;
    public String safetyPerson;
}
