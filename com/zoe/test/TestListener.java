package com.zoe.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.zoe.Agent;

public class TestListener {
	public static void main(String args[]) throws IOException, TimeoutException{
		Agent listener = new Agent("listen");
		listener.start();
	}
}
