package com.myriadtracker;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import emailSender.emailSenderHelper;
import trackerEngine.PriceTrackingEngine;

@SpringBootApplication
public class priceTrackerApplication {
	
	private static PriceTrackingEngine engine = new PriceTrackingEngine();

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		
		emailSenderHelper sender = new emailSenderHelper();
		
		SpringApplication.run(priceTrackerApplication.class, args);
		
		try {
			sender.sendWelcomeMessage();
			engine.startEngine();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}

