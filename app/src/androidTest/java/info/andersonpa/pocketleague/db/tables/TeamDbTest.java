package info.andersonpa.pocketleague.db.tables;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TeamDbTest extends DbBaseTestCase {
    private Player p1;
    private Player p2;
    private Player p3;

    private Team t1;
    private Team t2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        p1 = new Player(database, "Bob");
        p1.update();
        p2 = new Player(database, "Sue");
        p2.update();
        p3 = new Player(database, "Tom", "Doc", "Holiday", true, false, true, false, 44, 12,
                Color.BLACK, true, null);
        p3.update();

        t1 = new Team(database, "Test Team A", Arrays.asList(p1, p2));
        t1.update();

        t2 = new Team(database, "Test Team B", Arrays.asList(p2, p3, p1), Color.GREEN, true);
        t2.update();
    }

    @Test
    public void testConstructor() throws Exception {
        Team team = new Team("No doc team", null);
        assertNull(team.getId());

        team = new Team(database, "Doc team", null);
        assertNotNull(team.getId());
    }

    @Test
    public void testGetFromId() throws Exception {
        Team t = Team.getFromId(database, t1.getId());

        assertNotNull(t);
        assertEquals(t1.getId(), t.getId());
        assertEquals(t1.document.getCurrentRevisionId(), t.document.getCurrentRevisionId());
    }

    @Test
    public void testFindByName() throws Exception {
        Team t = Team.findByName(database, "Test Other Team");
        assertNull(t);

        t = Team.findByName(database, "Test Team A");
        assertNotNull(t);
        assertEquals(t1.getId(), t.getId());
        assertEquals(t1.document.getCurrentRevisionId(), t.document.getCurrentRevisionId());
    }

    @Test
    public void testGetAllTeams() throws Exception {
        List<Team> all_teams = Team.getAllTeams(database);
        assertEquals(5, all_teams.size());
    }

    @Test
    public void testGetTeams() throws Exception {
        // Players shouldn't show up here, since this is used for the teams fragment
        // and that only shows teams with size > 1. Players have their own fragment.
        List<Team> all_teams = Team.getTeams(database, true, true);
        assertEquals(1, all_teams.size());
    }

    @Test
    public void testGetMembers() throws Exception {
        List<Player> members = t1.getMembers();
        assertEquals(2, members.size());
        assertEquals("Bob", members.get(0).getName());
        assertEquals(p1.getId(), members.get(0).getId());
        assertEquals("Sue", members.get(1).getName());
        assertEquals(p2.getId(), members.get(1).getId());

        members = t2.getMembers();
        assertEquals(3, members.size());
        assertEquals("Sue", members.get(0).getName());
        assertEquals(p2.getId(), members.get(0).getId());
        assertEquals("Tom", members.get(1).getName());
        assertEquals(p3.getId(), members.get(1).getId());
        assertEquals("Bob", members.get(2).getName());
        assertEquals(p1.getId(), members.get(2).getId());

        members = p1.getMembers();
        assertEquals(1, members.size());
        assertEquals("Bob", members.get(0).getName());
    }

    @Test
    public void testGetSize() throws Exception {
        assertEquals(2, t1.getSize());
        assertEquals(3, t2.getSize());
        assertEquals(1, p2.getSize());
    }

    @Test
    public void testExists() throws Exception {
        assertTrue(t1.exists());
        assertTrue(t2.exists());
        Team t3 = new Team(database, "Test Team C", Arrays.asList(p1, p2));
        assertFalse(t3.exists());

        assertTrue(p1.exists());
        assertTrue(p2.exists());
        Player p4 = new Player(database, "Scooby");
        assertFalse(p4.exists());
    }

    @Test
    public void testStaticExists() throws Exception {
        assertTrue(Team.exists(database, t1.getName()));
        assertTrue(Team.exists(database, t2.getName()));
        assertFalse(Team.exists(database, "Nonexistent Team Name"));
    }
}
