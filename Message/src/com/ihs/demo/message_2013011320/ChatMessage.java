package com.ihs.demo.message_2013011320;

public class ChatMessage {
	String mid;
	String text;
	public ChatMessage(String mid,String text) {
		this.mid = mid;
		this.text = text;
		// TODO Auto-generated constructor stub
	}
	public String getMid(){
		return mid;
	}
	public String getText(){
		return text;
	}

}
