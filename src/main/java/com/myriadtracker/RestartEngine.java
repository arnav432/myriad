package com.myriadtracker;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emailSender.emailSenderHelper;
import trackerEngine.PriceTrackingEngine;

@RestController
public class RestartEngine {
	
	
	@RequestMapping("/restart")
	public void restart() throws IOException, MessagingException, InterruptedException {
		emailSenderHelper.sendRestartMessage();
		PriceTrackingEngine.startPriceTrackingEngine();
	}

}
