package me.liuzs.cabinetmanager.model;

public class StatusOption {
    public enum FanWorkModel {None, Auto, Manual}
    public FanWorkModel fanWorkModel = FanWorkModel.None;
    public boolean vocAlert;
    public boolean fgAlert;
    public boolean tempHighAlert;
    public boolean tempLowAlert;
    public boolean humidityHighAlert;
    public boolean humidityLowAlert;
    public boolean fireAlert;
    public Exception e;
}
