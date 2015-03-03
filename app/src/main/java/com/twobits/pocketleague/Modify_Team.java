package com.twobits.pocketleague;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.twobits.pocketleague.backend.Fragment_Edit;
import com.twobits.pocketleague.db.tables.Player;
import com.twobits.pocketleague.db.tables.Team;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Modify_Team extends Fragment_Edit {
	String tId;
	Team t;

	Button btn_create;
	TextView tv_name;
	TextView tv_num_selected;
	ListView lv_roster;
	CheckBox cb_isFavorite;

	List<Player> players = new ArrayList<>();
	List<Integer> playerIdxList = new ArrayList<>();
	List<String> playerNames = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_modify_team, container, false);

        Bundle args = getArguments();
        tId = args.getString("TID");

		btn_create = (Button) rootView.findViewById(R.id.button_createTeam);
		tv_name = (TextView) rootView.findViewById(R.id.editText_teamName);
		tv_num_selected = (TextView) rootView.findViewById(R.id.tv_num_selected);
		lv_roster = (ListView) rootView.findViewById(R.id.newTeam_playerSelection);
		cb_isFavorite = (CheckBox) rootView.findViewById(R.id.newTeam_isFavorite);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneButtonPushed();
            }
        });

		try {
            players.clear();
            playerNames.clear();
            QueryEnumerator result = Player.getAll(database, true, false);
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                players.add(Player.getFromId(database, row.getDocumentId()));
                playerNames.add(Player.getFromId(database, row.getDocumentId()).getName());
            }
		} catch (CouchbaseLiteException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		updateRosterCheckList();
		lv_roster.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView arg0, View view, int pos,
					long arg3) {
				if (playerIdxList.contains(pos)) {
					playerIdxList.remove((Integer) pos);
				} else {
					playerIdxList.add(pos);
				}
				tv_num_selected.setText(playerIdxList.size() + " selected");
			}
		});

		if (tId != null) {
			loadTeamValues();
		}

        return rootView;
	}

	private void loadTeamValues() {
        t = Team.getFromId(database, tId);
        btn_create.setText("Modify");
        tv_name.setText(t.getName());
        lv_roster.setVisibility(View.GONE);
        cb_isFavorite.setChecked(t.getIsFavorite());

        // TODO: if loading a team, show players but dont allow team size to change
        playerNames.clear();
	}

	public void updateRosterCheckList() {
		lv_roster.setAdapter(new ArrayAdapter<>(context, android.R.layout
                .simple_list_item_multiple_choice, playerNames));
	}

	public void doneButtonPushed() {
		String team_name = tv_name.getText().toString().trim();
		if (team_name.isEmpty()) {
			Toast.makeText(context, "Team name is required.", Toast.LENGTH_LONG).show();
		} else {
			Boolean is_favorite = cb_isFavorite.isChecked();
			int team_color = getResources().getColor(R.color.Aqua);

			if (tId != null) {
				modifyTeam(team_name, team_color, true, is_favorite);
			} else {
				createTeam(team_name, team_color, is_favorite);
			}
		}
	}

	private void createTeam(String team_name, int team_color, boolean is_favorite) {
        if (playerIdxList.size() == 1) {
            Toast.makeText(context, "Cannot create a team with one player.", Toast.LENGTH_SHORT).show();
//        } else if (newTeam.exists(database)) {
//            Toast.makeText(context, "Team already exists.", Toast.LENGTH_SHORT).show();
        } else {
            List<String> player_ids = new ArrayList<>();
            for (Integer playerIdx : playerIdxList) {
                player_ids.add(players.get(playerIdx).getId());
            }
            Team newTeam = new Team(database, team_name, player_ids, team_color, is_favorite);
            Toast.makeText(context, "Team created!", Toast.LENGTH_SHORT).show();
            mNav.onBackPressed();
        }
	}

	private void modifyTeam(String team_name, int team_color, boolean is_active,
                            boolean is_favorite) {
		t.setName(team_name);
		t.setColor(team_color);
		t.setIsActive(is_active);
		t.setIsFavorite(is_favorite);

        t.update();
        Toast.makeText(context, "Team modified.", Toast.LENGTH_SHORT).show();
        mNav.onBackPressed();
	}
}
