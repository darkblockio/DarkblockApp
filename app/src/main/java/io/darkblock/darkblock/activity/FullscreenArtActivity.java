package io.darkblock.darkblock.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.kevinsawicki.http.HttpRequest;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.Util;
import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.tools.EncryptionTools;

public class FullscreenArtActivity extends Activity {

    private ImageView fullscreenImageView;
    private ImageView loadingAnimation;
    private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_art);

        getActionBar().hide();

        // Get the artwork we want
        int artNum = getIntent().getIntExtra("artNum",-1);
        // If we have an error, go back
        if (artNum == -1) {
            showToastAndExit(R.string.error_no_art_specified);
            return;
        }
        Artwork art = ArtHelper.getArtByNum(artNum);

        // Get the views needed
        fullscreenImageView = findViewById(R.id.fullscreenArtView);
        loadingAnimation = findViewById(R.id.loadingAnimation);
        subtitle = findViewById(R.id.statusMessage);

        // Load animation
        Glide.with(this).load(R.raw.darkblock_animation_loop).asGif().into(loadingAnimation);

        // Is the art encrypted?
        new GetImageAndDecryptTask().execute(art);
//        if (art.isEncrypted()) {
//            // Check that we have a key
//            String key = App.getKeyFromUUID(art.getId());
//            if (key == null) {
//                // Whoops
//                showToastAndExit(R.string.error_no_key);
//            }else{
//                // Run a task to decrypt and show the art
//                new GetImageAndDecryptTask().execute(art);
//            }
//        }else{
//            // Just use glide
//            Glide.with(this)
//                    .load(getResourceURL(art))
//                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                    .placeholder(R.drawable.default_background)
//                    .listener(new RequestListener<String, GlideDrawable>() {
//                        @Override
//                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        // Show image once loaded
//                        @Override
//                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                            showFullscreenImage();
//                            return true;
//                        }
//                    })
//                    .into(fullscreenImageView);
//        }
    }


    /**
     * Show the fullscreen image
     */
    private void showFullscreenImage() {
        fullscreenImageView.setVisibility(View.VISIBLE);
        loadingAnimation.setVisibility(View.GONE);
        subtitle.setVisibility(View.GONE);
    }


    /**
     * Show a toast message and exit this screen
     * @param string
     */
    private void showToastAndExit(int string) {
        Toast.makeText(getApplicationContext(),string,Toast.LENGTH_LONG).show();
        finish();
    }


    /**
     * Get the resource url for a piece of art
     * @param artwork
     * @return
     */
    private String getResourceURL(Artwork artwork) {
        return "https://"+App.getSession().getWalletId()+".arweave.net/"+artwork.getTransactionId();
    }


    private void updateSubtitle(int text) {
        subtitle.setText(text);
    }


    /** Task used to load a piece of art and decrypt it, if needed
     */
    private class GetImageAndDecryptTask extends AsyncTask<Artwork, Void, byte[]> {

        @Override
        protected byte[] doInBackground(Artwork... artworks) {
            // Get the artwork and decryption key
            try {
                Artwork artwork = artworks[0];
                System.out.println("getting key from central store");
                //String key = App.getKeyFromUUID(artwork.getId());
                String url = Util.DARKBLOCK_API_ENDPOINT + "getkey/" + artwork.getCreator() + "/" + artwork.getId();
                System.out.println( "getkey: " + url );
                String key = Util.doJsonRequest(url).getString("key");
                System.err.println("found " + key + " for " + artwork.getTransactionId() + "  -- " + artwork.getId());

                String darkblockId = artwork.getDarkblockId();
                // Get the encrypted data
                String resourceURL = "https://ceankr7ih4fyn5y3v77h4xhkdv74674a3xfbvh7et5duadp2gkiq.arweave.net/" + darkblockId;
                System.out.println("art url : " + resourceURL);
                HttpRequest request = HttpRequest.get(resourceURL).followRedirects(true);
                String rawData = request.body();
                System.out.println("request response: " + request.code());
                System.out.println("length of data: " + rawData.length());
                // Make sure everything went through ok
                if (request.ok()) {
                    // Get those bytes
                    return EncryptionTools.aesDecrypt(key, rawData);
                } else {
                    System.out.println("request is not ok!");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            System.out.println("onPostExecute");
            // Make sure there wasn't an issue decrypting the image
            if (bytes != null) {
                System.out.println("onPostExecute");
                updateSubtitle(R.string.status_decrypting);
                // Turn bytes into a bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) {
                    // Assign bitmap and show
                    fullscreenImageView.setImageBitmap(bmp);
                    System.out.println("Decryption completed");

                    showFullscreenImage();
                }else{
                    showToastAndExit(R.string.error_image_processing);
                }
            }else{
                System.out.println("bytes are null");
                showToastAndExit(R.string.error_http);
            }
        }
    }
}