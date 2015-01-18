package com.twobits.pocketleague.db.tables;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.twobits.pocketleague.db.DatabaseHelper;

@DatabaseTable
public class Team {
	public static final String NAME = "name";
	public static final String TEAM_SIZE = "team_size";
	public static final String COLOR = "color";
	public static final String IS_ACTIVE = "is_active";
	public static final String IS_FAVORITE = "is_favorite";

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(canBeNull = false, uniqueCombo = true)
	private String name;

	@DatabaseField(uniqueCombo = true)
	private int team_size;

	@DatabaseField
	private int color;

	@DatabaseField
	private boolean is_active = true;

	@DatabaseField
	private boolean is_favorite = false;

	@ForeignCollectionField
	ForeignCollection<TeamMember> team_members;

	Team() {
	}

	public Team(String team_name, int team_size, int color, boolean is_favorite) {
		super();
		this.name = team_name;
		this.team_size = team_size;
		this.color = color;
		this.is_favorite = is_favorite;
	}

	public static Dao<Team, Long> getDao(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context);
		Dao<Team, Long> d = null;
		try {
			d = helper.getTeamDao();
		} catch (SQLException e) {
			throw new RuntimeException("Could not get team dao: ", e);
		}
		return d;
	}

	public long getId() {
		return id;
	}

	public String getTeamName() {
		return name;
	}

	public void setTeamName(String team_name) {
		this.name = team_name;
	}

	public int getTeamSize() {
		return team_size;
	}

	public void setTeamSize(int team_size) {
		this.team_size = team_size;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean getIsActive() {
		return is_active;
	}

	public void setIsActive(boolean is_active) {
		this.is_active = is_active;
	}

	public boolean getIsFavorite() {
		return is_favorite;
	}

	public void setIsFavorite(boolean is_favorite) {
		this.is_favorite = is_favorite;
	}

	// =========================================================================
	// Additional methods
	// =========================================================================

	public boolean exists(Context context) throws SQLException {
		return exists(name, context);
	}

	public static boolean exists(String name, Context context)
			throws SQLException {
		if (name == null) {
			return false;
		}

		try {
			List<Team> tList = getDao(context).queryBuilder().where()
					.eq(Team.NAME, name).query();
			if (tList.isEmpty()) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
	}
}