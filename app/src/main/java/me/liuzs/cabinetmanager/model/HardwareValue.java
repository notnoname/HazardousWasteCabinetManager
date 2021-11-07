package me.liuzs.cabinetmanager.model;

import me.liuzs.cabinetmanager.model.modbus.AirConditionerStatus;
import me.liuzs.cabinetmanager.model.modbus.EnvironmentStatus;
import me.liuzs.cabinetmanager.model.modbus.FrequencyConverterStatus;
import me.liuzs.cabinetmanager.model.modbus.SetupValue;
import me.liuzs.cabinetmanager.model.modbus.StatusOption;

public class HardwareValue {
    public long id = -1;
    public String cabinet_id;
    public SetupValue setupValue;
    public EnvironmentStatus environmentStatus;
    public AirConditionerStatus airConditionerStatus;
    public FrequencyConverterStatus frequencyConverterStatus;
    public StatusOption statusOption;
    public long createTime;
}
