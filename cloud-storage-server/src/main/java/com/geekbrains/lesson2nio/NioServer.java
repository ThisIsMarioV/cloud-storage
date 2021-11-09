package com.geekbrains.lesson2nio;

import com.geekbrains.Handler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
public class NioServer {
    private ByteBuffer byteBuffer;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public NioServer() throws IOException {
        byteBuffer = ByteBuffer.allocate(10);
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8189));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.debug("Server started...");
            //Handler handler = new Handler(serverSocketChannel);
            while (serverSocketChannel.isOpen()){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key : selectionKeys){
                    if(key.isAcceptable()){
                        handleAccept();
                    }
                    if(key.isReadable()){
                        handleRead(key);
                    }
                }
                selectionKeys.clear();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder stringBuilder = new StringBuilder("From server: ");

        while (true){
            int read = channel.read(byteBuffer);
            if(read == -1){
                channel.close();
                return;
            }
            if( read == 0){
                break;
            }
            if(read > 0){

                byteBuffer.flip();
                while (byteBuffer.hasRemaining()){
                    stringBuilder.append((char) byteBuffer.get());
                }
                byteBuffer.clear();
            }
        }
        // trim(); - чистит лишние пробелы и добавляет \n\r
        log.debug("Message received: {}", stringBuilder);
        channel.write(ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        // send welcome message for user
        log.debug("Client connected...");
    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }
}
