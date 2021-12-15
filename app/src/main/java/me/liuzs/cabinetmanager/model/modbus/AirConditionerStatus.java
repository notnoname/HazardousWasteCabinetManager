package me.liuzs.cabinetmanager.model.modbus;

/**
 * 空调控制器
 * 功能说明	PLC地址	数据意义说明
 * 温度（模块所在电箱内）	40569	有符号短整形，需乘0.01得到实际结果，单位：℃
 * 湿度（模块所在电箱内）	40570	有符号短整形，需乘0.01得到实际结果，单位：%
 * 信号类型	40571	0：电流互感器信号、1：光电传感器信号。信号类型取决于设备上信号选择拨码开关。
 * 检测信号值	40572	传感器信号参考值，仅供空调开关机状态的判断
 * 运行状态	40573	0：关机、1：开机
 * 红外状态	40574	0：空闲、1和2：工作
 * 波特率	40575	0：1200bps:1：2400bps:2：4800bps:3：9600bps:4：19200bps:5：38400bps:6：56000bps:7：57600bps:8：115200bps
 * 设备地址	40576	范围1-254
 * 万能遥控器ID 低位	40577	万能遥控器ID,从“万能空调遥控器对应表”获取相应品牌遥控器ID。0008H为低16位，0009H为高16位
 * 万能遥控器ID 高位	40578
 * 遥控器工作模式	40579	0：红外学习遥控器接收模式，1：红外学习遥控器发射模式，2：万能遥控器配对，>2：万能遥控器模式
 * 空调开关机设定	40580	0：关机，1：开机
 * 空调模式设定	40581	0：自动，1：制冷，2：制热，3：除湿；4送风
 * 空调温度设定	40582	范围16-30
 * 空调风速设定	40583	0：自动，1：低，2：中，3：高
 * 空调风向设定	40584	0：扫风关闭，1：扫风开启
 * 运行状态阈值	40585	开机信号阀值,用于开关机判断
 * 自动控制使能	40586	0：禁用，1：启用
 * 设备扩展地址	40587	设备硬件地址，不可修改类似MAC地址
 * 遥控器ID 低位s	40241	万能遥控器ID,从“万能空调遥控器对应表”获取相应品牌遥控器ID。0002H为低16位，0003H为高16位
 * 遥控器ID 高位s	40242
 * 遥控器工作模式s	40243	0：红外学习遥控器接收模式，1：红外学习遥控器发射模式，2：万能遥控器配对，>2：万能遥控器模式
 * 空调开关机设定s	40244	0：关机，1：开机
 * 空调模式设定s	40245	0：自动，1：制冷，2：制热，3：除湿；4送风
 * 空调温度设定s	40246	范围16-30
 * 空调风速设定s	40247	0：自动，1：低，2：中，3：高
 * 空调风向设定s	40248	0：扫风关闭，1：扫风开启
 * 运行阈值设定s	40249	此阀值用于判断空调开关机状态，需要根据空调关机和开机两种状态下的“空调运行状态检测信号值”进行设定。当大于此值，空调控制器会判读“空调运行状态”为开机。
 * 自动控制使能s	40250	"0：禁用，1：启用
 * 启用后，当出现温度高于设定值、市电来电、检测到空调关机这几种情况，空调控制器会自动每隔5分钟发送一次当前空调设定指令（除空调控制器设定为关机状态），确保空调工作运行。"
 * 遥控器ID使生效	002001	1：使设置的参数发送至红外控制器，0：红外控制器接受成功后自动归零
 * 遥控器工作模式使生效	002002
 * 运行阈值设定使生效	002003
 * 运行设置使生效	002004
 */
public class AirConditionerStatus {
    public static final int ACStatusAddress = 40573;
    public static final int ACRemoteWorkModelAddress = 40243;
    public static final int ACPowerSetAddress = 40244;
    public static final int ACWorkModelAddress = 40245;
    public static final int ACTargetTempAddress = 40246;
    public static final int ACFanSpeedModelAddress = 40247;
    public static final int ACFanSweepAddress = 40248;
    public static final int ACCtrlModelAddress = 40250;


    public static final int ACRemoteIDSetCommitAddress = 2001;
    public static final int ACRemoteWorkModelSetCommitAddress = 2001;
    public static final int ACThresholdCommitAddress = 2003;
    public static final int ACSetCommitAddress = 2004;


    /**
     * 空调工作模式
     */
    public enum WorkModel {
        Auto, Refrigeration, Heating, Dehumidification,
        AirSupply
    }

    /**
     * 空调遥控器工作模式
     */
    public enum RemoteWorkModel {
        Receive, Launch, Pair, Run
    }

    /**
     * 风速模式
     */
    public enum FanSpeedModel {
        Auto, Low, Middle, High
    }

    /**
     * 开关
     */
    public boolean powerOn;
    public boolean autoCtrl;
    public WorkModel workModel;
    public RemoteWorkModel remoteWorkModel;
    public int targetTemp;
    public FanSpeedModel fanSpeedModel;
    /**
     * 自动扫叶是否开启？
     */
    public boolean fanSweep;


    public transient Exception e;

}
