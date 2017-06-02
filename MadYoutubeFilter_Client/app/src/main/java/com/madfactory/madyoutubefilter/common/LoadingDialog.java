package com.madfactory.madyoutubefilter.common;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Window;

import com.madfactory.madyoutubefilter.R;

/**
 * Created by nnnyyy on 2017-06-02.
 */

public class LoadingDialog extends Dialog {
    public LoadingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);
    }
}
