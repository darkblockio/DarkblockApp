package io.darkblock.darkblock.app.layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.leanback.widget.ImageCardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.activity.ArtViewActivity;
import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.tools.ThumbnailLoader;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private static final float DESELECTED_SCALE = 0.85f;

    private final List<Artwork> artworkList;
    private int scrollAmount = 0;

    public GalleryAdapter(List<Artwork> artworkList) {
        this.artworkList = artworkList;
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //final ImageCardView cardView = new ImageCardView(new ContextThemeWrapper(parent.getContext(), R.style.GalleryCardStyle));
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_browser_item, parent, false);

        return new ViewHolder(view, (RecyclerView) parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapter.ViewHolder holder, int position) {
        position = position % artworkList.size();
        // Set image
        holder.setArtwork(artworkList.get(position));

        // Set default scale
        holder.getView().setScaleX(DESELECTED_SCALE);
        holder.getView().setScaleY(DESELECTED_SCALE);

        // Set on focus listener
        final int finalPosition = position % artworkList.size();
        holder.getView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Scale the view
                float scale = hasFocus ? 1f : DESELECTED_SCALE;
                v.animate()
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(250);

                if (hasFocus) {
                    // (screen width - width)/2
                    int x = (App.getDisplayWidth() - v.getWidth()) / 2;
                    int dx = (int) (v.getX() - x);
                    holder.recyclerView.smoothScrollBy(dx, 0);
                }
                // what the FUCK how did that work
            }
        });

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display fullscreen
                Intent intent = new Intent(holder.getView().getContext(), ArtViewActivity.class);
                intent.putExtra("ArtId", holder.getArtwork().getNum());
                holder.getView().getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artworkList.size()*10;
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View mainView;

        private final RecyclerView recyclerView;

        // Associated artwork
        private Artwork artwork;

        public ViewHolder(@NonNull View itemView, RecyclerView recyclerView) {
            super(itemView);
            mainView = itemView;
            mainView.setFocusable(true);
            this.recyclerView = recyclerView;
        }

        public void setArtwork(Artwork artwork) {
            this.artwork = artwork;
            ThumbnailLoader.glideLoadResize(artwork.getThumbnailImagUrl(), (ImageView) mainView.findViewById(R.id.image_gallery_item));
            ((TextView)mainView.findViewById(R.id.text_art_title)).setText(artwork.getTitle());
            ((TextView)mainView.findViewById(R.id.text_art_subtitle)).setText(artwork.getAuthor());
        }
        public Artwork getArtwork() {
            return artwork;
        }
        public View getView() {return mainView;}
    }

}
