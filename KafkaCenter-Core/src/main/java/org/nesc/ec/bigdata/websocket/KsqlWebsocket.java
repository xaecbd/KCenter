package org.nesc.ec.bigdata.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.nesc.ec.bigdata.model.WebSocketMessage;
import org.nesc.ec.bigdata.service.KsqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * @author Truman.P.Du
 * @date 2020/03/06
 * @description
 */
@ServerEndpoint("/ksql_console")
@Component
public class KsqlWebsocket {
    private final static Logger LOGGER = LoggerFactory.getLogger(KsqlWebsocket.class);
    private static KsqlService ksqlService;
    private Gson gson = new Gson();

    @Autowired
    public void setKsqlService(KsqlService ksqlService) {
        KsqlWebsocket.ksqlService = ksqlService;
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("client open: {}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        ksqlService.stopQuery(session);
        LOGGER.info("client close: {}", sessionId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("", throwable);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            WebSocketMessage webSocketMessage = null;
            try {
                webSocketMessage = gson.fromJson(message, WebSocketMessage.class);
            } catch (JsonSyntaxException e) {
            }

            if (webSocketMessage != null) {
                if ("run".equalsIgnoreCase(webSocketMessage.getOperate())) {
                    ksqlService.executeConsole(webSocketMessage, session);
                } else if ("stop".equalsIgnoreCase(webSocketMessage.getOperate())) {
                    ksqlService.stopQuery(session);
                } else {
                    LOGGER.info("received unused message: {}", message);
                }
            }
        } catch (Exception e) {
            LOGGER.error("onMessage message:{} error", message, e);
        }
    }
}
