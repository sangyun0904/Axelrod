package com.sykim.axelrod;

import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.repository.StockRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlphaVantageService {

    @Autowired
    private StockRepository stockRepository;

    public List<Stock.History> getStockData(String ticker) throws IOException {

        // TODO : URL deprecated HttpUrlConnection -> HttpUriRequest

//        URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + ticker +"&apikey=" + ALPHA_VANTAGE_API_KEY);
        URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=demo");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        StringBuilder fullResponseBuilder = new StringBuilder();
        Reader streamReader = null;
        streamReader = new InputStreamReader(con.getInputStream());

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        fullResponseBuilder.append(content);

        List<Stock.History> dataList = new ArrayList<>();
        String data = fullResponseBuilder.toString();

//        System.out.println(data);
        JSONObject jsonObject = new JSONObject(data).getJSONObject("Time Series (Daily)");
        String latestRefreshedDate = new JSONObject(data).getJSONObject("Meta Data").getString("3. Last Refreshed");

        JSONArray keys = jsonObject.names();

        for (int i = 0; i < keys.length(); i++) {
            String key = keys.getString(i);

            dataList.add(new Stock.History(key
                    , Double.parseDouble(jsonObject.getJSONObject(key).getString("1. open"))
                    , Double.parseDouble(jsonObject.getJSONObject(key).getString("2. high"))
                    , Double.parseDouble(jsonObject.getJSONObject(key).getString("3. low"))
                    , Double.parseDouble(jsonObject.getJSONObject(key).getString("4. close"))
                    , Double.parseDouble(jsonObject.getJSONObject(key).getString("4. close"))
                    , Long.parseLong(jsonObject.getJSONObject(key).getString("5. volume"))));

            if (key.equals(latestRefreshedDate)) {
                Stock stock = stockRepository.findByTicker(ticker).get();
                stock.setCurrentPrice(Double.parseDouble(jsonObject.getJSONObject(key).getString("4. close")));
                stockRepository.save(stock);
            }

        }

        return dataList;
    }

}
