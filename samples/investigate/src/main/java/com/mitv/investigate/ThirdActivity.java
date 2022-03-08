package com.mitv.investigate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ThirdActivity extends Activity {
   // https://github.com/bumptech/glide/issues/391

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      findViewById(R.id.iv_img).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            Glide.with(ThirdActivity.this)
                  .load("https://lmg.jj20.com/up/allimg/1114/113020142315/201130142315-1-1200.jpg")
                  .dontAnimate()
                  .diskCacheStrategy(DiskCacheStrategy.NONE)
                  .into((ImageView) v);
         }
      });
   }
}
