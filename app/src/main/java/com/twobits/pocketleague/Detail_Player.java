package com.twobits.pocketleague;

import java.sql.SQLException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.twobits.pocketleague.backend.MenuContainerActivity;
import com.twobits.pocketleague.db.DatabaseCommonQueue;
import com.twobits.pocketleague.db.OrmLiteFragment;
import com.twobits.pocketleague.db.tables.Player;
import com.twobits.pocketleague.db.tables.Team;

public class Detail_Player extends OrmLiteFragment {
	private static final String LOGTAG = "Detail_Player";
    private View rootView;
    private Context context;

	Long pId;
	Player p;
	Team t;
	Dao<Player, Long> pDao;
	Dao<Team, Long> tDao;

	TextView tv_playerName;
	TextView tv_playerId;
	TextView tv_height;
	TextView tv_weight;
	TextView tv_handed;
	TextView tv_footed;
	CheckBox cb_isFavorite;
	Switch sw_isActive;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_detail_player, container, false);

        Bundle args = getArguments();
        pId = args.getLong("PID", -1);

		pDao = Player.getDao(context);
		tDao = Team.getDao(context);

		tv_playerName = (TextView) rootView.findViewById(R.id.pDet_name);
		tv_playerId = (TextView) rootView.findViewById(R.id.pDet_id);
		tv_height = (TextView) rootView.findViewById(R.id.pDet_height);
		tv_weight = (TextView) rootView.findViewById(R.id.pDet_weight);
		tv_handed = (TextView) rootView.findViewById(R.id.pDet_handed);
		tv_footed = (TextView) rootView.findViewById(R.id.pDet_footed);
		cb_isFavorite = (CheckBox) rootView.findViewById(R.id.pDet_isFavorite);
		cb_isFavorite.setOnClickListener(favoriteClicked);
		sw_isActive = (Switch) rootView.findViewById(R.id.pDet_isActive);
		sw_isActive.setOnClickListener(activeClicked);

        return rootView;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem fav = menu.add(R.string.menu_modify);
		fav.setIcon(R.drawable.ic_action_edit);
		fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		Intent intent = new Intent(context, NewPlayer.class);
		intent.putExtra("PID", pId);
		fav.setIntent(intent);

        mNav.setTitle(p.getNickName());
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshDetails();
	}

	public void refreshDetails() {
		if (pId != -1) {
			try {
				p = pDao.queryForId(pId);
				t = DatabaseCommonQueue.findPlayerSoloTeam(context, p);
			} catch (SQLException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}

		tv_playerName.setText(p.getNickName() + " (" + p.getFirstName() + ' '
				+ p.getLastName() + ")");
		tv_playerId.setText(String.valueOf(p.getId()));
		tv_height.setText("Height: " + String.valueOf(p.getHeight()) + " cm");
		tv_weight.setText("Weight: " + String.valueOf(p.getWeight()) + " kg");
		if (p.getIsLeftHanded()) {
			if (p.getIsRightHanded()) {
				tv_handed.setText("L + R");
			} else {
				tv_handed.setText("L");
			}
		} else {
			tv_handed.setText("R");
		}

		if (p.getIsLeftFooted()) {
			if (p.getIsRightFooted()) {
				tv_footed.setText("L + R");
			} else {
				tv_footed.setText("L");
			}
		} else {
			tv_footed.setText("R");
		}

		cb_isFavorite.setChecked(p.getIsFavorite());
		sw_isActive.setChecked(p.getIsActive());
	}

	private OnClickListener favoriteClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (pId != -1) {
				boolean is_favorite = ((CheckBox) v).isChecked();
				p.setIsFavorite(is_favorite);
				t.setIsFavorite(is_favorite);
				updatePlayer();
			}
		}
	};

	private OnClickListener activeClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (pId != -1) {
				boolean is_active = ((Switch) v).isChecked();
				p.setIsActive(is_active);
				t.setIsActive(is_active);
				updatePlayer();
			}
		}
	};

	private void updatePlayer() {
		try {
			pDao.update(p);
			tDao.update(t);
		} catch (SQLException e) {
			loge("Could not update player", e);
			e.printStackTrace();
		}
	}
}
