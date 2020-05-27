package URLInformation;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

public class URLInformation {
    File databaseCountry;
    File databaseCity;
    DatabaseReader readerCountry;
    DatabaseReader readerCity;
    public URLInformation() throws IOException {
        databaseCountry = new File("GeoLite2-Country.mmdb");
        databaseCity = new File("GeoLite2-City.mmdb");
        readerCountry = new DatabaseReader.Builder(databaseCountry).build();
        readerCity = new DatabaseReader.Builder(databaseCity).build();
    }

    public Country Country(String url) throws IOException, GeoIp2Exception {
        InetAddress ipAddress = InetAddress.getByName(new URL(url).getHost());
        CountryResponse response = readerCountry.country(ipAddress);
        return response.getCountry();

    }

    public City City(String url)  throws IOException, GeoIp2Exception {
        InetAddress ipAddress = InetAddress.getByName(new URL(url).getHost());
        CityResponse response = readerCity.city(ipAddress);
        return response.getCity();
    }

    public static void main(String[] args) throws IOException, GeoIp2Exception {
        URLInformation U = new URLInformation();
        String url = "https://www.google.com";
        Country r = U.Country(url);
        System.out.println(r.getIsoCode());
        System.out.println(r.getName());
        System.out.println(r.getConfidence());

        City c = U.City(url);
        System.out.println(c.getName());
        System.out.println(c.getGeoNameId());
    }

}
