package io.darkblock.darkblock.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.activity.ArtViewActivity;
import io.darkblock.darkblock.activity.FullscreenArtActivity;
import io.darkblock.darkblock.activity.MainActivity;
import io.darkblock.darkblock.activity.WelcomeActivity;
import io.darkblock.darkblock.app.tools.EncryptionTools;
import io.darkblock.darkblock.app.tools.TransferManager;

public class App extends Application {
    // Resources needed to be accessed globally
    private static Resources resources;
    private static Context context;

    // App-specific data
    public static Session session;
    private static HashMap<UUID,String> keyMap = new HashMap<>(); // Used to associate art UUIDs to decryption keys
    private static String[] deviceKeys = new String[2]; // Device keys used for transfer encryption

    private static TransferManager transferManager; // Used to negotiate transfers between devices

    // Runs when the app is started
    @Override
    public void onCreate() {
        super.onCreate();

        // Get global context and resources
        resources = getResources();
        context = getApplicationContext();

        // Load keymap
        tryLoadKeys();
    }

    public static Resources getAppResources() {
        return resources;
    }
    public static Context getAppContext() {
        return context;
    }


    /**
     * Get the width of the display based on app rotation, used for alignment
     * @return The width of the screen in pixels, relative to the app's orientation
     */
    public static int getDisplayWidth() {
        int orientation = 0;
        if (session != null) {
            orientation = session.getScreenOrientation();
        }

        return (orientation == 0 || orientation == 2) ?
            Resources.getSystem().getDisplayMetrics().widthPixels :
                Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    /**
     * Rotate an activity to match out orientation
     * @param act
     */
    public static void orientActivity(Activity act) {

        if (session == null) {
            return;
        }

        int orientation = session.getScreenOrientation();

        // NGL this sucks but idk how else to do it
        View view;
        if (act instanceof MainActivity) {
            view = act.findViewById(R.id.root);
        }else if (act instanceof WelcomeActivity) {
            view = act.findViewById(R.id.welcome);
        }else if (act instanceof ArtViewActivity) {
            view = act.findViewById(R.id.art_view_constraint_layout);
        }else if (act instanceof FullscreenArtActivity) {
            view = act.findViewById(R.id.fullscreen_art_constraint_layout);
        }else{
            view = null;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;

        // Rotate
        view.setRotation(orientation*90);

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) view.getLayoutParams();
        if (lp != null) {
            // Resize the screen
            if (orientation%2!=0) {
                lp.height = w;
                lp.width = h;
                view.requestLayout();

                //view.setTranslationX((w - h) / 2);
                //view.setTranslationY((h - w) / 2);
            }else{
                // Default dimensions
                lp.height = h;
                lp.width = w;
                view.requestLayout();

                view.setTranslationX(0);
                view.setTranslationY(0);
            }
        }
    }

    public static void rotate() {
        if (session != null) {
            int orientation = session.getScreenOrientation();
            session.setScreenOrientation(orientation + 1);
        }
    }


    /**
     * Load a session from storage. Returns true if such a session exists
     * @return
     */
    public static boolean tryLoadSession() {
        session = new Session();
        boolean result = session.load(context.getSharedPreferences("session", MODE_PRIVATE));
        if (!result) {
            session = null;
        }else{
            setupSession();
        }
        return result;
    }

    // Create a new app session
    public static void createSession(String walletId) {
        if (session == null) {
            session = new Session(walletId);

            SharedPreferences userDetails = context.getSharedPreferences("session", MODE_PRIVATE);
            // Save the session info
            session.save(userDetails);
            setupSession();

        }else{
            Log.e("Application","A session already exists!");
        }
    }

    // Runs after the session is created or loaded
    private static void setupSession() {
        // Setup the transfer manager
        transferManager = new TransferManager();
        transferManager.start();
    }

    // Sign out
    public static void destroySession() {
        // Delete session file
        SharedPreferences userDetails = context.getSharedPreferences("session", MODE_PRIVATE);
        SharedPreferences.Editor editor = userDetails.edit();
        editor.clear();
        editor.apply();
        session = null;

        // Close transfer manager
        transferManager.interrupt();
        transferManager = null;
    }

    // Get the current session
    public static Session getSession() {
        return session;
    }

    /* ART KEY MANAGEMENT */
    // Get a file to save and load session info from
    public static File getKeyFile() {
        File file = new File(context.getFilesDir(),"keys.darkblock");
        // Make sure the file actually exists
        if (!file.exists()) {
            try {
                file.createNewFile();
            }catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    // Write a UUID/key pair
    public static void writeUUIDKeyPair(UUID uuid, String key) {
        keyMap.put(uuid,key);
        saveKeymapToFile();
    }

    // Destroy a keypauir
    public static void destroyByUUID(UUID uuid) {
        keyMap.remove(uuid);
        saveKeymapToFile();
    }

    public static String getKeyFromUUID(UUID uuid) {
        return keyMap.get(uuid);
    }

    // Get the device's public/private RSA keys
    public static String[] getDeviceKeys() {return deviceKeys;}

    // Save the keymap to file
    private static void saveKeymapToFile() {
        // Save data
        // Create a keymap object
        JSONObject jsonKeyMap = new JSONObject();
        for (UUID id : keyMap.keySet()) {
            try {
                jsonKeyMap.put(id.toString(),keyMap.get(id));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer = new FileWriter(getKeyFile(),false);
            writer.append(jsonKeyMap.toString());

            // Add device keys
            writer.append('\n');
            writer.append(deviceKeys[0]);
            writer.append('\n');
            writer.append(deviceKeys[1]);

            writer.close();
            System.out.println("Wrote keys to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Try and load art and device RSA keys
    private static void tryLoadKeys() {
        try {
            Scanner fileReader = new Scanner(getKeyFile());

            String rawJson;
            try {
                rawJson = fileReader.nextLine();
                JSONObject uuidKeyPairs = new JSONObject(rawJson);

                Iterator<String> iter = uuidKeyPairs.keys();
                while (iter.hasNext()) {
                    // Remap from JSON to hashmap
                    String uuidString = iter.next();
                    String key = uuidKeyPairs.getString(uuidString);
                    UUID uuid = UUID.fromString(uuidString);

                    keyMap.put(uuid,key);
                }

                // Hey, we did it!
                System.out.println("Loaded " + keyMap.size() + " keys");

            }catch (NoSuchElementException ex) {
                ex.printStackTrace();
            }

            // Try and load device keys
            if (fileReader.hasNextLine()) {
                deviceKeys[0] = fileReader.nextLine();
                deviceKeys[1] = fileReader.nextLine();
            }

            if (deviceKeys[0] == null || deviceKeys[1] == null || deviceKeys[0].equals("null") || deviceKeys[1].equals("null")) {
                // Generate new keys
                deviceKeys = EncryptionTools.generateRSAKeys();
                System.out.println("Generated new device keys");
            }else {
                System.out.println("Loaded existing device keys");
            }

            saveKeymapToFile();

        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    // Used to store information relevant to a single app session
    // Currently this is only used to store the wallet id
    public static class Session {

        private String walletId = null;
        private String resourceUrl = "";
        private int screenOrientation = 0;

        public Session(String walletId) {
            this.walletId = walletId;
            this.resourceUrl = "https://"+walletId+".arweave.net/";
        }
        public Session() {this("");}

        public String getWalletId() {
            return walletId;
        }
        public String getArweaveResourceURL(String resource) {
            if (resource == null) {
                resource = "";
            }
            return resourceUrl+resource;
        }

        // Save session info to the sharedpreferences thing
        public void save(SharedPreferences preferences) {
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString("walletId",this.walletId);
            editor.putInt("orientation",screenOrientation);
            editor.apply();
        }

        // Load session info from sharedpreferences
        public boolean load(SharedPreferences preferences) {
            // Load wallet id
            if (preferences.contains("walletId")) {
                walletId = preferences.getString("walletId","");
                screenOrientation = preferences.getInt("orientation",0);
                this.resourceUrl = "https://"+walletId+".arweave.net/";
                return walletId.length() > 0;
            }
            return false;
        }

        public int getScreenOrientation() {
            return screenOrientation;
        }

        public void setScreenOrientation(int screenOrientation) {
            this.screenOrientation = screenOrientation % 4;
        }
    }

}