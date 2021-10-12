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
    private static final String ModbusIP = "127.0.0.1";
    private static final int ModbusPort = 506;
    private static final int ModbusSlaveId = 1;
    private static final int VOCLowerAddress = 40513;
    private static final int VOCUpperAddress = 40518;
    private static final int FlammableGasLowerAddress = 40524;
    private static final int FlammableGasUpperAddress = 40528;
    private static final int TemperatureAAddress = 40534;
    private static final int TemperatureBAddress = 40543;
    private static final int HumidityAAddress = 40534;
    private static final int HumidityBAddress = 40548;

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
            batch.addLocator(0, BaseLocator.holdingRegister(ModbusSlaveId, VOCLowerAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(ModbusSlaveId, VOCUpperAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(ModbusSlaveId, FlammableGasLowerAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(ModbusSlaveId, FlammableGasUpperAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(4, BaseLocator.holdingRegister(ModbusSlaveId, TemperatureAAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(5, BaseLocator.holdingRegister(ModbusSlaveId, TemperatureBAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(6, BaseLocator.holdingRegister(ModbusSlaveId, HumidityAAddress - 1, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(7, BaseLocator.holdingRegister(ModbusSlaveId, HumidityBAddress - 1, DataType.TWO_BYTE_INT_SIGNED));

            ModbusMaster master = getMaster();

            batch.setContiguousRequests(false);
            BatchResults<Integer> results = master.send(batch);

            environmentStatus.vocLowerPart = results.getIntValue(0) / 100f;
            environmentStatus.vocUpperPart = results.getIntValue(1) / 100f;
            environmentStatus.flammableGasLowerPart = results.getIntValue(2);
            environmentStatus.flammableGasUpperPart = results.getIntValue(3);
            environmentStatus.temperatureA = results.getIntValue(4) / 10f;
            environmentStatus.temperatureB = results.getIntValue(5) / 10f;
            environmentStatus.humidityA = results.getIntValue(6) / 10f;
            environmentStatus.humidityB = results.getIntValue(7) / 10f;
        } catch (Exception e) {
            e.printStackTrace();
            environmentStatus.e = e;
        }

        Log.d(TAG, CabinetCore.GSON.toJson(environmentStatus));
        return environmentStatus;
    }

    public synchronized static SetupValue getSetupValue() {
        SetupValue result = new SetupValue();
        return result;
    }
}
