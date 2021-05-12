package vaccineAvailibility;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
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
	
	public void checkSlot() throws IOException, MessagingException, InterruptedException {
		int retryCount = 0;
		
		try {
		
		System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
		WebDriver driver = null;
		try {
			driver = new FirefoxDriver();
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
        		System.out.println("!!!!!!!!!!!!  VACCINE AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        		emailHelper.sendVaccineAvailibilityMessage();
        	}
        	else {
        		System.out.println("!!!!!!!!!!!!  VACCINE NOT AVAILABLE  !!!!!!!!!!!!!!!!!!!!!!!");
        	}

        }
        driver.quit();
		}
		catch(Exception e) {
			e.printStackTrace();
			restarter.restart();
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
