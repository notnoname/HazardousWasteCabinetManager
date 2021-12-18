package me.liuzs.cabinetmanager.model.modbus;

/**
 * 状态参数
 * 功能说明	PLC地址	数据意义说明	R/W特性	报警
 * 运行模式切换	40703	0:无，1：自动，2：手动	R/W
 * 风机启动	002009	手动模式下有效，按1松0	R/W
 * 风机停止	002010	手动模式下有效，按1松0	R/W
 * 光氧启动	002011	手动模式下有效，按1松0	R/W
 * 光氧停止	002012	手动模式下有效，按1松0	R/W
 * VOC浓度报警	002017	1：有效	R	Y
 * 可燃气体浓度报警	002018	1：有效，控制器自动触发声光报警器，启动风机（最大转速）	R	Y
 * 温度过高报警	002019	1：有效	R	Y
 * 温度过低报警	002020	1：有效	R	Y
 * 湿度过高报警	002021	1：有效	R	Y
 * 湿度过低报警	002022	1：有效	R	Y
 * 火灾报警	002023	1：有效，控制器自动触发声光报警器，切断主电源（UPS线路不受影响）	R	Y
 */
public class StatusOption {
    public static final int UnionWorkModelAddress = 703;
    public static final int FanStartAddress = 9;
    public static final int FanStopAddress = 10;
    public static final int OxygenStartAddress = 11;
    public static final int OxygenStopAddress = 12;

    public static final int AlertVOCAddress = 17;
    public static final int AlertFGAddress = 18;
    public static final int AlertTempHighAddress = 19;
    public static final int AlertHumidityHighAddress = 20;
    public static final int AlertTempLowAddress = 21;
    public static final int AlertHumidityLowAddress = 22;
    public static final int AlertFireAddress = 23;



    public enum FanWorkModel {None, Auto, Manual}
    public FanWorkModel fanWorkModel = FanWorkModel.None;
    public boolean vocAlert;
    public boolean fgAlert;
    public boolean tempHighAlert;
    public boolean tempLowAlert;
    public boolean humidityHighAlert;
    public boolean humidityLowAlert;
    public boolean fireAlert;

    public transient Exception e;
}
