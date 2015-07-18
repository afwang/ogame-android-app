package com.wikaba.ogapp.database;

import android.util.Log;

import de.greenrobot.dao.AbstractDao;
import eu.codlab.sharedmutex.Mutex;

/**
 * Created by kevinleperf on 04/07/15.
 */
public abstract class AbstractController<T extends AbstractDao> {
    private Mutex _mutex;

    public abstract T getDao();

    protected AbstractController() {
        _mutex = new Mutex("ogame_mutex");
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
