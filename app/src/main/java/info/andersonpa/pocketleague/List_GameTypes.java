package info.andersonpa.pocketleague;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import info.andersonpa.pocketleague.backend.Fragment_TopList;
import info.andersonpa.pocketleague.backend.Item_GameType;
import info.andersonpa.pocketleague.backend.ListAdapter_GameType;
import info.andersonpa.pocketleague.gameslibrary.GameSubtype;
import info.andersonpa.pocketleague.gameslibrary.GameType;

import java.util.ArrayList;
import java.util.List;


public class List_GameTypes extends Fragment_TopList {
    private GridView gv;
	private ListAdapter_GameType gametype_adapter;
	private List<Item_GameType> gametypes_list = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        mNav.setTitle("Select Game");
        mNav.setDrawerItemChecked(2);
		rootView = inflater.inflate(R.layout.fragment_view_gametypes, container, false);

		gv = (GridView) rootView.findViewById(R.id.gametypes_view);
		gametype_adapter = new ListAdapter_GameType(context, R.layout.grid_item, gametypes_list);
		gv.setAdapter(gametype_adapter);
		gv.setOnItemClickListener(gvItemClicked);

		return rootView;
	}

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

	}

	public void refreshDetails() {
		gametypes_list.clear();

		for (GameType gt : GameType.values()) {
			gametypes_list.add(new Item_GameType(gt));
		}
	}

	private OnItemClickListener gvItemClicked = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			GameType gt = (GameType) view.getTag();
            List<GameSubtype> gst = gt.toGameSubtype();
            if (gst.size() > 1) {
                mNav.viewGameSubtypes(gt.name());
            } else {
                mData.setCurrentGameSubtype(gst.get(0));
                mNav.viewSessions();
            }
		}
	};
}