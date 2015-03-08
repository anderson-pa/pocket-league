package com.twobits.pocketleague.db.tables;

import android.content.Context;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Team extends CouchDocumentBase {
    public static final String TYPE = "team";
    public static final String NAME = "name";
    public static final String MEMBERS = "member_ids";
    public static final String COLOR = "color";
    public static final String IS_ACTIVE = "is_active";
    public static final String IS_FAVORITE = "is_favorite";

    // Constructors
    public Team(String team_name, List<String> member_ids, int color, boolean is_favorite) {
        // name and size combination should be unique
        content.put("type", TYPE);
        content.put(NAME, team_name);
        if (member_ids == null) {
            content.put(MEMBERS, Arrays.asList(getId()));
        } else {
            content.put(MEMBERS, member_ids);
        }
        content.put(COLOR, color);
        content.put(IS_ACTIVE, true);
        content.put(IS_FAVORITE, is_favorite);
    }

    public Team(Database database, String team_name, List<String> member_ids, int color,
                boolean is_favorite) {
        this(team_name, member_ids, color, is_favorite);
        if (database != null) {
            createDocument(database);
        }
    }

    Team(Document document) {
        super(document);
    }

    // Static database methods
    public static Team getFromId(Database database, String id) {
        Document document = database.getDocument(id);
        return new Team(document);
    }

    public static Team findByName(Database database, String name) throws CouchbaseLiteException {
        Query query = database.getView("team-names").createQuery();
        query.setStartKey(name);
        query.setEndKey(name);
        QueryEnumerator result = query.run();

        if (result.hasNext()) {
            return Team.getFromId(database, result.next().getDocumentId());
        } else {
            return null;
        }
    }

    private static List<Team> getAll(Database database, List<Object> key_filter) throws
            CouchbaseLiteException {
        List<Team> teams = new ArrayList<>();

        Query query = database.getView("team-names").createQuery();
        query.setKeys(key_filter);
        QueryEnumerator rows = query.run();
        for (Iterator<QueryRow> it = rows; it.hasNext();) {
            QueryRow row = it.next();
            teams.add(getFromId(database, row.getDocumentId()));
        }
        return teams;
    }

    public static List<Team> getAllTeams(Database database) throws CouchbaseLiteException {
        return getAll(database, null);
    }

    public static List<Team> getTeams(Database database, boolean active,
                                      boolean only_favorite) throws CouchbaseLiteException {
        List<Object> key_filter = new ArrayList<>();

        Query query = database.getView("team-act.fav").createQuery();
        query.setStartKey(Arrays.asList(active, only_favorite));
        query.setEndKey(Arrays.asList(active, QUERY_END));
        QueryEnumerator filter = query.run();
        for (Iterator<QueryRow> it = filter; it.hasNext(); ) {
            QueryRow row = it.next();
            // key_filter.add(row.getDocumentId());
            String key_id = row.getDocumentId();
            key_filter.add(getFromId(database, key_id).getName());
        }
        return getAll(database, key_filter);
    }

    // Other methods
    public String getName() {
        return (String) content.get(NAME);
    }

    public void setName(String name) {
        content.put(NAME, name);
    }

    public List<Player> getMembers(Database database) {
        List<Player> members = new ArrayList<>();
        for (String member_id : (List<String>) content.get(MEMBERS)) {
            members.add(Player.getFromId(database, member_id));
        }
        return members;
    }

    public int getColor() {
        return (int) content.get(COLOR);
    }

    public void setColor(int color) {
        content.put(COLOR, color);
    }

    public boolean getIsActive() {
        return (boolean) content.get(IS_ACTIVE);
    }

    public void setIsActive(boolean is_active) {
        content.put(IS_ACTIVE, is_active);
    }

    public boolean getIsFavorite() {
        return (boolean) content.get(IS_FAVORITE);
    }

    public void setIsFavorite(boolean is_favorite) {
        content.put(IS_FAVORITE, is_favorite);
    }

    // =========================================================================
    // Additional methods
    // =========================================================================

    public int getSize() {
        return ((List<String>) content.get(MEMBERS)).size();
    }

    public boolean exists(Context context, Database database) {
        try {
            return exists(database, getName());
        } catch (CouchbaseLiteException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            loge("Existence check failed. ", e);
            return false;
        }
    }

    public static boolean exists(Database database, String name) throws CouchbaseLiteException {
        if (name == null) {
            return false;
        }

        Query query = database.getView("team-names").createQuery();
        query.setStartKey(name);
        query.setEndKey(name);
        QueryEnumerator result = query.run();

        assert (result.getCount() <= 1);
        return result.hasNext();
    }
}