package bfbc.photolib.techdemos.status;


import static spark.Spark.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletResponse;

public class StatusMain {
    public static void main(String[] args) {
    	port(8080);
    	
    	webSocket("/status", StatusWebSocket.class);
    	
    	String rootPath = "/" + StatusMain.class.getPackage().getName().replace('.', '/') + "/root";
    	staticFileLocation(rootPath);
		
		get("/img/:name", (request, response) -> {
			
			String filePath = "data" + File.separator + request.params(":name");
			
			response.type("image/jpeg");
			byte[] bytes = Files.readAllBytes(Paths.get(filePath));         
			HttpServletResponse raw = response.raw();

			raw.getOutputStream().write(bytes);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();

			return raw;
		});
    }
}