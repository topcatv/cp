package org.topcat.clam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jxl.read.biff.BiffException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.topcat.clam.model.PriceInfo;
import org.topcat.clam.model.Product;

public class Search {

    private static final PriceInfo PRICE_INFO = new PriceInfo(0, 0, 0);

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExcelWriter excelWriter = new ExcelWriter("d:/1.xls");
        List<Product> products;
        Search search = new Search();
        try {
            products = excelWriter.getProducts();
            for (Product product : products) {
                Map<String, PriceInfo> prices = new HashMap<String, PriceInfo>(
                        3);
                prices.put("dd", search.getPrice(product, StoreType.DD));
                prices.put("sn", search.getPrice(product, StoreType.SN));
                prices.put("z", search.getPrice(product, StoreType.Z));
                prices.put("jd", search.getPrice(product, StoreType.JD));
                excelWriter.write(product.getIndex(), prices);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public PriceInfo getPrice(Product product, StoreType type)
            throws ParserException, UnsupportedEncodingException, InterruptedException {
        if (type == StoreType.DD) {
            System.out.println(String.format("正在从 '当当' 获取图书：%s(%s),的价格......",
                    product.getBookName(), product.getBookNum()));
            sleep();
            return getDD(product);
        } else if (type == StoreType.JD) {
            System.out.println(String.format("正在从 '京东' 获取图书：%s(%s),的价格......",
                    product.getBookName(), product.getBookNum()));
            sleep();
            return getJD(product);
        } else if (type == StoreType.SN) {
            System.out.println(String.format("正在从 '苏宁' 获取图书：%s(%s),的价格......",
                    product.getBookName(), product.getBookNum()));
            sleep();
            return getSN(product);
        } else if (type == StoreType.Z) {
            System.out.println(String.format("正在从 '卓越' 获取图书：%s(%s),的价格......",
                    product.getBookName(), product.getBookNum()));
            sleep();
            return getZ(product);
        } else {
            return null;
        }
    }

    private void sleep() throws InterruptedException {
        Random random = new Random();
        int max = 5000;
        int min = 1000;
        int s = random.nextInt(max) % (max - min + 1) + min;
        Thread.sleep(s);
    }

    private PriceInfo getZ(Product product) throws ParserException {

        HttpClient httpclient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://www.amazon.cn/s/ref=nb_sb_noss?__mk_zh_CN=%E4%BA%9A%E9%A9%AC%E9%80%8A%E7%BD%91%E7%AB%99&url=search-alias%3Dstripbooks&field-keywords="
                + product.getBookNum()
                + "&rh=n%3A658390051%2Ck%3A"
                + product.getBookNum() + "&ajr=0");
        try {
            int statusCode = httpclient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod.getStatusLine());
                return new PriceInfo(1, 1, 1);
            }
            Parser parser = Parser.createParser(
                    getMethod.getResponseBodyAsString(), "UTF-8");
            NodeList nodeList = parser.parse(new HasAttributeFilter("class",
                    "newPrice"));
            NodeList children = nodeList.elementAt(0).getChildren();
            double old = getNumber(children.elementAt(2).toPlainTextString());
            double now = getNumber(children.elementAt(5).toPlainTextString());
            Double dicount = (now * 100) / old;
            return new PriceInfo(old, now, dicount.intValue());
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getMethod.releaseConnection();
        }
        return PRICE_INFO;
    }

    private PriceInfo getSN(Product product) throws ParserException, UnsupportedEncodingException {
        HttpClient httpclient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://www.suning.com/ssp-sp/searchMix?keyword="
                + URLEncoder.encode(product.getBookName(), "UTF-8")
                + "&start=0&row=20&orgId=0000A,28000B,50070F,50070Z,501611Z,501112Z,501313Z,500814Z,500831Z,501661Z,501662Z,501663Z,501664Z");
        try {
            int statusCode = httpclient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod.getStatusLine());
            }
            JSONObject jsonObject = new JSONObject(
                    getMethod.getResponseBodyAsString());
            JSONArray jsonArray = jsonObject.getJSONArray("docs");
            JSONObject jsonObject2 = jsonArray.getJSONObject(0);
            GetMethod getMethod2 = new GetMethod(
                    "http://www.suning.com/emall/SNProductStatusView?storeId=10052&catalogId=22001&productId="
                    + jsonObject2.getString("catentry_Id")
                    + "&langId=-7&partNumber=000000000102130398&cityId=9135&_=1336276995672");
            int statusCode2 = httpclient.executeMethod(getMethod2);
            if (statusCode2 != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod.getStatusLine());
            }

            JSONObject jsonObject3 = new JSONObject(
                    getMethod2.getResponseBodyAsString());

            double old = jsonObject3.getDouble("itemPrice");
            double now = jsonObject3.getDouble("promotionPrice");
            Double dicount = (now * 100) / old;
            return new PriceInfo(old, now, dicount.intValue());
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            getMethod.releaseConnection();
        }
        return PRICE_INFO;
    }

    private PriceInfo getJD(Product product) throws ParserException {
        HttpClient httpclient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://search.360buy.com/Search?keyword="
                + product.getBookNum());
        try {
            int statusCode = httpclient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod.getStatusLine());
            }
            Parser parser = Parser.createParser(
                    getMethod.getResponseBodyAsString(), "UTF-8");
            NodeList nodeList = parser.parse(new HasAttributeFilter("class",
                    "item-book"));
            Node node = nodeList.elementAt(0);
            String pn = getString(node.getText());

            GetMethod getMethod2 = new GetMethod("http://book.360buy.com/" + pn
                    + ".html");
            int statusCode2 = httpclient.executeMethod(getMethod2);
            if (statusCode2 != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod2.getStatusLine());
            }
            Parser parser2 = Parser.createParser(
                    getMethod2.getResponseBodyAsString(), "UTF-8");
            NodeList nodeList2 = parser2.parse(new HasAttributeFilter("id",
                    "book-price"));

            double old = getNumber(nodeList2.elementAt(0).toHtml());
            Matcher m = Pattern.compile(
                    "￥([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*)").matcher(
                    getMethod2.getResponseBodyAsString());
            int i = 1;
            double now = 1;
            while (m.find()) {
                if (i > 1) {
                    now = Double.parseDouble(m.group(1));
                }
                i++;
            }
            Double dicount = (now * 100) / old;
            return new PriceInfo(old, now, dicount.intValue());
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getMethod.releaseConnection();
        }
        return PRICE_INFO;
    }

    private PriceInfo getDD(Product product) throws ParserException {
        HttpClient httpclient = new HttpClient();
        GetMethod getMethod = new GetMethod("http://searchb.dangdang.com/?key="
                + product.getBookNum());
        try {
            int statusCode = httpclient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: "
                        + getMethod.getStatusLine());
            }
            Parser parser = Parser.createParser(
                    getMethod.getResponseBodyAsString(), "UTF-8");
            NodeList nodeList = parser.parse(new HasAttributeFilter("class",
                    "panel price"));
            Node node = nodeList.elementAt(0);
            NodeList children = node.getChildren();
            double originalPrice = getNumber(children.elementAt(1).toPlainTextString());
            double nowPrice = getNumber(children.elementAt(0).toPlainTextString());
            int discount = (int) getNumber(children.elementAt(2).toPlainTextString());
            return new PriceInfo(originalPrice, nowPrice, discount);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getMethod.releaseConnection();
        }
        return PRICE_INFO;
    }

    private double getNumber(String str) {
        return getNumber(str, 0);
    }

    private double getNumber(String str, int index) {
        Matcher m = Pattern.compile(
                "[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*").matcher(str);
        if (m.find()) {
            return Double.parseDouble(m.group(index));
        }
        return 0.0;
    }

    private String getString(String str) {
        Matcher m = Pattern.compile(
                "[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*").matcher(str);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    private enum StoreType {

        DD, SN, Z, JD
    }
}
