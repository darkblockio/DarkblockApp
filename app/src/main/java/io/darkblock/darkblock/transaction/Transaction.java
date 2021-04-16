package io.darkblock.darkblock.transaction;

import androidx.annotation.NonNull;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.darkblock.darkblock.app.Util;

/**
 * Wrapper class used to contain information for a single arweave transaction
 */
public class Transaction {

    private String id;
    private String owner;
    private final HashMap<String,String> tags = new HashMap<>();

    public Transaction(String id) {
        this.id = id;
    }

    public String getId() {return id;}

    // Tag accessors
    public void putTag(String name, String value) {
        tags.put(name,value);
    }

    public String getTag(String name) {
        return tags.get(name);
    }

    public String getTagOr(String name, String error) {
        String tag = getTag(name);
        return tag == null ? error : tag;
    }

    public void setOwner(String owner){
        this.owner = owner;
    }

    public String getOwner(){
        return owner;
    }

    public Map getTags(){ return tags; }

    @NonNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(id);
        result.append(" {");
        for (Map.Entry<String,String> tag : tags.entrySet()) {
            result.append(tag.getKey());
            result.append("=");
            result.append(tag.getValue());
            result.append(", ");
        }
        result.append("}");

        return result.toString();
    }

    public boolean hasTag(String name) {
        return tags.containsKey(name);
    }

    private static final String ARWEAVE_ENDPOINT = "https://arweave.net/graphql";
    /**
     * Query arweave for transactions
     * @param params Parameters for the HTTP request
     * @return An array containing the transactions, or null if there was an error
     */
    public static Transaction[] queryTransactions(String params) {

        HttpRequest transactionRequest = HttpRequest
                .get(ARWEAVE_ENDPOINT,true,"query",params);
        JSONObject result = Util.doJsonRequest(transactionRequest);
        System.err.println( result );
        if (result != null) {
            try {
                // Loop over everything
                JSONArray root = result.getJSONObject("data").getJSONObject("transactions").getJSONArray("edges");
                Transaction[] resultArray = new Transaction[root.length()];

                for (int i=0;i<root.length();i++) {

                    // Create transaction object
                    JSONObject obj = root.getJSONObject(i).getJSONObject("node");
                    Transaction transaction = new Transaction(obj.getString("id"));

                    transaction.setOwner(obj.getJSONObject("owner").getString("address"));
                    // Get tags
                    JSONArray tags = obj.getJSONArray("tags");
                    for (int j = 0; j < tags.length(); j++) {
                        JSONObject tag = tags.getJSONObject(j);
                        transaction.putTag(tag.getString("name"), tag.getString("value"));
                    }

                    // Add to array
                    resultArray[i] = transaction;
                }

                return resultArray;

            }catch (JSONException ex) {
                // On JSON error, return null
                ex.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

}
