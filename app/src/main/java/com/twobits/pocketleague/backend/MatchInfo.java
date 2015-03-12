package com.twobits.pocketleague.backend;

import com.twobits.pocketleague.db.tables.Team;

public class MatchInfo {
    private int id_in_session = -1;
    private String game_id;
    private Team team1;
    private Team team2;
    public String title = "";
    public String subtitle = "";
    private boolean creatable = false;
    private boolean viewable = false;

    MatchInfo(int id_in_session) {
        this.id_in_session = id_in_session;
    }

    MatchInfo(int id_in_session, String game_id, Team team1, Team team2) {
        this.id_in_session = id_in_session;
        this.game_id = game_id;
        this.team1 = team1;
        this.team2 = team2;
    }

    public int getIdInSession() {
        return id_in_session;
    }

    public void setIdInSession(int id_in_session) {
        this.id_in_session = id_in_session;
    }

    public String getGameId() {
        return game_id;
    }

    public void setGameId(String game_id) {
        this.game_id = game_id;
        if (game_id != "") {
            this.viewable = true;
        }
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
        if (team1 != null && team2 != null && game_id != null) {
            this.creatable = true;
        }
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
        if (team1 != null && team2 != null && game_id != null) {
            this.creatable = true;
        }
    }

    public boolean getCreatable() {
        return creatable;
    }

    public boolean getViewable() {
        return viewable;
    }

}
