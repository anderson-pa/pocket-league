package com.twobits.pocketleague;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.twobits.pocketleague.backend.Item_Player;
import com.twobits.pocketleague.backend.ListAdapter_Player;
import com.twobits.pocketleague.db.OrmLiteFragment;
import com.twobits.pocketleague.db.tables.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class View_Players extends OrmLiteFragment {
    public static final String LOGTAG = "View_Players";

    ListView lv;
    private ListAdapter_Player player_adapter;
    private List<Item_Player> player_list = new ArrayList<>();
    private Dao<Player, Long> pDao = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_view_listing, container, false);

        lv = (ListView) rootView.findViewById(R.id.dbListing);
        player_adapter = new ListAdapter_Player(context, R.layout.list_item_player, player_list,
                cbClicked);
        lv.setAdapter(player_adapter);
        lv.setOnItemClickListener(lvItemClicked);

        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem fav = menu.add("New Player");
        fav.setIcon(R.drawable.ic_menu_add);
        fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        fav.setIntent(new Intent(context, NewPlayer.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshListing();
    }

    protected void refreshListing() {
        player_adapter.clear();

        try {
            pDao = getHelper().getPlayerDao();
            for (Player p : pDao) {
                player_adapter.add(new Item_Player(p.getId(), p.getFullName(), p.getNickName(),
                        p.getColor(), p.getIsFavorite()));
            }
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            loge("Retrieval of players failed. ", e);
        }

        //		player_adapter.notifyDataSetChanged(); // required in case the list has changed
    }

    private AdapterView.OnItemClickListener lvItemClicked = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Long pId = Long.valueOf(player_list.get(position).getId());
            mNav.viewPlayerDetails(pId);
        }
    };

    private View.OnClickListener cbClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            long pId = (long) view.getTag();
            try {
                log(String.valueOf(pId));
                Player p = pDao.queryForId(pId);
                p.setIsFavorite(((CheckBox) view).isChecked());
                pDao.update(p);
            } catch (SQLException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                loge("Retrieval of session failed", e);
            }
        }
    };
}
