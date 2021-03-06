package info.andersonpa.pocketleague.backend;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LeagueTableTest {

    @Test
    public void testMemberPositionsToMatchId() throws Exception {
        // check upper triangle
        assertEquals(0, LeagueTable.memberPositionsToMatchId(new int[]{0,0}, 5));
        assertEquals(4, LeagueTable.memberPositionsToMatchId(new int[]{0,4}, 5));
        assertEquals(9, LeagueTable.memberPositionsToMatchId(new int[]{1,4}, 5));
        assertEquals(7, LeagueTable.memberPositionsToMatchId(new int[]{1,3}, 4));

        // check lower triangle
        assertEquals(0, LeagueTable.memberPositionsToMatchId(new int[]{0,0}, 5));
        assertEquals(4, LeagueTable.memberPositionsToMatchId(new int[]{0,4}, 5));
        assertEquals(9, LeagueTable.memberPositionsToMatchId(new int[]{1,4}, 5));
        assertEquals(7, LeagueTable.memberPositionsToMatchId(new int[]{1,3}, 4));

        // out of bounds indices
        try {
            LeagueTable.memberPositionsToMatchId(new int[]{5,0}, 5);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // success
        }

        try {
            LeagueTable.memberPositionsToMatchId(new int[]{0,5}, 5);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // success
        }
    }
}