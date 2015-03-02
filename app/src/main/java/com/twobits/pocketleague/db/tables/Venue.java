package com.twobits.pocketleague.db.tables;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

public class Venue extends CouchDocumentBase {
    static final String TYPE = "venue";
	public static final String NAME = "venue_name";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ZIP_CODE = "zip_code";
	public static final String IS_ACTIVE = "is_active";
	public static final String IS_FAVORITE = "is_favorite";

	public Venue(String venue_name, boolean is_favorite) {
        // name should be unique
        content.put("type", TYPE);
        content.put(NAME, venue_name);
        content.put(IS_ACTIVE, true);
        content.put(IS_FAVORITE, is_favorite);
	}

    public Venue(Database database, String venue_id) {
        super(database, venue_id);
    }

	public String getName() {
		return (String) content.get(NAME);
	}

	public void setName(String venue_name) {
		content.put(NAME, venue_name);
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
}
