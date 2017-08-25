package com.fishlog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.gson.Gson;


/**
 * Servlet implementation class DBConectionServlet
 */
@WebServlet("/DBConectionServlet")
@MultipartConfig
public class DBConectionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DBConectionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		
		System.out.println("doPost"); 
		String function = request.getParameter("func");
		System.out.println("function is :" + function + ":");
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		if(function.equals("insertRecord")){
			ServletContext sc = this.getServletContext(); 
			String name = request.getParameter("recName");
			String lat = request.getParameter("recLat");
			String lon = request.getParameter("recLon");
			String lure = request.getParameter("recLure");
			String weather = request.getParameter("recWeather");
			String species = request.getParameter("recSpecies");
			
			
			/*
			 * This assumes the request is of type multipart/form-data,
			 * and that there is only one file included.  If you want to
			 * include multiple files, you can use 'getParts', and then look
			 * for each one that is of type file.
			 */
			Part filePart = request.getPart("recImage");
			Files.copy( filePart.getInputStream(), Paths.get(filePart.getSubmittedFileName()) );			
			
			
			//response.setContentType("application/xml");
			DBAccess dba = new DBAccess();
			boolean flag = dba.insertRecord(name, lat, lon, lure, weather, species);
	
			//request.setAttribute("flag", flag);
			
			if(flag){	
				Gson gson = new Gson();
				String jsonStr = gson.toJson("success");
				response.getWriter().write(jsonStr);				
			}
			else{
				Gson gson = new Gson();
				String jsonStr = gson.toJson("failure");
				response.getWriter().write(jsonStr);
			}
		}
	}

}
