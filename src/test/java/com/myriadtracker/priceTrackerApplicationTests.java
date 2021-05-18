package com.myriadtracker;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketException;
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
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
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
	trackOrders tracker = new trackOrders();
	static ArrayList<Product> productList = new ArrayList<>();
	Set<Product> productSet = new HashSet<>();
	PriceTrackingEngine engine = new PriceTrackingEngine();
	private static final long TIMEOUT = 20000;
	private static String productPriceDataBook = "productData.csv";
	float currentPrice = 0;
	private static int counter;
	float lastObservedPrice = 0;
	RestartEngine backupengine = new RestartEngine();
	private static final long WAIT_DURATION_IN_MINUTES = 1; 
	private static boolean IS_FIRST_TIME = false;
	private static boolean restart=false;

	private static String[] FLIPKART_LINKS = {

			"https://www.flipkart.com/philips-10000-mah-power-bank-fast-charging/p/itm384bfda57c458?pid=PWBFQYJXSZ6JD2ZN&otracker=wishlist&lid=LSTPWBFQYJXSZ6JD2ZNIC6FUQ&fm=organic&iid=64c1be16-18b6-4b32-ba7e-e6ed5e3176d5.PWBFQYJXSZ6JD2ZN.PRODUCTSUMMARY&ssid=uhm6046fyo0000001616741953984" };

	@Test
	void contextLoads() {
	}
	
	@Test
	void checkSlotByPincode() throws IOException, MessagingException, InterruptedException {
		String[] valuesNotNeeded = {"COVAXIN","COVISHIELD","Booked","NA","Age 45+","Age 18+"};
		// Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
		System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
		
		//System.setProperty("webdriver.chrome.driver", "src/main/resources/geckodriver.exe");
		
        WebDriver driver = new FirefoxDriver();
        
		//WebDriver driver = new ChromeDriver();
		
		//String response = doc.normalise().select("script#is_script").html().toString();
        // And now use this to visit Google
        //driver.get("http://www.google.com");
        //driver.get("https://selfregistration.cowin.gov.in");
        
        
        driver.get("https://www.cowin.gov.in/home");
        
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        // Find the text input element by its name
        //WebElement element = driver.findElement(By.name("q"));
        //WebElement element = driver.findElement(By.className("mat-input-element mat-form-field-autofill-control pintextbox ng-pristine ng-invalid cdk-text-field-autofill-monitored ng-touched"));
        
        WebElement pincodeBar = driver.findElement(By.id("mat-input-0"));
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();",pincodeBar);
        

        // Enter something to search for
        pincodeBar.sendKeys("134109");
        
        driver.findElement(By.className("pin-search-btn")).click();
        
        WebElement ageFilter = driver.findElement(By.id("flexRadioDefault2"));
        
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        js1.executeScript("arguments[0].click();",ageFilter);
        
        //driver.findElement(By.id("flexRadioDefault2")).click();
        
        //WebElement ageFilter = driver.findElement(By.id("flexRadioDefault2")).click();

        // Now submit the form. WebDriver will find the form for us from the element
        //element.submit();

        // Check the title of the page
        //System.out.println("Page title is: " + driver.getTitle());
        
        List<WebElement> listOfCenters = driver.findElements(By.className("slot-available-wrap"));
        logger.info("size is "+listOfCenters.size());
        for(WebElement center : listOfCenters) {
        	logger.info("Center Info -> "+center.getText());
        	String textToCheck = center.getText().toString();
        	//logger.info("text to check->"+textToCheck);
        	String[] valuesToBeCheckedTemp = textToCheck.split("\n");
        	System.out.println("Size is "+valuesToBeCheckedTemp.length);
        	int totalSize = valuesToBeCheckedTemp.length;
        	int totalOccurences = 0;
        	totalOccurences += (countOccurencesOf(textToCheck, "Booked"))*3;
        	totalOccurences += countOccurencesOf(textToCheck, "NA");
        	if((totalSize-totalOccurences)!=0) {
        		System.out.println("!!!!!!!!!!!!  VACCINE AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        	}
        	else {
        		System.out.println("!!!!!!!!!!!!  VACCINE NOT AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        	}
        	
        	//logger.info("split text-> "+center.getText().split(" ").length);
        	
        }
        
        //pincodeBar.clear();
        //pincodeBar.sendKeys("134112");
        //driver.findElement(By.className("pin-search-btn")).click();
        //List<WebElement> listOfCenters2 = driver.findElements(By.className("slot-available-wrap"));
        //logger.info("size is "+listOfCenters2.size());
        //logger.info("size->"+driver.findElements(By.className("slot-available-wrap")).size());
        
        
        //D:\geckodriver.exe
        
        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
            	//logger.info("->->"+d.findElement(By.className(className)));
            	//d.findElements(By.className("center-name-title"));
            	//d.findElements(By.className("slot-available-wrap"));
            	String textToCheck;
            	List<WebElement> listOfText = d.findElements(By.className("slot-available-wrap"));
            	for(WebElement text: listOfText) {
            		
            	}
            	//logger.info("text->->"+d.findElements(By.className("slot-available-wrap")).get(0).getText());
            	//logger.info("->->"+d.findElements(By.className("center-name-title")));
            	//logger.info("->->"+d.findElements(By.className("slot-available-wrap")));
            	return d.getTitle().toLowerCase().startsWith("cheese!");
            }
        });

        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());

        //Close the browser
        driver.quit();
	}
	
	@Test
	void checkSlotByDistrict() throws IOException, MessagingException, InterruptedException, AWTException {
		/*
		System.setProperty("webdriver.chrome.driver", "C:\\selenium\\chromedriver.exe");
		chromeOptions options = new ChromeOptions();
		options.setExperimentalOption("debuggerAddress", "127.0.0.1:<customPort>");
		WebDriver driver = new ChromeDriver(options);
		*/
		
		
		// Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
		System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
		System.setProperty("java.awt.headless", "false");
        WebDriver driver = new FirefoxDriver();
        driver.manage().window().maximize();
        //Point point = new Point(300, 500);
        //driver.manage().window().setPosition(point);
        //String response = doc.normalise().select("script#is_script").html().toString();
        // And now use this to visit Google
        //driver.get("http://www.google.com");
        //driver.get("https://selfregistration.cowin.gov.in");
        
        
        driver.get("https://www.cowin.gov.in/home");
        
        
        // Alternatively the same thing can be done like this
        // driver.navigate().to("http://www.google.com");

        // Find the text input element by its name
        //WebElement element = driver.findElement(By.name("q"));
        //WebElement element = driver.findElement(By.className("mat-input-element mat-form-field-autofill-control pintextbox ng-pristine ng-invalid cdk-text-field-autofill-monitored ng-touched"));
        
        WebElement element = driver.findElement(By.id("mat-input-0"));
        
        // to select the search-for-district toggle option
        WebElement pinOrDistrictSelector = driver.findElement(By.id("status"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();",pinOrDistrictSelector);
        
        // filling in state value
        WebElement state = driver.findElement(By.id("mat-select-0"));
        state.sendKeys("Haryana");
        
        
        // filling in district value
        WebElement district = driver.findElement(By.id("mat-select-2"));
        district.click();
        district.sendKeys("Panchkula");
        
        /*
        WebElement searchButton = driver.findElement(By.className("pin-search-btn"));
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        js1.executeScript("arguments[0].click();",searchButton);
        */
        driver.findElement(By.className("pin-search-btn")).click();
        driver.findElement(By.className("pin-search-btn")).click();
        
        WebElement ageFilter = driver.findElement(By.id("flexRadioDefault2"));
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        js1.executeScript("arguments[0].click();",ageFilter);
        
        Robot robot = new Robot();

        // Scroll Down using Robot class
        robot.mouseWheel(12);
        //robot.keyPress(KeyEvent.VK_PAGE_DOWN);
        //robot.keyPress(KeyEvent.VK_PAGE_DOWN);
        //robot.keyPress(KeyEvent.VK_PAGE_DOWN);
        //robot.keyPress(KeyEvent.VK_PAGE_DOWN);
        //robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
        
        //WebElement searchButton = driver.findElement(By.cssSelector("button.pin-search-btn.district-search"));
        /*
        WebElement searchButton = driver.findElement(By.xpath("//button[contains(text(),'Search')]"));
        searchButton.sendKeys(Keys.ENTER);
        logger.info("->->"+searchButton.getLocation());
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        js1.executeScript("arguments[0].click();",searchButton);
        searchButton.click();
        */

        //WebElement searchButton = driver.findElement(By.className("pin-search-btn.district-search"));
        //driver.findElement(By.xpath("//button[contains(text(),'district')]")).click();
        
        /*
        WebElement ageFilter = driver.findElement(By.id("flexRadioDefault2"));
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        js1.executeScript("arguments[0].click();",ageFilter);
        */
        //Select dropdown = new Select(driver.findElement(By.id("mat-select-0")));
        //Select dropdown = new Select(driver.findElement(By.xpath("//mat-select[@id=\"mat-select-0\"]")));
        
        //Select dropdown = new Select(driver.findElement( By.xpath(xpathExpression) ));
        
        //dropdown.selectByValue("Haryana");
        
        
        
        //district.sendKeys("Search By District");
        
        
        
        //pinOrDistrictSelector.click();
        //district.click();

        // Enter something to search for
        //element.sendKeys("134109");
        
        //driver.findElement(By.className("pin-search-btn")).click();

        // Now submit the form. WebDriver will find the form for us from the element
        //element.submit();

        // Check the title of the page
        //System.out.println("Page title is: " + driver.getTitle());
        
        List<WebElement> listOfCenters = driver.findElements(By.className("slot-available-wrap"));
        for(WebElement center : listOfCenters) {
        	logger.info("Center Info -> "+center.getText());
        	String textToCheck = center.getText().toString();
        	//logger.info("text to check->"+textToCheck);
        	String[] valuesToBeCheckedTemp = textToCheck.split("\n");
        	System.out.println("Size is "+valuesToBeCheckedTemp.length);
        	int totalSize = valuesToBeCheckedTemp.length;
        	int totalOccurences = 0;
        	totalOccurences += (countOccurencesOf(textToCheck, "Booked"))*3;
        	totalOccurences += countOccurencesOf(textToCheck, "NA");
        	if((totalSize-totalOccurences)!=0) {
        		System.out.println("!!!!!!!!!!!!  VACCINE AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        	}
        	else {
        		System.out.println("!!!!!!!!!!!!  VACCINE NOT AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        	}
        	
        	//logger.info("split text-> "+center.getText().split(" ").length);
        	
        }
        //logger.info("size->"+driver.findElements(By.className("slot-available-wrap")).size());
        //D:\geckodriver.exe
        
        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
            	//logger.info("->->"+d.findElement(By.className(className)));
            	//d.findElements(By.className("center-name-title"));
            	//d.findElements(By.className("slot-available-wrap"));
            	String textToCheck;
            	List<WebElement> listOfText = d.findElements(By.className("slot-available-wrap"));
            	for(WebElement text: listOfText) {
            		
            	}
            	//logger.info("text->->"+d.findElements(By.className("slot-available-wrap")).get(0).getText());
            	//logger.info("->->"+d.findElements(By.className("center-name-title")));
            	//logger.info("->->"+d.findElements(By.className("slot-available-wrap")));
            	return d.getTitle().toLowerCase().startsWith("cheese!");
            }
        });

        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());

        //Close the browser
        driver.quit();
	}
	
	public int countOccurencesOf(String mainString, String word) {
		// split the string by spaces in a
	    //String a[] = mainString.split(" ");
		System.out.println("Checking -> "+mainString);
		String a[] = mainString.split("\n");
		
	    // search for pattern in a
	    int count = 0;
	    for (int i = 0; i < a.length; i++)
	    {
	    // if match found increase count
	    if (word.equals(a[i]))
	        count++;
	    }
	    
	    System.out.println(word + " occurred " + count + " times");
	    
	    return count;
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
	
	@Test
	public void testStart() throws IOException, MessagingException, InterruptedException {
		logger.info("Starting engine...........");
		trackOrders tracker = new trackOrders();
		productList = tracker.loadProductPriceData();
		logger.info("List size is "+productList.size());
		trackFlipkartOrdersV2(productList);
		
	}
	
	
	@Test
	@SuppressWarnings({ "static-access", "unchecked" })
	public void trackFlipkartOrdersV2(ArrayList<Product> productList) throws IOException, MessagingException, InterruptedException {
		logger.info(productList.size()+" items ready to be tracked...");
		counter = 0;
		for(Product product : productList) {
			IS_FIRST_TIME = false;
			if(product.getLastObservedPrice().isEmpty()) {
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
			if( (start>0) && (response != null) ) {
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
						//backupengine.restart();
					}
				}
			}
			else {
				logger.info("Product is currently unavailable, hence skipping it...");
				continue;
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
		 		}
		 		catch(Exception e) {
		 			e.printStackTrace();
		 			restart=false;
		 		}
	 			finally {
	 				if(!restart) {
						logger.info("Encountered exception while sending price drop mail, trying to restart...");
						//backupengine.restart();
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
		finally {
			if(!restart) {
				logger.info("Encountered exception while writing to CSV file, trying to restart...");
				//backupengine.restart();
			}
		}
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
		
		//String[] columnHeaders = new String[] {"LINK","Last Observed Price","Minimum Price","Maximum Price"};
		//strategy.setColumnMapping(columnHeaders);
		
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
        
        beanWriter.write(productList);
        writer.flush();
        logger.info("CSV File written successfully!!!");
		
		
		
	}
	
	@Test
	public void addNewProduct() throws IOException, URISyntaxException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		ArrayList<Product> productList = new ArrayList<>();
		Product p = new Product();
		p.setProductLink("https://www.flipkart.com/ambrane-10000-mah-power-bank-18-w-delivery-2-0-quick-charge-3-0/p/itm695a237d6db0d?pid=PWBFZ9AWGHMNRGFQ&otracker=wishlist&lid=LSTPWBFZ9AWGHMNRGFQNNEIM9&fm=organic&iid=1e9a4e8e-9f3f-4678-939a-d0c4d562c734.PWBFZ9AWGHMNRGFQ.PRODUCTSUMMARY&ppt=pp&ppn=pp&ssid=uhm6046fyo0000001616741953984");
		productList.add(p);
		writePojoToCsv();
		logger.info("File has ");
		csvToPojo();
		
		
		
		/*
		File file = new File(productPriceDataBook);
		FileWriter writer = new FileWriter(file.getAbsoluteFile(),true);
		Product product = new Product();
		product.setProductLink("testLink");
		writer.append(product.getProductLink());
		writer.append("\n");
		writer.close();
		*/
		
		
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

		//doc = Jsoup.connect("https://www.flipkart.com/acer-23-8-inch-full-hd-ips-panel-monitor-ha240y/p/itmfbeg6j2gmkhfu?pid=MONFBEG6RFZ4VSA6&otracker=wishlist&lid=LSTMONFBEG6RFZ4VSA6WI2QFH&fm=organic&iid=01e86f97-e121-4ada-8c47-554700482ee6.MONFBEG6RFZ4VSA6.PRODUCTSUMMARY&ssid=yabthaizio0000001616336471504%22").timeout((int) TIMEOUT).get();
		doc = Jsoup.connect("https://selfregistration.cowin.gov.in/appointment").userAgent("Chrome/23.0.1271.95").timeout((int) TIMEOUT).get();
		logger.info("->->"+doc.toString());
		//String response = doc.normalise().select("script#is_script").html().toString();
		
		String response = doc.normalise().select("script#is_script").html().toString();
		logger.info("response is ->" + response);
	}
	
	private static Properties getProperties()  {
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		return properties;
	}
	
	
	static Session session = Session.getInstance(getProperties(), new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(emailSenderHelper.getEmail(), emailSenderHelper.getPassword());
		}
	});
	
	@Test
	public void sendMailTest() {
		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(emailSenderHelper.getEmail()));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress("arnavmalhotra338@gmail.com"));
			message.setSubject("Order Tracker - order tracking stopped");
			message.setText("!!!!!!!!!!!!!Order tracking has been stopped !!!");
			Transport.send(message);
			logger.info("!!!!!!!!!!!!!!!!!!!!!Message sent successfully!!!!!!!!!!!!!!!!!!!!!");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
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
