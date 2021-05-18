package vaccineAvailibility;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myriadtracker.RestartEngine;

import emailSender.emailSenderHelper;
import orderTracker.trackOrders;

public class CheckVaccineSlot {
	
	static Logger logger = LoggerFactory.getLogger(CheckVaccineSlot.class);
	
	private static emailSenderHelper emailHelper = new emailSenderHelper(); 
	
	private static RestartEngine restarter = new RestartEngine();
	
	private static int[] numberOfpreviouslyBookedCentres = null;
	
	public void checkSlot() throws IOException, MessagingException, InterruptedException {
		int retryCount = 0;
		
		try {
		System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
		
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--disable-popup-blocking");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS,options);
		
		WebDriver driver = null;
		try {
			driver = new FirefoxDriver(capabilities);
		}
		catch(Exception e) {
			e.printStackTrace();
			restarter.restart();
			
		}

        driver.get("https://www.cowin.gov.in/home");

        WebElement element = driver.findElement(By.id("mat-input-0"));

        element.sendKeys("134109");
        
        driver.findElement(By.className("pin-search-btn")).click();

        element.submit();

        List<WebElement> listOfCenters = driver.findElements(By.className("slot-available-wrap"));
        for(WebElement center : listOfCenters) {
        	//logger.info("Center Info -> "+center.getText());
        	String textToCheck = center.getText().toString();
        	//logger.info("text to check->"+textToCheck);
        	String[] valuesToBeCheckedTemp = textToCheck.split("\n");
        	//System.out.println("Size is "+valuesToBeCheckedTemp.length);
        	int totalSize = valuesToBeCheckedTemp.length;
        	int totalOccurences = 0;
        	totalOccurences += (countOccurencesOf(textToCheck, "Booked"))*3;
        	totalOccurences += countOccurencesOf(textToCheck, "NA");
        	if((totalSize-totalOccurences)!=0) {
        		logger.info("!!!!!!!!!!!!  VACCINE AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        		emailHelper.sendVaccineAvailibilityMessage();
        	}
        	else {
        		logger.info("!!!!!!!!!!!!  VACCINE NOT AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        	}

        }
        driver.quit();
		}
		catch(Exception e) {
			e.printStackTrace();
			restarter.restart();
		}
	}
	

	public void checkSlotByDistrict() throws IOException, MessagingException, InterruptedException, AWTException {
		
		try {
			System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
			System.setProperty("java.awt.headless", "false");
	        WebDriver driver = new FirefoxDriver();
	        driver.manage().window().maximize();
	        FirefoxOptions options = new FirefoxOptions();
	        options.setCapability("debuggerAddress", "127.0.0.1:54363");
	        driver.get("https://www.cowin.gov.in/home");
	        
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

	        driver.findElement(By.className("pin-search-btn")).click();
	        driver.findElement(By.className("pin-search-btn")).click();
	        
	        //selecting 18+ filter radio button
	        WebElement ageFilter = driver.findElement(By.id("flexRadioDefault2"));
	        JavascriptExecutor js1 = (JavascriptExecutor) driver;
	        js1.executeScript("arguments[0].click();",ageFilter);
	        
	        Robot robot = new Robot();

	        // Scroll Down using Robot class
	        robot.mouseWheel(12);

	        List<WebElement> listOfCenters = driver.findElements(By.className("slot-available-wrap"));
	        
	        logger.info("Checking " + listOfCenters.size() + " centers");
	        for(WebElement center : listOfCenters) {
	        	//logger.info("Center Info -> "+center.getText());
	        	String textToCheck = center.getText().toString();
	        	//logger.info("text to check->"+textToCheck);
	        	String[] valuesToBeCheckedTemp = textToCheck.split("\n");
	        	//System.out.println("Size is "+valuesToBeCheckedTemp.length);
	        	int totalSize = valuesToBeCheckedTemp.length;
	        	int totalOccurences = 0;
	        	totalOccurences += (countOccurencesOf(textToCheck, "Booked"))*3;
	        	totalOccurences += countOccurencesOf(textToCheck, "NA");
	        	if((totalSize-totalOccurences)!=0) {
	        		logger.info("!!!!!!!!!!!!  VACCINE AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
	        		emailHelper.sendVaccineAvailibilityMessage();
	        	}
	        	else {
	        		logger.info("!!!!!!!!!!!!  VACCINE NOT AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
	        	}
	        	
	        	
	        }
	        
	        //Close the browser
	        driver.quit();
		}
		catch(Exception e) {
			e.printStackTrace();
			emailHelper.sendRestartMessage();
			restarter.restartVaccineSlotCheckEngine();
		}
		
	}
	
	public int countOccurencesOf(String mainString, String word) {
		// split the string by spaces in a
	    //String a[] = mainString.split(" ");
		//System.out.println("Checking -> "+mainString);
		String a[] = mainString.split("\n");
		
	    // search for pattern in a
	    int count = 0;
	    for (int i = 0; i < a.length; i++)
	    {
	    // if match found increase count
	    if (word.equals(a[i]))
	        count++;
	    }
	    
	    //System.out.println(word + " occurred " + count + " times");
	    
	    return count;
	}
	
	
	
}
