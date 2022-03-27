/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.util;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

import android.content.Context;
import android.service.wallpaper.WallpaperService;
import android.view.Surface;
import android.view.SurfaceControlViewHost;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.dot.customizations.module.Injector;
import com.dot.customizations.module.InjectorProvider;
import com.dot.customizations.module.PackageStatusNotifier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Default implementation of {@link SurfaceHolder.Callback} to render a static wallpaper when the
 * surface has been created.
 */
public class WallpaperSurfaceCallback implements SurfaceHolder.Callback {

    private final Context mContext;
    private final View mContainerView;
    private final SurfaceView mWallpaperSurface;
    @Nullable
    private final SurfaceListener mListener;
    @Nullable
    private final Future<Integer> mPlaceholderColor;
    private Surface mLastSurface;
    private SurfaceControlViewHost mHost;
    // Home workspace surface is behind the app window, and so must the home image wallpaper like
    // the live wallpaper. This view is rendered on here for home image wallpaper.
    private ImageView mHomeImageWallpaper;
    private boolean mSurfaceCreated;
    private PackageStatusNotifier.Listener mAppStatusListener;
    private PackageStatusNotifier mPackageStatusNotifier;
    public WallpaperSurfaceCallback(Context context, View containerView,
                                    SurfaceView wallpaperSurface, @Nullable Future<Integer> placeholderColor,
                                    @Nullable SurfaceListener listener) {
        mContext = context.getApplicationContext();
        mContainerView = containerView;
        mWallpaperSurface = wallpaperSurface;
        mListener = listener;

        // Notify WallpaperSurface to reset image wallpaper when encountered live wallpaper's
        // package been changed in background.
        Injector injector = InjectorProvider.getInjector();
        mPackageStatusNotifier = injector.getPackageStatusNotifier(context);
        mAppStatusListener = (packageName, status) -> {
            if (status != PackageStatusNotifier.PackageStatus.REMOVED) {
                resetHomeImageWallpaper();
            }
        };
        mPackageStatusNotifier.addListener(mAppStatusListener,
                WallpaperService.SERVICE_INTERFACE);
        mPlaceholderColor = placeholderColor;
    }

    public WallpaperSurfaceCallback(Context context, View containerView,
                                    SurfaceView wallpaperSurface, @Nullable SurfaceListener listener) {
        this(context, containerView, wallpaperSurface, null, listener);
    }

    public WallpaperSurfaceCallback(Context context, View containerView,
                                    SurfaceView wallpaperSurface) {
        this(context, containerView, wallpaperSurface, null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mLastSurface != holder.getSurface()) {
            mLastSurface = holder.getSurface();
            setupSurfaceWallpaper(/* forceClean= */ true);
        }
        if (mListener != null) {
            mListener.onSurfaceCreated();
        }
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
    }

    /**
     * Call to release resources and app status listener.
     */
    public void cleanUp() {
        releaseHost();
        if (mHomeImageWallpaper != null) {
            mHomeImageWallpaper.setImageDrawable(null);
        }
        mPackageStatusNotifier.removeListener(mAppStatusListener);
    }

    private void releaseHost() {
        if (mHost != null) {
            mHost.release();
            mHost = null;
        }
    }

    /**
     * Reset existing image wallpaper by creating a new ImageView for SurfaceControlViewHost
     * if surface state is not created.
     */
    private void resetHomeImageWallpaper() {
        if (mSurfaceCreated) {
            return;
        }

        if (mHost != null) {
            setupSurfaceWallpaper(/* forceClean= */ false);
        }
    }

    private void setupSurfaceWallpaper(boolean forceClean) {
        mHomeImageWallpaper = new ImageView(mContext);
        Integer placeholder = null;
        if (mPlaceholderColor != null && mPlaceholderColor.isDone()) {
            try {
                placeholder = mPlaceholderColor.get();
            } catch (InterruptedException | ExecutionException e) {
                // Ignore
            }
        }
        mHomeImageWallpaper.setBackgroundColor((placeholder != null) ? placeholder
                : ResourceUtils.getColorAttr(mContext, android.R.attr.colorSecondary));
        mHomeImageWallpaper.measure(makeMeasureSpec(mContainerView.getWidth(), EXACTLY),
                makeMeasureSpec(mContainerView.getHeight(), EXACTLY));
        mHomeImageWallpaper.layout(0, 0, mContainerView.getWidth(),
                mContainerView.getHeight());
        if (forceClean) {
            releaseHost();
            mHost = new SurfaceControlViewHost(mContext,
                    mContainerView.getDisplay(), mWallpaperSurface.getHostToken());
        }
        mHost.setView(mHomeImageWallpaper, mHomeImageWallpaper.getWidth(),
                mHomeImageWallpaper.getHeight());
        mWallpaperSurface.setChildSurfacePackage(mHost.getSurfacePackage());
    }

    @Nullable
    public ImageView getHomeImageWallpaper() {
        return mHomeImageWallpaper;
    }

    /**
     * Listener used to be notified when this surface is created
     */
    public interface SurfaceListener {
        /**
         * Called when {@link WallpaperSurfaceCallback#surfaceCreated(SurfaceHolder)} is called.
         */
        void onSurfaceCreated();
    }
}
