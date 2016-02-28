import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;


public class Shopicircuit {

    /**
     * This is a dynamic programming question so rather than using brute force, ask a friend to remember previous
     * sub-instances.
     * Pre condition: M is the maximum mass that can be held.
     *     compsAndKeys is an array containing only computers and keyboards
     *     compsAndKeys should contain values (m_i, p_i), are the mass and price of the ith object in the store.
     *Post condition: optCost is the cost of the optimal solution to fill your knapsack with the greatest amount you can
     *     carry. amountTaken is the amount the person can carry.
     * @param M                 Maximum mass that you can carry
     * @param compsAndKeys      JSONArray of all the computers and keyboards available
     * @return                  Return a string that prints the maximum amount of keyboards you can hold and the price
     */
    public static String maxAmountToCarry(int M, JSONArray compsAndKeys) {
        /*
        Table:
        subI[i][ M'] denotes the sub-instance of optimally holding set of items with mass M' with the first i objects.
        We store the cost optCost[i][ M'] of an optimal solution and amountTaken[i][M'].
        */

        int n = getNumberOfOptions(compsAndKeys);

        double[][] optCost = new double[n + 1][M + 1];
        int[][] amountTaken = new int[n + 1][M + 1];

        /*
        Base Cases: The number of objects is 0. When there are 0 objects opCost and amountTaken are 0.
         */
        for (int i = 0; i <= M; i++) {
            optCost[0][i] = 0;
            amountTaken[0][i] = 0;
        }

        JSONArray data = (JSONArray) compsAndKeys.clone();

        //Loop over sub-instances in the tables.
        int i = 1;
        for (Object obj : data) {

            JSONObject product = (JSONObject) obj;
            JSONArray variants = (JSONArray) product.get("variants");

            if (variants.size() > 0) {

                for (Object o : variants) {

                    for (int k = 0; k <= M; k++) {

                        JSONObject variant = (JSONObject) o;

                        /*
                        Solve instance subI[i][M'] and fill the tables at index {i, M'}
                        Decide whether to include(1) or exclude(2) the ith item. Either way we remove the last object,
                        but in case (2) we decrease the size of the amount held by  the space needed for this item.
                        Then we ask our friend for an optimal solution of the resulting sub-instance. They give us
                        (1)subI[i-1][ M'] or (2) subI[i-1][M'-m_i] which they had stored in the table. If we decide to
                        include the ith item, then we add this item to the friends solution.

                         Try each possible answer.
                         cases k = 1, 2 where 1=exclude 2=include
                         */

                        double optCost_opt_1 = optCost[i - 1][k];
                        double optCost_opt_2;
                        long m_i = (long) variant.get("grams");
                        String price_i = (String) variant.get("price");
                        double p_i = Double.parseDouble(price_i);

                        if ((k - m_i) >= 0) {
                            optCost_opt_2 = optCost[i - 1][(int) (k - m_i)] + p_i;

                        } else {
                            optCost_opt_2 = Double.NEGATIVE_INFINITY;
                        }
                        //Check which case gives the optimal result
                        if (optCost_opt_1 >= optCost_opt_2) {
                            optCost[i][k] = optCost_opt_1;
                        } else {
                            optCost[i][k] = optCost_opt_2;
                            amountTaken[i][k] = amountTaken[i - 1][(int) (k - m_i)] + 1;
                        }
                    }
                    i++;
                }
            }
        }
        return "The optimal amount of items to take is " + amountTaken[n][M] + " at a price of " + optCost[n][M];
    }

    /**
     * Parse the json string and return the products JSONArray
     * @param json      JSON formatted string
     * @return          JSONArray productList
     */
    public static JSONArray getProductList(String json) {
        try {
            JSONParser parser = new JSONParser();
            Object resultObject = parser.parse(json);

            if (resultObject instanceof JSONObject) {
                JSONObject obj = (JSONObject) resultObject;
                return (JSONArray) obj.get("products");
            }
        } catch (ParseException p) {
            p.printStackTrace();
        }
        return null;
    }

    /**
     * Loop through the elements in the JSONArray and return the amount of variants
     * @param json      JSONArray of products that have variants
     * @return          int value of how many variants exists of all the products .
     */
    public static int getNumberOfOptions(JSONArray json) {
        JSONArray data = (JSONArray) json.clone();
        int num = 0;
        for (Object obj : data) {
            JSONObject product = (JSONObject) obj;
            JSONArray variants = (JSONArray) product.get("variants");
            if (variants.size() > 0) {
                for (Object o : variants) {
                    num += 1;
                }
            }else{
                num += 1;
            }
        }
        return num;
    }

    /**
     * Submit a HTTP GET request and generate a JSON string
     * @param url       string url for get request
     * @return          json string response from get request
     * @throws IOException
     */
    public static String fetchJSON(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            try {

                HttpResponse result = httpclient.execute(httpGet);
                return EntityUtils.toString(result.getEntity(), "UTF-8");

            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    /**
     * Loops through product list and creates a JSONArray containing only computers and keyboards.
     * @param jsonString    JSON formatted string
     * @return              JSONArray with only computers and keyboards from the product list
     */
    public static JSONArray getCompsAndKeys(String jsonString) {
        JSONArray productList = getProductList(jsonString);
        JSONArray compsAndKeyboards = new JSONArray();
        for (Object obj : productList) {
            JSONObject product = (JSONObject) obj;
            if (product.get("product_type").equals("Computer")
                    || product.get("product_type").equals("Keyboard")) {
                compsAndKeyboards.add(product);
            }
        }
        return compsAndKeyboards;
    }

    public static void main(String[] args) throws Exception {
        int mass = 100000; // 100 kg
        String url = "http://shopicruit.myshopify.com/products.json";
        String jsonString = fetchJSON(url);
        JSONArray compsAndKeys = getCompsAndKeys(jsonString);
        String amountCanCarry = maxAmountToCarry(mass, compsAndKeys);
        System.out.println(amountCanCarry);
    }
}

