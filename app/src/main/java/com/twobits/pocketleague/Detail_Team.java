package com.twobits.pocketleague;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.twobits.pocketleague.backend.MenuContainerActivity;
import com.twobits.pocketleague.db.tables.Player;
import com.twobits.pocketleague.db.tables.Team;
import com.twobits.pocketleague.db.tables.TeamMember;

import java.sql.SQLException;
import java.util.List;

public class Detail_Team extends MenuContainerActivity {
	private static final String LOGTAG = "Detail_Team";
	Long tId;
	Team t;
	Dao<Team, Long> tDao;
	Dao<TeamMember, Long> tmDao;
	Dao<Player, Long> pDao;

	TextView tv_teamName;
	TextView tv_teamId;
	TextView tv_members;
	CheckBox cb_isFavorite;
	Switch sw_isActive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_team);

		Intent intent = getIntent();
		tId = intent.getLongExtra("TID", -1);

		tDao = Team.getDao(this);
		pDao = Player.getDao(this);
		tmDao = TeamMember.getDao(this);

		tv_teamName = (TextView) findViewById(R.id.tDet_name);
		tv_teamId = (TextView) findViewById(R.id.tDet_id);
		tv_members = (TextView) findViewById(R.id.tDet_members);
		cb_isFavorite = (CheckBox) findViewById(R.id.tDet_isFavorite);
		cb_isFavorite.setOnClickListener(favoriteClicked);
		sw_isActive = (Switch) findViewById(R.id.tDet_isActive);
		sw_isActive.setOnClickListener(activeClicked);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem fav = menu.add(R.string.menu_modify);
		fav.setIcon(R.drawable.ic_action_edit);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		Intent intent = new Intent(this, NewTeam.class);
		intent.putExtra("TID", tId);

		fav.setIntent(intent);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		refreshDetails();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDetails();
	}

	public void refreshDetails() {
		String memberNicks = "";
		if (tId != -1) {
			try {
				t = tDao.queryForId(tId);
				List<TeamMember> memberList = tmDao.queryBuilder().where()
						.eq(TeamMember.TEAM, tId).query();

				for (TeamMember tm : memberList) {
					pDao.refresh(tm.getPlayer());
					memberNicks = memberNicks.concat(tm.getPlayer()
							.getNickName() + ", ");
				}
				if (memberNicks.length() == 0) {
					memberNicks = "Anonymous team (no members).";
				} else {
					memberNicks = memberNicks.substring(0,
							memberNicks.length() - 2) + ".";
				}
			} catch (SQLException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}

		tv_teamName.setText(t.getTeamName());
		tv_teamId.setText(String.valueOf(t.getId()));
		tv_members.setText(memberNicks);
		cb_isFavorite.setChecked(t.getIsFavorite());
		sw_isActive.setChecked(t.getIsActive());
	}

	private OnClickListener favoriteClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (tId != -1) {
				t.setIsFavorite(((CheckBox) v).isChecked());
				updateTeam();
			}
		}
	};

	private OnClickListener activeClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (tId != -1) {
				t.setIsActive(((Switch) v).isChecked());
				updateTeam();
			}
		}
	};

	private void updateTeam() {
		try {
			tDao.update(t);
		} catch (SQLException e) {
			loge("Could not update team", e);
			e.printStackTrace();
		}
	}
}