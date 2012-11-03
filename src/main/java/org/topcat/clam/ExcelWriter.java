package org.topcat.clam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.topcat.clam.model.PriceInfo;
import org.topcat.clam.model.Product;

public class ExcelWriter {

	private String file;

	public ExcelWriter(String file) {
		this.file = file;
	}

	public void write(int index, Map<String, PriceInfo> prices)
			throws IndexOutOfBoundsException, BiffException, IOException {
		// Excel获得文件
		Workbook wb = Workbook.getWorkbook(new File(file));
		// 打开一个文件的副本，并且指定数据写回到原文件
		WritableWorkbook book = Workbook.createWorkbook(new File(file), wb);
		// 添加一个工作表
		WritableSheet sheet = book.getSheet(0);
		PriceInfo dd = prices.get("dd");
		PriceInfo sn = prices.get("sn");
		PriceInfo z = prices.get("z");
		PriceInfo jd = prices.get("jd");
		try {
			sheet.addCell(new Label(2, index, "定价：" + z.getOriginalPrice()));
			sheet.addCell(new Label(3, index, "卖价：" + z.getNowPrice()));
			sheet.addCell(new Label(4, index, "折扣：" + z.getDiscount() + "%"));
			sheet.addCell(new Label(5, index, "定价：" + sn.getOriginalPrice()));
			sheet.addCell(new Label(6, index, "卖价：" + sn.getNowPrice()));
			sheet.addCell(new Label(7, index, "折扣：" + sn.getDiscount() + "%"));
			sheet.addCell(new Label(8, index, "定价：" + dd.getOriginalPrice()));
			sheet.addCell(new Label(9, index, "卖价：" + dd.getNowPrice()));
			sheet.addCell(new Label(10, index, "折扣：" + dd.getDiscount() + "%"));
			sheet.addCell(new Label(11, index, "定价：" + jd.getOriginalPrice()));
			sheet.addCell(new Label(12, index, "卖价：" + jd.getNowPrice()));
			sheet.addCell(new Label(13, index, "折扣：" + jd.getDiscount() + "%"));
			book.write();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} finally {
			try {
				book.close();
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Product> getProducts() throws IndexOutOfBoundsException,
			BiffException, IOException {
		Sheet sheet = Workbook.getWorkbook(new File(file)).getSheet(0);
		int rows = sheet.getRows();
		List<Product> reList = new ArrayList<Product>(rows);
		for (int i = 2; i < rows; i++) {
			String name = sheet.getCell(0, i).getContents();
			String num = sheet.getCell(1, i).getContents();
			if ("".equals(name) || "".equals(num)) {
				break;
			}
			reList.add(new Product(i, name, num));
		}
		return reList;
	}

}
