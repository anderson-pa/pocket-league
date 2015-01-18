package com.twobits.pocketleague;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.twobits.pocketleague.backend.MenuContainerActivity;
import com.twobits.pocketleague.db.tables.Player;
import com.twobits.pocketleague.db.tables.Session;
import com.twobits.pocketleague.db.tables.Venue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class NewGame extends MenuContainerActivity {
	Spinner spinner_p1;
	Spinner spinner_p2;
	Spinner spinner_session;
	Spinner spinner_venue;
	Spinner spinner_ruleSet;
	ListView lv_players;
	Intent intent;

	int p1_pos = 0;
	int p2_pos = 1;
	int session_pos = 0;
	int venue_pos = 1;
	int ruleSet_pos = 0;

	long p1Id;
	long p2Id;

	List<Player> players = new ArrayList<Player>();
	List<Session> sessions = new ArrayList<Session>();
	List<Venue> venues = new ArrayList<Venue>();

	List<String> player_names = new ArrayList<String>();
	List<String> session_names = new ArrayList<String>();
	List<String> venue_names = new ArrayList<String>();
	List<String> ruleset_descriptions = new ArrayList<String>();
	List<Integer> ruleset_ids = new ArrayList<Integer>();

	Dao<Player, Long> pDao;
	Dao<Session, Long> sDao;
	Dao<Venue, Long> vDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game);
		intent = getIntent();
		spinner_p1 = (Spinner) findViewById(R.id.spinner_player1);
		spinner_p2 = (Spinner) findViewById(R.id.spinner_player2);
		spinner_session = (Spinner) findViewById(R.id.spinner_session);
		spinner_venue = (Spinner) findViewById(R.id.spinner_venue);
		spinner_ruleSet = (Spinner) findViewById(R.id.spinner_ruleSet);

		refreshSpinners();

		spinner_p1.setOnItemSelectedListener(mPlayerOneSelectedHandler);
		spinner_p2.setOnItemSelectedListener(mPlayerTwoSelectedHandler);
		spinner_session.setOnItemSelectedListener(mSessionSelectedHandler);
		spinner_venue.setOnItemSelectedListener(mVenueSelectedHandler);
		spinner_ruleSet.setOnItemSelectedListener(mRuleSetSelectedHandler);

		spinner_p2.setSelection(1);
		if (intent.hasExtra("p1") && intent.hasExtra("p2")) {
			if (p1Id > p2Id) {
				swapPlayers();
			}
		}
	}

	private OnItemSelectedListener mPlayerOneSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			p1_pos = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}

	};
	private OnItemSelectedListener mPlayerTwoSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			p2_pos = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}

	};
	private OnItemSelectedListener mSessionSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			session_pos = position;
			// if (sessions.get(position).getRuleSet() == -1) {
			// spinner_ruleSet.setEnabled(true);
			// } else {
			// spinner_ruleSet.setEnabled(false);
			// int selectedId = sessions.get(position).getRuleSet();
			// spinner_ruleSet.setSelection(ruleset_ids.indexOf(selectedId));
			// ruleSet_pos = ruleset_ids.indexOf(selectedId);
			// }
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	private OnItemSelectedListener mVenueSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			venue_pos = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	private OnItemSelectedListener mRuleSetSelectedHandler = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			ruleSet_pos = position;
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	public void refreshSpinners() {
		Context context = getApplicationContext();
		try {
			pDao = Player.getDao(context);
			sDao = Session.getDao(context);
			vDao = Venue.getDao(context);

			if (intent.hasExtra("p1") && intent.hasExtra("p2")) {
				spinner_p1.setEnabled(false);
				spinner_p2.setEnabled(false);
				p1Id = intent.getLongExtra("p1", -1);
				p2Id = intent.getLongExtra("p2", -1);
				players = pDao.queryBuilder().where().idEq(p1Id).or()
						.idEq(p2Id).query();
			} else {
				players = pDao.queryBuilder().where()
						.eq(Player.IS_ACTIVE, true).query();
			}

			if (intent.hasExtra("sId")) {
				spinner_session.setEnabled(false);
				long sId = intent.getLongExtra("sId", -1);
				sessions = sDao.queryBuilder().where().idEq(sId).query();
			} else {
				sessions = sDao.queryBuilder().where()
						.eq(Session.IS_ACTIVE, true).query();
			}

			venues = vDao.queryBuilder().where().eq(Venue.IS_ACTIVE, true)
					.query();
		} catch (SQLException e) {
			Log.e(PocketLeague.class.getName(), "Could not get objects", e);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		player_names.clear();
		session_names.clear();
		venue_names.clear();
		ruleset_descriptions.clear();
		ruleset_ids.clear();

		for (Player p : players) {
			player_names.add(p.getFirstName() + " " + p.getLastName());
		}
		for (Session s : sessions) {
			session_names.add(String.valueOf(s.getId()) + " "
					+ s.getSessionName());
		}
		for (Venue v : venues) {
			venue_names.add(String.valueOf(v.getId()) + " " + v.getName());
		}
		// for (RuleSet rs : RuleType.map.values()) {
		// ruleSetDescriptions.add(rs.getDescription());
		// ruleSetIds.add(rs.getId());
		// }

		ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, player_names);
		pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, session_names);
		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<String> vAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, venue_names);
		vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<String> rsAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item,
				ruleset_descriptions);
		rsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner_p1.setAdapter(pAdapter);
		spinner_p2.setAdapter(pAdapter);

		spinner_session.setAdapter(sAdapter);
		spinner_venue.setAdapter(vAdapter);
		spinner_ruleSet.setAdapter(rsAdapter);
	}

	public void swapPlayers(View vw) {
		swapPlayers();
	}

	public void swapPlayers() {
		int p1 = spinner_p1.getSelectedItemPosition();
		spinner_p1.setSelection(spinner_p2.getSelectedItemPosition());
		spinner_p2.setSelection(p1);
	}

	public void createGame(View view) {
		Player p1 = players.get(p1_pos);
		Player p2 = players.get(p2_pos);

		int ruleSetId = ruleset_ids.get(ruleSet_pos);

		// Game g = new Game(sessions.get(session_pos), venues.get(venue_pos),
		// ruleSetId, true);
		// long gid;
		// g.setDatePlayed(new Date());

		// try {
		// Dao<Game, Long> d = Game.getDao(getApplicationContext());
		// d.createIfNotExists(g);
		// gid = g.getId();
		// // Intent intent = new Intent(this, GameInProgress.class);
		// // intent.putExtra("GID", gid);
		// // startActivity(intent);
		// // finish();
		//
		// } catch (SQLException e) {
		// Toast.makeText(getApplicationContext(), e.getMessage(),
		// Toast.LENGTH_LONG).show();
		// }
	}
}
