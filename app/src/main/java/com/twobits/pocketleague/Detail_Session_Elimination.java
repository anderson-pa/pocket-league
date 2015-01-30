package com.twobits.pocketleague;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.twobits.pocketleague.backend.BracketHolder;
import com.twobits.pocketleague.backend.Detail_Session_Base;
import com.twobits.pocketleague.db.tables.SessionMember;
import com.twobits.pocketleague.enums.SessionType;

import java.sql.SQLException;

public class Detail_Session_Elimination extends Detail_Session_Base {
	static final String LOGTAG = "Detail_Session_DblElim";
	private BracketHolder bracketHolder = null;

	public void createSessionLayout(LayoutInflater inflater, ViewGroup container) {
		rootView = inflater.inflate(R.layout.activity_detail_session_singleelim, container, false);
		ScrollView sv = (ScrollView) rootView.findViewById(R.id.scrollView1);

//		DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
		rootView.getMeasuredWidth();

		boolean isDblElim = s.getSessionType() == SessionType.DBL_ELIM;
		bracketHolder = new BracketHolder(sv, s, isDblElim) {
			@Override
			public void onClick(View v) {
				mInfo = getMatchInfo(v.getId());
				log("gId: " + mInfo.getIdInSession() + ", create: "
						+ mInfo.getCreatable() + ", view: "
						+ mInfo.getViewable() + ", marquee: " + mInfo.title
						+ ", " + mInfo.subtitle);
				mActionMode = rootView.startActionMode(new ActionBarCallBack());
				v.setSelected(true);
			}
		};
	}

	@Override
	public void onPause() {
		super.onPause();

		// TODO: move this to bracket.java
		try {
			for (SessionMember sm : bracketHolder.sMembers) {
				smDao.update(sm);
			}
		} catch (SQLException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public void refreshDetails() {
		if (bracketHolder != null) {
			bracketHolder.refreshBrackets();
		}
	}
}
