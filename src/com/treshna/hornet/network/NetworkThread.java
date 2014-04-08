package com.treshna.hornet.network;

import java.util.LinkedList;
import java.util.Queue;

import android.os.Bundle;

public class NetworkThread extends Thread {
	 public  boolean is_networking = false;
	 private Queue<Integer> queue = new LinkedList<Integer>();
	 private Queue<Bundle> bundlequeue = new LinkedList<Bundle>(); 
	 private HornetDBService theService;
	 private int currentCall;
	 
	 private static NetworkThread instance;
	 
	 public NetworkThread() {
		 instance = this;
	 }
	 
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
				currentCall = queue.poll();
				Bundle bundle = bundlequeue.poll();
			   theService.startNetworking(currentCall, bundle);
			} else {
			   //the queue is empty, basically, we finish.
			}
		}
	}
	
	public void addNetwork(int call, Bundle bundle, HornetDBService parent){
		 if (queue.peek() == null ||currentCall != call) {
			queue.add(call);
			bundlequeue.add(bundle);
		 }
		 
		 if (parent != null) {
			 theService = parent;
		 }
		 
	}
	
	public static synchronized NetworkThread getInstance() {
		if (instance == null) {
			instance = new NetworkThread();
		}
		return instance;
	}
}
