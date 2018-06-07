package com.fishlog;

public class TestDriver {
	public static void main(String[] args){
		DBAccess dba = new DBAccess();
		boolean b = dba.insertRecord("recordName1","5.0","5.0","lure","clear","Muskellunge","1222:11:11:11", "5", "//home//picLocation", "user1", "12");
		System.out.println();	
	}
}