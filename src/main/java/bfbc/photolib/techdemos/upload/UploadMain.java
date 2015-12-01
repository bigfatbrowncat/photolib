package bfbc.photolib.techdemos.upload;


import static spark.Spark.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletResponse;

import bfbc.photolib.techdemos.status.StatusMain;

public class UploadMain {
    public static void main(String[] args) {
    	port(8080);
    	
    	webSocket("/receive/fileserver", FileServer.class);
    	
    	String rootPath = "/" + UploadMain.class.getPackage().getName().replace('.', '/') + "/root";
    	staticFileLocation(rootPath);
		
    	
    	init();
    }
}