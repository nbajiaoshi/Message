package com.ihs.demo.message_2013011320;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.message_2013011320.R;
import com.ihs.message_2013011320.managers.HSMessageManager;
import com.ihs.message_2013011320.managers.MyDBManager;
import com.ihs.message_2013011320.types.HSAudioMessage;
import com.ihs.message_2013011320.types.HSBaseMessage;
import com.ihs.message_2013011320.types.HSMessageType;
import com.ihs.message_2013011320.types.HSTextMessage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
public class MessageAdapter extends ArrayAdapter<HSBaseMessage> {
	private List<HSBaseMessage> messages;
    private Context context;
    DisplayImageOptions options;
    String myMid;
    int topNum;
    List<String> topMessage;
    public List<HSBaseMessage> getMessages(){
    	return messages;
    }
    public boolean isTop(int i){
    	return i < topNum;
    }
    synchronized private void update(int i){//内部命令，更新list里位置为i的项，使其放到从小到大排列他应该在的位置
    	if (i < topNum || i >= messages.size()){
    		if (i < topNum && !topMessage.contains(messages.get(i).getAnother(myMid))){
    			Log.e("luyu", "luyu:Message update error:" + messages.get(i).getAnother(myMid) + " " + i);
    		}
    		return;
    	}
    	HSBaseMessage tmp = messages.get(i);
    	if (topMessage.contains(tmp.getAnother(myMid))){
    		upToTop(i);
    		return;
    	}
    	while (i > topNum && tmp.getTimestamp().after(messages.get(i - 1).getTimestamp())){
    		messages.set(i,messages.get(i - 1));
    		i--;
    	}
    	while (i < messages.size() - 1 && tmp.getTimestamp().before(messages.get(i + 1).getTimestamp())){
    		messages.set(i,messages.get(i + 1));
    		i++;
    	}
    	messages.set(i,tmp);
    }
    synchronized public void update(HSBaseMessage message){//有新message来的时候，更新会话列表的显示
    	for (int i = 0;i < messages.size();i++)
    		if (message.getAnother(myMid).equals(messages.get(i).getAnother(myMid))){
    			messages.set(i,message);
    			update(i);
    			return;
    		}
    	messages.add(message);
    	update(messages.size() - 1);
    }
    synchronized public void remove(int i){//删除第i项
    	messages.remove(i);
    	if (i <topNum)
    		topNum--;
    }
    synchronized public void update(String mid){//以人为目标进行更新
    	for (int i = 0;i < messages.size();i++)
    		if (mid.equals(messages.get(i).getAnother(myMid))){
    			HSMessageManager.QueryResult result = HSMessageManager.getInstance().queryMessages(mid, 1, -1);
    			if (result.getCursor() == -1){
    				messages.remove(i);
    				if (i < topNum)
    					topNum--;
    			}
    			messages.set(i,result.getMessages().get(0));
    			update(i);
    			return;
    		}
    	HSMessageManager.QueryResult result = HSMessageManager.getInstance().queryMessages(mid, 1, -1);
    	if (result.getCursor() != -1){
    		messages.add(result.getMessages().get(0));
    		update(messages.size() - 1);
		}
    	
    }
    synchronized private void upToTop(int i){//将第i个联系人的会话置顶
    	HSBaseMessage tmp = messages.get(i);
    	for (int j = i;j > topNum;j--){
    		messages.set(j, messages.get(j - 1));
    	}
    	messages.set(topNum,tmp);
    	topNum++;
    }
    synchronized private void DownBelowTop(int i){//取消i的置顶
    	if (topNum >= messages.size() || i >= topNum)
    		return;
    	HSBaseMessage tmp = messages.get(i);
    	for (int j = i + 1;j < topNum;j++){
    		messages.set(j - 1, messages.get(j));
    	}
    	messages.set(topNum - 1,tmp);
    	topNum--;
    	update(topNum);
    }
    synchronized public void cancelTop(int i){ // 对外接口，取消i的置顶
    	String mid = messages.get(i).getAnother(myMid);
    	if (!topMessage.contains(mid))
    		return;
    	topMessage.remove(mid);
    	MyDBManager.setTopConverstation(topMessage);
    	if (i < topNum && topNum < messages.size()){
    		DownBelowTop(i);
    	}    	
    }
    synchronized public void setTop(int i){ // 对外，置顶
    	String mid = messages.get(i).getAnother(myMid);
    	if (topMessage.contains(mid))
    		return;
    	topMessage.add(mid);
    	MyDBManager.setTopConverstation(topMessage);
    	if (i >= topNum)
    		upToTop(i);
    }
    public MessageAdapter(Context context, int resource, List<HSBaseMessage> objects) {
        super(context, resource, objects);
        topNum = 0;
        topMessage = MyDBManager.getTopConverstation();
        this.messages = objects;
        this.context = context;
        myMid = HSAccountManager.getInstance().getMainAccount().getMID();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.chat_avatar_default_icon).showImageForEmptyUri(R.drawable.chat_avatar_default_icon)
                .showImageOnFail(R.drawable.chat_avatar_default_icon).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }    
	@SuppressWarnings("deprecation")
	public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        HSBaseMessage message = messages.get(position);
        convertView = inflater.inflate(R.layout.message_item, parent, false);
        TextView content = (TextView) convertView.findViewById(R.id.detail_content);
        TextView name = (TextView) convertView.findViewById(R.id.title_name);
        TextView time = (TextView) convertView.findViewById(R.id.title_time);
        TextView num = (TextView) convertView.findViewById(R.id.detail_content_num);
        ImageView head = (ImageView)convertView.findViewById(R.id.contact_avatar);
        Contact contact;
        if (position < topNum){
        	convertView.setBackgroundColor(Color.rgb(220,255,220));
        }
        if (message.getFrom().equals(myMid))
        	contact = FriendManager.getInstance().getFriend(message.getTo());
        else
        	contact = FriendManager.getInstance().getFriend(message.getFrom());
        name.setText(contact.getName());
        //[yyyy/MM/dd][HH:mm:ss]
        int unreadnum = HSMessageManager.getInstance().queryUnreadCount(message.getFrom());
        if (unreadnum <= 0){
        	num.setVisibility(View.INVISIBLE);        	
        }else{
        	num.setVisibility(View.VISIBLE);
        	if (unreadnum > 99)
        		num.setText("99+");
        	else
        		num.setText(new Integer(unreadnum).toString());
        }
        Date nowTime = new Date(System.currentTimeMillis());
        if (nowTime.getYear() != message.getTimestamp().getYear()){
        	time.setText(new SimpleDateFormat("yyyy/MM").format(message.getTimestamp()));
        }
        else {
        	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
	        String messageDate = dateFormat.format(message.getTimestamp());
	        if (messageDate.equals(dateFormat.format(nowTime)))
	        	time.setText(new SimpleDateFormat("HH:mm:ss").format(message.getTimestamp()));
	        else
	        	time.setText(messageDate);
        }
        switch (message.getType() ){
        case TEXT:
        	content.setText(((HSTextMessage)message).getText());
        	break;
        case IMAGE:
        	content.setText("[图片]");
        	break;
        case AUDIO:
        	content.setText("[语音]" + ((HSAudioMessage)message).getDuration()+"\"");
        	break;
        case LOCATION:
        	content.setText("发来一条定位");
        	break;
        default:
        	content.setText("未知消息");
        }        	
        ImageLoader.getInstance().displayImage("content://com.android.contacts/contacts/" + contact.getContactId(),head, options);
        return convertView;
    }
}
