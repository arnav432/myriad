package com.myriadtracker;

import java.io.IOException;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import emailSender.emailSenderHelper;
import trackerEngine.PriceTrackingEngine;

@SpringBootApplication
public class priceTrackerApplication {
	
	private static PriceTrackingEngine engine = new PriceTrackingEngine();
	
	private static Logger logger = LoggerFactory.getLogger(priceTrackerApplication.class);
	
	private static RestartEngine restarter = new RestartEngine();
	

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws MessagingException, InterruptedException, IOException {
		
		emailSenderHelper sender = new emailSenderHelper();
		
		SpringApplication.run(priceTrackerApplication.class, args);

		
		try {
			sender.sendWelcomeMessage();
			//engine.startEngine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("restarting engine...");
			restarter.restart();
		} 
		catch(Exception e) {
			e.printStackTrace();
			logger.info("restarting engine...");
			restarter.restart();
		}
		/*
		catch (MessagingException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
	}

}

