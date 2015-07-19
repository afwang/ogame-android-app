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
