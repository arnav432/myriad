package trackerEngine;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orderTracker.trackOrders;
import orderTrackingDatabase.Product;

public class PriceTrackingEngine {
	
	

	private static Logger logger = LoggerFactory.getLogger(PriceTrackingEngine.class);
	
	
	
	private static final long WAIT_DURATION_IN_MINUTES = 5; 
	
	private static final String LINK = "https://www.google.com/";
	
	private static final long TIMEOUT_IN_SECONDS = 5;
	
	private static final long STEP_SIZE_IN_SECONDS = 5;
	
	private static ArrayList<Product> productList = new ArrayList<>();
	
	public static void startEngine() throws IOException, MessagingException, InterruptedException {
		
		logger.info("Starting engine...........");
		trackOrders tracker = new trackOrders();
		productList = tracker.loadProductPriceData();
		logger.info("List size is "+productList.size());
		while(true) {
			//tracker.trackFlipkartOrders();
			tracker.trackFlipkartOrdersV2(productList);
			//tracker.trackAmazonOrders();
			logger.info("Going to sleep for "+WAIT_DURATION_IN_MINUTES+" minutes..............");
			stayAlive();
		}
		
		
		
	}
	
	

	public static void stayAlive() throws IOException, MessagingException, InterruptedException {
		long counter = 0;
		try {
			for(;counter<=(WAIT_DURATION_IN_MINUTES*60);counter+=STEP_SIZE_IN_SECONDS) {
				Thread.sleep(STEP_SIZE_IN_SECONDS*1000);
				Jsoup.connect(LINK).timeout(((int) TIMEOUT_IN_SECONDS)*1000).get();
			}
			}
			catch(SocketException e) {
				logger.info("restarting engine...");
				PriceTrackingEngine.startEngine();
				e.printStackTrace();
				
			}

	}
	
	
}
