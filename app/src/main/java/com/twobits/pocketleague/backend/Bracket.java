package com.twobits.pocketleague.backend;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twobits.pocketleague.BuildConfig;
import com.twobits.pocketleague.R;
import com.twobits.pocketleague.db.tables.Game;
import com.twobits.pocketleague.db.tables.GameMember;
import com.twobits.pocketleague.db.tables.SessionMember;
import com.twobits.pocketleague.enums.BrDrawable;
import com.twobits.pocketleague.enums.BrNodeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Bracket {
    public static String LOGTAG = "Bracket";
    private RelativeLayout rl;
    private Context context;
    public String label_text = "";
    private int ceiling_view_id = 0;
    private int header_offset = 0;
    private int match_offset = 0;
    private int n_leafs;
    private Map<Integer, Item_Match> matches = new HashMap<>();

    public Bracket(List<SessionMember> sMembers, RelativeLayout rl) {
        this.rl = rl;
        context = rl.getContext();
        this.n_leafs = sMembers.size();
        assert (n_leafs & (n_leafs - 1)) == 0;

        seed(sMembers);
    }

    /**
     * Use this constructor for a respawn bracket. ie, members will come from
     * another bracket after losing or reaching the top tier. The insane loops
     * generate a tiered losers bracket.
     */
    public Bracket(int n_leafs, boolean is_final, RelativeLayout rl) {
        this.rl = rl;
        context = rl.getContext();
//        if (is_final) {
//            this.n_leafs = 4;
//        } else {
            this.n_leafs = (int) Math.pow(2, 2 * factorTwos(n_leafs) - 1);
//        }
        assert (n_leafs & (n_leafs - 1)) == 0;

//        if (is_final) {
//            seed(n_leafs);
//        } else {
            seed();
//        }
    }

    public int getCeilingViewId() {
        return ceiling_view_id;
    }

    public void setCeilingViewId(int ceiling_view_id) {
        this.ceiling_view_id = ceiling_view_id;
    }

    public int getHeaderOffset() {
        return header_offset;
    }

    public void setHeaderOffset(int header_offset) {
        this.header_offset = header_offset;
    }

    public int getFirstColumnId() {
        // Reserve a view for bracket label and another for horizontal rule.
        return getHeaderOffset() + 3;
    }

    public int getLastHeaderId() {
        return getFirstColumnId() + factorTwos(n_leafs);
    }

    public int getMatchOffset() {
        return match_offset;
    }

    public void setMatchOffset(int match_offset) {
        this.match_offset = match_offset;
    }

    public int getLastMatchId() {
        return n_leafs - 1 + getMatchOffset();
    }

    public Item_Match getMatch(int viewId) {
        int match_id = viewId % BrNodeType.MOD - match_offset;
        if (matches.containsKey(match_id)) {
            return matches.get(match_id);
        } else {
            return new Item_Match(0);
        }
    }

    public void buildBracket(OnClickListener mListener) {
        buildBracket(100, -1, mListener);
    }

    public void buildBracket(int tierWidth, int columnViewId, OnClickListener mListener) {
        TextView tv;
        makeHeaders(300, tierWidth, columnViewId);

        for (Item_Match match : matches.values()) {
            makeMatchViews(match);
            match.setOnClickListener(mListener);
            addViewsToLayout(match);
        }
    }

    private void makeHeaders(int baseWidth, int tierWidth, int columnViewId) {
        // invisible headers are for spacing the bracket.
        TextView tv;
        RelativeLayout.LayoutParams lp;
        int vwHeight = 30;

        // bracket label view
        tv = new TextView(context);
        tv.setText(label_text);
        tv.setTextAppearance(context, android.R.style.TextAppearance_Large);
        tv.setId(getHeaderOffset() + 1);
        tv.setPadding(0, 40, 0, 0);
        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);

        if (getCeilingViewId() > 0) {
            lp.addRule(RelativeLayout.BELOW, getCeilingViewId());
        }
        if (label_text.equals("")) {
            tv.setHeight(0);
        }
        rl.addView(tv, lp);

        // horizontal rule
        tv = new TextView(context);
        tv.setHeight(2);
        tv.setPadding(10, 10, 10, 10);
        tv.setBackgroundColor(context.getResources().getColor(R.color.primary));
        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
        lp.addRule(RelativeLayout.BELOW, getHeaderOffset() + 1);
        rl.addView(tv, lp);

        // header for the labeled brackets on tier 0
        tv = new TextView(context);
        tv.setWidth(baseWidth);
        tv.setHeight(vwHeight);
        tv.setId(getFirstColumnId());
        tv.setText(String.valueOf(getFirstColumnId()));
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.BLACK);
        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (columnViewId > 0) {
            lp.addRule(RelativeLayout.ALIGN_RIGHT, columnViewId);
        } else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
        }

        lp.addRule(RelativeLayout.BELOW, getHeaderOffset() + 1);
        lp.setMargins(0, 10, 0, 0);
        rl.addView(tv, lp);

        // headers for the remaining tiers
        Integer nTiers = factorTwos(n_leafs);

        // tier width = (screen width - label width - arbitrary side spacing) / number of tiers
        // tierWidth = (svWidth - 350 - 100) / nTiers;

        int[] vwColor = {Color.RED, Color.BLUE, Color.GREEN};
        for (Integer i = 0; i < nTiers; i++) {
            tv = new TextView(context);
            lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

//            if (first_header_id != 0 && i == nTiers - 1) {
//                lp.addRule(RelativeLayout.ALIGN_RIGHT, first_header_id);
//            } else {
                tv.setWidth(tierWidth);
//            }
            tv.setHeight(vwHeight);
            tv.setId(getFirstColumnId() + i + 1);
            tv.setText(String.valueOf(getFirstColumnId() + i + 1));
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(vwColor[i % 3]);

            lp.addRule(RelativeLayout.ALIGN_BASELINE, getFirstColumnId());
            lp.addRule(RelativeLayout.RIGHT_OF, getFirstColumnId() + i);
            lp.setMargins(-14, 0, 0, 0);
            rl.addView(tv, lp);
        }
    }

    public void makeMatchViews(Item_Match match) {
        int match_id = match.getIdInSession();
        BrNodeType smType;

        TextView tv;
        String drwStr;
        int drwColor = Color.LTGRAY;

        drwStr = "upper";
        tv = new TextView(context);
        tv.setId(match_id + match_offset + BrNodeType.UPPER);
        smType = match.getUpperNodeType();
        if (smType == BrNodeType.TIP) {
            tv.setText(match.getUpperSeedName());
            drwStr += "_labeled";
            drwColor = match.getUpperColor();
        } else if (smType == BrNodeType.RESPAWN) {
            tv.setText("(" + (char) (match.getUpperMember().getSeed() + 65) + ") ");
            drwStr += "_labeled";
        }
        if (match.getLowerNodeType() == BrNodeType.NA) {
            drwStr = "endpoint";
        }
        tv.setBackgroundResource(BrDrawable.map.get(drwStr));
        tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);
        tv.setGravity(Gravity.END);
        tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        match.setUpperView(tv);


        drwStr = "lower";
        drwColor = Color.LTGRAY;
        tv = new TextView(context);
        tv.setId(match_id + match_offset + BrNodeType.LOWER);
        smType = match.getLowerNodeType();
        if (smType == BrNodeType.TIP) {
            tv.setText(match.getLowerSeedName());
            drwStr += "_labeled";
            drwColor = match.getLowerColor();
        } else if (smType == BrNodeType.RESPAWN) {
            tv.setText("(" + (char) (match.getLowerMember().getSeed() + 65) + ") ");
            drwStr += "_labeled";
        }
        tv.setBackgroundResource(BrDrawable.map.get(drwStr));
        tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);
        tv.setGravity(Gravity.END);
        tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        match.setLowerView(tv);
    }

    public void addViewsToLayout(Item_Match match) {
        RelativeLayout.LayoutParams lp;
        Integer tier = getTier(match);

        // add upper view
        TextView tv = (TextView) match.getUpperView();
        if (tier == getHighestTier()) {
            lp = getFinalLayoutParams(tier, tv.getId(), match.getUpperIsLabelled());
        } else {
            lp = getUpperLayoutParams(tier, tv.getId(), match.getUpperIsLabelled());
        }
        rl.addView(tv, lp);

        // add lower view
        tv = (TextView) match.getLowerView();
        if (tier != getHighestTier()) {
            lp = getLowerLayoutParams(tier, tv.getId(), match.getLowerIsLabelled());
            rl.addView(tv, lp);
        }
    }

    private RelativeLayout.LayoutParams getBaseLayoutParams(int tier, int view_id) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_RIGHT, getFirstColumnId() + tier);
        lp.addRule(RelativeLayout.BELOW, findViewAboveId(view_id));
        return lp;
    }

    private RelativeLayout.LayoutParams getFinalLayoutParams(int tier, int view_id, boolean is_labelled
    ) {
        RelativeLayout.LayoutParams lp = getBaseLayoutParams(tier, view_id);
        if (!is_labelled) {
            lp.addRule(RelativeLayout.ALIGN_LEFT, tier + getFirstColumnId());
            lp.setMargins(0, -25, 0, 0);
        }
        return lp;
    }

    private RelativeLayout.LayoutParams getUpperLayoutParams(int tier, int view_id, boolean is_labelled
                                                            ) {
        RelativeLayout.LayoutParams lp = getBaseLayoutParams(tier, view_id);
        if (is_labelled) {
            lp.setMargins(0, 8, 0, 0);
        } else {
            lp.addRule(RelativeLayout.ALIGN_LEFT, tier + getFirstColumnId());
            Integer topParentMatch = getUpperMatchParent(view_id);
            lp.addRule(RelativeLayout.ALIGN_BOTTOM,
                    topParentMatch + match_offset + BrNodeType.LOWER);
            lp.setMargins(0, -2, 0, 0);
        }
        return lp;
    }

    private RelativeLayout.LayoutParams getLowerLayoutParams(int tier, int view_id, boolean is_labelled
                                                            ) {
        RelativeLayout.LayoutParams lp = getBaseLayoutParams(tier, view_id);
        if (is_labelled) {
            lp.setMargins(0, 0, 0, 8);
        } else {
            lp.addRule(RelativeLayout.ALIGN_LEFT, tier + getFirstColumnId());
            Integer bottomParentMatch = getUpperMatchParent(view_id) + 1;
            lp.addRule(RelativeLayout.ABOVE,
                    bottomParentMatch + match_offset + BrNodeType.LOWER);
            lp.setMargins(0, 0, 0, -2);
        }
        return lp;
    }

    private void seed(List<SessionMember> sMembers) {
        Item_Match new_match;

        // seed the lowest tier
        for (Integer ii = 0; ii < n_leafs; ii += 2) {
            new_match = new Item_Match(ii / 2);
            new_match.setUpperMember(sMembers.get(ii));
            new_match.setUpperNodeType(BrNodeType.TIP);
            new_match.setUpperIsLabelled(true);

            new_match.setLowerMember(sMembers.get(ii + 1));
            if (sMembers.get(ii+1).getTeam() == null) {
                new_match.setLowerNodeType(BrNodeType.BYE);
            } else {
                new_match.setLowerNodeType(BrNodeType.TIP);
                new_match.setLowerIsLabelled(true);
            }
            matches.put(ii / 2, new_match);
        }

        // add the rest of the matches
        for (int ii = n_leafs / 2; ii < n_leafs; ii++) {
            new_match = new Item_Match(ii);
            matches.put(ii, new_match);
        }

        // last match is actually just the winner
        matches.get(matches.size() - 1).setLowerNodeType(BrNodeType.NA);

        byeByes();
    }

    private void seed() {
        List<Integer> idA = generateReseedMatchIds();

        // For every other tier, the order is swapped
        LinkedList<Integer> respawn_ids = new LinkedList<>();
        for (int ii = 0; ii < idA.size(); ii++) {
            respawn_ids.add(ii);
        }
        Collections.reverse(respawn_ids);

        int ii = (int) (Math.pow(2, (factorTwos(n_leafs) - 3) / 2));
        int idxA = 3 * ii;
        int idxB;

        if (ii > 0) {
            while (idxA < idA.size() - 1) {
                for (int jj = 0; jj < ii / 2; jj++) {
                    idxB = (idxA + ii - 1 - jj);
                    Collections.swap(respawn_ids, idxA + jj, idxB);
                }
                idxA += (9 * ii) / 4;
                ii /= 4;
            }
        }
        Collections.reverse(respawn_ids);

        int tier;
        Item_Match new_match;
        for (int match_id : idA) {
            tier = getTier(match_id);
            new_match = new Item_Match(match_id);

            if (tier % 2 == 0) {
                new_match.setUpperMember(new SessionMember(respawn_ids.pop()));
                new_match.setUpperNodeType(BrNodeType.RESPAWN);
                new_match.setUpperIsLabelled(true);
                if (tier == 0) {
                    new_match.setLowerNodeType(BrNodeType.BYE);
                }
            }
            matches.put(match_id, new_match);
        }

        // last match is actually just the winner
        matches.get(getLastMatchId()).setLowerNodeType(BrNodeType.NA);
        byeByes();
    }

    private LinkedList<Integer> generateRespawnIds() {
        return generateRespawnIds(n_leafs);
    }

    public static LinkedList<Integer> generateRespawnIds(int n_leafs) {
        int size = 2;
        for (int ii = 0; ii < (factorTwos(n_leafs) - 1) / 2; ii++) {
            size = size * 2 + 1;
        }

        LinkedList<Integer> respawn_ids = new LinkedList<>();
        for (int ii = 0; ii < size; ii++) {
            respawn_ids.add(ii);
        }
        Collections.reverse(respawn_ids);

        // For every other tier, the order is swapped
        int ii = (int) (Math.pow(2, (factorTwos(n_leafs) - 3) / 2));
        int idxA = 3 * ii;
        int idxB;

        if (ii > 0) {
            while (idxA < size - 1) {
                for (int jj = 0; jj < ii / 2; jj++) {
                    idxB = (idxA + ii - 1 - jj);
                    Collections.swap(respawn_ids, idxA + jj, idxB);
                }
                idxA += (9 * ii) / 4;
                ii /= 4;
            }
        }
        Collections.reverse(respawn_ids);
        return respawn_ids;
    }

    private List<Integer> generateReseedMatchIds() {
        return generateReseedMatchIds(n_leafs);
    }

    public static LinkedList<Integer> generateReseedMatchIds(int n_leafs) {
        List<Integer> idA = new ArrayList<>();
        idA.add(1);
        idA.add(1);
        List<Integer> idB = new ArrayList<>();
        idB.add(2);
        List<Integer> idC = new ArrayList<>();
        idC.add(1);
        idC.add(3);
        idA.addAll(idB);
        idA.addAll(idC);

        int last;
        for (int ii = 1; ii <= (factorTwos(n_leafs) - 3) / 2; ii++) {
            idB.addAll(idB);
            last = idB.size() - 1;
            idB.set(last, (int) (idB.get(last) + Math.pow(2, 2 * ii)));

            idC.addAll(idC);
            last = idC.size() - 1;
            idC.set(last, (int) (idC.get(last) + Math.pow(2, 2 * ii + 1)));

            idA.addAll(idB);
            idA.addAll(idC);
        }

        LinkedList<Integer> match_ids = new LinkedList<>();
        int match_id = -1;
        for (int ii = idA.size() - 1; ii >= 0; ii--) {
            match_id += idA.get(ii);
            match_ids.add(match_id);
        }
        return match_ids;
    }

//    private void preSeedFinal(int baseSize) {
//        int idxW = baseSize - 1;
//        int idxL = (int) (Math.pow(2, 2 * factorTwos(baseSize) - 1) - 1);
//        Log.i(LOGTAG, "winner base: " + idxW + ", loser base: " + idxL);
//
//        matchIds.addAll(Arrays.asList(0, 2, 3));
//        for (int idx = 0; idx < matchIds.size(); idx++) {
//            matchIds.set(idx, matchIds.get(idx) + match_offset);
//        }
//
//        sm1Idcs.addAll(Arrays.asList(idxW, -1, -1));
//        sm1Types.addAll(Arrays.asList(BrNodeType.RESPAWN, BrNodeType.UNSET, BrNodeType.UNSET));
//
//        sm2Idcs.addAll(Arrays.asList(idxL, idxW, -1));
//        sm2Types.addAll(Arrays.asList(BrNodeType.RESPAWN, BrNodeType.RESPAWN, BrNodeType.NA));
//
//        gameIds.addAll(Arrays.asList("", "", ""));
//    }

    private void byeByes() {
        List<Integer> bye_matches = new ArrayList<>();

        // promote players with a bye
        for (Item_Match match : matches.values()) {
            if (match.isBye()) {
                if (match.getUpperNodeType() == BrNodeType.BYE) {
                    promoteWinner(match, false);
                } else {
                    promoteWinner(match, true);
                }
                if (getChildIsUpper(match)) {
                    matches.get(getChildMatch(match)).setUpperIsLabelled(true);
                } else {
                    matches.get(getChildMatch(match)).setLowerIsLabelled(true);
                }
                bye_matches.add(match.getIdInSession());
            }
        }
        for (int key : bye_matches) {
            matches.remove(key);
        }
        //        logMatchList("Matches after removing byes: ");
    }

    public List<Game> matchMatches(List<Game> sGames) {
        Item_Match match;
        for (Game g : sGames) {
            if (matches.containsKey(g.getIdInSession())) {
                match = matches.get(g.getIdInSession());

                if (match.getGameId().equals("")) {
                    match.setGameId(g.getId());
                } else if (BuildConfig.DEBUG && !match.getGameId().equals(g.getId())) {
                    throw new AssertionError("Match id mismatch.");
                }

                if (g.getIsComplete()) {
                    if (g.getWinner().equals(match.getUpperTeam())) {
                        promoteWinner(matches.get(g.getIdInSession()), true);
                    } else if (g.getWinner().equals(match.getLowerTeam())) {
                        promoteWinner(matches.get(g.getIdInSession()), false);
                    }
                }
            }
        }

//        String gId;
//        int gsId;
//        int smASeed;
//        int smBSeed;
//
//        Iterator<Game> gIt = sGames.iterator();
//        while (gIt.hasNext()) {
//            Game g = gIt.next();
//            gId = g.getId();
//            gsId = g.getIdInSession();
//            //			Log.i(LOGTAG, "Game " + gId + ". "
//            //			+ g.getFirstPlayer().getFirstName() + " vs "
//            //			+ g.getSecondPlayer().getFirstName());
//
//            List<GameMember> gMembers = new ArrayList<>();
//            for (GameMember gm : g.getMembers()) {
//                gMembers.add(gm);
//            }
//            smASeed = smIdMap.get(gMembers.get(0).getTeam().getId());
//            smBSeed = smIdMap.get(gMembers.get(1).getTeam().getId());
//
//            if (gameIds.contains(gId)) {
//                gIt.remove();
//            } else {
//                int nMatches = length();
//                for (int idx = 0; idx < nMatches; idx++) {
//                    if (hasSm(idx, smASeed) && hasSm(idx, smBSeed) && gameIds.get(idx) == "") {
//                        Log.i(LOGTAG, "Matching game " + gId + " to match " + matchIds.get(idx));
//                        gameIds.set(idx, gId);
//                        gIt.remove();
//                        break;
//                    }
//                }
//            }
//
//            if (g.getIsComplete() && gameIds.contains(gId)) {
//                smASeed = smIdMap.get(g.getWinner().getId());
//                promoteWinner(gameIds.indexOf(g.getId()), smASeed);
//            }
//        }
        return sGames;
    }

    public Integer lowestViewId() {
        int match_id = n_leafs / 2 - 1 ;

        while (!matches.containsKey(match_id)) {
            match_id = getChildMatch(match_id);
        }
        return match_id + match_offset + BrNodeType.LOWER;
    }

    private void promoteWinner(Item_Match match, boolean promote_upper) {
        SessionMember winner;
        BrNodeType node_type;

        if (promote_upper) {
            winner = match.getUpperMember();
            node_type = match.getUpperNodeType();
        } else {
            winner = match.getLowerMember();
            node_type = match.getLowerNodeType();
        }

        Item_Match child_match = matches.get(getChildMatch(match));
        if (getChildIsUpper(match)) {
            child_match.setUpperNodeType(node_type);
            child_match.setUpperMember(winner);
        } else {
            child_match.setLowerNodeType(node_type);
            child_match.setLowerMember(winner);
        }
        match.setWinner(promote_upper);

        // if a player was promoted to play against self, as in finals
//        if (sm1Idcs.get(childIdx) == sm2Idcs.get(childIdx) && sm1Types.get(childIdx) == sm2Types.get(childIdx)) {
//            promoteSelf(childIdx);
//        }
    }

    public void refreshViews() {
        TextView tv;
        int drwColor;
        String drwString;

        for (Item_Match match : matches.values()) {
            drwString = "upper";
            drwColor = Color.LTGRAY;
            tv = (TextView) match.getUpperView();
            if (match.getUpperNodeType() != BrNodeType.UNSET
                    && match.getUpperNodeType() != BrNodeType.RESPAWN) {
                drwColor = match.getUpperColor();
            }
            if (match.getUpperNodeType() == BrNodeType.LOSS) {
                drwString += "_eliminated";
            }
            if (match.getUpperIsLabelled()) {
                if (match.getUpperNodeType() != BrNodeType.RESPAWN) {
                    tv.setText(match.getUpperSeedName());
                }
                drwString += "_labeled";
//                if (smLost(sm1Idcs.get(idx))) {
//                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                } else {
//                    tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                }
            }
            if (match.getLowerNodeType() == BrNodeType.NA) {
                drwString = "endpoint";
            }
            tv.setBackgroundResource(BrDrawable.map.get(drwString));
            tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);


            if (match.getLowerNodeType() != BrNodeType.NA) {
                drwString = "lower";
                drwColor = Color.LTGRAY;
                tv = (TextView) match.getLowerView();

                if (match.getLowerNodeType() != BrNodeType.UNSET
                        && match.getLowerNodeType() != BrNodeType.RESPAWN) {
                    drwColor = match.getLowerColor();
                }
                if (match.getLowerNodeType() == BrNodeType.LOSS) {
                    drwString += "_eliminated";
                }
                if (match.getLowerIsLabelled()) {
                    if (match.getLowerNodeType() != BrNodeType.RESPAWN) {
                        tv.setText(match.getLowerSeedName());
                    }
                    drwString += "_labeled";
//                    if (smLost(sm2Idcs.get(idx))) {
//                        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                    } else {
//                        tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                    }
                }
                tv.setBackgroundResource(BrDrawable.map.get(drwString));
                tv.getBackground().setColorFilter(drwColor, Mode.MULTIPLY);
            }
        }
    }

    private int getTier(Item_Match match) {
        return getTier(match.getIdInSession(), n_leafs);
    }

    private int getTier(int match_id) {
        return getTier(match_id, n_leafs);
    }

    private int getHighestTier() {
        return getTier(n_leafs - 1, n_leafs);
    }

    public static int getTier(int match_id, int n_leafs) {
        return ((Double) Math.floor(-Math.log(1 - ((double) match_id) / n_leafs) / Math.log(2)))
                .intValue();
    }

    private int getTopMatchOfTier(int tier) {
        return (int) (n_leafs * (1 - Math.pow(2, -tier)));
    }

    private int getUpperMatchParent(int view_id) {
        // can take a view id or a match id
        Integer match_id = view_id % BrNodeType.MOD;
        Integer tier = getTier(match_id);
        Integer topOfTier = getTopMatchOfTier(tier);
        Integer topOfPrevTier = getTopMatchOfTier(tier - 1);

        return topOfPrevTier + 2 * (match_id - topOfTier);
    }

    private int getChildMatch(Item_Match match) {
        return getChildMatch(match.getIdInSession());
    }

    private int getChildMatch(int match_id) {
        int tier = getTier(match_id);
        int top_of_tier = getTopMatchOfTier(tier);
        int top_next_tier = getTopMatchOfTier(tier + 1);

        return top_next_tier + (match_id - top_of_tier) / 2;
    }

    private boolean getChildIsUpper(Item_Match match) {
        return getChildIsUpper(match.getIdInSession());
    }

    private boolean getChildIsUpper(int match_id) {
        return match_id % 2 == 0;
    }

    public int findViewAboveId(int view_id) {
        int match_id = view_id % BrNodeType.MOD - match_offset;

        // default place view under the headers
        int viewAboveId = getFirstColumnId();

        if (!isUpperView(view_id)) {
            // lower views always go under their respective upper view
            viewAboveId = match_id + BrNodeType.UPPER;
        } else {
            Integer baseId = match_id;
            if (getTier(match_id) > 0) {
                baseId = getUpperMatchParent(match_id);
            }
            if (matches.containsKey(baseId) && baseId != match_id) {
                // if there is an upper parent match, place it under the parents upper view
                viewAboveId = baseId + BrNodeType.UPPER;
            } else {
                // view is in the first tier or its upper parent is gone
                while (getTier(baseId) > 0) {
                    // trace upper parents back to the base tier
                    baseId = getUpperMatchParent(baseId);
                }
                if (baseId > 0) {
                    // view isn't the top view of the bracket
                    baseId -= 1; // go to the previous match
                    if (matches.containsKey(baseId)) {
                        viewAboveId = baseId + BrNodeType.LOWER;
                    } else {
                        while (!matches.containsKey(baseId)) {
                            baseId = getChildMatch(baseId);
                        }
                        viewAboveId = baseId + BrNodeType.UPPER;
                    }
                }
            }
        }

        Log.i(LOGTAG, "viewId: " + view_id + " placed below " + viewAboveId);
        return viewAboveId + match_offset;
    }

    public static boolean isUpperView(int viewId) {
        assert viewId >= 1000;
        return viewId < 2000;
    }

    // find n such that 2**n >= p
    public static int factorTwos(int p) {
        Integer n = 0;
        while (Math.pow(2, n) < p) {
            n++;
        }
        return n;
    }
}
