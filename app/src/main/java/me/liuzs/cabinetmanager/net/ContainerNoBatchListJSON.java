package me.liuzs.cabinetmanager.net;

import java.util.List;

import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;

public class ContainerNoBatchListJSON {
    public List<ContainerNoBatchInfo> list;
    /**
     * 每页记录数
     */
    public int page_size;
    /**
     * 页数
     */
    public int page_count;
    /**
     * 第几页
     */
    public int page_index;
}
