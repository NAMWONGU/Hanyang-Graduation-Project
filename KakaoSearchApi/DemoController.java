package com.example.kakaoapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;




@RestController
public class DemoController {
	
	
	private String getQuery(String query) {
		
		String result = "";
		try {
        	String clientId = "k7Ce3hsh9oFI9dcA6O8f";
            String clientSecret = "p4O48qNltA";
            String text = URLEncoder.encode(query, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/encyc.json";
            String getParams = "query=" + text;
            URL url = new URL(apiURL + "?" + getParams);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            
            // post request
            

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
         
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            result = response.toString();
            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e);
        }
		
		return result;
	}
	
	
	@RequestMapping(value = "message", method = RequestMethod.POST)
	public String test3(@RequestBody Message message) {
		return "response";
	}

	@RequestMapping(value = "/keyboard", method = RequestMethod.GET)
	public String test2() {
		String response = makeDefaultButtonJson().toString();
		System.out.println("키보드 api 호출 : " + response);
		return makeDefaultButtonJson().toString();
	}

	
	
	private String makeMessageWithButton(String message) {
		JSONObject json = new JSONObject();
		JSONObject innerJson = new JSONObject();
		try {
			innerJson.put("text", message);
			json.put("message", innerJson);
			json.put("keyboard", makeDefaultButtonJson());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	private String makeMessageWithText(String message) {
		JSONObject json = new JSONObject();
		JSONObject innerJson = new JSONObject();
		try {
			innerJson.put("text", message);
			json.put("message", innerJson);
			json.put("keyboard", makeDefaultTextJson());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	private JSONObject makeDefaultButtonJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("type", "buttons");
			JSONArray jarr = new JSONArray();
			jarr.put("밥주기");
			jarr.put("말걸기");
			json.put("buttons", jarr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	private JSONObject makeDefaultTextJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("type", "text");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	private void noUse(Message message) {
		System.out.println("----받은 내용----");
		System.out.println("user_key : " + message.user_key);
		System.out.println("type : " + message.type);
		System.out.println("content : " + message.content);
		System.out.println("---- 끝 ----");
		
		JSONObject json = new JSONObject();
		JSONObject json2 = new JSONObject();
		JSONObject json3 = new JSONObject();
		try {
			json2.put("text", "고마워");
			// json3.put("label", "주유 쿠폰 받기");
			// json3.put("url", "https://coupon/url");
			// json2.put("message_button", json3);
			json.put("message", json2);
			json2 = new JSONObject();
			json2.put("type", "buttons");
			JSONArray jarr = new JSONArray();
			jarr.put("");
			jarr.put("");
			jarr.put("");
			json2.put("buttons", jarr);

			json.put("keyboard", json2);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(json.toString());
	}
	
	
}
