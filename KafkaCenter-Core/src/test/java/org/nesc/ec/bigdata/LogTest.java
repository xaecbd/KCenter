package org.nesc.ec.bigdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** 
* @author Truman.P.Du  
* @date 2019年4月24日 下午2:58:07 
* @version 1.0
*/
public class LogTest {
   private static final Logger LOG = LoggerFactory.getLogger(LogTest.class);
	public static void main(String[] args) {
		Map<String,String> map = new HashMap<>();
		map.put("test", "value");
		LOG.error("name:{} age:{} map:{}","truman",18,map.toString());

	}

}
