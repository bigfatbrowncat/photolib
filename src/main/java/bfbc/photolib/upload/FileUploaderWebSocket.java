package bfbc.photolib.upload;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket(maxBinaryMessageSize = 1024 * 1024 * 64 /* 64M */)
public class FileUploaderWebSocket {
    static File uploadedFile = null;
    static String fileName = null;
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
        if(!msg.equals("end")) {
            fileName=msg.substring(msg.indexOf(':')+1);
            uploadedFile = new File(filePath+fileName);
            try {
                fos = new FileOutputStream(uploadedFile);
            } catch (FileNotFoundException e) {     
                e.printStackTrace();
            }
        } else {
            try {
                fos.flush();
                fos.close();
                session.getRemote().sendString("complete");
            } catch (IOException e) {       
                e.printStackTrace();
            }
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