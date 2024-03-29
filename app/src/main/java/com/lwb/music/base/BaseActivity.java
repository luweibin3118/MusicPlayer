package com.lwb.music.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lwb.music.R;

import java.util.Iterator;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void startFragment(Fragment fragment) {
        startFragment(fragment, getContainerId());
    }

    public void startFragment(Fragment fragment, int containerId) {
        startFragment(fragment, containerId, true, null);
    }

    public void startFragment(Fragment fragment, int containerId, boolean addToBackStack) {
        startFragment(fragment, containerId, addToBackStack, null);
    }

    public void startFragment(Fragment fragment, int containerId, Map<View, String> sharedElements) {
        startFragment(fragment, containerId, true, sharedElements);
    }


    public void startFragment(Fragment fragment, int containerId, boolean addToBackStack, Map<View, String> sharedElements) {
        String tagName = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (sharedElements != null) {
            Iterator<Map.Entry<View, String>> iterator = sharedElements.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<View, String> entry = iterator.next();
                transaction.addSharedElement(entry.getKey(), entry.getValue());
            }
        }
        transaction.setCustomAnimations(R.anim.alpha_in_anim, R.anim.alpha_out_anim, R.anim.alpha_in_anim, R.anim.alpha_out_anim);
        transaction.replace(containerId, fragment, tagName);
        if (addToBackStack) {
            transaction.addToBackStack(tagName);
        } else {
            transaction.disallowAddToBackStack();
        }
        transaction.commit();
    }

    protected int getContainerId() {
        return 0;
    }
}