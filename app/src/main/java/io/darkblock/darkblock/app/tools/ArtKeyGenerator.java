package io.darkblock.darkblock.app.tools;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Util;

public class ArtKeyGenerator extends Thread {

    private static final String ENDPOINT_GENERATE = Util.DARKBLOCK_API_ENDPOINT+"artkeygenerator/";
    private static final String ENDPOINT_POST =  Util.DARKBLOCK_API_ENDPOINT+"artkeypost";
    private static final String ENDPOINT_SAVE = Util.DARKBLOCK_API_ENDPOINT+"savekey/";

    private String walletId;

    // Constantly poll for new keys
    public void run() {
        walletId = App.getSession().getWalletId();

        while (true) {
            try{ Thread.sleep( 2000 ); } catch( Exception e ){
                System.out.println("Art Key Generation Thread crashed or stopped");
                break;
            }
            if (shouldGenerateNewKey()) {
                System.out.println("Starting key generation process...");
                //will check every 2 seconds to see if we need to generate a new art key
                //if we are triggered we generate a key
                String key = EncryptionTools.generateAESKey();
                // Send to site
                Map<String, String> data = new HashMap<String, String>();
                data.put("key", key);
                data.put("wallet", walletId);

                UUID uuid = UUID.randomUUID();
                data.put("artid",uuid.toString());

                HttpRequest request = HttpRequest.post(ENDPOINT_POST).form(data);

                if (request.ok()) {
                    System.out.println("Generated new key");
                    //System.out.println(uuid.toString());
                    //System.out.println(key);
                    // Save UUID/key pair
                    App.writeUUIDKeyPair(uuid,key);
                }
                System.out.println( "now going to save key to central repo" );
                request = HttpRequest.get(ENDPOINT_SAVE + walletId +"/" + uuid + "/" + URLEncoder.encode( key ) );
                if (request.ok()) {
                    System.out.println( "saved key in central repo" );
                }
            }
        }
    }

    // Check if a new key is needed by polling an endpoint
    private boolean shouldGenerateNewKey() {
        HttpRequest request = HttpRequest.get(ENDPOINT_GENERATE+walletId);
        JSONObject obj = Util.doJsonRequest(request);
        // Make sure generate=true
        if (obj != null) {
            try {
                return obj.getBoolean("generate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
