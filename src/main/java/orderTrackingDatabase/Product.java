package orderTrackingDatabase;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class Product {
	
	@CsvBindByName(column = "productLink")
	@CsvBindByPosition(position = 0)
	public String productLink;
	
	@CsvBindByName(column = "lastObservedPrice")
	@CsvBindByPosition(position = 1)
	public String lastObservedPrice;
	
	@CsvBindByName(column = "minPrice")
	@CsvBindByPosition(position = 2)
	public String minPrice;
	
	@CsvBindByName(column = "maxPrice")
	@CsvBindByPosition(position = 3)
	public String maxPrice;

	public String getProductLink() {
		return productLink;
	}

	public void setProductLink(String productLink) {
		this.productLink = productLink;
	}

	public String getLastObservedPrice() {
		return lastObservedPrice;
	}

	public void setLastObservedPrice(String lastObservedPrice) {
		this.lastObservedPrice = lastObservedPrice;
	}

	public String getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(String minPrice) {
		this.minPrice = minPrice;
	}

	public String getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(String maxPrice) {
		this.maxPrice = maxPrice;
	}

	@Override
	public String toString() {
		return "Product [productLink=" + productLink + ", lastObservedPrice=" + lastObservedPrice + ", minPrice="
				+ minPrice + ", maxPrice=" + maxPrice + "]";
	}
	
	
	
	
	
}
