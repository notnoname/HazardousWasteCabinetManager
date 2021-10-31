package me.liuzs.cabinetmanager.net;

import java.util.List;

import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;

public class ContainerNoBatchListJSON {
    public List<ContainerNoBatchInfo> batches;
    /**
     * 每页记录数
     */
    public int page_size;
    /**
     * 总记录数
     */
    public int total_count;
    /**
     * 第几页,1开始
     */
    public int current_page;

    /**
     * 总页数
     */
    public int total_pages;
}
