package me.liuzs.cabinetmanager.model.modbus;

/**
 * 参数设置
 * 功能说明	寄存器地址 	PLC地址	数据意义说明	R/W特性
 * VOC高位	0x0D2	40211	自动运行模式下，VOC浓度max超过该值后，启动风机（最大转速）及光氧	R/W
 * VOC低位	0x0D3	40212	上述状态下，VOC浓度max降至该值后，停止风机及光氧，转至间歇运行模式	R/W
 * 停止分钟	0x0D4	40213	自动运行模式下，风机间歇运行，转速由“变频器初始频率”决定，该值决定停止段时间	R/W
 * 开启分钟	0x0D5	40214	间歇运行下，该值决定运转段时间	R/W
 * 初始频率设定	0x0C8	40201	自动模式时序运行及手动模式下初始输出频率 	R/W
 * VOC报警值	0x0D6	40215	VOC浓度max超过该值后，若报警使能生效，触发报警	R/W
 * 可燃气体报警值	0x0D7	40216	<=25，可燃气体浓度max超过该值后，触发报警	R/W
 * 温度高位报警值	0x0D8	40217	温度max超过该值后，若报警使能生效，触发报警	R/W
 * 温度低位报警值	0x0D9	40218	温度min低于该值后，若报警使能生效，触发报警	R/W
 * 湿度高位报警值	0x0DA	40219	湿度max超过该值后，若报警使能生效，触发报警	R/W
 * 湿度低位报警值	0x0DB	40220	湿度min低于该值后，若报警使能生效，触发报警	R/W
 * VOC报警使能	0x0DC	40221	1：生效, !=1:无效	R/W
 * 温度过高报警使能	0x0DD	40222	1：生效, !=1:无效	R/W
 * 温度过低报警使能	0x0DE	40223	1：生效, !=1:无效	R/W
 * 湿度过高报警使能	0x0DF	40224	1：生效, !=1:无效	R/W
 * 湿度过低报警使能	0x0E0	40225	1：生效, !=1:无效	R/W
 * 声光报警器使能	0x0E1	40226	1：生效, !=1:无效	R/W
 */
public class SetupValue {

    public static final int VOCUnionMaxAddress = 40211;
    public static final int VOCUnionMinAddress = 40212;
    public static final int FanUnionWorkTimeAddress = 40214;
    public static final int FanUnionStopTimeAddress = 40213;
    public static final int FanUnionFrequencyAddress = 40201;
    public static final int VOCAlertAutoAddress = 40221;
    public static final int TempHighAlertAutoAddress = 40222;
    public static final int TempLowAlertAutoAddress = 40223;
    public static final int HumidityHighAlertAutoAddress = 40224;
    public static final int HumidityLowAlertAutoAddress = 40225;
    public static final int VOCAlertAutoThresholdAddress = 40215;
    public static final int FGAlertAutoThresholdAddress = 40216;
    public static final int TempHighAlertThresholdAddress = 40217;
    public static final int TempLowAlertThresholdAddress = 40218;
    public static final int HumidityHighAlertThresholdAddress = 40219;
    public static final int HumidityLowAlertThresholdAddress = 40220;
    public static final int AlertSoundLightAddress = 40226;
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
    /**
     * 高温告警阈值
     */
    public float tempHighAlertThreshold;
    /**
     * 低温告警阈值
     */
    public float tempLowAlertThreshold;
    /**
     * 高湿度告警阈值
     */
    public float humidityHighAlertThreshold;
    /**
     * 低湿度告警阈值
     */
    public float humidityLowAlertThreshold;
    /**
     * 是否声光告警
     */
    public boolean alertSoundLight;
    public Exception e;

}
