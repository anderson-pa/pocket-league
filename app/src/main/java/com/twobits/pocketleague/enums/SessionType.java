package com.twobits.pocketleague.enums;

import com.twobits.pocketleague.Detail_Session_Elimination;
import com.twobits.pocketleague.Detail_Session_Ladder;
import com.twobits.pocketleague.Detail_Session_League;
import com.twobits.pocketleague.backend.Detail_Session_Base;

/** Enum for the different session types */
public enum SessionType {
	OPEN("Open", Detail_Session_Base.class),
	LEAGUE("League", Detail_Session_League.class),
	LADDER("Ladder", Detail_Session_Ladder.class),
	GROUP_STAGE("Group Stage", Detail_Session_Base.class),
	SNGL_ELIM("Single-elimination Tournament", Detail_Session_Elimination.class),
	DBL_ELIM("Double-elimination Tournament", Detail_Session_Elimination.class);

	private String sessiontype_label;
	private Class<?> sessiontype_activity;

	private SessionType(String label, Class<?> detail_activity) {
		sessiontype_label = label;
		sessiontype_activity = detail_activity;
	}

	@Override
	public String toString() {
		return sessiontype_label;
	}

	public Class<?> toClass() {
		return sessiontype_activity;
	}
}