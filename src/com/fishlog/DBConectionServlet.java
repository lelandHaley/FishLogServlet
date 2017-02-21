package com.fishlog;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;


/**
 * Servlet implementation class DBConectionServlet
 */
@WebServlet("/DBConectionServlet")
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
			//Bitmap image = request.getParameter("recImage");
			String fromClientphoto= request.getParameter("recImage");
			byte[] b = fromClientphoto.getBytes();

			FileOutputStream fos = new FileOutputStream("./testImg.jpg");
			fos.write(b);
		    fos.close(); 
			
			
			
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
