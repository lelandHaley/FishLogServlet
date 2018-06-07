package com.fishlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBAccess {

	private Connection con;

	

	public synchronized boolean insertRecord(String recordName, String recordLat, String recordLon, String recordLure, String recordWeather, String recordSpecies, String time, String temp, String path, String user, String hours) {
		
		String sqlStatement ="insert into Records(name, lat, lon, lure, weather, species, time, temperature, path, user, hourcaught) values (?,?,?,?,?,?,?,?,?,?,?)";
		boolean flag = false;
		ResultSet r = null;
		PreparedStatement myStmt = null;
		try {
			con = DBConnection.getConnection();
			myStmt = con.prepareStatement(sqlStatement);
			myStmt.setString(1, recordName);
			myStmt.setFloat(2, Float.valueOf(recordLat));
			myStmt.setFloat(3, Float.valueOf(recordLon));
			myStmt.setString(4, recordLure);
			myStmt.setString(5, recordWeather);
			myStmt.setString(6, recordSpecies);
			myStmt.setString(7, time);
			myStmt.setString(8, temp);
			myStmt.setString(9, path);
			myStmt.setString(10, user);
			myStmt.setString(11, hours);
			System.out.println(myStmt.toString());
			flag = (myStmt.executeUpdate()>=0);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnection.releaseConn(r, myStmt, con);
		}
		return flag;
	}


	public int createUser(String uname, String pword, String email) {

		boolean flag = false;
		Statement selectStatement = null;
		ResultSet r = null;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement ="Select count(*) from Users where uname = '"+uname+"'";
			System.out.println(sqlUserQueryStatement);
			r = selectStatement.executeQuery(sqlUserQueryStatement);
			if(!r.next())
				return 3; //error code, something went wrong
			int userExistanceIndicator= r.getInt(1);
			if(userExistanceIndicator > 0)
				return 1; // user exists already
			
			con = DBConnection.getConnection();
			String sqlEmailQueryStatement ="Select count(*) from Users where email = '"+email+"'";
			System.out.println(sqlEmailQueryStatement);
			ResultSet emailResult = null;
			emailResult = selectStatement.executeQuery(sqlEmailQueryStatement);
			if(!emailResult.next())
				return 5; //error code, something went wrong
			int emailExistanceIndicator= emailResult.getInt(1);
			if(emailExistanceIndicator > 0)
				return 6; // user exists already

			String sqlInsertUserStatement ="insert into Users(uname, pword, email) values (?,?,?)";

			con = DBConnection.getConnection(); // may not need thiz
			PreparedStatement insertStatement = null;
			insertStatement = con.prepareStatement(sqlInsertUserStatement);
			insertStatement.setString(1, uname);
			insertStatement.setString(2, pword);
			insertStatement.setString(3, email);

			flag = (insertStatement.executeUpdate()>=0);

		} catch (SQLException e) {

			return 4;// unknown error, possible user insertion failure
		} finally {
			DBConnection.releaseConn(r, selectStatement, con);
		}
		if(flag){
			return 0;
		}
		return 2; // insertion failure
	}

	public int checkLogin(String uname, String pword) {
		Statement selectStatement = null;
		ResultSet r = null;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement ="Select count(*) from Users where uname = '"+uname+"' and pword = '"+ pword +"'";
			System.out.println(sqlUserQueryStatement);
			r = selectStatement.executeQuery(sqlUserQueryStatement);
			if(!r.next())
				return 2; //error code, something went wrong
			int userExistanceIndicator= r.getInt(1);
			if(userExistanceIndicator != 1)
				return 1; // user password combo doesnt exist
			else{
				return 0;
			}
		} catch (SQLException e) {

			return 3;// unknown error
		} finally {
			DBConnection.releaseConn(r, selectStatement, con);
		}
	}

	public ArrayList<String> getRecords(String uname, String filter, String filterSpecifics, String filterSpecifics2, String lat, String lon) {
		Statement selectStatement = null;
		ResultSet r = null;
		ArrayList<String> results = new ArrayList<String>();
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlRecordQueryStatement ="Select * from Records where user = '"+uname+"'";// and pword = '"+ pword +"'";
			sqlRecordQueryStatement += "  or user in (select sender from friendships where receiver = '"+ uname +"' union select receiver from friendships where sender = '"+ uname +"')";
			if(filter.equalsIgnoreCase("Species")){
				sqlRecordQueryStatement += " and species = '" + filterSpecifics + "'";	
			}
			if(filter.equalsIgnoreCase("Weather Conditions")){
				sqlRecordQueryStatement += " and weather = '" + filterSpecifics + "'";	
			}
			if(filter.equalsIgnoreCase("Records Near Me")){
				
				Double curLon = Double.valueOf(lon);
				Integer longTraveledMiles = Integer.valueOf(filterSpecifics);
				Double latTraveledKM = longTraveledMiles * 0.621371;
				Double latTraveledDeg = (1 / 110.54) * latTraveledKM;

				Double curLat = Double.valueOf(lat);
				Double longTraveledKM = longTraveledMiles * 0.621371;
				Double longTraveledDeg = (1 / (Double.valueOf(lon) * Math.cos(curLat))) * longTraveledKM;
				Double rangeMinLat = curLat - latTraveledDeg;
				Double rangeMaxLat = curLat + latTraveledDeg;
				Double rangeMinLon = curLon - longTraveledDeg;
				Double rangeMaxLon = curLon + longTraveledDeg;
				if(curLat < 0){
					Double temp = rangeMinLat;
					rangeMinLat = rangeMaxLat;
					rangeMaxLat = temp;
				}
				if(curLon < 0){
					Double temp = rangeMinLon;
					rangeMinLon = rangeMaxLon;
					rangeMaxLon = temp;
				}
				
				sqlRecordQueryStatement += " and lat >= " + rangeMinLat + " AND lat  <=" + rangeMaxLat + " and lon >= " + rangeMinLon + " AND  lon <=" + rangeMaxLon;	
			}
			if(filter.equalsIgnoreCase("Time Of Day Recorded")){
				int starttime = Integer.valueOf(filterSpecifics);
				int endtime = Integer.valueOf(filterSpecifics2);
				sqlRecordQueryStatement += " and hourcaught >= " + starttime + " and hourcaught <= " + endtime; 
			}
			
			System.out.println(sqlRecordQueryStatement);
			r = selectStatement.executeQuery(sqlRecordQueryStatement);

			String result = "$$$$";
			 
			while(r.next()){
				result += "name@@" + r.getString("name") + "%%";
				result += "lat@@" + r.getString("lat") + "%%";
				result += "lon@@" + r.getString("lon") + "%%";
				result += "lure@@" + r.getString("lure") + "%%";
				result += "weather@@" + r.getString("weather") + "%%";
				result += "species@@" + r.getString("species") + "%%";
				result += "time@@" + r.getString("time") + "%%";
				result += "temperature@@" + r.getString("temperature") + "%%";
				result += "path@@" + r.getString("path") + "%%";
				result += "hour@@" + r.getString("hourcaught") + "%%";
				result += "user@@" + r.getString("user")  + "^^^^^";
			}
			if(result.length() <=4){
				results.add("No Records Matching Search");
			}else{
				result.substring(0, result.length() - 5);
				results.add(result);
			}
			
		} catch (SQLException e) {

			results.add("Unknown error");// unknown error
		}
		System.out.println(results);
		return results;
	}

	public int editRecord(String uname, String newName, String newLat, String newLon, String newLure,
			String newWeather, String newSpecies, String newTime, String newTemp, String origName, String origLat,
			String origLon, String origLure, String origWeather, String origSpecies, String origTime, String origTemp) {
		// TODO Auto-generated method stub
		Statement selectStatement = null;
		int editFlag = -1;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement = "UPDATE Records SET name = '"+newName+"', lat = "+newLat+", lon = "+newLon+", lure = '"+newLure+"', weather = '"+newWeather+"', species = '"+newSpecies+"', time = '"+newTime+"', temperature = "+newTemp+" WHERE user = '"+uname+"' and name = '"+origName+"' and lat = "+origLat+" and lon = "+origLon+" and lure = '"+origLure+"' and weather = '"+origWeather+"' and species = '"+origSpecies+"' and time = '"+origTime+"' and temperature = "+origTemp+"";
			System.out.println(sqlUserQueryStatement);
			editFlag = selectStatement.executeUpdate(sqlUserQueryStatement);
			return editFlag;
			
		} catch (SQLException e) {

			return 3;// unknown error
		} finally {
			DBConnection.releaseConn(null, selectStatement, con);
		}
	}





	public int deleteRecord(String uname, String name, String lat, String lon, String lure, String weather,
			String species, String time, String temp) {
		Statement selectStatement = null;
		int editFlag = -1;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement = "delete from Records WHERE user = '"+uname+"' and name = '"+name+"' and lat = "+lat+" and lon = "+lon+" and lure = '"+lure+"' and weather = '"+weather+"' and species = '"+species+"' and time = '"+time+"' and temperature = "+temp+"";
			System.out.println(sqlUserQueryStatement);
			editFlag = selectStatement.executeUpdate(sqlUserQueryStatement);

			return editFlag;
			
		} catch (SQLException e) {

			return 3;// unknown error
		} finally {
			DBConnection.releaseConn(null, selectStatement, con);
		}
	}


	public int createFreindship(String sender, String receiver) {
		boolean flag = false;
		Statement selectStatement = null;
		ResultSet r = null;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement ="Select count(*) from Users where uname = '"+receiver+"'";
			System.out.println(sqlUserQueryStatement);
			r = selectStatement.executeQuery(sqlUserQueryStatement);
			if(!r.next())
				return 5; //error code, something went wrong
			int userExistanceIndicator= r.getInt(1);
			if(userExistanceIndicator == 0)
				return 1; // user doesnt exits
			con = DBConnection.getConnection();
			String sqlfriendshipRequestQueryStatement ="Select count(*) from friendships where accepted = false and sender = '"+ sender +"' and receiver = '"+ receiver +"' or receiver = '"+ sender +"' and receiver = '"+ sender + "'";
			System.out.println(sqlfriendshipRequestQueryStatement);
			ResultSet friendshipRequestResult = null;
			friendshipRequestResult = selectStatement.executeQuery(sqlfriendshipRequestQueryStatement);
			if(!friendshipRequestResult.next())
				return 2; //error code, something went wrong
			int freindshipRequestExistenceIndicator= friendshipRequestResult.getInt(1);
			if(freindshipRequestExistenceIndicator > 0)
				return 7; // friendship request sent, not yet accepted
			
			String sqlfriendshipQueryStatement ="Select count(*) from friendships where accepted = true and sender = '"+ sender +"' and receiver = '"+ receiver +"' or receiver = '"+ sender +"' and receiver = '"+ sender + "'";
			System.out.println(sqlfriendshipQueryStatement);
			ResultSet friendshipResult = null;
			friendshipResult = selectStatement.executeQuery(sqlfriendshipQueryStatement);
			if(!friendshipResult.next())
				return 2; //error code, something went wrong
			int freindshipExistenceIndicator= friendshipResult.getInt(1);
			if(freindshipExistenceIndicator > 0)
				return 3; // freindship exists already

			String sqlInsertUserStatement ="insert into friendships(sender, receiver, accepted) values (?,?,?)";
			con = DBConnection.getConnection(); // may not need thiz
			PreparedStatement insertStatement = null;
			insertStatement = con.prepareStatement(sqlInsertUserStatement);
			insertStatement.setString(1, sender);
			insertStatement.setString(2, receiver);
			insertStatement.setBoolean(3, false);

			flag = (insertStatement.executeUpdate()>=0);

		} catch (SQLException e) {

			return 4;// unknown error, possible user insertion failure
		} finally {
			DBConnection.releaseConn(r, selectStatement, con);
		}
		if(flag){
			return 0;
		}else{
			return 6; // insertion failure
		}
	}
	
	public String getFriends(String user) {
		ArrayList<String> results = new ArrayList<String>();
		Statement selectStatement = null;
		String result;
		
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserIsSenderQueryStatement ="Select receiver from friendships where sender = '"+user+"' and accepted = true";
			System.out.println(sqlUserIsSenderQueryStatement);
			ResultSet userIsSender = null;
			result = "$$$$";
			userIsSender = selectStatement.executeQuery(sqlUserIsSenderQueryStatement);	
			while(userIsSender.next()){
				result += userIsSender.getString("receiver") + "^^^";
			}
			con = DBConnection.getConnection();
			String sqlUserIsReveiverQueryStatement = "Select sender from friendships where receiver = '"+user+"' and accepted = true";
			System.out.println(sqlUserIsReveiverQueryStatement);
			ResultSet userIsReceiver = null;
			userIsReceiver = selectStatement.executeQuery(sqlUserIsReveiverQueryStatement);
			while(userIsReceiver.next()){
				result += userIsReceiver.getString("sender") + "^^^";
			}
			if(result.length() > 4){
				result = result.substring(0, result.length() - 3);
			}		
			results.add(result);
			
			//for request not yet accepted
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserIsSenderNotAcceptedQueryStatement ="Select receiver from friendships where sender = '"+user+"' and accepted = false";
			System.out.println(sqlUserIsSenderNotAcceptedQueryStatement);
			ResultSet userIsSenderNotAccepted = null;
			result += "%%%%";
			userIsSenderNotAccepted = selectStatement.executeQuery(sqlUserIsSenderNotAcceptedQueryStatement);	
			while(userIsSenderNotAccepted.next()){
				result += userIsSenderNotAccepted.getString("receiver") + "^^^";
			}
			con = DBConnection.getConnection();
			String sqlUserIsReveiverNotAcceptedQueryStatement = "Select sender from friendships where receiver = '"+user+"' and accepted = false";
			System.out.println(sqlUserIsReveiverNotAcceptedQueryStatement);
			ResultSet userIsReceiverNotAccepted = null;
			userIsReceiverNotAccepted = selectStatement.executeQuery(sqlUserIsReveiverNotAcceptedQueryStatement);
			while(userIsReceiverNotAccepted.next()){
				result += userIsReceiverNotAccepted.getString("sender") + "^^^";
			}
			result = result.substring(0, result.length() - 3);
			results.add(result);

			if(results.isEmpty()){
				return "No Records Matching Search";
			}
			
		} catch (SQLException e) {
			return "SQL Error";
		}

		System.out.println(results);
		return result;
	}





	public int deleteFriend(String user, String userToDelete) {
		Statement selectStatement = null;
		int editFlag = -1;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement = "delete from friendships WHERE (sender = '"+user+"' and receiver = '"+userToDelete+"' and accepted = true) or (receiver = '"+user+"' and sender = '"+userToDelete+"' and accepted = true)";
			System.out.println(sqlUserQueryStatement);
			editFlag = selectStatement.executeUpdate(sqlUserQueryStatement);

			return editFlag;
			
		} catch (SQLException e) {

			return 2;// unknown error
		} finally {
			DBConnection.releaseConn(null, selectStatement, con);
		}

	}


	public String getRecordsForMap(String uname, String minlat, String minlon, String maxlat, String maxlon,
			String species, String weather, String startTime, String endTime, String showFriends) {
			
		System.out.println("DEBUG");
		
		int sTime = Integer.parseInt(startTime);
		int eTime = Integer.parseInt(endTime);
		
		Statement selectStatement = null;
		ResultSet r = null;
		ArrayList<String> results = new ArrayList<String>();
		String result = "";
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlRecordQueryStatement ="Select * from Records where (lat >= " + minlat + " AND lat  <=" + maxlat + " and lon >= " + minlon + " AND  lon <=" + maxlon;

			if(!species.equalsIgnoreCase("all species")){
				sqlRecordQueryStatement += " and species = '" + species + "'";	
			}
			if(!weather.equalsIgnoreCase("all conditions")){
				sqlRecordQueryStatement += " and weather = '" + weather + "'";	
			}
			if(sTime > 0){
				sqlRecordQueryStatement += " and hourcaught >= " + sTime;
			}
			if(eTime < 24){
				sqlRecordQueryStatement += " and hourcaught <= " + eTime;
			}
			
			sqlRecordQueryStatement += ") and (user = '"+uname+"'";
			
			if(showFriends.equalsIgnoreCase("true")){
				sqlRecordQueryStatement += "  or user in (select sender from friendships where receiver = '"+ uname +"' union select receiver from friendships where sender = '"+ uname +"'))";
			}else{
				sqlRecordQueryStatement += ")";
			}
			
			
			System.out.println(sqlRecordQueryStatement);
			r = selectStatement.executeQuery(sqlRecordQueryStatement);

			result = "$$$$";
			 
			while(r.next()){
				result += "name@@" + r.getString("name") + "%%";
				result += "lat@@" + r.getString("lat") + "%%";
				result += "lon@@" + r.getString("lon") + "%%";
				result += "lure@@" + r.getString("lure") + "%%";
				result += "weather@@" + r.getString("weather") + "%%";
				result += "species@@" + r.getString("species") + "%%";
				result += "time@@" + r.getString("time") + "%%";
				result += "temperature@@" + r.getString("temperature") + "%%";
				result += "path@@" + r.getString("path") + "%%";
				result += "hour@@" + r.getString("hourcaught") + "%%";
				result += "user@@" + r.getString("user")  + "^^^^^";

			}
			if(result.length() <= 4){
				result = "No Records Matching Search";
			}else{
				result.substring(0, result.length() - 5);
				results.add(result);
			}
			
		} catch (SQLException e) {

			results.add("Unknown error");// unknown error
		}

		System.out.println(results);
		return result;

	}

	public String getIncoming(String user) {

			ArrayList<String> results = new ArrayList<String>();
			Statement selectStatement = null;
			String result;
			
			try {
				con = DBConnection.getConnection();
				selectStatement = con.createStatement();
				String sqlUserIsSenderQueryStatement ="Select sender from friendships where receiver = '"+user+"' and accepted = false";
				System.out.println(sqlUserIsSenderQueryStatement);
				ResultSet userIsSender = null;
				result = "$$$$";
				userIsSender = selectStatement.executeQuery(sqlUserIsSenderQueryStatement);	
				while(userIsSender.next()){
					result += userIsSender.getString("sender") + "^^^";
				}
				
				if(result.length() > 4){
					result = result.substring(0, result.length() - 3);
				}else{
					result = "$$$$No Incoming Friendship Requests";
				}
				
			} catch (SQLException e) {
				return "SQL Error";
			}

			System.out.println(results);
			return result;

	}
	
	public int acceptRequest(String uname, String requestSender){

		Statement selectStatement = null;
		int editFlag = -1;
		try {
			con = DBConnection.getConnection();
			selectStatement = con.createStatement();
			String sqlUserQueryStatement = "UPDATE friendships SET accepted = true WHERE sender = '"+requestSender+"' and receiver = '"+uname+"' and accepted = false";
			System.out.println(sqlUserQueryStatement);
			editFlag = selectStatement.executeUpdate(sqlUserQueryStatement);

			return editFlag;
			
		} catch (SQLException e) {

			return 3;// unknown error
		} finally {
			DBConnection.releaseConn(null, selectStatement, con);
		}
	}
}
