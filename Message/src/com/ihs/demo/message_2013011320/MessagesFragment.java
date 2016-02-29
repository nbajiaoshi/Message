package com.ihs.demo.message_2013011320;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.message_2013011320.R;
import com.ihs.message_2013011320.managers.HSMessageManager;
import com.ihs.message_2013011320.types.HSBaseMessage;
import com.ihs.message_2013011320.types.HSTextMessage;

public class MessagesFragment extends Fragment implements OnItemLongClickListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private static ListView listView;
    public static MessageAdapter adapter = null;
    public static void resetAdapter(){
    	adapter = null;
    }
    String myMid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        myMid = HSAccountManager.getInstance().getMainAccount().getMID();
        listView = (ListView) view.findViewById(R.id.message_list);
        if (adapter == null){
        	Log.d("luyu", "luyu:now create");
        	adapter = new MessageAdapter(this.getActivity(), R.layout.message_item, new ArrayList<HSBaseMessage>());
        	List<Contact> contacts = FriendManager.getInstance().getAllFriends();
        	HSMessageManager.QueryResult result;
        	for (int i = 0;i < contacts.size();i++) if (!contacts.get(i).getMid().equals(myMid)){
        		result = HSMessageManager.getInstance().queryMessages(contacts.get(i).getMid(),1,-1);
        		if (result.getCursor() != -1)
            		adapter.update((result.getMessages().get(0)));
        	}
        }        
        listView.setAdapter(adapter);    
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if (adapter.getMessages().get(position).getTo().equals(myMid)){
            		chat(adapter.getMessages().get(position).getFrom());
            	}
            	else{
            		chat(adapter.getMessages().get(position).getTo());
            	}
            }
        });  
        listView.setOnItemLongClickListener(this);;
        refresh();
        return view;
    }
    void chat(String mid){
    	String name = FriendManager.getInstance().getFriend(mid).getName();
    	Intent intent = new Intent(this.getActivity(),ChatActivity.class);
    	intent.putExtra("toMid", mid);
    	intent.putExtra("name",name);
    	startActivity(intent);
    }
    void refresh() {
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());        
        final int i = position;

        builder.setItems(new String[] {"标记为已读",adapter.isTop(i)?"取消置顶":"置顶该聊天","删除该聊天"}, new DialogInterface.OnClickListener(){
			@SuppressLint("NewApi")
			@Override
			synchronized public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d("luyu","luyu:message:"+which+" " +i);
				switch (which){
				case 0:
					HSMessageManager.getInstance().markRead(adapter.getMessages().get(i).getAnother(myMid));
					refresh();
					break;
				case 1:
					if (adapter.isTop(i))
						adapter.cancelTop(i);
					else
						adapter.setTop(i);
					refresh();
					break;
				case 2:
					HSMessageManager.getInstance().deleteMessages(adapter.getMessages().get(i).getAnother(myMid));;					
					adapter.remove(i);
					refresh();
					break;
				}				
			}        	
        });  
        builder.show(); 
		return true;
	}
}
