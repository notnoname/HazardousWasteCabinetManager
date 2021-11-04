package me.liuzs.cabinetmanager.model;

public class OptLog {
    public long id;
    public String who;
    public String obj;
    public String opt;
    public String time;

    public OptLog(String who, String obj, String opt, String time) {
        this.who = who;
        this.obj = obj;
        this.opt = opt;
        this.time = time;
    }
}
