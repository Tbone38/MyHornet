package com.treshna.hornet;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Bundle;

public class NetworkThread extends Thread {
	 public  boolean is_networking = false;
	 private Queue<Integer> queue = new LinkedList<Integer>();
	 private Queue<Bundle> bundlequeue = new LinkedList<Bundle>(); 
	 private HornetDBService theService;
	 
	@Override
	public void run() {
		while (!queue.isEmpty()) {
			while (is_networking) {
				try {
					Thread.sleep(10000); //check if network is free every 10 seconds.
				} catch (InterruptedException e) { //interrupted   
				}
			}
			if (queue.peek() != null) {
				int currentCall = queue.poll();
				Bundle bundle = bundlequeue.poll();
			   theService.startNetworking(currentCall, bundle);
			} else {
			   //the queue is empty.
			}
		}
	}
	
	public void addNetwork(int call, Bundle bundle, HornetDBService parent){
		 if (queue.peek() == null ||queue.peek() != call) {
			queue.add(call);
			bundlequeue.add(bundle);
		 }
		 
		 if (parent != null) {
			 theService = parent;
		 }
		 
	}
}
