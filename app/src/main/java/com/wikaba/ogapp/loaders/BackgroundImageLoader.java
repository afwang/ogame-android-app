/*
	Copyright 2014 Alexander Wang

	This file is part of Ogame on Android.

	Ogame on Android is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Ogame on Android is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wikaba.ogapp.loaders;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.res.ResourcesCompat;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wikaba.ogapp.ApplicationController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * <p>This loader is meant to be used for loading non-essential
 * image resources, to take the work of image loading off the UI thread.
 * Additionally, this method will not load anything on low memory devices,
 * done by checking the isLowRamDevice offered by the ActivityManagerCompat
 * class</p>
 *
 * Created by afwang on 9/1/15.
 */
public class BackgroundImageLoader extends AsyncTaskLoader<ImageView> {
	private static final Logger logger = LoggerFactory.getLogger(BackgroundImageLoader.class);

	private int resid;
	private Context ctx;
	private WeakReference<Drawable> bgImg;

	/**
	 * @param appContext Application context needed to obtain ActivityManager for checking
	 * 		memory status of device. Low memory devices will not have the background loaded.
	 * 		This value is also needed to obtain a reference to the Resources object.
	 * @param resid Resource ID of the image resource
	 */
	public BackgroundImageLoader(ApplicationController appContext, int resid) {
		super(appContext);
		this.resid = resid;
		ctx = appContext;
		bgImg = null;
	}

	@Override
	public void onStartLoading() {
		this.forceLoad();
	}

	@Override
	public ImageView loadInBackground() {
		ActivityManager aMan = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
		boolean lowRam = ActivityManagerCompat.isLowRamDevice(aMan);
		if(lowRam) {
			logger.info("This is a low memory device. Not going to load background image for {}", resid);
			return null;
		}
		logger.info("This is not a low memory device. Now loading background image for {}", resid);
		Resources res = ctx.getResources();
		ImageView img = null;
		try {
			Drawable background = null;
			if(bgImg != null) {
				background = bgImg.get();
			}
			if(background == null) {
				background = ResourcesCompat.getDrawable(res, resid, null);
				bgImg = new WeakReference<Drawable>(background);
			}
			img = new ImageView(ctx);
			img.setImageDrawable(background);
			img.setScaleType(ImageView.ScaleType.CENTER_CROP);
			ViewGroup.LayoutParams layoutParams =
					new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT
					);
			img.setLayoutParams(layoutParams);
		} catch(Resources.NotFoundException nfe) {
			logger.error("Resource with given ID not found", nfe);
		}
		return img;
	}
}
