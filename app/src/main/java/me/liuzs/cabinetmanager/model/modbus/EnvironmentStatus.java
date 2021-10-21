package me.liuzs.cabinetmanager.model.modbus;

/**
 * 环境监控数据
 * 位置	功能说明	PLC地址	数据意义说明
 * VOC浓度-下部	浓度	40513
 * 	精度	40514	2
 * 	状态	40515
 * 	气体种类	40516	63
 * 	气体单位	40517	3：PPM
 * VOC浓度-上部	浓度	40518
 * 	精度	40519	2
 * 	状态	40520
 * 	气体种类	40521	63
 * 	气体单位	40522	3：PPM
 * 可燃气体浓度-下部	浓度	40523
 * 	 	40524	0
 * 	状态	40525
 * 	气体种类	40526	50
 * 	气体单位	40527	1：%LEL
 * 可燃气体浓度浓度-上部	浓度	40528
 * 	精度	40529	0
 * 	状态	40530
 * 	气体种类	40531	50
 * 	气体单位	40532	1：%LEL
 * 温度-A点	浓度	40533
 * 	精度	40534	1
 * 	状态	40535
 * 	气体种类	40536	65
 * 	气体单位	40537	7：℃
 * 湿度-A点	浓度	40538
 * 	精度	40539	1
 * 	状态	40540
 * 	气体种类	40541	66
 * 	气体单位	40542	9：%RH
 * 温度-B点	浓度	40543
 * 	精度	40544	1
 * 	状态	40545
 * 	气体种类	40546	65
 * 	气体单位	40547	7：℃
 * 湿度-B点	浓度	40548
 * 	精度	40549	1
 * 	状态	40550
 * 	气体种类	40551	66
 * 	气体单位	40552	9：%RH
 *
 *
 * 精度处理
 * 0	零小数
 * 1	1位小数（除以10）
 * 2	2位小数（除以100）
 * 3	3位小数（除以1000）
 *
 *
 * 状态
 * 0	预热
 * 1	正常
 * 3	传感器故障
 * 7	探头故障
 */
public class EnvironmentStatus {
    public static final int VOCLowerAddress = 40513;
    public static final int VOCUpperAddress = 40518;
    public static final int FGLowerAddress = 40524;
    public static final int FGUpperAddress = 40528;
    public static final int TemperatureAAddress = 40534;
    public static final int TemperatureBAddress = 40543;
    public static final int HumidityAAddress = 40534;
    public static final int HumidityBAddress = 40548;
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
