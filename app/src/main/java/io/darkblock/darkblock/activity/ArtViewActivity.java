package io.darkblock.darkblock.activity;

import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;

import java.time.format.DateTimeFormatter;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.tools.EncryptionTools;
import io.darkblock.darkblock.app.tools.ThumbnailLoader;

public class ArtViewActivity extends Activity {

    private Artwork artwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_view);

        // Get the artwork
        int id = getIntent().getIntExtra("ArtId",0);
        artwork = ArtHelper.getArtByNum(id);

        // Populate data
        ThumbnailLoader.glideLoad(artwork.getThumbnailImagUrl(),(ImageView) findViewById(R.id.previewImage));

        TextView title = findViewById(R.id.artTitle);
        TextView details = findViewById(R.id.artDetails);

        title.setText(artwork.getTitle());

        StringBuilder detailBuilder = new StringBuilder();
        detailBuilder.append(getString(R.string.author_detail,artwork.getAuthor()))
                .append("\n\n")
                .append(artwork.getDescription());

        details.setText(detailBuilder.toString());

        // Set decrypt and display button
        Button button = findViewById(R.id.buttonMaximize);

        if (!artwork.isEncrypted()) {
            button.setText(R.string.art_maximize_unencrypted);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to fullscreen art view activity
                Intent i = new Intent(getApplicationContext(),FullscreenArtActivity.class);
                i.putExtra("artNum",artwork.getNum());
                startActivity(i);
            }
        });
    }

    /**
     * DEBUG - copy transaction id
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Copy
        if (keyCode == 31) {// letter c

            String transferId = artwork.getTransactionId();

            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", transferId);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getApplicationContext(),"Copied transaction key to clipboard",Toast.LENGTH_SHORT).show();
        }

        return super.onKeyUp(keyCode, event);
    }
}