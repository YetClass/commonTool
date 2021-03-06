package com.zhouzifei.tool.dto;

import lombok.Data;

/**
 * @author 周子斐 (17600004572@163.com)
 * @remark 2021/1/25

 * @Description
 */
@Data
public class VideoUrlDTO {
    private int id;
    private String metareferer;
    private String code;
    private String parser;
    private String success;
    private String type;
    private String url;
    private String flash;
    private String player;
    private String domain;
    private String originalUrl;
    private String prefixUrl;
    private String prefixType;
    private String msg;
}
