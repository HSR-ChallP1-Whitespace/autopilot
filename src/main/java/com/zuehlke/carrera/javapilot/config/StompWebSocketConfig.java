package com.zuehlke.carrera.javapilot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker ( MessageBrokerRegistry config ) {
        config.enableSimpleBroker("/topic");

        /**
         * The given channel-prefixes are used to filter out incoming
         * SEND commands. Only those matching this filter will be handed over to the
         * Controllers for further processing.
         *
         * A client should therefore send a message to a destination channel like:
         *
         * channel: /app/echo
         *
         * Where '/echo' is the actual mapping in the controllers.
         *
         * */
        config.setApplicationDestinationPrefixes("/app"); // The client has
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Register a pure WebSocket endpoint (ws://myserver.com/ws/rest/messages)
        registry.addEndpoint("/messages");

        // Register additional fallback handling using sock-js (http://myserver.com/ws/rest/messages)
        registry.addEndpoint("/messages").withSockJS();

    }
}
