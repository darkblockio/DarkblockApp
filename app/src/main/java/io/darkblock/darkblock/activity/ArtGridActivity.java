package io.darkblock.darkblock.activity;

import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import io.darkblock.darkblock.ImageAdapter;
import io.darkblock.darkblock.R;

public class ArtGridActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_grid);

        // Set toolbar title
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setTitle(R.string.art_grid);

        GridView gridView = findViewById(R.id.artGrid);
        gridView.setAdapter(new ImageAdapter(this));


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ArtViewActivity.class);
                intent.putExtra("ArtId",position);
                startActivity(intent);
            }
        });

        gridView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            View previousItem;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (previousItem != null) {
                    previousItem.setScaleX(1);
                    previousItem.setScaleY(1);
                    previousItem.setTranslationZ(0);
                }
                view.setScaleX(1.2f);
                view.setScaleY(1.2f);
                view.setTranslationZ(8);

                previousItem = view;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
}