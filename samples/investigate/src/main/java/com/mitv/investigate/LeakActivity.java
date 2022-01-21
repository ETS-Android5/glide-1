package com.mitv.investigate;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

public class LeakActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InstanceHolder.Companion.injectInstance(this);
    }
}
