package me.liuzs.cabinetmanager.model;

public class AirConditionerStatus {
    /**
     * 工作模式
     */
    public enum WorkModel {
        Auto, Refrigeration, Heating, Dehumidification,
        AirSupply
    }

    /**
     * 风速模式
     */
    public enum FanSpeedModel {
        Auto, Low, Middle, High
    }
    public boolean powerOn;
    public boolean autoControl;
    public WorkModel workModel;
    public int targetTemperature;
    public FanSpeedModel fanSpeedModel;
    /**
     * 自动扫叶是否开启？
     */
    public boolean fanDirModel;

}
