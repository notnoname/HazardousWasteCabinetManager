package me.liuzs.cabinetmanager.model;

/**
 * 危废管环境监控数据
 */
public class EnvironmentStatus {
    public long createTime;
    public float vocLowerPart;
    public float vocUpperPart;
    public float flammableGasLowerPart;
    public float flammableGasUpperPart;
    public float temperatureA;
    public float temperatureB;
    public float humidityA;
    public float humidityB;
    public Exception e = null;
}
