package com.myriadtracker;

import java.awt.AWTException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import emailSender.emailSenderHelper;
import orderTracker.trackOrders;
import orderTrackingDatabase.Product;
import orderTrackingDatabase.orderLinks;
import trackerEngine.PriceTrackingEngine;

@Controller
public class Displaytext {

	static orderLinks orders = new orderLinks();

	static trackOrders tracker = new trackOrders();

	public static Logger logger = LoggerFactory.getLogger(Displaytext.class);

	RestartEngine backupengine = new RestartEngine();

	static emailSenderHelper sender = new emailSenderHelper();

	ArrayList<Product> productList = new ArrayList<>();

	PriceTrackingEngine engine = new PriceTrackingEngine();

	private static boolean restart = false;
	/*
	 * @ResponseBody
	 * 
	 * @RequestMapping("/") public String started() throws FileNotFoundException {
	 * 
	 * return "Tracking Started. "+tracker.getFileSize()+" items are being tracked";
	 * }
	 */

	@RequestMapping("/")
	public String started(Model model) {
		Product product = new Product();
		model.addAttribute("product", product);
		return "add";
	}

	@PostMapping("/add")
	public void addNewLink(Product product) throws IOException, MessagingException, InterruptedException {
		logger.info("New product added ->" + product.getProductLink() + ". Adding it to product file.");
		try {
			tracker.addNewProduct(product.getProductLink());
		} catch (CsvDataTypeMismatchException e) {
			restart = true;
		} catch (CsvRequiredFieldEmptyException e) {
			restart = true;
		} catch (IOException e) {
			restart = true;
		} catch (URISyntaxException e) {
			restart = true;
		} catch (Exception e) {
			restart = true;
		} finally {
			if (restart) {
				logger.info("error while adding new product to file, restarting...");
				backupengine.restart();
			}
		}
		logger.info("New Product added successfully. Sending confirmation message !!!");
		sender.sendNewProductAdditionMessage(product.getProductLink());
		backupengine.restart();
	}

	@PostMapping("/checkVaccine")
	public void startCheckingVaccine(Product product) throws IOException, MessagingException, InterruptedException, AWTException {

		logger.info("Starting to check slots for vaccine availibility");

		try {
			engine.startVaccineSlotEngine();
		}

		catch (Exception e) {
			restart = true;
		}

		finally {
			if (restart) {
				logger.info("error while startng to check vaccine slots...");
				backupengine.restartVaccineSlotCheckEngine();
			}
		}

	}

	@PostMapping("/startTracking")
	public void startTrackingOrder(Product product) throws IOException, MessagingException, InterruptedException {

		logger.info("Starting to track orders");

		try {
			engine.startPriceTrackingEngine();
		}

		catch (Exception e) {
			e.printStackTrace();
			restart = true;
		}

		finally {
			if (restart) {
				logger.info("error while startng to track orders...");
				backupengine.restart();
			}
		}

	}

	@PostMapping("/remove")
	public void removeLink(Product product) throws IOException, MessagingException, InterruptedException {
		logger.info("Product Link deletion request received ->" + product.getProductLink()
				+ ". preparing to delete product file.");
		restart = false;
		try {
			productList = tracker.loadProductPriceData();
			// Product notNeededProduct = new Product();
			// notNeededProduct.setProductLink(product.getProductLink());
			ArrayList<Product> FinalProductList = new ArrayList<>();
			for (Product product2 : productList) {
				if (!product2.getProductLink().equalsIgnoreCase(product.getProductLink())) {
					FinalProductList.add(product2);
				}
			}

			tracker.writePojoToCsv(FinalProductList);
			for (Product product2 : FinalProductList) {
				logger.info(product2.toString());
			}
		} catch (CsvDataTypeMismatchException e) {
			restart = true;
		} catch (CsvRequiredFieldEmptyException e) {
			restart = true;
		} catch (IOException e) {
			restart = true;
		} catch (URISyntaxException e) {
			restart = true;
		} catch (Exception e) {
			restart = true;
		} finally {
			if (restart) {
				logger.info("error while removing product from file, restarting...");
				backupengine.restart();
			}
		}
		logger.info("Product deleted successfully. Sending confirmation message !!!");
		sender.sendProductDeletionMessage(product.getProductLink());
		backupengine.restart();
	}

	@PostMapping("/stop")
	public void stopApplication() throws IOException, MessagingException, InterruptedException {
		restart = false;
		logger.info("Sending stopping message !!!");
		sender.sendStopTrackingMessage();
		//System.exit(1);
		try {
			engine.stopRequested();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@PostMapping("/restart")
	public void restartApplication() throws IOException, MessagingException, InterruptedException {
		restart = false;
		logger.info("Sending restarted message !!!");
		sender.sendRestartMessage();
		//System.exit(1);
		try {
			backupengine.restart();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@PostMapping("/kill")
	public void killApplication() throws IOException, MessagingException, InterruptedException {
		restart = false;
		logger.info("Sending application kill message !!!");
		sender.sendApplicationStopMessage();
		System.exit(1);
		
		
	}

}
