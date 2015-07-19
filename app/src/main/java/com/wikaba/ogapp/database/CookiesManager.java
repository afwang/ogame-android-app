package com.wikaba.ogapp.database;

import android.content.Context;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import greendao.Cookies;
import greendao.CookiesDao;

/**
 * Created by kevinleperf on 28/06/13.
 */
public class CookiesManager extends AbstractController<CookiesDao> {
    public static String GUID = "guid";

    private static CookiesManager _instance;

    private Context _context;

    private CookiesManager(Context context) {
        super();
        _context = context;
    }

    private static CookiesManager createNewInstance(Context context) {
        return new CookiesManager(context);
    }

    public static CookiesManager getInstance(Context context) {
        if (_instance == null) _instance = createNewInstance(context);
        return _instance;
    }

    public ArrayList<HttpCookie> getAllHttpCookies() {
        List<Cookies> all = getDao().loadAll();

        ArrayList<HttpCookie> result = new ArrayList<>();
        HttpCookie tmp;
        for (Cookies cookie : all) {
            tmp = cookie.toHttpCookie();
            if (tmp != null) result.add(tmp);
        }
        return result;
    }

    @Override
    public CookiesDao getDao() {
        return DatabaseManager.getInstance().getSession().getCookiesDao();
    }

    public void saveCookies(List<HttpCookie> cookies) {

        List<Cookies> cookies_obj = new ArrayList<>();

        Iterator<HttpCookie> cookieIter = cookies.iterator();
        Cookies cookie_obj;

        while (cookieIter.hasNext()) {
            HttpCookie cookie = cookieIter.next();
            String name = cookie.getName();
            String value = cookie.getValue();
            long maxAgeDelta = cookie.getMaxAge();
            //TODO EXPIRATION AS DATE AND USE JODATIME
            long expiration = Calendar.getInstance().getTimeInMillis() / 1000;
            expiration += maxAgeDelta;
            String domain = cookie.getDomain();
            String path = cookie.getPath();
            boolean secureFlag = cookie.getSecure();

            if (domain != null && domain.length() > 0) {
                if (path != null && path.length() == 0) {
                    path = "/";
                }
                cookie_obj = new Cookies();
                cookie_obj.setName(name);
                cookie_obj.setValue(value);
                cookie_obj.setExpiration(expiration);
                cookie_obj.setDomain(domain);
                cookie_obj.setPath(path);
                cookie_obj.setSecure(secureFlag ? 1 : 0);
                //No support for HTTP-only flag yet
                cookie_obj.setHttp_secure(0);

                cookies_obj.add(cookie_obj);
            }
        }

        lock();
        getDao().insertOrReplaceInTx(cookies_obj);
        unlock();
    }
}
