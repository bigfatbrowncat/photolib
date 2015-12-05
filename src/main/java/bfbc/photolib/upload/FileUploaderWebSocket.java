package bfbc.photolib.upload;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import bfbc.photolib.Heap;

@WebSocket(maxBinaryMessageSize = 1024 * 1024 * 64 /* 64M */)
public class FileUploaderWebSocket {
    static File uploadedFile = null;
    static String fileName = null;
    static String title = null;
    static FileOutputStream fos = null;
    final static String filePath = "data/";

    @OnWebSocketConnect
    public void open(Session session) {
        System.out.println("chat ws server open");
    }

    @OnWebSocketMessage
    public void message(Session session, byte buf[], int offset, int length) throws IOException {
        System.out.println("Binary Data");      
        fos.write(buf, offset, length);
    }

    @OnWebSocketMessage
    public void message(Session session, String msg) {
        System.out.println("got msg: " + msg);
        if (msg.length() >= 9 && msg.substring(0, 9).equals("filename:")) {
            fileName = msg.substring(9);
            uploadedFile = new File(filePath + fileName);
            try {
                fos = new FileOutputStream(uploadedFile);
            } catch (FileNotFoundException e) {     
                e.printStackTrace();
            }
        } else if (msg.length() >= 6 && msg.substring(0, 6).equals("title:")) {
        	title = msg.substring(6);
        } else if (msg.equals("end")) {
            try {
                fos.flush();
                fos.close();
                session.getRemote().sendString("complete");
                
                Heap heap = Heap.getInstanceFor(null);
                Heap.Image newImage = heap.new Image(title);
                heap.getImages().add(newImage);
                newImage.getFiles().add(newImage.new File(fileName, "image/jpeg"));	// TODO Get actual type
            } catch (IOException e) {       
                e.printStackTrace();
            }
        } else {
        	throw new RuntimeException("Invalid request: " + msg);
        }
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        System.out.println("socket closed: "+ reason);
    }

    @OnWebSocketError
    public void error(Session session, Throwable t) {
        t.printStackTrace();

    }
}