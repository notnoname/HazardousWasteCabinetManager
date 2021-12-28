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

import java.util.Objects;

import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.model.modbus.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.modbus.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.modbus.FrequencyConverterStatus;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.model.modbus.StatusOption;

@SuppressWarnings("unused")
public class ModbusService {
    private static final String TAG = "ModbusService";
    private static final int ModbusPort = 502;
    private static final int ModbusSlaveId = 1;
    /**
     * 工厂。
     */
    private final static ModbusFactory mModbusFactory = new ModbusFactory();
    private static String ModbusIP = CabinetCore.getModbusAddress();

    public static String getModbusIP() {
        return ModbusIP + ":" + ModbusPort + "(" + ModbusSlaveId + ")";
    }

    public static void setModbusAddress(String address) {
        ModbusIP = address;
    }

    /**
     * 获取master
     *
     * @return ModbusMaster
     * @throws ModbusInitException exception
     */
    private synchronized static ModbusMaster getMaster() throws ModbusInitException {
        IpParameters params = new IpParameters();
        params.setHost(ModbusIP);
        params.setPort(ModbusPort);
        // modbusFactory.createRtuMaster(wapper); //RTU 协议
        // modbusFactory.createUdpMaster(params);//UDP 协议
        // modbusFactory.createAsciiMaster(wrapper);//ASCII 协议
        ModbusMaster mMaster = mModbusFactory.createTcpMaster(params, false);// TCP 协议
        mMaster.setTimeout(500);
        mMaster.init();
        return mMaster;
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
        return getMaster().getValue(loc);
    }

    public static void writeCoilStatus(int offset, boolean value) throws ModbusInitException, ErrorResponseException, ModbusTransportException {
        BaseLocator<Boolean> loc = BaseLocator.coilStatus(ModbusSlaveId, offset);
        getMaster().setValue(loc, value);
    }

    public static void writeHoldingRegister(int offset, Integer value) throws ModbusInitException, ErrorResponseException, ModbusTransportException {
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
        return getMaster().getValue(loc);
    }

    /**
     * 读取[03 Holding Register类型 2x]模拟量数据
     *
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
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
    private static Number readInputRegisters(int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 04 Input Registers类型数据读取
        BaseLocator<Number> loc = BaseLocator.inputRegister(ModbusSlaveId, offset, dataType);
        return getMaster().getValue(loc);
    }

    /**
     * 读取变频器状态
     */
    public synchronized static FrequencyConverterStatus readFrequencyConverterStatus() {
        FrequencyConverterStatus frequencyConverterStatus = new FrequencyConverterStatus();

        try {
            BatchRead<Integer> batch = new BatchRead<>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, FrequencyConverterStatus.FCStatusAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, FrequencyConverterStatus.FCRotatingSpeedAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, FrequencyConverterStatus.FCFrequencyAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, FrequencyConverterStatus.FCFrequencyTargetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

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
        return frequencyConverterStatus;
    }

    /**
     * 读取告警状态
     */
    public synchronized static StatusOption readStatusOption() {
        StatusOption statusOption = new StatusOption();

        try {
            BatchRead<Integer> batch = new BatchRead<>();
            batch.addLocator(0, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertVOCAddress - 1));
            batch.addLocator(1, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertFGAddress - 1));
            batch.addLocator(2, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertTempHighAddress - 1));
            batch.addLocator(3, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertHumidityHighAddress - 1));
            batch.addLocator(4, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertTempLowAddress - 1));
            batch.addLocator(5, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertHumidityLowAddress - 1));
            batch.addLocator(6, BaseLocator.coilStatus(ModbusSlaveId, StatusOption.AlertFireAddress - 1));

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
            statusOption.fanWorkModel = StatusOption.FanWorkModel.values()[readHoldingRegister(StatusOption.UnionWorkModelAddress - 1, DataType.TWO_BYTE_INT_SIGNED).intValue()];

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
            BatchRead<Integer> batch = new BatchRead<>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.VOCLowerAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.VOCUpperAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.FGLowerAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.FGUpperAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.TemperatureAAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.TemperatureBAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.HumidityAAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(7, BaseLocator.holdingRegister(ModbusSlaveId, EnvironmentStatus.HumidityBAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);

            environmentStatus.vocLowerPart = Objects.requireNonNull(getIntValue(results, 0)).floatValue();
            environmentStatus.vocUpperPart = Objects.requireNonNull(getIntValue(results, 1)).floatValue();
            environmentStatus.fgLowerPart = Objects.requireNonNull(getIntValue(results, 2)).floatValue();
            environmentStatus.fgUpperPart = getIntValue(results, 3);
            environmentStatus.tempA = getIntValue(results, 4) / 10f;
            environmentStatus.tempB = getIntValue(results, 5) / 10f;
            environmentStatus.humidityA = getIntValue(results, 6) / 10f;
            environmentStatus.humidityB = getIntValue(results, 7) / 10f;
        } catch (Exception e) {
            e.printStackTrace();
            environmentStatus.e = e;
        }
        return environmentStatus;
    }

    /**
     * 读取环境数据
     */
    public synchronized static AirConditionerStatus readAirConditionerStatus() {
        AirConditionerStatus airConditionerStatus = new AirConditionerStatus();

        try {
            BatchRead<Integer> batch = new BatchRead<>();
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACPowerSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACCtrlModelSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACWorkModelSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACTargetTempSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACFanSpeedModelSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACFanSweepSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, AirConditionerStatus.ACRemoteWorkModelSetAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);

            airConditionerStatus.powerOn = getIntValue(results, 0) == 1;
            airConditionerStatus.autoCtrl = getIntValue(results, 1) == 1;
            airConditionerStatus.workModel = AirConditionerStatus.WorkModel.values()[getIntValue(results, 2)];
            airConditionerStatus.targetTemp = getIntValue(results, 3);
            airConditionerStatus.fanSpeedModel = AirConditionerStatus.FanSpeedModel.values()[getIntValue(results, 4)];
            airConditionerStatus.fanSweep = getIntValue(results, 5) == 1;
            airConditionerStatus.remoteWorkModel = AirConditionerStatus.RemoteWorkModel.values()[getIntValue(results, 6)];
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
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.VOCUnionMaxAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.VOCUnionMinAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.FanUnionWorkTimeAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.FanUnionStopTimeAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.FanUnionFrequencyAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.VOCAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.TempHighAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(7, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.TempLowAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(8, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.HumidityHighAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(9, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.HumidityLowAlertAutoAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(10, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.VOCAlertAutoThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(11, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.FGAlertAutoThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(12, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.TempHighAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(13, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.TempLowAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(14, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.HumidityHighAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(15, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.HumidityLowAlertThresholdAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(16, BaseLocator.holdingRegister(ModbusSlaveId, SetupValue.AlertSoundLightAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);
            setupValue.vocUnionMax = getIntValue(results, 0).floatValue();
            setupValue.vocUnionMin = getIntValue(results, 1).floatValue();
            setupValue.fanUnionWorkTime = getIntValue(results, 2);
            setupValue.fanUnionStopTime = getIntValue(results, 3);
            setupValue.fanUnionFrequency = getIntValue(results, 4) / 100f;
            setupValue.vocAlertAuto = getIntValue(results, 5) == 1;
            setupValue.tempHighAlertAuto = getIntValue(results, 6) == 1;
            setupValue.tempLowAlertAuto = getIntValue(results, 7) == 1;
            setupValue.humidityHighAlertAuto = getIntValue(results, 8) == 1;
            setupValue.humidityLowAlertAuto = getIntValue(results, 9) == 1;
            setupValue.vocAlertAutoThreshold = getIntValue(results, 10).floatValue();
            setupValue.fgAlertThreshold = getIntValue(results, 11).floatValue();
            setupValue.tempHighAlertThreshold = getIntValue(results, 12) / 10f;
            setupValue.tempLowAlertThreshold = getIntValue(results, 13) / 10f;
            setupValue.humidityHighAlertThreshold = getIntValue(results, 14) / 10f;
            setupValue.humidityLowAlertThreshold = getIntValue(results, 15) / 10f;
            setupValue.alertSoundLight = getIntValue(results, 16) == 1;

        } catch (Exception e) {
            e.printStackTrace();
            setupValue.e = e;
        }
        return setupValue;
    }

    public synchronized static boolean saveSetupValue(SetupValue setupValue) {
        try {
            if (setupValue.vocUnionMax != null)
                writeHoldingRegister(SetupValue.VOCUnionMaxAddress - 1, setupValue.vocUnionMax.intValue());
            if (setupValue.vocUnionMin != null)
                writeHoldingRegister(SetupValue.VOCUnionMinAddress - 1, setupValue.vocUnionMin.intValue());
            if (setupValue.fanUnionWorkTime != null)
                writeHoldingRegister(SetupValue.FanUnionWorkTimeAddress - 1, setupValue.fanUnionWorkTime);
            if (setupValue.fanUnionStopTime != null)
                writeHoldingRegister(SetupValue.FanUnionStopTimeAddress - 1, setupValue.fanUnionStopTime);
            if (setupValue.fanUnionFrequency != null)
                writeHoldingRegister(SetupValue.FanUnionFrequencyAddress - 1, (int) (setupValue.fanUnionFrequency * 100));
            if (setupValue.vocAlertAuto != null)
                writeHoldingRegister(SetupValue.VOCAlertAutoAddress - 1, setupValue.vocAlertAuto ? 1 : 0);
            if (setupValue.tempHighAlertAuto != null)
                writeHoldingRegister(SetupValue.TempHighAlertAutoAddress - 1, setupValue.tempHighAlertAuto ? 1 : 0);
            if (setupValue.tempLowAlertAuto != null)
                writeHoldingRegister(SetupValue.TempLowAlertAutoAddress - 1, setupValue.tempLowAlertAuto ? 1 : 0);
            if (setupValue.humidityHighAlertAuto != null)
                writeHoldingRegister(SetupValue.HumidityHighAlertAutoAddress - 1, setupValue.humidityHighAlertAuto ? 1 : 0);
            if (setupValue.humidityLowAlertAuto != null)
                writeHoldingRegister(SetupValue.HumidityLowAlertAutoAddress - 1, setupValue.humidityLowAlertAuto ? 1 : 0);
            if (setupValue.vocAlertAutoThreshold != null)
                writeHoldingRegister(SetupValue.VOCAlertAutoThresholdAddress - 1, setupValue.vocAlertAutoThreshold.intValue());
            if (setupValue.fgAlertThreshold != null)
                writeHoldingRegister(SetupValue.FGAlertAutoThresholdAddress - 1, (int) (setupValue.fgAlertThreshold).intValue());
            if (setupValue.tempHighAlertThreshold != null)
                writeHoldingRegister(SetupValue.TempHighAlertThresholdAddress - 1, (int) (setupValue.tempHighAlertThreshold * 10));
            if (setupValue.tempLowAlertThreshold != null)
                writeHoldingRegister(SetupValue.TempLowAlertThresholdAddress - 1, (int) (setupValue.tempLowAlertThreshold * 10));
            if (setupValue.humidityHighAlertThreshold != null)
                writeHoldingRegister(SetupValue.HumidityHighAlertThresholdAddress - 1, (int) (setupValue.humidityHighAlertThreshold * 10));
            if (setupValue.humidityLowAlertThreshold != null)
                writeHoldingRegister(SetupValue.HumidityLowAlertThresholdAddress - 1, (int) (setupValue.humidityLowAlertThreshold * 10));
            if (setupValue.alertSoundLight != null)
                writeHoldingRegister(SetupValue.AlertSoundLightAddress - 1, setupValue.alertSoundLight ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 设置硬件保持寄存器值
     */
    public synchronized static boolean setHardwareHoldingRegisterOption(int address, int value) {
        try {
            writeHoldingRegister(address - 1, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 设置硬件线圈值
     */
    public synchronized static boolean setHardwareCoilStatusOption(int address, boolean value) {
        try {
            writeCoilStatus(address - 1, value);
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
        try {
            Object o = br.getValue(key);
            if (o instanceof Short) {
                return ((Short) o).intValue();
            } else {
                return (Integer) o;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
