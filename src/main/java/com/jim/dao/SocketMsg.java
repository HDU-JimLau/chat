package com.jim.dao;

import lombok.Data;

/**
 * @author JimLau
 * @version 1.0
 * @date 2020/2/26 13:35
 */

@Data
public class SocketMsg {

    //聊天类型
    private int type;

    //消息发送者
    private String FromUser;

    //消息接收者
    private String ToUser;

    //消息体
    private String msg;


}
