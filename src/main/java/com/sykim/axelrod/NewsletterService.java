package com.sykim.axelrod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sykim.axelrod.model.Newsletter;
import com.sykim.axelrod.repository.NewsletterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class NewsletterService {
    @Value("${newyorktimes.apikey}")
    private String NYT_API_KEY;

    @Autowired
    private NewsletterRepository newsletterRepository;

    public List<Newsletter> getNewYorkTimesLetters() throws IOException {
        URL url = new URL("https://api.nytimes.com/svc/topstories/v2/home.json?api-key=" + NYT_API_KEY);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        Reader streamReader = new InputStreamReader(con.getInputStream());

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

//        System.out.println("newsletters : " + content);
        JsonArray resultsArray = new JsonObject(content.toString()).get

        return new ArrayList<>();
    }

}
