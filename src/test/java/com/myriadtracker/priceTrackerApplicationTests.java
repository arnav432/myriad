package com.myriadtracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import emailSender.emailSenderHelper;
import orderTracker.trackOrders;
import orderTrackingDatabase.Product;
import orderTrackingDatabase.orderLinks;
import trackerEngine.PriceTrackingEngine;

@SpringBootTest
class priceTrackerApplicationTests {

	static Logger logger = LoggerFactory.getLogger(trackOrders.class);

	ObjectMapper mapper = new ObjectMapper();
	ArrayList<Map<String, Float>> decreasedPriceProductsMap = new ArrayList<>();
	HashMap<String, Float> toBeAddedProducts = new HashMap<String, Float>();
	HashMap<String, Float> lastRecordedFlipkartPrices = new HashMap<String, Float>();
	HashMap<String, Float> lastRecordedAmazonPrices = new HashMap<String, Float>();
	emailSenderHelper sender = new emailSenderHelper();
	static ArrayList<Product> productList = new ArrayList<>();
	Set<Product> productSet = new HashSet<>();
	PriceTrackingEngine engine = new PriceTrackingEngine();
	private static final long TIMEOUT = 20000;
	private static String productPriceDataBook = "productData.csv";
	float currentPrice = 0;
	float lastObservedPrice = 0;
	private static final long WAIT_DURATION_IN_MINUTES = 1; 

	private static String[] FLIPKART_LINKS = {

			"https://www.flipkart.com/philips-10000-mah-power-bank-fast-charging/p/itm384bfda57c458?pid=PWBFQYJXSZ6JD2ZN&otracker=wishlist&lid=LSTPWBFQYJXSZ6JD2ZNIC6FUQ&fm=organic&iid=64c1be16-18b6-4b32-ba7e-e6ed5e3176d5.PWBFQYJXSZ6JD2ZN.PRODUCTSUMMARY&ssid=uhm6046fyo0000001616741953984" };

	@Test
	void contextLoads() {
	}
	
	@Test
	void startEngine() throws IOException, MessagingException, InterruptedException {
		logger.info("Starting engine...........");
		trackOrders tracker = new trackOrders();
		productList = tracker.loadProductPriceData();
		logger.info("List size is "+productList.size());
		printProductList(productList);
		while(true) {
			//tracker.trackFlipkartOrders();
			tracker.trackFlipkartOrdersV2(productList);
			//tracker.trackAmazonOrders();
			logger.info("Going to sleep for "+WAIT_DURATION_IN_MINUTES+" minutes..............");
			//stayAlive();
		}
	}
	
	@Test
	private void readFileUsingCSV() throws IOException, URISyntaxException {
		
		Reader reader = Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource(productPriceDataBook).toURI()));
		
		List<String[]> productListString = new ArrayList<>();
		
		CSVParser parser = new CSVParserBuilder()
				 					.withSeparator(',')
				 					.withIgnoreQuotations(true)
				 					.build();
		
		CSVReader csvReader = new CSVReaderBuilder(reader)
									.withSkipLines(1)
									.withCSVParser(parser)
									.build();
		
		try {
			productListString = csvReader.readAll();
			reader.close();
			csvReader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		for(String[] product : productListString) {
			for(int i = 0;i<product.length;i++) {
				logger.info("Got -> "+product[i]);
			}
		}
		
	}
	
	@Test
	public void printDataUsingCSV() throws IOException, URISyntaxException {
		
		Reader reader = Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource(productPriceDataBook).toURI()));
		
		List<String[]> productListString = new ArrayList<>();
		
		CSVParser parser = new CSVParserBuilder()
				 					.withSeparator(',')
				 					.withIgnoreQuotations(true)
				 					.build();
		
		CSVReader csvReader = new CSVReaderBuilder(reader)
									.withSkipLines(1)
									.withCSVParser(parser)
									.build();
		
		try {
			productListString = csvReader.readAll();
			reader.close();
			csvReader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		for(String[] product : productListString) {
			for(int i = 0;i<product.length;i++) {
				logger.info("Got -> "+product[i]);
				
				
			}
		}
		
	}
	
	@Test
	public void printDataUsingCSVbean() throws IOException, URISyntaxException {
		
		Reader reader = Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource(productPriceDataBook).toURI()));
		
		List<String[]> productListString = new ArrayList<>();
		
		CSVParser parser = new CSVParserBuilder()
				 					.withSeparator(',')
				 					.withIgnoreQuotations(true)
				 					.build();
		
		CSVReader csvReader = new CSVReaderBuilder(reader)
									.withSkipLines(1)
									.withCSVParser(parser)
									.build();
		
		try {
			productListString = csvReader.readAll();
			reader.close();
			csvReader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		for(String[] product : productListString) {
			for(int i = 0;i<product.length;i++) {
				logger.info("Got -> "+product[i]);
				
				
			}
		}
		
	}
	
	@Test
	public void csvToPojo() throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		ColumnPositionMappingStrategy<Product> strategy = new ColumnPositionMappingStrategy<Product>();
		char delimiter = ',';
		
		strategy.setType(Product.class);
		
		String[] columnHeaders = new String[] {"LINK","Last Observed Price","Minimum Price","Maximum Price"};
		strategy.setColumnMapping(columnHeaders);
		
		CsvToBean<Product> csv = new CsvToBean<Product>();
		csv.setMappingStrategy(strategy);
		csv.setCsvReader(new CSVReader(new FileReader(productPriceDataBook)));
		//productList = csv.parse(strategy, new CSVReader(new FileReader(productPriceDataBook),delimiter));
		
		productList = (ArrayList<Product>) csv.parse();
		
		for(Product product : productList) {
			logger.info("Product is "+product);
		}
		
		writePojoToCsv();
		
	}
	
	@Test
	public void writePojoToCsv() throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		Writer writer = new FileWriter(productPriceDataBook);
        
		ColumnPositionMappingStrategy<Product> mappingStrategy=  new ColumnPositionMappingStrategy<Product>(); 
        mappingStrategy.setType(Product.class); 

        StatefulBeanToCsvBuilder<Product> builder = new StatefulBeanToCsvBuilder<Product>(writer);
        StatefulBeanToCsv<Product> beanWriter = builder.withMappingStrategy(mappingStrategy).build(); 
        //.withMappingStrategy(mappingStrategy)  .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
        //List<Product> reportBeanList = new ArrayList<>(productSet);
        //Collections.sort(reportBeanList);
        
        beanWriter.write(productList);
        writer.flush();
        logger.info("CSV File written successfully!!!");
		
		
		
	}
	
	@Test
	public void addNewProduct() throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		
		
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		Product product = new Product();
		product.setProductLink("testLink");
		writer.append(product.getProductLink());
		writer.append("\n");
		writer.close();
		
		
	}
	
	public ArrayList<Product> loadProductPriceDataV2() throws IOException {
		logger.info("Loading product price data from csv file...");
		BufferedReader reader = getReader();
		String line = "";
		reader.readLine(); // to exclude the header of the csv file
		while((line = reader.readLine()) != null) {
				int counter=0;
				Product product = new Product();
			    String[] fields = line.split(",");
			    try {
			    	if(fields.length > 0) {
					    if(fields[0]!=null) {
					    	counter++;
					    	logger.info(fields[0]);
					    	product.setProductLink(fields[0]);
					    }
					    if(fields.length==counter) {
					    	productList.add(product);
					    	logger.info("Exiting after adding "+counter+" field in product ");
				    		continue;
				    	}
					    if(fields[1]!=null) {
					    	counter++;
					    	logger.info(fields[0]);
					    	product.setLastObservedPrice(fields[1]); 
					    }
					    if(fields.length==counter) {
					    	productList.add(product);
					    	logger.info("Exiting after adding "+counter+" field in product ");
				    		continue;
				    	}
						if(fields[2]!=null) {
							counter++;
							logger.info(fields[0]);
							product.setMinPrice(fields[2]);
						}
						if(fields.length==counter) {
							productList.add(product);
							logger.info("Exiting after adding "+counter+" field in product ");
				    		continue;
				    	}
						if(fields[3]!=null) {
							logger.info(fields[0]);
							product.setMaxPrice(fields[3]);
							productList.add(product);
						}
			    	}
			    }
			    catch(ArrayIndexOutOfBoundsException a) {
			    	a.printStackTrace();
			    }
			    catch(Exception e) {
			    	e.printStackTrace();
			    }
			   
		}
		logger.info("List SIZE is "+productList.size());
		reader.close();
		return productList;
	}
	
	private FileWriter getAppendedWriter() throws IOException {
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		return writer;
	}
	
	private FileWriter getOverwrittenWriter() throws IOException {
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),false);
		return writer;
	}
	
	private BufferedReader getReader() throws IOException {
		File file = new File(productPriceDataBook);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		return reader;
	}
	
	public void writeToProductPriceData(String LINK, float currentPrice) throws IOException {
		FileWriter writer = getAppendedWriter();
		Product product = new Product();
		product.setProductLink(LINK);
		//product.setLastObservedPrice(currentPrice);
		writer.append(product.getProductLink());
		writer.append(",");
		writer.append(String.valueOf(product.getLastObservedPrice()));
		writer.append("\n");
		writer.close();
	}

	private void writeToFile(String LINK, float lastObservedPrice) throws FileNotFoundException {

		File file = new File("productData.csv");
		PrintWriter writer = new PrintWriter(file);
		writer.printf(LINK, lastObservedPrice);
		writer.close();
		
	}
	
	private Float beingTrackedFirstTimeV2(Product product, float currentPrice) throws IOException {
		FileWriter writer = getAppendedWriter();
		File file = new File(productPriceDataBook);
		product.setProductLink("https://www.flipkart.com/acer-23-8-inch-full-hd-ips-panel-monitor-ha240y/p/itmfbeg6j2gmkhfu?pid=MONFBEG6RFZ4VSA6&otracker=wishlist&lid=LSTMONFBEG6RFZ4VSA6WI2QFH&fm=organic&iid=01e86f97-e121-4ada-8c47-554700482ee6.MONFBEG6RFZ4VSA6.PRODUCTSUMMARY&ssid=yabthaizio0000001616336471504");
		//product.setLastObservedPrice(currentPrice);
		List<String> out = Files.lines(file.toPath())
                				.filter(line -> !line.contains(product.getProductLink()))
                				.collect(Collectors.toList());
		Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		writer.append(product.getProductLink());
		writer.append(",");
		writer.append(String.valueOf(product.getLastObservedPrice()));
		writer.append(",");
		writer.append(String.valueOf(product.getLastObservedPrice()));
		writer.append(",");
		writer.append(String.valueOf(product.getLastObservedPrice()));
		writer.append("\n");
		return currentPrice;
		
	}
	
	private static void printProductList(ArrayList<Product> productList) {
		while(true) {
			for(Product product:productList) {
				logger.info(product.getProductLink());
			}
			logger.info("--------------------------------------------------------------------------------");
		}
		
	}
	
	@Test
	public void writeDataToCSVTest() throws IOException {
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		//Product product = new Product();
		for(String order : orderLinks.getFLIPKART_LINKS()) {
			writer.append(order);
			writer.append("\n");
		}
		//product.setProductLink(LINK);
		//product.setLastObservedPrice(currentPrice);
		//writer.append(product.getProductLink());
		//writer.append(",");
		//writer.append(String.valueOf(product.getLastObservedPrice()));
		//writer.append("\n");
		writer.close();
	}
	
	@Test
	public void printTestData() throws IOException {
		
		loadProductPriceData();
		
		for(Product product : productList) {
			if(product!=null) {
				logger.info("Link -> "+product.getProductLink()+" price -> "+product.getLastObservedPrice());
				
			}
		}
		
	}
	
	public void loadProductPriceData() throws IOException {
		logger.info("Loading product price data from csv file...");
		File file = new File(productPriceDataBook);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = "";
		reader.readLine(); // to exclude the header of the csv file 
		
		while((line = reader.readLine()) != null) {
				int counter=0;
				Product product = new Product();
			    String[] fields = line.split(",");
			    try {
			    	if(fields.length > 0) {
					    if(fields[0]!=null) {
					    	counter++;
					    	logger.info(fields[0]);
					    	product.setProductLink(fields[0]);
					    	
					    }
					    if(fields.length==counter) {
				    		continue;
				    	}
					    if(fields[1]!=null) {
					    	counter++;
					    	logger.info(fields[0]);
					    	//product.setLastObservedPrice(Float.parseFloat(fields[1])); 
					    }
					    if(fields.length==counter) {
				    		continue;
				    	}
						if(fields[2]!=null) {
							counter++;
							logger.info(fields[0]);
							//product.setMinPrice(Float.parseFloat(fields[2]));
						}
						if(fields.length==counter) {
				    		continue;
				    	}
						if(fields[3]!=null) {
							logger.info(fields[0]);
							//product.setMaxPrice(Float.parseFloat(fields[3]));
						}
					    productList.add(product);
					    }
			    }
			    catch(ArrayIndexOutOfBoundsException a) {
			    	a.printStackTrace();
			    }
			    catch(Exception e) {
			    	e.printStackTrace();
			    }
			    
			   
		}
		reader.close();
	}
	
	@Test
	public void test() throws IOException {
		logger.info("hi");
		Product product = new Product();
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		product.setProductLink("https://www.flipkart.com/acer-23-8-inch-full-hd-ips-panel-monitor-ha240y/p/itmfbeg6j2gmkhfu?pid=MONFBEG6RFZ4VSA6&otracker=wishlist&lid=LSTMONFBEG6RFZ4VSA6WI2QFH&fm=organic&iid=01e86f97-e121-4ada-8c47-554700482ee6.MONFBEG6RFZ4VSA6.PRODUCTSUMMARY&ssid=yabthaizio0000001616336471504");
		List<String> out = Files.lines(file.toPath())
                				.filter(line -> !line.contains(product.getProductLink()))
                				.collect(Collectors.toList());
		Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		//return currentPrice;
	}
	
	@Test
	private Float beingTrackedFirstTime(Product product, float currentPrice) throws IOException {
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		product.setProductLink("https://www.flipkart.com/acer-23-8-inch-full-hd-ips-panel-monitor-ha240y/p/itmfbeg6j2gmkhfu?pid=MONFBEG6RFZ4VSA6&otracker=wishlist&lid=LSTMONFBEG6RFZ4VSA6WI2QFH&fm=organic&iid=01e86f97-e121-4ada-8c47-554700482ee6.MONFBEG6RFZ4VSA6.PRODUCTSUMMARY&ssid=yabthaizio0000001616336471504");
		List<String> out = Files.lines(file.toPath())
                				.filter(line -> !line.contains(product.getProductLink()))
                				.collect(Collectors.toList());
		Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		return currentPrice;
		
	}
	
	@Test
	public void writetoFileTest() throws IOException {
		File file = new File("productData.csv");
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		Product product = new Product();
		product.setProductLink(FLIPKART_LINKS[0]);
		//product.setLastObservedPrice(2000);
		writer.append(product.getProductLink());
		writer.append(",");
		writer.append(String.valueOf(product.getLastObservedPrice()));
		writer.append("\n");
		writer.close();
	}
	
	@Test
	public void readFromFileTest() throws IOException {
		
		File file = new File("productData.csv");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = "";
		Product product = new Product();
		reader.readLine(); // to exclude the header of the csv file 
		while((line = reader.readLine()) != null) {
		    String[] fields = line.split(",");
		    
		    if(fields.length > 0) {
		     String link = fields[0];
		     String price = fields[1];
		     logger.info("link -> "+link+", price -> "+price);
		     float lastPrice = Float.parseFloat(price);
		     logger.info("price is "+lastPrice);
		     lastRecordedFlipkartPrices.put(link, Float.parseFloat(price));
		     logger.info("price for ->"+link+" is "+lastRecordedFlipkartPrices.get(link));
		     //product.setProductLink(fields[0]);
		     //product.setLastObservedPrice(Float.parseFloat(fields[1]));
		    }
		    logger.info("Product is ->"+product.toString());
		   }
		reader.close();
		
	}
	
	@Test
	public void getResponse() throws IOException {
		Document doc = null;

		doc = Jsoup.connect("https://www.flipkart.com/acer-23-8-inch-full-hd-ips-panel-monitor-ha240y/p/itmfbeg6j2gmkhfu?pid=MONFBEG6RFZ4VSA6&otracker=wishlist&lid=LSTMONFBEG6RFZ4VSA6WI2QFH&fm=organic&iid=01e86f97-e121-4ada-8c47-554700482ee6.MONFBEG6RFZ4VSA6.PRODUCTSUMMARY&ssid=yabthaizio0000001616336471504%22").timeout((int) TIMEOUT).get();

		//String response = doc.normalise().select("script#is_script").html().toString();
		
		String response = doc.normalise().select("script#is_script").html().toString();
		logger.info("response is ->" + response);
	}
	
	@Test
	public void getOrderStatus() throws IOException {
		Document doc = null;

		doc = Jsoup.connect("https://www.flipkart.com/lenovo-23-8-inch-full-hd-ips-panel-monitor-q24i-10/p/itm27fa69e2e111b?pid=MONFNV4KZRFSQ5WB&lid=LSTMONFNV4KZRFSQ5WBGIBQBN&marketplace=FLIPKART&fm=productRecommendation%2Fsimilar&iid=R%3As%3Bp%3AMONFBEG6RFZ4VSA6%3Bl%3ALSTMONFBEG6RFZ4VSA6WI2QFH%3Bpt%3App%3Buid%3Ac538d249-9054-11eb-83e7-b74a639941fb%3B.MONFNV4KZRFSQ5WB&ppt=pp&ppn=pp&ssid=yabthaizio0000001616336471504%22%2C&otracker=pp_reco_Similar%2BProducts_3_34.productCard.PMU_HORIZONTAL_lenovo%2B23.8%2Binch%2BFull%2BHD%2BIPS%2BPanel%2BMonitor%2B%2528Q24i-10%2529_MONFNV4KZRFSQ5WB_productRecommendation%2Fsimilar_2&otracker1=pp_reco_PINNED_productRecommendation%2Fsimilar_Similar%2BProducts_GRID_productCard_cc_3_NA_view-all&cid=MONFNV4KZRFSQ5WB").timeout((int) TIMEOUT).get();

		//String response = doc.normalise().select("script#is_script").html().toString();
		
		String response = doc.normalise().select("script#is_script").html().toString();
		logger.info("response is ->" + response);
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	@Test
	void trackFlipkartOrder() throws IOException {
		for (String LINK : FLIPKART_LINKS) {

			Document doc = null;

			doc = Jsoup.connect(LINK).timeout((int) TIMEOUT).get();

			String response = doc.normalise().select("script#is_script").html().toString();
			logger.info("response is ->" + response);
			char[] responseArray = response.toCharArray();
			long start = response.indexOf("finalPrice");
			boolean record = false;
			String result = "";
			for (; responseArray[(int) start] != '}'; start++) {
				if (responseArray[(int) start] == '{') {
					record = true;
				}
				if (record) {
					result += responseArray[(int) start];
				}
			}
			result += '}';
			Map<String, String> resultMap = mapper.readValue(result, Map.class);
			String price = resultMap.get("decimalValue");
			float currentPrice = Float.parseFloat(price);
			try {
				if (lastRecordedFlipkartPrices.containsKey(LINK)) {
					lastObservedPrice = lastRecordedFlipkartPrices.get(LINK);
				} else {
					lastObservedPrice = 0;
					writeToFile(LINK, currentPrice);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("Current price is ->" + currentPrice);
			if (currentPrice != lastObservedPrice) {
				if (lastObservedPrice != 0) {
					logger.info("!!!!!!!!!!!!!PRODUCT PRICE DECREASED!!!!!!!!!!!!!");
				}
				try {
					toBeAddedProducts.put(LINK, currentPrice);
					decreasedPriceProductsMap.add(toBeAddedProducts);
					logger.info("- Price of " + LINK + " changed from " + lastObservedPrice + " to "
							+ toBeAddedProducts.get(LINK));
					if (lastObservedPrice != 0) {
						//sender.sendPriceDropMail(LINK, toBeAddedProducts, lastObservedPrice);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				float temporaryPrice = (lastObservedPrice == 0.0) ? currentPrice : lastObservedPrice;
				logger.info("- Price didnt change from " + temporaryPrice + " for " + LINK);
			}
			try {
				lastRecordedFlipkartPrices.put(LINK, currentPrice);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
