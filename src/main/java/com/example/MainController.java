package com.example;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

@Controller
public class MainController {

	@Value("${iot.mgmt.url}")
	private String mgmtUrl;
	@Value("${iot.oid}")
	private String oid;
	@Value("${iot.dkey}")
	private String dkey;
	@Autowired
	private SimpMessagingTemplate template;

	private static String paser(String body,String name) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(body));
		Document doc = builder.parse(inputSource);
		Node node = doc.getElementsByTagName(name).item(0);
		Element element = (Element) node;
		return new String(Base64Utils.decodeFromString(element.getFirstChild().getNodeValue()));
	}

	@RequestMapping(value="/noti", method=RequestMethod.POST) // 서버에서 보내온 정보를 구독함.
	@ResponseStatus(value=HttpStatus.OK)
	public void notify(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
		String content = paser(paser(body,"representation"),"content");
		HttpEntity<String> entity = new HttpEntity<String>(content, headers);
		this.template.convertAndSend("/topic/subscribe", entity);
	}	

	@MessageMapping("/timeline") // 데모 페이지로 보냄.
	@SendTo("/topic/subscribe")
	public HttpEntity<String> timeline(@RequestBody String body) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		return entity;
	}

	// 데모 페이지에서 보낸 명령을 서버로 보내는 메서드
	@RequestMapping(value="/control", method=RequestMethod.POST) 
	public String control(@RequestBody String mode, Model model) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String rtn = "";
		try 
		{
			HttpPut put = new HttpPut(mgmtUrl);
			put.setHeader("X-M2M-RI", "12345");
			put.setHeader("X-M2M-Origin", "/S"+oid);
			put.setHeader("Authorization","Bearer "+dkey);
			put.setHeader("Content-Type","application/vnd.onem2m-res+json");        
			if(!mode.startsWith("2"))
				mode =mode.substring(0, 1);

			String body="{\"mgc\" : {\"cmt\" : 4,\"exra\" : {\"any\" : [ {\"nm\" : \"switch\",\"val\" : \""+mode+"\"} ]},\"exm\" : 1,\"exe\" : true,\"pexinc\" : true}}";

			put.setEntity(new StringEntity(body));
			CloseableHttpResponse res = httpclient.execute(put);

			try {
				Header[] heads = res.getAllHeaders();
				for (Header h : heads) {
					System.out.println(h.getName() + " : " + h.getValue());
				}
				System.out.println("status : " + res.getStatusLine().getStatusCode());
				if (res.getStatusLine().getStatusCode() == 200)
				{
					org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) res.getEntity();
					rtn = EntityUtils.toString(entity);
					System.out.println("body : " + rtn);
				}
			} finally {
				res.close();
			}		
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		} 
		finally 
		{
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rtn;
	}
}
