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
import me.liuzs.cabinetmanager.model.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.SetupValue;

public class ModbusService {
    private static final String TAG = "ModbusService";
    private static final String ModbusIP = "10.0.2.2";
    private static final int ModbusPort = 502;
    private static final int ModbusSlaveId = 1;

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
     * 参数设置
     */
    public class Setup {
        private static final int UnionWorkModelAddress = 40703;
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


    /**
     * 工厂。
     */
    private static ModbusFactory mModbusFactory;

    static {
        if (mModbusFactory == null) {
            mModbusFactory = new ModbusFactory();
        }
    }

    /**
     * 获取master
     *
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getMaster() throws ModbusInitException {
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
    public static Boolean readCoilStatus(int offset)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 01 Coil Status
        BaseLocator<Boolean> loc = BaseLocator.coilStatus(ModbusSlaveId, offset);
        Boolean value = getMaster().getValue(loc);
        return value;
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
    public static Boolean readInputStatus(int offset)
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
    public static Number readHoldingRegister(int offset, int dataType)
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
    public static Number readInputRegisters(int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 04 Input Registers类型数据读取
        BaseLocator<Number> loc = BaseLocator.inputRegister(ModbusSlaveId, offset, dataType);
        Number value = getMaster().getValue(loc);
        return value;
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

            environmentStatus.vocLowerPart = results.getIntValue(0) / 100f;
            environmentStatus.vocUpperPart = results.getIntValue(1) / 100f;
            environmentStatus.fgLowerPart = results.getIntValue(2);
            environmentStatus.fgUpperPart = results.getIntValue(3);
            environmentStatus.tempA = results.getIntValue(4) / 10f;
            environmentStatus.tempB = results.getIntValue(5) / 10f;
            environmentStatus.humidityA = results.getIntValue(6) / 10f;
            environmentStatus.humidityB = results.getIntValue(7) / 10f;
        } catch (Exception e) {
            e.printStackTrace();
            environmentStatus.e = e;
        }

        Log.d(TAG, CabinetCore.GSON.toJson(environmentStatus));
        return environmentStatus;
    }

    public synchronized static SetupValue readSetupValue() {
        SetupValue setupValue = new SetupValue();
        try {
            BatchRead<Integer> batch = new BatchRead<Integer>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, Setup.UnionWorkModelAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCUnionMaxAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCUnionMinAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FanUnionWorkTimeAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FanUnionStopTimeAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FanUnionFrequencyAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(7, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempHighAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(8, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempLowAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(9, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityHighAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(10, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityLowAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(11, BaseLocator.holdingRegister(ModbusSlaveId, Setup.VOCAlertAutoThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(12, BaseLocator.holdingRegister(ModbusSlaveId, Setup.FGAlertAutoThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(13, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempHighAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(14, BaseLocator.holdingRegister(ModbusSlaveId, Setup.TempLowAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(15, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityHighAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(16, BaseLocator.holdingRegister(ModbusSlaveId, Setup.HumidityLowAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(17, BaseLocator.holdingRegister(ModbusSlaveId, Setup.AlertSoundLightAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);
            setupValue.workModel = SetupValue.WorkModel.values()[getIntValue(results,0)];
            setupValue.vocUnionMax = getIntValue(results,1) / 100f;
            setupValue.vocUnionMin = getIntValue(results,2) / 100f;
            setupValue.fanUnionWorkTime = getIntValue(results,3);
            setupValue.fanUnionStopTime = getIntValue(results,4);
            setupValue.fanUnionFrequency = getIntValue(results,5) / 100f;
            setupValue.vocAlertAuto = getIntValue(results,6) == 1;
            setupValue.tempHighAlertAuto = getIntValue(results,7) == 1;
            setupValue.tempLowAlertAuto = getIntValue(results,8) == 1;
            setupValue.humidityHighAlertAuto = getIntValue(results,9) == 1;
            setupValue.humidityLowAlertAuto = getIntValue(results,10) == 1;
            setupValue.vocAlertAutoThreshold = getIntValue(results,11) / 100f;
            setupValue.fgAlertThreshold = getIntValue(results,12) / 100f;
            setupValue.tempHighAlertThreshold = getIntValue(results,13) / 100f;
            setupValue.tempLowAlertThreshold = getIntValue(results,14) / 100f;
            setupValue.humidityHighAlertThreshold = getIntValue(results,15) / 100f;
            setupValue.humidityLowAlertThreshold = getIntValue(results,16) / 100f;
            setupValue.alertSoundLight = getIntValue(results,17) == 1;

        } catch (Exception e) {
            e.printStackTrace();
            setupValue.e = e;
        }
        return setupValue;
    }

    public static void main(String[] args) {
        readSetupValue();
    }

    public static Integer getIntValue(BatchResults br, Object key) {
        Object o = br.getValue(key);
        if(o instanceof Short) {
            return ((Short)o).intValue();
        } else {
            return (Integer)o;
        }
    }
}
