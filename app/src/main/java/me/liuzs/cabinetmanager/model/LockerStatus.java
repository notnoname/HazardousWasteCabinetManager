package me.liuzs.cabinetmanager.model;

public class LockerStatus {
    public String deviceName;
    public String devId;
    public Status lock = Status.Unknown;
    public enum Status {
        Lock, Unlock, Unknown
    }
}
