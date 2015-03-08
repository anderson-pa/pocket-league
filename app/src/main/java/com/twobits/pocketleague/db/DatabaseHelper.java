package com.twobits.pocketleague.db;

import android.util.Log;

import com.couchbase.lite.Context;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.twobits.pocketleague.db.tables.Player;
import com.twobits.pocketleague.db.tables.Session;
import com.twobits.pocketleague.db.tables.Team;
import com.twobits.pocketleague.db.tables.Venue;

import junit.framework.Assert;

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

	public DatabaseHelper(Context context) {
		try {
            manager = new Manager(context, Manager.DEFAULT_OPTIONS);
            logd("Manager created.");
        } catch (IOException e) {
            loge("Cannot create manager object: ", e);
            return;
        }
        getDatabase();
	}

    public DatabaseHelper(android.content.Context context) {
        try {
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            logd("Manager created.");
        } catch (IOException e) {
            loge("Cannot create manager object: ", e);
            return;
        }
        getDatabase();
    }

    public Database getDatabase() {
        if (database == null) {
            if (!Manager.isValidDatabaseName(DATABASE_NAME)) {
                log("Bad database name");
            } else {
                try {
                    database = manager.getDatabase(DATABASE_NAME);
                    logd("Database created.");
                    createCouchViews();
                } catch (CouchbaseLiteException e) {
                    loge("Cannot get database: ", e);
                }
            }
        }
        return database;
    }

    public void createCouchViews() {
        View cvPlayerNames = database.getView("player-names");
        cvPlayerNames.setMap(mapField(Player.TYPE, Player.NAME), "1");

        View cvPlayerActFav = database.getView("player-act.fav");
        cvPlayerActFav.setMap(mapActFav(Player.TYPE), "1");

        View cvSessionNames = database.getView("session-names");
        cvSessionNames.setMap(mapField(Session.TYPE, Session.NAME), "1");

        View cvSessionActFav = database.getView("session-act.fav");
        cvSessionActFav.setMap(mapActFav(Session.TYPE), "1");

        View cvTeamNames = database.getView("team-names");
        cvTeamNames.setMap(mapField(Team.TYPE, Team.NAME), "1");

        View cvTeamActFav = database.getView("team-act.fav");
        cvTeamActFav.setMapReduce(mapActFav(Team.TYPE), null, "2");

        View cvVenueNames = database.getView("venue-names");
        cvVenueNames.setMapReduce(mapField(Venue.TYPE, Venue.NAME), null, "1");

        View cvVenueActFav = database.getView("venue-act.fav");
        cvVenueActFav.setMapReduce(mapActFav(Venue.TYPE), null, "1");
    }

    private Mapper mapField(String type_string, String field) {
        final String doc_type = type_string;
        final String doc_field = field;
        return new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (type.equals(doc_type)) {
                    emitter.emit(document.get(doc_field), null);
                }
            }
        };
    }

    private Mapper mapActFav(String type_string) {
        final String doc_type = type_string;
        return new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (type.equals(doc_type)) {
                    List<Object> keys = new ArrayList<>();
                    keys.add(document.get(Venue.IS_ACTIVE));
                    keys.add(document.get(Venue.IS_FAVORITE));
                    emitter.emit(keys, null);
                }
            }
        };
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
