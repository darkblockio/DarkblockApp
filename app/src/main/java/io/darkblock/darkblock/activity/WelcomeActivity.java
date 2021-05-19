package io.darkblock.darkblock.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.tools.SignInHelper;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getActionBar().hide();

        // Check if a session already exists
        if (App.tryLoadSession()) {
            // Skip to main screen
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
            this.finish();
            return;
        }
        // Otherwise, proceed as usual

        // Create sign in manager
        SignInHelper signInHelper = new SignInHelper();
        String code = signInHelper.getSignInCode();

        // Format code for display
        StringBuilder displayCode = new StringBuilder();
        for (int i=0;i<6;i++) {
            displayCode.append(code.charAt(i));
            if (i != 5) {
                displayCode.append(' ');
            }
            if (i == 2) {
                displayCode.append("- ");
            }
        }
        // Put on screen
        TextView codeDisplay = findViewById(R.id.signInCodeDisplay);
        codeDisplay.setText(displayCode.toString());

        new SignInTask().execute(signInHelper);
    }

    /**
     * Used to retrieve the wallet id given a sign in code
     */
    private class SignInTask extends AsyncTask<SignInHelper, Void, String> {
        @Override
        protected String doInBackground(SignInHelper... helpers) {
            SignInHelper helper = helpers[0];

            do {
                helper.pollWalletId();
                // Wait 2 seconds between each try
                try
                {
                    Thread.sleep( 2000 );
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }

            }while (helper.getWalletId() == null);

            // We got it!
            return helper.getWalletId();
        }

        // After we get the wallet id, move onto main activity
        protected void onPostExecute(String result) {
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            App.createSession(result);
            startActivity(i);
            finish();
        }
    }

}