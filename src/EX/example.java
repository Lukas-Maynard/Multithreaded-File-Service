//package file_service;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//
//public class example {
//    // DOWNLOAD
//    String fileName;
//    FileInputStream fs = new FileInputStream("ServerStorage/" + fileName);
//    FileChannel fc = fs.getChannel();
//    ByteBuffer content = ByteBuffer.allocate(1000);
//
//    while(fc.read(content) >= 0){
//        content.flip();
//        serverChannel.write(content);
//        content.clear();
//    }
//    serverChannel.shutdownOutput();
//
//
//    // CLIENT DOWNLOAD
//    FileOutputStream fs = new FileOutputStream("ClientStorage/" + fileName, true);
//    FileChannel fc = fs.getChannel();
//    ByteBuffer reply = ByteBuffer.allocate(1000);
//
//    while(channel.read(reply) >= 0){
//        reply.flip();
//        fc.write(reply);
//        reply.clear();
//    }
//}
