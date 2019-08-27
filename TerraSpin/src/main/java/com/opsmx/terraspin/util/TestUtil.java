package com.opsmx.terraspin.util;

public class TestUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String MYREGION = "abregion";
		String MYACCESSKEY = "abaccess" ;
		String MYSECRETKEY = "absecret"; 
		String providerConfig = "provider \"aws\"" + "{ region = \"" + MYREGION + "\"" + "\n access_key = \"" + MYACCESSKEY + "\"" + "\n secret_key = \"" + MYSECRETKEY + "\"  }";

		System.out.println("hey :" + providerConfig);
	}

}
