package bfbc.photolib.techdemos.status;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class StatusWebSocket {

	private static List<String> strings = new ArrayList<String>();
	private Gson gson;
	
	private void update(Session s) {
		try {
			String json = gson.toJson(strings);
			s.getRemote().sendString(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void broadcastUpdate() {
		synchronized (strings) {
    		List<Session> ss = new ArrayList<Session>(sessions);
    		for (Session s : ss) {
    				update(s);
    		}

		}
	}
	
	private int i = 0;
	
    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    public StatusWebSocket() {
    	gson = new GsonBuilder().create();
	}
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        update(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
		synchronized (strings) {
			strings.add(message);
		}
    	
    	broadcastUpdate();
    	
    }

}
