package com.quokka.Chat_Service.configuration;


import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;

@Configuration
public class SocketIOConfig {
    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setPort(8099);
        configuration.setOrigin("*");

        // Bổ sung bắt buộc để xử lý Unicode (tiếng Việt)
        configuration.setTransports(Transport.WEBSOCKET);
        configuration.getSocketConfig().setReuseAddress(true);
        configuration.getSocketConfig().setTcpNoDelay(true);
        configuration.getSocketConfig().setTcpKeepAlive(true);


        System.setProperty("io.netty.defaultCharset", "UTF-8");

        return new SocketIOServer(configuration);
    }
}
