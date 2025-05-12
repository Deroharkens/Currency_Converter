import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class CurrencyConverter {
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private final String apiKey;

    public CurrencyConverter(String apiKey) {
        this.apiKey = apiKey;
    }

    public double convert(double amount, String fromCurrency, String toCurrency) {
        try {
            JSONObject exchangeRates = getExchangeRates(fromCurrency);
            if (exchangeRates != null) {
                JSONObject rates = exchangeRates.getJSONObject("conversion_rates");
                if (rates.has(toCurrency)) {
                    double rate = rates.getDouble(toCurrency);
                    return amount * rate;
                } else {
                    System.out.println("Target currency not found in exchange rates.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return -1; // Indicates error
    }

    private JSONObject getExchangeRates(String baseCurrency) {
        try {
            URL url = new URL(API_BASE_URL + apiKey + "/latest/" + baseCurrency);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("result").equals("success")) {
                    return jsonResponse;
                } else {
                    System.err.println("API Error: " + jsonResponse.getString("error-type"));
                }
            } else {
                System.err.println("HTTP request failed with code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error fetching exchange rates: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        // Replace with your actual API key
        String apiKey = "YOUR_API_KEY_HERE";
        CurrencyConverter converter = new CurrencyConverter(apiKey);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Currency Converter");
        System.out.println("------------------");

        System.out.print("Enter amount to convert: ");
        double amount = scanner.nextDouble();

        System.out.print("Enter source currency (3-letter code, e.g. USD): ");
        String fromCurrency = scanner.next().toUpperCase();

        System.out.print("Enter target currency (3-letter code, e.g. EUR): ");
        String toCurrency = scanner.next().toUpperCase();

        double result = converter.convert(amount, fromCurrency, toCurrency);
        if (result >= 0) {
            System.out.printf("%.2f %s = %.2f %s%n", 
                amount, fromCurrency, result, toCurrency);
        } else {
            System.out.println("Conversion failed. Please check your inputs and try again.");
        }

        scanner.close();
    }
}