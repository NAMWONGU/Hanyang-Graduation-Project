package sample;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import annotation.Autowired;
import annotation.Controller;
import annotation.RequestMapping;
import annotation.RequestMethod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import org.snu.ids.ha.ma.Eojeol;
import org.snu.ids.ha.ma.MExpression;
import org.snu.ids.ha.ma.MorphemeAnalyzer;
import org.snu.ids.ha.ma.Sentence;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	@Autowired
	TestAutoWired test;

	MorphemeAnalyzer ma;

	public HomeController() {
		// TODO Auto-generated constructor stub
		ma = new MorphemeAnalyzer();
		// create logger, null then System.out is set as a default logger
	}

	ConcurrentHashMap<String, UserSession> userSessionMap = new ConcurrentHashMap<>();

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "home";
	}

	@RequestMapping(value = "/keyboard", method = RequestMethod.GET)
	public String keyboard() {
		String response = makeDefaultButtonJson().toString();
		return response;
	}

	@RequestMapping(value = "/message", method = RequestMethod.POST)
	public String message(String body) {
		JSONObject json;
		String user_key = null;
		String content = null;
		try {
			json = new JSONObject(body);
			user_key = json.getString("user_key");
			content = json.getString("content");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (userSessionMap.get(user_key) != null && userSessionMap.get(user_key).is_making) {
			insertIntoFile(userSessionMap.get(user_key), content);
			userSessionMap.get(user_key).is_making = false;
			return makeMessageWithButton3("입력을 하였습니다. 해당 학습을 입력하기 위해선 재시작을 하여야 하고, 해당 작업은 오래 걸릴 수 있습니다. 그리하여 오류메세지가 전달 될 수도 있으나 다시 하면 잘 작동할 것입니다.");
		}

		if (content.contains("검색")) {
		} else if (content.startsWith(":")) {
			if (content.equals(":알려준다.")) {
				if (userSessionMap.get(user_key) == null) {
					return makeMessageWithText("세션이 존재하지 않습니다.");
				}	
				userSessionMap.get(user_key).is_making = true;
				return makeMessageWithText("도로시에게 지금의 주제로 내가 원하는 응답을 알려줄지, 다른주제로 "
						+ "내가 원하는 응답을 알려줄 지, 선택해주세요.");
			} else if (content.equals(":알려주지 않는다.")) {
				return makeMessageWithText("실망 ..");
			} else if (content.equals(":같은주제")) {
				return makeMessageWithText("원하는 주제를 입력하세요,");
			} else if (content.equals(":다른주제")) {
				if (userSessionMap.get(user_key) == null) {
					return makeMessageWithText("세션이 존재하지 않습니다.");
				}
				userSessionMap.get(user_key).is_topic_creating = true;
				return makeMessageWithText("지금은 지원하지 않는 기능이야.");
			} else if (content.equals(":재시작")) {
				getMessageToBot(":build Dorothy reset");
				return makeMessageWithText("모든 학습이 반영되었습니다.");
			}
		} else {
			content = changeContent(content);
		}
		System.out.println(content);
		
		String response = getMessageToBot(content);
		
		if (response.equals("너가 뭐라는지 잘 모르겠어. 나에게 알려줄래?")) {
			if (userSessionMap.get(user_key) == null) {
				UserSession userSession = new UserSession();
				userSession.topic = "brabe";
				userSession.topic_count = "0";
				userSession.last_content = content;
				userSessionMap.put(user_key, userSession);
			} else {
				userSessionMap.get(user_key).last_content = content;
			}
	    	return makeMessageWithButton(response);
	    } else {
	    	try {
				String botMessage[] = response.split("/-");
				UserSession userSession = new UserSession();
				userSession.topic = botMessage[0];
				userSession.topic_count = botMessage[1];
				userSession.last_content = content;
				userSessionMap.put(user_key, userSession);
				response = botMessage[2];

			} catch (Exception e) {
				e.printStackTrace();
			}
	    }  
	    

		return makeMessageWithText(response);
	}
	
	private void insertIntoFile(UserSession userSession, String message) {
		try {
			String str;
			File file = new File("C:\\Users\\wg\\Documents\\SoftBotProject\\Dorothy\\"+userSession.topic+".top");
			RandomAccessFile rf= new RandomAccessFile(file, "rw");
			
			try {
				String botMessage[] = userSession.last_content.split(" ");
				str = botMessage[0] + " ";
			} catch (Exception e) {
				str = userSession.last_content;
			}
			insert("C:\\Users\\wg\\Documents\\SoftBotProject\\Dorothy\\"+userSession.topic+".top", 29, new String(str.getBytes("UTF-8"), "8859_1"));

			rf.seek(rf.length());
			str = "\r\nu:"+(Integer.parseInt(userSession.topic_count) + 1)+" ( << ";
			rf.writeBytes(new String(str.getBytes("UTF-8"), "8859_1"));
			str = userSession.last_content + " ";
			rf.writeBytes(new String(str.getBytes("UTF-8"), "8859_1"));
			str = ">> ) ";
			rf.writeBytes(new String(str.getBytes("UTF-8"), "8859_1"));
			str = userSession.topic + "/-" + (Integer.parseInt(userSession.topic_count) + 1) + "/-" + message;
			rf.writeBytes(new String(str.getBytes("UTF-8"), "8859_1"));
			rf.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void insert(String filename, long offset, String content) {
		try {
			File tmpFile = new File(filename + "~");
			RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
			RandomAccessFile rtemp = new RandomAccessFile(tmpFile, "rw");
			long fileSize = r.length();
			FileChannel sourceChannel = r.getChannel();
			FileChannel targetChannel = rtemp.getChannel();
			sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
			sourceChannel.truncate(offset);
			r.seek(offset);
			r.writeBytes(content);
			long newOffset = r.getFilePointer();
			targetChannel.position(0L);
			sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
			sourceChannel.close();
			targetChannel.close();
			tmpFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String changeContent(String content) {
		List ret;
		List stl = null;
		StringBuffer changeContent = new StringBuffer();
		try {
			ret = ma.analyze(content);
			// refine spacing
			ret = ma.postProcess(ret);
			// leave the best analyzed result
			ret = ma.leaveJustBest(ret);
			// divide result to setences
			stl = ma.divideToSentences(ret);

			Sentence st;
			Eojeol eojeol;
			// print the result
			for (int i = 0; i < stl.size(); i++) {
				st = (Sentence) stl.get(i);
				for (int j = 0; j < st.size(); j++) {
					eojeol = st.get(j);
					for (int k = 0; k < eojeol.size(); k++) {
						if (eojeol.get(k).getTag().startsWith("NN")) {
							changeContent.append(eojeol.get(k).getString() + " ");
						} else if (eojeol.get(k).getTag().startsWith("V")) {
							changeContent.append(eojeol.get(k).getString() + " ");
						}
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (changeContent.toString().isEmpty()) {
			try {
				ret = ma.analyze(content);
				// refine spacing
				ret = ma.postProcess(ret);
				// leave the best analyzed result
				ret = ma.leaveJustBest(ret);
				// divide result to setences
				stl = ma.divideToSentences(ret);

				Sentence st;
				Eojeol eojeol;
				// print the result
				for (int i = 0; i < stl.size(); i++) {
					st = (Sentence) stl.get(i);
					for (int j = 0; j < st.size(); j++) {
						eojeol = st.get(j);
						for (int k = 0; k < eojeol.size(); k++) {
							changeContent.append(eojeol.get(k).getString() + " ");
						}
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		return changeContent.toString();
	}

	private String getMessageToBot(String message) {
		String response = "";
		try {
			String urlParameters = "message=" + message + "&send=&user=wongu2";
			// Send data
			byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;
			String request = "http://127.0.0.1/ui_TESTBOT.php";
			URL url = new URL(request);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				response = response + line;
			}

			rd.close();
		} catch (Exception e) {
		}
		return response;
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
			jarr.put("시작하기");
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
	
	private JSONObject makeCustomButtonJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("type", "buttons");
			JSONArray jarr = new JSONArray();
			jarr.put(":알려준다.");
			jarr.put(":알려주지 않는다.");
			json.put("buttons", jarr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	private String makeMessageWithButton(String message) {
		JSONObject json = new JSONObject();
		JSONObject innerJson = new JSONObject();
		try {
			innerJson.put("text", message);
			json.put("message", innerJson);
			json.put("keyboard", makeCustomButtonJson());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	//--------------------------------------------------------------------------
	
	private JSONObject makeCustomButton2Json() {
		JSONObject json = new JSONObject();
		try {
			json.put("type", "buttons");
			JSONArray jarr = new JSONArray();
			jarr.put(":같은주제");
			jarr.put(":다른주제");
			json.put("buttons", jarr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	
	private String makeMessageWithButtonSubject(String message) {
		JSONObject json = new JSONObject();
		JSONObject innerJson = new JSONObject();
		try {
			innerJson.put("text", message);
			json.put("message", innerJson);
			json.put("keyboard", makeCustomButton2Json());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	//---------------------------------------------------------------------
	
	private JSONObject makeCustomButton3Json() {
		JSONObject json = new JSONObject();
		try {
			json.put("type", "buttons");
			JSONArray jarr = new JSONArray();
			jarr.put(":재시작");
			json.put("buttons", jarr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	private String makeMessageWithButton3(String message) {
		JSONObject json = new JSONObject();
		JSONObject innerJson = new JSONObject();
		try {
			innerJson.put("text", message);
			json.put("message", innerJson);
			json.put("keyboard", makeCustomButton3Json());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}

}
