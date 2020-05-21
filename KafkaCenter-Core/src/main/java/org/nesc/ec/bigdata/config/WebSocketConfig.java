package org.nesc.ec.bigdata.config;

import org.apache.catalina.Context;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author Truman.P.Du
 * @date 2020/02/29
 * @description
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 创建wss协议接口
     *
     * @return
     */
    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return context -> context.addServletContainerInitializer(new WsSci(), null);
    }
}
