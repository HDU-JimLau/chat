package com.jim.websocketServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jim.dao.SocketMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author JimLau
 * @version 1.0
 * @date 2020/2/26 10:57
 */

@ServerEndpoint("/myWs/{nickname}")
@Component
@Slf4j
public class MyWebSocket {

    //用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();


    //用来记录sessionId和该 session绑定
    private static Map<String,Session> map=new HashMap<>();

    //与某个客户端的连接会话，通过他给客户端发送数据
    private Session session;

    //用户昵称
    private String name;

    /**
     * 连接建立成功调用的方法
     *
     * @param session
     */
    @OnOpen
    public void OnOpen(Session session,@PathParam("nickname") String nickname) {
        this.session = session;
        this.name=nickname;
        //加入 set
        webSocketSet.add(this);

        //加入map
        if (!map.containsKey(session.getId())){
            map.put(session.getId(),session);
        }

        //连接成功
        log.info("有新连接加入:"+nickname+",当前在线人数为" + webSocketSet.size());
        this.session.getAsyncRemote().sendText("恭喜您成功连接上WebSocket-->当前在线人数为：" + webSocketSet.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void OnClose() {

        //移除该客户端
        webSocketSet.remove(this);
        log.info("有一连接关闭，当前在线人数为" + webSocketSet.size());
        broadCast("有一连接关闭，当前在线人数为" + webSocketSet.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param session
     * @param message
     * @param nickname
     */

    @OnMessage
    public void OnMessage(Session session, String message,@PathParam("nickname") String nickname) {

        ObjectMapper objectMapper = new ObjectMapper();
        SocketMsg socketMsg;
        try {
            socketMsg=objectMapper.readValue(message,SocketMsg.class);
            if(socketMsg.getType()==1){
                //单聊
                socketMsg.setFromUser(session.getId());
                Session formSession = map.get(socketMsg.getFromUser());
                Session toSession = map.get(socketMsg.getToUser());

                //发送给接受者
                if(toSession!=null){
                    formSession.getAsyncRemote().sendText(nickname+"频道号："+formSession.getId()+""+socketMsg.getMsg());
                    toSession.getAsyncRemote().sendText(nickname+"频道号："+formSession.getId()+""+socketMsg.getMsg());
                }else {
                    //发送给发送者.
                    formSession.getAsyncRemote().sendText("系统消息：对方不在线或者您输入的频道号不对");
                }

            }else{
                //群发消息
                broadCast(nickname+"频道号为："+session.getId()+": "+socketMsg.getMsg());
            }

        }catch (Exception e){

        }
    }

    @OnError
    public void OnError(Session session, Throwable error) {
        error.printStackTrace();
    }


    /**
     * 出现错误
     * @param message
     */
    public void broadCast(String message) {
        for (MyWebSocket myWebSocket : webSocketSet) {
            //同步发或者异步发送消息
            myWebSocket.session.getAsyncRemote().sendText(message);
        }
    }


}
