package com.alibaba.idst.nls.dialog.stock.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
public class StockService {
    public static final String appkey = "";
    public static final String sign = "";

    public static String getHttpResult(String url) throws IOException {
        Long startTime = System.currentTimeMillis();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        Response response = client.newBuilder().readTimeout(1000L, TimeUnit.MILLISECONDS).build().newCall(request).execute();
        if (url.indexOf("finance.stock_list") > 0){
            log.info("curl {}, cost time {}ms", url, System.currentTimeMillis() - startTime);
        }
        String result = response.body().string();
        log.info("curl {}, response is {}, cost time {}ms", url, result, System.currentTimeMillis() - startTime);
        return result;
    }

    public static Stock getStockCodeByNameMarket(String companyName, String marketCode){
        String url = String.format("http://api.k780.com/?app=finance.stock_list&category=%s&appkey=%s&sign=%s&format=json", marketCode, appkey, sign);
        Stock stock = new Stock(false);
        try {
            String result = getHttpResult(url);
            JSONObject resultJson = JSON.parseObject(result);
            JSONObject companyStock;
            for (Object tmpCompany : resultJson.getJSONObject("result").getJSONArray("lists")){
                companyStock = (JSONObject)tmpCompany;
                if (Objects.equals(companyName, companyStock.getString("sname"))){
                    stock.setExist(true);
                    stock.setMarket(marketCode);
                    stock.setStockCode(companyStock.getString("symbol"));
                    return stock;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stock;
    }

    public static String getValueFromRedis(String key){
        final int db = 3;
        final String host = "";
        final int port = 1 ;
        final String pw = "";
        String result = null;
        try (Jedis jedis = new Jedis(host, port)){
            jedis.auth(pw);
            jedis.select(db);
            result = jedis.get(key);
        }

        return result;
    }

    public static Stock getSelfStockCodeByNameMarket(String companyName){
        final String redisKey = "stock_companyname_to_code";
        Stock result = new Stock(false);
        String redisValue = getValueFromRedis(redisKey);
        if (Objects.isNull(redisValue)){
            return result;
        }

        JSONObject companyNameToCodeMap = JSON.parseObject(redisValue);
        if (companyName.contains(companyName)){
            result.setStockCode(companyNameToCodeMap.getString(companyName));
            result.setExist(true);
            if (result.getStockCode().startsWith("gb")){
                result.setMarket("us");
            }else if (result.getStockCode().startsWith("hk")){
                result.setMarket("hk");
            }else{
                result.setMarket("hs");
            }
        }
        return result;
    }

    public static Stock getStockCode(String companyName){
        List<String> marketList = Lists.newArrayList("us", "hk", "hs");
        Stock output = new Stock(false);
        for (String market : marketList){
            output = getStockCodeByNameMarket(companyName, market);
            if (Objects.nonNull(output) && output.isExist()){
                return output;
            }
        }
        return output;
    }

    public static Stock getSelfStockCode(String companyName){
        return getSelfStockCodeByNameMarket(companyName);
    }

    public static Double getCoinRate(String originalCoinCountry, String targetCoinCountry){
        String url = String.format("http://api.k780.com/?app=finance.rate&scur=%s&tcur=%s&appkey=%s&sign=%s", originalCoinCountry, targetCoinCountry, appkey, sign);

        try {
            String result = getHttpResult(url);
            JSONObject resultJson = JSON.parseObject(result);
            return resultJson.getJSONObject("result").getDouble("rate");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Stock getStockPrice(String publishCompany){
        Stock stock = new Stock(false);
        try{
            stock = getSelfStockCode(publishCompany);
            if (Objects.isNull(stock) || !stock.exist){
                return stock;
            }

            String url = String.format("http://api.k780.com/?app=finance.stock_realtime&symbol=%s&appkey=%s&sign=%s", stock.stockCode, appkey, sign);
            String httpResult = getHttpResult(url);
            JSONObject resultJson = JSON.parseObject(httpResult);
            Double price = resultJson.getJSONObject("result").getJSONObject("lists").getJSONObject(stock.stockCode).getDouble("last_price");
            stock.setMarketPrice(price);

            Double rate = 0.0;
            if (Objects.equals(stock.market, "us")){
                rate = getCoinRate("USD", "CNH");
                stock.setCnPrice(price * rate);
            }else if (Objects.equals(stock.market, "hk")){
                rate = getCoinRate("HKD", "CNH");
                stock.setCnPrice(price * rate);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return stock;
    }

    @Data
    public static class Stock{
        /**
         * 该股票在相应市场价格是多少
         */
        private Double marketPrice;

        /**
         * 股票的code，用来查询股价用
         */
        private String stockCode;

        /**
         * 该股票值多少人民币
         */
        private Double cnPrice;

        /**
         * 该股票属于哪个市场
         */
        private String market;

        /**
         * 该股票是否存在
         */
        private boolean exist;

        public Stock(boolean exist){
            this.exist = exist;
        }
    }
}
