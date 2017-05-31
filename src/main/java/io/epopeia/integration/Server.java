package io.epopeia.integration;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.messaging.Message;

@Profile("server")
@Configuration
public class Server {

    private final Integer port;

    private static final String fromClient = "fromClient";
    private static final String toClient = "toClient";

    @Autowired
    public Server(@Value("${server.port}") Integer port) {
        Objects.nonNull(port);
        this.port = port;
    }

    @Bean
    public AbstractServerConnectionFactory myServer() {
        return new TcpNetServerConnectionFactory(this.port);
    }

    @Bean
    @ServiceActivator(inputChannel = toClient)
    public TcpSendingMessageHandler mySender() {
        final TcpSendingMessageHandler sender = new TcpSendingMessageHandler();
        sender.setConnectionFactory(myServer()); // share the same connections
        return sender;
    }

    @Bean
    public TcpReceivingChannelAdapter myReceiver() {
        final TcpReceivingChannelAdapter receiver = new TcpReceivingChannelAdapter();
        receiver.setConnectionFactory(myServer()); // share the same connections
        receiver.setOutputChannelName(fromClient);
        return receiver;
    }

    @ServiceActivator(inputChannel = fromClient, outputChannel = toClient)
    public Message<byte[]> handleMessageFromClient(Message<byte[]> message) {
        message.getHeaders().forEach((k, v) -> System.out.printf("%s: %s\n", k, v));
        System.out.println("Received from client: " + new String(message.getPayload()));
        return message; // echo the same message to the client
    }
}
