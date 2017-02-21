package com.fishlog;

public class TestDriver {
	public static void main(String[] args){
		DBAccess dba = new DBAccess();
		boolean b = dba.insertRecord("name21234567890123456789012345678901234567890","1","2","lure","weather","species");
		System.out.println();	
	}
}
