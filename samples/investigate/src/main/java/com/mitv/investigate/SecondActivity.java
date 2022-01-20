package com.mitv.investigate;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.util.concurrent.atomic.AtomicInteger;

public class SecondActivity extends FragmentActivity {
    private AtomicInteger count = new AtomicInteger(0);

    ImageView mIvImgTop;
    ImageView mIvImgBottom;

    Bitmap mBitmapTop;

    CustomTarget<Bitmap> mCustomTarget = new CustomTarget<Bitmap>() {
        @RequiresApi(api = VERSION_CODES.KITKAT)
        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            mBitmapTop = resource;
            mIvImgTop.setImageBitmap(mBitmapTop);
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mIvImgTop = findViewById(R.id.iv_img_top);
        mIvImgBottom = findViewById(R.id.iv_img_bottom);
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.i("investigate", "load start");
//            if (count.get() % 2 == 0) {
//                Glide.with(this).asBitmap().load("https://c-ssl.duitang.com/uploads/item/202002/03/20200203232102_hjcin.thumb.700_0.jpg").into(customTarget);
//            } else {
//                Glide.with(this).asBitmap().load("https://c-ssl.duitang.com/uploads/item/202003/18/20200318222019_SFuNx.thumb.700_0.jpeg").into(customTarget);
//            }
            simpleInvestigate(mIvImgTop);
        }
        return true;
    }

    int previousWidth = 1200;
    int previousHeight = 675;

    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void simpleInvestigate(ImageView iv) {
        previousWidth -= 10;
        previousHeight -= 10;

//        Glide.with(this)
//                .asBitmap()
//                .load("https://lmg.jj20.com/up/allimg/1114/113020142315/201130142315-1-1200.jpg")
//                .into(mCustomTarget);
        Glide
                .with(this)
                .load("https://lmg.jj20.com/up/allimg/1114/113020142315/201130142315-1-1200.jpg")
//                .override(previousWidth, previousHeight) // 因为每次需要的宽高都与上次请求的宽高不同，因此不会使用上次的请求，而是重新创建一个
                .into(mIvImgBottom);

        Glide
                .with(this)
                .load("https://lmg.jj20.com/up/allimg/1114/113020142315/201130142315-1-1200.jpg")
//                .override(previousWidth, previousHeight) // 因为每次需要的宽高都与上次请求的宽高不同，因此不会使用上次的请求，而是重新创建一个
                .into(mIvImgTop);

////        iv.setDrawingCacheEnabled(true);
//        iv.postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                Bitmap cachedBitmap = ((BitmapDrawable) mIvImgTop.getDrawable()).getBitmap();
//
//                iv.setImageBitmap(null);
//                iv.setImageDrawable(null);
//
//                iv.post(new Runnable() {
//                    @Override
//                    public void run() {
////                        mBitmapTop.reconfigure(mBitmapTop.getWidth() - 1, mBitmapTop.getHeight() - 1, Config.RGB_565);
//                        mBitmapTop.reconfigure(mBitmapTop.getWidth() - 1, mBitmapTop.getHeight() - 1, mBitmapTop.getConfig());
//                        Log.i("simpleInvestigate", "cachedBitmap.getConfig():" + mBitmapTop.getConfig().name());
//
//                        iv.setImageBitmap(mBitmapTop);
//                        simpleInvestigate(iv);
//
//                    }
//                });
//
////                cachedBitmap.reconfigure(cachedBitmap.getWidth() - 1, cachedBitmap.getHeight() - 1, Config.RGB_565);
////                Log.i("simpleInvestigate", "cachedBitmap.getConfig():" + cachedBitmap.getConfig().name());
////
////                mIvImgBottom.setImageBitmap(cachedBitmap);
//            }
//        }, 500);

//        iv.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                simpleInvestigate(mIvImgBottom);
//            }
//        }, 1000);
    }
}