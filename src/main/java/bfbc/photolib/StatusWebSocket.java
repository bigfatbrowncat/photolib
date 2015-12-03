package bfbc.photolib;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class StatusWebSocket {

	private List<String> getFileNames() {
		Heap heap = Heap.getInstanceFor(this);
		List<Heap.Image> imgs = heap.getImages();
		ArrayList<String> fileNames = new ArrayList<>();
		for (Heap.Image img : imgs) {
			fileNames.add(img.getFiles().get(0).getName());
		}
		return fileNames;
	}
	
	//private static List<String> strings = new ArrayList<String>();
	private Gson gson;
	
	private void update(Session s, String request) {
		System.out.println("request: " + request);
	}

	private void update(Session s, List<String> fileNames) {
		try {
			
			String json = gson.toJson(fileNames);
			s.getRemote().sendString(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void broadcastUpdate(String request) {
		synchronized (Heap.getInstanceFor(this)) {
    		List<Session> ss = new ArrayList<Session>(sessions);
    		for (Session s : ss) {
    				update(s, request);
    		}

		}
	}
	
	public void broadcastUpdate() {		
		List<String> fileNames = getFileNames();
		
		synchronized (Heap.getInstanceFor(this)) {
    		List<Session> ss = new ArrayList<Session>(sessions);
    		for (Session s : ss) {
    				update(s, fileNames);
    		}

		}
	}
	
    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    public StatusWebSocket() {
    	gson = new GsonBuilder().create();
	}
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        update(session, getFileNames());
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	
    }

}
