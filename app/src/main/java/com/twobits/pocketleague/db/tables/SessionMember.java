package com.twobits.pocketleague.db.tables;

import android.util.ArrayMap;

import java.util.Map;

public class SessionMember implements Comparable<SessionMember> {
    public static final String TEAM_ID = "team_id";
	public static final String TEAM_SEED = "team_seed";
	public static final String TEAM_RANK = "team_rank";

	private Team team;
	private int team_seed;
	private int team_rank;

    public SessionMember(Team team, int team_seed, int team_rank) {
        this.team = team;
        this.team_seed = team_seed;
        this.team_rank = team_rank;
    }

    public SessionMember(Team team, int team_seed) {
        this(team, team_seed, 0);
    }

    public SessionMember(int team_seed, int team_rank) {
        this(null, team_seed, team_rank);
    }

	public Team getTeam() {
		return team;
	}

	public int getSeed() {
		return team_seed;
	}

	public int getRank() {
		return team_rank;
	}

	public void setRank(int team_rank) {
		this.team_rank = team_rank;
	}

    public void swapRank(SessionMember that_sm) {
        swapRank(this, that_sm);
    }

    public static void swapRank(SessionMember sm1, SessionMember sm2) {
        int sm1_rank = sm1.team_rank;
        sm1.team_rank = sm2.team_rank;
        sm2.team_rank = sm1_rank;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> contents = new ArrayMap<>();
        contents.put(TEAM_ID, team.getId());
        contents.put(TEAM_SEED, team_seed);
        contents.put(TEAM_RANK, team_rank);
        return contents;
    }

	public int compareTo(SessionMember another) {
        if (team_rank < another.team_rank) {
            return -1;
        } else if (team_rank == another.team_rank) {
            return 0;
        } else {
            return 1;
        }
    }
}
