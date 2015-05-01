package info.andersonpa.pocketleague;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;

import info.andersonpa.pocketleague.backend.Fragment_TopList;
import info.andersonpa.pocketleague.backend.Item_Session;
import info.andersonpa.pocketleague.backend.ListAdapter_Session;
import info.andersonpa.pocketleague.db.tables.Session;
import info.andersonpa.pocketleague.enums.SessionType;

import java.util.ArrayList;
import java.util.List;

public class List_Sessions extends Fragment_TopList {
    ListView lv;
    private ListAdapter_Session session_adapter;
    private List<Item_Session> session_list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setAddClicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNav.modifySession(null);
            }
        });

        mNav.setTitle("Sessions", "for " + mData.getCurrentGameType().toString());
        mNav.setDrawerItemChecked(0);
        rootView = inflater.inflate(R.layout.activity_view_listing, container, false);

        lv = (ListView) rootView.findViewById(R.id.dbListing);
        session_adapter = new ListAdapter_Session(context, R.layout.list_item_session,
                session_list, cbClicked);
        lv.setAdapter(session_adapter);
        lv.setOnItemClickListener(lvItemClicked);

        setupBarButtons(getString(R.string.open), getString(R.string.closed));

        return rootView;
    }

    @Override
    public void refreshDetails() {
        session_adapter.clear();
        List<Session> sessions = getSessions();

        for (Session s : sessions) {
            session_adapter.add(new Item_Session(s.getId(), s.getName(), s.getSessionType(),
                    s.getIsFavorite()));
        }
    }

    private AdapterView.OnItemClickListener lvItemClicked = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String sId = session_list.get(position).getId();
            SessionType session_type = session_list.get(position).getSessionType();
            mNav.viewSessionDetails(sId, session_type);
        }
    };

    private View.OnClickListener cbClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String sId = (String) view.getTag();

            Session s = Session.getFromId(database(), sId);
            s.setIsFavorite(((CheckBox) view).isChecked());
            s.update();
        }
    };

    private List<Session> getSessions() {
        List<Session> sessions = new ArrayList<>();
        try {
            sessions = Session.getSessions(database(), mData.getCurrentGameType(), show_actives, show_favorites);
        } catch (CouchbaseLiteException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            loge("Retrieval of sessions failed. ", e);
        }
        return sessions;
    }
}