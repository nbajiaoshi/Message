package com.ihs.demo.message_2013011320;
import com.ihs.account.api.account.HSAccountManager;
import com.ihs.demo.message_2013011320.*;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.ihs.message_2013011320.R;
import com.ihs.message_2013011320.types.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMessageAdapter extends ArrayAdapter<HSBaseMessage> {
	private List<HSBaseMessage> messages;
    private Context context;
    DisplayImageOptions options;
    String myMid,toMid;
    boolean needtoPlay;
    JSONObject player;
    FrameLayout movingExpressionLayout;
    Handler handler;
    public void notifyPlay(JSONObject player,FrameLayout movingExpressionLayout,Handler handler){
    	needtoPlay = true;
    	this.player = player;
    	this.movingExpressionLayout = movingExpressionLayout;
    	this.handler = handler;
    }
    public void addMessage(List<HSBaseMessage> list){//带过滤功能的添加消息
    	HSBaseMessage message;
    	for (int i = 0;i < list.size();i++){    		
    		message = list.get(i);
    		if ((message.getFrom().equals(toMid) && message.getTo().equals(myMid))
    			|| (message.getTo().equals(toMid) && message.getFrom().equals(myMid))){
    			messages.add(message);
    		}
    	}    		
    }
    public void addUpsideDownMessage(List<HSBaseMessage> list){ // 吧消息倒着加进去
    	HSBaseMessage message;
    	for (int i = list.size() - 1;i >= 0;i--){
    		message = list.get(i);
    		if ((message.getFrom().equals(toMid) && message.getTo().equals(myMid))
    			|| (message.getTo().equals(toMid) && message.getFrom().equals(myMid))){
    			messages.add(message);
    		}
    	}    		
    }
    public List<HSBaseMessage> getMessages(){
    	return messages;
    }
	public ChatMessageAdapter(Context context, int resource, List<HSBaseMessage> objects,String toMid) {
        super(context, resource, objects);
        this.toMid = toMid;
        this.messages = objects;
        this.context = context;
        needtoPlay = false;
        myMid = HSAccountManager.getInstance().getMainAccount().getMID();
        /*options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.chat_avatar_default_icon).showImageForEmptyUri(R.drawable.chat_avatar_default_icon)
                .showImageOnFail(R.drawable.chat_avatar_default_icon).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();*/
    }
	Bitmap convertToBitmap(String path, int w) {
		
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
           // 返回为空
        BitmapFactory.decodeFile(path, opts);
    	int width = opts.outWidth;
    	int height = opts.outHeight;
    	float scaleWidth = 0.f, scaleHeight = 0.f;
    	int h;
    	if (width > w) {
    		h = height * w /width;
    		// 缩放
    	    scaleWidth = ((float) width) / w;
    	    scaleHeight = scaleWidth;
    	}
    	else
    		return BitmapFactory.decodeFile(path);
    		
    	opts.inJustDecodeBounds = false;
    	float scale = Math.max(scaleWidth, scaleHeight);
    	opts.inSampleSize = (int)scale;
    	Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
    	if (bitmap == null)
    		return null;
    	WeakReference<Bitmap> weak = new WeakReference<Bitmap>(bitmap);
    	return Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == messages.size() - 1){
			Log.i("luyu", "luyu:chat build last message");
		}
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        HSBaseMessage message = messages.get(position);
        ImageView head;
        if (message.getType() == HSMessageType.IMAGE){//根据消息类型及接收方决定layout
        	if (message.getFrom().equals(myMid))
            	convertView = inflater.inflate(R.layout.chat_list_picture_send, parent, false);
            else
            	convertView = inflater.inflate(R.layout.chat_list_picture_receive, parent, false);
        }
        else{
        	if (message.getFrom().equals(myMid))
            	convertView = inflater.inflate(R.layout.chat_list_item_send, parent, false);
            else
            	convertView = inflater.inflate(R.layout.chat_list_item_receive, parent, false);
        }        
        head = (ImageView)convertView.findViewById(R.id.chat_image);
        Contact contact = FriendManager.getInstance().getFriend(message.getFrom());
        String text;
        Pattern pattern;
        Matcher matcher;
        SpannableStringBuilder builder;
        switch (message.getType()){
        case TEXT:
        	text = ((HSTextMessage)message).getText();
        	builder = new SpannableStringBuilder(text);
        	JSONObject j = message.getExtra();
        	if (text.equals("[彩蛋表情]") && j != null){
        		try {
        			//Log.i("luyu", "luyu:span begin" + j.getInt("ExpressionId"));
					builder.setSpan(  
					        new ImageSpan(context, R.drawable.e_00 + j.getInt("ExpressionId")), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					((TextView) convertView.findViewById(R.id.Chat_text)).setText(builder);
					//Log.i("luyu", "luyu:span end");
					break;
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}        	
        	pattern = Pattern.compile("\\[/\\d+\\]");  
        	matcher = pattern.matcher(text);  
        	while (matcher.find()) {
        		int num = Integer.parseInt(text.substring(matcher.start() + 2,matcher.end() - 1));
        		if (num < 24)
        			builder.setSpan(  
        	            new ImageSpan(context, R.drawable.e_00 + num), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        	} 
        	((TextView) convertView.findViewById(R.id.Chat_text)).setText(builder);
        	
        	break;
        case IMAGE: 
        	JSONObject extras = message.getExtra();
        	String path;
        	try {
        		if (extras != null){
        			extras.getBoolean("big");
        			path = ((HSImageMessage)message).getNormalImageFilePath();
        		}
        		else{
        			path = ((HSImageMessage)message).getThumbnailFilePath();
        		}
				
			} catch (JSONException e) {
				path = ((HSImageMessage)message).getThumbnailFilePath();
			}
        	Bitmap bitmap = convertToBitmap(path,720);
        	//Bitmap bitmap = BitmapFactory.decodeFile(path);
        	
        	if (bitmap != null)
        		((ImageView)convertView.findViewById(R.id.Chat_picture)).setImageBitmap(bitmap);        	
        	break;
        case AUDIO: //"[语音]" + ((HSAudioMessage)message).getDuration()+"\""
        	if (message.getFrom().equals(myMid)){
        		text ="$" +  ((HSAudioMessage)message).getDuration()+"\"";
            	builder = new SpannableStringBuilder(text);        	
            	builder.setSpan(  
        	            new ImageSpan(context,R.drawable.chat_send_audio_play ), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	}
        	else{
        		text = ((HSAudioMessage)message).getDuration()+"\"" + "$";
            	builder = new SpannableStringBuilder(text);        	
            	builder.setSpan(  
        	            new ImageSpan(context,R.drawable.chat_receive_audio_play ), text.length() - 1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	}
        	
        	((TextView) convertView.findViewById(R.id.Chat_text)).setText(builder);
        	break;
        case LOCATION:
        	((TextView) convertView.findViewById(R.id.Chat_text)).setText("发来一条定位");
        	break;
        default:
        	((TextView) convertView.findViewById(R.id.Chat_text)).setText("未知消息");
        }         	
        ImageLoader.getInstance().displayImage("content://com.android.contacts/contacts/" + contact.getContactId(),head, options);
        if (needtoPlay && position == messages.size() - 1){
        	needtoPlay = false;
        	try {
				if (player.getString(("ExpressionType")).equals("shoot")){
					new MovingExpressionThread(movingExpressionLayout,context,handler,
							head,convertView,
							message.getFrom().equals(myMid)?-1:1,player.getInt("ExpressionId"),player.getInt("ExpressionNum"),player.getInt("ExpressionSize")).start();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return convertView;
    }
}
