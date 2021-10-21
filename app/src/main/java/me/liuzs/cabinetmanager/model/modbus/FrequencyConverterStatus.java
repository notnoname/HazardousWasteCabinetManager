package me.liuzs.cabinetmanager.model.modbus;

/**
 * 变频器
 * 功能说明	PLC地址	数据意义说明	R/W特性
 * 变频器状态字1	40553	"0001H：正转运行中
 * 0002H：反转运行中
 * 0003H：变频器停机中
 * 0004H：变频器故障中
 * 0005H：变频器POFF状态"	R
 * 变频器状态字2	40554	"Bit0： =0：运行准备未就绪 =1：运行准备就绪
 * Bi1~2：=00：电机1 =01：电机2
 *        =10：电机3 =11：电机4
 * Bit3： =0：异步机 =1：同步机
 * Bit4：=0：未过载预报警 =1：过载预报警
 * Bit5~ Bit6：=00：键盘控制 =01：端子控制
 *             =10：通讯控制"	R
 * 变频器故障代码	40555	依手册	R
 * 变频器识别代码	40556	依手册	R
 * 运行频率	40557	需乘以0.01得到实际值，单位HZ	R
 * 设定频率	40558	需乘以0.01得到实际值，单位HZ	R
 * 母线电压	40559	需乘以0.1得到实际值，单位V	R
 * 输出电压	40560	单位V	R
 * 输出电流	40561	需乘以0.1得到实际值，单位A	R
 * 运行转速	40562	单位RPM	R
 * 输出功率	40563	暂无需求	R
 * 输出转矩	40564		R
 * 闭环设定	40565		R
 * 闭环反馈	40566		R
 * 输入IO状态	40567		R
 * 输出IO状态	40568		R
 * 频率设定	40701	手动运行时调整输出频率（实际值100倍）	R/W
 * 故障复位	40702	1：故障复位，！=1：变频器接受成功后自动归零	R/W
 */
public class FrequencyConverterStatus {
    public static final int FCStatusAddress = 40553;
    public static final int FCRotatingSpeedAddress = 40562;
    public static final int FCFrequencyAddress = 40557;
    public static final int FCFrequencyTargetAddress = 40558;
    public static final int FCResetAddress = 40558;

    public enum Status {
        Clockwise(1), Counterclockwise(2), Stop(3), Fault(4), PowerOff(5);
        private final int id;

        Status(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public Status status = Status.PowerOff;

    public float rotatingSpeed;

    public float frequency;

    public float targetFrequency;

    public Exception e;

}
