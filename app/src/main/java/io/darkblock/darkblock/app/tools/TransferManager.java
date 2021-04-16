package io.darkblock.darkblock.app.tools;

import android.os.HandlerThread;
import android.util.Base64;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Util;

public class TransferManager extends Thread {

    private static final String ENDPOINT = Util.DARKBLOCK_API_ENDPOINT+"transferpoll/";
    private static final String TRANSFER_ENDPOINT = Util.DARKBLOCK_API_ENDPOINT+"transferartkey/%s/%s/%s/%s";
    private static final String CONFIRMATION_ENDPOINT = Util.DARKBLOCK_API_ENDPOINT+"/transferconfirmation/%s/%s/%s";

    // Poll endpoints
    public void run() {
        String[] deviceKeys = App.getDeviceKeys();
        String walletId = App.getSession().getWalletId();
        String url = ENDPOINT+walletId;

        // Infinite loop
        while (true) {
            // Put a delay


            try {
                Thread.sleep(5000);
            }catch (InterruptedException ex) {
                System.err.println("Transfer thread interrupted or stopped");
                //break;
            }
            //System.err.println( "we are polling the endpoint, yay: " + url );

            // Poll
            HttpRequest req = HttpRequest.get(url);
            JSONObject json = Util.doJsonRequest(req);

            if (json != null && json.has("transaction")) {
                // Get transfer type
                try {
                    String transactionType = json.getString("transaction");
                    System.err.println("Found transfer of type " + transactionType);
                    if (transactionType.equals("save")) {
                        // Save new key!
                        System.err.println("Saving new device key...");

                        // Get data
                        String id = json.getString("artid");
                        String key = URLDecoder.decode(json.getString("art_key"));

                        UUID artId = UUID.fromString(id);

                        System.err.println( "saving key, id is " + id + " key: " + key );
                        // Save it!
                        App.writeUUIDKeyPair(artId,key);

                        // Shoot off a confirmation
                        System.err.println( "sending confirmation" );
                        HttpRequest conRequest = HttpRequest.get(String.format(CONFIRMATION_ENDPOINT,json.getString("wallet"),walletId,id));
                        Util.doJsonRequest(conRequest);
                    }
                }catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Called by @link{ArtHelper} to get rid of an art key we no longer need
     */
    public static void disposeArtKey(String fromWallet, String toWallet, UUID artId, String key) {
        System.err.println("Disposing of old art key for id " + artId);
        // Get URL
        String url = String.format(TRANSFER_ENDPOINT,fromWallet,toWallet,artId.toString(), URLEncoder.encode(key));
        System.err.println( url );
        // Perform transfer
        HttpRequest request = HttpRequest.get(url);
        JSONObject obj = Util.doJsonRequest(request);

        // Finally, get rid of the key from the device
        App.destroyByUUID(artId);
        System.err.println("Key destroyed");
    }

}
