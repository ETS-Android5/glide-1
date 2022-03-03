package com.bumptech.glide.load.engine;

import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.bumptech.glide.Important;
import com.bumptech.glide.Why;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.EngineResource.ResourceListener;
import com.bumptech.glide.util.Executors;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Synthetic;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

final class ActiveResources {
   private final boolean isActiveResourceRetentionAllowed;
   private final Executor monitorClearedResourcesExecutor;
   @Important("20.当从activeEngineResources中通过key移除掉一个ResourceWeakReference后，该ResourceWeakReference将不再被引用（只有被该activeEngineResources引用的话），之后它将会被自动添加到"
         + "resourceReferenceQueue中，然后会在cleanReferenceQueue方法中执行该ResourceWeakReference相关的清理工作")
   @VisibleForTesting
   final Map<Key, ResourceWeakReference> activeEngineResources = new HashMap<>();
   private final ReferenceQueue<EngineResource<?>> resourceReferenceQueue = new ReferenceQueue<>();

   private ResourceListener listener;

   private volatile boolean isShutdown;
   @Nullable private volatile DequeuedResourceCallback cb;

   ActiveResources(boolean isActiveResourceRetentionAllowed) {
      this(isActiveResourceRetentionAllowed, java.util.concurrent.Executors.newSingleThreadExecutor(
            new ThreadFactory() {
               @Override
               public Thread newThread(@NonNull final Runnable r) {
                  return new Thread(
                        new Runnable() {
                           @Override
                           public void run() {
                              Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                              r.run();
                           }
                        },
                        "glide-active-resources");
               }
            }));
   }

   @VisibleForTesting
   ActiveResources(boolean isActiveResourceRetentionAllowed, Executor monitorClearedResourcesExecutor) {
      this.isActiveResourceRetentionAllowed = isActiveResourceRetentionAllowed;
      this.monitorClearedResourcesExecutor = monitorClearedResourcesExecutor;

      monitorClearedResourcesExecutor.execute(new Runnable() {
         @Override
         public void run() {
            cleanReferenceQueue();
         }
      });
   }

   void setListener(ResourceListener listener) {
      synchronized (listener) {
         synchronized (this) {
            this.listener = listener;
         }
      }
   }

   synchronized void activate(Key key, EngineResource<?> resource) {
      ResourceWeakReference toPut = new ResourceWeakReference(key, resource, resourceReferenceQueue, isActiveResourceRetentionAllowed);

      ResourceWeakReference removed = activeEngineResources.put(key, toPut);
      if (removed != null) {
         removed.reset();
      }
   }

   synchronized void deactivate(Key key) {
      ResourceWeakReference removed = activeEngineResources.remove(key);
      if (removed != null) {
         removed.reset();
      }
   }

   @Nullable
   synchronized EngineResource<?> get(Key key) {
      ResourceWeakReference activeRef = activeEngineResources.get(key);
      if (activeRef == null) {
         return null;
      }

      EngineResource<?> active = activeRef.get();
      if (active == null) {
         cleanupActiveReference(activeRef);
      }
      return active;
   }

   @SuppressWarnings({"WeakerAccess", "SynchronizeOnNonFinalField"})
   @Synthetic
   void cleanupActiveReference(@NonNull ResourceWeakReference ref) {
      synchronized (this) {
         activeEngineResources.remove(ref.key);

         if (!ref.isCacheable || ref.resource == null) {
            return;
         }
      }

      @Why("onResourceReleased回调中会移除这个ResourceWeakReference，为什么上面的同步代码块中还要移除？")
      EngineResource<?> newResource = new EngineResource<>(ref.resource, /*isMemoryCacheable=*/ true, /*isRecyclable=*/ false, ref.key, listener);
      listener.onResourceReleased(ref.key, newResource);
   }

   @SuppressWarnings("WeakerAccess")
   @Synthetic
   void cleanReferenceQueue() {
      while (!isShutdown) {
         try {
            ResourceWeakReference ref = (ResourceWeakReference) resourceReferenceQueue.remove();
            cleanupActiveReference(ref);

            // This section for testing only.
            DequeuedResourceCallback current = cb;
            if (current != null) {
               current.onResourceDequeued();
            }
            // End for testing only.
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @VisibleForTesting
   void setDequeuedResourceCallback(DequeuedResourceCallback cb) {
      this.cb = cb;
   }

   @VisibleForTesting
   interface DequeuedResourceCallback {
      void onResourceDequeued();
   }

   @VisibleForTesting
   void shutdown() {
      isShutdown = true;
      if (monitorClearedResourcesExecutor instanceof ExecutorService) {
         ExecutorService service = (ExecutorService) monitorClearedResourcesExecutor;
         Executors.shutdownAndAwaitTermination(service);
      }
   }

   @VisibleForTesting
   static final class ResourceWeakReference extends WeakReference<EngineResource<?>> {
      @SuppressWarnings("WeakerAccess")
      @Synthetic
      final Key key;

      @SuppressWarnings("WeakerAccess")
      @Synthetic
      final boolean isCacheable;

      @Nullable
      @SuppressWarnings("WeakerAccess")
      @Synthetic
      Resource<?> resource;

      @Important("21.super(referent, queue)将EngineResource和ReferenceQueue关联起来")
      @Synthetic
      @SuppressWarnings("WeakerAccess")
      ResourceWeakReference(
            @NonNull Key key,
            @NonNull EngineResource<?> referent,
            @NonNull ReferenceQueue<? super EngineResource<?>> queue,
            boolean isActiveResourceRetentionAllowed) {
         super(referent, queue);
         this.key = Preconditions.checkNotNull(key);
         this.resource = referent.isMemoryCacheable() && isActiveResourceRetentionAllowed ? Preconditions.checkNotNull(referent.getResource()) : null;
         isCacheable = referent.isMemoryCacheable();
      }

      void reset() {
         resource = null;
         clear();
      }
   }
}
