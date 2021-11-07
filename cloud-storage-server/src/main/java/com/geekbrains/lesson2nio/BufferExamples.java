package com.geekbrains.lesson2nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class BufferExamples {
    public static void main(String[] args) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        byte ch = 'a';
        for (int i = 0; i < 3; i++) {
            buffer.put((byte)(ch + i));
        }
        //buffer.flip(); // ставит позицию в лимит, если остановилис на 3-м элементе, для следующих будет только 2. Запись переводим в чтение
        //buffer.rewind(); // сбрасывает позицию. даем читать, не меняя буфер
        //buffer.clear(); // чистит буфер. закончили работу, создаем новый
        while (buffer.hasRemaining()){
            System.out.println((char) buffer.get());
        }
        buffer.clear();
        Path text = Paths.get("cloud-storage-server", "server" , "root", "Hello.txt");
        SeekableByteChannel seekableByteChannel = Files.newByteChannel(text);
        System.out.println(seekableByteChannel);
        byte[] result = new byte[(int) seekableByteChannel.size()];
        int pos =0;
        while (true){
            int read = seekableByteChannel.read(buffer);
            if(read<=0){
                break;
            }
            buffer.flip(); // достаем из буфера
            while (buffer.hasRemaining()){
                result[pos] = buffer.get();
                pos++;
            }
            buffer.clear();
        }
        System.out.println(new String(result, StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in); // alt+enter автосоздание переменной
        Path copy = Paths.get("cloud-storage-server", "server" , "root", "copy.txt");
        SeekableByteChannel copyChannel = Files.newByteChannel(copy, StandardOpenOption.WRITE);

        buffer.clear();
        byte[] bytes = "Hello is my system!".getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i <bytes.length ; i++) {
            buffer.put(bytes[i]);
            if(i%5 ==0){
                buffer.flip();
                copyChannel.write(buffer);
                buffer.clear();
            }
            if(i == (bytes.length -1) && (i+1)%5!=0){
                buffer.flip();
                copyChannel.write(buffer);

            }
            
        }
    }
}
