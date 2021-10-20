package me.liuzs.cabinetmanager.model.modbus;

/**
 * 危废管环境监控数据
 */
public class EnvironmentStatus {
    public long createTime;
    public float vocLowerPart;
    public float vocUpperPart;
    public float fgLowerPart;
    public float fgUpperPart;
    public float tempA;
    public float tempB;
    public float humidityA;
    public float humidityB;
    public Exception e = null;
}
