package me.liuzs.cabinetmanager.net;

import java.util.List;

import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;

public class ContainerNoBatchListJSON {
    public List<ContainerNoBatchInfo> list;
    /**
     * 每页记录数
     */
    public int pageSize;
    /**
     * 页数
     */
    public int pageCount;
    /**
     * 第几页
     */
    public int pageIndex;
}
