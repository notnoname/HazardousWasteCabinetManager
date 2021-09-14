package me.liuzs.cabinetmanager.model;

import java.util.LinkedList;
import java.util.List;

import me.liuxy.cabinet.SubBoard;

public class HardwareValue {
    public static HardwareValue _Cache;
    public final List<SubBoard.StatusData> subBoardStatusData = new LinkedList<>();
    public TVOCsValue tvoCsValue;
    public boolean greenLight;
    public boolean redLight;
    public boolean fan;
    //单柜Locker状态，多副柜情况下一直未True。
    public boolean lock;
    public long createTime;
    public long recordTime;
}
