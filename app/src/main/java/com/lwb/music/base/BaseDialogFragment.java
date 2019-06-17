package com.lwb.music.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.lwb.music.R;

public abstract class BaseDialogFragment extends DialogFragment {
    protected View rootView;

    public interface OnDismissCallback {
        void onDismiss();
    }

    private OnDismissCallback onDismissCallback;

    public void setOnDismissCallback(OnDismissCallback onDismissCallback) {
        this.onDismissCallback = onDismissCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (showWithAnim()) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialog;
        }
        View view = inflater.inflate(getLayoutRes(), container, false);
        rootView = view;
        initView(rootView);
        return view;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onDismissCallback != null) {
            onDismissCallback.onDismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDismissCallback = null;
    }

    protected boolean showWithAnim() {
        return false;
    }

    protected abstract void initView(View rootView);

    protected abstract int getLayoutRes();

    protected int contentGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = contentGravity();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.dimAmount = 1 - 0.6f;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

}