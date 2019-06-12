/**
 * 
 */
package com.opsmx.terraApp.util;

/**
 * @author opsmx
 *
 */
public class HalConfigUtil {

	/**
	 * @param args
	 */
	public static String halConfig;
	
	public HalConfigUtil(){
	}
	
	public static String getHalConfig() {
		return halConfig;
	}

	public static void setHalConfig(String halConfig) {
		HalConfigUtil.halConfig = halConfig;
	}

	public HalConfigUtil(String halConfig){
		this.halConfig = halConfig;
	}
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
