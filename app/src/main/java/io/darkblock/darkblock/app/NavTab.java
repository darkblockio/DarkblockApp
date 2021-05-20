package io.darkblock.darkblock.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import io.darkblock.darkblock.R;

@SuppressLint("AppCompatCustomView")
public abstract class NavTab extends TextView  {

    public NavTab(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);
        setTextAppearance(R.style.NavigationItem);
    }

    public abstract Fragment makeFragment();

}
