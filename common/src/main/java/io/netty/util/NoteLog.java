/**
 * 
 */
package io.netty.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * @author zhailz
 *
 */
public  class NoteLog {

	 public  static final Logger log = LoggerFactory.getLogger("解释说");
	 
	  public  static void info(String format, Object arg){
		  log.info(format, arg); 
	  }
	  
	  public  static void info(String msg){
		  log.info(msg);
	  }


	 
	  public static void info(String format, Object arg1, Object arg2){
		  log.info(format, arg1, arg2);
	  }

	  public static void info(String format, Object... arguments){
		  log.info(format, arguments);
	  }

	  public static void info(String msg, Throwable t){
		  log.info(msg, t);
	  }

	  public static boolean isInfoEnabled(Marker marker){
		  return log.isInfoEnabled(marker);
	  }

	  public static void info(Marker marker, String msg){
		  log.info(marker,msg);
	  }

	  public static void info(Marker marker, String format, Object arg){
		  log.info(marker, format, arg);
	  }

	  public static void info(Marker marker, String format, Object arg1, Object arg2){
		  log.info(marker, format, arg1, arg2);
	  }


	  public static void info(Marker marker, String format, Object... arguments){
		  log.info(marker, format, arguments);
	  }


	  public static void info(Marker marker, String msg, Throwable t){
		  log.info(marker, msg, t);
		  
	  }


}
