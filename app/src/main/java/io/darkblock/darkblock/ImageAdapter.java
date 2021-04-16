package io.darkblock.darkblock;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.tools.ThumbnailLoader;

public class ImageAdapter extends BaseAdapter {

    private Context context;

    public ImageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return ArtHelper.getArtListSize();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(313, 176));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            //new CardPresenter().bindView(imageView,ArtHelper.getWork(position));

            imageView.setElevation(6);
            imageView.setClickable(true);
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        // Load thumbnail
        ThumbnailLoader.glideLoad(ArtHelper.getArtByNum(position).getThumbnailImagUrl(),imageView);
        //ThumbnailCache.loadThumbnail((imageView),ArtHelper.getArtByNum(position).getThumbnailImagUrl());
        return imageView;
    }

}
