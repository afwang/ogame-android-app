package com.wikaba.ogapp.database;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.wikaba.ogapp.utils.AccountCredentials;

import java.util.ArrayList;
import java.util.List;

import greendao.Accounts;
import greendao.AccountsDao;

/**
 * Created by kevinleperf on 28/06/13.
 */
public class AccountsManager extends AbstractController<AccountsDao> {
    private static LruCache<String, Accounts> _lru = new LruCache<>(20);

    private static AccountsManager _instance;

    private Context _context;

    private String getHashMapKey(String universe, String username) {
        return String.format("%s_%s", universe, username);
    }

    private AccountsManager(Context context) {
        super();
        _context = context;
    }

    private static AccountsManager createNewInstance(Context context) {
        return new AccountsManager(context);
    }

    public static AccountsManager getInstance(Context context) {
        if (_instance == null) _instance = createNewInstance(context);
        return _instance;
    }

    public boolean hasAccount(String universe, String username) {
        return getAccount(universe, username) != null;
    }

    public long addAccount(String universe, String username, String password) {
        return addAccount(new Accounts(0l, universe, username, password));
    }

    public long addAccount(Accounts account) {
        lock();
        Accounts acc = getAccount(account.getUniverse(), account.getUsername());
        if (acc == null) {
            acc = new Accounts();
            acc.setId(null);//config.getId());
            acc.setUniverse(account.getUniverse());
            acc.setUsername(account.getUsername());
        }
        acc.setPassword(account.getPassword());

        long result = getDao().insertOrReplace(acc);
        unlock();
        return result;
    }

    public Accounts getAccount(String universe, String username) {
        Accounts cache = _lru.get(getHashMapKey(universe, username));
        if (cache == null) {
            cache = getDao().queryBuilder()
                    .where(AccountsDao.Properties.Universe.eq(universe),
                            AccountsDao.Properties.Username.eq(username))
                    .unique();
            if (cache != null) {
                _lru.put(getHashMapKey(universe, username), cache);
            }
        }
        return cache;
    }

    public Accounts getAccount(long row_id) {
        Accounts accounts = getDao().queryBuilder()
                .where(AccountsDao.Properties.Id.eq(row_id))
                .unique();

        return accounts;
    }

    public AccountCredentials getAccountCredentials(long row_id) {
        return getAccountCredentials(getAccount(row_id));
    }

    public AccountCredentials getAccountCredentials(String universe, String username) {
        return getAccountCredentials(getAccount(universe, username));
    }

    public AccountCredentials getAccountCredentials(Accounts account) {
        if (account != null) {
            AccountCredentials credentials = new AccountCredentials();
            credentials.id = account.getId();
            credentials.passwd = account.getPassword();
            credentials.universe = account.getUniverse();
            credentials.username = account.getUsername();

            return credentials;
        }
        return null;
    }

    public ArrayList<AccountCredentials> getAccountsCredentialsWithoutPassword() {
        List<Accounts> list = findAll();
        ArrayList<AccountCredentials> credentials = new ArrayList<>(list.size());

        for (Accounts acc : list) {
            AccountCredentials credential = new AccountCredentials();
            credential.username = acc.getUsername();
            credential.universe = acc.getUniverse();
            credential.id = acc.getId();
            credentials.add(credential);
        }
        return credentials;
    }

    public boolean removeAccount(String universe, String username) {
        Accounts account = getAccount(universe, username);

        if (account != null) {
            getDao().delete(account);
            _lru.evictAll(); //TODO evict only the account
            return true;
        }
        return false;
    }

    public AccountsDao getDao() {
        return DatabaseManager.getInstance().getSession().getAccountsDao();
    }

    public List<Accounts> findAll() {
        List<Accounts> list = getDao().loadAll();
        //TODO PUT EACH IN CACHE
        return list;
    }
}
