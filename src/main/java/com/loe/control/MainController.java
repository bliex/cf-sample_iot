package com.loe.control;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class MainController {

	@Autowired
	private SimpMessagingTemplate template;

	private static String paser(String body,String name) throws Exception{
		if(name.equals("content")){
		
			JSONParser jsonParser = new JSONParser();
			JSONObject result = (JSONObject) jsonParser.parse(body);
			JSONObject cin = (JSONObject) result.get("cin");			
		    return (String)cin.get("con");	
		}
		JSONParser jsonParser = new JSONParser();
		JSONObject result = (JSONObject) jsonParser.parse(body);
	    JSONObject sgn = (JSONObject) result.get("sgn");
	    JSONObject nev = (JSONObject) sgn.get("nev");
        JSONObject rep = (JSONObject) nev.get("rep");
        return new String(Base64Utils.decodeFromString((String) rep.get("value"))); 	}

	@RequestMapping(value="/dashboard", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	public void dashboard(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
		String content = paser(paser(body,"representation"),"content");
        System.out.println("content : " + content);
        
		//content = "{ \""+content.split("'")[1]+"\" :  \""+content.split("'")[3]+"\" , \""+content.split("'")[5]+"\" :  \""+content.split("'")[7]+"\" , \""+content.split("'")[9]+"\" :  \""+content.split("'")[11]+"\" }";
		HttpEntity<String> entity = new HttpEntity<String>(content, headers);
		this.template.convertAndSend("/topic/subscribe",entity);
	}
	
/* Iolab  개소식 m2m 소스 
 
	@RequestMapping(value="/m2m", method=RequestMethod.POST) // 서버에서 보내온 정보를 구독함.
	@ResponseStatus(value=HttpStatus.OK)
	public void notify(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
		String content = paser(paser(body,"representation"),"content");
	    System.out.println("content : " + content);
	    String value = new String(Base64Utils.decodeFromString((String)content));
	       System.out.println("value : " +value);
	       
	    String light_url ="";
	    String led_url ="";
	    if(value.equals("1")){  //창문 OPEN
	    	System.out.println("window OPEN");
	    	sendMgmt("http://apim.startiot.or.kr:8281/charlot/base", "88888.8888.RP05", "switch1", "ON","switch2","ON", "fc4b959205accb2638f0259a1faa9b4b");
	    	sendMgmt("http://apim.startiot.or.kr:8281/charlot/base", "00000.0002.etc", "led", "OPEN","led2","OPEN", "3026d3880b8fc91991ff0e4ba290c0e0");

	    }else if(value.equals("0")){ //창문 Close
	    	System.out.println("window CLOSE");
	    	sendMgmt("http://apim.startiot.or.kr:8281/charlot/base", "88888.8888.RP05", "switch1", "OFF", "switch2","OFF","fc4b959205accb2638f0259a1faa9b4b");
	    	sendMgmt("http://apim.startiot.or.kr:8281/charlot/base", "00000.0002.etc", "led", "CLOSE","led2","CLOSE", "3026d3880b8fc91991ff0e4ba290c0e0");

	    	
	    }
	   
	    
		//HttpEntity<String> entity = new HttpEntity<String>(content, headers);
		//this.template.convertAndSend("/topic/subscribe", entity);
	}	
* 	
 */
	@MessageMapping("/timeline") // 데모 페이지로 보냄.
	@SendTo("/topic/subscribe")
	public HttpEntity<String> timeline(@RequestBody String body) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		return entity;
	}

	
public void sendMgmt(String url,String deviceName, String cmdName, String cmd, String cmdName1, String cmd1, String dKey) throws ParseException, IOException{
	 //RP05 -> 전구
   String desUrl = url+"/control-"+deviceName;
   System.out.println("desurl : " + desUrl);
    CloseableHttpClient httpclient = HttpClients.createDefault();
	try {
		HttpPut httpPut = new HttpPut(desUrl);
		httpPut.setHeader("X-M2M-RI", "RQI0001"); // 리퀘스트 ID
		httpPut.setHeader("X-M2M-Origin", "/S"+deviceName); // 제어자 이름
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("Authorization","Bearer "+dKey);
		httpPut.setHeader("Content-Type","application/vnd.onem2m-res+json");		
		String body="{ \"mgc\": {\"cmt\": 4,\"exra\": { \"any\":[{\"nm\" :\""+cmdName+"\", \"val\" : \""+cmd+"\"}, {\"nm\" :\""+cmdName1+"\", \"val\" : \""+cmd1+"\"} ]},\"exm\" : 1,\"exe\":true,\"pexinc\":true}}";
		System.out.println(body);
		httpPut.setEntity(new StringEntity(body));
		
	
		CloseableHttpResponse res = httpclient.execute(httpPut);

		try {
			if (res.getStatusLine().getStatusCode() == 200)
			{
				org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) res.getEntity();
			    System.out.println(EntityUtils.toString(entity));
			}else{
				System.out.println("eerr");
			}
		} finally {
			res.close();
		}
	} finally {
		httpclient.close();
	}

}


	@Value("${IOT_MGMT_URL}")
	private String cseUrl;
	
	@Value("${IOT_OID}")
	private String oid;
	
	@Value("${IOT_DKEY}")
	private String dkey;

@RequestMapping(value="/sendtoplug", method=RequestMethod.POST)
@ResponseStatus(value=HttpStatus.OK)
public void sendToplug(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
	System.out.println("in sendtoplug");  
	System.out.println(body);
	if(body.equals("ON")){
		System.out.println("in ON");
		sendMgmt(cseUrl, oid, "switch", "ON", "switch1", "null", dkey);
	}else {
		sendMgmt(cseUrl, oid, "switch", "OFF", "switch1", "null", dkey);
	}
	
}

	
}
