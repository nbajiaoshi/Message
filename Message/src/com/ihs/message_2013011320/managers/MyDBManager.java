package com.ihs.message_2013011320.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.message_2013011320.managers.HSMessageManager;
import com.ihs.message_2013011320.managers.HSMessageManager.SendMessageCallback;
import com.ihs.message_2013011320.types.HSBaseMessage;
import com.ihs.message_2013011320.types.HSMessageType;
import com.ihs.message_2013011320.types.HSTextMessage;

import android.os.Handler;
import android.util.Log;

public class MyDBManager {
	private static class C{
		final private static String signature = "DaLuDaYu-";
		final private static String topConverstation = "topConverstation";
		final private static String MessageAlertSettings = "MessageAlertSettings";
	}
	
	public MyDBManager() {
		// TODO Auto-generated constructor stub
	}
	public static void deleteMessage(String msgID){// 自定义的方便的删除消息的接口
		HSBaseMessage message = new HSBaseMessage(HSMessageType.TEXT, null, 0, null, HSBaseMessage.PUSH_TAG_TEXT, true, HSAccountManager.getInstance().getMainAccount().getMID(), HSAccountManager.getInstance().getMainAccount().getMID(), new Date(HSAccountManager
                .getInstance().getServerTime()), HSBaseMessage.HSMessageStatus.SENDING, HSBaseMessage.HSMessageMediaStatus.DOWNLOADED, 1);
		message.setMsgID(msgID);
		List<HSBaseMessage> messages = new ArrayList<HSBaseMessage>();
		messages.add(message);
		HSMessageManager.getInstance().deleteMessages(messages);                
	}
	public static List<String> getTopConverstation(){
		List<String> s = new ArrayList<String>();
		HSBaseMessage message = HSMessageManager.getInstance().queryMessage(C.signature + C.topConverstation);
		if (message != null){
			Scanner topConverstation = new Scanner(((HSTextMessage)message).getText());
			while (topConverstation.hasNext())
				s.add(topConverstation.next());			
		}
		return s;
	}
	private static JSONObject messageAlertSettings = getMessageAlertSettings();
	public static void setTopConverstation(List<String> topConverstation){//获取置顶会话名单
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < topConverstation.size();i++){
			buffer.append(topConverstation.get(i));
			buffer.append(" ");
		}
		deleteMessage(C.signature + C.topConverstation);
		SimpleMessage info = new SimpleMessage(C.signature + C.topConverstation,buffer.toString());
		HSMessageManager.getInstance().insertMessage(info);
	}
	static void setMessageAlertSettings(){
		deleteMessage(C.signature + C.MessageAlertSettings);
		SimpleMessage info = new SimpleMessage(C.signature + C.MessageAlertSettings,"");
		info.setExtra(messageAlertSettings);
		HSMessageManager.getInstance().insertMessage(info);		
	}
	static JSONObject getMessageAlertSettings(){
		/*if (messageAlertSettings != null){
			return messageAlertSettings;
		}*/
		List<String> s = new ArrayList<String>();
		HSBaseMessage message = HSMessageManager.getInstance().queryMessage(C.signature + C.MessageAlertSettings);
		if (message != null){
			return message.getExtra();		
		}
		else
			return new JSONObject();
	}
	public static int defualtAlertSetting = 0;
	public static int getAlertSetting(String mid){ // 查询某人的消息设置，是否屏蔽，本来还想做更细的提醒类型
		/*if (messageAlertSettings == null)
			return defualtAlertSetting;*/
		try {			
			return messageAlertSettings.getInt(mid);
		} catch (JSONException e) {
			return defualtAlertSetting;
		}
	}
	public static void setAlertSetting(String mid,int i){// 设置某人的消息设置，是否屏蔽，本来还想做更细的提醒类型
		/*if (messageAlertSettings == null)
			messageAlertSettings = getMessageAlertSettings();*/
		try {
			messageAlertSettings.remove(mid);
			messageAlertSettings.put(mid, i);
			setMessageAlertSettings();			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("luyu", "luyu DBM Error:" + e.getMessage() + " "+mid+" "+i);
		}
	}

}
class SimpleMessage extends HSTextMessage{
	SimpleMessage(String msgID,String text){
		super(HSAccountManager.getInstance().getMainAccount().getMID(),text);
		super.setMsgID(msgID);		
	}
}
