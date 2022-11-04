package me.darki.konas.util.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;

public class Requester {

    public static String toString(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Darki-Bot/1.0.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder result = new StringBuilder();
        in.lines().forEach(result::append);
        in.close();
        return result.toString();
    }

    public static InputStream toInputStream(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Darki-Bot/1.0.0");
        return conn.getInputStream();
    }

    @SafeVarargs
    public static String toStringWithHeader(URL url, AbstractMap.SimpleEntry<String, String>... headers) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Darki-Bot/1.0.0");
        for(AbstractMap.SimpleEntry<String, String> header : headers) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder result = new StringBuilder();
        in.lines().forEach(result::append);
        in.close();
        return result.toString();
    }

}
