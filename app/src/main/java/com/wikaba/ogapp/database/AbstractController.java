/*
	Copyright 2015 Kevin Le Perf

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

package com.wikaba.ogapp.database;

import android.util.Log;

import de.greenrobot.dao.AbstractDao;
import eu.codlab.sharedmutex.Mutex;

/**
 * Created by kevinleperf on 04/07/15.
 */
public abstract class AbstractController<T extends AbstractDao> {
    private static final Object object = new Object();
    private Mutex _mutex;

    public abstract T getDao();

    protected AbstractController() {
        synchronized (object) {
            if (_mutex == null) _mutex = new Mutex("ogame_mutex");
        }
    }

    public void delete(T item) {
        getDao().delete(item);
    }

    public void update(T item) {
        getDao().insertOrReplace(item);
    }

    protected void lock() {
        _mutex.lock();
        Log.e("AbstractController", "locking");
    }

    protected void unlock() {
        Log.e("AbstractController", "unlocking");
        _mutex.unlock();
    }
}
