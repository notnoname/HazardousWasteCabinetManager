package me.liuzs.cabinetmanager.service;

import android.util.Log;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.model.modbus.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.modbus.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.modbus.FrequencyConverterStatus;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.model.modbus.StatusOption;

public class ModbusService {
    private static final String TAG = "ModbusService";
    private static final String ModbusIP = "10.0.2.2";
    private static final int ModbusPort = 502;
    private static final int ModbusSlaveId = 1;
    /**
     * 工厂。
     */
    private final static ModbusFactory mModbusFactory = new ModbusFactory();

    /**
     * 获取master
     *
     * @return ModbusMaster
     * @throws ModbusInitException exception
     */
    private static ModbusMaster getMaster() throws ModbusInitException {
        IpParameters params = new IpParameters();
        params.setHost(ModbusIP);
        params.setPort(ModbusPort);
        // modbusFactory.createRtuMaster(wapper); //RTU 协议
        // modbusFactory.createUdpMaster(params);//UDP 协议
        // modbusFactory.createAsciiMaster(wrapper);//ASCII 协议
        ModbusMaster master = mModbusFactory.createTcpMaster(params, false);// TCP 协议
        master.init();
        return master;
    }

    /**
     * 读取[01 Coil Status 0x]类型 开关数据
     *
     * @param offset 位置
     * @return 读取值
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    private static Boolean readCoilStatus(int offset)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 01 Coil Status
        BaseLocator<Boolean> loc = BaseLocator.coilStatus(ModbusSlaveId, offset);
        Boolean value = getMaster().getValue(loc);
        return value;
    }

    private static void writeCoilStatus(int offset, boolean value) throws ModbusInitException, ErrorResponseException, ModbusTransportException {
        BaseLocator<Boolean> loc = BaseLocator.coilStatus(ModbusSlaveId, offset);
        getMaster().setValue(loc, value);
    }

    private static void writeHoldingRegister(int offset, Integer value) throws ModbusInitException, ErrorResponseException, ModbusTransportException {
        BaseLocator<Number> loc = BaseLocator.holdingRegister(ModbusSlaveId, offset, DataType.TWO_BYTE_INT_SIGNED);
        getMaster().setValue(loc, value);
    }

    /**
     * 读取[02 Input Status 1x]类型 开关数据
     *
     * @param offset 地址
     * @return 返回值
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    private static Boolean readInputStatus(int offset)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 02 Input Status
        BaseLocator<Boolean> loc = BaseLocator.inputStatus(ModbusSlaveId, offset);
        Boolean value = getMaster().getValue(loc);
        return value;
    }

    /**
     * 读取[03 Holding Register类型 2x]模拟量数据
     *
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    private static Number readHoldingRegister(int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 03 Holding Register类型数据读取
        BaseLocator<Number> loc = BaseLocator.holdingRegister(ModbusSlaveId, offset, dataType);
        Number value = getMaster().getValue(loc);
        return value;
    }

    /**
     * 读取[04 Input Registers 3x]类型 模拟量数据
     *
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return 返回结果
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    private static Number readInputRegisters(int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 04 Input Registers类型数据读取
        BaseLocator<Number> loc = BaseLocator.inputRegister(ModbusSlaveId, offset, dataType);
        Number value = getMaster().getValue(loc);
        return value;
    }

    /**
     * 读取变频器状态
     *
     * @return
     */
    public synchronized static FrequencyConverterStatus readFrequencyConverterStatus() {
        FrequencyConverterStatus frequencyConverterStatus = new FrequencyConverterStatus();

        try {
            BatchRead<Integer> batch = new BatchRead<Integer>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, FC.FCStatusAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, FC.FCRotatingSpeedAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, FC.FCFrequencyAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, FC.FCFrequencyTargetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);

            frequencyConverterStatus.status = FrequencyConverterStatus.Status.values()[getIntValue(results, 0)];
            frequencyConverterStatus.rotatingSpeed = getIntValue(results, 1);
            frequencyConverterStatus.frequency = getIntValue(results, 2) / 100f;
            frequencyConverterStatus.targetFrequency = getIntValue(results, 3) / 100f;

        } catch (Exception e) {
            e.printStackTrace();
            frequencyConverterStatus.e = e;
        }

        Log.d(TAG, CabinetCore.GSON.toJson(frequencyConverterStatus));
        return frequencyConverterStatus;
    }

    /**
     * 读取告警状态
     *
     * @return
     */
    public synchronized static StatusOption readStatusOption() {
        StatusOption statusOption = new StatusOption();

        try {
            BatchRead<Integer> batch = new BatchRead<>();
            batch.addLocator(0, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertVOCAddress - 1));
            batch.addLocator(1, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertFGAddress - 1));
            batch.addLocator(2, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertTempHighAddress - 1));
            batch.addLocator(3, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertHumidityHighAddress - 1));
            batch.addLocator(4, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertTempLowAddress - 1));
            batch.addLocator(5, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertHumidityLowAddress - 1));
            batch.addLocator(6, BaseLocator.coilStatus(ModbusSlaveId, Status.AlertFireAddress - 1));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);
            statusOption.vocAlert = (Boolean) results.getValue(0);
            statusOption.fgAlert = (Boolean) results.getValue(1);
            statusOption.tempHighAlert = (Boolean) results.getValue(2);
            statusOption.humidityHighAlert = (Boolean) results.getValue(3);
            statusOption.tempLowAlert = (Boolean) results.getValue(4);
            statusOption.humidityLowAlert = (Boolean) results.getValue(5);
            statusOption.fireAlert = (Boolean) results.getValue(6);
            statusOption.fanWorkModel = StatusOption.FanWorkModel.values()[readHoldingRegister(Status.UnionWorkModelAddress - 1, DataType.TWO_BYTE_INT_SIGNED).intValue()];

        } catch (Exception e) {
            e.printStackTrace();
            statusOption.e = e;
        }

        Log.d(TAG, CabinetCore.GSON.toJson(statusOption));
        return statusOption;
    }

    /**
     * 读取环境数据
     */
    public synchronized static EnvironmentStatus readEnvironmentStatus() {
        EnvironmentStatus environmentStatus = new EnvironmentStatus();

        try {
            BatchRead<Integer> batch = new BatchRead<Integer>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, Environment.VOCLowerAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, Environment.VOCUpperAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, Environment.FGLowerAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, Environment.FGUpperAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, Environment.TemperatureAAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, Environment.TemperatureBAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, Environment.HumidityAAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(7, BaseLocator.holdingRegister(ModbusSlaveId, Environment.HumidityBAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);

            environmentStatus.vocLowerPart = getIntValue(results, 0) / 100f;
            environmentStatus.vocUpperPart = getIntValue(results, 1) / 100f;
            environmentStatus.fgLowerPart = getIntValue(results, 2);
            environmentStatus.fgUpperPart = getIntValue(results, 3);
            environmentStatus.tempA = getIntValue(results, 4) / 10f;
            environmentStatus.tempB = getIntValue(results, 5) / 10f;
            environmentStatus.humidityA = getIntValue(results, 6) / 10f;
            environmentStatus.humidityB = getIntValue(results, 7) / 10f;
        } catch (Exception e) {
            e.printStackTrace();
            environmentStatus.e = e;
        }

        Log.d(TAG, CabinetCore.GSON.toJson(environmentStatus));
        return environmentStatus;
    }

    /**
     * 读取环境数据
     */
    public synchronized static AirConditionerStatus readAirConditionerStatus() {
        AirConditionerStatus airConditionerStatus = new AirConditionerStatus();

        try {
            BatchRead<Integer> batch = new BatchRead<Integer>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, AC.ACStatusAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, AC.ACCtrlModelAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, AC.ACWorkModelAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, AC.ACTargetTempAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, AC.ACFanSpeedModelAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, AC.ACFanSweepAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);

            airConditionerStatus.powerOn = getIntValue(results, 0) == 1;
            airConditionerStatus.autoCtrl = getIntValue(results, 1) == 1;
            airConditionerStatus.workModel = AirConditionerStatus.WorkModel.values()[getIntValue(results, 2)];
            airConditionerStatus.targetTemp = getIntValue(results, 3);
            airConditionerStatus.fanSpeedModel = AirConditionerStatus.FanSpeedModel.values()[getIntValue(results, 4)];
            airConditionerStatus.fanSweep = getIntValue(results, 5) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            airConditionerStatus.e = e;
        }

        Log.d(TAG, CabinetCore.GSON.toJson(airConditionerStatus));
        return airConditionerStatus;
    }

    public synchronized static SetupValue readSetupValue() {
        SetupValue setupValue = new SetupValue();
        try {
            BatchRead<Integer> batch = new BatchRead<>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCUnionMaxAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCUnionMinAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FanUnionWorkTimeAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FanUnionStopTimeAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FanUnionFrequencyAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempHighAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(7, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempLowAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(8, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityHighAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(9, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityLowAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(10, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCAlertAutoThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(11, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FGAlertAutoThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(12, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempHighAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(13, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempLowAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(14, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityHighAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(15, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityLowAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(16, BaseLocator.holdingRegister(ModbusSlaveId, Setup.AlertSoundLightAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);
            setupValue.vocUnionMax = getIntValue(results, 0) / 100f;
            setupValue.vocUnionMin = getIntValue(results, 1) / 100f;
            setupValue.fanUnionWorkTime = getIntValue(results, 2);
            setupValue.fanUnionStopTime = getIntValue(results, 3);
            setupValue.fanUnionFrequency = getIntValue(results, 4) / 100f;
            setupValue.vocAlertAuto = getIntValue(results, 5) == 1;
            setupValue.tempHighAlertAuto = getIntValue(results, 6) == 1;
            setupValue.tempLowAlertAuto = getIntValue(results, 7) == 1;
            setupValue.humidityHighAlertAuto = getIntValue(results, 8) == 1;
            setupValue.humidityLowAlertAuto = getIntValue(results, 9) == 1;
            setupValue.vocAlertAutoThreshold = getIntValue(results, 10) / 100f;
            setupValue.fgAlertThreshold = getIntValue(results, 11) / 100f;
            setupValue.tempHighAlertThreshold = getIntValue(results, 12) / 100f;
            setupValue.tempLowAlertThreshold = getIntValue(results, 13) / 100f;
            setupValue.humidityHighAlertThreshold = getIntValue(results, 14) / 100f;
            setupValue.humidityLowAlertThreshold = getIntValue(results, 15) / 100f;
            setupValue.alertSoundLight = getIntValue(results, 16) == 1;

        } catch (Exception e) {
            e.printStackTrace();
            setupValue.e = e;
        }
        return setupValue;
    }

    /**
     * 设置风机工作模式
     *
     * @param fanWorkModel fanWorkModel 风机工作模式
     */
    public synchronized static boolean setFanWorkModel(StatusOption.FanWorkModel fanWorkModel) {
        try {
            writeHoldingRegister(Status.UnionWorkModelAddress - 1, fanWorkModel.ordinal());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 设置空调工作模式
     *
     * @param workModel 工作模式
     * @return 成功失败
     */
    public synchronized static boolean setACWorkModel(AirConditionerStatus.WorkModel workModel) {
        try {
            writeHoldingRegister(AC.ACWorkModelAddress - 1, workModel.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized static boolean setACPower(boolean powerOn) {
        try {
            writeHoldingRegister(AC.ACPowerSetAddress - 1, powerOn ? 1 : 0);
            writeCoilStatus(AC.ACPowerSetCommitAddress - 1, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        readSetupValue();
    }

    private static Integer getIntValue(BatchResults br, Object key) {
        Object o = br.getValue(key);
        if (o instanceof Short) {
            return ((Short) o).intValue();
        } else {
            return (Integer) o;
        }
    }

    /**
     * 环境检测
     */
    public class Environment {
        private static final int VOCLowerAddress = 40513;
        private static final int VOCUpperAddress = 40518;
        private static final int FGLowerAddress = 40524;
        private static final int FGUpperAddress = 40528;
        private static final int TemperatureAAddress = 40534;
        private static final int TemperatureBAddress = 40543;
        private static final int HumidityAAddress = 40534;
        private static final int HumidityBAddress = 40548;
    }

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
    public class AC {
        private static final int ACStatusAddress = 40573;
        private static final int ACWorkModelAddress = 40581;
        private static final int ACCtrlModelAddress = 40586;
        private static final int ACTargetTempAddress = 40582;
        private static final int ACFanSpeedModelAddress = 40583;
        private static final int ACFanSweepAddress = 40584;
        private static final int ACPowerSetAddress = 40244;
        private static final int ACPowerSetCommitAddress = 002004;
    }

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
    public class Status {
        private static final int UnionWorkModelAddress = 40703;
        private static final int AlertVOCAddress = 2017;
        private static final int AlertFGAddress = 2018;
        private static final int AlertTempHighAddress = 2019;
        private static final int AlertHumidityHighAddress = 2020;
        private static final int AlertTempLowAddress = 2021;
        private static final int AlertHumidityLowAddress = 2022;
        private static final int AlertFireAddress = 2023;
    }

    /**
     * 变频器
     */
    public class FC {
        private static final int FCStatusAddress = 40553;
        private static final int FCRotatingSpeedAddress = 40562;
        private static final int FCFrequencyAddress = 40557;
        private static final int FCFrequencyTargetAddress = 40558;
    }

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
    public class Setup {
        private static final int VOCUnionMaxAddress = 40211;
        private static final int VOCUnionMinAddress = 40212;
        private static final int FanUnionWorkTimeAddress = 40214;
        private static final int FanUnionStopTimeAddress = 40213;
        private static final int FanUnionFrequencyAddress = 40201;
        private static final int VOCAlertAutoAddress = 40221;
        private static final int TempHighAlertAutoAddress = 40222;
        private static final int TempLowAlertAutoAddress = 40223;
        private static final int HumidityHighAlertAutoAddress = 40224;
        private static final int HumidityLowAlertAutoAddress = 40225;
        private static final int VOCAlertAutoThresholdAddress = 40215;
        private static final int FGAlertAutoThresholdAddress = 40216;
        private static final int TempHighAlertThresholdAddress = 40217;
        private static final int TempLowAlertThresholdAddress = 40218;
        private static final int HumidityHighAlertThresholdAddress = 40219;
        private static final int HumidityLowAlertThresholdAddress = 40220;
        private static final int AlertSoundLightAddress = 40226;
    }
}
