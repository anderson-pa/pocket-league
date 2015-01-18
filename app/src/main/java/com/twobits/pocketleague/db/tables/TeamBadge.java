package com.twobits.pocketleague.db.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class TeamBadge {

	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(foreign = true)
	private Team team;

	@DatabaseField(foreign = true)
	private Session session;

	@DatabaseField(canBeNull = false)
	private int badgeType;

	TeamBadge() {
	}

	public TeamBadge(Team team, Session session, int badgeType) {
		super();
		this.team = team;
		this.session = session;
		this.badgeType = badgeType;
	}

	public TeamBadge(Team team, int badgeType) {
		super();
		this.team = team;
		this.badgeType = badgeType;
	}

	// public static Dao<TeamBadge, Long> getDao(Context context)
	// throws SQLException {
	// DatabaseHelper helper = new DatabaseHelper(context);
	// Dao<TeamBadge, Long> d = helper.getTeamBadgeDao();
	// return d;
	// }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
