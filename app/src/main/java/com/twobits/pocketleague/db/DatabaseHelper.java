package com.twobits.pocketleague.db;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.android.AndroidContext;
import com.twobits.pocketleague.db.tables.Player;
import com.twobits.pocketleague.db.tables.Session;
import com.twobits.pocketleague.db.tables.Team;
import com.twobits.pocketleague.db.tables.Venue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    protected String LOGTAG = getClass().getSimpleName();
	private static final String DATABASE_NAME = "pocketleague";
	private static final String DATABASE_VERSION = "1";
    Manager manager;
    Database database = null;
	private Context context;

	public DatabaseHelper(Context context) {
		super();
		this.context = context;

        try {
            manager = new Manager(new AndroidContext(this.context), Manager.DEFAULT_OPTIONS);
            logd("Manager created.");
        } catch (IOException e) {
            loge("Cannot create manager object: ", e);
            return;
        }

        if (!Manager.isValidDatabaseName(DATABASE_NAME)) {
            log("Bad database name");
        } else {
            try {
                database = manager.getDatabase(DATABASE_NAME);
                logd ("Database created.");
                createCouchViews();
            } catch (CouchbaseLiteException e) {
                loge("Cannot get database: ", e);
            }
        }
	}

    public void createCouchViews() {
        com.couchbase.lite.View cViewAllPlayers = database.getView("all-players");
        cViewAllPlayers.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (type.equals(Player.TYPE)) {
                    List<Object> keys = new ArrayList<>();
                    keys.add(document.get(Player.IS_ACTIVE));
                    keys.add(document.get(Player.IS_FAVORITE));
                    emitter.emit(keys, null);
                }
            }
        }, "1");

        com.couchbase.lite.View cViewAllSessions = database.getView("all-sessions");
        cViewAllSessions.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (type.equals(Session.TYPE)) {
                    List<Object> keys = new ArrayList<>();
                    keys.add(document.get(Session.GAME_SUBTYPE));
                    keys.add(document.get(Session.IS_ACTIVE));
                    keys.add(document.get(Session.IS_FAVORITE));
                    emitter.emit(keys, null);
                }
            }
        }, "1");

        com.couchbase.lite.View cViewAllTeams = database.getView("all-teams");
        cViewAllTeams.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (type.equals(Team.TYPE)) {
                    List<Object> keys = new ArrayList<>();
                    keys.add(document.get(Team.IS_ACTIVE));
                    keys.add(document.get(Team.IS_FAVORITE));
                    emitter.emit(keys, null);
                }
            }
        }, "1");

        com.couchbase.lite.View cViewAllVenues = database.getView("all-venues");
        cViewAllVenues.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (type.equals(Venue.TYPE)) {
                    List<Object> keys = new ArrayList<>();
                    keys.add(document.get(Venue.IS_ACTIVE));
                    keys.add(document.get(Venue.IS_FAVORITE));
                    emitter.emit(keys, null);
                }
            }
        }, "1");
    }

    public Database getDatabase() {
        return database;
    }

    public void close() {
        manager.close();
    }

    public void create(Map<String, Object> content) {
        Document document = database.createDocument();
        try {
            document.putProperties(content);
        } catch (CouchbaseLiteException e) {
            loge("Cannot write document to database", e);
        }
    }

    public void get(long id) {

    }

    public void update(Document document, Map<String, Object> content) {
        try {
            document.putProperties(content);
            logd("updated retrievedDocument=" + String.valueOf(document.getProperties()));
        } catch (CouchbaseLiteException e) {
            loge("Cannot update document", e);
        }
    }

    public void delete(Document document) {
        try {
            document.delete();
            logd("Deleted document, deletion status = " + document.isDeleted());
        } catch (CouchbaseLiteException e) {
            loge("Cannot delete document", e);
        }
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
