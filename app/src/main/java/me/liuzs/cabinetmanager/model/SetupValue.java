package me.liuzs.cabinetmanager.model;

public class SetupValue {
    public enum WorkModel {None, Auto, Manual};

    public WorkModel workModel;

    public int vocThresholdMax;
    public int vocThresholdMin;
    public int workTime;
    public int stopTime;
    public int frequency;

    public boolean vocAlertAuto;
    public boolean temperatureHighAlertAuto;
    public boolean temperatureLowAlertAuto;
    public boolean humidityHighAlertAuto;
    public boolean humidityLowAlertAuto;

    public int vocAlertThreshold;
    public int flammableGasAlertThreshold;
    public int temperatureHighAlertThreshold;
    public int temperatureLowAlertThreshold;
    public int humidityHighAlertThreshold;
    public int humidityLowAlertThreshold;
    public Exception e;

}
