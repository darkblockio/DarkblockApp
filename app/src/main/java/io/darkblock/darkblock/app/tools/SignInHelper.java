package io.darkblock.darkblock.app.tools;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import io.darkblock.darkblock.app.Util;

public class SignInHelper {

    // Length of a signin code
    private static final int SIGN_IN_CODE_LENGTH = 6;

    private final String signInCode;
    private String walletId;

    // Create a signinhelper with a given code
    public SignInHelper(String signInCode) {
        this.signInCode = signInCode;
    }

    // Create a helper with a random coder
    public SignInHelper() {
        this(generateSignInCode());
    }

    /**
     * Try and get the wallet id for this sign in instance
     * @return
     */
    public String pollWalletId() {

        // Get wallet ID
        HttpRequest req = HttpRequest.get( Util.DARKBLOCK_API_ENDPOINT+"codepoller/"+signInCode);
        JSONObject json = Util.doJsonRequest(req);

        if (json != null) {
            // Convert to JSON
            try {
                // Get wallet id
                if (!json.isNull("wallet")) {
                    walletId = json.getString("wallet");
                }
                return walletId;

            }catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getSignInCode() {
        return signInCode;
    }

    /**
     * Generate a random signin code, consisting of alphanumeric characters
     * There are NOT guarenteed to be unique, and just exist for the sake of the demo
     * @return
     */
    public static String generateSignInCode() {
        StringBuilder code = new StringBuilder();
        for (int i=0;i<SIGN_IN_CODE_LENGTH;i++) {
            int x = (int) (Math.random()*36);
            if (x < 10) { // Number
                code.append(x);
            }else { // Character
                code.append((char) (x - 10 + 65));
            }
        }

        return code.toString();
    }

}