package com.ihs.demo.message_2013011320;

import test.contacts.demo.friends.api.HSContactFriendsMgr;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.utils.HSLog;
import com.ihs.message_2013011320.R;
import com.ihs.message_2013011320.managers.HSMessageManager;
import com.ihs.message_2013011320.types.*;

public class MainActivity extends HSActionBarActivity {
	public static MainActivity recentMainActivity;
    private final static String TAG = MainActivity.class.getName();
    private Tab tabs[];
    final int notificationID = 7566;    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recentMainActivity = this;
        ActionBar bar = this.getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        int[] tabNames = { R.string.contacts, R.string.messages, R.string.settings, R.string.sample };
        tabs = new Tab[4];
        for (int i = 0; i < 3; i++) {
            Tab tab = bar.newTab();
            tabs[i] = tab;
            tab.setText(tabNames[i]);
            tab.setTabListener(new TabListener() {

                @Override
                public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
                    HSLog.d(TAG, "unselected " + arg0);
                }

                @Override
                public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
                    HSLog.d(TAG, "selected " + arg0);
                    if (tabs[0] == arg0) {
                        Fragment f = new ContactsFragment();
                        arg1.replace(android.R.id.content, f);
                    } else if (tabs[1] == arg0) {
                        Fragment f = new MessagesFragment();
                        arg1.replace(android.R.id.content, f);
                    } else if (tabs[2] == arg0) {
                        Fragment f = new SettingsFragment();
                        arg1.replace(android.R.id.content, f);
                    } else if (tabs[3] == arg0) {
                        Fragment f = new SampleFragment();
                        arg1.replace(android.R.id.content, f);
                    }
                }

                @Override
                public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
                    HSLog.d(TAG, "reselected " + arg0);
                }
            });
            bar.addTab(tab);
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private List<String> notifyMidList = new ArrayList<String>();
    private int notifyNum = 0;
    public void refreshNotification(){ //清除所有notification，目的是进入应用的时候应该自动清除
    	final NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	notificationManager.cancelAll();
    	notifyNum = 0;
    	notifyMidList.clear();
    }
 
	public void buildNotification(HSBaseMessage message){ // 创建notification 并提示
    	String fromMid = message.getFrom();
    	if (fromMid.equals(HSAccountManager.getInstance().getMainAccount().getMID()))
    		return;
    	if (!notifyMidList.contains(fromMid))
    		notifyMidList.add(fromMid);
    	notifyNum++;
    	Notification notify = new Notification();
    	notify.icon = R.drawable.icon;
    	notify.when = System.currentTimeMillis();
    	notify.flags = Notification.FLAG_AUTO_CANCEL;  
    	notify.defaults |= Notification.DEFAULT_VIBRATE; 
    	long[] vibrate = {0,100,200,300}; 
    	notify.vibrate = vibrate;
    	Intent i = new Intent(MainActivity.this,ChatActivity.class);
    	i.putExtra("toMid", fromMid);
    	String title = FriendManager.getInstance().getFriend(fromMid).getName();
    	i.putExtra("name",title);
    	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
    	PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
    	//int unread = HSMessageManager.getInstance().queryUnreadCount(fromMid);
    	String text;
    	if (notifyMidList.size() <= 1){
    		switch (message.getType()){
	    	case TEXT:
	    		text = ((HSTextMessage)message).getText();
	    		break;
	    	case IMAGE:
	    		text = "[图片]";
	        	break;
	        case AUDIO:
	        	text = "[语音]" + ((HSAudioMessage)message).getDuration()+"\"";
	        	break;
	        case LOCATION:
	        	text = "发来一条定位";
	        	break;
	    	default:
	    		text = "未知消息";
	    	}
    	}
    	else{
    		title = "Message";
    		text = "你收到了来自"+notifyMidList.size()+"位朋友给你发的"+notifyNum+"条消息";
    		
    	}
    	/*if (unread > 1){
    		text = "发来"+unread+"条消息";
    	}
    	else{
    		HSMessageManager.QueryResult result =HSMessageManager.getInstance().queryMessages(fromMid, 1, -1);
    		if (result.getCursor() != -1){
    			HSBaseMessage message = result.getMessages().get(0);
    			
    			
    		}
    		else
    			text = "No Message Error";
    	}*/
    	notify.setLatestEventInfo(MainActivity.this, title,text, contentIntent);
    	//notification = notify;
    	final NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	notificationManager.notify(notificationID, notify);
    	
    }
    public void onStart(){
    	super.onStart();
    	Log.i("luyu", "luyu:main start");
    }
    public void onPause(){
    	super.onPause();
    	Log.i("luyu", "luyu:main Pause");
    }
    public void onStop(){
    	super.onStop();
    	Log.i("luyu", "luyu:main Stop");
    }
    public void onResume() {
        super.onResume();
        Log.i("luyu", "luyu:main resume");
        refreshNotification();
        HSMessageManager.getInstance().pullMessages();
        HSContactFriendsMgr.startSync(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
