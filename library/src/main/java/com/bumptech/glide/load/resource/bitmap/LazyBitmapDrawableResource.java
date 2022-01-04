package com.bumptech.glide.load.resource.bitmap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.util.Preconditions;

/**
 * Lazily allocates a {@link android.graphics.drawable.BitmapDrawable} from a given {@link android.graphics.Bitmap} on the first call to {@link #get()}.
 */
public final class LazyBitmapDrawableResource implements Resource<BitmapDrawable>, Initializable {

    private final Resources resources;
    private final Resource<Bitmap> bitmapResource;

    @Nullable
    public static Resource<BitmapDrawable> obtain(
            @NonNull Resources resources, @Nullable Resource<Bitmap> bitmapResource) {
        if (bitmapResource == null) {
            return null;
        }
        return new LazyBitmapDrawableResource(resources, bitmapResource);
    }

    private LazyBitmapDrawableResource(
            @NonNull Resources resources, @NonNull Resource<Bitmap> bitmapResource) {
        this.resources = Preconditions.checkNotNull(resources);
        this.bitmapResource = Preconditions.checkNotNull(bitmapResource);
    }

    @NonNull
    @Override
    public Class<BitmapDrawable> getResourceClass() {
        return BitmapDrawable.class;
    }

    @NonNull
    @Override
    public BitmapDrawable get() {
        return new BitmapDrawable(resources, bitmapResource.get());
    }

    @Override
    public int getSize() {
        return bitmapResource.getSize();
    }

    @Override
    public void recycle() {
        bitmapResource.recycle();
    }

    @Override
    public void initialize() {
        if (bitmapResource instanceof Initializable) {
            ((Initializable) bitmapResource).initialize();
        }
    }
}
