package me.liuzs.cabinetmanager.service;

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

public class ModbusService {
    private static final String ModbusIP = "127.0.0.1";
    private static final int ModbusPort = 506;
    private static final int ModbusSlaveId = 1;

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
     * 批量读取使用方法
     *
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     * @throws ModbusInitException      异常
     */
    public static void batchRead() throws ModbusTransportException, ErrorResponseException, ModbusInitException {

        BatchRead<Integer> batch = new BatchRead<Integer>();

        batch.addLocator(0, BaseLocator.holdingRegister(1, 1, DataType.FOUR_BYTE_FLOAT));
        batch.addLocator(1, BaseLocator.inputStatus(1, 0));

        ModbusMaster master = getMaster();

        batch.setContiguousRequests(false);
        BatchResults<Integer> results = master.send(batch);
        System.out.println(results.getValue(0));
        System.out.println(results.getValue(1));
    }
}
