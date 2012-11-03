package org.topcat.clam.model;

public class Product {

	private String bookName;
	private String bookNum;
	private int index;
	
	public Product(int index,String bookName,String bookNum){
		this.index = index;
		this.bookName = bookName;
		this.bookNum = bookNum;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getBookNum() {
		return bookNum;
	}

	public void setBookNum(String bookNum) {
		this.bookNum = bookNum;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return String.format("index:%d,bookName:%s,bookNum:%s", index, bookName, bookNum);
	}

}
