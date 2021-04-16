package io.darkblock.darkblock.app;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {

    // HTTP constants
    public static final String DARKBLOCK_API_ENDPOINT = "https://dev1.darkblock.io/api/";

    public static JSONObject doJsonRequest(String requestUrl) {
        return doJsonRequest(HttpRequest.get(requestUrl));
    }
    /**
     * Perform an HTTP request with the expectation of receiving JSON
     * @param request
     * @return
     */
    public static JSONObject doJsonRequest(HttpRequest request) {
        request.acceptJson();
        try {
            String body = request.body();
            if (request.ok()) {
                try {
                    return new JSONObject(body);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                System.err.println("Error performing HTTP request, code " + request.code());
                return null;
            }
        }catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
