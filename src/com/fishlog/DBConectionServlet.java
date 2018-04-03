package com.fishlog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

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
		response.getWriter().append("Served at: ").append(request.getLocalAddr()).append(request.getContextPath());
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
			String user = request.getParameter("curUser");
			String species = request.getParameter("recSpecies");
			String temp = request.getParameter("recTemp");
			String path = request.getParameter("recPath");
			//Bitmap image = request.getParameter("recImage");
			String fromClientphoto= request.getParameter("recImage");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
			String time = sdf.format(new Date());


			//	        byte[] b = fromClientphoto.getBytes();
			//
			//			FileOutputStream fos = new FileOutputStream("./testImg.jpg");
			//			fos.write(b);
			//			fos.close(); 

			// Part filePart = request.getPart("recImage");
			//Files.copy( filePart.getInputStream(), Paths.get(filePart.getSubmittedFileName()) );

			//response.setContentType("application/xml");
			DBAccess dba = new DBAccess();
			//boolean flag = dba.insertRecord(name, lat, lon, lure, weather, species);
			boolean flag = dba.insertRecord(name, lat, lon, lure, weather, species, time, temp, path, user);
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
		else if(function.equals("createUser")){
			ServletContext sc = this.getServletContext(); 
			String uname = request.getParameter("uname");
			String pword = request.getParameter("pword");
			String email = request.getParameter("email");
			String fromAddr = "wildlogicapps@gmail.com";
			String welcomSubject = "Welcome to the Fish Log Community!";
			String welcomeBody = "Thank you for joining our community of anglers!\nWe hope that this app "
					+ "will aid you in keeping track of the fish you have caught, take some of the guesswork out "
					+ "of fishing, and help you catch more fish. Please feel free to reach out with any issues or "
					+ "concerns to wildlogicapps@gmail.com\n\nThanks and Good Luck out there!\n-WildLogic";

			DBAccess dba = new DBAccess();
			int flag = dba.createUser(uname, pword, email);

			//request.setAttribute("flag", flag);

			//if(flag == 0){	
			Gson gson = new Gson();
			String jsonStr = gson.toJson(flag);
			if(flag == 0){
				Emailer e = new Emailer();
				e.sendEmail(fromAddr, email, welcomSubject, welcomeBody);
			}
			//response.setStatus(flag);
			response.getWriter().write(" responsCode:" + jsonStr);				
			//				}
			//				else{
			//					Gson gson = new Gson();
			//					String jsonStr = gson.toJson(Integer.toString(flag));
			//					response.getWriter().write(jsonStr);
			//				}
		}else if(function.equals("checkLogin")){
			ServletContext sc = this.getServletContext(); 
			String uname = request.getParameter("uname");
			String pword = request.getParameter("pword");

			DBAccess dba = new DBAccess();
			int flag = dba.checkLogin(uname, pword);
			Gson gson = new Gson();
			String jsonStr = gson.toJson(flag);
			response.getWriter().write(" responsCode:" + jsonStr);				
		}else if(function.equals("getRecords")){

			ServletContext sc = this.getServletContext(); 
			String uname = request.getParameter("uname");
			String filter = request.getParameter("filter");
			String filterSpecifics = request.getParameter("filterSpecifics");
			String filterSpecifics2 = request.getParameter("filterSpecifics2");
			String lat = request.getParameter("latitude");
			String lon = request.getParameter("longitude");


			DBAccess dba = new DBAccess();
			ArrayList<String> results = dba.getRecords(uname, filter,filterSpecifics, filterSpecifics2, lat, lon);

			Gson gson = new Gson();
			String jsonStr = gson.toJson(results);
			response.getWriter().write(" responsCode:" + jsonStr);
		}else if(function.equals("editRecord")){

			String uname = request.getParameter("uname");
			String newName = request.getParameter("newName");
			String newLat = request.getParameter("newLat");
			String newLon = request.getParameter("newLon");
			String newLure = request.getParameter("newLure");
			String newWeather = request.getParameter("newWeather");
			String newSpecies = request.getParameter("newSpecies");
			String newTime = request.getParameter("newTime");
			String newTemp = request.getParameter("newTemp");
			String origName = request.getParameter("origName");
			String origLat = request.getParameter("origLat");
			String origLon = request.getParameter("origLon");
			String origLure = request.getParameter("origLure");
			String origWeather = request.getParameter("origWeather");
			String origSpecies = request.getParameter("origSpecies");
			String origTime = request.getParameter("origTime");
			String origTemp = request.getParameter("origTemp");





			DBAccess dba = new DBAccess();
			int results = dba.editRecord(uname, newName, newLat, newLon, newLure, newWeather, newSpecies, newTime, newTemp, origName, origLat, origLon, origLure, origWeather, origSpecies, origTime, origTemp);

			Gson gson = new Gson();
			String jsonStr = gson.toJson(results);
			response.getWriter().write(" responsCode:" + jsonStr);
		}else if(function.equals("deleteRecord")){

			String uname = request.getParameter("uname");
			String name = request.getParameter("name");
			String lat = request.getParameter("lat");
			String lon = request.getParameter("lon");
			String lure = request.getParameter("lure");
			String weather = request.getParameter("weather");
			String species = request.getParameter("species");
			String time = request.getParameter("time");
			String temp = request.getParameter("temp");


			DBAccess dba = new DBAccess();
			int results = dba.deleteRecord(uname, name, lat, lon, lure, weather, species, time, temp);

			Gson gson = new Gson();
			String jsonStr = gson.toJson(results);
			response.getWriter().write(" responsCode:" + jsonStr);
		}else if(function.equals("addFriend")){
			ServletContext sc = this.getServletContext(); 
			String sender = request.getParameter("sender");
			String receiver = request.getParameter("receiver");
			//String uname = request.getParameter("uname");

			DBAccess dba = new DBAccess();
			int flag = dba.createFreindship(sender, receiver);
			Gson gson = new Gson();
			String jsonStr = gson.toJson(flag);
			response.getWriter().write(" responsCode:" + jsonStr);		
		}else if(function.equals("viewFriends")){
			ServletContext sc = this.getServletContext(); 
			String user = request.getParameter("user");
			// receiver = request.getParameter("receiver");
			//String uname = request.getParameter("uname");

			DBAccess dba = new DBAccess();
			//int flag = dba.createFreindship(sender, receiver);
			//Gson gson = new Gson();
			//String jsonStr = gson.toJson(flag);
			//response.getWriter().write(" responsCode:" + jsonStr);		
		
		
//			ArrayList<String> results = dba.getFriends(user);
			String results = dba.getFriends(user);

			Gson gson = new Gson();
			String jsonStr = gson.toJson(results);
			response.getWriter().write("results:" + jsonStr);
		}else if(function.equals("deleteFriend")){
			ServletContext sc = this.getServletContext(); 
			String user = request.getParameter("user");
			String userToDelete = request.getParameter("userToDelete");
			DBAccess dba = new DBAccess();
			//int flag = dba.createFreindship(sender, receiver);
			//Gson gson = new Gson();
			//String jsonStr = gson.toJson(flag);
			//response.getWriter().write(" responsCode:" + jsonStr);		
		
		
//			ArrayList<String> results = dba.getFriends(user);
			int flag = dba.deleteFriend(user, userToDelete);

			Gson gson = new Gson();
			String jsonStr = gson.toJson("ResponseCode:" + flag);
			response.getWriter().write("results:" + jsonStr);
		}
		
	}


	public static class Emailer  //Class names should always be capitalized
	{

		
		private String user = "wildlogicapps@gmail.com";
		private String pass = "Xycheo38364#^dnIBDUBd4D%3830rno987Nh7H&*86%%##ENNYB::L>lkj";
		public Emailer (){
			
		}
		public  void sendEmail(String fromAddr, String toAddr, String subject, String body)
		{
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			//Session session = Session.getInstance(props, null);

			Session session = Session.getDefaultInstance(props, 
				    new javax.mail.Authenticator(){
				        protected PasswordAuthentication getPasswordAuthentication() {
				            return new PasswordAuthentication(
				                user, pass);// Specify the Username and the PassWord
				        }
				});
			
			
			MimeMessage message = new MimeMessage(session);

			try
			{
				message.setFrom(new InternetAddress(fromAddr));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddr));
				message.setSubject(subject);
				message.setText(body);
				//	        Transport.send(message, user, pass);
				Transport.send(message);
				
			}
			catch (AddressException e) {e.printStackTrace();}
			catch (MessagingException e) {e.printStackTrace();}
			 System.out.println("GOODY");
		}
	}

}
