package trackerEngine;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myriadtracker.RestartEngine;

import orderTracker.trackOrders;
import orderTrackingDatabase.Product;
import vaccineAvailibility.CheckVaccineSlot;

public class PriceTrackingEngine {
	
	

	private static Logger logger = LoggerFactory.getLogger(PriceTrackingEngine.class);
	
	
	
	private static final long WAIT_DURATION_IN_MINUTES = 1; 
	
	private static final String LINK = "https://www.google.com/";
	
	private static final long TIMEOUT_IN_SECONDS = 5;
	
	private static final long STEP_SIZE_IN_SECONDS = 5;

	private static final double WAIT_DURATION_FOR_VACCINE = 0.5;
	
	private static ArrayList<Product> productList = new ArrayList<>();
	
	private static CheckVaccineSlot slotChecker = new CheckVaccineSlot();
	
	private static RestartEngine restarter = new RestartEngine();


	
	public static void startPriceTrackingEngine() throws IOException, MessagingException, InterruptedException {
		
		logger.info("Starting engine...........");
		trackOrders tracker = new trackOrders();
		productList = tracker.loadProductPriceData();
		logger.info("List size is "+productList.size());
		while(true) {
			//tracker.trackFlipkartOrders();
			tracker.trackFlipkartOrdersV2(productList);
			//tracker.trackAmazonOrders();
			//slotChecker.checkSlot();
			logger.info("Going to sleep for "+WAIT_DURATION_IN_MINUTES+" minutes..............");
			stayAlive();
		}
		
		
		
	}
	
	public static void startVaccineSlotEngine() throws IOException, MessagingException, InterruptedException {
		
		logger.info("Starting vaccine checking engine...........");
		
		while(true) {
			slotChecker.checkSlot();
			
			logger.info("Going to sleep for "+WAIT_DURATION_IN_MINUTES+" minutes..............");
			
			stayAliveForVaccine(WAIT_DURATION_FOR_VACCINE);
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
				e.printStackTrace();
				logger.info("restarting engine...");
				restarter.restart();
			}
			catch(SocketTimeoutException e) {
				e.printStackTrace();
				logger.info("restarting engine...");
				restarter.restart();
			}
			catch(Exception e) {
				e.printStackTrace();
				logger.info("restarting engine...");
				restarter.restart();
			}

	}
	
	public static void stayAliveForVaccine(double waitDurationForVaccine) throws IOException, MessagingException, InterruptedException {
		long counter = 0;
		try {
			for(;counter<=(waitDurationForVaccine*60);counter+=STEP_SIZE_IN_SECONDS) {
				Thread.sleep(STEP_SIZE_IN_SECONDS*1000);
				Jsoup.connect(LINK).timeout(((int) TIMEOUT_IN_SECONDS)*1000).get();
			}
			}
			catch(SocketException e) {
				e.printStackTrace();
				logger.info("restarting engine...");
				restarter.restart();
			}
			catch(SocketTimeoutException e) {
				e.printStackTrace();
				logger.info("restarting engine...");
				restarter.restart();
			}
			catch(Exception e) {
				e.printStackTrace();
				logger.info("restarting engine...");
				restarter.restart();
			}

	}
	
	
}
