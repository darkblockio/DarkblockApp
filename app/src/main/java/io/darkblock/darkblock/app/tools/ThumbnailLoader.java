package io.darkblock.darkblock.app.tools;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.App;

public class ThumbnailLoader {

    /**
     * Load an image via glide
     * @param thumbnailURL The URL of the thumbnail
     * @param target The imageview to laod into
     */
    public static void glideLoad(String thumbnailURL, ImageView target) {
        Glide.with(target.getContext())
                .load(thumbnailURL)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.color.secondary_blue)
                .error(R.drawable.default_background)
                .into(target);
    }

    /**
     * Load an image via glide
     * @param thumbnailURL The URL of the thumbnail
     * @param target The imageview to laod into
     */
    public static void glideLoadResize(String thumbnailURL, final ImageView target) {
        Glide.with(target.getContext())
                .load(thumbnailURL)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.color.secondary_blue)
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        target.setImageDrawable(resource);
                        // Resize
                        target.setMinimumWidth(resource.getMinimumWidth());
                        target.setMinimumHeight(resource.getMinimumHeight());

                    }
                });
    }

}
