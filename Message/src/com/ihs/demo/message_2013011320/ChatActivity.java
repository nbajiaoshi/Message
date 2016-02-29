package com.ihs.demo.message_2013011320;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.demo.message_2013011320.*;
import com.ihs.message_2013011320.R;
import com.ihs.message_2013011320.managers.HSMessageChangeListener;
import com.ihs.message_2013011320.managers.HSMessageManager;
import com.ihs.message_2013011320.managers.MyDBManager;
import com.ihs.message_2013011320.managers.HSMessageManager.SendMessageCallback;
import com.ihs.message_2013011320.types.*;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import test.contacts.demo.friends.api.HSContactFriendsMgr;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

@SuppressLint("NewApi")
public class ChatActivity extends HSActivity implements OnItemClickListener,OnItemLongClickListener,HSMessageChangeListener,OnClickListener,HSMessageManager.SendMessageCallback {
	private ListView listView;
	public static String soundFilePath;
    private ChatMessageAdapter adapter = null;
    EditText chatMessage;
    ImageButton sound,extra,back,setting,sendPicture,sendLocation,sendContact,movingExpressionTouch,expressionButton;
    String myMid,toMid,name;
    Button disappear,expressionSetting;
    RelativeLayout chatButtomBar,chatMain,extraMenu,settingMenu;
    FrameLayout movingExpressionLayout;
    GridView expressionList;
    RadioGroup expressionRadioBox;
    MediaRecorder mRecorder;
    int alertSetting;
    int MenuHeight;
    SoundPool soundPool=new SoundPool(12, 0,5);
    HashMap musicId=new HashMap();
    boolean MenuExpression,MenuExtra,MenuSetting,inSelfStatus;
	protected void onCreate(Bundle savedInstanceState) {			
        super.onCreate(savedInstanceState);
        Log.i("luyu", "luyu:must");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);	 
        soundFilePath = HSApplication.getContext().getCacheDir() + "/" + "audio_sound.amr";
        initButtomAndView();
        myMid = HSAccountManager.getInstance().getMainAccount().getMID();
        Intent intent = getIntent();
        toMid = intent.getStringExtra("toMid");
        name = intent.getStringExtra("name");
        HSMessageManager.getInstance().markRead(toMid);
        adapter = new ChatMessageAdapter(this, R.layout.cell_item_contact,new ArrayList<HSBaseMessage>(),toMid);
        listView.setAdapter(adapter);
        HSMessageManager.QueryResult history = HSMessageManager.getInstance().queryMessages(toMid, 20, -1);
        if (history.getCursor() != -1)
        	adapter.addUpsideDownMessage(history.getMessages()); 
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        ((TextView)findViewById(R.id.chat_name)).setText(name);
        //movingExpressionLayout.removeView(view);        
        MenuExpression = false;
        MenuExtra = false;
        MenuSetting = false;
        inSelfStatus = false;
        moveExtraMenu();
        moveSettingMenu();
        expressionList.setVisibility(View.INVISIBLE);
        expressionRadioBox.setVisibility(View.INVISIBLE);
        alertSetting = MyDBManager.getAlertSetting(toMid);
        HSMessageManager.getInstance().addListener(this, new Handler());
        switch (alertSetting){
        case 1:
        	disappear.setText("取消屏蔽此人");
        	break;
        default: 
        }
        setExpressionListAdapter();
        expressionList.setOnItemClickListener(this);
        musicId.put(1, soundPool.load(this, R.raw.message_ringtone_sent, 1));
        musicId.put(2, soundPool.load(this, R.raw.message_ringtone_received, 1));
        buildExpressionSettingDialog();
        refresh();
        Log.i("luyu", "luyu:2");
        chatMessage.clearFocus();
    }
	int expressionParameter[] = {50,60,8,4,72};
	AlertDialog settingDialog;
	SeekBar bars[];
	void buildExpressionSettingDialog(){//初始化彩蛋表情设置的dialog
		settingDialog = new AlertDialog.Builder(ChatActivity.this).create();
		LinearLayout layout = new LinearLayout(ChatActivity.this);
		layout.setOrientation(LinearLayout.VERTICAL);
		OnSeekBarChangeListener listener = new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				for (int i = 0;i < 5;i++)
					if (seekBar == bars[i]){
						expressionParameter[i] = progress;
						return;
					}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		};
	    TextView text;
	    bars = new SeekBar[5];
	    String content[] = {"表情雨雨滴个数：","扫射子弹个数：","自定义轨迹表情个数","自定义轨迹表情间隔","表情大小"};
	    int MaxProgress[] = {100,100,50,30,200};
	    for (int i = 0;i < 5;i++){
	    	bars[i] = new SeekBar(ChatActivity.this);
	    	text = new TextView(ChatActivity.this);
	    	bars[i].setProgress(expressionParameter[i]);
	    	bars[i].setMax(MaxProgress[i]);
	    	bars[i].setOnSeekBarChangeListener(listener);
	    	text.setText(content[i]);	    	
	    	layout.addView(text); 
		    layout.addView(bars[i]);
	    }
	    settingDialog.setView(layout);
		
	}
	private void setExpressionListAdapter() {//为表情彩蛋的gridview添加adapter
		int []expressions = {R.drawable.e_00,
				R.drawable.e_01,R.drawable.e_02,R.drawable.e_03,R.drawable.e_04,
				R.drawable.e_05,R.drawable.e_06,R.drawable.e_07,R.drawable.e_08,
				R.drawable.e_09,R.drawable.e_10,R.drawable.e_11,R.drawable.e_12,
				R.drawable.e_13,R.drawable.e_14,R.drawable.e_15,R.drawable.e_16,
				R.drawable.e_17,R.drawable.e_18,R.drawable.e_19,R.drawable.e_20,
				R.drawable.e_21,R.drawable.e_22,R.drawable.e_23};
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<expressions.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("expressions", expressions[i]);
            data_list.add(map);
        }
		String []from = {"expressions"};
		int []to = {R.id.expression_image};
		expressionList.setAdapter(new SimpleAdapter(this, data_list, R.layout.expression_item, from, to));
	}
	@SuppressLint("NewApi")
	private void initButtomAndView(){// 初始化变量和view的映射
    	setContentView(R.layout.activity_chat);  
        listView = (ListView) findViewById(R.id.chat_list);
        expressionList = (GridView)findViewById(R.id.chat_expression_list);
        settingMenu = (RelativeLayout) findViewById(R.id.chat_setting_menu);
        chatButtomBar = (RelativeLayout)findViewById(R.id.chat_buttom_bar);
        chatMain = (RelativeLayout)findViewById(R.id.chat_main);
        extraMenu = (RelativeLayout)findViewById(R.id.extra_menu);
        movingExpressionLayout = (FrameLayout)findViewById(R.id.moving_expression_layout );
        chatMessage = (EditText)findViewById(R.id.chat_content);
        sound = (ImageButton)findViewById(R.id.chat_sound_button);
        extra = (ImageButton)findViewById(R.id.chat_extra_button);
        back = (ImageButton)findViewById(R.id.chat_back);
        setting = (ImageButton)findViewById(R.id.chat_setting);
        sendPicture = (ImageButton)findViewById(R.id.send_picture);
        sendLocation = (ImageButton)findViewById(R.id.send_location);
        sendContact = (ImageButton)findViewById(R.id.send_contact);
        movingExpressionTouch = (ImageButton)findViewById(R.id.moving_expression_touch);
        expressionButton = (ImageButton)findViewById(R.id.expression_button);
        expressionSetting = (Button)findViewById(R.id.expression_setting);
        disappear = (Button)findViewById(R.id.disappear);
        expressionRadioBox = (RadioGroup)findViewById(R.id.expression_radio_box);
        sound.setOnClickListener(this);        
        extra.setOnClickListener(this);
        back.setOnClickListener(this);
        setting.setOnClickListener(this);
        chatMessage.setOnClickListener(this);
        sendPicture.setOnClickListener(this);
        sendLocation.setOnClickListener(this);
        sendContact.setOnClickListener(this);  
        disappear.setOnClickListener(this);
        expressionButton.setOnClickListener(this);
        expressionSetting.setOnClickListener(this);
        final ChatActivity father = this;
        sound.setOnTouchListener(new OnTouchListener(){			
			long startMili;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d("luyu", "luyu:sound" + event.getAction() + " " + event.getX() + " " + event.getY());
				if (event.getAction() == 0){					
					mRecorder = new MediaRecorder();  
			        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
			        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			        mRecorder.setOutputFile(soundFilePath);  
			        try {  
			            mRecorder.prepare();
			            mRecorder.start();
			            Toast.makeText(ChatActivity.this,"开始录音", Toast.LENGTH_LONG).show();
			        } catch (IOException e) {  
			            Log.e("luyu", "luyu:prepare() failed" + e.getMessage());  
			        } 
			        startMili=System.currentTimeMillis(); 
					return true;
				}
				if (event.getAction() == 1){
			        mRecorder.stop();
			        mRecorder.release();
			        double time = (System.currentTimeMillis() - startMili)*0.001;
			        HSAudioMessage audioMessage = new HSAudioMessage(toMid,soundFilePath, time);
                    HSMessageManager.getInstance().send(audioMessage,father, new Handler());
					return true;
				}
				return false;
			}
        	
        });
        movingExpressionTouch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				inSelfStatus = true;
				Toast.makeText(ChatActivity.this,"选择你要用到的表情", Toast.LENGTH_SHORT).show();
				MenuExpression = true;
		        expressionList.setVisibility(View.VISIBLE);
				
			}
        	
        });
    }
	public String listToString(List<Coordinate> trace){ // 功能函数，将自定义表情轨迹的轨迹序列转为字符串
		StringBuffer b = new StringBuffer();
		for (int i = 0;i < trace.size();i++){
			b.append(trace.get(i).x + " " + trace.get(i).y +" ");
		}
		return b.toString();		
	}
	public List<Coordinate> stringToList(String s){//将字符串转为轨迹序列
		Scanner scanner = new Scanner(s);
		List<Coordinate> list = new ArrayList<Coordinate>();
		while (scanner.hasNext()){
			list.add(new Coordinate(Float.parseFloat(scanner.next()),Float.parseFloat(scanner.next())));
		}
		return list;
	}
	synchronized void moveExtraMenu(){// 让额外菜单栏显示/消失
		RelativeLayout.LayoutParams rlp = (LayoutParams) chatMain.getLayoutParams();
		RelativeLayout.LayoutParams mlp = (LayoutParams) extraMenu.getLayoutParams();
		if (rlp.bottomMargin == 0){
			rlp.bottomMargin = -mlp.bottomMargin;
			mlp.bottomMargin = 0;
		}
		else{
			mlp.bottomMargin = -rlp.bottomMargin;
			rlp.bottomMargin = 0;
		}
		chatMain.setLayoutParams(rlp);
		extraMenu.setLayoutParams(mlp);
	}
	synchronized void moveSettingMenu(){//让设置菜单栏显示/消失
		RelativeLayout.LayoutParams lp = (LayoutParams) settingMenu.getLayoutParams();
		if (lp.rightMargin == 0){
			lp.rightMargin = -lp.width;
		}
		else{
			lp.rightMargin = 0;
		}
		settingMenu.setLayoutParams(lp);
	}
    @Override
    public void onClick(View v) {
    	if (inSelfStatus){
    		expressionList.setVisibility(View.INVISIBLE);
    		expressionRadioBox.setVisibility(View.INVISIBLE);
    		MenuExpression = false;
    		inSelfStatus = false;
    	}    		
    	//------sub-button-------------------------------------------------------
    	if (v == disappear){
    		switch (alertSetting){
            case 0:
            	moveSettingMenu();
            	disappear.setText("取消屏蔽此人");
            	alertSetting = 1;
            	break;
            case 1:
            	moveSettingMenu();
            	disappear.setText("屏蔽此人消息");
            	alertSetting = 0;            	
            	break;
            default:
            	break;
            }
    		MenuSetting = !MenuSetting;
    		MyDBManager.setAlertSetting(toMid, alertSetting);
    		return;
    	}
    	if (v == expressionSetting){
    		settingDialog.show();
    		MenuSetting = !MenuSetting;
    		moveSettingMenu();
    	}
    	if (v == sendPicture){
    		Intent intent =new Intent();  
            /* 开启Pictures画面Type设定为image */  
            intent.setType("image/*");  
            /* 使用Intent.ACTION_GET_CONTENT这个Action */  
            intent.setAction(Intent.ACTION_GET_CONTENT);
            
            /* 取得相片后返回本画面 */  
            startActivityForResult(intent, 1);  
    		return;
    	}
    	if (v == sendLocation){
    		Log.d("luyu", "luyu:Location");
    		return;
    	}
    	if (v == sendContact){
    		Log.d("luyu", "luyu:sendContact");
    		return;
    	}
    	//---------------------------------------------------------------------------------------
    	if (v == expressionButton){
    		expressionList.setVisibility(MenuExpression?View.INVISIBLE:View.VISIBLE);
    		expressionRadioBox.setVisibility(MenuExpression?View.INVISIBLE:View.VISIBLE);
    		MenuExpression = !MenuExpression;
    		return;
    	}
    	else if (MenuExpression){
    		expressionList.setVisibility(View.INVISIBLE);
    		expressionRadioBox.setVisibility(View.INVISIBLE);
    		MenuExpression = false;
    	}
    	if (v == extra){
    		moveExtraMenu();
    		MenuExtra = !MenuExtra;
    		return;
    	}
    	else if (MenuExtra){
    		MenuExtra = false;
    		moveExtraMenu();
    	}    	
    	if (v == setting){
    		moveSettingMenu();
    		MenuSetting = !MenuSetting;
    		return;
    	}
    	else if (MenuSetting){
    		MenuSetting = false;
    		moveSettingMenu();
    	}
    	
    		
    	
    	
    	if (v == chatMessage && chatMessage.getText().length() > 0){    		
    		HSMessageManager.getInstance().send(
    				new HSTextMessage(toMid, chatMessage.getText().toString()),
    				this, new Handler());    		 
    		chatMessage.setText("");
    		return;
    	}
    	
    	if (v == sound){
    		Log.d("luyu", "luyu:sound");
    		return;
    	}
    	if (v == back){
    		super.onBackPressed();
    		return;
    	}
    	  	
    	Log.d("luyu","luyu:nofound");
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode > 100)
    		return;
        if (resultCode == RESULT_OK) {  
        	Bitmap bm = null; 
        	ContentResolver resolver = getContentResolver(); 
        	Uri originalUri = data.getData(); //获得图片的uri 
        	try {
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
        	String[] proj = {MediaStore.Images.Media.DATA}; 
        	@SuppressWarnings("deprecation")
			Cursor cursor = managedQuery(originalUri, proj, null, null, null); 
        	int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
        	cursor.moveToFirst(); 
        	String path = cursor.getString(column_index); 
        	Log.d("luyu", "luyu:send image path: " + path);
        	HSImageMessage imageMessage = new HSImageMessage(toMid, path);
        	HSMessageManager.getInstance().send(imageMessage, this, new Handler());
        	
        }  
        super.onActivityResult(requestCode, resultCode, data);  
    } 
    void refresh() {
    	adapter.notifyDataSetChanged();
    	listView.setSelection(adapter.getMessages().size() - 1);
    }
    @Override
    public void finish(){
    	Log.d("luyu", "luyu:ChatFinish");
        HSMessageManager.getInstance().removeListener(this);
    	super.finish();
    }

	@Override
	public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
		if (!success){
			Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT);
		}
		soundPool.play((Integer) musicId.get(1),1,1, 0, 0, 1);
		if (success){
			switch (message.getType()){
			case TEXT:
				Log.d("luyu", "luyu:"+message.getChatterMid()+":" + ((HSTextMessage)message).getText());
				break;
			default:
				Log.d("luyu", "luyu:"+message.getTypeString()+" send successful");
			}
			
			refresh();
		}
	}

	@Override
	public void onMessageChanged(HSMessageChangeType changeType, List<HSBaseMessage> messages) {
		// TODO Auto-generated method stub
		
		if (changeType == HSMessageChangeListener.HSMessageChangeType.ADDED){
			for (int i = 0;i < messages.size();i++)
				if (messages.get(i).getFrom().equals(myMid)){
					if (alertSetting != 1){
						soundPool.play((Integer) musicId.get(2),1,1, 0, 0, 1);
						break;
					}
					messages.remove(i);
					i--;
				}
			for (int i = 0;i < messages.size();i++)
				if (messages.get(i).getAnother(myMid).equals(toMid) && messages.get(i).getType() == HSMessageType.TEXT && ((HSTextMessage)messages.get(i)).getText().equals("[彩蛋表情]")){
					JSONObject j = messages.get(i).getExtra();
					if (j == null)
						continue;
					try {
						if (j.getString(("ExpressionType")).equals("rainy")){
							new MovingExpressionThread(movingExpressionLayout,ChatActivity.this,new Handler(),j.getInt("ExpressionId"),j.getInt("ExpressionNum"),j.getInt("ExpressionSize")).start();
						}
						if (j.getString(("ExpressionType")).equals("shoot")){
							adapter.notifyPlay(j, movingExpressionLayout, new Handler());
						}
						if (j.getString(("ExpressionType")).equals("self")){
							List<Coordinate> trace = stringToList(j.getString("ExpressionTrace"));
							new MovingExpressionThread(movingExpressionLayout, this, new Handler(), trace, j.getInt("ExpressionNum"),j.getInt("ExpressionGap"),j.getInt("ExpressionId"),j.getInt("ExpressionSize")).start();
						}
					} catch (JSONException e) {
						Log.e("luyu", "luyu:get color expression error:"+e.getMessage());
					}
				}
			HSMessageManager.getInstance().markRead(toMid);
			adapter.addMessage(messages);
			refresh();
		}
	}

	@Override
	public void onTypingMessageReceived(String fromMid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOnlineMessageReceived(HSOnlineMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnreadMessageCountChanged(String mid, int newCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingRemoteNotification(JSONObject pushInfo) {
		// TODO Auto-generated method stub
		
	}
	synchronized private void eraseMessage(int i){
		HSBaseMessage message = adapter.getMessages().get(i);
		MyDBManager.deleteMessage(message.getMsgID());
		if (MessagesFragment.adapter != null){
			MessagesFragment.adapter.update(message.getAnother(myMid));
			MessagesFragment.adapter.notifyDataSetChanged();
		}					
		adapter.getMessages().remove(i);
		adapter.notifyDataSetChanged();
	}
	private void showTextMessageDialog(int position){
		final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);        
        final int i = position;
        builder.setItems(new String[] {"复制","删除"}, new DialogInterface.OnClickListener(){
			@SuppressLint("NewApi")
			@Override
			synchronized public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d("luyu","luyu:"+which+" " +i);
				switch (which){
				case 0:
					ClipboardManager clip = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					clip.setText(((HSTextMessage)adapter.getMessages().get(i)).getText());
					break;
				case 1:
					eraseMessage(i);
					break;
				}				
			}        	
        });  
        builder.show(); 
	}
	private void showImageMessageDialog(int position){
		final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);        
        final int i = position;
        builder.setItems(new String[] {"显示大图","删除"}, new DialogInterface.OnClickListener(){
			@SuppressLint("NewApi")
			@Override
			synchronized public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d("luyu","luyu:"+which+" " +i);
				switch (which){
				case 0:					
					JSONObject extras = new JSONObject();
					try {
						extras.put("big", true);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					adapter.getMessages().get(i).setExtra(extras );
					adapter.notifyDataSetChanged();
					break;
				case 1:
					eraseMessage(i);
					break;
				}				
			}        	
        });  
        builder.show(); 
	}
	private void showOtherMessageDialog(final int position,final View view){
		final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);        
        final int i = position;
        
        builder.setView(findViewById(R.layout.setting_bar));
        builder.setItems(new String[] {"删除"}, new DialogInterface.OnClickListener(){
			@SuppressLint("NewApi")
			@Override
			synchronized public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d("luyu","luyu:"+which+" " +i);
				switch (which){
				case 0:
					eraseMessage(i);
					break;
				}				
			}        	
        });  
        builder.show();
        
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		switch (adapter.getMessages().get(position).getType()){
		case TEXT:
			showTextMessageDialog(position);
			break;
		case IMAGE:
			showImageMessageDialog(position);
			break;
		default:
			//settingDialog.show();
			showOtherMessageDialog(position,view);		
		}
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HSBaseMessage message;
		if (view.getParent() == listView){
			message = adapter.getMessages().get(position);
			switch (message.getType()){
			case TEXT:
				if (((HSTextMessage)message).getText().equals("[彩蛋表情]")){
					JSONObject j = message.getExtra();
					if (j == null)
						break;
					try {
						if (j.getString(("ExpressionType")).equals("rainy")){
							new MovingExpressionThread(movingExpressionLayout,ChatActivity.this,new Handler(),j.getInt("ExpressionId"),j.getInt("ExpressionNum"),j.getInt("ExpressionSize")).start();
						}
						if (j.getString(("ExpressionType")).equals("shoot")){
							new MovingExpressionThread(movingExpressionLayout,ChatActivity.this,new Handler(),
									view.findViewById(R.id.chat_image),view,
									message.getFrom().equals(myMid)?-1:1,j.getInt("ExpressionId"),j.getInt("ExpressionNum"),j.getInt("ExpressionSize")).start();
						}
						if (j.getString(("ExpressionType")).equals("self")){
							List<Coordinate> trace = stringToList(j.getString("ExpressionTrace"));
							new MovingExpressionThread(movingExpressionLayout, this, new Handler(), trace, j.getInt("ExpressionNum"),j.getInt("ExpressionGap"),j.getInt("ExpressionId"),j.getInt("ExpressionSize")).start();
						}
						
					} catch (JSONException e) {
						Log.e("luyu", "luyu:get color expression error:"+e.getMessage());
					}
				}
				break;
			case AUDIO:
				String audioPath = ((HSAudioMessage)message).getAudioFilePath();			
				MediaPlayer player =new MediaPlayer();
				try {
					player.setDataSource(audioPath);
					player.prepare();
					player.start();
				} catch (IllegalArgumentException e) {
					Log.e("luyu","luyu:"+e.getMessage());
				} catch (SecurityException e) {
					Log.e("luyu","luyu:"+e.getMessage());
				} catch (IllegalStateException e) {
					Log.e("luyu","luyu:"+e.getMessage());
				} catch (IOException e) {
					Log.e("luyu","luyu:"+e.getMessage());
				}
				break;
			case IMAGE:
				Log.e("luyu","luyu: Image");
				JSONObject extras = new JSONObject();
				try {
					extras.put("big", true);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.getMessages().get(position).setExtra(extras );
				adapter.notifyDataSetChanged();
				break;
			default:		
			}
			return;
		}
		//expressionList == view.getParent()
		if (!inSelfStatus){
			JSONObject j;
			Log.e("luyu","luyu: press expression:" + position+ " ");
			switch (expressionRadioBox.getCheckedRadioButtonId()){
			case R.id.expression_radioButton_default:
				chatMessage.setText(chatMessage.getText() + "[/"+position+"]");
				chatMessage.setSelection(chatMessage.getText().length());
				break;
			case R.id.expression_radioButton_rainy:
				j = new JSONObject();
				try {
					j.put("ExpressionType", "rainy");
					j.put("ExpressionId", position);
					j.put("ExpressionNum", expressionParameter[0]);
					j.put("ExpressionSize", expressionParameter[4]);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				message = new HSTextMessage(toMid, "[彩蛋表情]");
				message.setExtra(j);
				HSMessageManager.getInstance().send(message, new SendMessageCallback() {
	                public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {}
	            }, new Handler());	    		
				break;
			case R.id.expression_radioButton_shoot:
				j = new JSONObject();
				try {
					j.put("ExpressionType", "shoot");
					j.put("ExpressionId", position);
					j.put("ExpressionNum", expressionParameter[1]);
					j.put("ExpressionSize", expressionParameter[4]);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				message = new HSTextMessage(toMid, "[彩蛋表情]");
				message.setExtra(j);
				HSMessageManager.getInstance().send(message, new SendMessageCallback() {
	                public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {}
	            }, new Handler());
				break;
			}
			expressionList.setVisibility(View.INVISIBLE);
	    	expressionRadioBox.setVisibility(View.INVISIBLE);
	    	MenuExpression = false;
			return;
		}
		else{
			final int  i = position;
			inSelfStatus = false;			
			Toast.makeText(ChatActivity.this,"将手指在屏幕上滑动以创造轨迹", Toast.LENGTH_LONG).show();
			movingExpressionLayout.setOnTouchListener(new OnTouchListener(){
	        	List<Coordinate> scrollTrace = new ArrayList<Coordinate>();
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:
						scrollTrace = new ArrayList<Coordinate>();
						break;
					case MotionEvent.ACTION_MOVE:
						scrollTrace.add(new Coordinate(event.getX(),event.getY()));
						break;
					case MotionEvent.ACTION_UP:
						
						movingExpressionLayout.setOnTouchListener(null);
						expressionList.setVisibility(View.INVISIBLE);
						HSTextMessage message= new HSTextMessage(toMid, "[彩蛋表情]");
						JSONObject j = new JSONObject();
						try {
							j.put("ExpressionType", "self");
							j.put("ExpressionId", i);
							j.put("ExpressionTrace", listToString(scrollTrace));
							j.put("ExpressionNum", expressionParameter[2]);
							j.put("ExpressionGap", expressionParameter[3]);
							j.put("ExpressionSize", expressionParameter[4]);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						message.setExtra(j);
						HSMessageManager.getInstance().send(message, new SendMessageCallback() {
	                        public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {}
	                    }, new Handler());
						Log.d("luyu", "luyu:self-determin");
						
						Log.d("luyu", "luyu:self-determin-get");
						
						//new MovingExpressionThread(movingExpressionLayout, father, new Handler(), scrollTrace, 8,4,1).start();
						break;
					}
					// TODO Auto-generated method stub
					return true;
				}
	        	
	        });
		}
		
		
	}
	public void onResume() {
        super.onResume();
        MainActivity.recentMainActivity.refreshNotification();
    }
}
