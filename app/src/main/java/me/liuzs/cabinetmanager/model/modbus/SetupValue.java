package me.liuzs.cabinetmanager.model.modbus;

public class SetupValue {

    /**
     * VOC联动，最大值
     */
    public float vocUnionMax;

    /**
     * VOC联动，最小值
     */
    public float vocUnionMin;

    /**
     * 风机联动，工作时长
     */
    public int fanUnionWorkTime;

    /**
     * 风机联动，停止时长
     */
    public int fanUnionStopTime;

    /**
     * 风机联动，启动频率
     */
    public float fanUnionFrequency;

    /**
     * VOC自动告警
     */
    public boolean vocAlertAuto;
    /**
     * 可燃气体自动告警
     */
    public final boolean fgAlertAuto = true;
    /**
     * 温度高位自动报警
     */
    public boolean tempHighAlertAuto;
    /**
     * 温度低位自动报警
     */
    public boolean tempLowAlertAuto;
    /**
     * 湿度高位自动报警
     */
    public boolean humidityHighAlertAuto;
    /**
     * 湿度低位自动报警
     */
    public boolean humidityLowAlertAuto;

    /**
     * VOC自动告警阈值
     */
    public float vocAlertAutoThreshold;

    /**
     * 可燃气体自动告警阈值
     */
    public float fgAlertThreshold;
    public float tempHighAlertThreshold;
    public float tempLowAlertThreshold;
    public float humidityHighAlertThreshold;
    public float humidityLowAlertThreshold;
    public boolean alertSoundLight;
    public Exception e;

}
