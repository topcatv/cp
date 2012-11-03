package org.topcat.clam.model;

public class PriceInfo {

	private double originalPrice;
	private double nowPrice;
	private int discount;
	
	public PriceInfo(double originalPrice, double nowPrice, int discount) {
		this.originalPrice = originalPrice;
		this.nowPrice = nowPrice;
		this.discount = discount;
	}

	@Override
	public String toString() {
		return "PriceInfo [originalPrice="
				+ originalPrice
				+ ", nowPrice="
				+ nowPrice
				+ ", discount="
				+ discount
				+ "]";
	}

	public double getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(double originalPrice) {
		this.originalPrice = originalPrice;
	}

	public double getNowPrice() {
		return nowPrice;
	}

	public void setNowPrice(double nowPrice) {
		this.nowPrice = nowPrice;
	}

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}
}
