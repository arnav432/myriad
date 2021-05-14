package orderTracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myriadtracker.RestartEngine;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import emailSender.emailSenderHelper;
import orderTrackingDatabase.Product;
import orderTrackingDatabase.orderLinks;
import trackerEngine.PriceTrackingEngine;


public class trackOrders {
	
	
	
	static Logger logger = LoggerFactory.getLogger(trackOrders.class);
	
	ObjectMapper mapper = new ObjectMapper();
	ArrayList<Map<String, Float>> decreasedPriceProductsMap = new ArrayList<>();
	HashMap<String, Float> toBeAddedProducts = new HashMap<String,Float>();
	HashMap<String,Float> lastRecordedFlipkartPrices = new HashMap<String,Float>();
	HashMap<String,Float> lastRecordedAmazonPrices = new HashMap<String,Float>();
	ArrayList<Product> productList = new ArrayList<>();
	emailSenderHelper sender = new emailSenderHelper();
	orderLinks links = new orderLinks();
	PriceTrackingEngine engine = new PriceTrackingEngine();
	RestartEngine backupengine = new RestartEngine();
	
	private static final long TIMEOUT = 20000;
	private static int counter;
	private static float currentPrice = 0;
	private static float lastObservedPrice = 0;
	private static boolean restart=false;
	private static String productPriceDataBook = "productData.csv";
	private static boolean IS_FIRST_TIME = false;
	
	public ArrayList<Product> loadProductPriceData() throws IOException {
		
		ColumnPositionMappingStrategy<Product> strategy = new ColumnPositionMappingStrategy<Product>();
		
		strategy.setType(Product.class);
		
		String[] columnHeaders = new String[] {"LINK","Last Observed Price","Minimum Price","Maximum Price"};
		strategy.setColumnMapping(columnHeaders);
		
		CsvToBean<Product> csv = new CsvToBean<Product>();
		csv.setMappingStrategy(strategy);
		csv.setCsvReader(new CSVReader(new FileReader(productPriceDataBook)));
		
		productList = (ArrayList<Product>) csv.parse();
		
		//for(Product product : productList) { logger.info("Product is "+product); }
		
		return productList;
		
	}
	
	public int getFileSize() throws FileNotFoundException {
		ColumnPositionMappingStrategy<Product> strategy = new ColumnPositionMappingStrategy<Product>();
		
		strategy.setType(Product.class);
		
		String[] columnHeaders = new String[] {"LINK","Last Observed Price","Minimum Price","Maximum Price"};
		strategy.setColumnMapping(columnHeaders);
		
		CsvToBean<Product> csv = new CsvToBean<Product>();
		csv.setMappingStrategy(strategy);
		csv.setCsvReader(new CSVReader(new FileReader(productPriceDataBook)));
		
		productList = (ArrayList<Product>) csv.parse();
		
		return productList.size();
	}
	
	public void writePojoToCsv(ArrayList<Product> productList) throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		Writer writer = new FileWriter(productPriceDataBook);
        
		ColumnPositionMappingStrategy<Product> mappingStrategy=  new ColumnPositionMappingStrategy<Product>(); 
        mappingStrategy.setType(Product.class); 

        StatefulBeanToCsvBuilder<Product> builder = new StatefulBeanToCsvBuilder<Product>(writer);
        StatefulBeanToCsv<Product> beanWriter = builder.withMappingStrategy(mappingStrategy).build(); 
        
        beanWriter.write(productList);
        writer.flush();
        logger.info("CSV File written successfully!!!");
		
	}
	
	public void addNewProduct(String productLink) throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		Product product = new Product();
		product.setProductLink(productLink);
		writer.append(product.getProductLink());
		writer.append("\n");
		writer.close();
		
	}
	
	public void deleteProduct(String productLink) throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		Product product = new Product();
		product.setProductLink(productLink);
		writer.append(product.getProductLink());
		writer.append("\n");
		writer.close();
		
	}
	
	private Document getNewConnection(String LINK) throws IOException, MessagingException, InterruptedException {
		Document doc = null;
		try {
			//doc = Jsoup.connect(LINK).userAgent("Mozilla/17.0").get();
			doc = Jsoup.connect(LINK).timeout((int) TIMEOUT).get();
			}
			catch(SocketException e) {
				e.printStackTrace();
			}
			finally {
				if(restart) {
				logger.info("error while fetching from link, restarting...");
				backupengine.restart();
			}
			}
		return doc;
	}
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public void trackFlipkartOrdersV2(ArrayList<Product> productList) throws IOException, MessagingException, InterruptedException {
		logger.info(productList.size()+" items ready to be tracked...");
		counter = 0;
		for(Product product : productList) {
			IS_FIRST_TIME = false;
			if(product.getLastObservedPrice() == null) {
				logger.info("Product being tracked first time");
				IS_FIRST_TIME = true;
			}
			
			restart=false;
			counter++;
			Document doc = null;
			
			doc = getNewConnection(product.getProductLink());
			
			String response = doc.normalise().select("script#is_script").html().toString();
			char[] responseArray = response.toCharArray();
			long start = response.indexOf("finalPrice");
			boolean record = false;
			String result = "";
			if( (start < 0) || (response == null) ) {
				logger.info("Product currently unavailable, hence skipping it...");
				continue;
			}
			try {
				for(;responseArray[(int) start]!='}';start++) {
		 			if(responseArray[(int) start]=='{') {
		 				record = true;
		 			}
		 			if(record) {
		 				result+=responseArray[(int) start];
		 			}
		 		}
				restart = true;
			}
			catch(ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				restart=false;
			}
			finally {
				if(!restart) {
					logger.info("Encountered array out of bound exception, trying to restart...");
					backupengine.restart();
				}
			}
	 		result+='}';
	 		Map<String,String> resultMap = mapper.readValue(result, Map.class);
	 		String price = resultMap.get("decimalValue");
	 		currentPrice = Float.parseFloat(price);
	 		//logger.info("Current price is "+currentPrice);
	 		
	 		
	 		try {
	 			lastObservedPrice = !product.getLastObservedPrice().isEmpty() ? Float.parseFloat(product.getLastObservedPrice()) : currentPrice;
	 		}
	 		catch(Exception e) {
	 			IS_FIRST_TIME=true;
	 		}
	 		
	 		if(currentPrice!=lastObservedPrice) {
	 			if(!IS_FIRST_TIME) {
	 			logger.info("!!!!!!!!!!!!!PRODUCT PRICE CHANGED!!!!!!!!!!!!!");
	 			}
	 			try {
		 			logger.info(counter+"- Price of "+product.getProductLink()+" changed from "+lastObservedPrice+" to "+currentPrice);
		 			if(!IS_FIRST_TIME) {
		 				//sender.sendPriceDropMail(product.getProductLink(),currentPrice,lastObservedPrice);
		 			}
		 			restart = true;
		 		}
		 		catch(Exception e) {
		 			e.printStackTrace();
		 			restart=false;
		 		}
	 			finally {
	 				if(!restart) {
						logger.info("Encountered exception while sending price drop mail, trying to restart...");
						backupengine.restart();
					}
	 			}
	 			
	 		}
	 		else {
	 			float temporaryPrice = (lastObservedPrice==0.0) ? currentPrice : lastObservedPrice;
	 			logger.info(counter+"- Price didnt change from "+temporaryPrice+" for "+product.getProductLink());
	 		}
	 		
			/*
			 * if(IS_FIRST_TIME) { lastObservedPrice=currentPrice; }
			 */
	 		
	 		if(IS_FIRST_TIME) {
	 			product.setLastObservedPrice(String.valueOf(currentPrice));
	 			product.setMinPrice(String.valueOf(currentPrice));
	 			product.setMaxPrice(String.valueOf(currentPrice));
	 			//logger.info("Product changed to "+product.toString());
	 		}
	 		else {
	 			product.setLastObservedPrice(String.valueOf(currentPrice));
	 			if(currentPrice<Float.parseFloat(product.getMinPrice())) {
		 			product.setMinPrice(String.valueOf(currentPrice));
		 		}
		 		
		 		if(currentPrice>Float.parseFloat(product.getMaxPrice())) {
		 			product.setMaxPrice(String.valueOf(currentPrice));
		 		}
	 		}
	 		
	 		
	 		
	 		
		}
		
		//for(Product product : productList) { logger.info(product.toString()); }
		
		try {
			writePojoToCsv(productList);
			restart = true;
		} catch (CsvDataTypeMismatchException e) {
			e.printStackTrace();
			restart=false;
		} catch (CsvRequiredFieldEmptyException e) {
			e.printStackTrace();
			restart=false;
		} catch (IOException e) {
			e.printStackTrace();
			restart=false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			restart=false;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(!restart) {
				logger.info("Encountered exception while writing to CSV file, trying to restart...");
				backupengine.restart();
			}
		}
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	public void trackFlipkartOrders() throws IOException, MessagingException, InterruptedException {
		
		counter = 0;
		//for(String LINK : links.getFLIPKART_LINKS()) {
		for(String LINK : links.getPARTICULAR_ORDER()) {
			restart=false;
			counter++;
			Document doc = null;
			
			doc = getNewConnection(LINK);
			
			String response = doc.normalise().select("script#is_script").html().toString();
			
			
			char[] responseArray = response.toCharArray();
			long start = response.indexOf("finalPrice");
			boolean record = false;
			String result = "";
			
			try {
				for(;responseArray[(int) start]!='}';start++) {
		 			if(responseArray[(int) start]=='{') {
		 				record = true;
		 			}
		 			if(record) {
		 				result+=responseArray[(int) start];
		 			}
		 		}
				restart = true;
			}
			catch(ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			finally {
				if(!restart) {
					logger.info("Encountered array out of bound exception, trying to restart...");
					backupengine.restart();
				}
			}
			
	 		
	 		result+='}';
	 		Map<String,String> resultMap = mapper.readValue(result, Map.class);
	 		String price = resultMap.get("decimalValue");
	 		currentPrice = Float.parseFloat(price);
	 		if(!response.contains("Out of Stock")) {
				logger.info("Product is in stock");
				emailSenderHelper.sendStockStatusMessage(LINK,currentPrice);
			}
	 		try {
	 			if(lastRecordedFlipkartPrices.containsKey(LINK)) {
		 			lastObservedPrice = lastRecordedFlipkartPrices.get(LINK);
		 		}
		 		else {
		 			lastObservedPrice = 0;
		 			//writeToFile(LINK,lastObservedPrice);
		 		}
	 		}
	 		catch(Exception e) {
	 			e.printStackTrace();
	 		}
	 		
	 		if(currentPrice!=lastObservedPrice) {
	 			if(lastObservedPrice!=0) {
	 			logger.info("!!!!!!!!!!!!!PRODUCT PRICE DECREASED!!!!!!!!!!!!!");
	 			}
	 			try {
	 				toBeAddedProducts.put(LINK, currentPrice);
		 			decreasedPriceProductsMap.add(toBeAddedProducts);
		 			logger.info(counter+"- Price of "+LINK+" changed from "+lastObservedPrice+" to "+toBeAddedProducts.get(LINK));
		 			if(lastObservedPrice!=0) {
		 				//sender.sendPriceDropMail(product.getProductLink(),currentPrice,lastObservedPrice);
		 			}
		 		}
		 		catch(Exception e) {
		 			e.printStackTrace();
		 		}
	 			finally {
	 				if(!restart) {
						logger.info("Encountered exception while sending price drop mail, trying to restart...");
						backupengine.restart();
					}
	 			}
	 			
	 		}
	 		else {
	 			float temporaryPrice = (lastObservedPrice==0.0) ? currentPrice : lastObservedPrice;
	 			logger.info(counter+"- Price didnt change from "+temporaryPrice+" for "+LINK);
	 		}
	 		try {
	 			lastRecordedFlipkartPrices.put(LINK, currentPrice);
	 		}
	 		catch(Exception e) {
	 			e.printStackTrace();
	 		}
	 		finally {
 				if(!restart) {
					logger.info("Encountered exception, trying to restart...");
					backupengine.restart();
				}
 			}
	 		
		}
		toBeAddedProducts.clear();
		decreasedPriceProductsMap.clear();
		
	}
	
	@SuppressWarnings({ "static-access"})
	public void trackAmazonOrders() throws IOException, MessagingException, InterruptedException {
			counter=0;
			for(String LINK : links.getAMAZON_LINKS()) {
			counter++;
			Document doc = null;;
			
			doc = getNewConnection(LINK);
		    
			String response = doc.normalise().select("span#tp_price_block_total_price_in").html().toString();
			char[] responseArray = response.toCharArray();
			int start = response.indexOf(">");
			String result = "";
			for(int i=start+1;responseArray[i]!='<';i++) {
				if(responseArray[i]!=',') {
					result+=responseArray[i];
				}
			}
			float currentPrice = Float.parseFloat(result);
			
			try {
	 			if(lastRecordedAmazonPrices.containsKey(LINK)) {
		 			lastObservedPrice = lastRecordedAmazonPrices.get(LINK);
		 		}
		 		else {
		 			lastObservedPrice = 0;
		 		}
	 		}
	 		catch(Exception e) {
	 			e.printStackTrace();
	 		}
	 		
	 		if(currentPrice!=lastObservedPrice) {
	 			if(lastObservedPrice!=0) {
	 				logger.info("!!!!!!!!!!!!!PRODUCT PRICE DECREASED!!!!!!!!!!!!!");
	 			}
	 			try {
	 				toBeAddedProducts.put(LINK, currentPrice);
		 			decreasedPriceProductsMap.add(toBeAddedProducts);
		 			logger.info(counter+"- Price of "+LINK+" changed from "+lastObservedPrice+" to "+toBeAddedProducts.get(LINK));
		 			if(lastObservedPrice!=0) {
		 				//sender.sendPriceDropMail(product.getProductLink(),currentPrice,lastObservedPrice);
		 			}
		 		}
		 		catch(Exception e) {
		 			e.printStackTrace();
		 		}
	 			
	 		}
	 		else {
	 			float temporaryPrice = (lastObservedPrice==0.0) ? currentPrice : lastObservedPrice;
	 			logger.info(counter+"- Price didnt change from "+temporaryPrice+" for "+LINK);
	 		}
	 		try {
	 			lastRecordedAmazonPrices.put(LINK, currentPrice);
	 		}
	 		catch(Exception e) {
	 			e.printStackTrace();
	 		}
	 		
		}
		toBeAddedProducts.clear();
		decreasedPriceProductsMap.clear();
	 		
		}

	
	
}
