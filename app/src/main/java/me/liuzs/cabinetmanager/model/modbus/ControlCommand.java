package me.liuzs.cabinetmanager.model.modbus;

public class ControlCommand {
    public Boolean fanWorkModel;
    /**
     * 风机开关
     */
    public Boolean fanPowerOn;
    /**
     * 光氧开关
     */
    public Boolean oxygenPowerOn;
    /**
     * 空调控制模式
     */
    public Boolean autoCtrl;
    /**
     * 空调开关
     */
    public Boolean powerOn;
    /**
     * 空调工作模式
     */
    public AirConditionerStatus.WorkModel workModel;
    /**
     * 空调遥控器工作模式
     */
    public AirConditionerStatus.RemoteWorkModel remoteWorkModel;
    /**
     * 变频器复位
     */
    public Boolean frequencyReset;

}
