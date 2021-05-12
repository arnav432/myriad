package emailSender;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orderTracker.trackOrders;
import orderTrackingDatabase.orderLinks;

public class emailSenderHelper {

	public static Logger logger = LoggerFactory.getLogger(emailSenderHelper.class);

	public static final String RECIPIENT = "arnavmalhotra338@gmail.com";
	
	static String accountEmail = getEmail();
	
	static String password = getPassword();
	
	private static Properties properties = getProperties();
	
	static orderLinks orders = new orderLinks();
	
	static trackOrders tracker = new trackOrders();
	
	static Session session = Session.getInstance(properties, new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(accountEmail, password);
		}
	});

	public static void sendPriceDropMail(String LINK, float currentPrice, float lastObservedPrice)
			throws MessagingException {

		logger.info("Preparing to send messsage...");

		Message message = preparePriceDropMessage(session, accountEmail, LINK, currentPrice, lastObservedPrice);
		
		sendMail(message);

	}

	private static Message preparePriceDropMessage(Session session, String accountEmail, String LINK,
			float currentPrice, float lastObservedPrice) {
		Message message = new MimeMessage(session);

		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker");
			message.setText("Price for " + LINK + " has changed from " + lastObservedPrice + " to "
					+ currentPrice);
			return message;
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void sendWelcomeMessage() throws FileNotFoundException {
		logger.info("Sending Welcome Message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker - Welcome Message");
			message.setText("!!!!!!!!!!!!!Price Tracker Successfully Started!!!!!!!!!!!!!"+"\n"+tracker.getFileSize()+" items being tracked");
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void sendStockStatusMessage(String LINK, float currentPrice) {
		logger.info("Sending product stock status message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker - Status Message");
			message.setText("BINGO !!!! Product came back in stock..  "+LINK+" with price of "+currentPrice);
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void sendRestartMessage() throws FileNotFoundException {
		logger.info("Sending Restart Message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker - Restarted Message");
			message.setText("!!!!!!!!!!!!!Price Tracker Successfully Restarted!!!!!!!!!!!!!"+"\n"+tracker.getFileSize()+" items being tracked");
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendNewProductAdditionMessage(String productLink) throws FileNotFoundException {
		
		logger.info("Sending product addition message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker - product added Message");
			message.setText("!!!!!!!!!!!!!The following product has been added!!!!!!!!!!!!!"+"\n"+productLink+"\n"+tracker.getFileSize()+" items being tracked"+"\n"+"Restarting !!!");
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendProductDeletionMessage(String productLink) throws FileNotFoundException {
		
		logger.info("Sending product deletion message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker - product deleted Message");
			message.setText("!!!!!!!!!!!!!The following product has been deleted!!!!!!!!!!!!!"+"\n"+productLink+"\n"+tracker.getFileSize()+" items being tracked"+"\n"+"Restarting !!!");
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendVaccineAvailibilityMessage() {
		
		logger.info("Sending vaccine availablity message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Vaccine Slot Available");
			message.setText("!!!!!!!!!!!!!Vaccine is available!!!!!!!!!!");
			//message.setText("!!!!!!!!!!!!!The following product has been deleted!!!!!!!!!!!!!"+"\n"+productLink+"\n"+tracker.getFileSize()+" items being tracked"+"\n"+"Restarting !!!");
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}

	
	public void sendApplicationStopMessage() {
		logger.info("Sending aplication stopping message...");
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(accountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT));
			message.setSubject("Order Tracker - application stopped");
			message.setText("!!!!!!!!!!!!!Application has been stopped !!!");
			sendMail(message);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void sendMail(Message message) {
		try {
			Transport.send(message);
			logger.info("!!!!!!!!!!!!!!!!!!!!!Message sent successfully!!!!!!!!!!!!!!!!!!!!!");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}
	
	private static Properties getProperties()  {
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		return properties;
	}
	
	private static String getEmail() {
		return "myriadprojects123@gmail.com";
	}
	
	private static String getPassword() {
		return "Arnav@123";
	}

	
	

	

}
