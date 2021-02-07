package communication;

import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mail {

	private Stack<String> mail;
	private Queue<String> chat;
	
	public Mail() {
		mail = new Stack<String>();
		chat = new ConcurrentLinkedQueue<String>();
	}
	
	public void addChat(String text) {
		chat.offer(text);
	}
	
	public void addMail(String text) {
		mail.add(text);
	}
	
	public String getChat() {
		return chat.poll();
	}
	
	// includes chat!!
	public String getMail() {
		String res = getChat();
		
		if (res == null) {
			res = mail.pop();
		} else {
			res = "#" + res;
		}
		
		return "MAIL" + res;
	}

	public boolean hasMail() {
		return !mail.isEmpty() || !chat.isEmpty();
	}
	
}
