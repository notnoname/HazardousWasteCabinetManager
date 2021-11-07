package me.liuzs.cabinetmanager.model;

public class DepositRecord {
    public long localId = -1;
    public String id;
    public String storage_no;
    public String storage_name;
    public String container_size;
    public String laboratory_id;
    public String laboratory;
    public String input_operator;
    public String output_operator;
    public String output_time;
    public String input_time;
    public String harmful_infos;
    public String remark;
    public String input_weight;
    public transient boolean has_input_weight = false;
    public String output_weight;
    public transient boolean has_output_weight = false;
    public String storage_rack;
    public transient boolean has_storage_rack = false;
    public String storage_id;
    public transient boolean isSent;

}
