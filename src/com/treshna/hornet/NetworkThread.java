package com.treshna.hornet;

import java.util.LinkedList;
import java.util.Queue;

public class NetworkThread extends Thread {
	 public  boolean is_networking = false;
	 private Queue<Integer> queue = new LinkedList<Integer>();
	 private String theResource; 
	 private HornetDBService theService;
	 
	@Override
	public void run() {
		while (!queue.isEmpty()) {
			while (is_networking) {
				try {
					Thread.sleep(15000); //check if network is free every 15 seconds.
				} catch (InterruptedException e) { //interrupted   
				}
			}
			if (queue.peek() != null) {
				int currentCall = queue.poll(); 
			   //startNetworking(currentCall, intent);
			   theService.startNetworking(currentCall, theResource);
			} else {
			   //the queue is empty.
			}
		}
	}
	
	public void addNetwork(int call, String resourceid, HornetDBService parent){
		 if (queue.peek() == null ||queue.peek() != call) {
			queue.add(call);
		 }
		 if (resourceid != null) {
			 theResource = resourceid;
		 }
		 if (parent != null) {
			 theService = parent;
		 }
		 
	}
}
