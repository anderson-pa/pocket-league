package info.andersonpa.pocketleague;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.couchbase.lite.Database;
import info.andersonpa.pocketleague.backend.DataInterface;
import info.andersonpa.pocketleague.gameslibrary.GameSubtype;
import info.andersonpa.pocketleague.gameslibrary.GameType;

public abstract class DataInterfaceActivity extends ActionBarActivity implements DataInterface {
    protected String LOGTAG = getClass().getSimpleName();

    public static final String APP_PREFS = "PocketLeaguePreferences";
    private SharedPreferences settings;
    private SharedPreferences.Editor prefs_editor;

    public Database getDatabase() {
        return ((PocketLeagueApp) getApplication()).getDatabase();
    }

    public void deleteDatabase() {
        ((PocketLeagueApp) getApplication()).deleteDatabase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = this.getSharedPreferences(APP_PREFS, 0);
        prefs_editor = settings.edit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public String getPreference(String pref_name, String pref_default) {
        return settings.getString(pref_name, pref_default);
    }

    public void setPreference(String pref_name, String pref_value) {
        prefs_editor.putString(pref_name, pref_value);
        prefs_editor.commit();
    }

    public GameType getCurrentGameType() {
        return getCurrentGameSubtype().toGameType();
    }

    public GameSubtype getCurrentGameSubtype() {
        return GameSubtype.valueOf(getPreference("currentGameSubtype", GameSubtype.UNDEFINED.name()));
    }

    public void setCurrentGameSubtype(GameSubtype gamesubtype) {
        setPreference("currentGameSubtype", gamesubtype.name());
    }

    public void log(String msg) {
        Log.i(LOGTAG, msg);
    }

    public void logd(String msg) {
        Log.d(LOGTAG, msg);
    }

    public void loge(String msg, Exception e) {
        Log.e(LOGTAG, msg + ": " + e.getMessage());
    }
}