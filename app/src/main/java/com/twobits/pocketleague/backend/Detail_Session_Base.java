package com.twobits.pocketleague.backend;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.twobits.pocketleague.R;
import com.twobits.pocketleague.db.tables.Game;
import com.twobits.pocketleague.db.tables.GameMember;
import com.twobits.pocketleague.db.tables.Session;

import java.sql.SQLException;

public abstract class Detail_Session_Base extends Fragment_Detail {
	public String sId;
	public Session s;

	public MatchInfo mInfo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setModifyClicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNav.modifySession(sId);
            }
        });

        setFavoriteClicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sId != null) {
                    boolean is_favorite = ((ToggleButton) v).isChecked();
                    s.setIsFavorite(is_favorite);
                    s.update();
                }
            }
        });

        Bundle args = getArguments();
		sId = args.getString("SID");

		if (sId != null) {
            s = Session.getFromId(database, sId);
        }

        createSessionLayout(inflater, container);
        setupBarButtons();
        bar_isActive.setVisibility(View.GONE);

        return rootView;
	}

	public abstract void createSessionLayout(LayoutInflater inflater, ViewGroup container);

	@Override
	public void onResume() {
		super.onResume();
		refreshBaseDetails();
	}

	public void refreshBaseDetails() {
        mNav.setTitle(s.getName(), s.getSessionType().toString());

		TextView sName = (TextView) rootView.findViewById(R.id.sDet_name);
		TextView sId = (TextView) rootView.findViewById(R.id.sDet_id);
		TextView sessionGameType = (TextView) rootView.findViewById(R.id.sDet_gameType);

		sName.setText(s.getName());
		sId.setText(String.valueOf(s.getId()));
		sessionGameType.setText(s.getDescriptor().getName());
        bar_isFavorite.setChecked(s.getIsFavorite());
	}

	public class ActionBarCallBack implements ActionMode.Callback {
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);

			mode.setTitle(mInfo.title);
			mode.setSubtitle(mInfo.subtitle);

			MenuItem mItm = menu.findItem(R.id.action_match);
			if (mInfo.getCreatable()) {
				mItm.setTitle("Create");
			} else if (mInfo.getViewable()) {
				mItm.setTitle("Load");
			} else {
				mItm.setVisible(false);
			}
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_match:
				if (mInfo.getCreatable()) {
					createMatch();
				} else if (mInfo.getViewable()) {
					loadMatch(mInfo.getGameId());
				}
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	}

	private void createMatch() {
//		GameMember t1 = new GameMember(g, mInfo.getTeam1());
//        GameMember t2 = new GameMember(g, mInfo.getTeam2());
//        Game g = new Game(database, s, mInfo.getIdInSession(), s.getCurrentVenue(database), false);
//
//		try {
//			gDao.setObjectCache(true);
//			gDao.create(g);
//			gmDao.create(t1);
//			gmDao.create(t2);
//		} catch (SQLException e) {
//			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
//		}
//
//		mNav.loadGame(g.getId());

		// load a game that is in progress
		// Intent intent = new Intent(v.getContext(), GameInProgress.class);
		// intent.putExtra("GID", mInfo.gameId);
		// startActivity(intent);
		// finish();

		// load a game that is finished
		// Intent intent = new Intent(v.getContext(), Detail_Game.class);
		// intent.putExtra("GID", mInfo.gameId);
		// startActivity(intent);
	}

	private void loadMatch(String game_id) {
		mNav.loadGame(game_id);
	}
}
